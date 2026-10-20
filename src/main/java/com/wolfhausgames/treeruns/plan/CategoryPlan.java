package com.wolfhausgames.treeruns.plan;

import com.wolfhausgames.treeruns.data.Patch;
import com.wolfhausgames.treeruns.data.TreeType;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * The player's choices for one {@link com.wolfhausgames.treeruns.data.PatchKind}. Serialized with Gson,
 * so fields hold names rather than enums to survive renames and removals.
 */
public class CategoryPlan
{
	/** {@link TreeType} name, or null for "None". */
	@Nullable
	private String tree;
	private boolean protect = true;
	private boolean remove = true;
	/** Patches the player unticked. Stored as exclusions so newly unlocked patches start ticked. */
	private Set<String> excludedPatches = new HashSet<>();
	private boolean collapsed;

	CategoryPlan()
	{
	}

	CategoryPlan(@Nullable TreeType tree)
	{
		this.tree = tree == null ? null : tree.name();
	}

	@Nullable
	public TreeType getTree()
	{
		if (tree == null)
		{
			return null;
		}
		try
		{
			return TreeType.valueOf(tree);
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	void setTree(@Nullable TreeType tree)
	{
		this.tree = tree == null ? null : tree.name();
	}

	public boolean isProtect()
	{
		return protect;
	}

	void setProtect(boolean protect)
	{
		this.protect = protect;
	}

	public boolean isRemove()
	{
		return remove;
	}

	void setRemove(boolean remove)
	{
		this.remove = remove;
	}

	public boolean isIncluded(Patch patch)
	{
		return excludedPatches == null || !excludedPatches.contains(patch.name());
	}

	void setIncluded(Patch patch, boolean included)
	{
		if (excludedPatches == null)
		{
			excludedPatches = new HashSet<>();
		}
		if (included)
		{
			excludedPatches.remove(patch.name());
		}
		else
		{
			excludedPatches.add(patch.name());
		}
	}

	public boolean isCollapsed()
	{
		return collapsed;
	}

	void setCollapsed(boolean collapsed)
	{
		this.collapsed = collapsed;
	}
}
