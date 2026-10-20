package com.wolfhausgames.treeruns;

import com.wolfhausgames.treeruns.data.Patch;
import com.wolfhausgames.treeruns.data.PatchKind;
import com.wolfhausgames.treeruns.data.PatchState;
import com.wolfhausgames.treeruns.data.TreeState;
import com.wolfhausgames.treeruns.state.ItemTracker;
import com.wolfhausgames.treeruns.state.PatchStatus;
import com.wolfhausgames.treeruns.state.PatchTracker;
import com.wolfhausgames.treeruns.state.UnlockTracker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.time.Instant;
import java.util.List;
import javax.inject.Inject;
import javax.swing.BorderFactory;
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
	private final PatchTracker patchTracker;
	private final UnlockTracker unlockTracker;
	private final ItemTracker itemTracker;

	private final JPanel content = new JPanel();

	@Inject
	TreeRunsPanel(PatchTracker patchTracker, UnlockTracker unlockTracker, ItemTracker itemTracker)
	{
		this.patchTracker = patchTracker;
		this.unlockTracker = unlockTracker;
		this.itemTracker = itemTracker;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		final JLabel title = new JLabel("Tree Runs");
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(ColorScheme.BRAND_ORANGE);
		title.setBorder(new EmptyBorder(0, 0, 8, 0));
		add(title, BorderLayout.NORTH);

		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBackground(ColorScheme.DARK_GRAY_COLOR);
		add(content, BorderLayout.CENTER);

		rebuild();
	}

	/** Redraws the panel from the trackers. Safe to call from any thread. */
	void refresh()
	{
		SwingUtilities.invokeLater(this::rebuild);
	}

	private void rebuild()
	{
		content.removeAll();

		if (!unlockTracker.isLoaded())
		{
			content.add(smallLabel("Log in to see your patches.", ColorScheme.LIGHT_GRAY_COLOR));
		}
		else
		{
			content.add(smallLabel("Farming level: " + unlockTracker.getFarmingLevel()
				+ (unlockTracker.getBoostedFarmingLevel() != unlockTracker.getFarmingLevel()
				? " (boosted " + unlockTracker.getBoostedFarmingLevel() + ")" : ""), Color.WHITE));
			content.add(smallLabel(itemTracker.isBankKnown() ? "Bank contents saved." : "Open your bank once to record its contents.",
				ColorScheme.LIGHT_GRAY_COLOR));

			for (PatchKind kind : PatchKind.values())
			{
				content.add(Box.createVerticalStrut(8));
				content.add(sectionHeader(kind.getDisplayName()));
				for (Patch patch : Patch.forKind(kind))
				{
					content.add(patchRow(patch));
				}
			}
		}

		content.revalidate();
		content.repaint();
	}

	private Component patchRow(Patch patch)
	{
		final JPanel row = new JPanel(new BorderLayout());
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(new EmptyBorder(4, 6, 4, 6));
		row.setAlignmentX(Component.LEFT_ALIGNMENT);

		final List<String> missing = unlockTracker.getMissingRequirements(patch);
		final boolean unlocked = missing.isEmpty();

		final JLabel name = new JLabel(patch.getLocationName());
		name.setFont(FontManager.getRunescapeSmallFont());
		name.setForeground(unlocked ? Color.WHITE : ColorScheme.MEDIUM_GRAY_COLOR);
		row.add(name, BorderLayout.WEST);

		final JLabel status = new JLabel();
		status.setFont(FontManager.getRunescapeSmallFont());
		if (unlocked)
		{
			describe(patchTracker.getStatus(patch), status);
		}
		else
		{
			status.setText("Locked");
			status.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
			final JLabel reason = new JLabel("<html>Requires " + String.join(", ", missing) + "</html>");
			reason.setFont(FontManager.getRunescapeSmallFont());
			reason.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
			row.add(reason, BorderLayout.SOUTH);
		}
		row.add(status, BorderLayout.EAST);

		final JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
		wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		wrapper.add(row);
		return wrapper;
	}

	private static void describe(PatchStatus status, JLabel label)
	{
		if (!status.isSeen())
		{
			label.setText("Not seen yet");
			label.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
			label.setToolTipText("Visit this patch once so the plugin can read its state.");
			return;
		}

		final String source = status.isFromTimeTracking() ? " Last seen by the Time Tracking plugin." : "";
		final PatchState state = status.getState();
		final String tree = state.getTree() != null ? state.getTree().getDisplayName() + ": " : "";
		final TreeState treeState = state.getState();
		switch (treeState)
		{
			case GROWING:
				final long seconds = status.getReadyAt() - Instant.now().getEpochSecond();
				label.setText(tree + "~" + formatDuration(seconds));
				label.setForeground(ColorScheme.PROGRESS_INPROGRESS_COLOR);
				label.setToolTipText("Estimated from when you last saw this patch." + source);
				return;
			case READY:
			case CHECKED:
			case STUMP:
				label.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
				break;
			case DISEASED:
			case DEAD:
				label.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
				break;
			default:
				label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		}
		label.setText(tree + treeState.getDisplayName());
		label.setToolTipText(source.isEmpty() ? null : source.trim());
	}

	private static String formatDuration(long seconds)
	{
		final long minutes = Math.max(0, seconds) / 60;
		final long hours = minutes / 60;
		return hours > 0 ? hours + "h " + (minutes % 60) + "m" : (minutes % 60) + "m";
	}

	private static JLabel sectionHeader(String text)
	{
		final JLabel label = new JLabel(text);
		label.setFont(FontManager.getRunescapeBoldFont());
		label.setForeground(ColorScheme.BRAND_ORANGE);
		label.setBorder(new EmptyBorder(0, 0, 4, 0));
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
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
