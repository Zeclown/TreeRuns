package com.wolfhausgames.treeruns;

import com.wolfhausgames.treeruns.data.Patch;
import com.wolfhausgames.treeruns.run.RunSession;
import com.wolfhausgames.treeruns.run.RunStep;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

/**
 * The sidebar during a run: the current stop's steps and the list of stops.
 */
class RunPanel extends JPanel
{
	private final RunSession runSession;

	private final JLabel current = new JLabel();
	private final JPanel steps = new JPanel();
	private final JPanel stops = new JPanel();
	private final JButton back = button("Back");
	private final JButton next = button("Next");

	RunPanel(RunSession runSession, TreeRunsPlugin plugin)
	{
		this.runSession = runSession;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		current.setFont(FontManager.getRunescapeBoldFont());
		current.setForeground(ColorScheme.BRAND_ORANGE);
		current.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(current);

		steps.setLayout(new BoxLayout(steps, BoxLayout.Y_AXIS));
		steps.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		steps.setBorder(new EmptyBorder(6, 6, 6, 6));
		steps.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(steps);

		final JPanel buttons = new JPanel(new GridLayout(1, 3, 4, 0));
		buttons.setBackground(ColorScheme.DARK_GRAY_COLOR);
		buttons.setBorder(new EmptyBorder(6, 0, 8, 0));
		buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
		final JButton end = button("End run");
		back.addActionListener(e -> plugin.previousStop());
		next.addActionListener(e -> plugin.nextStop());
		end.addActionListener(e -> plugin.endRun());
		buttons.add(back);
		buttons.add(next);
		buttons.add(end);
		add(buttons);

		stops.setLayout(new BoxLayout(stops, BoxLayout.Y_AXIS));
		stops.setBackground(ColorScheme.DARK_GRAY_COLOR);
		stops.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(stops);
	}

	void update()
	{
		final Patch patch = runSession.getCurrent();
		final List<Patch> all = runSession.getStops();

		if (runSession.isComplete())
		{
			current.setText("Run complete!");
		}
		else if (patch != null)
		{
			current.setText((runSession.getIndex() + 1) + "/" + all.size() + ": " + patch.getLocationName());
		}

		steps.removeAll();
		final List<RunStep> runSteps = runSession.getSteps();
		for (int i = 0; i < runSteps.size(); i++)
		{
			final JLabel step = new JLabel("<html>" + (i == 0 ? "&gt; " : "") + runSteps.get(i).getText() + "</html>");
			step.setFont(FontManager.getRunescapeSmallFont());
			step.setForeground(i == 0 ? Color.WHITE : ColorScheme.LIGHT_GRAY_COLOR);
			steps.add(step);
		}
		if (runSteps.isEmpty())
		{
			final JLabel done = new JLabel(runSession.isComplete() ? "Every patch is done." : "Waiting for the patch...");
			done.setFont(FontManager.getRunescapeSmallFont());
			done.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			steps.add(done);
		}

		back.setEnabled(runSession.getIndex() > 0);
		next.setEnabled(patch != null);
		next.setText(patch != null && runSession.isDone(patch) ? "Next" : "Skip");

		stops.removeAll();
		for (int i = 0; i < all.size(); i++)
		{
			final Patch stop = all.get(i);
			final String mark;
			final Color color;
			if (stop == patch)
			{
				mark = "> ";
				color = Color.WHITE;
			}
			else if (runSession.isSkipped(stop))
			{
				mark = "Skipped: ";
				color = ColorScheme.PROGRESS_INPROGRESS_COLOR;
			}
			else if (runSession.isDone(stop))
			{
				mark = "Done: ";
				color = ColorScheme.PROGRESS_COMPLETE_COLOR;
			}
			else
			{
				mark = "";
				color = ColorScheme.LIGHT_GRAY_COLOR;
			}
			final JLabel label = new JLabel(mark + (i + 1) + ". " + stop.getLocationName());
			label.setFont(FontManager.getRunescapeSmallFont());
			label.setForeground(color);
			label.setBorder(new EmptyBorder(2, 0, 2, 0));
			stops.add(label);
		}

		revalidate();
		repaint();
	}

	private static JButton button(String text)
	{
		final JButton button = new JButton(text);
		button.setFont(FontManager.getRunescapeSmallFont());
		button.setFocusPainted(false);
		return button;
	}
}
