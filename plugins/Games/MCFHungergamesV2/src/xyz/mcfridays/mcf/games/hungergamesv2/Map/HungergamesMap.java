package xyz.mcfridays.mcf.games.hungergamesv2.Map;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.World;

import xyz.mcfridays.mcf.mcfcore.Utils.BlockUtils;
import xyz.mcfridays.mcf.mcfcore.Utils.LocationData;

public class HungergamesMap {
	private World world;

	private Location spectatorLocation;

	private ArrayList<Location> startLocations;
	private ArrayList<Location> dropLocations;
	private ArrayList<Location> countdownLocations;

	private HungergamesMapData mapData;

	public HungergamesMap(World world, HungergamesMapData mapData) {
		this.world = world;

		System.out.println(world);

		this.spectatorLocation = mapData.getSpectatorLocation().toLocation(world);

		this.startLocations = new ArrayList<Location>();
		this.dropLocations = new ArrayList<Location>();
		this.countdownLocations = new ArrayList<Location>();

		for (LocationData ld : mapData.getStartLocations()) {
			this.startLocations.add(ld.toLocation(world));
		}

		for (LocationData ld : mapData.getDropLocations()) {
			this.dropLocations.add(ld.toLocation(world));
		}

		for (LocationData ld : mapData.getCountdownLocations()) {
			this.countdownLocations.add(ld.toLocation(world));
		}

		this.mapData = mapData;
	}

	public World getWorld() {
		return world;
	}

	public HungergamesMapData getMapData() {
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

	public ArrayList<Location> getDropLocations() {
		return dropLocations;
	}

	public ArrayList<Location> getCountdownLocations() {
		return countdownLocations;
	}

	public int getPlayCount() {
		return this.getMapData().getPlayCount();
	}

	public static int getMapPlayCount(String mapName) {
		return HungergamesMapData.getMapPlayCount(mapName);
	}

	public boolean increasePlayCount() {
		return this.getMapData().increasePlayCount();
	}

	public static boolean increaseMapPlayCount(String mapName) {
		return HungergamesMapData.increaseMapPlayCount(mapName);
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