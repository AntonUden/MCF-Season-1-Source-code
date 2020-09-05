package xyz.mcfridays.mcf.mcfcore.Commands;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import xyz.mcfridays.mcf.mcfcommons.database.DBConnection;
import xyz.mcfridays.mcf.mcfcore.MCFCore;

public class DatabaseCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender.hasPermission("mcf.database") || sender instanceof ConsoleCommandSender) {
			if (args.length == 0) {
				showHelp(sender);
				return true;
			} else {
				if (args[0].equalsIgnoreCase("help")) {
					showHelp(sender);
					return false;
				}

				if (args[0].equalsIgnoreCase("status")) {
					try {
						boolean connected = DBConnection.isConnected();
						boolean working = DBConnection.testQuery();
						sender.sendMessage(ChatColor.GOLD + "===== Database status =====");
						sender.sendMessage(ChatColor.GOLD + "Connected: " + (connected ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
						sender.sendMessage(ChatColor.GOLD + "Test query: " + (working ? ChatColor.GREEN + "Ok" : ChatColor.RED + "Failure"));
						sender.sendMessage(ChatColor.GOLD + "===========================");
					} catch (Exception e) {
						sender.sendMessage(ChatColor.RED + e.getMessage() + "\n" + e.getStackTrace());
					}
					return true;
				} else if (args[0].equalsIgnoreCase("reconnect")) {
					sender.sendMessage(ChatColor.RED + "Trying to reconnect");
					Bukkit.getServer().broadcastMessage(ChatColor.RED + "The server is reconnectiong to the database. This might prevent score from registering correctly");

					try {
						if (MCFCore.getInstance().reconnectDatabase()) {
							sender.sendMessage(ChatColor.GREEN + "Success");
							Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "Database connection restored");
						} else {
							sender.sendMessage(ChatColor.DARK_RED + "Reconnect failed");
							Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED + "Failed to reconnect. Score registration might not update in real time");
						}
					} catch (SQLException e) {
						sender.sendMessage(ChatColor.DARK_RED + "Database failure: " + e.getMessage() + "\n" + e.getStackTrace());
						Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED + "Failed to reconnect. Score registration might not update in real time");
						e.printStackTrace();
					}
					return true;
				}

				sender.sendMessage(ChatColor.RED + "Invalid useage. type /mcf help for help");
				return true;
			}
		} else {
			sender.sendMessage(ChatColor.DARK_RED + "You don't have permission to use this command");
		}
		return false;
	}

	private void showHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.GOLD + "/database help : show help");
		sender.sendMessage(ChatColor.GOLD + "/database status : show status");
		sender.sendMessage(ChatColor.RED + "/database reconnect : try to reconnect to database. Only for use in an emergency");
	}
}