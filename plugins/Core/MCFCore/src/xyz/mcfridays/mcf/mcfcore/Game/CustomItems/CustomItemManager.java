package xyz.mcfridays.mcf.mcfcore.Game.CustomItems;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import xyz.mcfridays.mcf.mcfcore.MCFCore;

public class CustomItemManager implements Listener {
	private HashMap<String, CustomItem> customItems;

	public CustomItemManager() {
		this.customItems = new HashMap<String, CustomItem>();
	}

	public org.bukkit.inventory.ItemStack getItem(Class<? extends CustomItem> clazz, @Nullable Player player) {
		if (customItems.containsKey(clazz.getName())) {
			CustomItem item = customItems.get(clazz.getName());

			ItemStack nmsStack = CraftItemStack.asNMSCopy(item.getItem(player));

			NBTTagCompound compound = (nmsStack.hasTag() ? nmsStack.getTag() : new NBTTagCompound());

			compound.setString("mcf_customitem_class", item.getClass().getName());

			return CraftItemStack.asBukkitCopy(nmsStack);
		}
		return null;
	}

	public org.bukkit.inventory.ItemStack getItem(Class<? extends CustomItem> clazz) {
		return this.getItem(clazz, null);
	}

	public boolean isCustomItem(org.bukkit.inventory.ItemStack item) {
		if (item.getType() == null) {
			return false;
		}

		if (item.getType() == Material.AIR) {
			return false;
		}

		ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		if (nmsStack.hasTag()) {
			if (nmsStack.getTag().hasKey("mcf_customitem_class")) {
				return true;
			}
		}

		return false;
	}

	public static boolean isType(org.bukkit.inventory.ItemStack item, CustomItem customItem) {
		return CustomItemManager.isType(item, customItem.getClass());
	}

	public static boolean isType(org.bukkit.inventory.ItemStack item, Class<? extends CustomItem> clazz) {
		ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		if (nmsStack.hasTag()) {
			if (nmsStack.getTag().hasKey("mcf_customitem_class")) {
				if (nmsStack.getTag().getString("mcf_customitem_class").equals(clazz.getName())) {
					return true;
				}
			}
		}

		return false;
	}

	@Nullable
	public CustomItem getCustomItem(org.bukkit.inventory.ItemStack item) {
		net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		if (nmsStack.hasTag()) {
			if (nmsStack.getTag().hasKey("mcf_customitem_class")) {
				return customItems.get(nmsStack.getTag().getString("mcf_customitem_class"));
			}
		}

		return null;
	}
	
	public CustomItem getCustomItem(Class<? extends CustomItem> clazz) {
		return customItems.get(clazz.getName());
	}

	public boolean hasCustomItem(Class<? extends CustomItem> clazz) {
		return customItems.containsKey(clazz.getName());
	}

	public boolean hasCustomItem(CustomItem customItem) {
		return this.hasCustomItem(customItem.getClass());
	}

	public boolean add(Class<? extends CustomItem> clazz) {
		try {
			return this.add(clazz.getConstructor().newInstance(new Object[] {}));
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean add(CustomItem customItem) {
		if (customItems.containsKey(customItem.getClass().getName())) {
			return false;
		}

		customItems.put(customItem.getClass().getName(), customItem);

		if (customItem instanceof Listener) {
			Bukkit.getServer().getPluginManager().registerEvents((Listener) customItem, MCFCore.getInstance());
		}

		customItem.onAdded();
		System.out.println("Added custom item " + customItem.getClass().getName());
		return true;
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		if (this.isCustomItem(e.getItemDrop().getItemStack())) {
			CustomItem customItem = this.getCustomItem(e.getItemDrop().getItemStack());

			if (customItem != null) {
				customItem.onPlayerDropItem(e);
			}
		}
	}
	
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent e) {
		if (this.isCustomItem(e.getItem().getItemStack())) {
			CustomItem customItem = this.getCustomItem(e.getItem().getItemStack());

			if (customItem != null) {
				customItem.onPlayerPickupItem(e);
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getItem() != null) {
			if (this.isCustomItem(e.getItem())) {
				CustomItem customItem = this.getCustomItem(e.getItem());
				if (customItem != null) {
					customItem.onPlayerInteract(e);
				}
			}
		}
	}

	public HashMap<String, CustomItem> getCustomItems() {
		return customItems;
	}
}