package xyz.mcfridays.mcf.mcfcore.Loot.LootTables.V2;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.inventory.ItemStack;

public class LootEntryV2 {
	private ItemStack item;
	private int minAmount;
	private int maxAmount;
	private int chance;
	private Random random;
	private ArrayList<LootEntryV2> extraItems;

	public LootEntryV2(ItemStack item, int chance, int minAmount, int maxAmount, ArrayList<LootEntryV2> extraItems) {
		this.item = item;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
		this.random = new Random();
		this.extraItems = extraItems;
		this.chance = chance;
	}

	public ItemStack generateItem() {
		return this.generateItem(getAmount(minAmount, maxAmount));
	}

	public ItemStack generateItem(int amount) {
		ItemStack result = item.clone();

		result.setAmount(amount);

		return result;
	}

	public ItemStack getItem() {
		return item;
	}

	public int getMinAmount() {
		return minAmount;
	}

	public int getMaxAmount() {
		return maxAmount;
	}

	private int getAmount(int min, int max) {
		if (min == max) {
			return min;
		}
		return min + random.nextInt((max + 1) - min);
	}
	
	public boolean hasExtraItems() {
		return extraItems != null;
	}
	
	public ArrayList<LootEntryV2> getExtraItems() {
		return extraItems;
	}
	
	public int getChance() {
		return chance;
	}
}