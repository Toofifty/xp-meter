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
		name = "Display",
		description = "Display settings",
		position = 2
	)
	String display = "display";

	@ConfigItem(
		name = "Span",
		keyName = "span",
		description = "Show the last X seconds in the overlay",
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

	@ConfigItem(
		name = "Show performance",
		keyName = "showPerformance",
		description = "Show debugging performance metrics like compute time / cache info",
		section = display,
		position = 10
	)
	default boolean showPerformance()
	{
		return false;
	}

	@ConfigItem(
		name = "Use cache",
		keyName = "useCache",
		description = "",
		section = display,
		position = 11
	)
	default boolean useCache()
	{
		return false;
	}

	@ConfigSection(
		name = "Interactivity",
		description = "Interactivity settings",
		position = 3
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
}
