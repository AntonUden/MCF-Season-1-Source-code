package xyz.mcfridays.mcf.mcfcore.timers;

import org.bukkit.Bukkit;

import xyz.mcfridays.mcf.mcfcommons.utils.Callback;
import xyz.mcfridays.mcf.mcfcommons.utils.TimerCallback;
import xyz.mcfridays.mcf.mcfcore.MCFCore;

public class ScoreboardTimer implements Timer {
	private int seconds;
	private int taskId;
	private int line;

	private String countdownName;

	private boolean finished;

	private Callback callback;
	private TimerCallback tickCallback;

	public ScoreboardTimer(int seconds, String name, int line) {
		this.seconds = seconds;
		this.countdownName = name;
		this.line = line;

		this.finished = false;
		this.callback = null;
		this.taskId = -1;
	}

	public ScoreboardTimer setCallback(Callback callback) {
		this.callback = callback;
		return this;
	}

	public Callback getCallback() {
		return callback;
	}

	public boolean hasCallback() {
		return callback != null;
	}

	public ScoreboardTimer setTickCallback(TimerCallback tickCallback) {
		this.tickCallback = tickCallback;
		return this;
	}

	public TimerCallback getTickCallback() {
		return tickCallback;
	}

	public boolean hasTickCallback() {
		return tickCallback != null;
	}

	public int getTaskId() {
		return taskId;
	}

	public boolean isFinished() {
		return finished;
	}

	public boolean stop() {
		if (taskId == -1) {
			return false;
		}

		Bukkit.getScheduler().cancelTask(taskId);
		taskId = -1;

		MCFCore.getInstance().getMcfScoreboardManager().deleteCustomLine(line);

		return true;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	public int getSeconds() {
		return seconds;
	}

	public boolean start() {
		if (taskId != -1) {
			return false;
		}

		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFCore.getInstance(), new Runnable() {
			@Override
			public void run() {
				seconds--;

				MCFCore.getInstance().getMcfScoreboardManager().setCustomLine(line, countdownName + String.format("%02d:%02d", seconds / 60, seconds % 60));

				if (hasTickCallback()) {
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