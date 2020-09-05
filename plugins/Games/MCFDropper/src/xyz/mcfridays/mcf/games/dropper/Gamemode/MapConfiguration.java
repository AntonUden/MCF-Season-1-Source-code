package xyz.mcfridays.mcf.games.dropper.Gamemode;

import org.bukkit.Location;
import org.bukkit.World;

public class MapConfiguration {
	private Location pos1;
	private Location pos2;

	private Location spawnLocation;
	private Location spectatorLocation;

	private World world;

	public MapConfiguration(World world, Location pos1, Location pos2, Location spawnLocation, Location spectatorLocation) {
		this.world = world;

		this.pos1 = pos1;
		this.pos2 = pos2;
		this.spawnLocation = spawnLocation;
		this.spectatorLocation = spectatorLocation;

		int x1, x2, y1, y2, z1, z2;

		x1 = (this.pos1.getBlockX() < this.pos2.getBlockX() ? this.pos1.getBlockX() : this.pos2.getBlockX());
		x2 = (this.pos1.getBlockX() > this.pos2.getBlockX() ? this.pos1.getBlockX() : this.pos2.getBlockX());

		y1 = (this.pos1.getBlockY() < this.pos2.getBlockY() ? this.pos1.getBlockY() : this.pos2.getBlockY());
		y2 = (this.pos1.getBlockY() > this.pos2.getBlockY() ? this.pos1.getBlockY() : this.pos2.getBlockY());

		z1 = (this.pos1.getBlockZ() < this.pos2.getBlockZ() ? this.pos1.getBlockZ() : this.pos2.getBlockZ());
		z2 = (this.pos1.getBlockZ() > this.pos2.getBlockZ() ? this.pos1.getBlockZ() : this.pos2.getBlockZ());

		this.pos1.setX(x1);
		this.pos2.setX(x2);

		this.pos1.setY(y1);
		this.pos2.setY(y2);

		this.pos1.setZ(z1);
		this.pos2.setZ(z2);
	}

	public World getWorld() {
		return world;
	}

	public Location getPos1() {
		return pos1;
	}

	public Location getPos2() {
		return pos2;
	}

	public Location getSpawnLocation() {
		return spawnLocation;
	}

	public Location getSpectatorLocation() {
		return spectatorLocation;
	}

	public boolean isInsideArena(Location location) {
		if (location.getWorld().getUID() != world.getUID()) {
			return false;
		}

		if (pos1.getBlockX() <= location.getBlockX() && pos2.getBlockX() >= location.getBlockX()) {
			if (pos1.getBlockY() <= location.getBlockY() && pos2.getBlockY() >= location.getBlockY()) {
				if (pos1.getBlockZ() <= location.getBlockZ() && pos2.getBlockZ() >= location.getBlockZ()) {
					return true;
				}
			}
		}

		return false;
	}
}