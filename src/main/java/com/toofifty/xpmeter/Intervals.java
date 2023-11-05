package com.toofifty.xpmeter;

import static com.toofifty.xpmeter.Util.secondsToTicks;
import static com.toofifty.xpmeter.Util.ticksToSeconds;
import java.util.ArrayList;
import java.util.List;

public class Intervals
{
	/**
	 * Generate XP intervals based on the maximum XP
	 * displayed in the chart.
	 * Aims to have 1-3 intervals displayed at one time.
	 */
	public static List<Integer> getXpIntervals(int max)
	{
		var interval = 500_000;
		if (max < 10_000)
		{
			interval = 5_000;
		}
		else if (max < 25_000)
		{
			interval = 10_000;
		}
		else if (max < 100_000)
		{
			interval = 25_000;
		}
		else if (max < 200_000)
		{
			interval = 50_000;
		}
		else if (max < 500_000)
		{
			interval = 100_000;
		}
		else if (max < 1_000_000)
		{
			interval = 250_000;
		}

		final var xpIntervals = new ArrayList<Integer>();
		for (var i = 0; i < max; i += interval)
		{
			xpIntervals.add(i);
		}
		return xpIntervals;
	}

	/**
	 * Generate time intervals based on the time span
	 * displayed in the chart.
	 * Aims to have 4-8 intervals displayed at one time.
	 */
	public static List<Integer> getTimeIntervals(int startTick, int endTick)
	{
		final var span = ticksToSeconds(endTick - startTick);

		var interval = secondsToTicks(3600);
		if (span < 120) // 2min
		{
			interval = secondsToTicks(15);
		}
		else if (span < 240) // 4min
		{
			interval = secondsToTicks(30);
		}
		else if (span < 480) // 8min
		{
			interval = secondsToTicks(60);
		}
		else if (span < 1800) // 30min
		{
			interval = secondsToTicks(300);
		}
		else if (span < 3600) // 1h
		{
			interval = secondsToTicks(900);
		}
		else if (span < 7200) // 2h
		{
			interval = secondsToTicks(1800);
		}

		final var timeIntervals = new ArrayList<Integer>();

		// always add span start time
		timeIntervals.add(startTick);

		// generate all time intervals up to current tick
		// only keep if > spanStart
		for (var i = 0; i < endTick; i += interval)
		{
			if (i > startTick) timeIntervals.add(i);
		}

		// always add current time
		timeIntervals.add(endTick);

		return timeIntervals;
	}
}
