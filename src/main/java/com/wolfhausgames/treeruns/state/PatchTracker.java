package com.wolfhausgames.treeruns.state;

import com.wolfhausgames.treeruns.TreeRunsPlugin;
import com.wolfhausgames.treeruns.data.Patch;
import com.wolfhausgames.treeruns.data.PatchState;
import com.wolfhausgames.treeruns.data.PatchStateDecoder;
import com.wolfhausgames.treeruns.data.TreeState;
import com.wolfhausgames.treeruns.data.TreeType;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;

/**
 * Remembers the last state seen for each patch, per account. The game only sends a patch's
 * state while the player is nearby, so everything else is an estimate from the last sighting.
 */
@Slf4j
@Singleton
public class PatchTracker
{
	private static final String KEY_PREFIX = "patch.";

	// Time Tracking's saved data, read so patches it has seen don't start as "Not seen yet".
	// These keys are internal to that plugin, so anything unexpected is ignored.
	private static final String TIME_TRACKING_GROUP = "timetracking";
	private static final String TIME_TRACKING_OFFSET = "farmTickOffset";
	private static final String TIME_TRACKING_OFFSET_PRECISION = "farmTickOffsetPrecision";

	private final Client client;
	private final ConfigManager configManager;
	private final Map<Patch, PatchRecord> records = new ConcurrentHashMap<>();

	/** How far this account's growth clock is shifted from real time, in minutes, as learned by Time Tracking. */
	private volatile Integer tickOffsetMinutes;
	private volatile Integer tickOffsetPrecisionMinutes;

	@Inject
	PatchTracker(Client client, ConfigManager configManager)
	{
		this.client = client;
		this.configManager = configManager;
	}

	/** Reloads saved records for the current account. */
	public void load()
	{
		records.clear();
		for (Patch patch : Patch.values())
		{
			final PatchRecord own = PatchRecord.parse(
				configManager.getRSProfileConfiguration(TreeRunsPlugin.CONFIG_GROUP, KEY_PREFIX + patch.name()));
			final PatchRecord imported = PatchRecord.parse(configManager.getRSProfileConfiguration(TIME_TRACKING_GROUP,
				patch.getPrimaryRegionId() + "." + patch.getVarbit()));

			// Use whichever sighting is more recent; it reflects the patch's latest known state.
			if (imported != null && (own == null
				|| (imported.getVarbitValue() != own.getVarbitValue() && imported.getTimestamp() > own.getTimestamp())))
			{
				records.put(patch, imported.asFromTimeTracking());
			}
			else if (own != null)
			{
				records.put(patch, own);
			}
		}

		tickOffsetMinutes = readInt(TIME_TRACKING_OFFSET);
		tickOffsetPrecisionMinutes = readInt(TIME_TRACKING_OFFSET_PRECISION);
	}

	private Integer readInt(String key)
	{
		try
		{
			return configManager.getRSProfileConfiguration(TIME_TRACKING_GROUP, key, int.class);
		}
		catch (RuntimeException e)
		{
			return null;
		}
	}

	/**
	 * Seconds to add to real time to line up with this account's growth ticks. Time Tracking only
	 * trusts an offset observed on a tick at least as long as the one being predicted (or 40+ minutes).
	 */
	private long tickOffsetSeconds(int minutesPerStage)
	{
		final Integer offset = tickOffsetMinutes;
		final Integer precision = tickOffsetPrecisionMinutes;
		if (offset == null || precision == null || (precision < minutesPerStage && precision < 40))
		{
			return 0;
		}
		return (offset % minutesPerStage) * 60L;
	}

	/**
	 * Reads the varbits of every patch transmitted at the player's location. Must be called on the client thread.
	 *
	 * @return whether any patch changed
	 */
	public boolean update(WorldPoint location)
	{
		boolean changed = false;
		final long now = Instant.now().getEpochSecond();
		for (Patch patch : Patch.values())
		{
			if (!patch.isTransmittedAt(location))
			{
				continue;
			}

			final int value = client.getVarbitValue(patch.getVarbit());
			final PatchRecord previous = records.get(patch);
			if (previous != null && previous.getVarbitValue() == value)
			{
				continue;
			}

			final PatchRecord record = new PatchRecord(value, now);
			records.put(patch, record);
			configManager.setRSProfileConfiguration(TreeRunsPlugin.CONFIG_GROUP, KEY_PREFIX + patch.name(), record.serialize());
			log.debug("{} is now {}", patch, PatchStateDecoder.decode(patch.getKind(), value));
			changed = true;
		}
		return changed;
	}

	/** The state the game last sent for the patch, without estimating growth since. Safe to call from any thread. */
	public TreeState getSeenState(Patch patch)
	{
		final PatchRecord record = records.get(patch);
		return record == null ? TreeState.UNKNOWN
			: PatchStateDecoder.decode(patch.getKind(), record.getVarbitValue()).getState();
	}

	/** The patch's estimated status right now. Safe to call from any thread. */
	public PatchStatus getStatus(Patch patch)
	{
		final PatchRecord record = records.get(patch);
		if (record == null)
		{
			return PatchStatus.UNSEEN;
		}

		final PatchState seen = PatchStateDecoder.decode(patch.getKind(), record.getVarbitValue());
		final TreeType tree = seen.getTree();
		if (seen.getState() != TreeState.GROWING || tree == null)
		{
			return new PatchStatus(seen, record.getTimestamp(), 0, record.isFromTimeTracking());
		}

		// Growth happens on a per-account clock: every tree of a type ticks at the same moment.
		final long tickSeconds = tree.getMinutesPerStage() * 60L;
		final long offset = tickOffsetSeconds(tree.getMinutesPerStage());
		final long seenTick = (record.getTimestamp() + offset) / tickSeconds;
		final int ticksRemaining = (tree.getStages() - 1) - seen.getStage();
		final long readyAt = (seenTick + ticksRemaining) * tickSeconds - offset;

		final long nowTick = (Instant.now().getEpochSecond() + offset) / tickSeconds;
		final long elapsed = nowTick - seenTick;
		final PatchState estimate = elapsed >= ticksRemaining
			? new PatchState(tree, TreeState.READY, tree.getStages() - 1)
			: new PatchState(tree, TreeState.GROWING, seen.getStage() + (int) elapsed);
		return new PatchStatus(estimate, record.getTimestamp(), readyAt, record.isFromTimeTracking());
	}
}
