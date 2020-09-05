package xyz.mcfridays.mcf.games.hungergamesv2.utils;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.bukkit.ChatColor;

import com.gmail.filoghost.holographicdisplays.api.Hologram;

public class BigHologramText {
	public static boolean setText(ArrayList<Hologram> holograms, String text, int width, int height, ChatColor color, ChatColor backgroundColor) {
		for (Hologram hologram : holograms) {
			hologram.clearLines();
		}
		String block = new String("\u2588");

		try {
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics g = image.getGraphics();
			g.setFont(new Font("Dialog", Font.PLAIN, height));
			Graphics2D graphics = (Graphics2D) g;
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			drawCenteredString(g, text, new Rectangle(width, height - 2), g.getFont());

			for (int y = 0; y < height; y++) {
				StringBuilder sb = new StringBuilder();
				for (int x = 0; x < width; x++) {
					sb.append(image.getRGB(x, y) == -16777216 ? backgroundColor + block : image.getRGB(x, y) == -1 ? color + block : color + block);
				}
				if (sb.toString().trim().isEmpty()) {
					continue;
				}

				for (Hologram hologram : holograms) {
					hologram.appendTextLine(color + sb.toString());
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Draw a String centered in the middle of a Rectangle.
	 *
	 * @param g    The Graphics instance.
	 * @param text The String to draw.
	 * @param rect The Rectangle to center the text in.
	 */
	private static void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
		// Get the FontMetrics
		FontMetrics metrics = g.getFontMetrics(font);
		// Determine the X coordinate for the text
		int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
		// Determine the Y coordinate for the text (note we add the ascent, as in java
		// 2d 0 is top of the screen)
		int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
		// Set the font
		g.setFont(font);
		// Draw the String
		g.drawString(text, x, y);
	}
}