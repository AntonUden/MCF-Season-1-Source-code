package xyz.mcfridays.mcf.mcfcore.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import xyz.mcfridays.mcf.mcfcore.MCFCore;

public class RespawnPlayerCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender.hasPermission("mcf.respawnplayer")) {
			if (args.length == 1) {
				Player target = Bukkit.getServer().getPlayer(args[0]);

				if (target != null) {
					if (MCFCore.getInstance().getGameManager().hasActiveGame()) {
						if (!MCFCore.getInstance().getGameManager().getActiveGame().getPlayers().contains(target.getUniqueId())) {
							MCFCore.getInstance().getGameManager().getActiveGame().getPlayers().add(target.getUniqueId());
							sender.sendMessage(ChatColor.GREEN + "OK");
						} else {
							sender.sendMessage(ChatColor.RED + "That player is already in the game");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "No active game");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Could not find player named " + args[0]);
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Use /respawnplayer <Player>");
			}
		}
		return false;
	}
}