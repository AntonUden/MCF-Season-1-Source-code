package xyz.mcfridays.mcf.mcfcore.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;

public class DisableAchievements implements Listener {
	@EventHandler
	public void onPlayerAchievementAwarded(PlayerAchievementAwardedEvent e) {
		e.setCancelled(true);
	}
}