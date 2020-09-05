package xyz.mcfridays.mcf.games.uhc.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import xyz.mcfridays.mcf.games.uhc.MCFUHC;

public class UHCAdminCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (p.hasPermission("mcf.uhc.admin")) {
				if (args.length != 0) {
					if (args[0].equalsIgnoreCase("start")) {
						MCFUHC.getInstance().startTpCountdown();
						return true;
					}
					if (args[0].equalsIgnoreCase("forcestart")) {
						MCFUHC.getInstance().forcestart();
						return true;
					}
					if (args[0].equalsIgnoreCase("gotoworld")) {
						p.teleport(MCFUHC.getInstance().getGame().getWorld().getSpawnLocation());
						return true;
					}

					else if (args[0].equalsIgnoreCase("help")) {
						p.sendMessage(ChatColor.GOLD + "/uhcadmin help : show help");
						p.sendMessage(ChatColor.GOLD + "/uhcadmin start : start game");
						p.sendMessage(ChatColor.GOLD + "/uhcadmin forcestart : force start game");
						p.sendMessage(ChatColor.GOLD + "/uhcadmin forcestartdm : force start deathmatch");
						p.sendMessage(ChatColor.GOLD + "/uhcadmin gotodm : tp to deathmatch world");
						p.sendMessage(ChatColor.GOLD + "/uhcadmin gotoworld : tp to uhc wolrd");
						return true;
					}

					p.sendMessage(ChatColor.DARK_RED + "Invalid usage. use /uhcadmin help for help");
				} else {
					p.sendMessage(ChatColor.DARK_RED + "Invalid usage. use /uhcadmin help for help");
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