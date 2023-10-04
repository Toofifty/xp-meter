package com.toofifty.xpmeter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

@Singleton
public class XPMeterOverlay extends OverlayPanel
{
	public static final int DEFAULT_WIDTH = 180;
	public static final int DEFAULT_HEIGHT = 60;

	@Inject
	private XPTracker tracker;

	@Inject
	private Client client;

	@Setter
	private int updateInterval;

	@Getter
	private final XPChart chart = new XPChart();

	@Inject
	private XPMeterOverlay()
	{
		if (getPreferredSize() == null)
		{
			setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		}

		setPosition(OverlayPosition.TOP_LEFT);
		setMinimumSize(XPChart.MIN_WIDTH);
		setResizable(true);
		setResettable(true);
	}

	public void update()
	{
		var maxXPPerHour = 0;
		final var skillHistories = new HashMap<Skill, List<Point>>();
		for (var skill : tracker.getTrackedSkills())
		{
			final var history = tracker.getXPPerHourHistory(
				skill,
				Math.max(updateInterval, 1)
			);
			skillHistories.put(skill, history);

			for (var point : history)
			{
				if (point.getY() > maxXPPerHour)
				{
					maxXPPerHour = point.getY();
				}
			}
		}

		chart.setSkillXpHistories(skillHistories);
		chart.setMaxXPPerHour(maxXPPerHour);
		chart.setCurrentTick(client.getTickCount());
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (chart.hasData())
		{
			var size = getPreferredSize();
			if (size == null || size.width < DEFAULT_WIDTH || size.height < DEFAULT_HEIGHT)
			{
				size = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
			}

			chart.setPreferredSize(size);
			panelComponent.getChildren().add(chart);
		}
		else
		{
			panelComponent.getChildren().add(
				LineComponent.builder()
					.left("XP Meter - not enough data")
					.build()
			);
		}
		return super.render(graphics);
	}

	@Override
	public void onMouseOver()
	{
		super.onMouseOver();
	}
}
