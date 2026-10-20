package com.wolfhausgames.treeruns;

import com.wolfhausgames.treeruns.data.Patch;
import com.wolfhausgames.treeruns.plan.CategoryPlan;
import com.wolfhausgames.treeruns.plan.PlanManager;
import com.wolfhausgames.treeruns.plan.RunPlanner;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

/**
 * The run order, reordered by dragging stops up and down.
 */
class RoutePanel extends JPanel
{
	private static final Border ROW_BORDER = new EmptyBorder(5, 6, 5, 6);
	private static final Border DROP_ABOVE = BorderFactory.createCompoundBorder(
		BorderFactory.createMatteBorder(2, 0, 0, 0, ColorScheme.BRAND_ORANGE), new EmptyBorder(3, 6, 5, 6));
	private static final Border DROP_BELOW = BorderFactory.createCompoundBorder(
		BorderFactory.createMatteBorder(0, 0, 2, 0, ColorScheme.BRAND_ORANGE), new EmptyBorder(5, 6, 3, 6));

	private final PlanManager planManager;
	private final RunPlanner runPlanner;
	private final Runnable onChange;

	private final JPanel list = new JPanel();
	private final JButton reset = new JButton("Reset to default order");
	private final List<Row> rows = new ArrayList<>();

	@Nullable
	private Row dragging;
	/** Where the dragged row would land: before this index, or rows.size() for the end. */
	private int dropIndex = -1;

	RoutePanel(PlanManager planManager, RunPlanner runPlanner, Runnable onChange)
	{
		this.planManager = planManager;
		this.runPlanner = runPlanner;
		this.onChange = onChange;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		final JLabel hint = new JLabel("<html>Drag stops to reorder the run.</html>");
		hint.setFont(FontManager.getRunescapeSmallFont());
		hint.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		hint.setBorder(new EmptyBorder(0, 0, 6, 0));
		add(hint, BorderLayout.NORTH);

		list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
		list.setBackground(ColorScheme.DARK_GRAY_COLOR);
		add(list, BorderLayout.CENTER);

		reset.setFont(FontManager.getRunescapeSmallFont());
		reset.setFocusPainted(false);
		reset.addActionListener(e ->
		{
			planManager.resetRoute();
			onChange.run();
		});
		add(reset, BorderLayout.SOUTH);
	}

	void update()
	{
		// Don't pull rows out from under an active drag
		if (dragging != null)
		{
			return;
		}

		rows.clear();
		list.removeAll();

		final List<Patch> route = planManager.getRunRoute();
		final List<Patch> growing = runPlanner.getGrowing();
		if (route.isEmpty())
		{
			final JLabel empty = new JLabel("<html>No patches in the run yet. Pick trees in the Plan tab.</html>");
			empty.setFont(FontManager.getRunescapeSmallFont());
			empty.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			list.add(empty);
		}

		for (int i = 0; i < route.size(); i++)
		{
			final Row row = new Row(route.get(i), i + 1, growing.contains(route.get(i)));
			rows.add(row);
			list.add(row);
		}

		reset.setEnabled(planManager.isRouteCustomized());
		list.revalidate();
		list.repaint();
	}

	private void updateDropIndicator()
	{
		for (int i = 0; i < rows.size(); i++)
		{
			final Row row = rows.get(i);
			if (dragging != null && i == dropIndex)
			{
				row.setBorder(DROP_ABOVE);
			}
			else if (dragging != null && i == rows.size() - 1 && dropIndex == rows.size())
			{
				row.setBorder(DROP_BELOW);
			}
			else
			{
				row.setBorder(ROW_BORDER);
			}
			row.setBackground(row == dragging ? ColorScheme.DARK_GRAY_HOVER_COLOR : ColorScheme.DARKER_GRAY_COLOR);
		}
	}

	/** Which gap a point in the list falls into: before row i, or after the last row. */
	private int dropIndexAt(Point inList)
	{
		for (int i = 0; i < rows.size(); i++)
		{
			final Row row = rows.get(i);
			if (inList.y < row.getY() + row.getHeight() / 2)
			{
				return i;
			}
		}
		return rows.size();
	}

	private void drop()
	{
		final Row dragged = dragging;
		final int index = dropIndex;
		dragging = null;
		dropIndex = -1;
		if (dragged == null || index < 0)
		{
			updateDropIndicator();
			return;
		}

		final int from = rows.indexOf(dragged);
		// Dropping just above or below itself doesn't move it
		if (index != from && index != from + 1)
		{
			planManager.moveInRoute(dragged.patch, index < rows.size() ? rows.get(index).patch : null);
		}
		onChange.run();
	}

	private final class Row extends JPanel
	{
		private final Patch patch;

		private Row(Patch patch, int number, boolean growing)
		{
			this.patch = patch;
			setLayout(new BorderLayout());
			setBorder(ROW_BORDER);
			setBackground(ColorScheme.DARKER_GRAY_COLOR);
			setAlignmentX(Component.LEFT_ALIGNMENT);
			setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

			final CategoryPlan plan = planManager.get(patch.getKind());
			final JLabel name = new JLabel(number + ". " + patch.getLocationName());
			name.setFont(FontManager.getRunescapeSmallFont());
			name.setForeground(growing ? ColorScheme.MEDIUM_GRAY_COLOR : Color.WHITE);
			add(name, BorderLayout.WEST);

			final JLabel tree = new JLabel(growing ? "Growing" : plan.getTree() == null ? "" : plan.getTree().getDisplayName());
			tree.setFont(FontManager.getRunescapeSmallFont());
			tree.setForeground(growing ? ColorScheme.PROGRESS_INPROGRESS_COLOR : ColorScheme.LIGHT_GRAY_COLOR);
			if (growing)
			{
				setToolTipText("Still growing, so it is left out of this run.");
			}
			add(tree, BorderLayout.EAST);

			final MouseAdapter drag = new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent e)
				{
					dragging = Row.this;
					dropIndex = -1;
					updateDropIndicator();
				}

				@Override
				public void mouseDragged(MouseEvent e)
				{
					if (dragging == null)
					{
						return;
					}
					dropIndex = dropIndexAt(SwingUtilities.convertPoint(Row.this, e.getPoint(), list));
					updateDropIndicator();
				}

				@Override
				public void mouseReleased(MouseEvent e)
				{
					drop();
				}
			};
			addMouseListener(drag);
			addMouseMotionListener(drag);
		}
	}
}
