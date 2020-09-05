package xyz.mcfridays.mcf.games.bingo.commands;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import xyz.mcfridays.mcf.games.bingo.MCFBingo;

public class TopCommand implements CommandExecutor {
	private HashMap<UUID, Integer> cooldownList;

	public TopCommand() {
		cooldownList = new HashMap<UUID, Integer>();

		Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFBingo.getInstance(), new Runnable() {
			@Override
			public void run() {
				for (UUID uuid : cooldownList.keySet()) {
					if (cooldownList.get(uuid) <= 1) {
						cooldownList.remove(uuid);
						
						Player player = Bukkit.getServer().getPlayer(uuid);
						if(player != null) {
							if(player.isOnline()) {
								player.sendMessage(ChatColor.GREEN + "You can now use /top again");
							}
						}
						continue;
					}

					cooldownList.put(uuid, cooldownList.get(uuid) - 1);
				}
			}
		}, 20L, 20L);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (p.getLocation().getWorld().getEnvironment() == Environment.NETHER) {
				p.sendMessage(ChatColor.RED + "You cant use /top in the nether");
				return true;
			}
			
			if (cooldownList.containsKey(p.getUniqueId())) {
				p.sendMessage(ChatColor.RED + "Please wait " + cooldownList.get(p.getUniqueId()) + " seconds before using this command again");
				return true;
			}

			Location location = p.getLocation().clone();

			cooldownList.put(p.getUniqueId(), 60);
			
			location.setY(256);
			
			for (int i = 255; i > 1; i--) {
				location.setY(i);
				
				if (location.getBlock() != null) {
					if (location.getBlock().getType() != Material.AIR) {
						location.add(0, 1, 0);
						p.teleport(location);
						break;
					}
				}
			}
		} else {
			sender.sendMessage(ChatColor.DARK_RED + "This command is only for players");
		}
		return true;
	}
}