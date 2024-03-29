package com.toofifty.xpmeter;

import com.google.inject.Inject;
import com.google.inject.Provides;
import static com.toofifty.xpmeter.Util.secondsToTicks;
import java.util.HashSet;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.KeyCode;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.OverlayMenuClicked;
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
	@Inject private RuneLiteConfig runeLiteConfig;

	@Inject private XPMeterConfig config;
	@Inject private XPTracker tracker;
	@Inject private XPMeterOverlay overlay;

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		syncConfig(null);
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
		if (tracker.getCurrentTick() % secondsToTicks(config.resolution()) == 0)
		{
			update();
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
			syncConfig(event.getKey());

			if (event.getKey().equals("enableDataMenuOptions"))
			{
				overlay.updateMenuEntries(config.enableDataMenuOptions());
			}
		}

		// listen to changes in runelite config's overlay color,
		// since setting the overlay color to "null" causes it to
		// become out of sync with the runelite setting
		if (event.getGroup().equals(RuneLiteConfig.GROUP_NAME)
			&& event.getKey().equals("overlayBackgroundColor"))
		{
			syncConfig(null);
		}
	}

	@Subscribe
	public void onOverlayMenuClicked(OverlayMenuClicked event)
	{
		if (event.getOverlay() == overlay)
		{
			if (event.getEntry().getOption().equals("Export data"))
			{
				configManager.setConfiguration(
					XPMeterConfig.GROUP_NAME,
					"sessionData",
					tracker.export()
				);
			}
			else if (event.getEntry().getOption().equals("Restore data"))
			{
				tracker.restore(config.sessionData());
			}
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
	private void syncConfig(String changedKey)
	{
		final var chart = overlay.getChart();

		chart.setSpan(secondsToTicks(config.span()));
		chart.setChartHeight(config.chartHeight());
		chart.setShowTimeLabels(config.showTimeLabels());
		chart.setShowTimeMarkers(config.showTimeMarkers());
		chart.setShowXpLabels(config.showXpLabels());
		chart.setShowXpMarkers(config.showXpMarkers());
		chart.setShowCurrentRates(config.showCurrentRates());
		chart.setStackCurrentRates(config.stackCurrentRates());
		chart.setShowSkillIcons(config.showSkillIcons());
		chart.setLongFormatNumbers(config.longFormatNumbers());
		chart.setShowPerformance(config.showPerformance());
		chart.setShowHoverTooltips(config.showHoverTooltips());
		chart.setDimNonHoveredSkills(config.dimNonHoveredSkills());
		chart.setShowAllHovers(config.showAllHovers());

		overlay.setBackgroundColor(
			config.theme().overlayBackground != null
				? config.theme().overlayBackground
				: runeLiteConfig.overlayBackgroundColor()
		);
		chart.setTheme(config.theme());

		if ("windowInterval".equals(changedKey)
			|| "trackingMode".equals(changedKey))
		{
			tracker.clearCache();
		}

		if (changedKey == null || changedKey.startsWith("track"))
		{
			final var enabledSkills = new HashSet<Skill>();
			for (var skill : Skill.values())
			{
				final var enabled = configManager.<Boolean>getConfiguration(
					XPMeterConfig.GROUP_NAME,
					"track" + skill.getName(),
					boolean.class
				);
				if (enabled)
				{
					enabledSkills.add(skill);
				}
			}
			tracker.setEnabledSkills(enabledSkills);

			if (changedKey != null)
			{
				// immediately show changes in skill filtering in
				// the chart
				update();
			}
		}

		if ("span".equals(changedKey))
		{
			// stops line segments overflowing the chart
			// before the next update tick
			update();
		}
	}

	private void update()
	{
		final var chart = overlay.getChart();

		chart.setSkillXpHistories(tracker.getAggregate(chart.getBounds().width));
		chart.setSortedSkills(tracker.getSortedSkills());
		chart.setMaxXpPerHour(tracker.getMaxXpPerHour());
		chart.setCurrentTick(tracker.getCurrentTick());
		chart.setPauses(tracker.getPauses());
		chart.setLogouts(tracker.getLogouts());
		chart.setPerformance(tracker.getPerformance());
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
