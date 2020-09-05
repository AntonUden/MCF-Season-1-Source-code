package xyz.mcfridays.mcf.mcfcore.Utils;

import org.bukkit.Location;
import org.bukkit.WorldBorder;

public class LocationUtils {
	public static boolean isOutsideOfBorder(Location location) {
		WorldBorder border = location.getWorld().getWorldBorder();
		double size = border.getSize() / 2;
		Location center = border.getCenter();
		double x = location.getX() - center.getX(), z = location.getZ() - center.getZ();
		return ((x > size || (-x) > size) || (z > size || (-z) > size));
	}
}