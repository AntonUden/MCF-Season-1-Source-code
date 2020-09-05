package xyz.mcfridays.mcf.mcfcore.Log;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class ErrorLogger {
	public static void logException(Exception e, String source) {
		String message = ChatColor.YELLOW + "" + ChatColor.BOLD + "[Warning] " + ChatColor.RESET + ChatColor.DARK_RED + "Caught exception in " + source + "! " + e.getClass().getName() + " " + e.getMessage() + ". See console for more info";

		Bukkit.getServer().broadcast(message, "mcf.errorlog");
		e.printStackTrace();
	}
}