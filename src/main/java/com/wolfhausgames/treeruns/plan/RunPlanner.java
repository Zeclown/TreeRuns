package com.wolfhausgames.treeruns.plan;

import com.wolfhausgames.treeruns.data.ItemStack;
import com.wolfhausgames.treeruns.data.Patch;
import com.wolfhausgames.treeruns.data.PatchKind;
import com.wolfhausgames.treeruns.data.TreeState;
import com.wolfhausgames.treeruns.data.TreeType;
import com.wolfhausgames.treeruns.data.Tools;
import com.wolfhausgames.treeruns.state.ItemNames;
import com.wolfhausgames.treeruns.state.ItemTracker;
import com.wolfhausgames.treeruns.state.PatchTracker;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.gameval.ItemID;

/**
 * Turns the plan into this run's stops and the items they need.
 */
@Singleton
public class RunPlanner
{
	private final PlanManager planManager;
	private final PatchTracker patchTracker;
	private final ItemTracker itemTracker;
	private final ItemNames itemNames;

	@Inject
	RunPlanner(PlanManager planManager, PatchTracker patchTracker, ItemTracker itemTracker, ItemNames itemNames)
	{
		this.planManager = planManager;
		this.patchTracker = patchTracker;
		this.itemTracker = itemTracker;
		this.itemNames = itemNames;
	}

	/** Patches to visit, in route order. Trees that are still growing are left for a later run. */
	public List<Patch> getStops()
	{
		final List<Patch> stops = new ArrayList<>();
		for (Patch patch : planManager.getRunRoute())
		{
			if (!isGrowing(patch))
			{
				stops.add(patch);
			}
		}
		return stops;
	}

	/** Patches in the plan that are skipped this time because their tree is still growing (or diseased). */
	public List<Patch> getGrowing()
	{
		final List<Patch> growing = new ArrayList<>();
		for (Patch patch : planManager.getRunRoute())
		{
			if (isGrowing(patch))
			{
				growing.add(patch);
			}
		}
		return growing;
	}

	public List<ShoppingLine> getShoppingList()
	{
		final List<Patch> stops = getStops();
		// Keyed by name so payments shared between trees (e.g. coconuts) add up on one line
		final Map<String, Need> needs = new LinkedHashMap<>();
		int coins = 0;
		boolean axe = false;

		for (Patch patch : stops)
		{
			final CategoryPlan plan = planManager.get(patch.getKind());
			final TreeType tree = plan.getTree();
			if (tree == null)
			{
				continue;
			}

			add(needs, itemNames.get(tree.getSaplingItemId()), Set.of(tree.getSaplingItemId()), 1);
			if (plan.isProtect())
			{
				for (ItemStack payment : tree.getProtectionPayment())
				{
					add(needs, itemNames.get(payment.getItemId()), Set.of(payment.getItemId()), payment.getQuantity());
				}
			}

			// An empty patch has nothing to remove
			final boolean hasTree = patchTracker.getStatus(patch).getState().getState() != TreeState.EMPTY;
			if (hasTree && patch.isRemovable() && plan.isRemove())
			{
				coins += patch.getRemovalCost();
			}
			else if (hasTree && Tools.needsAxeToClear(patch.getKind()))
			{
				axe = true;
			}
			// Celastrus bark and crystal shards are harvested with an axe even when the gardener clears the patch
			if (hasTree && (patch.getKind() == PatchKind.CELASTRUS || patch.getKind() == PatchKind.CRYSTAL))
			{
				axe = true;
			}
		}

		final List<ShoppingLine> lines = new ArrayList<>();
		if (stops.isEmpty())
		{
			return lines;
		}

		for (Map.Entry<String, Need> entry : needs.entrySet())
		{
			lines.add(line(entry.getKey(), entry.getValue().itemIds, entry.getValue().quantity, false));
		}
		if (coins > 0)
		{
			lines.add(line("Coins", Set.of(ItemID.COINS), coins, false));
		}
		lines.add(line("Spade", Tools.SPADE, 1, false));
		if (axe)
		{
			lines.add(line("Any axe", Tools.AXES, 1, false));
		}
		lines.add(line("Rake", Tools.RAKE, 1, true));
		lines.add(line("Secateurs", Tools.SECATEURS, 1, true));
		return lines;
	}

	private boolean isGrowing(Patch patch)
	{
		final TreeState state = patchTracker.getStatus(patch).getState().getState();
		return state == TreeState.GROWING || state == TreeState.DISEASED;
	}

	private ShoppingLine line(String name, Set<Integer> itemIds, int needed, boolean optional)
	{
		int carried = 0;
		int banked = 0;
		for (int itemId : itemIds)
		{
			carried += itemTracker.getCarriedCount(itemId);
			banked += itemTracker.getBankCount(itemId);
		}
		return new ShoppingLine(name, itemIds, needed, carried, banked, optional);
	}

	private static void add(Map<String, Need> needs, String name, Set<Integer> itemIds, int quantity)
	{
		needs.computeIfAbsent(name, k -> new Need(itemIds)).quantity += quantity;
	}

	private static final class Need
	{
		private final Set<Integer> itemIds;
		private int quantity;

		private Need(Set<Integer> itemIds)
		{
			this.itemIds = itemIds;
		}
	}
}
