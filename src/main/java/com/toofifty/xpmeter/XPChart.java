package com.toofifty.xpmeter;

import com.google.common.collect.Lists;
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
import java.util.Set;
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

	private static final Color PAUSE_MARKER_COLOR = new Color(0, 166, 255, 128);
	private static final Color LOGOUT_MARKER_COLOR = new Color(255, 68, 0, 128);

	private static final Color CURSOR_MARKER_COLOR = new Color(255, 255, 255, 128);

	private static final Color BACKGROUND_COLOR = new Color(0, 0, 0, 32);
	private static final Color RATE_BACKGROUND_COLOR = new Color(0, 0, 0, 64);
	private static final Color TOOLTIP_BACKGROUND_COLOR = new Color(0, 0, 0, 128);

	@Setter private SkillIconManager skillIconManager;

	// incoming data

	@Setter private Map<Skill, List<Point>> skillXpHistories = null;

	/**
	 * Skill keys, sorted from the lowest current rate to highest
	 */
	@Setter private List<Skill> sortedSkills = null;
	@Setter private int maxXpPerHour = 0;
	@Setter private int currentTick = 0;
	@Setter private Set<Integer> pauses = null;
	@Setter private Set<Integer> logouts = null;

	@Setter private XPTracker.Performance performance;

	@Setter private Point mouse = null;

	// configs

	@Setter private int span = secondsToTicks(180);
	@Setter private int resolution = 1;
	@Setter private int chartHeight = 60;
	@Setter private boolean showTimeLabels = true;
	@Setter private boolean showTimeMarkers = true;
	@Setter private boolean showXpLabels = true;
	@Setter private boolean showXpMarkers = true;
	@Setter private boolean showCurrentRates = true;
	@Setter private boolean showSkillIcons = true;
	@Setter private boolean longFormatNumbers = false;
	@Setter private boolean showPerformance = false;
	@Setter private boolean showHoverTooltips = true;
	@Setter private boolean dimNonHoveredSkills = true;
	@Setter private boolean showAllHovers = false;

	// local data

	private Skill hoveredSkill = null;

	public boolean hasData()
	{
		return currentTick > 0 && maxXpPerHour > 0;
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

		setHeightScale(maxXpPerHour);
		setWidthMin(Math.max(currentTick - span, 0));
		setWidthMax(currentTick);

		// background
		setColor(BACKGROUND_COLOR);
		fillRect(0, 0, size.width, size.height);

		drawXpLabels();
		drawTimeLabels();
		drawHistoryPlot();
		drawCurrentRates();
		drawPauses();
		drawPerformance();
		drawMouseOver();

		return dimension;
	}

	private void drawXpLabels()
	{
		final var xpIntervals = Intervals.getXpIntervals(maxXpPerHour);

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
			final var skillColor = getSkillColor(skill);
			final var history = skillXpHistories.get(skill);
			final var last = history.get(history.size() - 1);

			if (last != null && last.getY() != 0)
			{
				final var rate = format(last.y);
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
			setColor(getSkillColor(skill));
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

	public void drawPauses()
	{
		setColor(PAUSE_MARKER_COLOR);
		for (var tick : pauses)
		{
			drawVMarker(mapX(tick));
		}

		setColor(LOGOUT_MARKER_COLOR);
		for (var tick : logouts)
		{
			drawVMarker(mapX(tick));
		}
	}

	private void drawPerformance()
	{
		if (!showPerformance)
		{
			return;
		}

		final var time = performance.getComputeTime() == 0 ? "<1" : "" + performance.getComputeTime();

		final var text = time + "ms "
			+ "Cached: " + performance.getCacheSize() + " "
			+ "Hits: " + performance.getCacheHits() + " "
			+ "Misses: " + performance.getCacheMisses() + " "
			+ "Resolution: " + performance.getRenderedResolution() + "s ";

		final var y = size.height + fontHeight + TIME_LABEL_TPAD + (showTimeLabels ? fontHeight + TIME_LABEL_TPAD : 0);

		if (performance.getComputeTime() > 30)
		{
			setColor(Color.RED);
		}
		else if (performance.getComputeTime() > 10)
		{
			setColor(Color.ORANGE);
		}
		else if (performance.getComputeTime() > 4)
		{
			setColor(Color.YELLOW);
		}
		else
		{
			setColor(TIME_LABEL_COLOR);
		}

		drawText(text, 0, y, true);
	}

	public void drawMouseOver()
	{
		if (mouse == null || !showHoverTooltips)
		{
			hoveredSkill = null;
			return;
		}

		final var mx = mouse.x - offset.x;
		final var my = mouse.y - offset.y;

		if (mx < 0 || my < 0 || mx > size.width || my > size.height)
		{
			hoveredSkill = null;
			return;
		}

		// offset by half updateInterval to pseudo "round up"
		final var hoveredTick = unmapX(mx) + resolution / 2;
		// the span start can be off-phase from the updateInterval,
		// so the correction is added so hoveredInterval always lands
		// on a number divisible by updateInterval
		final var spanStartCorrection = Math.max(currentTick - span, 0) % resolution;
		final var hoveredInterval = hoveredTick - hoveredTick % resolution
			+ spanStartCorrection;

		// TODO: both add and sub correction from interval, then use closest to original x
		// if sub < 0, use add
		// if add > width, use sub

		// map back to x for visual clarity
		final var x = mapX(hoveredInterval) + CURR_RATE_LPAD;

		setColor(CURSOR_MARKER_COLOR);
		drawVMarker(x - CURR_RATE_LPAD);

		var closestY = Integer.MIN_VALUE;
		var closestXp = 0;
		Skill closestSkill = null;

		for (var skill : skillXpHistories.keySet())
		{
			final var history = skillXpHistories.get(skill);
			final var dataPoint = history.stream()
				.filter(point -> point.x == hoveredInterval)
				.findFirst().orElse(null);

			if (dataPoint != null && dataPoint.y != 0)
			{
				final var y = mapY(dataPoint.y, true);
				if (Math.abs(y - mouse.y) < Math.abs(closestY - mouse.y))
				{
					closestY = y;
					closestXp = dataPoint.y;
					closestSkill = skill;
				}

				if (!showAllHovers)
				{
					continue;
				}

				final var label = skill.getName() + ": " + format(dataPoint.y) + "/hr";

				setColor(TOOLTIP_BACKGROUND_COLOR);
				fillRoundRect(x - 1, y - fontHeight / 2 - 1, width(label) + 2, fontHeight + 2, 2);
				setColor(getSkillColor(skill));
				drawText(label, x, y + fontHeight / 2, true);
			}
		}

		hoveredSkill = closestSkill;

		if (!showAllHovers && closestSkill != null)
		{
			final var label = closestSkill.getName() + ": " + format(closestXp) + "/hr";

			setColor(TOOLTIP_BACKGROUND_COLOR);
			fillRoundRect(x - 1, closestY - fontHeight / 2 - 1, width(label) + 2, fontHeight + 2, 2);
			setColor(getSkillColor(closestSkill));
			drawText(label, x, closestY + fontHeight / 2, true);
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
			for (var xp : Intervals.getXpIntervals(maxXpPerHour))
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

				final var text = format(last.y);
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
		return (showTimeLabels ? fontHeight : 0) + (showPerformance ? fontHeight + TIME_LABEL_TPAD : 0);
	}

	private Color getSkillColor(Skill skill)
	{
		final var color = SkillColor.get(skill);
		if (dimNonHoveredSkills && hoveredSkill != null && skill != hoveredSkill)
		{
			return Color.DARK_GRAY;
		}

		return color;
	}

	private String format(int number)
	{
		return longFormatNumbers ? Util.format(number) : Util.shortFormat(number);
	}
}
