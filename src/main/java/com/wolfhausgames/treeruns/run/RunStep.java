package com.wolfhausgames.treeruns.run;

import java.util.Set;
import lombok.Value;

/**
 * One thing to do at a patch, what in the scene it involves, and which items it uses.
 */
@Value
public class RunStep
{
	public enum Target
	{
		NONE,
		PATCH,
		GARDENER
	}

	String text;
	Target target;
	/** Items to use for this step, any of which will do. Empty if none. */
	Set<Integer> itemIds;

	public RunStep(String text, Target target)
	{
		this(text, target, Set.of());
	}

	public RunStep(String text, Target target, Set<Integer> itemIds)
	{
		this.text = text;
		this.target = target;
		this.itemIds = itemIds;
	}
}
