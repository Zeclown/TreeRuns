package com.wolfhausgames.treeruns;

import com.wolfhausgames.treeruns.data.PatchKind;
import com.wolfhausgames.treeruns.plan.PlanManager;
import com.wolfhausgames.treeruns.plan.RunPlanner;
import com.wolfhausgames.treeruns.run.RunSession;
import com.wolfhausgames.treeruns.state.ItemNames;
import com.wolfhausgames.treeruns.state.ItemTracker;
import com.wolfhausgames.treeruns.state.PatchTracker;
import com.wolfhausgames.treeruns.state.UnlockTracker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;

class TreeRunsPanel extends PluginPanel
{
	private final UnlockTracker unlockTracker;
	private final ItemTracker itemTracker;
	private final RunPlanner runPlanner;
	private final RunSession runSession;

	private final JLabel loginHint = smallLabel("Log in to plan your tree run.", ColorScheme.LIGHT_GRAY_COLOR);
	private final JLabel farmingLevel = smallLabel("", Color.WHITE);
	private final JLabel bankStatus = smallLabel("", ColorScheme.LIGHT_GRAY_COLOR);
	private final JButton startRun = new JButton();

	private final JPanel planning = new JPanel(new BorderLayout());
	private final List<CategorySection> categorySections = new ArrayList<>();
	private final RoutePanel routePanel;
	private final ItemsSection itemsSection;
	private final RunPanel runPanel;

	@Inject
	TreeRunsPanel(TreeRunsPlugin plugin, PlanManager planManager, PatchTracker patchTracker, UnlockTracker unlockTracker,
		ItemTracker itemTracker, ItemNames itemNames, RunPlanner runPlanner, RunSession runSession)
	{
		this.unlockTracker = unlockTracker;
		this.itemTracker = itemTracker;
		this.runPlanner = runPlanner;
		this.runSession = runSession;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		final JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
		top.setBackground(ColorScheme.DARK_GRAY_COLOR);
		top.setBorder(new EmptyBorder(0, 0, 8, 0));

		final JLabel title = new JLabel("Tree Runs");
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(ColorScheme.BRAND_ORANGE);
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		title.setBorder(new EmptyBorder(0, 0, 6, 0));
		top.add(title);
		top.add(loginHint);
		top.add(farmingLevel);
		top.add(bankStatus);
		add(top, BorderLayout.NORTH);

		// Planning: start button above Plan / Route tabs
		planning.setBackground(ColorScheme.DARK_GRAY_COLOR);
		startRun.setFont(FontManager.getRunescapeBoldFont());
		startRun.setFocusPainted(false);
		startRun.addActionListener(e -> plugin.startRun());

		final JPanel display = new JPanel(new BorderLayout());
		display.setBackground(ColorScheme.DARK_GRAY_COLOR);
		display.setBorder(new EmptyBorder(6, 0, 0, 0));
		final MaterialTabGroup tabs = new MaterialTabGroup(display);
		tabs.setBorder(new EmptyBorder(8, 0, 0, 0));

		final JPanel planTab = new JPanel();
		planTab.setLayout(new BoxLayout(planTab, BoxLayout.Y_AXIS));
		planTab.setBackground(ColorScheme.DARK_GRAY_COLOR);
		itemsSection = new ItemsSection(runPlanner, itemTracker, this::update);
		planTab.add(itemsSection);
		for (PatchKind kind : PatchKind.values())
		{
			final CategorySection section = new CategorySection(kind, planManager, unlockTracker, patchTracker,
				itemNames, this::update);
			categorySections.add(section);
			planTab.add(section);
		}

		routePanel = new RoutePanel(planManager, runPlanner, this::update);

		final MaterialTab plan = new MaterialTab("Plan", tabs, planTab);
		tabs.addTab(plan);
		tabs.addTab(new MaterialTab("Route", tabs, routePanel));
		tabs.select(plan);

		final JPanel header = new JPanel(new BorderLayout());
		header.setBackground(ColorScheme.DARK_GRAY_COLOR);
		header.add(startRun, BorderLayout.NORTH);
		header.add(tabs, BorderLayout.CENTER);
		planning.add(header, BorderLayout.NORTH);
		planning.add(display, BorderLayout.CENTER);
		add(planning, BorderLayout.CENTER);

		runPanel = new RunPanel(runSession, plugin);

		update();
	}

	/** Redraws the panel. Safe to call from any thread. */
	void refresh()
	{
		SwingUtilities.invokeLater(this::update);
	}

	private void update()
	{
		final boolean loaded = unlockTracker.isLoaded();
		final boolean running = runSession.isActive();
		loginHint.setVisible(!loaded);
		farmingLevel.setVisible(loaded);
		bankStatus.setVisible(loaded && !running);

		// Swap between planning and the run view
		final Component wanted = running ? runPanel : planning;
		if (wanted.getParent() != this)
		{
			remove(running ? planning : runPanel);
			add(wanted, BorderLayout.CENTER);
		}
		wanted.setVisible(loaded);

		if (loaded)
		{
			final int real = unlockTracker.getFarmingLevel();
			final int boosted = unlockTracker.getBoostedFarmingLevel();
			farmingLevel.setText("Farming level: " + real + (boosted != real ? " (boosted " + boosted + ")" : ""));
			bankStatus.setText(itemTracker.isBankKnown() ? "Bank contents saved." : "Open your bank once to record its contents.");

			if (running)
			{
				runPanel.update();
			}
			else
			{
				final int stops = runPlanner.getStops().size();
				startRun.setText(stops == 0 ? "Nothing to do yet" : "Start run (" + stops + " patches)");
				startRun.setEnabled(stops > 0);
				categorySections.forEach(CategorySection::update);
				routePanel.update();
				itemsSection.update();
			}
		}

		revalidate();
		repaint();
	}

	private static JLabel smallLabel(String text, Color color)
	{
		final JLabel label = new JLabel(text);
		label.setFont(FontManager.getRunescapeSmallFont());
		label.setForeground(color);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
	}
}
