package xyz.mcfridays.mcf.mcfcore.GUI;

import java.util.HashMap;

import javax.annotation.Nullable;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class GUIInventoryHolder implements InventoryHolder, CloseCallbackHolder {
	private GUICloseCallback closeCallback;

	private HashMap<Integer, GUIClickCallback> slotCallbacks = new HashMap<Integer, GUIClickCallback>();

	public HashMap<Integer, GUIClickCallback> getSlotCallbacks() {
		return slotCallbacks;
	}

	public void addClickCallback(int slot, GUIClickCallback callback) {
		slotCallbacks.put(slot, callback);
	}

	public boolean hasClickCallback(int slot) {
		return slotCallbacks.containsKey(slot);
	}

	public GUIClickCallback getClickCallback(int slot) {
		return slotCallbacks.get(slot);
	}

	public GUIInventoryHolder setCloseCallback(GUICloseCallback callback) {
		closeCallback = callback;
		return this;
	}

	@Override
	@Nullable
	public GUICloseCallback getCloseCallback() {
		return closeCallback;
	}

	/**
	 * Deprecated. use null check {@link GUIInventoryHolder#getCloseCallback()}
	 * instead
	 * 
	 * @return <code>true</code> if callback is null
	 */
	@Deprecated
	public boolean hasCloseCallback() {
		return closeCallback != null;
	}

	@Override
	@Deprecated
	public Inventory getInventory() {
		return null;
	}

}