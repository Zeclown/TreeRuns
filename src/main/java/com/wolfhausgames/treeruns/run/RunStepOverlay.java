package com.wolfhausgames.treeruns.run;

import com.wolfhausgames.treeruns.TreeRunsConfig;
import com.wolfhausgames.treeruns.data.Patch;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/**
 * A small on-screen checklist of the current patch's steps.
 */
public class RunStepOverlay extends OverlayPanel
{
	private final RunSession runSession;
	private final TreeRunsConfig config;

	@Inject
	RunStepOverlay(RunSession runSession, TreeRunsConfig config)
	{
		this.runSession = runSession;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.stepOverlay() || !runSession.isActive())
		{
			return null;
		}

		if (runSession.isComplete())
		{
			panelComponent.getChildren().add(TitleComponent.builder()
				.text("Tree run complete")
				.color(Color.GREEN)
				.build());
			return super.render(graphics);
		}

		final Patch patch = runSession.getCurrent();
		if (patch == null)
		{
			return null;
		}

		panelComponent.getChildren().add(TitleComponent.builder()
			.text(patch.getLocationName() + " (" + (runSession.getIndex() + 1) + "/" + runSession.getStops().size() + ")")
			.color(Color.ORANGE)
			.build());

		final List<RunStep> steps = runSession.getSteps();
		for (int i = 0; i < steps.size(); i++)
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left((i == 0 ? "> " : "  ") + steps.get(i).getText())
				.leftColor(i == 0 ? Color.WHITE : Color.GRAY)
				.build());
		}
		return super.render(graphics);
	}
}
