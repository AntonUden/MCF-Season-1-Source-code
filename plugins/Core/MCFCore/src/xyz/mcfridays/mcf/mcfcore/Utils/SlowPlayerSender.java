package xyz.mcfridays.mcf.mcfcore.Utils;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import xyz.mcfridays.mcf.mcfcommons.utils.Callback;
import xyz.mcfridays.mcf.mcfcore.MCFCore;

public class SlowPlayerSender {
	private int taskId;
	private int index;
	private int delay;

	private Callback callback;

	private String server;

	private Player[] players;

	public SlowPlayerSender(Collection<? extends Player> collection, String server) {
		this(collection, server, 4);
	}

	public SlowPlayerSender(Collection<? extends Player> players, String server, int delay) {
		this.players = new Player[players.size()];
		int i = 0;
		for (Player player : players) {
			this.players[i] = player;
			i++;
		}

		if (server == null) {
			throw new IllegalArgumentException("server is null");
		}

		this.server = server;

		this.delay = delay;
		this.index = 0;
		this.taskId = -1;
	}

	public SlowPlayerSender setCallback(Callback callback) {
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
							BungeecordUtils.sendToServer(p, server);
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