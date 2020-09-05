package xyz.mcfridays.mcf.lobby.Listeners;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import xyz.mcfridays.mcf.mcfcore.Utils.BlockUtils;

public class KOTL implements Listener {
	private int x;
	private int z;

	private int topY;

	private double radius;

	private World world;
	
	public KOTL(int x, int z, int topY, double radius, World world) {
		this.x = x;
		this.z = z;

		this.topY = topY;

		this.radius = radius;

		this.world = world;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	public int getTopY() {
		return topY;
	}

	public double getRadius() {
		return radius;
	}

	public World getWorld() {
		return world;
	}

	public boolean inArena(Location location) {
		Location arenaLocation = new Location(world, BlockUtils.blockCenter(x), location.getY(), BlockUtils.blockCenter(z));

		double dist = arenaLocation.distance(location);

		return dist <= radius;
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player player = (Player) e.getEntity();
			if (inArena(player.getLocation())) {
				e.setCancelled(false);
				e.setDamage(0);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if(e.getDamager() instanceof FishHook) {
			e.setCancelled(true);
			return;
		}
		
		if (e.getEntity() instanceof Player) {
			if (e.getDamager() instanceof Player) {
				Player player = (Player) e.getEntity();
				if (inArena(player.getLocation())) {
					Player damager = (Player) e.getDamager();
					if (inArena(damager.getLocation())) {
						e.setCancelled(false);
						e.setDamage(0);
					}
				}
			}
		}
	}
}