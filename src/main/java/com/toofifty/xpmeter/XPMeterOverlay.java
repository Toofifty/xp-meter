package com.toofifty.xpmeter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

@Singleton
public class XPMeterOverlay extends OverlayPanel
{
	public static final int DEFAULT_WIDTH = 180;
	public static final int DEFAULT_HEIGHT = 60;

	@Inject private XPTracker tracker;

	@Inject private Client client;

	@Setter private int updateInterval;

	@Getter private final XPChart chart = new XPChart();
	@Getter private boolean isMouseOver = false;

	@Inject
	private XPMeterOverlay(SkillIconManager skillIconManager)
	{
		if (getPreferredSize() == null)
		{
			setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		}

		setPosition(OverlayPosition.TOP_LEFT);
		setMinimumSize(XPChart.MIN_WIDTH);
		setResizable(true);
		setResettable(true);

		chart.setSkillIconManager(skillIconManager);
	}

	/**
	 * Recalculate XP histories and update the chart
	 */
	public void update()
	{
		var maxXPPerHour = 0;
		final var skillHistories = new HashMap<Skill, List<Point>>();
		final var skillCurrentRates = new HashMap<Skill, Integer>();
		for (var skill : tracker.getTrackedSkills())
		{
			final var history = tracker.getHistory(
				skill,
				Math.max(updateInterval, 1)
			);
			skillHistories.put(skill, history);

			final var last = history.get(history.size() - 1);
			skillCurrentRates.put(skill, last == null ? 0 : last.y);

			for (var point : history)
			{
				if (point.y > maxXPPerHour)
				{
					maxXPPerHour = point.y;
				}
			}
		}

		final var sortedSkills = skillCurrentRates.entrySet().stream()
			.sorted(Comparator.comparingInt(Map.Entry::getValue))
			.map(Map.Entry::getKey)
			.collect(Collectors.toList());

		chart.setSkillXpHistories(skillHistories);
		chart.setSortedSkills(sortedSkills);
		chart.setMaxXPPerHour(maxXPPerHour);
		chart.setCurrentTick(client.getTickCount());
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (chart.hasData())
		{
			panelComponent.getChildren().add(chart);
		}
		else
		{
			panelComponent.getChildren().add(
				LineComponent.builder()
					.left("XP Meter inactive")
					.build()
			);
		}

		// onMouseOver() is called each frame immediately after render,
		// so we can flick off the value for it to be re-set immediately
		// (if mouse is hovering)
		isMouseOver = false;

		return super.render(graphics);
	}

	@Override
	public void onMouseOver()
	{
		isMouseOver = true;
	}
}
