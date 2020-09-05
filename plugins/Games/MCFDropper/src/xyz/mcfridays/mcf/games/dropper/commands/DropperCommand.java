package xyz.mcfridays.mcf.games.dropper.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import xyz.mcfridays.mcf.games.dropper.MCFDropper;

public class DropperCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (p.hasPermission("mcf.dropper.admin")) {
				if (args.length != 0) {
					if (args[0].equalsIgnoreCase("start")) {
						MCFDropper.getInstance().start();
						return true;
					} else if (args[0].equalsIgnoreCase("help")) {
						p.sendMessage(ChatColor.GOLD + "/dropper help : show help");
						p.sendMessage(ChatColor.GOLD + "/dropper start : start game");
						return true;
					}

					p.sendMessage(ChatColor.DARK_RED + "Invalid usage. use /dropper help for help");
				} else {
					p.sendMessage(ChatColor.DARK_RED + "Invalid usage. use /dropper help for help");
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