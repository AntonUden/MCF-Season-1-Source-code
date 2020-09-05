package xyz.mcfridays.mcf.mcfcore.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import xyz.mcfridays.mcf.mcfcore.MCFCore;

public class RecipesCommands implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {

			MCFCore.getInstance().getCustomRecipeGUI().show((Player) sender);
			return true;
		}

		sender.sendMessage(ChatColor.RED + "Only players can use this command");
		return false;
	}
}