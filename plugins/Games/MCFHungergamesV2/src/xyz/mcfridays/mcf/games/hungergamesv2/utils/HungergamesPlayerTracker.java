package xyz.mcfridays.mcf.games.hungergamesv2.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import xyz.mcfridays.mcf.games.hungergamesv2.MCFHungergames;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.TrackerCompass.CompassTracker;

public class HungergamesPlayerTracker implements CompassTracker {
	@Override
	public Player getCompassTarget(Player player) {
		Player closest = null;
		double closestDistance = Double.MAX_VALUE;
		for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
			if (p2.getUniqueId() == player.getUniqueId()) {
				continue;
			}
			
			if(!player.getWorld().getUID().toString().equalsIgnoreCase(p2.getWorld().getUID().toString())) {
				return null;
			}

			if (MCFHungergames.getInstance().getGame() != null) {
				if (MCFHungergames.getInstance().getGame().getPlayers().contains(p2.getUniqueId())) {
					if (!MCFCore.getInstance().getTeamManager().isPlayerInTeamWith(player, p2)) {
						double dist = player.getLocation().distance(p2.getLocation());

						if (closestDistance > dist) {
							closestDistance = dist;
							closest = p2;
						}
					}
				}
			}
		}

		return closest;
	}
}