package xyz.mcfridays.games.skywars.mapselector;

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