package com.toofifty.xpmeter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import static com.toofifty.xpmeter.Util.secondsToTicks;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.Skill;

@Singleton
public class XPTracker
{
	private static final int MIN_TICKS = 100;

	@Inject
	private Client client;

	@Inject
	private XPMeterConfig config;

	private final Map<Skill, List<XPGain>> xpGained = new HashMap<>();
	private final Map<Skill, Integer> lastXp = new HashMap<>();

	@Getter
	private int ticks;

	@Setter
	private int windowTickInterval = 300;

	public void track(Skill skill, int xp)
	{
		if (lastXp.containsKey(skill))
		{
			if (!xpGained.containsKey(skill))
			{
				xpGained.put(skill, new ArrayList<>());
			}

			final var diff = xp - lastXp.get(skill);
			final var xpGains = xpGained.get(skill);
			xpGains.add(new XPGain(skill, diff, client.getTickCount()));
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

		final Predicate<XPGain> predicate = (XPGain xpGain) ->
			xpGain.tick <= tick && (config.trackingMode() == TrackingMode.CUMULATIVE || xpGain.tick > (tick - interval));

		final var xpGained = xpGains.stream()
			.filter(predicate)
			.mapToInt(xpGain -> xpGain.xp)
			.sum();

		if (config.trackingMode() == TrackingMode.CUMULATIVE)
		{
			return xpGained * 6000 / Math.max(MIN_TICKS, tick);
		}

		// 6000 = 1 hour in ticks
		return xpGained * 6000 / interval;
	}

	public List<Point> getXPPerHourHistory(Skill skill, int updateInterval)
	{
		final var history = new ArrayList<Point>();
		final var startTick = Math.max(ticks - secondsToTicks(config.span()), 0);
		for (int t = startTick; t < ticks; t += updateInterval)
		{
			history.add(new Point(t, getXPPerHourAt(skill, t)));
		}

		return history;
	}

	public void tick()
	{
		ticks++;
	}

	public void reset(Skill skill)
	{
		xpGained.put(skill, new ArrayList<>());
	}

	public void reset()
	{
		xpGained.clear();
	}

	@AllArgsConstructor
	static class XPGain
	{
		private Skill skill;
		private int xp;
		private int tick;
	}
}
