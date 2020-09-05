package xyz.mcfridays.mcf.mcfcore.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import xyz.mcfridays.mcf.mcfcommons.database.DBConnection;
import xyz.mcfridays.mcf.mcfcore.Utils.BungeecordUtils;

public class BackCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			String server = DBConnection.getActiveServer();

			if (server != null) {
				Player player = (Player) sender;
				sender.sendMessage(ChatColor.GREEN + "Trying to connect you to " + server + "...");
				BungeecordUtils.sendToServer(player, server);
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "No active game to reconnect to");
			}

		} else {
			sender.sendMessage(ChatColor.RED + "This command is only for players");
		}
		return false;
	}
}