package xyz.mcfridays.mcf.games.hungergamesv2.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import xyz.mcfridays.mcf.games.hungergamesv2.MCFHungergames;

public class HGDebugCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (p.hasPermission("mcf.hungergames.debug")) {
				if (args.length != 0) {
					if (args[0].equalsIgnoreCase("help")) {
						p.sendMessage(ChatColor.GOLD + "/hgdebug help : show help");
						p.sendMessage(ChatColor.GOLD + "/hgdebug cage_on : place start cage");
						p.sendMessage(ChatColor.GOLD + "/hgdebug cage_off : remove start cage");
					} else if (args[0].equalsIgnoreCase("cage_on")) {
						if (MCFHungergames.getInstance().hasActiveMap()) {
							p.sendMessage(MCFHungergames.getInstance().getGame().setCages(true) ? ChatColor.GREEN + "ok" : ChatColor.RED + "failed");
							return true;
						} else {
							p.sendMessage(ChatColor.RED + "No map");
							return true;
						}
					} else if (args[0].equalsIgnoreCase("cage_off")) {
						if (MCFHungergames.getInstance().hasActiveMap()) {
							p.sendMessage(MCFHungergames.getInstance().getGame().setCages(false) ? ChatColor.GREEN + "ok" : ChatColor.RED + "failed");
							return true;
						} else {
							p.sendMessage(ChatColor.RED + "No map");
							return true;
						}
					}
					p.sendMessage(ChatColor.DARK_RED + "Invalid usage. use /hgdebug help for help");
				} else {
					p.sendMessage(ChatColor.DARK_RED + "Invalid usage. use /hgdebug help for help");
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