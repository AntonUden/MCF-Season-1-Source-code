package xyz.mcfridays.mcf.games.uhc.customitems;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.SpawnEgg;

import net.md_5.bungee.api.ChatColor;
import xyz.mcfridays.mcf.mcfcore.Game.CustomItems.CustomItem;
import xyz.mcfridays.mcf.mcfcore.Game.CustomItems.CustomItemManager;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class FenrirItem extends CustomItem {
	@Override
	public ItemStack getItem(Player player) {
		ItemStack item = new SpawnEgg(EntityType.WOLF).toItemStack(1);

		item = new ItemBuilder(item).setName(ChatColor.GREEN + "Fenrir").addLore("Spawns a tamed wolf").build();

		return item;
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
			e.setCancelled(true);

			if (e.getAction() == Action.RIGHT_CLICK_AIR) {
				return;
			}

			Player p = e.getPlayer();
			if (e.getItem().getAmount() > 1) {
				e.getItem().setAmount(e.getItem().getAmount() - 1);
			} else {
				if (CustomItemManager.isType(p.getItemInHand(), this) && p.getItemInHand().getAmount() == 1) {
					p.setItemInHand(null);
				} else {
					boolean removed = false;
					for (int i = 0; i < p.getInventory().getSize(); i++) {
						ItemStack item = p.getInventory().getItem(i);
						if (item != null) {
							if (item.getType() != Material.AIR) {
								if (CustomItemManager.isType(item, this)) {
									if (item.getAmount() > 1) {
										item.setAmount(item.getAmount() - 1);
										removed = true;
										break;
									} else {
										p.getInventory().setItem(i, null);
										removed = true;
										break;
									}
								}
							}
						}
					}

					if (!removed) {
						return;
					}
				}
			}

			Location spawnLocation = e.getClickedBlock().getLocation().add(0, 1, 0);

			Wolf wolf = (Wolf) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.WOLF);

			wolf.setOwner(e.getPlayer());
			wolf.setMaxHealth(20);
			wolf.setHealth(20);
			wolf.setAdult();

			String name = "Fenrir";

			if (new Random().nextInt(50) == 1) {
				name = "\u0432\u043E\u043B\u043A";
			}

			wolf.setCustomName(e.getPlayer().getName() + "s " + name);
		}
	}
}