package xyz.mcfridays.mcf.games.hungergamesv2.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import xyz.mcfridays.mcf.games.hungergamesv2.MCFHungergames;

public class HGAdminCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (p.hasPermission("mcf.hungergames.admin")) {
				if (args.length != 0) {
					if (args[0].equalsIgnoreCase("start")) {
						if (!MCFHungergames.getInstance().hasStarted()) {
							p.sendMessage(ChatColor.GREEN + "Starting game");

							Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "Game starting");

							MCFHungergames.getInstance().start();
							return true;
						} else {
							p.sendMessage(ChatColor.RED + "Already started");
							return true;
						}
					} else if (args[0].equalsIgnoreCase("forcestart")) {
						p.sendMessage(ChatColor.GREEN + "Force starting game");

						Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "Game starting");

						MCFHungergames.getInstance().start(false);
						return true;
					} else if (args[0].equalsIgnoreCase("lootdrop")) {
						p.sendMessage(ChatColor.GREEN + "Spawning loot drop");

						if (!MCFHungergames.getInstance().getGame().spawnLootDrop()) {
							p.sendMessage(ChatColor.RED + "Failed to spawn loot drop");
						}
						return true;
					} else if (args[0].equalsIgnoreCase("refill")) {
						p.sendMessage(ChatColor.GREEN + "Refilling chests");

						MCFHungergames.getInstance().getGame().refillChests(true);
						return true;
					} else if (args[0].equalsIgnoreCase("status")) {
						String players = "";
						for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
							boolean ingame = MCFHungergames.getInstance().getGame().getPlayers().contains(pl.getUniqueId());

							players += (ingame == true ? ChatColor.GREEN : ChatColor.RED) + pl.getName() + " ";
						}

						p.sendMessage(ChatColor.GOLD + "========= " + ChatColor.AQUA + "Status" + ChatColor.GOLD + " =========");
						p.sendMessage(ChatColor.GOLD + "Stage: " + ChatColor.AQUA + MCFHungergames.getInstance().getGame().getStage().name());
						p.sendMessage(ChatColor.GOLD + "Players: " + players);
						p.sendMessage(ChatColor.GOLD + "=========================");

						return true;
					} else if (args[0].equalsIgnoreCase("help")) {
						p.sendMessage(ChatColor.GOLD + "/hgadmin start : start game");
						p.sendMessage(ChatColor.GOLD + "/hgadmin forcestart : start game without countdown");
						p.sendMessage(ChatColor.GOLD + "/hgadmin lootdrop : spawn a loot drop");
						p.sendMessage(ChatColor.GOLD + "/hgadmin status : show status");
						return true;
					}

					p.sendMessage(ChatColor.DARK_RED + "Invalid usage. use /hgadmin help for help");
				} else {
					p.sendMessage(ChatColor.DARK_RED + "Invalid usage. use /hgadmin help for help");
				}
			} else {
				p.sendMessage(ChatColor.DARK_RED + "You dont have permission to use this");
			}
		} else {
			sender.sendMessage(ChatColor.DARK_RED + "This command is only for players");
		}
		return false;
	}
}