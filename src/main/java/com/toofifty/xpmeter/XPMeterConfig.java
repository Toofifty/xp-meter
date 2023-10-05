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
		name = "Update interval",
		keyName = "updateInterval",
		description = "Plot a point on the chart every X seconds",
		position = 3,
		section = tracking
	)
	@Units(Units.SECONDS)
	@Range(min = 1)
	default int updateInterval()
	{
		return 1;
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

	@ConfigSection(
		name = "Interactivity",
		description = "Interactivity settings",
		position = 3
	)
	String interactivity = "interactivity";

	@ConfigItem(
		name = "Hover effects",
		keyName = "mouseHover",
		description = "Show momentary XP rates when hovering over the overlay",
		section = interactivity,
		position = 1
	)
	default boolean mouseHover()
	{
		return true;
	}

	@ConfigItem(
		name = "Scroll to zoom",
		keyName = "scrollZoom",
		description = "Hold Shift + scroll over the overlay to adjust the display span",
		section = interactivity,
		position = 2
	)
	default boolean scrollZoom()
	{
		return false;
	}
}
