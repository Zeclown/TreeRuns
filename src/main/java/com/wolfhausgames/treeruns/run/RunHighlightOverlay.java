package com.wolfhausgames.treeruns.run;

import com.wolfhausgames.treeruns.TreeRunsConfig;
import com.wolfhausgames.treeruns.data.Patch;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

/**
 * Outlines whatever the current step involves: the patch or its gardener.
 */
public class RunHighlightOverlay extends Overlay
{
	private static final int OUTLINE_WIDTH = 2;
	private static final int OUTLINE_FEATHER = 2;

	private final RunSession runSession;
	private final SceneTracker sceneTracker;
	private final TreeRunsConfig config;
	private final ModelOutlineRenderer outlineRenderer;

	@Inject
	RunHighlightOverlay(RunSession runSession, SceneTracker sceneTracker, TreeRunsConfig config,
		ModelOutlineRenderer outlineRenderer)
	{
		this.runSession = runSession;
		this.sceneTracker = sceneTracker;
		this.config = config;
		this.outlineRenderer = outlineRenderer;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final Patch patch = runSession.getCurrent();
		if (patch == null)
		{
			return null;
		}

		final RunStep step = runSession.getCurrentStep();
		if (step == null)
		{
			return null;
		}

		if (step.getTarget() == RunStep.Target.PATCH && config.highlightPatch())
		{
			final GameObject object = sceneTracker.getPatchObject(patch);
			if (object != null)
			{
				outlineRenderer.drawOutline(object, OUTLINE_WIDTH, config.highlightColor(), OUTLINE_FEATHER);
			}
		}
		else if (step.getTarget() == RunStep.Target.GARDENER && config.highlightGardener())
		{
			for (NPC gardener : sceneTracker.getGardeners(patch))
			{
				outlineRenderer.drawOutline(gardener, OUTLINE_WIDTH, config.highlightColor(), OUTLINE_FEATHER);
			}
		}
		return null;
	}
}
