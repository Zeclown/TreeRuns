/*
 * Gardener NPCs are derived from RuneLite's Time Tracking plugin and patch locations from
 * Quest Helper, and gardeners with several IDs noted from Farm Run Autopilot (all BSD 2-Clause).
 * See THIRD_PARTY_NOTICES.
 */
package com.wolfhausgames.treeruns.data;

import java.util.EnumMap;
import java.util.Map;
import lombok.Value;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.NpcID;

/**
 * Where each patch is and which NPCs tend it. Gardener IDs follow RuneLite's timetracking FarmingWorld.
 * Locations come from Quest Helper's tree run and, for the special trees, the OSRS Wiki's patch maps;
 * they are used for the hint arrow and Shortest Path, so they only need to be on or next to the patch.
 */
@Value
public class PatchSite
{
	private static final Map<Patch, PatchSite> SITES = new EnumMap<>(Patch.class);

	static
	{
		add(Patch.LUMBRIDGE_TREE, 3193, 3231, NpcID.FARMING_GARDENER_TREE_4);
		// Treznor has a different ID depending on Varrock quest progress
		add(Patch.VARROCK_TREE, 3229, 3459, NpcID.FARMING_GARDENER_TREE_3, NpcID.FARMING_GARDENER_TREE_3_02,
			NpcID.DOV_TREZNOR);
		add(Patch.FALADOR_TREE, 3004, 3373, NpcID.FARMING_GARDENER_TREE_2);
		add(Patch.TAVERLEY_TREE, 2936, 3438, NpcID.FARMING_GARDENER_TREE_1);
		add(Patch.GNOME_STRONGHOLD_TREE, 2436, 3415, NpcID.FARMING_GARDENER_TREE_GNOME);
		add(Patch.FARMING_GUILD_TREE, 1232, 3736, NpcID.FARMING_GARDENER_FARMGUILD_T2);
		add(Patch.NEMUS_RETREAT_TREE, 1367, 3322, NpcID.FARMING_GARDENER_TREE_7);

		add(Patch.GNOME_STRONGHOLD_FRUIT, 2476, 3446, NpcID.FARMING_GARDENER_FRUIT_1);
		add(Patch.TREE_GNOME_VILLAGE_FRUIT, 2490, 3180, NpcID.FARMING_GARDENER_FRUIT_2);
		add(Patch.BRIMHAVEN_FRUIT, 2765, 3213, NpcID.GARTH);
		add(Patch.CATHERBY_FRUIT, 2860, 3433, NpcID.FARMING_GARDENER_FRUIT_4);
		add(Patch.LLETYA_FRUIT, 2347, 3162, NpcID.FARMING_GARDENER_FRUIT_TREE_5);
		add(Patch.FARMING_GUILD_FRUIT, 1242, 3758, NpcID.FARMING_GARDENER_FARMGUILD_T3);
		add(Patch.KASTORI_FRUIT, 1350, 3057, NpcID.FARMING_GARDENER_FRUIT_7);

		add(Patch.FOSSIL_ISLAND_EAST, 3715, 3835, NpcID.FOSSIL_SQUIRREL_GARDENER1);
		add(Patch.FOSSIL_ISLAND_MIDDLE, 3708, 3833, NpcID.FOSSIL_SQUIRREL_GARDENER2);
		add(Patch.FOSSIL_ISLAND_WEST, 3702, 3837, NpcID.FOSSIL_SQUIRREL_GARDENER3);
		add(Patch.LOCUS_OASIS_HARDWOOD, 1687, 2972, NpcID.FROG_QUEST_MARCELLUS, NpcID.FROG_QUEST_MARCELLUS_NORMAL,
			NpcID.FROG_QUEST_MARCELLUS_FARMER);
		add(Patch.ANGLERS_RETREAT_HARDWOOD, 2470, 2704, NpcID.FARMING_GARDENER_HARDWOOD_TREE_5);

		add(Patch.TAI_BWO_WANNAI_CALQUAT, 2795, 3102, NpcID.FARMING_GARDENER_CALQUAT);
		add(Patch.KASTORI_CALQUAT, 1366, 3033, NpcID.FARMING_GARDENER_CALQUAT_2);
		add(Patch.SUMMER_SHORE_CALQUAT, 3129, 2406, NpcID.FARMING_GARDENER_CALQUAT_3);

		add(Patch.PORT_SARIM_SPIRIT, 3060, 3258, NpcID.FARMING_GARDENER_SPIRIT_TREE_1);
		add(Patch.ETCETERIA_SPIRIT, 2613, 3858, NpcID.FARMING_GARDENER_SPIRIT_TREE_2);
		add(Patch.BRIMHAVEN_SPIRIT, 2802, 3203, NpcID.FARMING_GARDENER_SPIRIT_TREE_3);
		add(Patch.HOSIDIUS_SPIRIT, 1693, 3542, NpcID.FARMING_GARDENER_SPIRIT_TREE_4);
		add(Patch.FARMING_GUILD_SPIRIT, 1253, 3750, NpcID.FARMING_GARDENER_SPIRIT_TREE_5);

		add(Patch.FARMING_GUILD_CELASTRUS, 1244, 3750, NpcID.FARMING_GARDENER_FARMGUILD_CELASTRUS);
		add(Patch.FARMING_GUILD_REDWOOD, 1229, 3755, NpcID.FARMING_GARDENER_FARMGUILD_REDWOOD);
		// Nobody tends the crystal tree; it can't be protected or removed.
		add(Patch.PRIFDDINAS_CRYSTAL, 3292, 6119);
	}

	WorldPoint location;
	int[] gardenerNpcIds;

	public static PatchSite of(Patch patch)
	{
		return SITES.get(patch);
	}

	public boolean isGardener(int npcId)
	{
		for (int id : gardenerNpcIds)
		{
			if (id == npcId)
			{
				return true;
			}
		}
		return false;
	}

	private static void add(Patch patch, int x, int y, int... gardeners)
	{
		SITES.put(patch, new PatchSite(new WorldPoint(x, y, 0), gardeners));
	}
}
