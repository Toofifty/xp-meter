package com.toofifty.xpmeter;

import java.awt.Color;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Theme
{
	RUNELITE(
		new Color(0, 0, 0, 32), // chartBackground
		null, // chartBorder
		null, // overlayBackground

		new Color(255, 255, 255, 128), // axisLabelColor
		false, // axisLabelShadow
		new Color(255, 255, 255, 32), // verticalMarkerColor
		new Color(0, 0, 0, 32), // horizontalMarkerColor
		true, // plotShadow

		null, // tooltipBorder
		new Color(0, 0, 0, 64), // tooltipBackground
		null, // tooltipTextColor
		true,  // tooltipTextShadow
		null, // rateOuterBorder
		null, // rateInnerBorder
		new Color(0, 0, 0, 64), // rateBackground
		null, // rateTextColor
		1, // ratePadding
		3 // rateMargin
	),
	TRANSPARENT(
		new Color(0, 0, 0, 32), // chartBackground
		null, // chartBorder
		new Color(0, 0, 0, 0), // overlayBackground

		new Color(255, 255, 255, 128), // axisLabelColor
		false, // axisLabelShadow
		new Color(255, 255, 255, 32), // verticalMarkerColor
		new Color(0, 0, 0, 32), // horizontalMarkerColor
		true, // plotShadow

		null, // tooltipBorder
		new Color(0, 0, 0, 128), // tooltipBackground
		null, // tooltipTextColor
		true, // tooltipTextShadow
		null, // rateOuterBorder
		null, // rateInnerBorder
		new Color(0, 0, 0, 128), // rateBackground
		null, // rateTextColor
		1, // ratePadding
		3 // rateMargin
	),
	OLD_SCHOOL(
		new Color(192, 176, 141, 255), // chartBackground
		new Color(45, 42, 35, 255), // chartBorder
		new Color(72, 63, 53, 255), // overlayBackground

		new Color(253, 151, 32, 255), // axisLabelColor
		true, // axisLabelShadow
		new Color(0, 0, 0, 64), // verticalMarkerColor
		new Color(0, 0, 0, 32), // horizontalMarkerColor
		false, // plotShadow

		Color.BLACK, // tooltipBorder
		new Color(253, 253, 160, 255), // tooltipBackground
		Color.BLACK,  // tooltipTextColor
		false, // tooltipTextShadow
		new Color(45, 42, 34, 255), // rateOuterBorder
		new Color(114, 100, 81, 255), // rateInnerBorder
		new Color(85, 76, 65, 255), // rateBackground
		new Color(255, 255, 0, 255), // rateTextColor
		3, // ratePadding
		3 // rateMargin
	);

	public final Color chartBackground;
	public final Color chartBorder;
	public final Color overlayBackground;

	public final Color axisLabelColor;
	public final boolean axisLabelShadow;
	public final Color verticalMarkerColor;
	public final Color horizonalMarkerColor;
	public final boolean plotShadow;

	public final Color tooltipBorder;
	public final Color tooltipBackground;
	public final Color tooltipTextColor;
	public final boolean tooltipTextShadow;

	public final Color rateOuterBorder;
	public final Color rateInnerBorder;
	public final Color rateBackground;
	public final Color rateTextColor;
	// x pad inside container
	public final int ratePadding;
	// x margin outside container
	public final int rateMargin;
}
