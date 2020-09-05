package xyz.mcfridays.games.skywars.map;

import org.bukkit.ChatColor;

public class NoBuildZone {
	private int centerX;
	private int centerZ;

	private int radius;

	private int maxY;

	public NoBuildZone(int centerX, int centerZ, int radius, int maxY) {
		this.centerX = centerX;
		this.centerZ = centerZ;

		this.radius = radius;

		this.maxY = maxY;
	}

	public int getCenterX() {
		return centerX;
	}

	public int getCenterZ() {
		return centerZ;
	}

	public int getRadius() {
		return radius;
	}

	public int getMaxY() {
		return maxY;
	}

	public String getMassage() {
		return ChatColor.GOLD + "" + ChatColor.BOLD + "There is a no build zone at y level " + ChatColor.AQUA + ChatColor.BOLD + maxY + ChatColor.GOLD + ChatColor.BOLD + " with a radius of " + ChatColor.AQUA + ChatColor.BOLD + radius + ChatColor.GOLD + ChatColor.BOLD + " blocks from the center of the arena! Building in this arena might cause you to fall and die. We do not take any responsibility for death caused by this system!";
	}
}