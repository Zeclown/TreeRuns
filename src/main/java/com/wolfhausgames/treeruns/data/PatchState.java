package com.wolfhausgames.treeruns.data;

import javax.annotation.Nullable;
import lombok.Value;

@Value
public class PatchState
{
	public static final PatchState UNKNOWN = new PatchState(null, TreeState.UNKNOWN, 0);
	public static final PatchState EMPTY = new PatchState(null, TreeState.EMPTY, 0);

	/** The tree in the patch, or null when the patch is empty or unknown. */
	@Nullable
	TreeType tree;
	TreeState state;
	/** Growth stage, starting at 0 when planted. Only meaningful while {@link TreeState#GROWING}. */
	int stage;
}
