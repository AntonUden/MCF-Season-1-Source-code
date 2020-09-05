package xyz.mcfridays.mcf.mcfcore.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InvseeCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (sender.hasPermission("mcf.invsee")) {
				Player player = (Player) sender;

				if(args.length == 1) {
					Player target = Bukkit.getServer().getPlayer(args[0]);
					
					if(target != null) {
						player.openInventory(target.getInventory());
					} else {
						player.sendMessage(ChatColor.RED + "Could not find player named " + args[0]);	
					}
				} else {
					player.sendMessage(ChatColor.RED + "Use /invsee <Player>");
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "This command is only for players");
		}
		return false;
	}
}