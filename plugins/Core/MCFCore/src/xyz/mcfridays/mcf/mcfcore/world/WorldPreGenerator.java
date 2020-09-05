package xyz.mcfridays.mcf.mcfcore.world;

import org.bukkit.Bukkit;
import org.bukkit.World;

import xyz.mcfridays.mcf.mcfcommons.utils.Callback;
import xyz.mcfridays.mcf.mcfcore.MCFCore;

public class WorldPreGenerator {
	private World world;

	private int taskId;

	private int speed;

	private Callback callback;

	private int chunkX;
	private int chunkZ;

	private int size;

	private int total;
	private int progress;

	public WorldPreGenerator(World world, int size, int speed, Callback callback) {
		this.world = world;
		this.size = size;
		this.speed = speed;
		this.callback = callback;

		this.chunkX = size * -1;
		this.chunkZ = size * -1;

		this.total = size * size * 4;
		this.progress = 0;

		this.taskId = -1;
	}

	public int getTotal() {
		return total;
	}

	public int getProgress() {
		return progress;
	}

	public int getSize() {
		return size;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public void stop() {
		if (taskId != -1) {
			Bukkit.getScheduler().cancelTask(taskId);
		}
	}

	public void start() {
		if (taskId == -1) {
			taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFCore.getInstance(), new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < speed; i++) {
						chunkX++;
						if (chunkX > size) {
							chunkX = size * -1;
							chunkZ++;
							if (chunkZ > size) {
								Bukkit.getScheduler().cancelTask(taskId);
								taskId = -1;
								world.save();

								if (callback != null) {
									callback.execute();
								}

								return;
							}
						}

						progress++;

						world.loadChunk(chunkX, chunkZ);
						world.unloadChunk(chunkX * 16, chunkZ * 16, true);
					}
				}
			}, 20L, 20L);
		}
	}
}