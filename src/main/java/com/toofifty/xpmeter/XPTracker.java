package com.toofifty.xpmeter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import static com.toofifty.xpmeter.Util.secondsToTicks;
import java.awt.Point;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Skill;

@Slf4j
@Singleton
public class XPTracker
{
	private static final int ONE_MINUTE = 100;
	private static final int ONE_HOUR = 60 * ONE_MINUTE;

	@Inject private XPMeterConfig config;

	private final Map<Skill, List<XPGain>> xpGained = new HashMap<>();
	private final Map<Skill, Integer> lastXp = new HashMap<>();
	private final Map<Skill, Integer> startTicks = new HashMap<>();

	private final Map<Integer, Integer> cache = new HashMap<>();

	@Getter private int currentTick;
	@Getter private boolean paused;
	@Getter private final Set<Integer> pauses = new HashSet<>();
	@Getter private final Set<Integer> logouts = new HashSet<>();
	@Getter private List<Skill> sortedSkills;
	@Getter private int maxXpPerHour;

	private int cacheHits = 0;
	private int cacheMisses = 0;

	@Getter private Performance performance;

	public void track(Skill skill, int xp)
	{
		if (paused)
		{
			return;
		}

		if (lastXp.containsKey(skill))
		{
			if (!xpGained.containsKey(skill))
			{
				xpGained.put(skill, new ArrayList<>());
				startTicks.put(skill, currentTick);
			}

			final var diff = xp - lastXp.get(skill);
			xpGained.get(skill).add(new XPGain(currentTick, diff));
		}

		lastXp.put(skill, xp);
	}

	public boolean isTracking()
	{
		return !lastXp.isEmpty();
	}

	public Set<Skill> getTrackedSkills()
	{
		return xpGained.keySet();
	}

	public int getXPPerHourAt(Skill skill, int tick, int windowInterval, TrackingMode trackingMode)
	{
		final var hash = Objects.hash(skill, tick, windowInterval, trackingMode);

		if (!cache.containsKey(hash) || !config.useCache())
		{
			final var xpGains = xpGained.getOrDefault(skill, List.of());
			final var interval = secondsToTicks(config.windowInterval());
			final var elapsed = tick - startTicks.getOrDefault(skill, 0);

			final var xpGained = xpGains.stream()
				.filter((XPGain xpGain) -> xpGain.tick <= tick && (
					config.trackingMode() == TrackingMode.CUMULATIVE || xpGain.tick > (tick - interval)
				))
				.mapToInt(xpGain -> xpGain.xp)
				.sum();

			if (config.trackingMode() == TrackingMode.CUMULATIVE)
			{
				cache.put(hash, xpGained * ONE_HOUR / Math.max(ONE_MINUTE, elapsed));
			}
			else
			{
				cache.put(hash, xpGained * ONE_HOUR / interval);
			}

			cacheMisses++;
		}
		else
		{
			cacheHits++;
		}

		return cache.get(hash);
	}

	public List<Point> getHistory(Skill skill, int resolution)
	{
		final var history = new ArrayList<Point>();
		final var startTick = Math.max(currentTick - secondsToTicks(config.span()), 0);
		final var windowInterval = config.windowInterval();
		final var trackingMode = config.trackingMode();
		for (int t = startTick; t < currentTick; t += resolution)
		{
			history.add(new Point(t, getXPPerHourAt(skill, t, windowInterval, trackingMode)));
		}

		return history;
	}

	public Map<Skill, List<Point>> getAggregate()
	{
		cacheHits = 0;
		cacheMisses = 0;
		final var start = Instant.now();

		maxXpPerHour = 0;
		final var resolution = config.resolution();
		final var skillHistories = new HashMap<Skill, List<Point>>();
		final var skillCurrentRates = new HashMap<Skill, Integer>();
		for (var skill : getTrackedSkills())
		{
			final var history = getHistory(
				skill,
				Math.max(resolution, 1)
			);
			skillHistories.put(skill, history);

			final var last = history.get(history.size() - 1);
			skillCurrentRates.put(skill, last == null ? 0 : last.y);

			for (var point : history)
			{
				if (point.y > maxXpPerHour)
				{
					maxXpPerHour = point.y;
				}
			}
		}

		sortedSkills = skillCurrentRates.entrySet().stream()
			.sorted(Comparator.comparingInt(Map.Entry::getValue))
			.map(Map.Entry::getKey)
			.collect(Collectors.toList());

		final var time = Duration.between(start, Instant.now()).toMillis();
		performance = new Performance(time, cache.size(), cacheHits, cacheMisses);

		return skillHistories;
	}

	public void tick()
	{
		currentTick++;
	}

	public void reset()
	{
		xpGained.clear();
		startTicks.clear();
		cache.clear();
		pauses.clear();
		logouts.clear();
		currentTick = 0;
	}

	public void clearCache()
	{
		cache.clear();
	}

	public void pause()
	{
		paused = true;
		pauses.add(currentTick);
	}

	public void unpause()
	{
		paused = false;
	}

	public void trackLogout()
	{
		logouts.add(currentTick);
	}

	@AllArgsConstructor
	static class XPGain
	{
		private int tick;
		private int xp;
	}

	@Getter
	@AllArgsConstructor
	static class Performance
	{
		private long computeTime;
		private int cacheSize;
		private int cacheHits;
		private int cacheMisses;
	}
}
