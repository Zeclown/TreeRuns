/*
 * Varbit value ranges derived from RuneLite's timetracking PatchImplementation,
 * Copyright (c) 2019 Abex. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.wolfhausgames.treeruns.data;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * Decodes a patch's farming varbit value into a {@link PatchState}.
 */
public final class PatchStateDecoder
{
	private static final int VALUES = 256;
	private static final Map<PatchKind, PatchState[]> TABLES = new EnumMap<>(PatchKind.class);

	static
	{
		for (PatchKind kind : PatchKind.values())
		{
			final PatchState[] table = new PatchState[VALUES];
			Arrays.fill(table, PatchState.UNKNOWN);
			TABLES.put(kind, table);
		}

		// FRUIT_TREE
		add(PatchKind.FRUIT_TREE, 8, 13, TreeType.APPLE, TreeState.GROWING, 8); // Apple tree[Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 14, 20, TreeType.APPLE, TreeState.CHECKED); // Apple tree[Chop-down,Inspect,Guide,Pick-apple]
		add(PatchKind.FRUIT_TREE, 21, 26, TreeType.APPLE, TreeState.DISEASED); // Diseased apple tree[Prune,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 27, 32, TreeType.APPLE, TreeState.DEAD); // Dead apple tree[Clear,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 33, 33, TreeType.APPLE, TreeState.STUMP); // Apple tree stump[Clear,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 34, 34, TreeType.APPLE, TreeState.READY); // Apple tree[Check-health,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 35, 40, TreeType.BANANA, TreeState.GROWING, 35); // Banana tree[Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 41, 47, TreeType.BANANA, TreeState.CHECKED); // Banana tree[Chop-down,Inspect,Guide,Pick-banana]
		add(PatchKind.FRUIT_TREE, 48, 53, TreeType.BANANA, TreeState.DISEASED); // Diseased banana tree[Prune,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 54, 59, TreeType.BANANA, TreeState.DEAD); // Dead banana tree[Clear,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 60, 60, TreeType.BANANA, TreeState.STUMP); // Banana tree stump[Clear,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 61, 61, TreeType.BANANA, TreeState.READY); // Banana tree[Check-health,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 72, 77, TreeType.ORANGE, TreeState.GROWING, 72); // Orange tree[Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 78, 84, TreeType.ORANGE, TreeState.CHECKED); // Orange tree[Chop-down,Inspect,Guide,Pick-orange]
		add(PatchKind.FRUIT_TREE, 85, 89, TreeType.ORANGE, TreeState.DISEASED); // Diseased orange tree[Prune,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 90, 90, TreeType.ORANGE, TreeState.DISEASED); // Diseased orange tree[Chop-down,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 91, 96, TreeType.ORANGE, TreeState.DEAD); // Dead orange tree[Clear,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 97, 97, TreeType.ORANGE, TreeState.STUMP); // Orange tree stump[Clear,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 98, 98, TreeType.ORANGE, TreeState.READY); // Orange tree[Check-health,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 99, 104, TreeType.CURRY, TreeState.GROWING, 99); // Curry tree[Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 105, 111, TreeType.CURRY, TreeState.CHECKED); // Curry tree[Chop-down,Inspect,Guide,Pick-leaf]
		add(PatchKind.FRUIT_TREE, 112, 117, TreeType.CURRY, TreeState.DISEASED); // Diseased curry tree[Prune,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 118, 123, TreeType.CURRY, TreeState.DEAD); // Dead curry tree[Clear,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 124, 124, TreeType.CURRY, TreeState.STUMP); // Curry tree stump[Clear,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 125, 125, TreeType.CURRY, TreeState.READY); // Curry tree[Check-health,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 136, 141, TreeType.PINEAPPLE, TreeState.GROWING, 136); // Pineapple plant[Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 142, 148, TreeType.PINEAPPLE, TreeState.CHECKED); // Pineapple plant[Chop down,Inspect,Guide,Pick-pineapple]
		add(PatchKind.FRUIT_TREE, 149, 154, TreeType.PINEAPPLE, TreeState.DISEASED); // Diseased pineapple plant[Prune,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 155, 160, TreeType.PINEAPPLE, TreeState.DEAD); // Dead pineapple plant[Clear,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 161, 161, TreeType.PINEAPPLE, TreeState.STUMP); // Pineapple plant stump[Clear,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 162, 162, TreeType.PINEAPPLE, TreeState.READY); // Pineapple plant[Check-health,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 163, 168, TreeType.PAPAYA, TreeState.GROWING, 163); // Papaya tree[Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 169, 175, TreeType.PAPAYA, TreeState.CHECKED); // Papaya tree[Chop-down,Inspect,Guide,Pick-fruit]
		add(PatchKind.FRUIT_TREE, 176, 181, TreeType.PAPAYA, TreeState.DISEASED); // Diseased papaya tree[Prune,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 182, 187, TreeType.PAPAYA, TreeState.DEAD); // Dead papaya tree[Clear,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 188, 188, TreeType.PAPAYA, TreeState.STUMP); // Papaya tree stump[Clear,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 189, 189, TreeType.PAPAYA, TreeState.READY); // Papaya tree[Check-health,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 200, 205, TreeType.PALM, TreeState.GROWING, 200); // Palm tree[Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 206, 212, TreeType.PALM, TreeState.CHECKED); // Palm tree[Chop-down,Inspect,Guide,Pick-coconut]
		add(PatchKind.FRUIT_TREE, 213, 218, TreeType.PALM, TreeState.DISEASED); // Diseased palm tree[Prune,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 219, 224, TreeType.PALM, TreeState.DEAD); // Dead palm tree[Clear,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 225, 225, TreeType.PALM, TreeState.STUMP); // Palm tree stump[Clear,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 226, 226, TreeType.PALM, TreeState.READY); // Palm tree[Check-health,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 227, 232, TreeType.DRAGONFRUIT, TreeState.GROWING, 227); // Dragonfruit tree[Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 233, 239, TreeType.DRAGONFRUIT, TreeState.CHECKED); // Dragonfruit tree[Chop down,Inspect,Guide,Pick-dragonfruit]
		add(PatchKind.FRUIT_TREE, 240, 245, TreeType.DRAGONFRUIT, TreeState.DISEASED); // Diseased dragonfruit plant[Prune,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 246, 251, TreeType.DRAGONFRUIT, TreeState.DEAD); // Dead dragonfruit plant[Clear,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 252, 252, TreeType.DRAGONFRUIT, TreeState.STUMP); // Dragonfruit tree stump[Clear,Inspect,Guide]
		add(PatchKind.FRUIT_TREE, 253, 253, TreeType.DRAGONFRUIT, TreeState.READY); // Dragonfruit tree[Check-health,Inspect,Guide]
		addEmpty(PatchKind.FRUIT_TREE, 0, 7);
		addEmpty(PatchKind.FRUIT_TREE, 62, 71);
		addEmpty(PatchKind.FRUIT_TREE, 126, 135);
		addEmpty(PatchKind.FRUIT_TREE, 190, 199);
		addEmpty(PatchKind.FRUIT_TREE, 254, 255);
		// TREE
		add(PatchKind.TREE, 8, 11, TreeType.OAK, TreeState.GROWING, 8); // Oak[Inspect,Guide]
		add(PatchKind.TREE, 12, 12, TreeType.OAK, TreeState.READY); // Oak[Check-health,Inspect,Guide]
		add(PatchKind.TREE, 13, 13, TreeType.OAK, TreeState.CHECKED); // Oak[Chop down,Inspect,Guide]
		add(PatchKind.TREE, 14, 14, TreeType.OAK, TreeState.STUMP); // Oak tree stump[Clear,Inspect,Guide]
		add(PatchKind.TREE, 15, 20, TreeType.WILLOW, TreeState.GROWING, 15); // Willow Tree[Inspect,Guide]
		add(PatchKind.TREE, 21, 21, TreeType.WILLOW, TreeState.READY); // Willow Tree[Check-health,Inspect,Guide]
		add(PatchKind.TREE, 22, 22, TreeType.WILLOW, TreeState.CHECKED); // Willow Tree[Chop down,Inspect,Guide]
		add(PatchKind.TREE, 23, 23, TreeType.WILLOW, TreeState.STUMP); // Willow tree stump[Clear,Inspect,Guide]
		add(PatchKind.TREE, 24, 31, TreeType.MAPLE, TreeState.GROWING, 24); // Maple Tree[Inspect,Guide]
		add(PatchKind.TREE, 32, 32, TreeType.MAPLE, TreeState.READY); // Maple Tree[Check-health,Inspect,Guide]
		add(PatchKind.TREE, 33, 33, TreeType.MAPLE, TreeState.CHECKED); // Maple Tree[Chop down,Inspect,Guide]
		add(PatchKind.TREE, 34, 34, TreeType.MAPLE, TreeState.STUMP); // Tree stump[Clear,Inspect,Guide]
		add(PatchKind.TREE, 35, 44, TreeType.YEW, TreeState.GROWING, 35); // Yew sapling,Yew tree[Inspect,Guide]
		add(PatchKind.TREE, 45, 45, TreeType.YEW, TreeState.READY); // Yew tree[Check-health,Inspect,Guide]
		add(PatchKind.TREE, 46, 46, TreeType.YEW, TreeState.CHECKED); // Yew tree[Chop down,Inspect,Guide]
		add(PatchKind.TREE, 47, 47, TreeType.YEW, TreeState.STUMP); // Yew tree stump[Clear,Inspect,Guide]
		add(PatchKind.TREE, 48, 59, TreeType.MAGIC, TreeState.GROWING, 48); // Magic Tree[Inspect,Guide]
		add(PatchKind.TREE, 60, 60, TreeType.MAGIC, TreeState.READY); // Magic Tree[Check-health,Inspect,Guide]
		add(PatchKind.TREE, 61, 61, TreeType.MAGIC, TreeState.CHECKED); // Magic Tree[Chop down,Inspect,Guide]
		add(PatchKind.TREE, 62, 62, TreeType.MAGIC, TreeState.STUMP); // Magic Tree Stump[Clear,Inspect,Guide]
		add(PatchKind.TREE, 73, 75, TreeType.OAK, TreeState.DISEASED); // Diseased Oak[Prune,Inspect,Guide]
		add(PatchKind.TREE, 77, 77, TreeType.OAK, TreeState.DISEASED); // Diseased Oak[Prune,Inspect,Guide]
		add(PatchKind.TREE, 80, 84, TreeType.WILLOW, TreeState.DISEASED); // Diseased Willow[Prune,Inspect,Guide]
		add(PatchKind.TREE, 86, 86, TreeType.WILLOW, TreeState.DISEASED); // Diseased Willow[Prune,Inspect,Guide]
		add(PatchKind.TREE, 89, 95, TreeType.MAPLE, TreeState.DISEASED); // Diseased Maple[Prune,Inspect,Guide]
		add(PatchKind.TREE, 97, 97, TreeType.MAPLE, TreeState.DISEASED); // Diseased Maple[Prune,Inspect,Guide]
		add(PatchKind.TREE, 100, 108, TreeType.YEW, TreeState.DISEASED); // Diseased Yew[Prune,Inspect,Guide]
		add(PatchKind.TREE, 110, 110, TreeType.YEW, TreeState.DISEASED); // Diseased Yew[Prune,Inspect,Guide]
		add(PatchKind.TREE, 113, 123, TreeType.MAGIC, TreeState.DISEASED); // Diseased Magic Tree[Prune,Inspect,Guide]
		add(PatchKind.TREE, 125, 125, TreeType.MAGIC, TreeState.DISEASED); // Diseased Magic Tree[Prune,Inspect,Guide]
		add(PatchKind.TREE, 137, 139, TreeType.OAK, TreeState.DEAD); // Dead Oak[Clear,Inspect,Guide]
		add(PatchKind.TREE, 141, 141, TreeType.OAK, TreeState.DEAD); // Dead Oak[Clear,Inspect,Guide]
		add(PatchKind.TREE, 144, 148, TreeType.WILLOW, TreeState.DEAD); // Dead Willow[Clear,Inspect,Guide]
		add(PatchKind.TREE, 150, 150, TreeType.WILLOW, TreeState.DEAD); // Dead Willow[Clear,Inspect,Guide]
		add(PatchKind.TREE, 153, 159, TreeType.MAPLE, TreeState.DEAD); // Dead Maple[Clear,Inspect,Guide]
		add(PatchKind.TREE, 161, 161, TreeType.MAPLE, TreeState.DEAD); // Dead Maple[Clear,Inspect,Guide]
		add(PatchKind.TREE, 164, 172, TreeType.YEW, TreeState.DEAD); // Dead Yew[Clear,Inspect,Guide]
		add(PatchKind.TREE, 174, 174, TreeType.YEW, TreeState.DEAD); // Dead Yew[Clear,Inspect,Guide]
		add(PatchKind.TREE, 177, 187, TreeType.MAGIC, TreeState.DEAD); // Dead Magic Tree[Clear,Inspect,Guide]
		add(PatchKind.TREE, 189, 189, TreeType.MAGIC, TreeState.DEAD); // Dead Magic Tree[Clear,Inspect,Guide]
		add(PatchKind.TREE, 192, 197, TreeType.WILLOW, TreeState.CHECKED); // Willow Tree[Chop down,Inspect,Guide]
		addEmpty(PatchKind.TREE, 0, 7);
		addEmpty(PatchKind.TREE, 63, 72);
		addEmpty(PatchKind.TREE, 78, 79);
		addEmpty(PatchKind.TREE, 87, 88);
		addEmpty(PatchKind.TREE, 98, 99);
		addEmpty(PatchKind.TREE, 111, 112);
		addEmpty(PatchKind.TREE, 126, 136);
		addEmpty(PatchKind.TREE, 142, 143);
		addEmpty(PatchKind.TREE, 151, 152);
		addEmpty(PatchKind.TREE, 162, 163);
		addEmpty(PatchKind.TREE, 175, 176);
		addEmpty(PatchKind.TREE, 190, 191);
		addEmpty(PatchKind.TREE, 198, 255);
		// HARDWOOD
		add(PatchKind.HARDWOOD, 8, 14, TreeType.TEAK, TreeState.GROWING, 8); // Teak Tree[Inspect,Guide]
		add(PatchKind.HARDWOOD, 15, 15, TreeType.TEAK, TreeState.READY); // Teak Tree[Check-health,Inspect,Guide]
		add(PatchKind.HARDWOOD, 16, 16, TreeType.TEAK, TreeState.CHECKED); // Teak Tree[Chop down,Inspect,Guide]
		add(PatchKind.HARDWOOD, 17, 17, TreeType.TEAK, TreeState.STUMP); // Tree stump[Clear,Inspect,Guide]
		add(PatchKind.HARDWOOD, 18, 23, TreeType.TEAK, TreeState.DISEASED); // Diseased Teak[Prune,Inspect,Guide]
		add(PatchKind.HARDWOOD, 24, 29, TreeType.TEAK, TreeState.DEAD); // Dead Teak[Clear,Inspect,Guide]
		add(PatchKind.HARDWOOD, 30, 37, TreeType.MAHOGANY, TreeState.GROWING, 30); // Mahogany sapling,Mahogany tree[Inspect,Guide]
		add(PatchKind.HARDWOOD, 38, 38, TreeType.MAHOGANY, TreeState.READY); // Mahogany tree[Check-health,Inspect,Guide]
		add(PatchKind.HARDWOOD, 39, 39, TreeType.MAHOGANY, TreeState.CHECKED); // Mahogany tree[Chop down,Inspect,Guide]
		add(PatchKind.HARDWOOD, 40, 40, TreeType.MAHOGANY, TreeState.STUMP); // Mahogany tree stump[Clear,Inspect,Guide]
		add(PatchKind.HARDWOOD, 41, 47, TreeType.MAHOGANY, TreeState.DISEASED); // Diseased Mahogany[Prune,Inspect,Guide]
		add(PatchKind.HARDWOOD, 48, 54, TreeType.MAHOGANY, TreeState.DEAD); // Dead Mahogany[Clear,Inspect,Guide]
		add(PatchKind.HARDWOOD, 55, 62, TreeType.CAMPHOR, TreeState.GROWING, 55); // Camphor sapling,Camphor tree[Inspect,Guide]
		add(PatchKind.HARDWOOD, 63, 63, TreeType.CAMPHOR, TreeState.READY); // Camphor tree[Check-health,Inspect,Guide]
		add(PatchKind.HARDWOOD, 64, 64, TreeType.CAMPHOR, TreeState.CHECKED); // Camphor tree[Chop down,Inspect,Guide]
		add(PatchKind.HARDWOOD, 65, 65, TreeType.CAMPHOR, TreeState.STUMP); // Camphor tree stump[Clear,Inspect,Guide]
		add(PatchKind.HARDWOOD, 66, 72, TreeType.CAMPHOR, TreeState.DISEASED); // Diseased Camphor[Prune,Inspect,Guide]
		add(PatchKind.HARDWOOD, 73, 79, TreeType.CAMPHOR, TreeState.DEAD); // Dead Camphor[Clear,Inspect,Guide]
		add(PatchKind.HARDWOOD, 80, 87, TreeType.IRONWOOD, TreeState.GROWING, 80); // Ironwood sapling,Ironwood tree[Inspect,Guide]
		add(PatchKind.HARDWOOD, 88, 88, TreeType.IRONWOOD, TreeState.READY); // Ironwood tree[Check-health,Inspect,Guide]
		add(PatchKind.HARDWOOD, 89, 89, TreeType.IRONWOOD, TreeState.CHECKED); // Ironwood tree[Chop down,Inspect,Guide]
		add(PatchKind.HARDWOOD, 90, 90, TreeType.IRONWOOD, TreeState.STUMP); // Ironwood tree stump[Clear,Inspect,Guide]
		add(PatchKind.HARDWOOD, 91, 97, TreeType.IRONWOOD, TreeState.DISEASED); // Diseased Ironwood[Prune,Inspect,Guide]
		add(PatchKind.HARDWOOD, 98, 104, TreeType.IRONWOOD, TreeState.DEAD); // Dead Ironwood[Clear,Inspect,Guide]
		add(PatchKind.HARDWOOD, 105, 113, TreeType.ROSEWOOD, TreeState.GROWING, 105); // Rosewood sapling,Rosewood tree[Inspect,Guide]
		add(PatchKind.HARDWOOD, 114, 114, TreeType.ROSEWOOD, TreeState.READY); // Rosewood tree[Check-health,Inspect,Guide]
		add(PatchKind.HARDWOOD, 115, 115, TreeType.ROSEWOOD, TreeState.CHECKED); // Rosewood tree[Chop down,Inspect,Guide]
		add(PatchKind.HARDWOOD, 116, 116, TreeType.ROSEWOOD, TreeState.STUMP); // Rosewood tree stump[Clear,Inspect,Guide]
		add(PatchKind.HARDWOOD, 117, 124, TreeType.ROSEWOOD, TreeState.DISEASED); // Diseased Rosewood[Prune,Inspect,Guide]
		add(PatchKind.HARDWOOD, 125, 132, TreeType.ROSEWOOD, TreeState.DEAD); // Dead Rosewood[Clear,Inspect,Guide]
		addEmpty(PatchKind.HARDWOOD, 0, 7);
		addEmpty(PatchKind.HARDWOOD, 133, 255);
		// REDWOOD
		add(PatchKind.REDWOOD, 8, 17, TreeType.REDWOOD, TreeState.GROWING, 8); // Redwood tree[Inspect,Guide]
		add(PatchKind.REDWOOD, 18, 18, TreeType.REDWOOD, TreeState.CHECKED); // Redwood tree[Clear,Inspect,Guide]
		add(PatchKind.REDWOOD, 19, 27, TreeType.REDWOOD, TreeState.DISEASED); // Diseased Redwood tree[Prune,Inspect,Guide]
		add(PatchKind.REDWOOD, 28, 36, TreeType.REDWOOD, TreeState.DEAD); // Dead Redwood tree[Clear,Inspect,Guide]
		add(PatchKind.REDWOOD, 37, 37, TreeType.REDWOOD, TreeState.READY); // Redwood tree[Check-health,Inspect,Guide]
		add(PatchKind.REDWOOD, 41, 55, TreeType.REDWOOD, TreeState.CHECKED); // Redwood tree[Clear,Inspect,Guide]
		addEmpty(PatchKind.REDWOOD, 0, 7);
		// SPIRIT
		add(PatchKind.SPIRIT, 8, 19, TreeType.SPIRIT, TreeState.GROWING, 8); // Spirit Tree[Inspect,Guide]
		add(PatchKind.SPIRIT, 20, 20, TreeType.SPIRIT, TreeState.CHECKED); // Spirit Tree[Travel,Talk-to,Inspect,Guide,Clear]
		add(PatchKind.SPIRIT, 21, 31, TreeType.SPIRIT, TreeState.DISEASED); // Diseased Spirit Tree[Prune,Inspect,Guide]
		add(PatchKind.SPIRIT, 32, 43, TreeType.SPIRIT, TreeState.DEAD); // Dead Spirit Tree[Clear,Inspect,Guide]
		add(PatchKind.SPIRIT, 44, 44, TreeType.SPIRIT, TreeState.READY); // Spirit Tree[Check-health,Inspect,Guide]
		addEmpty(PatchKind.SPIRIT, 0, 7);
		addEmpty(PatchKind.SPIRIT, 45, 63);
		// CALQUAT
		add(PatchKind.CALQUAT, 4, 11, TreeType.CALQUAT, TreeState.GROWING, 4); // Calquat Tree[Inspect,Guide]
		add(PatchKind.CALQUAT, 12, 18, TreeType.CALQUAT, TreeState.CHECKED); // Calquat Tree[Clear,Inspect,Guide,Pick-fruit]
		add(PatchKind.CALQUAT, 19, 25, TreeType.CALQUAT, TreeState.DISEASED); // Diseased Calquat[Prune,Inspect,Guide]
		add(PatchKind.CALQUAT, 26, 33, TreeType.CALQUAT, TreeState.DEAD); // Dead Calquat[Clear,Inspect,Guide]
		add(PatchKind.CALQUAT, 34, 34, TreeType.CALQUAT, TreeState.READY); // Calquat Tree[Check-health,Inspect,Guide]
		addEmpty(PatchKind.CALQUAT, 0, 3);
		addEmpty(PatchKind.CALQUAT, 35, 255);
		// CELASTRUS
		add(PatchKind.CELASTRUS, 8, 12, TreeType.CELASTRUS, TreeState.GROWING, 8); // Celastrus tree[Inspect,Guide]
		add(PatchKind.CELASTRUS, 13, 13, TreeType.CELASTRUS, TreeState.READY); // Celastrus tree[Check-health,Inspect,Guide]
		add(PatchKind.CELASTRUS, 14, 16, TreeType.CELASTRUS, TreeState.CHECKED); // Celastrus tree[Harvest,Inspect,Guide]
		add(PatchKind.CELASTRUS, 17, 17, TreeType.CELASTRUS, TreeState.CHECKED); // Harvested Celastrus tree[Chop,Inspect,Guide]
		add(PatchKind.CELASTRUS, 18, 22, TreeType.CELASTRUS, TreeState.DISEASED); // Diseased celastrus tree[Prune,Inspect,Guide]
		add(PatchKind.CELASTRUS, 23, 27, TreeType.CELASTRUS, TreeState.DEAD); // Dead celastrus tree[Clear,Inspect,Guide]
		add(PatchKind.CELASTRUS, 28, 28, TreeType.CELASTRUS, TreeState.STUMP); // Celastrus tree stump[Clear,Inspect,Guide]
		addEmpty(PatchKind.CELASTRUS, 0, 7);
		addEmpty(PatchKind.CELASTRUS, 29, 255);
		// CRYSTAL
		add(PatchKind.CRYSTAL, 8, 13, TreeType.CRYSTAL, TreeState.GROWING, 8); // Crystal tree[Inspect,Guide]
		add(PatchKind.CRYSTAL, 14, 14, TreeType.CRYSTAL, TreeState.READY); // Crystal tree[Check-health,Inspect,Guide]
		add(PatchKind.CRYSTAL, 15, 15, TreeType.CRYSTAL, TreeState.CHECKED); // Crystal tree[Chop-down,Inspect,Guide]
		addEmpty(PatchKind.CRYSTAL, 0, 3);
	}

	private PatchStateDecoder()
	{
	}

	public static PatchState decode(PatchKind kind, int varbitValue)
	{
		if (varbitValue < 0 || varbitValue >= VALUES)
		{
			return PatchState.UNKNOWN;
		}
		return TABLES.get(kind)[varbitValue];
	}

	private static void add(PatchKind kind, int from, int to, TreeType tree, TreeState state)
	{
		final PatchState patchState = new PatchState(tree, state, 0);
		Arrays.fill(TABLES.get(kind), from, to + 1, patchState);
	}

	private static void add(PatchKind kind, int from, int to, TreeType tree, TreeState state, int firstStageValue)
	{
		final PatchState[] table = TABLES.get(kind);
		for (int value = from; value <= to; value++)
		{
			table[value] = new PatchState(tree, state, value - firstStageValue);
		}
	}

	private static void addEmpty(PatchKind kind, int from, int to)
	{
		Arrays.fill(TABLES.get(kind), from, to + 1, PatchState.EMPTY);
	}
}
