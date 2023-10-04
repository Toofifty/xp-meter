package com.toofifty.xpmeter;

import com.google.inject.Inject;
import com.google.inject.Provides;
import static com.toofifty.xpmeter.Util.secondsToTicks;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "XP Meter",
	description = "Interactive XP/h Meter"
)
public class XPMeterPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private XPMeterConfig config;

	@Inject
	private XPTracker tracker;

	@Inject
	private XPMeterOverlay overlay;

	private boolean overlayActive = false;

	@Override
	protected void startUp()
	{
		addOverlay();
		updateConfig();
	}

	@Override
	protected void shutDown()
	{
		removeOverlay();
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		tracker.track(event.getSkill(), event.getXp());

		if (!overlayActive)
		{
			addOverlay();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!overlayActive || client.getGameState() != GameState.LOGGED_IN)
		{
			// TODO: track logout
			return;
		}

		tracker.tick();
		if (tracker.getTicks() % secondsToTicks(config.updateInterval()) == 0)
		{
			overlay.update();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("xp-meter"))
		{
			updateConfig();
		}
	}

	/**
	 * Push config values into overlay, so they aren't
	 * read every frame
	 */
	private void updateConfig()
	{
		overlay.setUpdateInterval(secondsToTicks(config.updateInterval()));
		overlay.getChart().setSpan(secondsToTicks(config.span()));
		overlay.getChart().setChartHeight(config.chartHeight());
		overlay.getChart().setShowTimeLabels(config.showTimeLabels());
		overlay.getChart().setShowTimeMarkers(config.showTimeMarkers());
		overlay.getChart().setShowXpLabels(config.showXpLabels());
		overlay.getChart().setShowXpMarkers(config.showXpMarkers());
		overlay.getChart().setShowCurrentRates(config.showCurrentRates());
		overlay.getChart().setShowSkillIcons(config.showSkillIcons());
	}

	private void addOverlay()
	{
		overlayManager.add(overlay);
		overlayActive = true;
	}

	private void removeOverlay()
	{
		overlayManager.remove(overlay);
		overlayActive = false;
	}

	@Provides
	public XPMeterConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(XPMeterConfig.class);
	}
}
