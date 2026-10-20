/*
 * Gardener payment detection is derived from RuneLite's Time Tracking plugin
 * (BSD 2-Clause). See THIRD_PARTY_NOTICES.
 */
package com.wolfhausgames.treeruns.run;

import com.wolfhausgames.treeruns.data.ItemStack;
import com.wolfhausgames.treeruns.data.Patch;
import com.wolfhausgames.treeruns.data.PatchSite;
import com.wolfhausgames.treeruns.data.Tools;
import com.wolfhausgames.treeruns.data.TreeState;
import com.wolfhausgames.treeruns.data.TreeType;
import com.wolfhausgames.treeruns.plan.CategoryPlan;
import com.wolfhausgames.treeruns.plan.PlanManager;
import com.wolfhausgames.treeruns.state.ItemNames;
import com.wolfhausgames.treeruns.state.PatchStatus;
import com.wolfhausgames.treeruns.state.PatchTracker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetModelType;

/**
 * A run in progress: which stop is current, what to do there, and when it's finished.
 * Driven from the client thread; the panel reads it from the Swing thread.
 */
@Slf4j
@Singleton
public class RunSession
{
	private static final String FALADOR_DIARY_TEXT = "The gardener protects your tree for you, free of charge, as a token of gratitude for completing the Falador elite diary.";

	private final Client client;
	private final PlanManager planManager;
	private final PatchTracker patchTracker;
	private final ItemNames itemNames;

	private volatile List<Patch> stops = Collections.emptyList();
	private final Map<Patch, Progress> progress = new EnumMap<>(Patch.class);
	private volatile int index;
	private volatile boolean active;
	/** The current stop's steps as of the last tick, for overlays and the panel. */
	private volatile List<RunStep> steps = Collections.emptyList();

	@Inject
	RunSession(Client client, PlanManager planManager, PatchTracker patchTracker, ItemNames itemNames)
	{
		this.client = client;
		this.planManager = planManager;
		this.patchTracker = patchTracker;
		this.itemNames = itemNames;
	}

	public synchronized void start(List<Patch> runStops)
	{
		stops = List.copyOf(runStops);
		progress.clear();
		stops.forEach(patch -> progress.put(patch, new Progress()));
		index = 0;
		active = !stops.isEmpty();
		steps = Collections.emptyList();
	}

	public synchronized void end()
	{
		active = false;
		stops = Collections.emptyList();
		progress.clear();
		steps = Collections.emptyList();
	}

	public boolean isActive()
	{
		return active;
	}

	public List<Patch> getStops()
	{
		return stops;
	}

	public int getIndex()
	{
		return index;
	}

	public boolean isComplete()
	{
		return active && index >= stops.size();
	}

	@Nullable
	public Patch getCurrent()
	{
		final List<Patch> current = stops;
		final int i = index;
		return active && i < current.size() ? current.get(i) : null;
	}

	public synchronized boolean isDone(Patch patch)
	{
		final Progress p = progress.get(patch);
		return p != null && isDone(patch, p);
	}

	public synchronized boolean isSkipped(Patch patch)
	{
		final Progress p = progress.get(patch);
		return p != null && p.skipped;
	}

	/** Moves on from the current stop, marking it skipped unless it was already finished. */
	public synchronized void next()
	{
		final Patch current = getCurrent();
		if (current != null)
		{
			final Progress p = progress.get(current);
			p.skipped = !isDone(current, p);
			p.forcedBack = false;
			advance();
		}
	}

	/** Goes back to the previous stop, even if it was finished. */
	public synchronized void back()
	{
		if (!active || index == 0)
		{
			return;
		}
		index--;
		final Progress p = progress.get(stops.get(index));
		p.skipped = false;
		p.forcedBack = true;
	}

	/**
	 * Checks the current stop's patch and the chat box. Must be called on the client thread
	 * after the patch tracker has been updated.
	 *
	 * @return whether the current stop or its steps changed
	 */
	public synchronized boolean onTick(WorldPoint playerLocation)
	{
		final int before = index;
		final List<RunStep> beforeSteps = steps;
		update(playerLocation);
		steps = computeSteps(playerLocation);
		return index != before || !steps.equals(beforeSteps);
	}

	private void update(WorldPoint playerLocation)
	{
		final Patch current = getCurrent();
		if (current == null)
		{
			return;
		}

		detectProtection();

		final Progress p = progress.get(current);
		if (current.isTransmittedAt(playerLocation))
		{
			final TreeState state = patchTracker.getStatus(current).getState().getState();
			if (state == TreeState.GROWING)
			{
				if (p.sawUnplanted && !p.sawDiseased)
				{
					p.planted = true;
				}
				else if (!p.planted)
				{
					// Growing since we arrived: someone planted it before the run, or it recovered from disease
					p.alreadyGrowing = true;
				}
			}
			else if (state != TreeState.UNKNOWN)
			{
				p.sawUnplanted = true;
				p.sawDiseased |= state == TreeState.DISEASED;
			}
		}

		if (isDone(current, p) && !p.forcedBack)
		{
			advance();
		}
	}

	public void onGameMessage(String message)
	{
		if (FALADOR_DIARY_TEXT.equals(message))
		{
			markProtected(Patch.FALADOR_TREE);
		}
	}

	/**
	 * The first step that involves something in the scene; travelling has nothing to point at yet.
	 * Null when there is none.
	 */
	@Nullable
	public RunStep getCurrentStep()
	{
		for (RunStep step : steps)
		{
			if (step.getTarget() != RunStep.Target.NONE)
			{
				return step;
			}
		}
		return null;
	}

	/** What to do at the current stop, first step first, as of the last tick. Empty when nothing is left. */
	public List<RunStep> getSteps()
	{
		return steps;
	}

	private List<RunStep> computeSteps(@Nullable WorldPoint playerLocation)
	{
		final Patch patch = getCurrent();
		if (patch == null)
		{
			return Collections.emptyList();
		}

		final List<RunStep> steps = new ArrayList<>();
		final Progress p = progress.get(patch);
		final CategoryPlan plan = planManager.get(patch.getKind());
		final TreeType tree = plan.getTree();
		final boolean near = playerLocation != null && patch.isTransmittedAt(playerLocation);
		final PatchStatus status = patchTracker.getStatus(patch);

		if (!near)
		{
			steps.add(new RunStep("Travel to " + patch.getLocationName(), RunStep.Target.NONE));
		}

		if (!p.planted && !p.alreadyGrowing)
		{
			switch (status.getState().getState())
			{
				case READY:
					steps.add(new RunStep("Check the tree's health", RunStep.Target.PATCH));
					steps.add(removalStep(patch, plan));
					break;
				case CHECKED:
					steps.add(removalStep(patch, plan));
					break;
				case STUMP:
					steps.add(new RunStep("Dig up the stump", RunStep.Target.PATCH, Tools.SPADE));
					break;
				case DEAD:
					steps.add(new RunStep("Clear the dead tree with a spade", RunStep.Target.PATCH, Tools.SPADE));
					break;
				case DISEASED:
					// Pruning saves the tree; there's nothing to plant
					steps.add(new RunStep("Prune the tree with secateurs", RunStep.Target.PATCH, Tools.SECATEURS));
					return steps;
				case UNKNOWN:
					steps.add(new RunStep("Look at the patch", RunStep.Target.PATCH));
					break;
				default:
					break;
			}
			if (tree != null)
			{
				steps.add(new RunStep("Plant " + itemNames.get(tree.getSaplingItemId()).toLowerCase(),
					RunStep.Target.PATCH, Set.of(tree.getSaplingItemId())));
			}
		}

		if (!p.alreadyGrowing && needsProtection(patch) && !p.protectedPaid && tree != null)
		{
			steps.add(new RunStep("Pay the gardener " + tree.getProtectionPayment().stream()
				.map(itemNames::describe).collect(Collectors.joining(", ")), RunStep.Target.GARDENER,
				tree.getProtectionPayment().stream().map(ItemStack::getItemId).collect(Collectors.toSet())));
		}
		return steps;
	}

	private RunStep removalStep(Patch patch, CategoryPlan plan)
	{
		if (patch.isRemovable() && plan.isRemove())
		{
			return new RunStep("Pay the gardener " + String.format("%,d", patch.getRemovalCost())
				+ " coins to remove the tree", RunStep.Target.GARDENER, Set.of(ItemID.COINS));
		}

		switch (patch.getKind())
		{
			case CALQUAT:
			case SPIRIT:
				return new RunStep("Dig up the tree with a spade", RunStep.Target.PATCH, Tools.SPADE);
			case CELASTRUS:
				return new RunStep("Harvest the bark, then chop the tree down", RunStep.Target.PATCH, Tools.AXES);
			default:
				return new RunStep("Chop the tree down", RunStep.Target.PATCH, Tools.AXES);
		}
	}

	private boolean needsProtection(Patch patch)
	{
		final CategoryPlan plan = planManager.get(patch.getKind());
		final TreeType tree = plan.getTree();
		return tree != null && tree.isProtectable() && plan.isProtect();
	}

	private boolean isDone(Patch patch, Progress p)
	{
		return p.skipped || p.alreadyGrowing || (p.planted && (p.protectedPaid || !needsProtection(patch)));
	}

	private void advance()
	{
		do
		{
			index++;
		}
		while (index < stops.size() && isDone(stops.get(index), progress.get(stops.get(index))));
		log.debug("Tree run moved to stop {} of {}", index + 1, stops.size());
	}

	/**
	 * Gardeners confirm payment in dialogue rather than a game message, so look for their reply.
	 * The same check RuneLite's Time Tracking plugin uses, but matched loosely since line breaks vary.
	 */
	private void detectProtection()
	{
		final Widget text = client.getWidget(InterfaceID.ChatLeft.TEXT);
		final Widget head = client.getWidget(InterfaceID.ChatLeft.HEAD);
		if (text == null || head == null || head.getModelType() != WidgetModelType.NPC_CHATHEAD)
		{
			return;
		}

		final String dialogue = text.getText().replace("<br>", " ");
		final boolean paid = dialogue.contains("Leave it with me")
			&& (dialogue.contains("make sure that patch grows") || dialogue.contains("make sure that tree grows")
			|| dialogue.contains("look after that nursery"));
		final boolean alreadyPaid = dialogue.contains("I'm already looking after that patch");
		if (!paid && !alreadyPaid)
		{
			return;
		}

		final Patch current = getCurrent();
		if (current != null && PatchSite.of(current).isGardener(head.getModelId()))
		{
			markProtected(current);
		}
	}

	private synchronized void markProtected(Patch patch)
	{
		final Progress p = progress.get(patch);
		if (p != null && !p.protectedPaid)
		{
			log.debug("Protection paid for {}", patch);
			p.protectedPaid = true;
		}
	}

	private static final class Progress
	{
		/** The patch was seen without a growing tree, so a growing tree afterwards was planted now. */
		boolean sawUnplanted;
		boolean sawDiseased;
		boolean planted;
		boolean alreadyGrowing;
		boolean protectedPaid;
		boolean skipped;
		/** The player went back to this stop; stay here until they move on. */
		boolean forcedBack;
	}
}
