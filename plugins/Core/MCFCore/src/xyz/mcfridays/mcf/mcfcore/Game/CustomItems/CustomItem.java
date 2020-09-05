package xyz.mcfridays.mcf.mcfcore.Game.CustomItems;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public abstract class CustomItem {
	public abstract ItemStack getItem(@Nullable Player player);

	public ItemStack getItem() {
		return this.getItem(null);
	}

	public void onPlayerInteract(PlayerInteractEvent e) {
	}

	public void onPlayerDropItem(PlayerDropItemEvent e) {
	}

	public void onBlockPlace(BlockPlaceEvent e) {
	}
	
	public void onPlayerPickupItem(PlayerPickupItemEvent e) {
	}

	public void onAdded() {
	}
}