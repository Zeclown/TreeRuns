package com.wolfhausgames.treeruns.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A category of patch. The planner lets the player pick one tree per kind.
 */
@Getter
@RequiredArgsConstructor
public enum PatchKind
{
	TREE("Trees"),
	FRUIT_TREE("Fruit trees"),
	HARDWOOD("Hardwood trees"),
	CALQUAT("Calquat"),
	SPIRIT("Spirit trees"),
	CELASTRUS("Celastrus"),
	REDWOOD("Redwood"),
	CRYSTAL("Crystal tree");

	private final String displayName;
}
