package com.wolfhausgames.treeruns.state;

import javax.annotation.Nullable;
import lombok.Value;

/**
 * A patch's varbit value and when it was seen with that value.
 * <p>
 * The serialized form, {@code <value>:<epoch seconds>}, is the same one the Time Tracking plugin uses.
 */
@Value
class PatchRecord
{
	int varbitValue;
	/** Epoch seconds. */
	long timestamp;
	/** Whether this was imported from the Time Tracking plugin rather than seen by this plugin. */
	boolean fromTimeTracking;

	PatchRecord(int varbitValue, long timestamp)
	{
		this(varbitValue, timestamp, false);
	}

	PatchRecord(int varbitValue, long timestamp, boolean fromTimeTracking)
	{
		this.varbitValue = varbitValue;
		this.timestamp = timestamp;
		this.fromTimeTracking = fromTimeTracking;
	}

	PatchRecord asFromTimeTracking()
	{
		return new PatchRecord(varbitValue, timestamp, true);
	}

	String serialize()
	{
		return varbitValue + ":" + timestamp;
	}

	@Nullable
	static PatchRecord parse(@Nullable String value)
	{
		if (value == null)
		{
			return null;
		}

		final String[] parts = value.split(":");
		if (parts.length != 2)
		{
			return null;
		}

		try
		{
			return new PatchRecord(Integer.parseInt(parts[0]), Long.parseLong(parts[1]));
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}
}
