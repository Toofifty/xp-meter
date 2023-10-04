package com.toofifty.xpmeter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TrackingMode
{
	CUMULATIVE("Cumulative"),
	SLIDING_WINDOW("Sliding window");

	private final String name;
}
