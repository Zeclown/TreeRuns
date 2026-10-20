package com.wolfhausgames.treeruns.data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.VarbitID;

/**
 * Every tree-run patch. Region IDs, varbits and bounds follow RuneLite's timetracking FarmingWorld;
 * requirements and removal fees are from the OSRS Wiki.
 * <p>
 * The game reuses the FARMING_TRANSMIT varbits everywhere, so a patch's varbit is only meaningful
 * while the player is in one of its regions and inside its bounds.
 */
@Getter
public enum Patch
{
	// Regular trees
	LUMBRIDGE_TREE(PatchKind.TREE, "Lumbridge", VarbitID.FARMING_TRANSMIT_A, 200,
		regions(12594, 12850)),
	VARROCK_TREE(PatchKind.TREE, "Varrock", VarbitID.FARMING_TRANSMIT_A, 200,
		regions(12854, 12853)),
	FALADOR_TREE(PatchKind.TREE, "Falador", VarbitID.FARMING_TRANSMIT_A, 200,
		regions(11828, 12084)),
	TAVERLEY_TREE(PatchKind.TREE, "Taverley", VarbitID.FARMING_TRANSMIT_A, 200,
		regions(11573, 11829)),
	GNOME_STRONGHOLD_TREE(PatchKind.TREE, "Gnome Stronghold", VarbitID.FARMING_TRANSMIT_A, 200,
		Regions.GNOME_STRONGHOLD),
	FARMING_GUILD_TREE(PatchKind.TREE, "Farming Guild", VarbitID.FARMING_TRANSMIT_G, 200,
		Regions.FARMING_GUILD,
		Requirement.skill(Skill.FARMING, 65, true)),
	NEMUS_RETREAT_TREE(PatchKind.TREE, "Nemus Retreat", VarbitID.FARMING_TRANSMIT_A, 200,
		regions(5427, 5428, 5684),
		Requirement.questCompleted(Quest.CHILDREN_OF_THE_SUN)),

	// Fruit trees
	GNOME_STRONGHOLD_FRUIT(PatchKind.FRUIT_TREE, "Gnome Stronghold", VarbitID.FARMING_TRANSMIT_B, 200,
		Regions.GNOME_STRONGHOLD),
	TREE_GNOME_VILLAGE_FRUIT(PatchKind.FRUIT_TREE, "Tree Gnome Village", VarbitID.FARMING_TRANSMIT_A, 200,
		regions(9777, 10033)),
	BRIMHAVEN_FRUIT(PatchKind.FRUIT_TREE, "Brimhaven", VarbitID.FARMING_TRANSMIT_A, 200,
		Regions.BRIMHAVEN),
	// Region 11317 also covers the Catherby allotments, which send the same varbit when downstairs to the west.
	CATHERBY_FRUIT(PatchKind.FRUIT_TREE, "Catherby", VarbitID.FARMING_TRANSMIT_A, 200,
		regions(11317), loc -> loc.getX() >= 2840 || loc.getY() < 3440 || loc.getPlane() == 1),
	LLETYA_FRUIT(PatchKind.FRUIT_TREE, "Lletya", VarbitID.FARMING_TRANSMIT_A, 200,
		regions(9265, 11103),
		Requirement.questStarted(Quest.MOURNINGS_END_PART_I)),
	FARMING_GUILD_FRUIT(PatchKind.FRUIT_TREE, "Farming Guild", VarbitID.FARMING_TRANSMIT_K, 200,
		Regions.FARMING_GUILD,
		Requirement.skill(Skill.FARMING, 85, true)),
	KASTORI_FRUIT(PatchKind.FRUIT_TREE, "Kastori", VarbitID.FARMING_TRANSMIT_B, 200,
		Regions.KASTORI,
		Requirement.questCompleted(Quest.CHILDREN_OF_THE_SUN)),

	// Hardwood trees
	FOSSIL_ISLAND_EAST(PatchKind.HARDWOOD, "Fossil (east)", VarbitID.FARMING_TRANSMIT_A, 200,
		Regions.FOSSIL_ISLAND, Patch::onFossilIslandSurface,
		Requirement.questCompleted(Quest.BONE_VOYAGE)),
	FOSSIL_ISLAND_MIDDLE(PatchKind.HARDWOOD, "Fossil (middle)", VarbitID.FARMING_TRANSMIT_B, 200,
		Regions.FOSSIL_ISLAND, Patch::onFossilIslandSurface,
		Requirement.questCompleted(Quest.BONE_VOYAGE)),
	FOSSIL_ISLAND_WEST(PatchKind.HARDWOOD, "Fossil (west)", VarbitID.FARMING_TRANSMIT_C, 200,
		Regions.FOSSIL_ISLAND, Patch::onFossilIslandSurface,
		Requirement.questCompleted(Quest.BONE_VOYAGE)),
	// The wiki doesn't state Marcellus's fee; 200 is inferred from the Farming runs page.
	LOCUS_OASIS_HARDWOOD(PatchKind.HARDWOOD, "Locus Oasis", VarbitID.FARMING_TRANSMIT_A, 200,
		regions(6702, 6446),
		Requirement.questCompleted(Quest.THE_RIBBITING_TALE_OF_A_LILY_PAD_LABOUR_DISPUTE)),
	ANGLERS_RETREAT_HARDWOOD(PatchKind.HARDWOOD, "Anglers' Retreat", VarbitID.FARMING_TRANSMIT_A, 200,
		regions(9770),
		Requirement.skill(Skill.SAILING, 51, false)),

	// Calquat. Grown calquats are cleared with a spade, so there is no removal fee.
	TAI_BWO_WANNAI_CALQUAT(PatchKind.CALQUAT, "Tai Bwo Wannai", VarbitID.FARMING_TRANSMIT_A, 0,
		regions(11056)),
	KASTORI_CALQUAT(PatchKind.CALQUAT, "Kastori", VarbitID.FARMING_TRANSMIT_A, 0,
		Regions.KASTORI,
		Requirement.questCompleted(Quest.CHILDREN_OF_THE_SUN)),
	SUMMER_SHORE_CALQUAT(PatchKind.CALQUAT, "The Summer Shore", VarbitID.FARMING_TRANSMIT_C, 0,
		regions(12581, 12325, 12326, 12327, 12580, 12582, 12583, 12836, 12837, 12838, 12839, 13092, 13093, 13194),
		Requirement.questStarted(Quest.TROUBLED_TORTUGANS)),

	// Spirit trees can't be chopped; they are cleared with a spade.
	PORT_SARIM_SPIRIT(PatchKind.SPIRIT, "Port Sarim", VarbitID.FARMING_TRANSMIT_A, 0,
		regions(12082, 12083), loc -> loc.getY() < 3272),
	ETCETERIA_SPIRIT(PatchKind.SPIRIT, "Etceteria", VarbitID.FARMING_TRANSMIT_B, 0,
		regions(10300),
		Requirement.questCompleted(Quest.THE_FREMENNIK_TRIALS)),
	BRIMHAVEN_SPIRIT(PatchKind.SPIRIT, "Brimhaven", VarbitID.FARMING_TRANSMIT_B, 0,
		Regions.BRIMHAVEN),
	HOSIDIUS_SPIRIT(PatchKind.SPIRIT, "Hosidius", VarbitID.FARMING_TRANSMIT_F, 0,
		regions(6967, 6711)),
	FARMING_GUILD_SPIRIT(PatchKind.SPIRIT, "Farming Guild", VarbitID.FARMING_TRANSMIT_A, 0,
		Regions.FARMING_GUILD,
		Requirement.skill(Skill.FARMING, 85, true)),

	// Celastrus is chopped and then dug up by the player.
	FARMING_GUILD_CELASTRUS(PatchKind.CELASTRUS, "Farming Guild", VarbitID.FARMING_TRANSMIT_L, 0,
		Regions.FARMING_GUILD,
		Requirement.skill(Skill.FARMING, 85, true)),

	FARMING_GUILD_REDWOOD(PatchKind.REDWOOD, "Farming Guild", VarbitID.FARMING_TRANSMIT_I, 2000,
		Regions.FARMING_GUILD,
		Requirement.skill(Skill.FARMING, 85, true)),

	// Harvesting a crystal tree removes it.
	PRIFDDINAS_CRYSTAL(PatchKind.CRYSTAL, "Prifddinas", VarbitID.FARMING_TRANSMIT_E, 0,
		regions(13151, 12895, 12894, 13150, 12994, 12993, 12737, 12738, 12126, 12127, 13250),
		Requirement.questCompleted(Quest.SONG_OF_THE_ELVES));

	private final PatchKind kind;
	private final String locationName;
	private final int varbit;
	/** Coins the gardener charges to remove a grown tree, or 0 if the player clears it themselves. */
	private final int removalCost;
	private final int[] regionIds;
	@Getter(AccessLevel.NONE)
	private final Predicate<WorldPoint> bounds;
	private final List<Requirement> requirements;

	Patch(PatchKind kind, String locationName, int varbit, int removalCost, int[] regionIds, Requirement... requirements)
	{
		this(kind, locationName, varbit, removalCost, regionIds, loc -> true, requirements);
	}

	Patch(PatchKind kind, String locationName, int varbit, int removalCost, int[] regionIds,
		Predicate<WorldPoint> bounds, Requirement... requirements)
	{
		this.kind = kind;
		this.locationName = locationName;
		this.varbit = varbit;
		this.removalCost = removalCost;
		this.regionIds = regionIds;
		this.bounds = bounds;
		this.requirements = List.of(requirements);
	}

	/** The region Time Tracking files this patch under. */
	public int getPrimaryRegionId()
	{
		return regionIds[0];
	}

	public boolean isRemovable()
	{
		return removalCost > 0;
	}

	/** Whether this patch's varbit describes this patch when the player stands at {@code location}. */
	public boolean isTransmittedAt(WorldPoint location)
	{
		final int region = location.getRegionID();
		for (int id : regionIds)
		{
			if (id == region)
			{
				return bounds.test(location);
			}
		}
		return false;
	}

	/** Must be called on the client thread. */
	public boolean isUnlocked(Client client)
	{
		for (Requirement requirement : requirements)
		{
			if (!requirement.isMet(client))
			{
				return false;
			}
		}
		return true;
	}

	public static List<Patch> forKind(PatchKind kind)
	{
		final List<Patch> patches = new ArrayList<>();
		for (Patch patch : values())
		{
			if (patch.kind == kind)
			{
				patches.add(patch);
			}
		}
		return patches;
	}

	private static int[] regions(int... ids)
	{
		return ids;
	}

	/**
	 * Hardwood varbits are sent anywhere on the surface of Fossil Island, except on a few
	 * stair and ladder tiles where they arrive a tick early.
	 */
	private static boolean onFossilIslandSurface(WorldPoint loc)
	{
		// Stairs to the house on the hill
		if (loc.getX() == 3753 && loc.getY() >= 3868 && loc.getY() <= 3870)
		{
			return false;
		}
		// East and west ladders to the rope bridge
		if ((loc.getX() == 3729 || loc.getX() == 3728 || loc.getX() == 3747 || loc.getX() == 3746)
			&& loc.getY() <= 3832 && loc.getY() >= 3830)
		{
			return false;
		}
		return loc.getPlane() == 0;
	}

	/** Region groups shared by several patches. Nested so the enum constants can reference them. */
	private static final class Regions
	{
		static final int[] GNOME_STRONGHOLD = {9781, 9782, 9526, 9525};
		static final int[] FARMING_GUILD = {4922, 5177, 5178, 5179, 4921, 4923, 4665, 4666, 4667};
		static final int[] BRIMHAVEN = {11058, 11057};
		static final int[] KASTORI = {5423, 5167, 5424};
		static final int[] FOSSIL_ISLAND = {14651, 14907, 14908, 15164, 14652, 14906, 14650, 15162, 15163};
	}
}
