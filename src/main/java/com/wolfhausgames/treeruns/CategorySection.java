package com.wolfhausgames.treeruns;

import com.wolfhausgames.treeruns.data.Patch;
import com.wolfhausgames.treeruns.data.PatchKind;
import com.wolfhausgames.treeruns.data.PatchState;
import com.wolfhausgames.treeruns.data.TreeState;
import com.wolfhausgames.treeruns.data.TreeType;
import com.wolfhausgames.treeruns.plan.CategoryPlan;
import com.wolfhausgames.treeruns.plan.PlanManager;
import com.wolfhausgames.treeruns.state.ItemNames;
import com.wolfhausgames.treeruns.state.PatchStatus;
import com.wolfhausgames.treeruns.state.PatchTracker;
import com.wolfhausgames.treeruns.state.UnlockTracker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

/**
 * One collapsible category in the planner: tree choice, payments and the patch checklist.
 * Built once and updated in place, so refreshing never closes an open dropdown.
 */
class CategorySection extends JPanel
{
	private final PatchKind kind;
	private final PlanManager planManager;
	private final UnlockTracker unlockTracker;
	private final PatchTracker patchTracker;
	private final ItemNames itemNames;

	private final JLabel header = new JLabel();
	private final JLabel summary = new JLabel();
	private final JPanel body = new JPanel();
	private final JComboBox<TreeOption> treeBox = new JComboBox<>();
	private final JCheckBox protect = new JCheckBox();
	private final JCheckBox remove = new JCheckBox();
	private final Map<Patch, PatchRow> rows = new EnumMap<>(Patch.class);

	/** Set while the section is being updated from the plan, so listeners ignore the changes. */
	private boolean updating;
	private List<TreeOption> shownOptions = new ArrayList<>();

	CategorySection(PatchKind kind, PlanManager planManager, UnlockTracker unlockTracker, PatchTracker patchTracker,
		ItemNames itemNames, Runnable onChange)
	{
		this.kind = kind;
		this.planManager = planManager;
		this.unlockTracker = unlockTracker;
		this.patchTracker = patchTracker;
		this.itemNames = itemNames;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setBorder(new EmptyBorder(0, 0, 6, 0));
		setAlignmentX(Component.LEFT_ALIGNMENT);

		final JPanel headerRow = new JPanel(new BorderLayout());
		headerRow.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		headerRow.setBorder(new EmptyBorder(5, 6, 5, 6));
		headerRow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		header.setFont(FontManager.getRunescapeBoldFont());
		header.setForeground(ColorScheme.BRAND_ORANGE);
		summary.setFont(FontManager.getRunescapeSmallFont());
		summary.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		headerRow.add(header, BorderLayout.WEST);
		headerRow.add(summary, BorderLayout.EAST);
		headerRow.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				planManager.setCollapsed(kind, !planManager.get(kind).isCollapsed());
				onChange.run();
			}
		});
		add(headerRow, BorderLayout.NORTH);

		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
		body.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		body.setBorder(new EmptyBorder(2, 6, 6, 6));
		add(body, BorderLayout.CENTER);

		treeBox.setFont(FontManager.getRunescapeSmallFont());
		treeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		treeBox.addActionListener(e ->
		{
			final TreeOption option = (TreeOption) treeBox.getSelectedItem();
			if (!updating && option != null)
			{
				planManager.setTree(kind, option.tree);
				onChange.run();
			}
		});
		body.add(treeBox);

		styleCheckBox(protect);
		protect.addActionListener(e ->
		{
			if (!updating)
			{
				planManager.setProtect(kind, protect.isSelected());
				onChange.run();
			}
		});
		body.add(protect);

		styleCheckBox(remove);
		remove.addActionListener(e ->
		{
			if (!updating)
			{
				planManager.setRemove(kind, remove.isSelected());
				onChange.run();
			}
		});
		body.add(remove);

		for (Patch patch : Patch.forKind(kind))
		{
			final PatchRow row = new PatchRow(patch, onChange);
			rows.put(patch, row);
			body.add(row);
		}
	}

	void update()
	{
		updating = true;
		try
		{
			final CategoryPlan plan = planManager.get(kind);
			final TreeType tree = plan.getTree();

			header.setText((plan.isCollapsed() ? "+ " : "- ") + kind.getDisplayName());
			summary.setText(tree == null ? "None" : tree.getDisplayName() + " x" + planManager.countInRun(kind));
			body.setVisible(!plan.isCollapsed());

			updateTreeOptions(tree);

			protect.setVisible(tree != null && tree.isProtectable());
			if (tree != null && tree.isProtectable())
			{
				protect.setSelected(plan.isProtect());
				protect.setText("<html>Pay to protect: " + tree.getProtectionPayment().stream()
					.map(itemNames::describe).collect(Collectors.joining(", ")) + "</html>");
			}

			final int removalCost = removalCost();
			remove.setVisible(tree != null && removalCost > 0);
			remove.setSelected(plan.isRemove());
			remove.setText("Pay for removal: " + String.format("%,d", removalCost) + " coins");

			final boolean atSpiritLimit = kind == PatchKind.SPIRIT
				&& planManager.countInRun(kind) >= planManager.getSpiritTreeLimit();
			rows.values().forEach(row -> row.update(tree != null, atSpiritLimit));
		}
		finally
		{
			updating = false;
		}
	}

	/** The coins each removal costs in this category; every patch of a kind charges the same. */
	private int removalCost()
	{
		for (Patch patch : Patch.forKind(kind))
		{
			if (patch.isRemovable())
			{
				return patch.getRemovalCost();
			}
		}
		return 0;
	}

	private void updateTreeOptions(TreeType selected)
	{
		final List<TreeOption> options = new ArrayList<>();
		options.add(new TreeOption(null));
		for (TreeType tree : TreeType.forKind(kind))
		{
			// Keep a saved choice visible even if a boost that allowed it has worn off
			if (unlockTracker.canPlant(tree) || tree == selected)
			{
				options.add(new TreeOption(tree));
			}
		}

		if (!options.equals(shownOptions) && !treeBox.isPopupVisible())
		{
			treeBox.setModel(new DefaultComboBoxModel<>(options.toArray(new TreeOption[0])));
			shownOptions = options;
		}
		treeBox.setSelectedItem(new TreeOption(selected));
	}

	private static void styleCheckBox(JCheckBox box)
	{
		box.setFont(FontManager.getRunescapeSmallFont());
		box.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		box.setForeground(Color.WHITE);
		box.setFocusPainted(false);
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
	}

	private static final class TreeOption
	{
		private final TreeType tree;

		private TreeOption(TreeType tree)
		{
			this.tree = tree;
		}

		@Override
		public String toString()
		{
			return tree == null ? "None" : tree.getDisplayName() + " (" + tree.getFarmingLevel() + ")";
		}

		@Override
		public boolean equals(Object o)
		{
			return o instanceof TreeOption && ((TreeOption) o).tree == tree;
		}

		@Override
		public int hashCode()
		{
			return Objects.hashCode(tree);
		}
	}

	/** A patch's checkbox, its current state, and why it is locked. */
	private final class PatchRow extends JPanel
	{
		private final Patch patch;
		private final JCheckBox box = new JCheckBox();
		private final JLabel status = new JLabel();
		private final JLabel reason = new JLabel();

		private PatchRow(Patch patch, Runnable onChange)
		{
			this.patch = patch;
			setLayout(new BorderLayout());
			setBackground(ColorScheme.DARKER_GRAY_COLOR);
			setAlignmentX(Component.LEFT_ALIGNMENT);

			styleCheckBox(box);
			box.setText(patch.getLocationName());
			box.addActionListener(e ->
			{
				if (!updating)
				{
					planManager.setIncluded(patch, box.isSelected());
					onChange.run();
				}
			});
			add(box, BorderLayout.CENTER);

			status.setFont(FontManager.getRunescapeSmallFont());
			add(status, BorderLayout.EAST);

			reason.setFont(FontManager.getRunescapeSmallFont());
			reason.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
			reason.setBorder(new EmptyBorder(0, 22, 2, 0));
			add(reason, BorderLayout.SOUTH);
		}

		private void update(boolean categoryActive, boolean atSpiritLimit)
		{
			final List<String> missing = unlockTracker.getMissingRequirements(patch);
			final boolean unlocked = missing.isEmpty();
			final boolean inRun = planManager.isInRun(patch);

			box.setSelected(inRun);
			box.setEnabled(categoryActive && unlocked && (inRun || !atSpiritLimit));
			box.setForeground(unlocked ? Color.WHITE : ColorScheme.MEDIUM_GRAY_COLOR);
			box.setToolTipText(unlocked && atSpiritLimit && !inRun ? spiritLimitText() : null);

			reason.setVisible(!unlocked);
			reason.setText("<html>Requires " + String.join(", ", missing) + "</html>");

			if (unlocked)
			{
				describe(patchTracker.getStatus(patch), status);
			}
			else
			{
				status.setText("Locked");
				status.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
				status.setToolTipText(null);
			}
		}
	}

	private String spiritLimitText()
	{
		final int outside = planManager.getSpiritTreesOutsideRun();
		return "Your Farming level allows " + planManager.getSpiritTreeLevelLimit() + " spirit tree(s) at once"
			+ (outside > 0 ? ", and " + outside + " already growing elsewhere." : ".");
	}

	static void describe(PatchStatus status, JLabel label)
	{
		if (!status.isSeen())
		{
			label.setText("Not seen");
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
				label.setText("~" + formatDuration(status.getReadyAt() - Instant.now().getEpochSecond()));
				label.setForeground(ColorScheme.PROGRESS_INPROGRESS_COLOR);
				label.setToolTipText(tree + "estimated from when you last saw this patch." + source);
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
		label.setText(treeState.getShortName());
		label.setToolTipText((tree + treeState.getDisplayName() + "." + source).trim());
	}

	private static String formatDuration(long seconds)
	{
		final long minutes = Math.max(0, seconds) / 60;
		final long hours = minutes / 60;
		return hours > 0 ? hours + "h " + (minutes % 60) + "m" : (minutes % 60) + "m";
	}
}
