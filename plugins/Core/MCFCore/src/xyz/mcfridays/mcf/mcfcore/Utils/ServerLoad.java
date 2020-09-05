package xyz.mcfridays.mcf.mcfcore.Utils;

import org.bukkit.Bukkit;

import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import xyz.mcfridays.mcf.mcfcore.MCFCore;

public class ServerLoad {
	private double load;

	private int schedulerId;

	public ServerLoad() {
		this.load = 0;

		this.schedulerId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFCore.getInstance(), new Runnable() {
			@Override
			public void run() {
				load = MathHelper.a(MinecraftServer.getServer().h) * 1.0E-6D;
				;
			}
		}, 10L, 10L);
	}

	public void stop() {
		if (schedulerId != -1) {
			Bukkit.getScheduler().cancelTask(schedulerId);
			schedulerId = -1;
		}
	}

	public double getLoad() {
		return load;
	}

	public int getLoadPercentage() {
		return (int) Math.round(load);
	}
}