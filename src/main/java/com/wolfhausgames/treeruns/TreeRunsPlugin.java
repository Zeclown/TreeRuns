package com.wolfhausgames.treeruns;

import com.wolfhausgames.treeruns.state.ItemTracker;
import com.wolfhausgames.treeruns.state.PatchTracker;
import com.wolfhausgames.treeruns.state.UnlockTracker;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Tree Runs",
	description = "Plan and follow customizable farming tree runs",
	tags = {"farming", "tree", "run", "helper"}
)
public class TreeRunsPlugin extends Plugin
{
	public static final String CONFIG_GROUP = "treeruns";

	/** Quests can't be watched directly, so unlocks are re-checked about once a minute. */
	private static final int UNLOCK_REFRESH_TICKS = 100;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private PatchTracker patchTracker;

	@Inject
	private UnlockTracker unlockTracker;

	@Inject
	private ItemTracker itemTracker;

	private TreeRunsPanel panel;
	private NavigationButton navButton;
	private int ticksUntilUnlockRefresh;

	@Override
	protected void startUp() throws Exception
	{
		panel = injector.getInstance(TreeRunsPanel.class);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");
		navButton = NavigationButton.builder()
			.tooltip("Tree Runs")
			.icon(icon)
			.priority(7)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);

		clientThread.invoke(() ->
		{
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				loadProfile();
			}
		});
		log.debug("Tree Runs started");
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		unlockTracker.clear();
		navButton = null;
		panel = null;
		log.debug("Tree Runs stopped");
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		loadProfile();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			// Quest and skill state arrive with the first ticks, so check on the next one.
			ticksUntilUnlockRefresh = 0;
		}
		else if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			unlockTracker.clear();
			panel.refresh();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		boolean changed = false;

		if (--ticksUntilUnlockRefresh <= 0)
		{
			ticksUntilUnlockRefresh = UNLOCK_REFRESH_TICKS;
			unlockTracker.refresh();
			// Also redraws the growth countdowns.
			changed = true;
		}

		final Player player = client.getLocalPlayer();
		if (player != null && patchTracker.update(player.getWorldLocation()))
		{
			changed = true;
		}

		if (changed)
		{
			panel.refresh();
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (event.getSkill() == Skill.FARMING || event.getSkill() == Skill.SAILING)
		{
			ticksUntilUnlockRefresh = 0;
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (itemTracker.onContainerChanged(event.getContainerId(), event.getItemContainer()))
		{
			panel.refresh();
		}
	}

	private void loadProfile()
	{
		patchTracker.load();
		itemTracker.load();
		ticksUntilUnlockRefresh = 0;
		panel.refresh();
	}
}
