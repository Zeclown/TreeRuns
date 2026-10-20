package com.wolfhausgames.treeruns.plan;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.wolfhausgames.treeruns.TreeRunsPlugin;
import com.wolfhausgames.treeruns.data.Patch;
import com.wolfhausgames.treeruns.data.PatchKind;
import com.wolfhausgames.treeruns.data.TreeType;
import com.wolfhausgames.treeruns.state.UnlockTracker;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

/**
 * The player's run plan, saved per account. Changes are made from the Swing thread.
 */
@Slf4j
@Singleton
public class PlanManager
{
	private static final String PLAN_KEY = "plan";
	private static final Type PLAN_TYPE = new TypeToken<Map<PatchKind, CategoryPlan>>()
	{
	}.getType();

	private final ConfigManager configManager;
	private final UnlockTracker unlockTracker;
	private final Gson gson;

	private Map<PatchKind, CategoryPlan> categories = new EnumMap<>(PatchKind.class);

	@Inject
	PlanManager(ConfigManager configManager, UnlockTracker unlockTracker, Gson gson)
	{
		this.configManager = configManager;
		this.unlockTracker = unlockTracker;
		this.gson = gson;
	}

	/** Reloads the plan for the current account. */
	public synchronized void load()
	{
		categories = new EnumMap<>(PatchKind.class);
		final String json = configManager.getRSProfileConfiguration(TreeRunsPlugin.CONFIG_GROUP, PLAN_KEY);
		if (json == null)
		{
			return;
		}

		try
		{
			final Map<PatchKind, CategoryPlan> saved = gson.fromJson(json, PLAN_TYPE);
			if (saved != null)
			{
				saved.forEach((kind, plan) ->
				{
					// Unknown kinds deserialize as null keys
					if (kind != null && plan != null)
					{
						categories.put(kind, plan);
					}
				});
			}
		}
		catch (JsonSyntaxException e)
		{
			log.warn("Discarding unreadable tree run plan", e);
		}
	}

	/**
	 * The plan for a category. A category the player hasn't touched starts with the best tree
	 * they can plant, so call this only once the Farming level is known.
	 */
	public synchronized CategoryPlan get(PatchKind kind)
	{
		final CategoryPlan plan = categories.get(kind);
		if (plan != null)
		{
			return plan;
		}

		final CategoryPlan defaults = new CategoryPlan(bestPlantable(kind));
		// Don't remember a default chosen before the Farming level is known
		if (unlockTracker.isLoaded())
		{
			categories.put(kind, defaults);
		}
		return defaults;
	}

	/**
	 * How many spirit trees the player may have planted at once. Boosts count.
	 */
	public int getSpiritTreeLimit()
	{
		final int level = Math.max(unlockTracker.getFarmingLevel(), unlockTracker.getBoostedFarmingLevel());
		if (level >= 99)
		{
			return Integer.MAX_VALUE;
		}
		if (level >= 96)
		{
			return 4;
		}
		if (level >= 93)
		{
			return 3;
		}
		if (level >= 88)
		{
			return 2;
		}
		return level >= 83 ? 1 : 0;
	}

	public synchronized void setTree(PatchKind kind, @Nullable TreeType tree)
	{
		get(kind).setTree(tree);
		save();
	}

	public synchronized void setProtect(PatchKind kind, boolean protect)
	{
		get(kind).setProtect(protect);
		save();
	}

	public synchronized void setRemove(PatchKind kind, boolean remove)
	{
		get(kind).setRemove(remove);
		save();
	}

	public synchronized void setIncluded(Patch patch, boolean included)
	{
		final CategoryPlan plan = get(patch.getKind());
		if (patch.getKind() == PatchKind.SPIRIT)
		{
			// Untick spirit patches the limit is hiding first, so freeing a slot doesn't silently fill it
			final List<Patch> hidden = new ArrayList<>();
			for (Patch other : Patch.forKind(PatchKind.SPIRIT))
			{
				if (other != patch && isTickedAndUsable(other) && !isInRun(other))
				{
					hidden.add(other);
				}
			}
			hidden.forEach(other -> plan.setIncluded(other, false));
		}
		plan.setIncluded(patch, included);
		save();
	}

	public synchronized void setCollapsed(PatchKind kind, boolean collapsed)
	{
		get(kind).setCollapsed(collapsed);
		save();
	}

	/**
	 * Whether a patch is part of the run: its category has a tree, it is unlocked, and it is ticked.
	 * Ticked spirit patches beyond the player's spirit tree limit are left out, in patch order.
	 */
	public synchronized boolean isInRun(Patch patch)
	{
		if (!isTickedAndUsable(patch))
		{
			return false;
		}
		if (patch.getKind() != PatchKind.SPIRIT)
		{
			return true;
		}

		int before = 0;
		for (Patch other : Patch.forKind(PatchKind.SPIRIT))
		{
			if (other == patch)
			{
				break;
			}
			if (isTickedAndUsable(other))
			{
				before++;
			}
		}
		return before < getSpiritTreeLimit();
	}

	/** How many patches of a kind are in the run. */
	public synchronized int countInRun(PatchKind kind)
	{
		int count = 0;
		for (Patch patch : Patch.forKind(kind))
		{
			if (isInRun(patch))
			{
				count++;
			}
		}
		return count;
	}

	private boolean isTickedAndUsable(Patch patch)
	{
		final CategoryPlan plan = get(patch.getKind());
		return plan.getTree() != null && plan.isIncluded(patch) && unlockTracker.isUnlocked(patch);
	}

	@Nullable
	private TreeType bestPlantable(PatchKind kind)
	{
		final List<TreeType> trees = TreeType.forKind(kind);
		for (int i = trees.size() - 1; i >= 0; i--)
		{
			if (unlockTracker.canPlant(trees.get(i)))
			{
				return trees.get(i);
			}
		}
		return null;
	}

	private void save()
	{
		configManager.setRSProfileConfiguration(TreeRunsPlugin.CONFIG_GROUP, PLAN_KEY, gson.toJson(categories, PLAN_TYPE));
	}
}
