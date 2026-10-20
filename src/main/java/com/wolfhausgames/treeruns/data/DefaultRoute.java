package com.wolfhausgames.treeruns.data;

import java.util.List;

/**
 * The starting order for a run, grouping patches that are close together or share a teleport.
 * The player can reorder it; patches they don't use are simply left out.
 */
public final class DefaultRoute
{
	public static final List<Patch> ORDER = List.of(
		// Farming Guild, then the rest of Kourend
		Patch.FARMING_GUILD_TREE,
		Patch.FARMING_GUILD_FRUIT,
		Patch.FARMING_GUILD_SPIRIT,
		Patch.FARMING_GUILD_CELASTRUS,
		Patch.FARMING_GUILD_REDWOOD,
		Patch.HOSIDIUS_SPIRIT,
		// Varlamore
		Patch.NEMUS_RETREAT_TREE,
		Patch.KASTORI_FRUIT,
		Patch.KASTORI_CALQUAT,
		Patch.LOCUS_OASIS_HARDWOOD,
		// Asgarnia and Misthalin
		Patch.TAVERLEY_TREE,
		Patch.FALADOR_TREE,
		Patch.PORT_SARIM_SPIRIT,
		Patch.LUMBRIDGE_TREE,
		Patch.VARROCK_TREE,
		// Gnomes
		Patch.GNOME_STRONGHOLD_TREE,
		Patch.GNOME_STRONGHOLD_FRUIT,
		Patch.TREE_GNOME_VILLAGE_FRUIT,
		// Kandarin and Karamja
		Patch.CATHERBY_FRUIT,
		Patch.BRIMHAVEN_FRUIT,
		Patch.BRIMHAVEN_SPIRIT,
		Patch.TAI_BWO_WANNAI_CALQUAT,
		// Elves
		Patch.LLETYA_FRUIT,
		Patch.PRIFDDINAS_CRYSTAL,
		// Further afield
		Patch.ETCETERIA_SPIRIT,
		Patch.FOSSIL_ISLAND_EAST,
		Patch.FOSSIL_ISLAND_MIDDLE,
		Patch.FOSSIL_ISLAND_WEST,
		Patch.ANGLERS_RETREAT_HARDWOOD,
		Patch.SUMMER_SHORE_CALQUAT
	);

	private DefaultRoute()
	{
	}
}
