package xyz.mcfridays.mcf.mcfcore.Loot.LootTables.V2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import xyz.mcfridays.mcf.mcfcore.Loot.LootTable;

public class LootTableV2 extends LootTable {
	// Also trash but it has more features

	private HashMap<UUID, LootEntryV2> items = new HashMap<UUID, LootEntryV2>();
	private ArrayList<UUID> lootChance = new ArrayList<UUID>();

	public void addItem(LootEntryV2 lootEntry) {
		UUID uuid = UUID.randomUUID();
		items.put(uuid, lootEntry);
		for (int i = 0; i < lootEntry.getChance(); i++) {
			lootChance.add(uuid);
		}
	}

	@Deprecated
	public void addItem(ItemStack itemStack, int chance, int minAmount, int maxAmount, ArrayList<LootEntryV2> extraItems) {
		if (chance <= 0) {
			return;
		}

		UUID uuid = UUID.randomUUID();

		LootEntryV2 lootEntry = new LootEntryV2(itemStack, chance, minAmount, maxAmount, extraItems);

		items.put(uuid, lootEntry);
		for (int i = 0; i < chance; i++) {
			lootChance.add(uuid);
		}
	}

	public LootTableV2(String name, int minItems, int maxItems) {
		super(name, minItems, maxItems);
	}

	public LootTableV2(String name, String displayName, int minItems, int maxItems) {
		super(name, displayName, minItems, maxItems);
	}

	@Override
	public ArrayList<ItemStack> generateLoot(Random random, int count) {
		ArrayList<ItemStack> result = new ArrayList<ItemStack>();

		for (int i = 0; i < count; i++) {
			int r = random.nextInt(lootChance.size());
			UUID lootUuid = lootChance.get(r);

			LootEntryV2 entry = items.get(lootUuid);

			result.add(entry.generateItem());
			
			if (entry.hasExtraItems()) {
				result.addAll(getExtraItems(entry));
			}
		}

		return result;
	}
	
	private ArrayList<ItemStack> getExtraItems(LootEntryV2 lootEntry) {
		ArrayList<ItemStack> result = new ArrayList<ItemStack>();
		if (lootEntry.hasExtraItems()) {
			for (LootEntryV2 lootEntry2 : lootEntry.getExtraItems()) {
				if (lootEntry2.hasExtraItems()) {
					ArrayList<ItemStack> extra = this.getExtraItems(lootEntry2);
					result.addAll(extra);
				}
				result.add(lootEntry2.generateItem());
			}
		}

		return result;
	}
}