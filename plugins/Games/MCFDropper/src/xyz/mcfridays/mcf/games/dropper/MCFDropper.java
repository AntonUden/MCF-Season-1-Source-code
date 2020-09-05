package xyz.mcfridays.mcf.games.dropper;

import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import xyz.mcfridays.mcf.games.dropper.Gamemode.DropperGamemode;
import xyz.mcfridays.mcf.games.dropper.Gamemode.DropperLoader;
import xyz.mcfridays.mcf.games.dropper.Gamemode.Stage;
import xyz.mcfridays.mcf.games.dropper.commands.DropperCommand;
import xyz.mcfridays.mcf.mcfcommons.utils.Callback;
import xyz.mcfridays.mcf.mcfcommons.utils.TimerCallback;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Listeners.DisableAchievements;
import xyz.mcfridays.mcf.mcfcore.timers.ScoreboardTimer;

public class MCFDropper extends JavaPlugin implements Listener {
	private static MCFDropper instance;

	private DropperGamemode game;

	private ScoreboardTimer tpCountdown;

	public static MCFDropper getInstance() {
		return instance;
	}

	public DropperGamemode getDropperGamemode() {
		return game;
	}

	@Override
	public void onEnable() {
		instance = this;

		saveDefaultConfig();

		game = DropperLoader.load(new File(getDataFolder().getAbsoluteFile() + "/arena.json"));
		if (game == null) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Dropper]: Failed to load arena. Disabling...");
			Bukkit.getServer().getPluginManager().disablePlugin(this);
			return;
		}

		MCFCore.getInstance().getGameManager().setActiveGame(game, this);

		getCommand("dropper").setExecutor(new DropperCommand());

		game.setLobbyServer("lobby");

		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		
		Bukkit.getServer().getPluginManager().registerEvents(new DisableAchievements(), this);

		MCFCore.getInstance().getMcfScoreboardManager().setServerString(ChatColor.YELLOW + "" + org.bukkit.ChatColor.BOLD + "Dropper");

		MCFCore.getInstance().setActiveMcfPlugin(this);
	}
	
	@Override
	public void onDisable() {
		MCFCore.getInstance().getGameManager().disable();
		HandlerList.unregisterAll((Plugin) this);
		Bukkit.getScheduler().cancelTasks(this);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();

		if (p.getLocation().getY() < -2) {
			p.setHealth(0);
		}
	}

	public void start() {
		if (game.getArenaStage() == Stage.WAITING) {
			if (tpCountdown == null) {
				Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Teleporting to arena in 30 seconds");
				tpCountdown = new ScoreboardTimer(30, ChatColor.AQUA + "Starting in: ", 9);
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					p.playSound(p.getLocation(), Sound.NOTE_PLING, 1F, 1F);
				}

				tpCountdown.setCallback(new Callback() {
					@Override
					public void execute() {
						Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Teleporting to arena");
						game.start();
					}
				});
				tpCountdown.setTickCallback(new TimerCallback() {
					@Override
					public void execute(int timeLeft) {
						if (timeLeft <= 0 || timeLeft > 5) {
							return;
						}
						Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Teleporting to arena in " + ChatColor.AQUA + ChatColor.BOLD + timeLeft);
						for (Player p : Bukkit.getServer().getOnlinePlayers()) {
							p.playSound(p.getLocation(), Sound.NOTE_PLING, 1F, 1F);
						}
					}
				});
				tpCountdown.start();
			}
		}
	}
}