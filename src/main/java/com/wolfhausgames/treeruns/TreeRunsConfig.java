package com.wolfhausgames.treeruns;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(TreeRunsPlugin.CONFIG_GROUP)
public interface TreeRunsConfig extends Config
{
	@ConfigSection(
		name = "Run guidance",
		description = "What to show while doing a run",
		position = 0
	)
	String guidanceSection = "guidance";

	@ConfigItem(
		keyName = "highlightPatch",
		name = "Highlight patch",
		description = "Outline the patch you're working on",
		section = guidanceSection,
		position = 1
	)
	default boolean highlightPatch()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightGardener",
		name = "Highlight gardener",
		description = "Outline the gardener when you need to pay them",
		section = guidanceSection,
		position = 2
	)
	default boolean highlightGardener()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightItems",
		name = "Highlight items",
		description = "Outline the inventory item to use for the current step, such as the sapling to plant",
		section = guidanceSection,
		position = 3
	)
	default boolean highlightItems()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hintArrow",
		name = "Hint arrow",
		description = "Point the game's hint arrow at the current patch",
		section = guidanceSection,
		position = 4
	)
	default boolean hintArrow()
	{
		return true;
	}

	@ConfigItem(
		keyName = "stepOverlay",
		name = "Step checklist",
		description = "Show the current patch's steps on screen",
		section = guidanceSection,
		position = 5
	)
	default boolean stepOverlay()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "highlightColor",
		name = "Highlight colour",
		description = "Colour of the patch, gardener and item outlines",
		section = guidanceSection,
		position = 6
	)
	default Color highlightColor()
	{
		return new Color(0, 255, 0, 200);
	}

	@ConfigItem(
		keyName = "shortestPath",
		name = "Shortest Path integration",
		description = "Ask the Shortest Path plugin (if installed) to show the way to the next patch",
		position = 10
	)
	default boolean shortestPath()
	{
		return false;
	}
}
