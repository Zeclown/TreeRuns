package com.wolfhausgames.treeruns.state;

import com.wolfhausgames.treeruns.data.Patch;
import com.wolfhausgames.treeruns.data.Requirement;
import com.wolfhausgames.treeruns.data.TreeType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Skill;

/**
 * Which patches and trees the player can currently use. Quest and skill state can only be read on
 * the client thread, so {@link #refresh()} takes a snapshot that other threads read.
 */
@Singleton
public class UnlockTracker
{
	private final Client client;

	private volatile Map<Patch, List<String>> missingByPatch = Collections.emptyMap();
	@Getter
	private volatile int farmingLevel;
	@Getter
	private volatile int boostedFarmingLevel;

	@Inject
	UnlockTracker(Client client)
	{
		this.client = client;
	}

	/** Must be called on the client thread. */
	public void refresh()
	{
		final Map<Patch, List<String>> missing = new EnumMap<>(Patch.class);
		for (Patch patch : Patch.values())
		{
			final List<String> unmet = new ArrayList<>();
			for (Requirement requirement : patch.getRequirements())
			{
				if (!requirement.isMet(client))
				{
					unmet.add(requirement.getDescription());
				}
			}
			missing.put(patch, Collections.unmodifiableList(unmet));
		}
		missingByPatch = missing;
		farmingLevel = client.getRealSkillLevel(Skill.FARMING);
		boostedFarmingLevel = client.getBoostedSkillLevel(Skill.FARMING);
	}

	public void clear()
	{
		missingByPatch = Collections.emptyMap();
		farmingLevel = 0;
		boostedFarmingLevel = 0;
	}

	/** Whether this snapshot has been taken since login. */
	public boolean isLoaded()
	{
		return farmingLevel > 0;
	}

	public boolean isUnlocked(Patch patch)
	{
		return getMissingRequirements(patch).isEmpty();
	}

	/** Descriptions of the requirements the player hasn't met, or empty if the patch is usable. */
	public List<String> getMissingRequirements(Patch patch)
	{
		return missingByPatch.getOrDefault(patch, Collections.emptyList());
	}

	/** Whether the player can plant this tree, counting a Farming boost. */
	public boolean canPlant(TreeType tree)
	{
		return Math.max(farmingLevel, boostedFarmingLevel) >= tree.getFarmingLevel();
	}
}
