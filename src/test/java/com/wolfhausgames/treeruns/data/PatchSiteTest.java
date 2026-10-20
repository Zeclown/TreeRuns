package com.wolfhausgames.treeruns.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.HashSet;
import org.junit.Test;

public class PatchSiteTest
{
	@Test
	public void everyPatchHasASiteInsideItsRegions()
	{
		for (Patch patch : Patch.values())
		{
			final PatchSite site = PatchSite.of(patch);
			assertNotNull(patch + " site", site);
			// The location must be somewhere the patch's state is sent, or the run can't tell when you've arrived
			assertTrue(patch + " location " + site.getLocation(), patch.isTransmittedAt(site.getLocation()));
		}
	}

	@Test
	public void defaultRouteHasEveryPatchOnce()
	{
		assertEquals(Patch.values().length, DefaultRoute.ORDER.size());
		assertEquals(Patch.values().length, new HashSet<>(DefaultRoute.ORDER).size());
	}

	@Test
	public void onlyTheCrystalTreeHasNoGardener()
	{
		for (Patch patch : Patch.values())
		{
			assertEquals(patch.toString(), patch == Patch.PRIFDDINAS_CRYSTAL, PatchSite.of(patch).getGardenerNpcIds().length == 0);
		}
	}
}
