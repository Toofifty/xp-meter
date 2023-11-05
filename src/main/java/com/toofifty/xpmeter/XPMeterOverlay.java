package com.toofifty.xpmeter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

@Singleton
public class XPMeterOverlay extends OverlayPanel
{
	public static final int DEFAULT_WIDTH = 180;
	public static final int DEFAULT_HEIGHT = 60;

	@Inject private XPTracker tracker;
	@Inject private Client client;

	@Getter private final XPChart chart = new XPChart();
	@Getter private boolean isMouseOver = false;

	@Setter private Theme theme = Theme.RUNELITE;

	@Inject
	private XPMeterOverlay(SkillIconManager skillIconManager, XPMeterConfig config)
	{
		if (getPreferredSize() == null)
		{
			setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		}

		setPosition(OverlayPosition.TOP_LEFT);
		setMinimumSize(XPChart.MIN_WIDTH);
		setResizable(true);
		setResettable(true);

		chart.setSkillIconManager(skillIconManager);

		addMenuEntry(
			MenuAction.RUNELITE_OVERLAY,
			"Reset",
			"XP Meter",
			e -> tracker.reset()
		);

		updateMenuEntries(config.enableDataMenuOptions());
	}

	public void updateMenuEntries(boolean enableDataMenuOptions)
	{
		removeMenuEntry(MenuAction.RUNELITE_OVERLAY, "Pause", "XP Meter");
		removeMenuEntry(MenuAction.RUNELITE_OVERLAY, "Unpause", "XP Meter");

		addMenuEntry(
			MenuAction.RUNELITE_OVERLAY,
			// tracker will be null on first call
			tracker != null && tracker.isPaused() ? "Unpause" : "Pause",
			"XP Meter",
			e -> {
				if (tracker.isPaused())
				{
					tracker.unpause();
				}
				else
				{
					tracker.pause();
				}

				updateMenuEntries(enableDataMenuOptions);
			}
		);

		removeMenuEntry(MenuAction.RUNELITE_OVERLAY, "Export data", "XP Meter");
		removeMenuEntry(MenuAction.RUNELITE_OVERLAY, "Restore data", "XP Meter");

		if (enableDataMenuOptions)
		{
			addMenuEntry(MenuAction.RUNELITE_OVERLAY, "Export data", "XP Meter");
			addMenuEntry(MenuAction.RUNELITE_OVERLAY, "Restore data", "XP Meter");
		}
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (chart.hasData())
		{
			panelComponent.getChildren().add(chart);
		}
		else
		{
			panelComponent.getChildren().add(
				LineComponent.builder()
					.left("XP Meter inactive")
					.build()
			);
		}

		setPreferredColor(theme.overlayBackground);

		// onMouseOver() is called each frame immediately after render,
		// so we can flick off the value for it to be re-set immediately
		// (if mouse is hovering)
		isMouseOver = false;

		return super.render(graphics);
	}

	@Override
	public void onMouseOver()
	{
		isMouseOver = true;

		final var bounds = getBounds();
		final var canvas = client.getMouseCanvasPosition();
		final var mouse = new Point(
			canvas.getX() - bounds.x,
			canvas.getY() - bounds.y
		);

		chart.setMouse(mouse);
	}
}
