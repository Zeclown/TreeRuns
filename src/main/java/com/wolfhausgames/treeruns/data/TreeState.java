package com.wolfhausgames.treeruns.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * What a tree patch currently looks like, from the point of view of a tree run.
 */
@Getter
@RequiredArgsConstructor
public enum TreeState
{
	EMPTY("Empty"),
	GROWING("Growing"),
	/** Fully grown and waiting for "Check-health". */
	READY("Ready to check"),
	/** Health has been checked; the tree can be chopped, picked or removed. */
	CHECKED("Health checked"),
	STUMP("Stump"),
	DISEASED("Diseased"),
	DEAD("Dead"),
	UNKNOWN("Unknown");

	private final String displayName;

	public boolean isFullyGrown()
	{
		return this == READY || this == CHECKED || this == STUMP;
	}
}
