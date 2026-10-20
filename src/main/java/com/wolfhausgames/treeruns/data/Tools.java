package com.wolfhausgames.treeruns.data;

import java.util.Set;
import net.runelite.api.gameval.ItemID;

/**
 * Tools a tree run can need. Each set is a group of items that do the same job.
 */
public final class Tools
{
	public static final Set<Integer> SPADE = Set.of(ItemID.SPADE);
	public static final Set<Integer> RAKE = Set.of(ItemID.RAKE);
	public static final Set<Integer> SECATEURS = Set.of(ItemID.SECATEURS, ItemID.FAIRY_ENCHANTED_SECATEURS);
	public static final Set<Integer> AXES = Set.of(
		ItemID.BRONZE_AXE, ItemID.IRON_AXE, ItemID.STEEL_AXE, ItemID.BLACK_AXE, ItemID.MITHRIL_AXE,
		ItemID.ADAMANT_AXE, ItemID.RUNE_AXE, ItemID.DRAGON_AXE, ItemID.INFERNAL_AXE, ItemID.INFERNAL_AXE_EMPTY,
		ItemID._3A_AXE, ItemID.TRAIL_GILDED_AXE, ItemID.CRYSTAL_AXE, ItemID.CRYSTAL_AXE_INACTIVE,
		ItemID.TRAILBLAZER_AXE, ItemID.TRAILBLAZER_AXE_EMPTY, ItemID.TRAILBLAZER_AXE_NO_INFERNAL,
		ItemID.TRAILBLAZER_RELOADED_AXE, ItemID.TRAILBLAZER_RELOADED_AXE_EMPTY, ItemID.TRAILBLAZER_RELOADED_AXE_NO_INFERNAL,
		// Felling axes
		ItemID.BRONZE_AXE_2H, ItemID.IRON_AXE_2H, ItemID.STEEL_AXE_2H, ItemID.BLACK_AXE_2H, ItemID.MITHRIL_AXE_2H,
		ItemID.ADAMANT_AXE_2H, ItemID.RUNE_AXE_2H, ItemID.DRAGON_AXE_2H, ItemID.CRYSTAL_AXE_2H,
		ItemID.CRYSTAL_AXE_2H_INACTIVE, ItemID._3A_AXE_2H
	);

	private Tools()
	{
	}

	/** Whether a tree of this kind has to be chopped by the player when the gardener doesn't remove it. */
	public static boolean needsAxeToClear(PatchKind kind)
	{
		switch (kind)
		{
			// Dug up with a spade instead
			case CALQUAT:
			case SPIRIT:
				return false;
			default:
				return true;
		}
	}
}
