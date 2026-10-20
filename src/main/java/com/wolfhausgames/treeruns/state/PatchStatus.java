package com.wolfhausgames.treeruns.state;

import com.wolfhausgames.treeruns.data.PatchState;
import lombok.Value;

@Value
public class PatchStatus
{
	public static final PatchStatus UNSEEN = new PatchStatus(PatchState.UNKNOWN, 0, 0, false);

	/** The state now. For growing trees this is estimated from the last sighting. */
	PatchState state;
	/** Epoch seconds when the patch was last seen changing, or 0 if never seen. */
	long lastSeen;
	/** Epoch seconds when a growing tree is expected to be fully grown, or 0 if not growing. */
	long readyAt;
	/** Whether the last sighting came from the Time Tracking plugin's saved data. */
	boolean fromTimeTracking;

	public boolean isSeen()
	{
		return lastSeen != 0;
	}
}
