package xyz.mcfridays.mcf.mcfcore.Loot.LootTables.V1;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.JSONArray;
import org.json.JSONObject;

import xyz.mcfridays.mcf.mcfcore.Loot.LootTable;
import xyz.mcfridays.mcf.mcfcore.Loot.LootTableLoader;

@Deprecated
public class LootTableLoaderV1 implements LootTableLoader {
	@Override
	@Deprecated
	public LootTable read(File file) {
		try {
			String data = FileUtils.readFileToString(file, "UTF-8");

			JSONObject jsonData = new JSONObject(data);

			LootTableV1 lootTable = new LootTableV1(jsonData.getString("name"), jsonData.getInt("min_items"), jsonData.getInt("max_items"));

			JSONArray items = jsonData.getJSONArray("items");

			Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "LootTableLoaderV1: reading loot table named " + jsonData.getString("name"));

			for (int i = 0; i < items.length(); i++) {
				JSONObject json = items.getJSONObject(i);
				try {
					ItemStack item = new ItemStack(Material.getMaterial(json.getString("material")));

					if (json.has("amount")) {
						item.setAmount(json.getInt("amount"));
					}

					int chance = 1;

					if (json.has("chance")) {
						chance = json.getInt("chance");
					}

					if (json.has("display_name")) {
						String name = json.getString("display_name");
						ItemMeta meta = item.getItemMeta();

						meta.setDisplayName(name);

						item.setItemMeta(meta);
					}

					lootTable.addItem(item, chance);
				} catch (Exception e) {
					e.printStackTrace();
					Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Failed to load loot table named " + jsonData.getString("name") + " error occured while adding item " + json.getString("material"));

					return null;
				}
			}

			return (LootTable) lootTable;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}