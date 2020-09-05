package xyz.mcfridays.mcf.mcfcore.GUI;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;

public interface GUICloseCallback {
	public void onClose(HumanEntity humanEntity, Inventory inventory);
}