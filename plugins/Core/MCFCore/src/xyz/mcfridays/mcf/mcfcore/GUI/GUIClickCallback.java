package xyz.mcfridays.mcf.mcfcore.GUI;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;

public interface GUIClickCallback {
	public void onClick(HumanEntity whoClicked, Inventory clickedInventory, int slot, InventoryAction action);
}