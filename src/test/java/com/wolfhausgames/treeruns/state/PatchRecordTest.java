package com.wolfhausgames.treeruns.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.wolfhausgames.treeruns.data.Patch;
import net.runelite.api.gameval.VarbitID;
import org.junit.Test;

public class PatchRecordTest
{
	@Test
	public void roundTripsTimeTrackingFormat()
	{
		final PatchRecord record = PatchRecord.parse("61:1760000000");
		assertEquals(new PatchRecord(61, 1760000000L), record);
		assertEquals("61:1760000000", record.serialize());
	}

	@Test
	public void rejectsMalformedValues()
	{
		assertNull(PatchRecord.parse(null));
		assertNull(PatchRecord.parse("61"));
		assertNull(PatchRecord.parse("abc:def"));
	}

	@Test
	public void primaryRegionsMatchTimeTracking()
	{
		// Time Tracking keys are <first region of the FarmingRegion>.<varbit>
		assertEquals(12594, Patch.LUMBRIDGE_TREE.getPrimaryRegionId());
		assertEquals(4922, Patch.FARMING_GUILD_REDWOOD.getPrimaryRegionId());
		assertEquals(VarbitID.FARMING_TRANSMIT_I, Patch.FARMING_GUILD_REDWOOD.getVarbit());
		assertEquals(14651, Patch.FOSSIL_ISLAND_WEST.getPrimaryRegionId());
		assertEquals(11317, Patch.CATHERBY_FRUIT.getPrimaryRegionId());
		assertEquals(12581, Patch.SUMMER_SHORE_CALQUAT.getPrimaryRegionId());
	}
}
