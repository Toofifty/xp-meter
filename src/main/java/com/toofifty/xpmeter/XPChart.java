package com.toofifty.xpmeter;

import com.google.common.collect.Lists;
import static com.toofifty.xpmeter.Util.format;
import static com.toofifty.xpmeter.Util.secondsToTicks;
import static com.toofifty.xpmeter.Util.shortFormat;
import static com.toofifty.xpmeter.Util.ticksToTime;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Skill;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;

public class XPChart extends XPChartBase implements LayoutableRenderableEntity
{
	public static final int MIN_WIDTH = 60;
	public static final int SKILL_ICON_WIDTH = 16;

	// padding

	private static final int TIME_LABEL_TPAD = 2;
	private static final int TIME_LABEL_SPACING = 4;
	private static final int XP_LABEL_RPAD = 2;
	private static final int CURR_RATE_LPAD = 4;

	private static final Color XP_LABEL_COLOR = new Color(255, 255, 255, 128);
	private static final Color XP_MARKER_COLOR = new Color(0, 0, 0, 32);
	private static final Color TIME_LABEL_COLOR = new Color(255, 255, 255, 128);
	private static final Color TIME_MARKER_COLOR = new Color(255, 255, 255, 32);

	private static final Color BACKGROUND_COLOR = new Color(0, 0, 0, 32);
	private static final Color RATE_BACKGROUND_COLOR = new Color(0, 0, 0, 64);

	@Setter private Map<Skill, List<Point>> skillXpHistories = null;
	@Setter private SkillIconManager skillIconManager;

	/**
	 * Skill keys, sorted from the lowest current rate to highest
	 */
	@Setter private List<Skill> sortedSkills = null;
	@Setter private int maxXPPerHour = 0;
	@Setter private int currentTick = 0;

	// configs

	@Setter @Getter private int span = secondsToTicks(180);
	@Setter private int chartHeight = 60;
	@Setter private boolean showTimeLabels = true;
	@Setter private boolean showTimeMarkers = true;
	@Setter private boolean showXpLabels = true;
	@Setter private boolean showXpMarkers = true;
	@Setter private boolean showCurrentRates = true;
	@Setter private boolean showSkillIcons = true;

	public boolean hasData()
	{
		return currentTick > 0 && maxXPPerHour > 0;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!hasData())
		{
			return new Dimension(0, 0);
		}

		// antialias off results in thicker looking lines,
		// and are better upscaled by xBR
		graphics.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_OFF
		);

		final var dimension = super.render(graphics);

		setHeightScale(maxXPPerHour);
		setWidthMin(Math.max(currentTick - span, 0));
		setWidthMax(currentTick);

		// background
		setColor(BACKGROUND_COLOR);
		fillRect(0, 0, size.width, size.height);

		drawXpLabels();
		drawTimeLabels();
		drawHistoryPlot();
		drawCurrentRates();

		return dimension;
	}

	private void drawXpLabels()
	{
		final var xpIntervals = Intervals.getXpIntervals(maxXPPerHour);

		if (showXpLabels)
		{
			var lastY = 0;
			setColor(XP_LABEL_COLOR);
			// reverse so topmost labels have priority
			for (var xp : Lists.reverse(xpIntervals))
			{
				// no point rendering 0
				if (xp == 0) continue;

				final var label = shortFormat(xp);
				final var x = -width(label) - XP_LABEL_RPAD;
				final var y = mapY(xp, true) + fontHeight / 2;

				// do not draw overlapping xps
				if (y - fontHeight > lastY)
				{
					drawText(label, x, y);
					lastY = y;
				}
			}
		}

		if (showXpMarkers)
		{
			setColor(XP_MARKER_COLOR);
			for (var xp : xpIntervals)
			{
				drawHMarker(mapY(xp, true));
			}
		}
	}

	private void drawTimeLabels()
	{
		final var timeIntervals = Intervals.getTimeIntervals(
			Math.max(currentTick - span, 0),
			currentTick
		);

		var originX = Integer.MIN_VALUE;

		// removing it now prevents the first marker
		// from showing even if showTimeLabels is false
		final var origin = timeIntervals.remove(0);
		final var y = size.height + fontHeight + TIME_LABEL_TPAD;

		if (showTimeLabels)
		{
			setColor(TIME_LABEL_COLOR);
			{
				final var time = ticksToTime(origin);
				final var width = width(time);
				final var x = mapX(origin) - width / 2;

				drawText(time, x, y);

				originX = x + width + TIME_LABEL_SPACING;
			}

			var lastX = Integer.MAX_VALUE;
			// reverse so rightmost labels have priority
			for (var timeTick : Lists.reverse(timeIntervals))
			{
				final var time = ticksToTime(timeTick);
				final var width = width(time);
				final var x = mapX(timeTick) - width / 2;

				// do not draw overlapping times
				// (either on next time to the right, or origin)
				if (x + width < lastX && x > originX)
				{
					drawText(time, x, y);

					lastX = x - TIME_LABEL_SPACING;
				}
			}
		}

		if (showTimeMarkers)
		{
			setColor(TIME_MARKER_COLOR);
			for (var timeTick : timeIntervals)
			{
				drawVMarker(mapX(timeTick));
			}
		}
	}

	private void drawCurrentRates()
	{
		if (!showCurrentRates && !showSkillIcons)
		{
			return;
		}

		final var baseX = size.width + CURR_RATE_LPAD;

		for (var skill : sortedSkills)
		{
			final var skillColor = SkillColor.get(skill);
			final var history = skillXpHistories.get(skill);
			final var last = history.get(history.size() - 1);

			if (last != null && last.getY() != 0)
			{
				final var rate = showSkillIcons
					? shortFormat(last.y)
					: format(last.y);

				final var y = mapY(last.y, true);
				var x = baseX;

				if (showSkillIcons)
				{
					final var icon = skillIconManager.getSkillImage(skill, true);
					x += icon.getWidth() + CURR_RATE_LPAD;
					drawImage(icon, baseX, y - icon.getHeight() / 2);
				}

				if (showCurrentRates)
				{
					setColor(RATE_BACKGROUND_COLOR);
					fillRoundRect(x - 1, y - fontHeight / 2 - 1, width(rate) + 2, fontHeight + 2, 2);
					setColor(skillColor);
					drawText(rate, x, y + fontHeight / 2, true);
				}
			}
		}
	}

	private void drawHistoryPlot()
	{
		for (var skill : sortedSkills)
		{
			setColor(SkillColor.get(skill));
			var isFlatlining = false;

			Point prev = null;
			for (var point : skillXpHistories.get(skill))
			{
				final var x = mapX(point.x);
				final var y = mapY(point.y, true);

				// flat lining if xp = 0, and trying to draw at same y coord
				isFlatlining = prev != null && point.getY() == 0 && prev.y == y;

				if (prev != null && !isFlatlining)
				{
					drawLine(prev.x, prev.y, x, y, true);
				}

				prev = new Point(x, y);
			}

			// draw to end of chart
			if (prev != null && !isFlatlining)
			{
				drawLine(prev.x, prev.y, size.width, prev.y, true);
			}
		}
	}

	@Override
	public Rectangle getBounds()
	{
		final var size = getPreferredSize();
		if (size == null)
		{
			// default size
			return new Rectangle(
				XPMeterOverlay.DEFAULT_WIDTH,
				chartHeight
			);
		}

		return new Rectangle(
			Math.max(size.width, MIN_WIDTH),
			Math.max(size.height, chartHeight)
		);
	}

	@Override
	protected int calculateLeftMargin()
	{
		var xpLabelWidth = 0;
		if (showXpLabels)
		{
			for (var xp : Intervals.getXpIntervals(maxXPPerHour))
			{
				final var width = width(shortFormat(xp)) + XP_LABEL_RPAD;
				if (width > xpLabelWidth)
				{
					xpLabelWidth = width;
				}
			}
		}

		// width of half "00:00" or "0:00:00"
		// (using actual origin time would cause the entire chart
		// to jiggle)
		final var originTimeWidth = showTimeLabels
			? width(ticksToTime(currentTick > 6000 ? 6000 : 0)) / 2
			: 0;

		return Math.max(xpLabelWidth, originTimeWidth);
	}

	@Override
	protected int calculateTopMargin()
	{
		// should not need a margin - chart will already be a little
		// taller than any graphics
		return 0;
	}

	@Override
	protected int calculateRightMargin()
	{
		// width of the largest current rate
		var currentRateWidth = 0;
		if (showCurrentRates || showSkillIcons)
		{
			for (var rates : skillXpHistories.values())
			{
				final var last = rates.get(rates.size() - 1);
				if (last == null)
				{
					continue;
				}

				final var text = showSkillIcons ? shortFormat(last.y) : format(last.y);
				final var width = (showCurrentRates ? width(text) + CURR_RATE_LPAD : 0)
					+ (showSkillIcons ? SKILL_ICON_WIDTH + CURR_RATE_LPAD : 0);

				if (width > currentRateWidth)
				{
					currentRateWidth = width;
				}
			}
		}

		// width of half current time
		final var currentTimeWidth = showTimeLabels
			? width(ticksToTime(currentTick)) / 2
			: 0;

		return Math.max(currentRateWidth, currentTimeWidth);
	}

	@Override
	protected int calculateBottomMargin()
	{
		return showTimeLabels ? fontHeight : 0;
	}
}
