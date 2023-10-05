package com.toofifty.xpmeter;

import com.google.inject.Inject;
import com.google.inject.Provides;
import static com.toofifty.xpmeter.Util.secondsToTicks;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.KeyCode;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptCallbackEvent;
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
	@Inject private Client client;
	@Inject private OverlayManager overlayManager;
	@Inject private ConfigManager configManager;

	@Inject private XPMeterConfig config;
	@Inject private XPTracker tracker;
	@Inject private XPMeterOverlay overlay;

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		syncConfig();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		tracker.track(event.getSkill(), event.getXp());
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (tracker.isPaused())
		{
			return;
		}

		tracker.tick();
		// only update histories every Nth tick, otherwise the plotted
		// lines start to wobble (as the current tick changes, it calculates each
		// history point off-phase which means the values are different every tick)
		if (tracker.getCurrentTick() % secondsToTicks(config.updateInterval()) == 0)
		{
			overlay.update();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN
			&& event.getGameState() != GameState.LOADING
			&& tracker.isTracking())
		{
			tracker.trackLogout();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals(XPMeterConfig.GROUP_NAME))
		{
			syncConfig();
		}
	}

	@Subscribe
	public void onScriptCallbackEvent(ScriptCallbackEvent event)
	{
		if (!config.scrollZoom())
		{
			return;
		}

		if ("scrollWheelZoomIncrement".equals(event.getEventName())
			&& overlay.isMouseOver()
			&& client.isKeyPressed(KeyCode.KC_SHIFT))
		{
			final var intStack = client.getIntStack();

			updateScroll(intStack[1]);

			// this stops the client scroll - don't ask how
			intStack[2] = -intStack[1];
		}
	}

	/**
	 * Push config values into overlay, so they aren't
	 * read every frame
	 */
	private void syncConfig()
	{
		final var shouldRecalculate = overlay.getChart().getSpan() != config.span();

		overlay.setUpdateInterval(secondsToTicks(config.updateInterval()));
		overlay.getChart().setSpan(secondsToTicks(config.span()));
		overlay.getChart().setChartHeight(config.chartHeight());
		overlay.getChart().setShowTimeLabels(config.showTimeLabels());
		overlay.getChart().setShowTimeMarkers(config.showTimeMarkers());
		overlay.getChart().setShowXpLabels(config.showXpLabels());
		overlay.getChart().setShowXpMarkers(config.showXpMarkers());
		overlay.getChart().setShowCurrentRates(config.showCurrentRates());
		overlay.getChart().setShowSkillIcons(config.showSkillIcons());

		if (shouldRecalculate)
		{
			// re-calculate to update span
			overlay.update();
		}
	}

	private void updateScroll(int dir)
	{
		final var span = (int) (config.span() * Math.pow(1.2, dir));

		configManager.setConfiguration(
			XPMeterConfig.GROUP_NAME,
			"span",
			Math.max(span, 10)
		);
	}

	@Provides
	public XPMeterConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(XPMeterConfig.class);
	}
}
