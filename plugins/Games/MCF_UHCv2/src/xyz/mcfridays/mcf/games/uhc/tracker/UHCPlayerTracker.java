package xyz.mcfridays.mcf.games.uhc.tracker;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import xyz.mcfridays.mcf.mcfcore.Teams.Team;
import xyz.mcfridays.mcf.mcfcore.TrackerCompass.CompassTracker;
import xyz.mcfridays.mcf.mcfcore.TrackerCompass.CustomTrackerMessage;

public class UHCPlayerTracker implements CompassTracker, CustomTrackerMessage {
	@Override
	public Player getCompassTarget(Player player) {
		Team team = Team.getPlayerTeam(player);
		if (team == null) {
			return null;
		}

		ArrayList<Player> targets = new ArrayList<Player>();

		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			if(!player.getWorld().getUID().toString().equalsIgnoreCase(p.getWorld().getUID().toString())) {
				return null;
			}
			if (!team.isMember(player)) {
				targets.add(p);
			}
		}

		Player closest = null;
		double closestDistance = Double.MAX_VALUE;
		for (Player p2 : targets) {
			if (p2.getUniqueId() == player.getUniqueId()) {
				continue;
			}

			if (player.getWorld().getUID() != p2.getWorld().getUID()) {
				continue;
			}

			double dist = player.getLocation().distance(p2.getLocation());

			if (closestDistance > dist) {
				closestDistance = dist;
				closest = p2;
			}
		}

		return closest;
	}

	@Override
	public String getMessage(Player user, Player target) {
		return ChatColor.GREEN + "Tracking your teammate " + target.getName() + ". Distance: " + Math.round(user.getLocation().distance(target.getLocation()));
	}

	@Override
	public String getNoTargetMessage(Player player) {
		return ChatColor.RED + "Could not find a teammate to track";
	}
}