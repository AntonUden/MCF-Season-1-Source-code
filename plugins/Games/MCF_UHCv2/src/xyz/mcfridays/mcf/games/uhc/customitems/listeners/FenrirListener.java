package xyz.mcfridays.mcf.games.uhc.customitems.listeners;

import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Teams.Team;

public class FenrirListener implements Listener {
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Wolf) {
			Wolf wolf = (Wolf) e.getEntity();

			AnimalTamer tamer = wolf.getOwner();

			if (tamer instanceof Player) {
				Player owner = (Player) tamer;
				Team team = MCFCore.getInstance().getTeamManager().getPlayerTeam(owner);

				if (e.getDamager() instanceof Player) {
					Player p = (Player) e.getDamager();
					Team playerTeam = MCFCore.getInstance().getTeamManager().getPlayerTeam(p);
					if (playerTeam != null) {
						if (team.getTeamNumber() == playerTeam.getTeamNumber()) {
							e.setCancelled(true);
						}
					}
				}
			}
		}
	}
}