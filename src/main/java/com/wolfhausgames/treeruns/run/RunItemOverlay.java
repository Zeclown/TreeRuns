package com.wolfhausgames.treeruns.run;

import com.wolfhausgames.treeruns.TreeRunsConfig;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

/**
 * Outlines the inventory items the current step uses, such as the sapling to plant.
 */
public class RunItemOverlay extends WidgetItemOverlay
{
	private final RunSession runSession;
	private final TreeRunsConfig config;
	private final ItemManager itemManager;

	@Inject
	RunItemOverlay(RunSession runSession, TreeRunsConfig config, ItemManager itemManager)
	{
		this.runSession = runSession;
		this.config = config;
		this.itemManager = itemManager;
		showOnInventory();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		if (!config.highlightItems())
		{
			return;
		}

		final RunStep step = runSession.getCurrentStep();
		// Noted payments count too, so compare the unnoted form
		if (step == null || !step.getItemIds().contains(itemManager.canonicalize(itemId)))
		{
			return;
		}

		final Rectangle bounds = widgetItem.getCanvasBounds();
		final BufferedImage outline = itemManager.getItemOutline(itemId, widgetItem.getQuantity(), config.highlightColor());
		graphics.drawImage(outline, (int) bounds.getX(), (int) bounds.getY(), null);
	}
}
