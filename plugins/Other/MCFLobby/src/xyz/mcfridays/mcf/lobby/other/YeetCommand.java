package xyz.mcfridays.mcf.lobby.other;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class YeetCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (sender.isOp()) {
				Player player = (Player) sender;
				for (Player player2 : Bukkit.getServer().getOnlinePlayers()) {
					Vector toPlayer2 = player2.getLocation().toVector().subtract(player.getLocation().toVector());

					Vector direction = player.getLocation().getDirection();

					double dot = toPlayer2.normalize().dot(direction);

					if (player.getLocation().distance(player2.getLocation()) < 30) {
						if (dot > 0.80) {
							player2.setVelocity(direction.multiply(4 - (player.getLocation().distance(player2.getLocation()) / 4)));
						}
					}
				}
			} else {
				sender.sendMessage(ChatColor.RED + "no");
			}
		}
		
		return true;
	}
}