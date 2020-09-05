package xyz.mcfridays.mcf.mcfcore.GUI;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;

public class GUIListener implements Listener {
	@EventHandler
	public void onInventoryInteract(InventoryInteractEvent e) {
		if (e.getInventory().getHolder() instanceof ReadOnlyGUIInventoryHolder) {
			e.setCancelled(true);
			return;
		}

		if (e.getInventory().getHolder() instanceof GUIInventoryHolder) {
			// System.out.println("cancel interact " + e);
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getInventory().getHolder() instanceof ReadOnlyGUIInventoryHolder) {
			e.setCancelled(true);
			return;
		}

		if (e.getInventory().getHolder() instanceof GUIInventoryHolder) {
			if (e.getClickedInventory() != null) {
				if (e.getClickedInventory().getHolder() instanceof GUIInventoryHolder) {
					e.setCancelled(true);
					GUIInventoryHolder holder = (GUIInventoryHolder) e.getInventory().getHolder();
					if (holder.hasClickCallback(e.getSlot())) {
						holder.getClickCallback(e.getSlot()).onClick(e.getWhoClicked(), e.getClickedInventory(), e.getSlot(), e.getAction());
					}
				}
			}
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		if (e.getInventory().getHolder() instanceof CloseCallbackHolder) {
			CloseCallbackHolder holder = (CloseCallbackHolder) e.getInventory().getHolder();
			if (holder.getCloseCallback() != null) {
				holder.getCloseCallback().onClose(e.getPlayer(), e.getInventory());
			}
		}
	}
}