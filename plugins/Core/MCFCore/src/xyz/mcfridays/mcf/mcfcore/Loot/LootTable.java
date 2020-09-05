package xyz.mcfridays.mcf.mcfcore.Loot;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

public abstract class LootTable {
	private UUID uuid;
	private String name;
	private String displayName;
	private int minItems;
	private int maxItems;

	public LootTable(String name, int minItems, int maxItems) {
		this(name, name, minItems, maxItems);
	}

	public LootTable(String name, String displayName, int minItems, int maxItems) {
		this.uuid = UUID.randomUUID();
		this.name = name;
		this.displayName = displayName;
		this.minItems = minItems;
		this.maxItems = maxItems;
	}

	/**
	 * Get {@link UUID} of loot table
	 * 
	 * @return loot table {@link UUID}
	 */
	public UUID getUuid() {
		return uuid;
	}

	/**
	 * Get name of loot table
	 * 
	 * @return name {@link String}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get display name of loot table
	 * 
	 * @return display name {@link String}
	 */
	public String getDisplayName() {
		return displayName;
	}

	protected void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	/**
	 * Get minimum items to add
	 * 
	 * @return minimum items
	 */
	public int getMinItems() {
		return minItems;
	}

	/**
	 * Get max items to add
	 * 
	 * @return max items
	 */
	public int getMaxItems() {
		return maxItems;
	}

	/**
	 * Generate loot using loot table
	 * 
	 * @return {@link ArrayList} with {@link ItemStack}
	 */
	public ArrayList<ItemStack> generateLoot() {
		return this.generateLoot(new Random());
	}

	/**
	 * Generate loot using loot table
	 * 
	 * @param random instance of {@link Random} to use
	 * @return {@link ArrayList} with {@link ItemStack}
	 */
	public ArrayList<ItemStack> generateLoot(Random random) {
		int count = minItems + random.nextInt((maxItems - minItems) + 1);
		return this.generateLoot(random, count);
	}

	/**
	 * Generate loot using loot table
	 * 
	 * @param count amount of {@link ItemStack} to generate
	 * @return {@link ArrayList} with {@link ItemStack}
	 */
	public ArrayList<ItemStack> generateLoot(int count) {
		return this.generateLoot(new Random(), count);
	}

	/**
	 * Generate loot using loot table
	 * 
	 * @param random instance of {@link Random} to use
	 * @param count  amount of {@link ItemStack} to generate
	 * @return {@link ArrayList} with {@link ItemStack}
	 */
	public abstract ArrayList<ItemStack> generateLoot(Random random, int count);
}