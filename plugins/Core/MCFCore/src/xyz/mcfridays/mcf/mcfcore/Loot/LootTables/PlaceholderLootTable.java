package xyz.mcfridays.mcf.mcfcore.Loot.LootTables;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import xyz.mcfridays.mcf.mcfcore.Loot.LootTable;

public class PlaceholderLootTable extends LootTable {
	public PlaceholderLootTable() {
		super("placeholder", 1, 1);
		
	}

	@Override
	public ArrayList<ItemStack> generateLoot(Random random, int count) {
		ArrayList<ItemStack> result = new ArrayList<ItemStack>();

		ItemStack stack = new ItemStack(Material.WRITTEN_BOOK);

		BookMeta bm = (BookMeta) stack.getItemMeta();

		bm.setDisplayName("Bruh");
		bm.setAuthor("Zeeraa01");

		bm.addPage("imagine forgetting to add the loot table lmao");

		stack.setItemMeta(bm);

		result.add(stack);

		return result;
	}
}