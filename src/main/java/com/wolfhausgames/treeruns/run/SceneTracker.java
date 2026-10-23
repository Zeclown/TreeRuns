/*
 * Keeping gardeners across scene loads is adapted from Farm Run Autopilot (BSD 2-Clause).
 * See THIRD_PARTY_NOTICES.
 */
package com.wolfhausgames.treeruns.run;

import com.wolfhausgames.treeruns.data.Patch;
import com.wolfhausgames.treeruns.data.PatchSite;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.WorldView;

/**
 * Keeps track of the patch objects and gardeners currently loaded in the scene, from spawn events.
 * <p>
 * Patch objects are found by the farming varbit they're bound to plus their location, so no
 * object IDs need to be listed.
 */
@Singleton
public class SceneTracker
{
	/** Regions that contain a tree-run patch; objects elsewhere are skipped without a definition lookup. */
	private static final Set<Integer> PATCH_REGIONS = new HashSet<>();
	private static final Set<Integer> GARDENER_IDS = new HashSet<>();

	static
	{
		for (Patch patch : Patch.values())
		{
			for (int region : patch.getRegionIds())
			{
				PATCH_REGIONS.add(region);
			}
			for (int npcId : PatchSite.of(patch).getGardenerNpcIds())
			{
				GARDENER_IDS.add(npcId);
			}
		}
	}

	private final Client client;
	private final Map<Patch, GameObject> patchObjects = new EnumMap<>(Patch.class);
	private final List<NPC> gardeners = new ArrayList<>();

	@Inject
	SceneTracker(Client client)
	{
		this.client = client;
	}

	@Nullable
	public GameObject getPatchObject(Patch patch)
	{
		return patchObjects.get(patch);
	}

	/** Loaded gardeners who tend this patch. */
	public List<NPC> getGardeners(Patch patch)
	{
		final PatchSite site = PatchSite.of(patch);
		final List<NPC> result = new ArrayList<>();
		for (NPC npc : gardeners)
		{
			if (site.isGardener(npc.getId()))
			{
				result.add(npc);
			}
		}
		return result;
	}

	public void clear()
	{
		clearObjects();
		gardeners.clear();
	}

	/**
	 * Forgets the patch objects, which spawn again when a scene loads. Gardeners still in range
	 * don't spawn again, so they're kept.
	 */
	public void clearObjects()
	{
		patchObjects.clear();
	}

	/** Picks up everything already loaded, for when the plugin starts while logged in. */
	public void scan()
	{
		clear();
		final WorldView worldView = client.getTopLevelWorldView();
		if (worldView == null)
		{
			return;
		}

		final Scene scene = worldView.getScene();
		for (Tile[][] plane : scene.getTiles())
		{
			for (Tile[] row : plane)
			{
				for (Tile tile : row)
				{
					if (tile == null)
					{
						continue;
					}
					for (GameObject object : tile.getGameObjects())
					{
						if (object != null)
						{
							onObjectSpawned(object);
						}
					}
				}
			}
		}
		for (NPC npc : worldView.npcs())
		{
			onNpcSpawned(npc);
		}
	}

	public void onObjectSpawned(GameObject object)
	{
		if (!PATCH_REGIONS.contains(object.getWorldLocation().getRegionID()))
		{
			return;
		}

		final Patch patch = patchFor(object);
		if (patch != null)
		{
			patchObjects.put(patch, object);
		}
	}

	public void onObjectDespawned(GameObject object)
	{
		patchObjects.values().remove(object);
	}

	public void onNpcSpawned(NPC npc)
	{
		if (GARDENER_IDS.contains(npc.getId()))
		{
			gardeners.add(npc);
		}
	}

	public void onNpcDespawned(NPC npc)
	{
		gardeners.remove(npc);
	}

	@Nullable
	private Patch patchFor(GameObject object)
	{
		final ObjectComposition composition = client.getObjectDefinition(object.getId());
		if (composition == null || composition.getVarbitId() == -1)
		{
			return null;
		}

		for (Patch patch : Patch.values())
		{
			if (patch.getVarbit() == composition.getVarbitId() && patch.isTransmittedAt(object.getWorldLocation()))
			{
				return patch;
			}
		}
		return null;
	}
}
