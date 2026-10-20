package com.wolfhausgames.treeruns;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TreeRunsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TreeRunsPlugin.class);
		RuneLite.main(args);
	}
}
