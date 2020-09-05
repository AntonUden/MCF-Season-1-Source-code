package xyz.mcfridays.mcf.mcfcore.Loot.LootTables.V2;

import java.io.File;

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

@Deprecated
public class LootTableLoaderV2R1 implements LootTableLoader {
	@Override
	public LootTable read(File file) {
		try {
			String data = FileUtils.readFileToString(file, "UTF-8");

			JSONObject jsonData = new JSONObject(data);

			String lootTableName = jsonData.getString("name");
			String lootTableDisplayName;

			if (jsonData.has("display_name")) {
				lootTableDisplayName = jsonData.getString("display_name");
			} else {
				lootTableDisplayName = lootTableName;
			}

			LootTableV2 lootTable = new LootTableV2(lootTableName, lootTableDisplayName, jsonData.getInt("min_items"), jsonData.getInt("max_items"));

			JSONArray items = jsonData.getJSONArray("items");

			Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "LootTableLoaderV2: reading loot table named " + jsonData.getString("name"));

			for (int i = 0; i < items.length(); i++) {
				JSONObject json = items.getJSONObject(i);
				try {

					ItemStack item;

					if (json.has("data")) {
						short itemData = (short) json.getInt("data");
						item = new ItemStack(Material.getMaterial(json.getString("material")), 1, itemData);
					} else {
						item = new ItemStack(Material.getMaterial(json.getString("material")), 1);
					}

					if (json.has("display_name")) {
						String displayName = json.getString("display_name");

						ItemMeta meta = item.getItemMeta();

						meta.setDisplayName(displayName);

						item.setItemMeta(meta);
					}

					int minAmount = 1;
					int maxAmount = 1;

					if (json.has("potion_data")) {
						PotionMeta meta = (PotionMeta) item.getItemMeta();

						JSONObject potionData = json.getJSONObject("potion_data");

						if (potionData.has("main_effect")) {
							JSONObject mainEffect = potionData.getJSONObject("main_effect");
							PotionEffectType type = PotionEffectType.getByName(mainEffect.getString("type"));
							meta.setMainEffect(type);
						}

						if (potionData.has("custom_effects")) {
							JSONArray customEffects = potionData.getJSONArray("custom_effects");
							for (int j = 0; j < customEffects.length(); j++) {
								meta.addCustomEffect(readPotionEffect(customEffects.getJSONObject(i)), true);
							}
						}

						item.setItemMeta(meta);
					}

					if (json.has("amount")) {
						minAmount = json.getInt("amount");
						maxAmount = minAmount;
					} else {
						if (json.has("min_amount")) {
							minAmount = json.getInt("min_amount");
						}

						if (json.has("max_amount")) {
							maxAmount = json.getInt("max_amount");
							if (minAmount > maxAmount) {
								Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Warning in loot table " + jsonData.getString("name") + json.getString("material") + " item " + " has a min_amount > max_amount " + minAmount + " > " + maxAmount + " changing max_amount to " + minAmount);

								maxAmount = minAmount;
							}
						} else {
							maxAmount = minAmount;
						}
					}

					if (json.has("enchantments")) {
						JSONObject enchantments = json.getJSONObject("enchantments");

						for (String enchant : enchantments.keySet()) {
							int level = enchantments.getInt(enchant);

							item.addUnsafeEnchantment(Enchantment.getByName(enchant), level);
						}
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

					lootTable.addItem(item, chance, minAmount, maxAmount, null);
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
}