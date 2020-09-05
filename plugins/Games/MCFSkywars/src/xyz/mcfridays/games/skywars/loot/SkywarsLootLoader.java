package xyz.mcfridays.games.skywars.loot;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.JSONArray;
import org.json.JSONObject;

import xyz.mcfridays.mcf.mcfcore.Loot.LootTable;
import xyz.mcfridays.mcf.mcfcore.Loot.LootTableLoader;

public class SkywarsLootLoader implements LootTableLoader {
	@Override
	public LootTable read(File file) {
		try {
			String data = FileUtils.readFileToString(file, "UTF-8");

			JSONObject jsonData = new JSONObject(data);

			Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "LootTableLoaderV2: reading loot table named " + jsonData.getString("name"));

			String lootTableName = jsonData.getString("name");
			String lootTableDisplayName;

			if (jsonData.has("display_name")) {
				lootTableDisplayName = jsonData.getString("display_name");
			} else {
				lootTableDisplayName = lootTableName;
			}

			JSONObject groups = jsonData.getJSONObject("groups");

			ArrayList<SkywarsLootGroup> lootGroups = new ArrayList<SkywarsLootGroup>();

			for (String key : groups.keySet()) {
				JSONObject group = groups.getJSONObject(key);

				ArrayList<SkywarsLootEntry> entries = new ArrayList<SkywarsLootEntry>();

				JSONArray items = group.getJSONArray("items");

				for (int i = 0; i < items.length(); i++) {
					SkywarsLootEntry entry = readLootEntry(items.getJSONObject(i));

					for (int j = 0; j < entry.getChance(); j++) {
						entries.add(entry);
					}
				}

				Collections.shuffle(entries);

				SkywarsLootGroup lootGroup = new SkywarsLootGroup(group.getInt("min_items"), group.getInt("max_items"), entries);

				lootGroups.add(lootGroup);
			}

			SkywarsIslandLootTable lootTable = new SkywarsIslandLootTable(lootTableName, lootTableDisplayName, lootGroups);

			return (LootTable) lootTable;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static PotionEffect readPotionEffect(JSONObject potion) {
		PotionEffectType type = PotionEffectType.getByName(potion.getString("type"));
		int duration = potion.getInt("duration");

		int amplifier = 0;
		if (potion.has("amplifier")) {
			amplifier = potion.getInt("amplifier");
		}

		boolean ambient = false;
		if (potion.has("ambient")) {
			ambient = potion.getBoolean("ambient");
		}

		boolean particles = true;
		if (potion.has("particles")) {
			particles = potion.getBoolean("particles");
		}

		return new PotionEffect(type, duration, amplifier, ambient, particles);
	}

	private static SkywarsLootEntry readLootEntry(JSONObject itemJson) {
		ItemStack item;

		if (itemJson.has("data")) {
			short itemData = (short) itemJson.getInt("data");
			item = new ItemStack(Material.getMaterial(itemJson.getString("material")), 1, itemData);
		} else {
			item = new ItemStack(Material.getMaterial(itemJson.getString("material")), 1);
		}

		if (itemJson.has("display_name")) {
			String displayName = itemJson.getString("display_name");

			ItemMeta meta = item.getItemMeta();

			meta.setDisplayName(displayName);

			item.setItemMeta(meta);
		}

		int minAmount = 1;
		int maxAmount = 1;

		if (itemJson.has("potion_data")) {
			PotionMeta meta = (PotionMeta) item.getItemMeta();

			JSONObject potionData = itemJson.getJSONObject("potion_data");

			if (potionData.has("main_effect")) {
				JSONObject mainEffect = potionData.getJSONObject("main_effect");
				PotionEffectType type = PotionEffectType.getByName(mainEffect.getString("type"));
				meta.setMainEffect(type);
			}

			if (potionData.has("custom_effects")) {
				JSONArray customEffects = potionData.getJSONArray("custom_effects");
				for (int i = 0; i < customEffects.length(); i++) {
					meta.addCustomEffect(readPotionEffect(customEffects.getJSONObject(i)), true);
				}
			}
			item.setItemMeta(meta);
		}

		if (itemJson.has("amount")) {
			minAmount = itemJson.getInt("amount");
			maxAmount = minAmount;
		} else {
			if (itemJson.has("min_amount")) {
				minAmount = itemJson.getInt("min_amount");
			}

			if (itemJson.has("max_amount")) {
				maxAmount = itemJson.getInt("max_amount");
				if (minAmount > maxAmount) {
					maxAmount = minAmount;
				}
			} else {
				maxAmount = minAmount;
			}
		}

		if (itemJson.has("enchantments")) {
			JSONObject enchantments = itemJson.getJSONObject("enchantments");

			for (String enchant : enchantments.keySet()) {
				int level = enchantments.getInt(enchant);

				item.addUnsafeEnchantment(Enchantment.getByName(enchant), level);
			}
		}

		int chance = 1;

		if (itemJson.has("chance")) {
			chance = itemJson.getInt("chance");
		}

		if (itemJson.has("display_name")) {
			String name = itemJson.getString("display_name");
			ItemMeta meta = item.getItemMeta();

			meta.setDisplayName(name);

			item.setItemMeta(meta);
		}

		ArrayList<SkywarsLootEntry> extraItems = null;

		if (itemJson.has("extra_items")) {
			extraItems = new ArrayList<SkywarsLootEntry>();
			JSONArray extraItemsJson = itemJson.getJSONArray("extra_items");

			for (int i = 0; i < extraItemsJson.length(); i++) {
				JSONObject extraItem = extraItemsJson.getJSONObject(i);

				extraItems.add(readLootEntry(extraItem));
			}
		}

		SkywarsLootEntry entry = new SkywarsLootEntry(item, chance, minAmount, maxAmount, extraItems);

		return entry;
	}
}