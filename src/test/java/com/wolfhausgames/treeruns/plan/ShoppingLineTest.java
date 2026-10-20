package com.wolfhausgames.treeruns.plan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Set;
import org.junit.Test;

public class ShoppingLineTest
{
	private static ShoppingLine line(int needed, int carried, int stored, int banked)
	{
		return new ShoppingLine("Test", Set.of(1), needed, carried, stored, banked, false);
	}

	@Test
	public void leprechaunCountsAsAtHand()
	{
		final ShoppingLine spade = line(1, 0, 1, 0);
		assertFalse(spade.isCarried());
		assertTrue(spade.isAtHand());
		assertEquals(0, spade.getToWithdraw());
		assertEquals(0, spade.getMissing());
	}

	@Test
	public void withdrawsOnlyWhatIsStillNeeded()
	{
		final ShoppingLine coconuts = line(150, 40, 0, 500);
		assertFalse(coconuts.isAtHand());
		assertTrue(coconuts.isAvailable());
		assertEquals(110, coconuts.getToWithdraw());
	}

	@Test
	public void reportsShortfall()
	{
		final ShoppingLine saplings = line(6, 1, 0, 2);
		assertFalse(saplings.isAvailable());
		assertEquals(3, saplings.getMissing());
		assertEquals(2, saplings.getToWithdraw());
	}
}
