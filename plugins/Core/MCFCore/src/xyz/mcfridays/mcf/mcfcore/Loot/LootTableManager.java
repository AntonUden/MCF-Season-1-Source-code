package xyz.mcfridays.mcf.mcfcore.Loot;

import java.io.File;
import java.util.HashMap;

import xyz.mcfridays.mcf.mcfcore.Loot.LootTables.PlaceholderLootTable;

public class LootTableManager {
	private HashMap<String, LootTable> lootTables;
	private LootTable placeholderLootTable;

	private LootTableLoader lootTableLoader;

	public HashMap<String, LootTable> getLootTables() {
		return lootTables;
	}

	public LootTableLoader getLootTableLoader() {
		return lootTableLoader;
	}

	public void loadAll(File file) {
		loadAllWithLoader(file, lootTableLoader);
	}

	public void loadLootTable(File file) {
		loadLootTableWithLoader(file, lootTableLoader);
	}
	
	public void loadAllWithLoader(File file, LootTableLoader loader) {
		for (File fileEntry : file.listFiles()) {
			if (!fileEntry.isDirectory()) {
				loadLootTableWithLoader(fileEntry, loader);
			}
		}
	}

	public void loadLootTableWithLoader(File file, LootTableLoader loader) {
		LootTable lootTable = loader.read(file);
		lootTables.put(lootTable.getName(), lootTable);
	}

	public LootTable getLootTable(String name) {
		return lootTables.get(name);
	}

	public LootTable getPlaceholderLootTable() {
		return placeholderLootTable;
	}

	public LootTableManager(LootTableLoader lootTableLoader) {
		this.lootTables = new HashMap<String, LootTable>();
		this.placeholderLootTable = (LootTable) new PlaceholderLootTable();

		this.lootTableLoader = lootTableLoader;
	}
}