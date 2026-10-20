package com.wolfhausgames.treeruns.state;

import com.wolfhausgames.treeruns.TreeRunsPlugin;
import com.wolfhausgames.treeruns.data.TrackedItems;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;

/**
 * Counts what the player is carrying and what was in their bank the last time it was opened.
 * Noted items are counted as their unnoted form, since gardeners accept either.
 */
@Singleton
public class ItemTracker
{
	private static final String BANK_KEY = "bank";
	private static final String BANK_SEEN_KEY = "bankSeen";

	private final ItemManager itemManager;
	private final ConfigManager configManager;

	private volatile Map<Integer, Integer> inventory = Collections.emptyMap();
	private volatile Map<Integer, Integer> bank = Collections.emptyMap();
	private volatile boolean bankKnown;

	@Inject
	ItemTracker(ItemManager itemManager, ConfigManager configManager)
	{
		this.itemManager = itemManager;
		this.configManager = configManager;
	}

	/** Reloads the saved bank for the current account. */
	public void load()
	{
		inventory = Collections.emptyMap();
		bank = deserialize(configManager.getRSProfileConfiguration(TreeRunsPlugin.CONFIG_GROUP, BANK_KEY));
		bankKnown = configManager.getRSProfileConfiguration(TreeRunsPlugin.CONFIG_GROUP, BANK_SEEN_KEY) != null;
	}

	/**
	 * Must be called on the client thread.
	 *
	 * @return whether the container is one this tracker cares about
	 */
	public boolean onContainerChanged(int containerId, ItemContainer container)
	{
		if (containerId == InventoryID.INV)
		{
			inventory = count(container);
			return true;
		}
		if (containerId == InventoryID.BANK)
		{
			final Map<Integer, Integer> tracked = new HashMap<>();
			count(container).forEach((id, quantity) ->
			{
				if (TrackedItems.isTracked(id))
				{
					tracked.put(id, quantity);
				}
			});
			bank = Collections.unmodifiableMap(tracked);
			bankKnown = true;
			configManager.setRSProfileConfiguration(TreeRunsPlugin.CONFIG_GROUP, BANK_SEEN_KEY, true);
			configManager.setRSProfileConfiguration(TreeRunsPlugin.CONFIG_GROUP, BANK_KEY, serialize(bank));
			return true;
		}
		return false;
	}

	public boolean isBankKnown()
	{
		return bankKnown;
	}

	public int getInventoryCount(int itemId)
	{
		return inventory.getOrDefault(itemId, 0);
	}

	public int getBankCount(int itemId)
	{
		return bank.getOrDefault(itemId, 0);
	}

	private Map<Integer, Integer> count(ItemContainer container)
	{
		final Map<Integer, Integer> counts = new HashMap<>();
		for (Item item : container.getItems())
		{
			if (item.getId() <= 0 || item.getQuantity() <= 0)
			{
				continue;
			}
			counts.merge(itemManager.canonicalize(item.getId()), item.getQuantity(), Integer::sum);
		}
		return Collections.unmodifiableMap(counts);
	}

	private static String serialize(Map<Integer, Integer> counts)
	{
		final StringBuilder sb = new StringBuilder();
		for (Map.Entry<Integer, Integer> entry : counts.entrySet())
		{
			if (sb.length() > 0)
			{
				sb.append(',');
			}
			sb.append(entry.getKey()).append(':').append(entry.getValue());
		}
		return sb.toString();
	}

	private static Map<Integer, Integer> deserialize(String value)
	{
		if (value == null || value.isEmpty())
		{
			return Collections.emptyMap();
		}

		final Map<Integer, Integer> counts = new HashMap<>();
		for (String entry : value.split(","))
		{
			final String[] parts = entry.split(":");
			if (parts.length != 2)
			{
				continue;
			}
			try
			{
				counts.put(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
			}
			catch (NumberFormatException ignored)
			{
				// skip corrupt entries
			}
		}
		return Collections.unmodifiableMap(counts);
	}
}
