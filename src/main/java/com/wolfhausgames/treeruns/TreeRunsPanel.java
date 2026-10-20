package com.wolfhausgames.treeruns;

import com.wolfhausgames.treeruns.data.PatchKind;
import com.wolfhausgames.treeruns.plan.PlanManager;
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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

class TreeRunsPanel extends PluginPanel
{
	private final UnlockTracker unlockTracker;
	private final ItemTracker itemTracker;

	private final JLabel loginHint = smallLabel("Log in to plan your tree run.", ColorScheme.LIGHT_GRAY_COLOR);
	private final JLabel farmingLevel = smallLabel("", Color.WHITE);
	private final JLabel bankStatus = smallLabel("", ColorScheme.LIGHT_GRAY_COLOR);
	private final JPanel sections = new JPanel();
	private final List<CategorySection> categorySections = new ArrayList<>();

	@Inject
	TreeRunsPanel(PlanManager planManager, PatchTracker patchTracker, UnlockTracker unlockTracker,
		ItemTracker itemTracker, ItemNames itemNames)
	{
		this.unlockTracker = unlockTracker;
		this.itemTracker = itemTracker;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		final JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
		top.setBackground(ColorScheme.DARK_GRAY_COLOR);

		final JLabel title = new JLabel("Tree Runs");
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(ColorScheme.BRAND_ORANGE);
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		title.setBorder(new EmptyBorder(0, 0, 6, 0));
		top.add(title);
		top.add(loginHint);
		top.add(farmingLevel);
		top.add(bankStatus);
		top.add(Box.createVerticalStrut(8));
		add(top, BorderLayout.NORTH);

		sections.setLayout(new BoxLayout(sections, BoxLayout.Y_AXIS));
		sections.setBackground(ColorScheme.DARK_GRAY_COLOR);
		for (PatchKind kind : PatchKind.values())
		{
			final CategorySection section = new CategorySection(kind, planManager, unlockTracker, patchTracker,
				itemNames, this::update);
			categorySections.add(section);
			sections.add(section);
		}
		add(sections, BorderLayout.CENTER);

		update();
	}

	/** Redraws the panel from the trackers and plan. Safe to call from any thread. */
	void refresh()
	{
		SwingUtilities.invokeLater(this::update);
	}

	private void update()
	{
		final boolean loaded = unlockTracker.isLoaded();
		loginHint.setVisible(!loaded);
		farmingLevel.setVisible(loaded);
		bankStatus.setVisible(loaded);
		sections.setVisible(loaded);

		if (loaded)
		{
			final int real = unlockTracker.getFarmingLevel();
			final int boosted = unlockTracker.getBoostedFarmingLevel();
			farmingLevel.setText("Farming level: " + real + (boosted != real ? " (boosted " + boosted + ")" : ""));
			bankStatus.setText(itemTracker.isBankKnown() ? "Bank contents saved." : "Open your bank once to record its contents.");
			categorySections.forEach(CategorySection::update);
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
