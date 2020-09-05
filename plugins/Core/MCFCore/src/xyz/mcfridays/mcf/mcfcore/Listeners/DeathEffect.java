package xyz.mcfridays.mcf.mcfcore.Listeners;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import xyz.mcfridays.mcf.mcfcore.Utils.ColoredParticle;

public class DeathEffect implements Listener {
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Location l = e.getEntity().getLocation();

		int distance = 30;

		l.add(0, 0.5, 0);

		Random random = new Random();

		for (int i = 0; i < 10; i++) {
			Location l2 = l.clone();

			l2.add((random.nextDouble() - 0.5), (random.nextDouble() - 0.5), (random.nextDouble() - 0.5));

			ColoredParticle.REDSTONE.send(l2, distance, 255, 0, 0);
		}
	}
}