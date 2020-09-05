package xyz.mcfridays.mcf.lobby.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class AutoRespawn implements Listener {
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		e.getEntity().spigot().respawn();
	}
}