package xyz.mcfridays.mcf.games.bingo.items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import xyz.mcfridays.mcf.mcfcore.Game.CustomItems.CustomItem;
import xyz.mcfridays.mcf.mcfcore.Utils.BookUtils;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class BingoBookItem extends CustomItem {

	@Override
	public ItemStack getItem(Player player) {
		return new ItemBuilder(BookUtils.createBook(ChatColor.GREEN + "Bingo", "MCF", "")).addLore(ChatColor.GREEN + "Click to open bingo menu").addLore(ChatColor.GREEN + "or use the /bingo command").build();
	}

	@Override
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		e.setCancelled(true);
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent e) {
		e.setCancelled(true);
		Bukkit.getServer().dispatchCommand(e.getPlayer(), "bingo");
	}
}