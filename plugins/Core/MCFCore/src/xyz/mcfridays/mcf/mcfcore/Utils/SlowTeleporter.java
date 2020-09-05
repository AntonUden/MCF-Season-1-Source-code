package xyz.mcfridays.mcf.mcfcore.Utils;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import xyz.mcfridays.mcf.mcfcommons.utils.Callback;
import xyz.mcfridays.mcf.mcfcore.MCFCore;

public class SlowTeleporter {
	private int taskId;
	private int index;
	private int delay;

	private Callback callback;

	private Location location;

	private Player[] players;

	public SlowTeleporter(Collection<? extends Player> players, Location location) {
		this(players, location, 4);
	}

	public SlowTeleporter(Collection<? extends Player> players, Location location, int delay) {
		this.players = new Player[players.size()];
		int i = 0;
		for (Player player : players) {
			this.players[i] = player;
			i++;
		}

		this.location = location;

		this.delay = delay;
		this.index = 0;
		this.taskId = -1;
	}

	public SlowTeleporter setCallback(Callback callback) {
		this.callback = callback;
		return this;
	}

	public int start() {
		if (this.taskId == -1) {
			this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFCore.getInstance(), new Runnable() {
				@Override
				public void run() {
					if (index < players.length) {
						Player p = players[index];

						if (p.isOnline()) {
							p.teleport(location);
						}
					}

					index++;

					if (index >= players.length) {
						Bukkit.getScheduler().cancelTask(taskId);
						if (callback != null) {
							callback.execute();
						}
					}
				}
			}, this.delay, this.delay);
		}

		return this.players.length * this.delay;
	}
}