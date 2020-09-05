package xyz.mcfridays.mcf.mcfcore.TrackerCompass;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import xyz.mcfridays.mcf.mcfcore.MCFCore;

public class TrackerCompassManager implements Listener {
	private CompassTracker compassTracker;
	private CustomTrackerMessage customTrackerMessage;
	private int taskId;

	public TrackerCompassManager() {
		this.compassTracker = null;
		this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFCore.getInstance(), new Runnable() {
			@Override
			public void run() {
				if (hasTracker()) {
					for (Player player : Bukkit.getOnlinePlayers()) {
						Player target = getTracker().getCompassTarget(player);
						Location location = null;

						if (target == null) {
							location = new Location(player.getWorld(), 10000, 0, 0);
						} else {
							location = target.getLocation();
						}

						player.setCompassTarget(location);
					}
				}
			}
		}, 4L, 4L);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (e.getPlayer().getItemInHand().getType() == Material.COMPASS) {
			if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (hasTracker()) {
					Player target = getTracker().getCompassTarget(p);

					if (target != null) {
						if (hasCustomTrackerMessage()) {
							p.sendMessage(getCustomTrackerMessage().getMessage(p, target));
							return;
						}
						p.sendMessage(ChatColor.GREEN + "Tracking " + target.getName());
						return;
					}
				}
				if (hasCustomTrackerMessage()) {
					p.sendMessage(getCustomTrackerMessage().getNoTargetMessage(p));
					return;
				}
				p.sendMessage(ChatColor.RED + "No targets to track");
			}
		}
	}

	public CustomTrackerMessage getCustomTrackerMessage() {
		return customTrackerMessage;
	}

	public void setCustomTrackerMessage(CustomTrackerMessage customTrackerMessage) {
		this.customTrackerMessage = customTrackerMessage;
	}

	public boolean hasCustomTrackerMessage() {
		return customTrackerMessage != null;
	}

	public void stop() {
		if (taskId != -1) {
			Bukkit.getScheduler().cancelTask(taskId);
		}
	}

	public CompassTracker getTracker() {
		return compassTracker;
	}

	public void setTracker(CompassTracker compassTracker) {
		this.compassTracker = compassTracker;
		if (compassTracker != null) {
			if (compassTracker instanceof CustomTrackerMessage) {
				this.setCustomTrackerMessage((CustomTrackerMessage) compassTracker);
			}
		}
	}

	public boolean hasTracker() {
		return compassTracker != null;
	}
}