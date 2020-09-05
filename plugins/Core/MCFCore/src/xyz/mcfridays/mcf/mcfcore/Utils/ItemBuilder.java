package xyz.mcfridays.mcf.mcfcore.Utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemBuilder {
	protected ItemStack item;
	protected ItemMeta meta;

	public ItemBuilder(Material material) {
		this(material, 1);
	}

	public ItemBuilder(Material material, int ammount) {
		this(new ItemStack(material, ammount), false);
	}

	public ItemBuilder(ItemStack itemStack) {
		this(itemStack, false);
	}

	public ItemBuilder(ItemStack itemStack, boolean clone) {
		if (clone) {
			this.item = itemStack.clone();
		} else {
			this.item = itemStack;
		}
		this.meta = this.item.getItemMeta();
	}

	public ItemBuilder setName(String name) {
		meta.setDisplayName(name);
		return this;
	}

	public ItemBuilder addEnchant(Enchantment ench, int level) {
		return this.addEnchant(ench, level, false);
	}

	public ItemBuilder addEnchant(Enchantment ench, int level, boolean ignoreLevelRestriction) {
		meta.addEnchant(ench, level, ignoreLevelRestriction);
		return this;
	}

	public ItemBuilder removeEnchant(Enchantment ench) {
		meta.removeEnchant(ench);
		return this;
	}

	public ItemBuilder addItemFlags(ItemFlag itemFlag) {
		meta.addItemFlags(itemFlag);
		return this;
	}

	public ItemBuilder removeItemFlags(ItemFlag itemFlag) {
		meta.removeItemFlags(itemFlag);
		return this;
	}

	public ItemBuilder setAmount(int amount) {
		item.setAmount(amount);
		return this;
	}

	public ItemBuilder setItemMeta(ItemMeta meta) {
		item.setItemMeta(meta);
		this.meta = meta;
		return this;
	}
	
	public ItemBuilder setUnbreakable(boolean unbreakable) {
		meta = item.getItemMeta();
		meta.spigot().setUnbreakable(unbreakable);
		item.setItemMeta(meta);
		return this;
	}

	public ItemBuilder addLore(String string) {
		List<String> lore;
		if (meta.hasLore()) {
			lore = meta.getLore();
		} else {
			lore = new ArrayList<String>();
		}

		lore.add(string);
		meta.setLore(lore);
		return this;
	}

	public ItemBuilder setDurability(short durability) {
		item.setDurability(durability);
		return this;
	}

	public ItemStack build() {
		this.item.setItemMeta(meta);
		return this.item;
	}
}