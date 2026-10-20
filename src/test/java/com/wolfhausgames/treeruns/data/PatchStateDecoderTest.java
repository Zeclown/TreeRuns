package com.wolfhausgames.treeruns.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

public class PatchStateDecoderTest
{
	@Test
	public void decodesMagicTreeLifecycle()
	{
		assertEquals(new PatchState(TreeType.MAGIC, TreeState.GROWING, 0), PatchStateDecoder.decode(PatchKind.TREE, 48));
		assertEquals(TreeState.READY, PatchStateDecoder.decode(PatchKind.TREE, 60).getState());
		assertEquals(TreeState.CHECKED, PatchStateDecoder.decode(PatchKind.TREE, 61).getState());
		assertEquals(TreeState.STUMP, PatchStateDecoder.decode(PatchKind.TREE, 62).getState());
		assertEquals(TreeState.DISEASED, PatchStateDecoder.decode(PatchKind.TREE, 113).getState());
		assertEquals(TreeState.DEAD, PatchStateDecoder.decode(PatchKind.TREE, 189).getState());
	}

	@Test
	public void finalGrowingStageIsOneBeforeCheckHealth()
	{
		for (TreeType tree : TreeType.values())
		{
			int lastGrowing = -1;
			for (int value = 0; value < 256; value++)
			{
				final PatchState state = PatchStateDecoder.decode(tree.getKind(), value);
				if (state.getTree() == tree && state.getState() == TreeState.GROWING)
				{
					lastGrowing = Math.max(lastGrowing, state.getStage());
				}
			}
			assertEquals(tree + " stage count", tree.getStages() - 2, lastGrowing);
		}
	}

	@Test
	public void weedsAreEmpty()
	{
		final PatchState state = PatchStateDecoder.decode(PatchKind.FRUIT_TREE, 0);
		assertEquals(TreeState.EMPTY, state.getState());
		assertNull(state.getTree());
	}

	@Test
	public void outOfRangeIsUnknown()
	{
		assertEquals(PatchState.UNKNOWN, PatchStateDecoder.decode(PatchKind.CRYSTAL, 200));
		assertEquals(PatchState.UNKNOWN, PatchStateDecoder.decode(PatchKind.TREE, -1));
	}

	@Test
	public void everyKindHasTreesAndPatches()
	{
		for (PatchKind kind : PatchKind.values())
		{
			assertTrue(kind + " trees", !TreeType.forKind(kind).isEmpty());
			assertTrue(kind + " patches", !Patch.forKind(kind).isEmpty());
		}
	}

	@Test
	public void catherbyFruitTreeIgnoresAllotmentArea()
	{
		assertTrue(Patch.CATHERBY_FRUIT.isTransmittedAt(new WorldPoint(2860, 3433, 0)));
		assertTrue(!Patch.CATHERBY_FRUIT.isTransmittedAt(new WorldPoint(2820, 3445, 0)));
	}
}
