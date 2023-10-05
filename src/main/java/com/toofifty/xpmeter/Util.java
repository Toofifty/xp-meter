package com.toofifty.xpmeter;

public class Util
{
	public static int ticksToSeconds(int ticks)
	{
		return (int) (ticks * 0.6d);
	}

	public static int secondsToTicks(int seconds)
	{
		return (int) ((double) seconds / 0.6d);
	}

	public static String ticksToTime(int ticks)
	{
		final var seconds = ticksToSeconds(ticks);
		final var minutes = (int) (seconds / 60d);
		final var hours = (int) (minutes / 60d);

		return hours > 0
			? String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60)
			: String.format("%02d:%02d", minutes % 60, seconds % 60);
	}

	public static String format(int number)
	{
		if (number < 100000)
		{
			return String.format("%,d", number);
		}

		if (number < 10000000)
		{
			return String.format("%,dK", number / 1000);
		}

		return String.format("%,dM", number / 1000000);
	}

	public static String shortFormat(int number)
	{
		if (number < 1000)
		{
			return "" + number;
		}

		if (number < 1_000_000)
		{
			return String.format("%,dK", number / 1000);
		}

		return String.format("%,dM", number / 1000000);
	}
}
