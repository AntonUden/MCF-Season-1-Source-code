package xyz.mcfridays.games.skywars.map;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.json.JSONArray;
import org.json.JSONObject;

import xyz.mcfridays.mcf.mcfcore.Utils.LocationData;

public class MapLoader {
	public static ArrayList<SkywarsMapData> load(File file) throws IOException {
		ArrayList<SkywarsMapData> result = new ArrayList<SkywarsMapData>();

		for (File fileEntry : file.listFiles()) {
			String data = FileUtils.readFileToString(fileEntry, "UTF-8");

			JSONObject jsonData = new JSONObject(data);

			String name = jsonData.getString("arena_name");
			String displayName = jsonData.getString("display_name");
			String worldName = jsonData.getString("world");

			String chestLootTableName = jsonData.getString("chest_loot_table");
			String islandLootTableName = jsonData.getString("island_loot_table");

			LocationData spectatorLocation = new LocationData(jsonData.getJSONObject("spectator_location"));

			spectatorLocation.center();

			NoBuildZone noBuildZone = null;
			if (jsonData.has("no_build_zone")) {
				JSONObject noBuildZoneData = jsonData.getJSONObject("no_build_zone");
				noBuildZone = new NoBuildZone(noBuildZoneData.getInt("center_x"), noBuildZoneData.getInt("center_z"), noBuildZoneData.getInt("radius"), noBuildZoneData.getInt("max_y"));
			}

			SkywarsMapData mapData = new SkywarsMapData(name, displayName, worldName, chestLootTableName, islandLootTableName, spectatorLocation, jsonData.getBoolean("enabled"), noBuildZone);

			if (jsonData.has("ender_chest_loot_table")) {
				mapData.setEnderChestLootTable(jsonData.getString("ender_chest_loot_table"));
			}

			/*
			 * JSONArray placeableBlocks = jsonData.getJSONArray("placeable_blocks"); for
			 * (int i = 0; i < placeableBlocks.length(); i++) { try { Material material =
			 * Material.getMaterial(placeableBlocks.getString(i));
			 * 
			 * mapData.getPlaceableBlocks().add(material); } catch (Exception e) {
			 * Bukkit.getConsoleSender().sendMessage(ChatColor.RED +
			 * "Failed to add material " + placeableBlocks.getString(i) +
			 * " to placeableBlocks"); e.printStackTrace(); } }
			 */

			JSONArray breakableBlocks = jsonData.getJSONArray("breakable_blocks");
			for (int i = 0; i < breakableBlocks.length(); i++) {
				try {
					Material material = Material.getMaterial(breakableBlocks.getString(i));

					mapData.getBreakableBlocks().add(material);
				} catch (Exception e) {
					Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Failed to add material " + breakableBlocks.getString(i) + " to breakableBlocks");
					e.printStackTrace();
				}
			}

			JSONArray spawns = jsonData.getJSONArray("spawns");
			for (int i = 0; i < spawns.length(); i++) {
				JSONObject spawn = spawns.getJSONObject(i);

				LocationData ld = new LocationData(spawn);

				ld.center();

				mapData.getStartLocations().add(ld);
			}

			if (jsonData.has("description")) {
				JSONArray description = jsonData.getJSONArray("description");
				for (int i = 0; i < description.length(); i++) {
					mapData.getDescription().add(description.getString(i));
				}
			}

			if (jsonData.has("no_fall_duration")) {
				System.out.println("No fall duration: " + jsonData.getInt("no_fall_duration"));
				mapData.setNoFallDuration(jsonData.getInt("no_fall_duration"));
			}

			result.add(mapData);
		}

		return result;
	}
}