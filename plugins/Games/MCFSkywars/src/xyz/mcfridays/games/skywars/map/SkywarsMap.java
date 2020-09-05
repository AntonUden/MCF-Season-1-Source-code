package xyz.mcfridays.games.skywars.map;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.World;

import xyz.mcfridays.mcf.mcfcore.Utils.BlockUtils;
import xyz.mcfridays.mcf.mcfcore.Utils.LocationData;

public class SkywarsMap {
	private World world;

	private Location spectatorLocation;

	private ArrayList<Location> startLocations;

	private SkywarsMapData mapData;

	public SkywarsMap(World world, SkywarsMapData mapData) {
		this.world = world;

		System.out.println(world);

		this.spectatorLocation = mapData.getSpectatorLocation().toLocation(world);

		this.startLocations = new ArrayList<Location>();

		for (LocationData ld : mapData.getStartLocations()) {
			this.startLocations.add(ld.toLocation(world));
		}

		this.mapData = mapData;
	}

	public World getWorld() {
		return world;
	}

	public SkywarsMapData getMapData() {
		return mapData;
	}

	public Location getSpectatorLocation() {
		return spectatorLocation;
	}

	public void setSpectatorLocation(Location spectatorLocation) {
		this.spectatorLocation = spectatorLocation;
	}

	public ArrayList<Location> getStartLocations() {
		return startLocations;
	}

	public int getPlayCount() {
		return this.getMapData().getPlayCount();
	}

	public static int getMapPlayCount(String mapName) {
		return SkywarsMapData.getMapPlayCount(mapName);
	}

	public boolean increasePlayCount() {
		return this.getMapData().increasePlayCount();
	}

	public static boolean increaseMapPlayCount(String mapName) {
		return SkywarsMapData.increaseMapPlayCount(mapName);
	}

	public boolean hasNoBuildZone() {
		return getMapData().hasNoBuildZone();
	}

	public NoBuildZone getNoBouldZone() {
		return getMapData().getNoBuildZone();
	}

	public boolean isInsideNoBuildZone(Location location) {
		if (location.getY() > getMapData().getNoBuildZone().getMaxY()) {
			Location checkLocation = new Location(getWorld(), BlockUtils.blockCenter(getMapData().getNoBuildZone().getCenterX()), location.getY(), BlockUtils.blockCenter(getMapData().getNoBuildZone().getCenterZ()));

			if (location.distance(checkLocation) < getMapData().getNoBuildZone().getRadius()) {
				return true;
			}
		}

		return false;
	}
}