package com.wolfhausgames.treeruns.plan;

import java.util.Set;
import lombok.Value;

/**
 * One item the run needs, with how many the player has.
 */
@Value
public class ShoppingLine
{
	String name;
	/** Any of these count towards the line, e.g. every kind of axe. */
	Set<Integer> itemIds;
	int needed;
	int carried;
	/** Held by the tool leprechaun, which can be reached at most patches. */
	int stored;
	int banked;
	/** Useful but not required, such as a rake for weeds. */
	boolean optional;

	public boolean isCarried()
	{
		return carried >= needed;
	}

	/** Covered by what's carried plus what the tool leprechaun holds. */
	public boolean isAtHand()
	{
		return carried + stored >= needed;
	}

	public boolean isAvailable()
	{
		return carried + stored + banked >= needed;
	}

	public int getMissing()
	{
		return Math.max(0, needed - carried - stored - banked);
	}

	/** How many to take out of the bank. */
	public int getToWithdraw()
	{
		return Math.max(0, Math.min(needed - carried - stored, banked));
	}
}
