package com.toofifty.xpmeter;

import java.awt.Color;
import lombok.AllArgsConstructor;
import net.runelite.api.Skill;

@AllArgsConstructor
public enum SkillColor
{
	// must match order of Skill enum

	ATTACK(new Color(145, 58, 42)),
	DEFENCE(new Color(119, 133, 196)),
	STRENGTH(new Color(57, 113, 78)),
	HITPOINTS(new Color(167, 57, 30)),
	RANGED(new Color(93, 111, 35)),
	PRAYER(new Color(192, 185, 185)),
	MAGIC(new Color(76, 77, 157)),
	COOKING(new Color(109, 53, 137)),
	WOODCUTTING(new Color(128, 110, 65)),
	FLETCHING(new Color(65, 101, 105)),
	FISHING(new Color(134, 172, 221)),
	FIREMAKING(new Color(198, 135, 44)),
	CRAFTING(new Color(115, 94, 71)),
	SMITHING(new Color(84, 84, 67)),
	MINING(new Color(81, 80, 65)),
	HERBLORE(new Color(60, 139, 22)),
	AGILITY(new Color(68, 68, 201)),
	THIEVING(new Color(109, 71, 99)),
	SLAYER(new Color(104, 95, 95)),
	FARMING(new Color(57, 89, 45)),
	RUNECRAFT(new Color(178, 178, 168)),
	HUNTER(new Color(128, 124, 103)),
	CONSTRUCTION(new Color(169, 159, 138)),
	SAILING(new Color(0, 71, 255));

	private final Color color;

	public static Color get(Skill skill)
	{
		return values()[skill.ordinal()].color;
	}
}
