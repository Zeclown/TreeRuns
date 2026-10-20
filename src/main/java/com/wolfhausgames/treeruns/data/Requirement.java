package com.wolfhausgames.treeruns.data;

import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;

/**
 * Something the player must have done to use a patch or plant a tree.
 * {@link #isMet(Client)} reads game state, so it must be called on the client thread.
 */
public interface Requirement
{
	boolean isMet(Client client);

	/** Short text shown to the player, e.g. "65 Farming". */
	String getDescription();

	/**
	 * A skill level. Boostable requirements accept a temporary boost, as the game does for planting.
	 */
	static Requirement skill(Skill skill, int level, boolean boostable)
	{
		return new Requirement()
		{
			@Override
			public boolean isMet(Client client)
			{
				final int current = boostable
					? Math.max(client.getRealSkillLevel(skill), client.getBoostedSkillLevel(skill))
					: client.getRealSkillLevel(skill);
				return current >= level;
			}

			@Override
			public String getDescription()
			{
				return level + " " + skill.getName() + (boostable ? "" : " (unboostable)");
			}
		};
	}

	static Requirement questCompleted(Quest quest)
	{
		return new Requirement()
		{
			@Override
			public boolean isMet(Client client)
			{
				return quest.getState(client) == QuestState.FINISHED;
			}

			@Override
			public String getDescription()
			{
				return quest.getName();
			}
		};
	}

	static Requirement questStarted(Quest quest)
	{
		return new Requirement()
		{
			@Override
			public boolean isMet(Client client)
			{
				return quest.getState(client) != QuestState.NOT_STARTED;
			}

			@Override
			public String getDescription()
			{
				return quest.getName() + " (started)";
			}
		};
	}

	static Requirement anyOf(Requirement... options)
	{
		return new Requirement()
		{
			@Override
			public boolean isMet(Client client)
			{
				for (Requirement option : options)
				{
					if (option.isMet(client))
					{
						return true;
					}
				}
				return false;
			}

			@Override
			public String getDescription()
			{
				final StringBuilder sb = new StringBuilder();
				for (Requirement option : options)
				{
					if (sb.length() > 0)
					{
						sb.append(" or ");
					}
					sb.append(option.getDescription());
				}
				return sb.toString();
			}
		};
	}

	static Requirement allOf(Requirement... parts)
	{
		return new Requirement()
		{
			@Override
			public boolean isMet(Client client)
			{
				for (Requirement part : parts)
				{
					if (!part.isMet(client))
					{
						return false;
					}
				}
				return true;
			}

			@Override
			public String getDescription()
			{
				final StringBuilder sb = new StringBuilder();
				for (Requirement part : parts)
				{
					if (sb.length() > 0)
					{
						sb.append(" + ");
					}
					sb.append(part.getDescription());
				}
				return sb.toString();
			}
		};
	}
}
