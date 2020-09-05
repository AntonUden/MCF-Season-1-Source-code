package xyz.mcfridays.mcf.mcfcore.GUI;

import javax.annotation.Nullable;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ReadOnlyGUIInventoryHolder implements InventoryHolder, CloseCallbackHolder {
	private GUICloseCallback closeCallback;

	public ReadOnlyGUIInventoryHolder() {
		this.closeCallback = null;
	}

	public ReadOnlyGUIInventoryHolder(GUICloseCallback callback) {
		this.closeCallback = callback;
	}

	@Override
	@Deprecated
	public Inventory getInventory() {
		return null;
	}

	public ReadOnlyGUIInventoryHolder setCloseCallback(GUICloseCallback callback) {
		closeCallback = callback;
		return this;
	}

	@Override
	@Nullable
	public GUICloseCallback getCloseCallback() {
		return closeCallback;
	}
}