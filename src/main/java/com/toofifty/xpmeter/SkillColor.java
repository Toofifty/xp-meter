package com.toofifty.xpmeter;

import java.awt.Color;
import net.runelite.api.Skill;

public class SkillColor
{
	public static Color get(Skill skill)
	{
		switch (skill)
		{
			case AGILITY:
				return new Color(68, 68, 201);
			case ATTACK:
				return new Color(145, 58, 42);
			case CONSTRUCTION:
				return new Color(169, 159, 138);
			case COOKING:
				return new Color(109, 53, 137);
			case CRAFTING:
				return new Color(115, 94, 71);
			case DEFENCE:
				return new Color(119, 133, 196);
			case FARMING:
				return new Color(57, 89, 45);
			case FIREMAKING:
				return new Color(198, 135, 44);
			case FISHING:
				return new Color(134, 172, 221);
			case FLETCHING:
				return new Color(65, 101, 105);
			case HERBLORE:
				return new Color(60, 139, 22);
			case HITPOINTS:
				return new Color(167, 57, 30);
			case HUNTER:
				return new Color(128, 124, 103);
			case MAGIC:
				return new Color(76, 77, 157);
			case MINING:
				return new Color(81, 80, 65);
			case PRAYER:
				return new Color(192, 185, 185);
			case RANGED:
				return new Color(93, 111, 35);
			case RUNECRAFT:
				return new Color(178, 178, 168);
			case SLAYER:
				return new Color(65, 54, 54);
			case SMITHING:
				return new Color(84, 84, 67);
			case STRENGTH:
				return new Color(57, 113, 78);
			case THIEVING:
				return new Color(109, 71, 99);
			case WOODCUTTING:
				return new Color(128, 110, 65);
		}

		return null;
	}
}
