package com.wolfhausgames.treeruns.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.runelite.api.gameval.ItemID;

/**
 * Items a tree run can need. Only these are remembered from the bank.
 */
public final class TrackedItems
{
	private static final Set<Integer> ITEMS;

	static
	{
		final Set<Integer> items = new HashSet<>();
		items.add(ItemID.COINS);
		items.add(ItemID.SPADE);
		items.add(ItemID.RAKE);
		items.add(ItemID.SECATEURS);
		items.add(ItemID.FAIRY_ENCHANTED_SECATEURS);
		for (TreeType tree : TreeType.values())
		{
			items.add(tree.getSaplingItemId());
			for (ItemStack payment : tree.getProtectionPayment())
			{
				items.add(payment.getItemId());
			}
		}
		ITEMS = Collections.unmodifiableSet(items);
	}

	private TrackedItems()
	{
	}

	public static boolean isTracked(int itemId)
	{
		return ITEMS.contains(itemId);
	}
}
