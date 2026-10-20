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
	EMPTY("Empty", "Empty"),
	GROWING("Growing", "Growing"),
	/** Fully grown and waiting for "Check-health". */
	READY("Ready to check health", "Ready"),
	/** Health has been checked; the tree can be chopped, picked or removed. */
	CHECKED("Health checked", "Checked"),
	STUMP("Stump", "Stump"),
	DISEASED("Diseased", "Diseased"),
	DEAD("Dead", "Dead"),
	UNKNOWN("Unknown", "Unknown");

	private final String displayName;
	/** For the narrow patch rows in the sidebar. */
	private final String shortName;
}
