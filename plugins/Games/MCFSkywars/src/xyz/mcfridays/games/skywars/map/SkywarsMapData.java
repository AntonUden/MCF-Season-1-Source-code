package xyz.mcfridays.games.skywars.map;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import xyz.mcfridays.games.skywars.MCFSkywars;
import xyz.mcfridays.games.skywars.events.SkywarsMapLoadedEvent;
import xyz.mcfridays.mcf.mcfcommons.database.DBConnection;
import xyz.mcfridays.mcf.mcfcore.Utils.LocationData;

public class SkywarsMapData {
	private String name;
	private String displayName;
	private String worldName;

	private boolean enabled;

	private ArrayList<Material> breakableBlocks;
	private ArrayList<Material> placeableBlocks;

	private String chestLootTableName;
	private String islandLootTable;
	private String enderChestLootTable;

	private LocationData spectatorLocation;

	private ArrayList<LocationData> startLocations;

	private ArrayList<String> description;

	private NoBuildZone noBuildZone;

	private int noFallDuration;

	public SkywarsMapData(String name, String displayName, String worldName, String chestLootTableName,String islandLootTable, LocationData spectatorLocation, boolean enabled, NoBuildZone noBuildZone) {
		this.name = name;
		this.displayName = displayName;
		this.worldName = worldName;

		this.enabled = enabled;

		this.breakableBlocks = new ArrayList<Material>();
		this.placeableBlocks = new ArrayList<Material>();

		this.chestLootTableName = chestLootTableName;
		this.islandLootTable = islandLootTable;
		this.enderChestLootTable = null;

		this.startLocations = new ArrayList<LocationData>();

		this.description = new ArrayList<String>();

		this.noFallDuration = 0;

		this.spectatorLocation = spectatorLocation;

		this.noBuildZone = noBuildZone;
	}

	public String getName() {
		return name;
	}

	public ArrayList<String> getDescription() {
		return description;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getWorldName() {
		return worldName;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public ArrayList<Material> getPlaceableBlocks() {
		return placeableBlocks;
	}

	public ArrayList<Material> getBreakableBlocks() {
		return breakableBlocks;
	}

	public void setEnderChestLootTable(String enderChestLootTable) {
		this.enderChestLootTable = enderChestLootTable;
	}

	public String getEnderChestLootTable() {
		return enderChestLootTable;
	}
	
	public void setIslandLootTable(String islandLootTable) {
		this.islandLootTable = islandLootTable;
	}
	
	public String getIslandLootTable() {
		return islandLootTable;
	}

	public String getChestLootTableName() {
		return chestLootTableName;
	}

	public LocationData getSpectatorLocation() {
		return spectatorLocation;
	}

	public ArrayList<LocationData> getStartLocations() {
		return startLocations;
	}

	public int getNoFallDuration() {
		return noFallDuration;
	}

	public void setNoFallDuration(int noFallDuration) {
		this.noFallDuration = noFallDuration;
	}

	public SkywarsMap init() throws Exception {
		File worldFile = Paths.get(MCFSkywars.getInstance().getDataFolder().getAbsolutePath() + "/worlds/" + worldName).toFile();
		if (!worldFile.exists()) {
			throw new FileNotFoundException("Could not file world: " + worldFile.getPath());
		}

		File worldContainer = Bukkit.getServer().getWorldContainer();

		File targetFile = Paths.get(worldContainer.getAbsolutePath() + "/" + worldName).toFile();
		if (targetFile.exists()) {
			System.out.println("Replacing old world " + worldName);
			targetFile.delete();
			FileUtils.deleteDirectory(targetFile);
		}

		Thread.sleep(100);

		System.out.println("Adding " + worldName + " to " + worldContainer.getAbsolutePath());
		FileUtils.copyDirectory(worldFile, targetFile);

		Thread.sleep(100);

		World world = Bukkit.getServer().createWorld(new WorldCreator(worldName));

		world.setDifficulty(Difficulty.NORMAL);

		SkywarsMap map = new SkywarsMap(world, this);

		SkywarsMapLoadedEvent event = new SkywarsMapLoadedEvent(map);

		Bukkit.getServer().getPluginManager().callEvent(event);

		return map;
	}

	public int getPlayCount() {
		return getMapPlayCount(this.getName());
	}

	public static int getMapPlayCount(String mapName) {
		int result = 0;
		try {
			String sql = "SELECT play_count FROM map_play_count WHERE map_name = ?";
			PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

			ps.setString(1, mapName);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getInt("play_count");
			}

			rs.close();
			ps.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public boolean increasePlayCount() {
		return increaseMapPlayCount(this.getName());
	}

	public static boolean increaseMapPlayCount(String mapName) {
		boolean success = false;
		try {
			boolean hasMap = false;

			String sql = "SELECT id FROM map_play_count WHERE map_name = ?";
			PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

			ps.setString(1, mapName);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				hasMap = true;
			}

			rs.close();
			ps.close();

			if (hasMap) {
				sql = "UPDATE map_play_count SET play_count = play_count + 1 WHERE map_name = ?";
				PreparedStatement ps2 = DBConnection.connection.prepareStatement(sql);

				ps2.setString(1, mapName);

				if (ps2.execute()) {
					success = true;
				}

				ps2.close();
			} else {
				sql = "INSERT INTO map_play_count (map_name, play_count) VALUES (?, 1)";
				PreparedStatement ps2 = DBConnection.connection.prepareStatement(sql);

				ps2.setString(1, mapName);

				if (ps2.execute()) {
					success = true;
				}

				ps2.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return success;
	}

	public boolean hasNoBuildZone() {
		return noBuildZone != null;
	}

	public NoBuildZone getNoBuildZone() {
		return noBuildZone;
	}
}