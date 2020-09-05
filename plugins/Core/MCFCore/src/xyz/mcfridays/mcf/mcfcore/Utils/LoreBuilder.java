package xyz.mcfridays.mcf.mcfcore.Utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LoreBuilder {
	private List<String> lore;

	public LoreBuilder(String... lines) {
		this();

		for (String line : lines) {
			this.lore.add(line);
		}
	}

	public LoreBuilder(String line) {
		this();

		this.lore.add(line);
	}

	public LoreBuilder() {
		this.lore = new ArrayList<String>();
	}
	
	public LoreBuilder(List<String> lore) {
		this.lore = lore;
	}
	
	public LoreBuilder(ItemStack item) {
		this(item.getItemMeta());
	}
	
	public LoreBuilder(ItemMeta meta) {
		this.lore = meta.getLore();
	}
	
	public LoreBuilder addLine(String... lines) {
		for (String line : lines) {
			this.lore.add(line);
		}
		
		return this;
	}
	
	public LoreBuilder clearLines() {
		this.lore.clear();
		
		return this;
	}

	public List<String> build() {
		return this.lore;
	}
	
	public static List<String> fromString(String... lines) {
		return new LoreBuilder(lines).build();
	}
}