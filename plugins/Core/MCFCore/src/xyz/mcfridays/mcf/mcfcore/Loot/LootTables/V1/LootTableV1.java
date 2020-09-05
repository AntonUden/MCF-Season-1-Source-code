package xyz.mcfridays.mcf.mcfcore.Loot.LootTables.V1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import xyz.mcfridays.mcf.mcfcore.Loot.LootTable;

@Deprecated
public class LootTableV1 extends LootTable {
	// this system is trash and will be replaced by a better one in LootTableV2

	private HashMap<UUID, ItemStack> items = new HashMap<UUID, ItemStack>();
	private ArrayList<UUID> lootChance = new ArrayList<UUID>();

	@Deprecated
	public void addItem(ItemStack itemStack, int chance) {
		if (chance <= 0) {
			return;
		}

		UUID uuid = UUID.randomUUID();
		items.put(uuid, itemStack);
		for (int i = 0; i < chance; i++) {
			lootChance.add(uuid);
		}
	}

	@Deprecated
	public LootTableV1(String name, int minItems, int maxItems) {
		super(name, minItems, maxItems);
	}

	@Override
	@Deprecated
	public ArrayList<ItemStack> generateLoot(Random random, int count) {
		ArrayList<ItemStack> result = new ArrayList<ItemStack>();

		for (int i = 0; i < count; i++) {
			int r = random.nextInt(lootChance.size());
			UUID lootUuid = lootChance.get(r);

			result.add(items.get(lootUuid).clone());
		}

		return result;
	}
}