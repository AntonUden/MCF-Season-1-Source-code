package xyz.mcfridays.mcf.games.hungergamesv2.Map.MapSelector;

import java.util.HashMap;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class MapVoteInventoryHolder implements InventoryHolder {
	private HashMap<Integer, String> mapSlots;

	public MapVoteInventoryHolder() {
		this.mapSlots = new HashMap<Integer, String>();
	}

	public HashMap<Integer, String> getMapSlots() {
		return mapSlots;
	}

	@Override
	public Inventory getInventory() {
		return null;
	}
}