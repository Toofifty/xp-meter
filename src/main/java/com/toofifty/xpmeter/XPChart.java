package com.toofifty.xpmeter;

import com.google.common.collect.Lists;
import static com.toofifty.xpmeter.Util.rsFormat;
import static com.toofifty.xpmeter.Util.secondsToTicks;
import static com.toofifty.xpmeter.Util.shortRsFormat;
import static com.toofifty.xpmeter.Util.ticksToSeconds;
import static com.toofifty.xpmeter.Util.ticksToTime;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Skill;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;

public class XPChart implements LayoutableRenderableEntity
{
	public static final int MIN_WIDTH = 60;
	public static final int SKILL_ICON_WIDTH = 16;

	private static final int TIME_LABEL_TPAD = 2;
	private static final int TIME_LABEL_SPACING = 4;
	private static final int XP_LABEL_RPAD = 2;
	private static final int CURR_RATE_LPAD = 4;

	private static final Color XP_LABEL_COLOR = new Color(255, 255, 255, 128);
	private static final Color XP_MARKER_COLOR = new Color(0, 0, 0, 32);
	private static final Color TIME_LABEL_COLOR = new Color(255, 255, 255, 128);
	private static final Color TIME_MARKER_COLOR = new Color(255, 255, 255, 32);

	private static final Color BACKGROUND_COLOR = new Color(0, 0, 0, 32);

	@Getter
	@Setter
	private Dimension preferredSize;

	@Getter
	@Setter
	private Point preferredLocation;

	@Setter
	private Map<Skill, List<net.runelite.api.Point>> skillXpHistories = null;

	@Setter
	private int maxXPPerHour = 0;

	@Setter
	private int currentTick = 0;

	// configs

	@Setter private int span = secondsToTicks(180);
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

		graphics.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_OFF
		);
		graphics.setFont(FontManager.getRunescapeSmallFont());

		final var size = getBounds();
		// initial starting point based on parent layout
		final var loc = getPreferredLocation();

		final var marginLeft = calculateLeftMargin(graphics);
		final var marginRight = calculateRightMargin(graphics);
		final var marginTop = calculateTopMargin(graphics);
		final var marginBottom = calculateBottomMargin(graphics);

		final var margin = new Point(loc.x + marginLeft, loc.y + marginTop);

		// background
		graphics.setColor(BACKGROUND_COLOR);
		graphics.fillRect(margin.x, margin.y, size.width, size.height);

		drawXpLabels(graphics, size, margin);
		drawTimeLabels(graphics, size, margin);
		plotHistories(graphics, size, margin);
		drawCurrentRates(graphics, size, margin);

		return new Dimension(
			marginLeft + size.width + marginRight,
			marginTop + size.height + marginBottom
		);
	}

	@Override
	public Rectangle getBounds()
	{
		final var size = getPreferredSize();
		if (size == null)
		{
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

	private void drawXpLabels(Graphics2D graphics, Rectangle size, Point loc)
	{
		final var xpIntervals = getXpIntervals();
		graphics.setColor(XP_LABEL_COLOR);

		final var availableHeight = getAvailableHeight(graphics, size);
		final var yOffset = getYOffset(graphics, loc);

		if (showXpLabels)
		{
			for (var xp : xpIntervals)
			{
				// no point rendering 0
				if (xp == 0) continue;

				final var y = availableHeight - xp * availableHeight / maxXPPerHour;
				final var label = shortRsFormat(xp);
				final var width = graphics.getFontMetrics().stringWidth(label);
				final var height = graphics.getFontMetrics().getHeight();

				graphics.drawString(
					label,
					loc.x - width - XP_LABEL_RPAD,
					yOffset + y + height / 2
				);
			}
		}

		if (showXpMarkers)
		{
			graphics.setColor(XP_MARKER_COLOR);
			for (var xp : xpIntervals)
			{
				final var y = availableHeight - xp * availableHeight / maxXPPerHour;

				graphics.drawLine(
					loc.x, yOffset + y,
					loc.x + size.width, yOffset + y
				);
			}
		}
	}

	private List<Integer> getXpIntervals()
	{
		var interval = 500_000;
		if (maxXPPerHour < 10_000)
		{
			interval = 5_000;
		}
		else if (maxXPPerHour < 25_000)
		{
			interval = 10_000;
		}
		else if (maxXPPerHour < 100_000)
		{
			interval = 25_000;
		}
		else if (maxXPPerHour < 250_000)
		{
			interval = 100_000;
		}
		else if (maxXPPerHour < 500_000)
		{
			interval = 250_000;
		}

		final var xpIntervals = new ArrayList<Integer>();
		for (var i = 0; i < maxXPPerHour; i += interval)
		{
			xpIntervals.add(i);
		}
		return xpIntervals;
	}

	private void drawTimeLabels(Graphics2D graphics, Rectangle size, Point loc)
	{
		final var timeIntervals = getTimeIntervals();
		graphics.setColor(TIME_LABEL_COLOR);

		final var fontHeight = graphics.getFontMetrics().getHeight();

		var originX = Integer.MIN_VALUE;

		// removing it now prevents the first marker
		// from showing even if showTimeLabels is false
		final var origin = timeIntervals.remove(0);

		if (showTimeLabels)
		{
			{
				final var x = tickToX(origin);
				final var time = ticksToTime(origin);
				final var width = graphics.getFontMetrics().stringWidth(time);

				graphics.drawString(
					time,
					loc.x + (x - width / 2),
					loc.y + size.height + fontHeight + TIME_LABEL_TPAD
				);

				originX = x + width / 2 + TIME_LABEL_SPACING;
			}

			// reverse so rightmost labels have priority
			var lastX = Integer.MAX_VALUE;
			for (var timeTick : Lists.reverse(timeIntervals))
			{
				final var x = tickToX(timeTick);

				final var time = ticksToTime(timeTick);
				final var width = graphics.getFontMetrics().stringWidth(time);

				// do not draw overlapping times
				// (either on next time to the right, or origin)
				if (x + width / 2 < lastX && x - width / 2 > originX)
				{
					graphics.drawString(
						time,
						loc.x + (x - width / 2),
						loc.y + size.height + fontHeight + TIME_LABEL_TPAD
					);

					lastX = x - width / 2 - TIME_LABEL_SPACING;
				}
			}
		}

		if (showTimeMarkers)
		{
			graphics.setColor(TIME_MARKER_COLOR);
			for (var timeTick : timeIntervals)
			{
				final var x = tickToX(timeTick);
				graphics.drawLine(
					loc.x + x, loc.y,
					loc.x + x, loc.y + size.height
				);
			}
		}
	}

	private List<Integer> getTimeIntervals()
	{
		final var chartSpan = ticksToSeconds(Math.min(span, currentTick));

		var interval = secondsToTicks(3600);
		if (chartSpan < secondsToTicks(120)) // 2min
		{
			interval = secondsToTicks(15);
		}
		else if (chartSpan < secondsToTicks(240)) // 4min
		{
			interval = secondsToTicks(30);
		}
		else if (chartSpan < secondsToTicks(480)) // 8min
		{
			interval = secondsToTicks(60);
		}
		else if (chartSpan < secondsToTicks(1800)) // 30min
		{
			interval = secondsToTicks(300);
		}
		else if (chartSpan < secondsToTicks(3600)) // 1h
		{
			interval = secondsToTicks(900);
		}
		else if (chartSpan < secondsToTicks(7200)) // 2h
		{
			interval = secondsToTicks(1800);
		}

		final var timeIntervals = new ArrayList<Integer>();
		final var spanStart = Math.max(currentTick - span, 0);

		// always add span start time
		timeIntervals.add(spanStart);

		// generate all time intervals up to current tick
		// only keep if > spanStart
		for (var i = 0; i < currentTick; i += interval)
		{
			if (i > spanStart) timeIntervals.add(i);
		}

		// always add current time
		timeIntervals.add(currentTick);

		return timeIntervals;
	}

	private void drawCurrentRates(Graphics2D graphics, Rectangle size, Point loc)
	{
		if (!showCurrentRates && !showSkillIcons)
		{
			return;
		}

		final var availableHeight = getAvailableHeight(graphics, size);
		final var yOffset = getYOffset(graphics, loc);

		for (var skill : skillXpHistories.keySet())
		{
			final var color = SkillColor.get(skill);
			final var history = skillXpHistories.get(skill);
			final var last = history.get(history.size() - 1);

			if (last != null && last.getY() != 0)
			{
				final var rate = showSkillIcons
					? shortRsFormat(last.getY())
					: rsFormat(last.getY());
				final var width = CURR_RATE_LPAD + graphics.getFontMetrics().stringWidth(rate);

				final var y = availableHeight - last.getY() * availableHeight / maxXPPerHour
					+ graphics.getFontMetrics().getHeight() / 2;

				if (showCurrentRates)
				{
					// shadow
					graphics.setColor(Color.BLACK);
					graphics.drawString(
						rate,
						loc.x + size.width + CURR_RATE_LPAD + 1,
						yOffset + y + 1
					);
					graphics.setColor(color);
					graphics.drawString(
						rate,
						loc.x + size.width + CURR_RATE_LPAD,
						yOffset + y
					);
				}
			}
		}
	}

	private void plotHistories(Graphics2D graphics, Rectangle size, Point loc)
	{
		final var availableHeight = getAvailableHeight(graphics, size);
		final var yOffset = getYOffset(graphics, loc);

		for (var skill : skillXpHistories.keySet())
		{
			final var color = SkillColor.get(skill);
			var isFlatlining = false;

			Point prev = null;
			for (var point : skillXpHistories.get(skill))
			{
				final var x = tickToX(point.getX());
				final var y = availableHeight - point.getY() * availableHeight / maxXPPerHour;

				// flat lining if xp = 0, and trying to draw at same y coord
				isFlatlining = prev != null && point.getY() == 0 && prev.y == y;

				if (prev != null && !isFlatlining)
				{
					// shadow
					graphics.setColor(Color.BLACK);
					graphics.drawLine(
						loc.x + prev.x + 1, yOffset + prev.y + 1,
						loc.x + x + 1, yOffset + y + 1
					);
					// line
					graphics.setColor(color);
					graphics.drawLine(
						loc.x + prev.x, yOffset + prev.y,
						loc.x + x, yOffset + y
					);
				}

				prev = new Point(x, y);
			}

			// draw to end of chart
			if (prev != null && !isFlatlining)
			{
				// shadow
				graphics.setColor(Color.BLACK);
				graphics.drawLine(
					loc.x + prev.x + 1, yOffset + prev.y + 1,
					loc.x + size.width + 1, yOffset + prev.y + 1
				);
				// line
				graphics.setColor(color);
				graphics.drawLine(
					loc.x + prev.x, yOffset + prev.y,
					loc.x + size.width, yOffset + prev.y
				);
			}
		}
	}

	/**
	 * Translate absolute game tick to relative
	 * X position in chart (not including parent offset)
	 */
	private int tickToX(int t)
	{
		final var size = getBounds();
		final var spanStart = Math.max(currentTick - span, 0);
		final var tickInSpan = t - spanStart;

		return (tickInSpan * size.width) / Math.min(currentTick, span);
	}

	private int calculateLeftMargin(Graphics2D graphics)
	{
		var xpLabelWidth = 0;
		if (showXpLabels)
		{
			for (var xp : getXpIntervals())
			{
				final var label = shortRsFormat(xp);
				final var width = graphics.getFontMetrics().stringWidth(label)
					+ XP_LABEL_RPAD;
				if (width > xpLabelWidth)
				{
					xpLabelWidth = width;
				}
			}
		}

		// width of half "00:00"
		// (using actual origin time would cause the entire chart
		// to jiggle)
		final var originTimeWidth = showTimeLabels
			? graphics.getFontMetrics().stringWidth(ticksToTime(0)) / 2
			: 0;

		return Math.max(xpLabelWidth, originTimeWidth);
	}

	private int calculateTopMargin(Graphics2D graphics)
	{
		// should not need a margin - chart will already be a little
		// taller than any graphics
		return 0;
	}

	private int calculateRightMargin(Graphics2D graphics)
	{
		// width of the largest current rate
		var currentRateWidth = 0;
		if (showCurrentRates || showSkillIcons)
		{
			for (var rates : skillXpHistories.values())
			{
				final var last = rates.get(rates.size() - 1);
				if (last != null)
				{
					final var text = showSkillIcons
						? shortRsFormat(last.getY())
						: rsFormat(last.getY());
					var width = showCurrentRates
						? graphics.getFontMetrics().stringWidth(text)
						+ CURR_RATE_LPAD
						: 0;
					if (showSkillIcons)
					{
						width += SKILL_ICON_WIDTH;
					}

					if (width > currentRateWidth)
					{
						currentRateWidth = width;
					}
				}
			}
		}

		// width of half current time
		final var currentTimeWidth = showTimeLabels
			? graphics.getFontMetrics().stringWidth(ticksToTime(currentTick)) / 2
			: 0;

		return Math.max(currentRateWidth, currentTimeWidth);
	}

	private int calculateBottomMargin(Graphics2D graphics)
	{
		return showTimeLabels
			? graphics.getFontMetrics().getHeight()
			: 0;
	}

	private int getYOffset(Graphics2D graphics, Point loc)
	{
		// always allow room for half a text line at the top
		return loc.y + graphics.getFontMetrics().getHeight() / 2;
	}

	private int getAvailableHeight(Graphics2D graphics, Rectangle size)
	{
		// always allow room for half a text line at the top
		return size.height - graphics.getFontMetrics().getHeight() / 2;
	}
}
