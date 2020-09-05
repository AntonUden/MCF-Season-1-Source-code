package xyz.mcfridays.mcf.lobby.other;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import xyz.mcfridays.mcf.lobby.MCFLobby;
import xyz.mcfridays.mcf.mcfcore.Utils.RandomFireworkEffect;
import xyz.mcfridays.mcf.mcfcore.Utils.RandomGenerator;

public class FireworksCommand implements CommandExecutor {
	private int taskId;
	private int count;

	private int minX, maxX, y, minZ, maxZ;

	public FireworksCommand() {
		taskId = -1;
		count = -1;

		y = 28;

		minX = 4;
		maxX = 64;

		minZ = 4;
		maxZ = 80;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender.isOp() || sender.hasPermission("mcf.fireworks")) {
			if (taskId == -1) {
				count = 60;
				taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFLobby.getInstance(), new Runnable() {
					public void run() {
						count--;

						for (int i = 0; i < 10; i++) {
							fireRandom();
						}

						if (count <= 0) {
							Bukkit.getScheduler().cancelTask(taskId);
							taskId = -1;
							for (int i = 0; i < 40; i++) {
								fireRandom();
							}
						}
					}
				}, 10L, 10L);
			}
		}
		return false;
	}

	public void fireRandom() {
		World w = Bukkit.getServer().getWorlds().get(0);

		Location l = new Location(w, RandomGenerator.generate(minX, maxX), y, RandomGenerator.generate(minZ, maxZ));

		Firework fw = (Firework) l.getWorld().spawnEntity(l, EntityType.FIREWORK);
		FireworkMeta fwm = fw.getFireworkMeta();

		fwm.setPower(2);
		fwm.addEffect(RandomFireworkEffect.randomFireworkEffect());

		fw.setFireworkMeta(fwm);
	}
}