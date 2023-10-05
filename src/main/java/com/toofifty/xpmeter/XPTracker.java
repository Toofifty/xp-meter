package com.toofifty.xpmeter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import static com.toofifty.xpmeter.Util.secondsToTicks;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Skill;

@Singleton
public class XPTracker
{
	private static final int ONE_MINUTE = 100;
	private static final int ONE_HOUR = 60 * ONE_MINUTE;

	@Inject private Client client;
	@Inject private XPMeterConfig config;

	private final Map<Skill, List<XPGain>> xpGained = new HashMap<>();
	private final Map<Skill, Integer> lastXp = new HashMap<>();

	@Getter
	private int currentTick;

	public void track(Skill skill, int xp)
	{
		if (lastXp.containsKey(skill))
		{
			if (!xpGained.containsKey(skill))
			{
				xpGained.put(skill, new ArrayList<>());
			}

			final var diff = xp - lastXp.get(skill);
			xpGained.get(skill).add(new XPGain(currentTick, diff));
		}

		lastXp.put(skill, xp);
	}

	public Set<Skill> getTrackedSkills()
	{
		return xpGained.keySet();
	}

	public int getXPPerHourAt(Skill skill, int tick)
	{
		final var xpGains = xpGained.getOrDefault(skill, List.of());
		final var interval = secondsToTicks(config.windowInterval());

		final var xpGained = xpGains.stream()
			.filter((XPGain xpGain) -> xpGain.tick <= tick && (
				config.trackingMode() == TrackingMode.CUMULATIVE || xpGain.tick > (tick - interval)
			))
			.mapToInt(xpGain -> xpGain.xp)
			.sum();

		if (config.trackingMode() == TrackingMode.CUMULATIVE)
		{
			return xpGained * ONE_HOUR / Math.max(ONE_MINUTE, tick);
		}

		return xpGained * ONE_HOUR / interval;
	}

	public List<Point> getHistory(Skill skill, int updateInterval)
	{
		final var history = new ArrayList<Point>();
		final var startTick = Math.max(currentTick - secondsToTicks(config.span()), 0);
		for (int t = startTick; t < currentTick; t += updateInterval)
		{
			history.add(new Point(t, getXPPerHourAt(skill, t)));
		}

		return history;
	}

	public void tick()
	{
		currentTick++;
	}

	public void reset()
	{
		xpGained.clear();
	}

	@AllArgsConstructor
	static class XPGain
	{
		private int tick;
		private int xp;
	}
}
