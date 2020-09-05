package xyz.mcfridays.mcf.games.uhc.customitems;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;
import xyz.mcfridays.mcf.games.uhc.MCFUHC;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Game.CustomItems.CustomItem;
import xyz.mcfridays.mcf.mcfcore.Game.CustomItems.CustomItemManager;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class AndurilItem extends CustomItem {
	public AndurilItem() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFUHC.getInstance(), new Runnable() {
			@Override
			public void run() {
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					ItemStack item = p.getItemInHand();

					if (item != null) {
						if (MCFCore.getInstance().getCustomItemManager().isCustomItem(item)) {
							if (CustomItemManager.isType(item, AndurilItem.class)) {
								p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 1), true);
								p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1), true);
							}
						}
					}
				}
			}
		}, 10L, 10L);
	}

	@Override
	public ItemStack getItem(Player player) {
		ItemStack item = new ItemBuilder(Material.IRON_SWORD).setAmount(1).addEnchant(Enchantment.DAMAGE_ALL, 2).setName(ChatColor.GREEN + "Andúril").addLore(ChatColor.AQUA + "Resistance I (While holding)").addLore(ChatColor.AQUA + "Speed I (While holding)").build();
		return item;
	}
}