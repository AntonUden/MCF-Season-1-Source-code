package xyz.mcfridays.mcf.mcfcore.Worldborder;

import org.bukkit.Bukkit;
import org.bukkit.World;
import xyz.mcfridays.mcf.mcfcore.MCFCore;

public class WorldBorderShrinkTask {
	private World world;

	@SuppressWarnings("unused")
	private int endSize;

	private int taskId;

	private int stepTime;
	private double stepShrinkValue;
	private int totalSteps;

	private int activeStep;

	private double lastSize;

	private WorldBorderShrinkMode mode;

	/**
	 * @param world      {@link World} to shrink border in
	 * @param endSize    Final world border size
	 * @param shrinkTime Time for world border to shrink
	 * @param stepTime   Time in seconds for each step
	 */
	public WorldBorderShrinkTask(World world, int startSize, int endSize, int shrinkTime, int stepTime, WorldBorderShrinkMode mode) {
		this.world = world;

		this.endSize = endSize;

		this.stepTime = stepTime;

		this.mode = mode;

		this.totalSteps = shrinkTime / stepTime;

		this.stepShrinkValue = (startSize - endSize) / (double) totalSteps;

		this.lastSize = startSize;

		this.taskId = -1;
		this.activeStep = 0;
	}

	public boolean cancel() {
		if (taskId != -1) {
			Bukkit.getScheduler().cancelTask(taskId);
			taskId = -1;
			return true;
		}
		return false;
	}

	public boolean start() {
		if (taskId == -1) {
			taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFCore.getInstance(), new Runnable() {
				@Override
				public void run() {
					if (activeStep >= totalSteps) {
						cancel();

						//setSize(endSize);

						return;
					}

					setSize(lastSize - stepShrinkValue, (long) stepTime);
					lastSize -= stepShrinkValue;

					activeStep++;
				}
			}, 0, stepTime * 20);
			return true;
		}
		return false;
	}
	
	public boolean isRunning() {
		return taskId != -1;
	}

	@SuppressWarnings("unused")
	private void setSize(double size) {
		this.setSize(size, 0);
	}

	private void setSize(double size, long delay) {
		if (mode == WorldBorderShrinkMode.API) {
			world.getWorldBorder().setSize(size, (long) (delay + 0.3));
		} else {
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "worldborder set " + size + (delay == 0 ? "" : " " + (delay + 0.3)));
		}
	}
}