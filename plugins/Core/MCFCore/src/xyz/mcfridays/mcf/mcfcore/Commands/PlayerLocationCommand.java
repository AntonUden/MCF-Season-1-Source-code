package xyz.mcfridays.mcf.mcfcore.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class PlayerLocationCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender.hasPermission("mcf.playerlocation") || sender instanceof ConsoleCommandSender) {
			for(Player p : Bukkit.getServer().getOnlinePlayers()) {
				sender.sendMessage(p.getUniqueId().toString() + " (" + p.getName() + ") gamemode: " + p.getGameMode() + " world: " + p.getLocation().getWorld().getName() + " location: X: " + p.getLocation().getX() + " Y: " +p.getLocation().getY() + " Z: " + p.getLocation().getZ() + " Yaw: " + p.getLocation().getY() + " Pitch: " + p.getPlayer().getLocation().getPitch()); 
			}
			return true;
		} else {
			sender.sendMessage(ChatColor.RED + "You don't have permission to use this command");
		}
		return false;
	}
}