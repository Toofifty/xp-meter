package com.toofifty.xpmeter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(XPMeterConfig.GROUP_NAME)
public interface XPMeterConfig extends Config
{
	String GROUP_NAME = "xp-meter";

	@ConfigSection(
		name = "Tracking",
		description = "XP tracking settings",
		position = 1
	)
	String tracking = "tracking";

	@ConfigItem(
		name = "Tracking mode",
		keyName = "trackingMode",
		description = "Method of calculating XP/hr",
		position = 1,
		section = tracking
	)
	default TrackingMode trackingMode()
	{
		return TrackingMode.SLIDING_WINDOW;
	}

	@ConfigItem(
		name = "Sliding window",
		keyName = "windowInterval",
		description = "Calculate XP/hr data points based on XP gain in the last X seconds",
		position = 2,
		section = tracking
	)
	@Units(Units.SECONDS)
	@Range(min = 1)
	default int windowInterval()
	{
		return 60;
	}

	@ConfigItem(
		name = "Resolution",
		keyName = "resolution",
		description = "How frequently the plot updates",
		position = 3,
		section = tracking
	)
	@Units(Units.SECONDS)
	@Range(min = 1)
	default int resolution()
	{
		return 5;
	}

	@ConfigSection(
		name = "Skills",
		description = "Skill filtering",
		position = 2,
		closedByDefault = true
	)
	String skills = "skills";

	@ConfigItem(
		name = "Attack",
		keyName = "trackAttack",
		description = "Track Attack",
		position = 1,
		section = skills
	)
	default boolean trackAttack()
	{
		return true;
	}

	@ConfigItem(
		name = "Defence",
		keyName = "trackDefence",
		description = "Track Defence",
		position = 2,
		section = skills
	)
	default boolean trackDefence()
	{
		return true;
	}

	@ConfigItem(
		name = "Strength",
		keyName = "trackStrength",
		description = "Track Strength",
		position = 3,
		section = skills
	)
	default boolean trackStrength()
	{
		return true;
	}

	@ConfigItem(
		name = "Hitpoints",
		keyName = "trackHitpoints",
		description = "Track Hitpoints",
		position = 4,
		section = skills
	)
	default boolean trackHitpoints()
	{
		return true;
	}

	@ConfigItem(
		name = "Ranged",
		keyName = "trackRanged",
		description = "Track Ranged",
		position = 5,
		section = skills
	)
	default boolean trackRanged()
	{
		return true;
	}

	@ConfigItem(
		name = "Prayer",
		keyName = "trackPrayer",
		description = "Track Prayer",
		position = 6,
		section = skills
	)
	default boolean trackPrayer()
	{
		return true;
	}

	@ConfigItem(
		name = "Magic",
		keyName = "trackMagic",
		description = "Track Magic",
		position = 7,
		section = skills
	)
	default boolean trackMagic()
	{
		return true;
	}

	@ConfigItem(
		name = "Cooking",
		keyName = "trackCooking",
		description = "Track Cooking",
		position = 8,
		section = skills
	)
	default boolean trackCooking()
	{
		return true;
	}

	@ConfigItem(
		name = "Woodcutting",
		keyName = "trackWoodcutting",
		description = "Track Woodcutting",
		position = 9,
		section = skills
	)
	default boolean trackWoodcutting()
	{
		return true;
	}

	@ConfigItem(
		name = "Fletching",
		keyName = "trackFletching",
		description = "Track Fletching",
		position = 10,
		section = skills
	)
	default boolean trackFletching()
	{
		return true;
	}

	@ConfigItem(
		name = "Fishing",
		keyName = "trackFishing",
		description = "Track Fishing",
		position = 11,
		section = skills
	)
	default boolean trackFishing()
	{
		return true;
	}

	@ConfigItem(
		name = "Firemaking",
		keyName = "trackFiremaking",
		description = "Track Firemaking",
		position = 12,
		section = skills
	)
	default boolean trackFiremaking()
	{
		return true;
	}

	@ConfigItem(
		name = "Crafting",
		keyName = "trackCrafting",
		description = "Track Crafting",
		position = 13,
		section = skills
	)
	default boolean trackCrafting()
	{
		return true;
	}

	@ConfigItem(
		name = "Smithing",
		keyName = "trackSmithing",
		description = "Track Smithing",
		position = 14,
		section = skills
	)
	default boolean trackSmithing()
	{
		return true;
	}

	@ConfigItem(
		name = "Mining",
		keyName = "trackMining",
		description = "Track Mining",
		position = 15,
		section = skills
	)
	default boolean trackMining()
	{
		return true;
	}

	@ConfigItem(
		name = "Herblore",
		keyName = "trackHerblore",
		description = "Track Herblore",
		position = 16,
		section = skills
	)
	default boolean trackHerblore()
	{
		return true;
	}

	@ConfigItem(
		name = "Agility",
		keyName = "trackAgility",
		description = "Track Agility",
		position = 17,
		section = skills
	)
	default boolean trackAgility()
	{
		return true;
	}

	@ConfigItem(
		name = "Thieving",
		keyName = "trackThieving",
		description = "Track Thieving",
		position = 18,
		section = skills
	)
	default boolean trackThieving()
	{
		return true;
	}

	@ConfigItem(
		name = "Slayer",
		keyName = "trackSlayer",
		description = "Track Slayer",
		position = 19,
		section = skills
	)
	default boolean trackSlayer()
	{
		return true;
	}

	@ConfigItem(
		name = "Farming",
		keyName = "trackFarming",
		description = "Track Farming",
		position = 20,
		section = skills
	)
	default boolean trackFarming()
	{
		return true;
	}

	@ConfigItem(
		name = "Runecraft",
		keyName = "trackRunecraft",
		description = "Track Runecraft",
		position = 21,
		section = skills
	)
	default boolean trackRunecraft()
	{
		return true;
	}

	@ConfigItem(
		name = "Hunter",
		keyName = "trackHunter",
		description = "Track Hunter",
		position = 22,
		section = skills
	)
	default boolean trackHunter()
	{
		return true;
	}

	@ConfigItem(
		name = "Construction",
		keyName = "trackConstruction",
		description = "Track Construction",
		position = 23,
		section = skills
	)
	default boolean trackConstruction()
	{
		return true;
	}

	@ConfigSection(
		name = "Display",
		description = "Display settings",
		position = 3
	)
	String display = "display";

	@ConfigItem(
		name = "Span",
		keyName = "span",
		description = "Show the last X seconds in the chart",
		position = 1,
		section = display
	)
	@Units(Units.SECONDS)
	@Range(min = 10)
	default int span()
	{
		return 180;
	}

	@ConfigItem(
		name = "Chart height",
		keyName = "chartHeight",
		description = "Adjust chart height",
		position = 2,
		section = display
	)
	@Range(min = 10,
		   max = 200)
	default int chartHeight()
	{
		return 60;
	}

	@ConfigItem(
		name = "Time labels",
		keyName = "showTimeLabels",
		description = "Show time labels on the X axis",
		section = display,
		position = 3
	)
	default boolean showTimeLabels()
	{
		return true;
	}

	@ConfigItem(
		name = "Time markers",
		keyName = "showTimeMarkers",
		description = "Show vertical time marker lines in the chart",
		section = display,
		position = 4
	)
	default boolean showTimeMarkers()
	{
		return true;
	}

	@ConfigItem(
		name = "XP labels",
		keyName = "showXpLabels",
		description = "Show XP labels on the Y axis",
		section = display,
		position = 5
	)
	default boolean showXpLabels()
	{
		return true;
	}

	@ConfigItem(
		name = "XP markers",
		keyName = "showXpMarkers",
		description = "Show horizontal XP marker lines in the chart",
		section = display,
		position = 6
	)
	default boolean showXpMarkers()
	{
		return true;
	}

	@ConfigItem(
		name = "Display current rates",
		keyName = "showCurrentRates",
		description = "Show current XP rates at the end of each line",
		section = display,
		position = 7
	)
	default boolean showCurrentRates()
	{
		return true;
	}

	@ConfigItem(
		name = "Display skill icons",
		keyName = "showSkillIcons",
		description = "Show mini skill icons at the end of each line",
		section = display,
		position = 8
	)
	default boolean showSkillIcons()
	{
		return false;
	}

	@ConfigItem(
		name = "Long format numbers",
		keyName = "longFormatNumbers",
		description = "Show all rates in a longer format. e.g. 69,420 instead of 69K, or 1,200K instead of 1M",
		section = display,
		position = 9
	)
	default boolean longFormatNumbers()
	{
		return false;
	}

	@ConfigSection(
		name = "Interactivity",
		description = "Interactivity settings",
		position = 4
	)
	String interactivity = "interactivity";

	@ConfigItem(
		name = "Hover effects",
		keyName = "showHoverTooltips",
		description = "Show momentary XP rates when hovering over the chart",
		section = interactivity,
		position = 1
	)
	default boolean showHoverTooltips()
	{
		return true;
	}

	@ConfigItem(
		name = "Dim non-hovered skills",
		keyName = "dimNonHoveredSkills",
		description = "Dim other skills when hovering to make it easier to see the target skill's plot",
		section = interactivity,
		position = 2
	)
	default boolean dimNonHoveredSkills()
	{
		return false;
	}

	@ConfigItem(
		name = "Display all tooltips",
		keyName = "showAllHovers",
		description = "Toggle between showing 1 or all skills when hovering over the chart",
		section = interactivity,
		position = 3
	)
	default boolean showAllHovers()
	{
		return true;
	}

	@ConfigItem(
		name = "Scroll to zoom",
		keyName = "scrollZoom",
		description = "Hold Shift + scroll over the overlay to adjust the display span",
		section = interactivity,
		position = 4
	)
	default boolean scrollZoom()
	{
		return false;
	}

	@ConfigSection(
		name = "Debugging",
		description = "Developer tools for debugging",
		position = 99,
		closedByDefault = true
	)
	String debugging = "debugging";

	@ConfigItem(
		name = "Show performance",
		keyName = "showPerformance",
		description = "Show debugging performance metrics like compute time / cache info",
		section = debugging,
		position = 1
	)
	default boolean showPerformance()
	{
		return false;
	}

	@ConfigItem(
		name = "Disable cache",
		keyName = "disableCache",
		description = "Disables caching computed values. Disabling this can have a large performance impact.",
		section = debugging,
		position = 2
	)
	default boolean disableCache()
	{
		return false;
	}

	@ConfigItem(
		name = "Disable dynamic resolution",
		keyName = "disableDynamicResolution",
		description = "Disables resolution dynamically scaling to the width of the chart.",
		section = debugging,
		position = 3
	)
	default boolean disableDynamicResolution()
	{
		return false;
	}

	@ConfigItem(
		name = "Enable data import/export",
		keyName = "enableDataMenuOptions",
		description = "Enables data import and export tool options when shift + right-clicking the overlay",
		section = debugging,
		position = 4
	)
	default boolean enableDataMenuOptions()
	{
		return false;
	}

	@ConfigItem(
		name = "Session data",
		keyName = "sessionData",
		description = "Result of the last data export",
		section = debugging,
		position = 5
	)
	default String sessionData()
	{
		return "";
	}
}
