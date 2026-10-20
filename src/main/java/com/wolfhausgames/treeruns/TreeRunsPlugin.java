package com.wolfhausgames.treeruns;

import com.google.inject.Provides;
import com.wolfhausgames.treeruns.data.Patch;
import com.wolfhausgames.treeruns.data.PatchSite;
import com.wolfhausgames.treeruns.plan.PlanManager;
import com.wolfhausgames.treeruns.plan.RunPlanner;
import com.wolfhausgames.treeruns.run.RunHighlightOverlay;
import com.wolfhausgames.treeruns.run.RunItemOverlay;
import com.wolfhausgames.treeruns.run.RunSession;
import com.wolfhausgames.treeruns.run.RunStepOverlay;
import com.wolfhausgames.treeruns.run.SceneTracker;
import com.wolfhausgames.treeruns.state.ItemNames;
import com.wolfhausgames.treeruns.state.ItemTracker;
import com.wolfhausgames.treeruns.state.PatchTracker;
import com.wolfhausgames.treeruns.state.UnlockTracker;
import java.awt.image.BufferedImage;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.StatChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PluginMessage;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Tree Run Planner",
	description = "Plan and follow customizable farming tree runs",
	tags = {"farming", "tree", "run", "helper"}
)
public class TreeRunsPlugin extends Plugin
{
	public static final String CONFIG_GROUP = "treeruns";

	/** Quests can't be watched directly, so unlocks are re-checked about once a minute. */
	private static final int UNLOCK_REFRESH_TICKS = 100;

	private static final String SHORTEST_PATH = "shortestpath";

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private EventBus eventBus;

	@Inject
	private TreeRunsConfig config;

	@Inject
	private PatchTracker patchTracker;

	@Inject
	private UnlockTracker unlockTracker;

	@Inject
	private ItemTracker itemTracker;

	@Inject
	private ItemNames itemNames;

	@Inject
	private PlanManager planManager;

	@Inject
	private RunPlanner runPlanner;

	@Inject
	private RunSession runSession;

	@Inject
	private SceneTracker sceneTracker;

	@Inject
	private RunHighlightOverlay highlightOverlay;

	@Inject
	private RunStepOverlay stepOverlay;

	@Inject
	private RunItemOverlay itemOverlay;

	private TreeRunsPanel panel;
	private NavigationButton navButton;
	private int ticksUntilUnlockRefresh;

	/** The patch this plugin's hint arrow points at, or null if the arrow isn't ours. */
	@Nullable
	private Patch hintArrowPatch;
	/** The patch Shortest Path was last sent to, or null if it hasn't been sent one. */
	@Nullable
	private Patch shortestPathPatch;

	@Provides
	TreeRunsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TreeRunsConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		panel = injector.getInstance(TreeRunsPanel.class);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");
		navButton = NavigationButton.builder()
			.tooltip("Tree Run Planner")
			.icon(icon)
			.priority(7)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
		overlayManager.add(highlightOverlay);
		overlayManager.add(stepOverlay);
		overlayManager.add(itemOverlay);

		clientThread.invoke(() ->
		{
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				loadProfile();
				sceneTracker.scan();
			}
		});
		log.debug("Tree Run Planner started");
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientThread.invoke(() ->
		{
			runSession.end();
			updateGuidance();
		});
		overlayManager.remove(highlightOverlay);
		overlayManager.remove(stepOverlay);
		overlayManager.remove(itemOverlay);
		clientToolbar.removeNavigation(navButton);
		sceneTracker.clear();
		unlockTracker.clear();
		navButton = null;
		log.debug("Tree Run Planner stopped");
	}

	/** Starts a run over the current stops. Safe to call from any thread. */
	void startRun()
	{
		clientThread.invoke(() ->
		{
			runSession.start(runPlanner.getStops());
			afterRunChange();
		});
	}

	void nextStop()
	{
		clientThread.invoke(() ->
		{
			runSession.next();
			afterRunChange();
		});
	}

	void previousStop()
	{
		clientThread.invoke(() ->
		{
			runSession.back();
			afterRunChange();
		});
	}

	void endRun()
	{
		clientThread.invoke(() ->
		{
			runSession.end();
			afterRunChange();
		});
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		runSession.end();
		updateGuidance();
		loadProfile();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		switch (event.getGameState())
		{
			case LOGGED_IN:
				// Quest and skill state arrive with the first ticks, so check on the next one.
				ticksUntilUnlockRefresh = 0;
				break;
			case LOADING:
				// Objects and NPCs spawn again once the new scene is loaded
				sceneTracker.clear();
				break;
			case LOGIN_SCREEN:
				unlockTracker.clear();
				panel.refresh();
				break;
			default:
				break;
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
		if (player != null)
		{
			final WorldPoint location = player.getWorldLocation();
			changed |= patchTracker.update(location);
			changed |= itemTracker.refreshToolStore();
			if (runSession.isActive() && runSession.onTick(location))
			{
				changed = true;
			}
		}

		updateGuidance();
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

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() == ChatMessageType.GAMEMESSAGE && runSession.isActive())
		{
			runSession.onGameMessage(event.getMessage());
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		sceneTracker.onObjectSpawned(event.getGameObject());
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		sceneTracker.onObjectDespawned(event.getGameObject());
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		sceneTracker.onNpcSpawned(event.getNpc());
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		sceneTracker.onNpcDespawned(event.getNpc());
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (CONFIG_GROUP.equals(event.getGroup()))
		{
			// Point (or clear) the hint arrow and Shortest Path with the new settings
			clientThread.invoke(this::updateGuidance);
		}
	}

	/** Must be called on the client thread. */
	private void afterRunChange()
	{
		final Player player = client.getLocalPlayer();
		if (player != null && runSession.isActive())
		{
			runSession.onTick(player.getWorldLocation());
		}
		updateGuidance();
		panel.refresh();
	}

	/**
	 * Points the hint arrow and Shortest Path at the current stop, and clears whatever this
	 * plugin set once the run ends. Must be called on the client thread.
	 */
	private void updateGuidance()
	{
		final Patch current = runSession.getCurrent();
		updateHintArrow(current != null && config.hintArrow() ? current : null);

		// Only message Shortest Path when the destination changes, so it isn't asked to re-path every tick
		final Patch pathTarget = current != null && config.shortestPath() ? current : null;
		if (pathTarget != shortestPathPatch)
		{
			if (pathTarget != null)
			{
				eventBus.post(new PluginMessage(SHORTEST_PATH, "path",
					Map.<String, Object>of("target", PatchSite.of(pathTarget).getLocation())));
			}
			else
			{
				eventBus.post(new PluginMessage(SHORTEST_PATH, "clear"));
			}
			shortestPathPatch = pathTarget;
		}
	}

	/**
	 * Points the hint arrow at a patch, without ever replacing or clearing an arrow the game set,
	 * such as a quest arrow on an NPC.
	 */
	private void updateHintArrow(@Nullable Patch wanted)
	{
		// Our arrow is gone if the game replaced it or cleared it (e.g. on a loading screen)
		if (hintArrowPatch != null && !isOurHintArrow(hintArrowPatch))
		{
			hintArrowPatch = null;
		}
		if (wanted == hintArrowPatch)
		{
			return;
		}

		if (hintArrowPatch != null)
		{
			client.clearHintArrow();
			hintArrowPatch = null;
		}
		if (wanted != null && !client.hasHintArrow())
		{
			client.setHintArrow(PatchSite.of(wanted).getLocation());
			hintArrowPatch = wanted;
		}
	}

	private boolean isOurHintArrow(Patch patch)
	{
		return client.hasHintArrow()
			&& client.getHintArrowNpc() == null
			&& client.getHintArrowPlayer() == null
			&& PatchSite.of(patch).getLocation().equals(client.getHintArrowPoint());
	}

	private void loadProfile()
	{
		patchTracker.load();
		itemTracker.load();
		itemNames.load();
		planManager.load();
		ticksUntilUnlockRefresh = 0;
		panel.refresh();
	}
}
