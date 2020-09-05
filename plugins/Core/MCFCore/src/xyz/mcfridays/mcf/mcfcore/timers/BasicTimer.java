package xyz.mcfridays.mcf.mcfcore.timers;

import org.bukkit.Bukkit;

import xyz.mcfridays.mcf.mcfcommons.utils.Callback;
import xyz.mcfridays.mcf.mcfcommons.utils.TimerCallback;
import xyz.mcfridays.mcf.mcfcore.MCFCore;

public class BasicTimer implements Timer, CountdownTimer {
	private int seconds;
	private int taskId;

	private boolean finished;

	private Callback callback;
	private TimerCallback tickCallback;

	public BasicTimer(int seconds) {
		this.seconds = seconds;

		this.finished = false;
		this.callback = null;
		this.taskId = -1;
	}

	public Timer setCallback(Callback callback) {
		this.callback = callback;
		return this;
	}

	public Callback getCallback() {
		return callback;
	}

	public boolean hasCallback() {
		return callback != null;
	}

	public Timer setTickCallback(TimerCallback tickCallback) {
		this.tickCallback = tickCallback;
		return this;
	}

	public TimerCallback getTimerCallback() {
		return tickCallback;
	}

	public boolean hasTimerCallback() {
		return tickCallback != null;
	}

	public int getTaskId() {
		return taskId;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	public int getSeconds() {
		return seconds;
	}

	public boolean stop() {
		if (taskId == -1) {
			return false;
		}

		Bukkit.getScheduler().cancelTask(taskId);
		taskId = -1;

		return true;
	}

	public boolean start() {
		if (taskId != -1) {
			return false;
		}

		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFCore.getInstance(), new Runnable() {
			@Override
			public void run() {
				seconds--;

				if (hasTimerCallback()) {
					tickCallback.execute(seconds);
				}

				if (seconds <= 0) {
					stop();

					finished = true;

					if (hasCallback()) {
						callback.execute();
					}
				}
			}
		}, 20L, 20L);
		return true;
	}
}