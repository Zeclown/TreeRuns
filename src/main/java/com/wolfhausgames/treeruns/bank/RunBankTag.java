/*
 * The bank title bar button is adapted from Farm Run Autopilot's FarmBankTabButton, which follows
 * Quest Helper's QuestBankTabInterface (both BSD 2-Clause). See THIRD_PARTY_NOTICES.
 */
package com.wolfhausgames.treeruns.bank;

import com.wolfhausgames.treeruns.plan.RunPlanner;
import com.wolfhausgames.treeruns.plan.ShoppingLine;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.ScriptEvent;
import net.runelite.api.ScriptID;
import net.runelite.api.SoundEffectID;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.banktags.BankTagsService;
import net.runelite.client.plugins.banktags.TagManager;

/**
 * A button in the bank's title bar that shows only the items the planned run needs, as a Bank Tags
 * tag. Bank Tags does the filtering, so withdrawing works as usual. Client-side only.
 */
@Singleton
public class RunBankTag
{
	private static final String TAG = "tree runs";
	private static final String ACTION = "Show tree run items";
	private static final int BUTTON_SIZE = 25;
	/** Left of Quest Helper's button (x 408), which sits left of the close button. */
	private static final int BUTTON_X = 382;
	private static final int BUTTON_Y = 5;

	private final Client client;
	private final PluginManager pluginManager;
	private final BankTagsService bankTagsService;
	private final TagManager tagManager;
	private final ChatMessageManager chatMessageManager;
	private final RunPlanner runPlanner;

	/** The items shown under the tag, worked out each time it's opened. */
	private volatile Set<Integer> neededItems = Collections.emptySet();
	private Widget background;
	private Widget icon;

	@Inject
	RunBankTag(Client client, PluginManager pluginManager, BankTagsService bankTagsService, TagManager tagManager,
		ChatMessageManager chatMessageManager, RunPlanner runPlanner)
	{
		this.client = client;
		this.pluginManager = pluginManager;
		this.bankTagsService = bankTagsService;
		this.tagManager = tagManager;
		this.chatMessageManager = chatMessageManager;
		this.runPlanner = runPlanner;
	}

	public void startUp()
	{
		tagManager.registerTag(TAG, itemId -> neededItems.contains(itemId));
	}

	/** Must be called on the client thread. */
	public void shutDown()
	{
		if (isOpen())
		{
			bankTagsService.closeBankTag();
		}
		tagManager.unregisterTag(TAG);
		hide(background);
		hide(icon);
		background = null;
		icon = null;
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() != InterfaceID.BANKMAIN)
		{
			return;
		}

		final Widget parent = client.getWidget(InterfaceID.Bankmain.UNIVERSE);
		if (parent == null)
		{
			return;
		}
		background = createGraphic(parent, SpriteID.Miscgraphics3.UNKNOWN_BUTTON_SQUARE_SMALL, BUTTON_SIZE,
			BUTTON_X, BUTTON_Y);
		icon = createGraphic(parent, SpriteID.Staticons2.FARMING, BUTTON_SIZE - 6, BUTTON_X + 3, BUTTON_Y + 3);
		updateButton();
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		// The bank redraws whenever a tab, tag or search changes, so follow whatever is showing now
		if (event.getScriptId() == ScriptID.BANKMAIN_FINISHBUILDING)
		{
			updateButton();
		}
	}

	private void onClick(ScriptEvent event)
	{
		if (event.getOp() != 1)
		{
			return;
		}

		if (isOpen())
		{
			bankTagsService.closeBankTag();
		}
		else if (!pluginManager.isPluginActive((Plugin) bankTagsService))
		{
			chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage("Turn on the Bank Tags plugin to see your tree run items in the bank.")
				.build());
			return;
		}
		else
		{
			final Set<Integer> items = new HashSet<>();
			for (ShoppingLine line : runPlanner.getShoppingList())
			{
				items.addAll(line.getItemIds());
			}
			neededItems = items;
			bankTagsService.openBankTag(TAG, BankTagsService.OPTION_NO_LAYOUT);
		}
		client.playSoundEffect(SoundEffectID.UI_BOOP);
		updateButton();
	}

	private boolean isOpen()
	{
		return TAG.equals(bankTagsService.getActiveTag());
	}

	private void updateButton()
	{
		if (background == null)
		{
			return;
		}
		final int sprite = isOpen() ? SpriteID.Miscgraphics3.UNKNOWN_BUTTON_SQUARE_SMALL_SELECTED
			: SpriteID.Miscgraphics3.UNKNOWN_BUTTON_SQUARE_SMALL;
		if (background.getSpriteId() != sprite)
		{
			background.setSpriteId(sprite);
			background.revalidate();
		}
	}

	private Widget createGraphic(Widget parent, int spriteId, int size, int x, int y)
	{
		final Widget widget = parent.createChild(-1, WidgetType.GRAPHIC);
		widget.setOriginalWidth(size);
		widget.setOriginalHeight(size);
		widget.setOriginalX(x);
		widget.setOriginalY(y);
		widget.setSpriteId(spriteId);
		widget.setName("Tree Runs");
		// Both halves take the click, so it works wherever the button is pressed
		widget.setAction(0, ACTION);
		widget.setHasListener(true);
		widget.setOnOpListener((JavaScriptCallback) this::onClick);
		widget.revalidate();
		return widget;
	}

	private static void hide(Widget widget)
	{
		if (widget != null)
		{
			widget.setHidden(true);
		}
	}
}
