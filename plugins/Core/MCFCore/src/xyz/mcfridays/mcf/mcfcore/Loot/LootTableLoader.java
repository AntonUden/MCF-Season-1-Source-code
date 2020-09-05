package xyz.mcfridays.mcf.mcfcore.Loot;

import java.io.File;

public interface LootTableLoader {
	/**
	 * Read loot table from file
	 * 
	 * @param file {@link File} to read
	 * @return {@link LootTable}
	 */
	public LootTable read(File file);
}