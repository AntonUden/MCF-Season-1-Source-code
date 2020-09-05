package xyz.mcfridays.mcf.mcfcore.timers;

import org.bukkit.Bukkit;

import xyz.mcfridays.mcf.mcfcommons.utils.Callback;
import xyz.mcfridays.mcf.mcfcore.MCFCore;

public class DelayedTask implements Timer {
	private int ticks;
	private int taskId;

	private Callback callback;

	private boolean finished;

	public DelayedTask(int ticks) {
		this.ticks = ticks;

		this.taskId = -1;
		this.finished = false;
	}

	public boolean isFinished() {
		return finished;
	}

	@Override
	public boolean start() {
		if (taskId == -1) {
			taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(MCFCore.getInstance(), new Runnable() {
				@Override
				public void run() {
					if (hasCallback()) {
						getCallback().execute();
					}
					finished = true;
					taskId = -1;
				}
			}, ticks);
		}
		return false;
	}

	@Override
	public boolean stop() {
		if (taskId == -1) {
			return false;
		}

		Bukkit.getScheduler().cancelTask(taskId);
		finished = true;
		taskId = -1;
		return true;
	}

	@Override
	public DelayedTask setCallback(Callback callback) {
		this.callback = callback;
		return this;
	}

	public Callback getCallback() {
		return callback;
	}

	@Override
	public boolean hasCallback() {
		return callback != null;
	}
}