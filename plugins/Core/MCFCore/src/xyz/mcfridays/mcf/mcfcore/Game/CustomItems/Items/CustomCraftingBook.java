package xyz.mcfridays.mcf.mcfcore.Game.CustomItems.Items;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Game.CustomItems.CustomItem;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class CustomCraftingBook extends CustomItem {
	@Override
	public ItemStack getItem(Player player) {
		ItemStack stack = new ItemBuilder(Material.WRITTEN_BOOK).setName(ChatColor.GOLD + "" + ChatColor.BOLD + "Custom craftings").build();

		BookMeta meta = (BookMeta) stack.getItemMeta();

		meta.addPage("You're not supposed to be in here.");
		meta.addPage("Last warning. Leave, now.");
		meta.addPage("I'm not going to warn you again. Get out, or I'll call the guards!");
		meta.addPage("Guards! Trespasser!");
		meta.addPage("You should have listened. Guards! Help! Trespasser!");
		meta.setAuthor(new Random().nextInt(1000) == 1 ? "Your mom" : "Zeeraa01");
		meta.setTitle("Custom craftings");

		stack.setItemMeta(meta);

		return stack;
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent e) {
		e.setCancelled(true);
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			e.setCancelled(true);
			Player p = e.getPlayer();

			MCFCore.getInstance().getCustomRecipeGUI().show(p);
		}
	}
}