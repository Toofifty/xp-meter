package com.xpmeter;

import com.toofifty.xpmeter.XPMeterPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class XPMeterPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(XPMeterPlugin.class);
		RuneLite.main(args);
	}
}