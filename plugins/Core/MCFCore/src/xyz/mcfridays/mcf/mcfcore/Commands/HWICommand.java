package xyz.mcfridays.mcf.mcfcore.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class HWICommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		sender.sendMessage(ChatColor.GOLD + "AvailableProcessors: " + Runtime.getRuntime().availableProcessors());
		sender.sendMessage(ChatColor.GOLD + "FreeMemory: " + Runtime.getRuntime().freeMemory());
		sender.sendMessage(ChatColor.GOLD + "MaxMemory: " + Runtime.getRuntime().maxMemory());
		sender.sendMessage(ChatColor.GOLD + "os.arch: " + System.getProperty("os.arch"));
		sender.sendMessage(ChatColor.GOLD + "java.version: " + System.getProperty("java.version"));
		sender.sendMessage(ChatColor.GOLD + "os.name: " + System.getProperty("os.name"));
		sender.sendMessage(ChatColor.GOLD + "os.version: " + System.getProperty("os.version"));

		return false;
	}
}