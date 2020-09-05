package xyz.mcfridays.mcf.lobby.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class FreeRodSign implements Listener {
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getClickedBlock().getType() == Material.SIGN_POST || e.getClickedBlock().getType() == Material.WALL_SIGN) {
				if (e.getPlayer().getGameMode() != GameMode.SPECTATOR) {
					if (e.getClickedBlock().getState() instanceof Sign) {
						Sign sign = (Sign) e.getClickedBlock().getState();
						if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[Free]") && ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase("Fishing rod")) {
							Player p = e.getPlayer();
							
							if(!p.getInventory().contains(Material.FISHING_ROD)) {
								p.getInventory().addItem(new ItemBuilder(Material.FISHING_ROD).setUnbreakable(true).build());
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		if (e.getLine(0).equalsIgnoreCase("[free rod]")) {
			e.setLine(0, ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "[Free]");
			e.setLine(1, ChatColor.BLUE + "Fishing rod");
			e.setLine(2, "");
			e.setLine(3, "");
		}
	}
}