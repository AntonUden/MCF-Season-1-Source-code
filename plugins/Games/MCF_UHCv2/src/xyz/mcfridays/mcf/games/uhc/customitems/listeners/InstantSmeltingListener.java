package xyz.mcfridays.mcf.games.uhc.customitems.listeners;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Utils.BlockUtils;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class InstantSmeltingListener implements Listener {
	private HashMap<Material, ItemStack> drops;

	public InstantSmeltingListener() {
		this.drops = new HashMap<Material, ItemStack>();
		
		drops.put(Material.IRON_ORE, new ItemBuilder(Material.IRON_INGOT).setAmount(1).build());
		drops.put(Material.GOLD_ORE, new ItemBuilder(Material.GOLD_INGOT).setAmount(1).build());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		if (!e.isCancelled()) {
			if (e.getPlayer() != null) {
				if (MCFCore.getInstance().getGameManager().isPlaying(e.getPlayer())) {
					if(drops.containsKey(e.getBlock().getType())) {
						e.setCancelled(true);
						
						Location location = e.getBlock().getLocation();
						
						location.setX(BlockUtils.blockCenter(location.getBlockX()));
						location.setY(BlockUtils.blockCenter(location.getBlockY()));
						location.setZ(BlockUtils.blockCenter(location.getBlockZ()));
						
						Item item = e.getBlock().getLocation().getWorld().dropItem(location, drops.get(e.getBlock().getType()).clone());
						item.teleport(location);
						item.setVelocity(item.getVelocity().zero());
						
						e.getBlock().setType(Material.AIR);
					}
				}
			}
		}
	}
}