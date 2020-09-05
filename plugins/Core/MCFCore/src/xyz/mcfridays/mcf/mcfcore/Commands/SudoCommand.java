package xyz.mcfridays.mcf.mcfcore.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class SudoCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender.hasPermission("mcf.sudo.use") || sender instanceof ConsoleCommandSender) {
			if (args.length > 1) {
				Player target = Bukkit.getServer().getPlayer(args[0]);

				if (target != null) {
					if (target.isOnline()) {
						if (target.hasPermission("mcf.sudo.exempt")) {
							sender.sendMessage(ChatColor.RED + "You can't you use sudo on that user");
						} else {
							String content = "";
							for (int i = 1; i < args.length; i++) {
								content += args[i] + " ";
							}

							target.chat(content);

							return true;
						}
					} else {
						sender.sendMessage(ChatColor.RED + target.getName() + " is not online");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Could not find player named " + args[0]);
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Usage /sudo <Player> <Command or chat message>");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "You don't have permission to use this command");
		}
		return false;
	}
}