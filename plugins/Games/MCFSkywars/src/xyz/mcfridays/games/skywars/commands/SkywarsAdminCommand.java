package xyz.mcfridays.games.skywars.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import xyz.mcfridays.games.skywars.MCFSkywars;

public class SkywarsAdminCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (p.hasPermission("mcf.skywars.admin")) {
				if (args.length != 0) {
					if (args[0].equalsIgnoreCase("start")) {
						if (!MCFSkywars.getInstance().hasStarted()) {
							p.sendMessage(ChatColor.GREEN + "Starting game");

							Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "Game starting");

							MCFSkywars.getInstance().start();
							return true;
						} else {
							p.sendMessage(ChatColor.RED + "Already started");
							return true;
						}
					} else if (args[0].equalsIgnoreCase("forcestart")) {
						p.sendMessage(ChatColor.GREEN + "Force starting game");

						Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "Game starting");

						MCFSkywars.getInstance().start(false);
						return true;
					} else if (args[0].equalsIgnoreCase("refill")) {
						p.sendMessage(ChatColor.GREEN + "Refilling chests");

						MCFSkywars.getInstance().getGame().refillChests(true);
						return true;
					} else if (args[0].equalsIgnoreCase("status")) {
						String players = "";
						for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
							boolean ingame = MCFSkywars.getInstance().getGame().getPlayers().contains(pl.getUniqueId());

							players += (ingame == true ? ChatColor.GREEN : ChatColor.RED) + pl.getName() + " ";
						}

						p.sendMessage(ChatColor.GOLD + "========= " + ChatColor.AQUA + "Status" + ChatColor.GOLD + " =========");
						p.sendMessage(ChatColor.GOLD + "Stage: " + ChatColor.AQUA + MCFSkywars.getInstance().getGame().getStage().name());
						p.sendMessage(ChatColor.GOLD + "Players: " + players);
						p.sendMessage(ChatColor.GOLD + "=========================");

						return true;
					} else if (args[0].equalsIgnoreCase("cage_on")) {
						if (MCFSkywars.getInstance().hasActiveMap()) {
							p.sendMessage(MCFSkywars.getInstance().getGame().setCages(true) ? ChatColor.GREEN + "ok" : ChatColor.RED + "failed");
							return true;
						} else {
							p.sendMessage(ChatColor.RED + "No map");
							return true;
						}
					} else if (args[0].equalsIgnoreCase("cage_off")) {
						if (MCFSkywars.getInstance().hasActiveMap()) {
							p.sendMessage(MCFSkywars.getInstance().getGame().setCages(false) ? ChatColor.GREEN + "ok" : ChatColor.RED + "failed");
							return true;
						} else {
							p.sendMessage(ChatColor.RED + "No map");
							return true;
						}
					} else if (args[0].equalsIgnoreCase("help")) {
						p.sendMessage(ChatColor.GOLD + "/skywarsadmin start : start game");
						p.sendMessage(ChatColor.GOLD + "/skywarsadmin forcestart : start game without countdown");
						p.sendMessage(ChatColor.GOLD + "/skywarsadmin refill : refull chests");
						p.sendMessage(ChatColor.GOLD + "/skywarsadmin status : show status");
						p.sendMessage(ChatColor.GOLD + "/skywarsadmin cage_on : place start cage");
						p.sendMessage(ChatColor.GOLD + "/skywarsadmin cage_off : remove start cage");
						return true;
					}

					p.sendMessage(ChatColor.DARK_RED + "Invalid usage. use /skywarsadmin help for help");
				} else {
					p.sendMessage(ChatColor.DARK_RED + "Invalid usage. use /skywarsadmin help for help");
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