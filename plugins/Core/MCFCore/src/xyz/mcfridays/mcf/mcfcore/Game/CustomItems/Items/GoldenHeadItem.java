package xyz.mcfridays.mcf.mcfcore.Game.CustomItems.Items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import xyz.mcfridays.mcf.mcfcore.Game.CustomItems.CustomItem;
import xyz.mcfridays.mcf.mcfcore.Game.CustomItems.CustomItemManager;
import xyz.mcfridays.mcf.mcfcore.Utils.SkullUtils;

public class GoldenHeadItem extends CustomItem {
	@Override
	public ItemStack getItem(Player player) {
		ItemStack stack = SkullUtils.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjkzN2UxYzQ1YmI4ZGEyOWIyYzU2NGRkOWE3ZGE3ODBkZDJmZTU0NDY4YTVkZmI0MTEzYjRmZjY1OGYwNDNlMSJ9fX0=");

		ItemMeta meta = stack.getItemMeta();

		meta.setDisplayName(ChatColor.GOLD + "Golden head");

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

			p.getWorld().playSound(p.getLocation(), Sound.EAT, 1F, 1F);

			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30 * 20, 1));
			p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 15 * 20, 2));
			p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 60 * 20, 1));
		}
	}
}