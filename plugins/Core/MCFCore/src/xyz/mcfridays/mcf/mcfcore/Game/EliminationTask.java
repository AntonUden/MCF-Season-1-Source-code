package xyz.mcfridays.mcf.mcfcore.Game;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import xyz.mcfridays.mcf.mcfcommons.utils.UUIDCallback;
import xyz.mcfridays.mcf.mcfcore.MCFCore;

public class EliminationTask {
	private UUID uuid;
	private String username;
	private int taskId;
	private int timeLeft;

	private UUIDCallback eliminationCallback;

	public EliminationTask(UUID uuid, String username, int time, UUIDCallback eliminationCallback) {
		this.uuid = uuid;
		this.username = username;
		this.timeLeft = time;
		this.eliminationCallback = eliminationCallback;

		this.taskId = -2;
		
		startTask();
	}
	
	private void startTask() {
		if(taskId != -2) {
			return;
		}
		this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFCore.getInstance(), new Runnable() {
			@Override
			public void run() {
				timeLeft--;

				if (timeLeft == 60) {
					Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + username + ChatColor.RED + ChatColor.BOLD + " will be eliminated in 1 minute");
				} else if (timeLeft == 30) {
					Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + username + ChatColor.RED + ChatColor.BOLD + " will be eliminated in 30 seconds");
				} else if (timeLeft == 0) {
					eliminationCallback.execute(uuid);
					stop();
				}
			}
		}, 20L, 20L);
	}

	private void stop() {
		if (taskId != -1) {
			Bukkit.getScheduler().cancelTask(taskId);
			this.taskId = -1;
		}
	}

	public void cancel() {
		stop();
	}
}