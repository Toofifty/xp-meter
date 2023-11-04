package com.toofifty.xpmeter;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
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

	// data
	@Expose private final Map<Skill, List<XPGain>> xpGained = new HashMap<>();
	@Expose private final Map<Skill, Integer> lastXp = new HashMap<>();
	@Expose private final Map<Skill, Integer> startTicks = new HashMap<>();

	@Expose @Getter private int currentTick;
	@Expose @Getter private boolean paused;
	@Expose @Getter private final Set<Integer> pauses = new HashSet<>();
	@Expose @Getter private final Set<Integer> logouts = new HashSet<>();

	// transient
	private final Map<Integer, Integer> cache = new HashMap<>();
	@Getter private List<Skill> sortedSkills;
	@Getter private int maxXpPerHour;

	private int cacheHits = 0;
	private int cacheMisses = 0;

	@Getter private Performance performance;

	public void track(Skill skill, int xp)
	{
		if (paused || xp == 0)
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

		if (!cache.containsKey(hash) || config.disableCache())
		{
			final var xpGains = xpGained.getOrDefault(skill, List.of());
			final var interval = secondsToTicks(windowInterval);
			final var elapsed = tick - startTicks.getOrDefault(skill, 0);

			final var xpGained = xpGains.stream()
				.filter((XPGain xpGain) -> xpGain.tick <= tick && (
					trackingMode == TrackingMode.CUMULATIVE || xpGain.tick > (tick - interval)
				))
				.mapToInt(xpGain -> xpGain.xp)
				.sum();

			if (trackingMode == TrackingMode.CUMULATIVE)
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

	public String export()
	{
		return new GsonBuilder()
			.excludeFieldsWithoutExposeAnnotation()
			.create()
			.toJson(this);
	}

	public void restore(String json)
	{
		reset();
		var data = new JsonParser().parse(json).getAsJsonObject();

		for (var entry : data.get("xpGained").getAsJsonObject().entrySet())
		{
			var jsonGains = entry.getValue().getAsJsonArray();
			var gains = new ArrayList<XPGain>();
			for (var jsonGain : jsonGains)
			{
				var gain = jsonGain.getAsJsonObject();
				gains.add(new XPGain(gain.get("tick").getAsInt(), gain.get("xp").getAsInt()));
			}
			xpGained.put(Skill.valueOf(entry.getKey()), gains);
		}

		for (var entry : data.get("lastXp").getAsJsonObject().entrySet())
		{
			lastXp.put(Skill.valueOf(entry.getKey()), entry.getValue().getAsInt());
		}

		for (var entry : data.get("startTicks").getAsJsonObject().entrySet())
		{
			startTicks.put(Skill.valueOf(entry.getKey()), entry.getValue().getAsInt());
		}

		currentTick = data.get("currentTick").getAsInt();

		paused = data.get("paused").getAsBoolean();

		for (var logout : data.get("pauses").getAsJsonArray())
		{
			pauses.add(logout.getAsInt());
		}

		for (var logout : data.get("logouts").getAsJsonArray())
		{
			logouts.add(logout.getAsInt());
		}
	}

	@AllArgsConstructor
	static class XPGain
	{
		@Expose private int tick;
		@Expose private int xp;
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
