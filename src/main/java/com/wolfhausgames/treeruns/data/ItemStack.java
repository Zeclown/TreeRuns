package com.wolfhausgames.treeruns.data;

import lombok.Value;

/**
 * An item and a quantity, such as a protection payment.
 */
@Value
public class ItemStack
{
	int itemId;
	int quantity;

	public static ItemStack of(int itemId, int quantity)
	{
		return new ItemStack(itemId, quantity);
	}
}
