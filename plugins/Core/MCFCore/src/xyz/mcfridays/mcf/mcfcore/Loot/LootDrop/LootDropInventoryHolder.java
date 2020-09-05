package xyz.mcfridays.mcf.mcfcore.Loot.LootDrop;

import java.util.UUID;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class LootDropInventoryHolder implements InventoryHolder {
	private UUID uuid;

	public LootDropInventoryHolder(UUID uuid) {
		this.uuid = uuid;
	}

	public UUID getUuid() {
		return uuid;
	}

	@Override
	public Inventory getInventory() {
		return null;
	}
}