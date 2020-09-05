package xyz.mcfridays.mcf.mcfcore.Listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerHeadDrop implements Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		ItemStack playerHead = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);

		SkullMeta meta = (SkullMeta) playerHead.getItemMeta();

		meta.setOwner(p.getName());

		playerHead.setItemMeta(meta);

		p.getWorld().dropItem(p.getLocation(), playerHead);
	}
}