package com.wolfhausgames.treeruns.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import net.runelite.api.gameval.ItemID;

/**
 * Every tree that can be planted in a tree-run patch. Levels and protection payments are from the OSRS Wiki.
 * Stage counts match the varbit states in {@link PatchStateDecoder}: the last stage is "Check-health".
 */
@Getter
public enum TreeType
{
	OAK(PatchKind.TREE, "Oak", 15, ItemID.PLANTPOT_OAK_SAPLING, 5, 40,
		ItemStack.of(ItemID.BASKET_TOMATO_5, 1)),
	WILLOW(PatchKind.TREE, "Willow", 30, ItemID.PLANTPOT_WILLOW_SAPLING, 7, 40,
		ItemStack.of(ItemID.BASKET_APPLE_5, 1)),
	MAPLE(PatchKind.TREE, "Maple", 45, ItemID.PLANTPOT_MAPLE_SAPLING, 9, 40,
		ItemStack.of(ItemID.BASKET_ORANGE_5, 1)),
	YEW(PatchKind.TREE, "Yew", 60, ItemID.PLANTPOT_YEW_SAPLING, 11, 40,
		ItemStack.of(ItemID.CACTUS_SPINE, 10)),
	MAGIC(PatchKind.TREE, "Magic", 75, ItemID.PLANTPOT_MAGIC_TREE_SAPLING, 13, 40,
		ItemStack.of(ItemID.COCONUT, 25)),

	APPLE(PatchKind.FRUIT_TREE, "Apple", 27, ItemID.PLANTPOT_APPLE_SAPLING, 7, 160,
		ItemStack.of(ItemID.SWEETCORN, 9)),
	BANANA(PatchKind.FRUIT_TREE, "Banana", 33, ItemID.PLANTPOT_BANANA_SAPLING, 7, 160,
		ItemStack.of(ItemID.BASKET_APPLE_5, 4)),
	ORANGE(PatchKind.FRUIT_TREE, "Orange", 39, ItemID.PLANTPOT_ORANGE_SAPLING, 7, 160,
		ItemStack.of(ItemID.BASKET_STRAWBERRY_5, 3)),
	CURRY(PatchKind.FRUIT_TREE, "Curry", 42, ItemID.PLANTPOT_CURRY_SAPLING, 7, 160,
		ItemStack.of(ItemID.BASKET_BANANA_5, 5)),
	PINEAPPLE(PatchKind.FRUIT_TREE, "Pineapple", 51, ItemID.PLANTPOT_PINEAPPLE_SAPLING, 7, 160,
		ItemStack.of(ItemID.WATERMELON, 10)),
	PAPAYA(PatchKind.FRUIT_TREE, "Papaya", 57, ItemID.PLANTPOT_PAPAYA_SAPLING, 7, 160,
		ItemStack.of(ItemID.PINEAPPLE, 10)),
	PALM(PatchKind.FRUIT_TREE, "Palm", 68, ItemID.PLANTPOT_PALM_SAPLING, 7, 160,
		ItemStack.of(ItemID.PAPAYA, 15)),
	DRAGONFRUIT(PatchKind.FRUIT_TREE, "Dragonfruit", 81, ItemID.PLANTPOT_DRAGONFRUIT_SAPLING, 7, 160,
		ItemStack.of(ItemID.COCONUT, 15)),

	TEAK(PatchKind.HARDWOOD, "Teak", 35, ItemID.PLANTPOT_TEAK_SAPLING, 8, 640,
		ItemStack.of(ItemID.LIMPWURT_ROOT, 15)),
	MAHOGANY(PatchKind.HARDWOOD, "Mahogany", 55, ItemID.PLANTPOT_MAHOGANY_SAPLING, 9, 640,
		ItemStack.of(ItemID.YANILLIAN_HOPS, 25)),
	CAMPHOR(PatchKind.HARDWOOD, "Camphor", 66, ItemID.PLANTPOT_CAMPHOR_SAPLING, 9, 640,
		ItemStack.of(ItemID.WHITE_BERRIES, 10)),
	IRONWOOD(PatchKind.HARDWOOD, "Ironwood", 80, ItemID.PLANTPOT_IRONWOOD_SAPLING, 9, 640,
		ItemStack.of(ItemID.CURRY_LEAF, 10)),
	ROSEWOOD(PatchKind.HARDWOOD, "Rosewood", 92, ItemID.PLANTPOT_ROSEWOOD_SAPLING, 10, 640,
		ItemStack.of(ItemID.DRAGONFRUIT, 8)),

	CALQUAT(PatchKind.CALQUAT, "Calquat", 72, ItemID.PLANTPOT_CALQUAT_SAPLING, 9, 160,
		ItemStack.of(ItemID.POISONIVY_BERRIES, 8)),
	SPIRIT(PatchKind.SPIRIT, "Spirit tree", 83, ItemID.PLANTPOT_SPIRIT_TREE_SAPLING, 13, 320,
		ItemStack.of(ItemID.MM_MONKEY_NUTS, 5),
		ItemStack.of(ItemID.MM_MONKEY_BAR, 1),
		ItemStack.of(ItemID.LUNAR_GROUNDTOOTH, 1)),
	CELASTRUS(PatchKind.CELASTRUS, "Celastrus", 85, ItemID.PLANTPOT_CELASTRUS_TREE_SAPLING, 6, 160,
		ItemStack.of(ItemID.CACTUS_POTATO, 8)),
	REDWOOD(PatchKind.REDWOOD, "Redwood", 90, ItemID.PLANTPOT_REDWOOD_TREE_SAPLING, 11, 640,
		ItemStack.of(ItemID.DRAGONFRUIT, 6)),
	// Crystal trees cannot become diseased, so there is nothing to pay for.
	CRYSTAL(PatchKind.CRYSTAL, "Crystal tree", 74, ItemID.PLANTPOT_CRYSTAL_TREE_SAPLING, 7, 80);

	private final PatchKind kind;
	private final String displayName;
	private final int farmingLevel;
	private final int saplingItemId;
	/** Number of growth states, including the final "Check-health" state. */
	private final int stages;
	private final int minutesPerStage;
	/** Items the gardener takes to protect the tree. Empty when the tree can't be protected. */
	private final List<ItemStack> protectionPayment;

	TreeType(PatchKind kind, String displayName, int farmingLevel, int saplingItemId, int stages, int minutesPerStage,
		ItemStack... protectionPayment)
	{
		this.kind = kind;
		this.displayName = displayName;
		this.farmingLevel = farmingLevel;
		this.saplingItemId = saplingItemId;
		this.stages = stages;
		this.minutesPerStage = minutesPerStage;
		this.protectionPayment = Collections.unmodifiableList(Arrays.asList(protectionPayment));
	}

	public boolean isProtectable()
	{
		return !protectionPayment.isEmpty();
	}

	public static List<TreeType> forKind(PatchKind kind)
	{
		final List<TreeType> trees = new ArrayList<>();
		for (TreeType tree : values())
		{
			if (tree.kind == kind)
			{
				trees.add(tree);
			}
		}
		return trees;
	}
}
