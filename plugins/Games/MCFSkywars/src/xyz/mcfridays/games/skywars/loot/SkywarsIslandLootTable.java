package xyz.mcfridays.games.skywars.loot;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.inventory.ItemStack;

import xyz.mcfridays.mcf.mcfcore.Loot.LootTable;

public class SkywarsIslandLootTable extends LootTable {
	private ArrayList<SkywarsLootGroup> lootGroups;

	public SkywarsIslandLootTable(String name, String lootTableDisplayName, ArrayList<SkywarsLootGroup> lootGroups) {
		super(name, lootTableDisplayName, 1, 1);
		this.lootGroups = lootGroups;
	}

	/**
	 * Skywars does not support item count
	 */
	@Override
	@Deprecated
	public ArrayList<ItemStack> generateLoot(Random random, int count) {
		return generateLoot();
	}

	public ArrayList<ItemStack> generateLoot(Random random) {
		ArrayList<ItemStack> result = new ArrayList<ItemStack>();

		for (SkywarsLootGroup lootGroup : lootGroups) {
			ArrayList<ItemStack> items = lootGroup.generate();
			for (ItemStack item : items) {
				result.add(item);
			}
		}

		return result;
	}

	@Override
	public ArrayList<ItemStack> generateLoot() {
		return this.generateLoot(new Random());
	}

	@Override
	@Deprecated
	public int getMaxItems() {
		return 0;
	}

	@Override
	@Deprecated
	public int getMinItems() {
		return 0;
	}
}