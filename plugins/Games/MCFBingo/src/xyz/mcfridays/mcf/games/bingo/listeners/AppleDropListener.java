package xyz.mcfridays.mcf.games.bingo.listeners;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.material.Tree;

import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class AppleDropListener implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		if(e.getBlock().getType() == Material.LEAVES) {
			Tree tree = (Tree) e.getBlock().getState().getData();
			if(tree.getSpecies() == TreeSpecies.GENERIC) {
				Random random = new Random();
				
				if(random.nextInt(50) == 1) {
					e.setCancelled(true);
					
					e.getBlock().setType(Material.AIR);
					
					e.getBlock().getLocation().getWorld().dropItemNaturally(e.getBlock().getLocation(),new ItemBuilder(Material.APPLE).build());
				}
			}
		}
	}
}