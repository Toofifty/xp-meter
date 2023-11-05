package com.toofifty.xpmeter;

import java.awt.Color;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Theme
{
	RUNELITE(
		new Color(0, 0, 0, 32),
		null,
		null,
		new Color(255, 255, 255, 128), false,
		new Color(255, 255, 255, 32),
		new Color(0, 0, 0, 32),
		true,
		null,
		new Color(0, 0, 0, 64),
		null, true,
		null,
		new Color(0, 0, 0, 64),
		null
	),
	TRANSPARENT(
		new Color(0, 0, 0, 32),
		null,
		new Color(0, 0, 0, 0),
		new Color(255, 255, 255, 128), false,
		new Color(255, 255, 255, 32),
		new Color(0, 0, 0, 32),
		true,
		null,
		new Color(0, 0, 0, 64),
		null, true,
		null,
		new Color(0, 0, 0, 64),
		null
	),
	OLD_SCHOOL(
		new Color(192, 176, 141, 255),
		new Color(45, 42, 35, 255),
		new Color(72, 63, 53, 255),
		new Color(253, 151, 32, 255), true,
		new Color(0, 0, 0, 64),
		new Color(0, 0, 0, 32),
		false,
		Color.BLACK,
		new Color(253, 253, 160, 255),
		Color.BLACK, false,
		new Color(92, 87, 72, 255),
		new Color(40, 37, 31, 255),
		new Color(255, 255, 0, 255)
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

	public final Color rateBorder;
	public final Color rateBackground;
	public final Color rateTextColor;
}
