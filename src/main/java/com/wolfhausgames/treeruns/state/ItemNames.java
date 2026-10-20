package com.wolfhausgames.treeruns.state;

import com.wolfhausgames.treeruns.data.ItemStack;
import com.wolfhausgames.treeruns.data.TrackedItems;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.game.ItemManager;

/**
 * Item names for the panel. Names can only be looked up on the client thread, so the
 * tracked items are looked up once and cached.
 */
@Singleton
public class ItemNames
{
	private final ItemManager itemManager;
	private volatile Map<Integer, String> names = Collections.emptyMap();

	@Inject
	ItemNames(ItemManager itemManager)
	{
		this.itemManager = itemManager;
	}

	/** Must be called on the client thread. */
	public void load()
	{
		final Map<Integer, String> loaded = new HashMap<>();
		for (int itemId : TrackedItems.all())
		{
			loaded.put(itemId, itemManager.getItemComposition(itemId).getName());
		}
		names = Collections.unmodifiableMap(loaded);
	}

	public String get(int itemId)
	{
		return names.getOrDefault(itemId, "Item " + itemId);
	}

	/** e.g. "10 x Pineapple". */
	public String describe(ItemStack stack)
	{
		return stack.getQuantity() + " x " + get(stack.getItemId());
	}
}
