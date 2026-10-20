package com.wolfhausgames.treeruns;

import com.wolfhausgames.treeruns.data.Patch;
import com.wolfhausgames.treeruns.plan.RunPlanner;
import com.wolfhausgames.treeruns.plan.ShoppingLine;
import com.wolfhausgames.treeruns.state.ItemTracker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

/**
 * Everything the run needs, and how much of it the player has.
 */
class ShoppingPanel extends JPanel
{
	private final RunPlanner runPlanner;
	private final ItemTracker itemTracker;

	ShoppingPanel(RunPlanner runPlanner, ItemTracker itemTracker)
	{
		this.runPlanner = runPlanner;
		this.itemTracker = itemTracker;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(ColorScheme.DARK_GRAY_COLOR);
	}

	void update()
	{
		removeAll();

		final List<ShoppingLine> lines = runPlanner.getShoppingList();
		if (lines.isEmpty())
		{
			add(note("No patches need visiting right now.", ColorScheme.LIGHT_GRAY_COLOR));
		}
		else if (!itemTracker.isBankKnown())
		{
			add(note("Open your bank to include what's in it.", ColorScheme.PROGRESS_INPROGRESS_COLOR));
		}

		for (ShoppingLine line : lines)
		{
			add(row(line));
		}

		final List<Patch> growing = runPlanner.getGrowing();
		if (!growing.isEmpty())
		{
			add(note("Not needed yet (still growing): " + growing.stream()
				.map(Patch::getLocationName).collect(Collectors.joining(", ")), ColorScheme.MEDIUM_GRAY_COLOR));
		}

		if (!lines.isEmpty())
		{
			add(note("Green: carried or with the tool leprechaun. Orange: in the bank. Red: missing.", ColorScheme.MEDIUM_GRAY_COLOR));
		}

		revalidate();
		repaint();
	}

	private static Component row(ShoppingLine line)
	{
		final JPanel row = new JPanel(new BorderLayout());
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(new EmptyBorder(4, 6, 4, 6));
		row.setAlignmentX(Component.LEFT_ALIGNMENT);

		final Color color = line.isAtHand() ? ColorScheme.PROGRESS_COMPLETE_COLOR
			: line.isAvailable() ? ColorScheme.PROGRESS_INPROGRESS_COLOR
			: line.isOptional() ? ColorScheme.MEDIUM_GRAY_COLOR
			: ColorScheme.PROGRESS_ERROR_COLOR;

		final JLabel name = new JLabel("<html>" + line.getNeeded() + " x " + line.getName()
			+ (line.isOptional() ? " (optional)" : "") + "</html>");
		name.setFont(FontManager.getRunescapeSmallFont());
		name.setForeground(color);
		row.add(name, BorderLayout.CENTER);

		final String have;
		if (line.isCarried())
		{
			have = "Have";
		}
		else if (line.isAtHand())
		{
			have = "Leprechaun";
		}
		else if (line.isAvailable())
		{
			have = "Bank " + line.getToWithdraw();
		}
		else
		{
			have = "Need " + line.getMissing();
		}
		final JLabel status = new JLabel(have);
		status.setFont(FontManager.getRunescapeSmallFont());
		status.setForeground(color);
		status.setToolTipText("Carrying " + line.getCarried()
			+ (line.getStored() > 0 ? ", " + line.getStored() + " with the tool leprechaun" : "")
			+ ", " + line.getBanked() + " in the bank");
		row.add(status, BorderLayout.EAST);

		final JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		wrapper.setBorder(new EmptyBorder(0, 0, 2, 0));
		wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		wrapper.add(row);
		return wrapper;
	}

	private static Component note(String text, Color color)
	{
		final JLabel label = new JLabel("<html>" + text + "</html>");
		label.setFont(FontManager.getRunescapeSmallFont());
		label.setForeground(color);
		label.setBorder(new EmptyBorder(4, 0, 4, 0));
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
	}
}
