package com.toofifty.xpmeter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;

/**
 * Generic base methods for drawing the XPChart and handling layout
 */
public abstract class XPChartBase implements LayoutableRenderableEntity
{
	@Getter @Setter
	private Dimension preferredSize;

	@Getter @Setter
	private Point preferredLocation;

	protected Graphics2D graphics;
	protected FontMetrics fontMetrics;
	protected int fontHeight;
	protected Rectangle size;
	protected Point offset;

	@Setter protected int heightScale;
	@Setter protected int widthMax;
	@Setter protected int widthMin;

	@Override
	public Dimension render(Graphics2D g)
	{
		graphics = g;
		fontMetrics = g.getFontMetrics();
		fontHeight = fontMetrics.getHeight();

		final var marginLeft = calculateLeftMargin();
		final var marginRight = calculateRightMargin();
		final var marginTop = calculateTopMargin();
		final var marginBottom = calculateBottomMargin();

		size = getBounds();
		final var loc = getPreferredLocation();
		offset = new Point(loc.x + marginLeft, loc.y + marginTop);

		return new Dimension(
			marginLeft + size.width + marginRight,
			marginTop + size.height + marginBottom
		);
	}

	protected void setColor(Color color)
	{
		graphics.setColor(color);
	}

	protected void drawText(String text, int x, int y, boolean shadow)
	{
		if (shadow)
		{
			final var color = graphics.getColor();
			graphics.setColor(Color.BLACK);
			graphics.drawString(text, offset.x + x + 1, offset.y + y + 1);
			graphics.setColor(color);
		}

		graphics.drawString(text, offset.x + x, offset.y + y);
	}

	protected void drawText(String text, int x, int y)
	{
		drawText(text, x, y, false);
	}

	protected void drawLine(int x1, int y1, int x2, int y2, boolean shadow)
	{
		if (shadow)
		{
			final var color = graphics.getColor();
			graphics.setColor(Color.BLACK);
			graphics.drawLine(
				offset.x + x1 + 1, offset.y + y1 + 1,
				offset.x + x2 + 1, offset.y + y2 + 1
			);
			graphics.setColor(color);
		}

		graphics.drawLine(
			offset.x + x1, offset.y + y1,
			offset.x + x2, offset.y + y2
		);
	}

	protected void drawLine(int x1, int y1, int x2, int y2)
	{
		drawLine(x1, y1, x2, y2, false);
	}

	protected void fillRect(int x, int y, int w, int h)
	{
		graphics.fillRect(offset.x + x, offset.y + y, w, h);
	}

	protected void fillRoundRect(int x, int y, int w, int h, int arc)
	{
		graphics.fillRoundRect(offset.x + x, offset.y + y, w, h, arc, arc);
	}

	protected void drawVMarker(int x)
	{
		drawLine(x, 0, x, size.height);
	}

	protected void drawHMarker(int y)
	{
		drawLine(0, y, size.width, y);
	}

	/**
	 * Map an absolute X coordinate (i.e. tick number) to a
	 * relative X coordinate in the chart
	 */
	protected int mapX(int x)
	{
		return (x - widthMin) * size.width / (widthMax - widthMin);
	}

	/**
	 * Map a Y coordinate (i.e. xp rate) to a Y
	 * coordinate in the chart.
	 * Y coordinates are flipped here, as the rendering
	 * Y plane is upside down.
	 * Using topMargin will re-scale the value so there's
	 * an empty space of 1/2 fontHeight at the top.
	 */
	protected int mapY(int y, boolean topMargin)
	{
		var height = size.height;
		var yOffset = 0;

		if (topMargin)
		{
			height -= fontHeight / 2;
			yOffset += fontHeight / 2;
		}

		return yOffset + height - y * height / heightScale;
	}

	protected int width(String text)
	{
		return fontMetrics.stringWidth(text);
	}

	abstract int calculateLeftMargin();

	abstract int calculateTopMargin();

	abstract int calculateRightMargin();

	abstract int calculateBottomMargin();
}
