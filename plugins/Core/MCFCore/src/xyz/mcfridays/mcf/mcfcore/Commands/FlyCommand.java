package xyz.mcfridays.mcf.mcfcore.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (sender.hasPermission("mcf.fly")) {
				Player p = (Player) sender;

				boolean newState = !p.isFlying();

				if (newState) {
					p.setAllowFlight(newState);
				}
				p.setFlying(newState);
				if (!newState) {
					p.setAllowFlight(newState);
				}

				p.sendMessage(ChatColor.GOLD + "Flight " + (newState ? "enabled" : "disabled"));
			}
		} else {
			sender.sendMessage(ChatColor.RED + "This command is only for players");
		}
		return false;
	}
}