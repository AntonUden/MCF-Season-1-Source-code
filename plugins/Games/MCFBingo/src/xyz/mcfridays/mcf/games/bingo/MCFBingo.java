package xyz.mcfridays.mcf.games.bingo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import xyz.mcfridays.mcf.games.bingo.commands.BingoAdminCommand;
import xyz.mcfridays.mcf.games.bingo.commands.BingoCommand;
import xyz.mcfridays.mcf.games.bingo.commands.TopCommand;
import xyz.mcfridays.mcf.games.bingo.gamemode.BingoGamemode;
import xyz.mcfridays.mcf.games.bingo.gamemode.Stage;
import xyz.mcfridays.mcf.games.bingo.items.BingoBookItem;
import xyz.mcfridays.mcf.games.bingo.listeners.AppleDropListener;
import xyz.mcfridays.mcf.mcfcommons.utils.Callback;
import xyz.mcfridays.mcf.mcfcommons.utils.TimerCallback;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Listeners.PlayerHeadDrop;
import xyz.mcfridays.mcf.mcfcore.Utils.BlockUtils;
import xyz.mcfridays.mcf.mcfcore.timers.ScoreboardTimer;

public class MCFBingo extends JavaPlugin implements Listener {
	private static MCFBingo instance;

	private Location lobbyLocation;
	private Location playerScoreboardLocation;
	private Location teamScoreboardLocation;

	public static MCFBingo getInstance() {
		return instance;
	}

	private BingoGamemode game;

	public BingoGamemode getGame() {
		return game;
	}

	private ScoreboardTimer tpCountdown;

	@Override
	public void onEnable() {
		instance = this;

		saveDefaultConfig();

		this.game = new BingoGamemode(48, 100);

		// Set game
		MCFCore.getInstance().getGameManager().setActiveGame(game, this);
		MCFCore.getInstance().getMcfScoreboardManager().setServerString(ChatColor.YELLOW + "" + ChatColor.BOLD + "Bingo");

		// Locations
		ConfigurationSection ll = getConfig().getConfigurationSection("lobby_location");
		lobbyLocation = new Location(Bukkit.getServer().getWorlds().get(0), BlockUtils.blockCenter(ll.getInt("x")), ll.getInt("y"), BlockUtils.blockCenter(ll.getInt("z")), ll.getInt("yaw"), ll.getInt("pitch"));

		ConfigurationSection psl = getConfig().getConfigurationSection("player_scoreboard");
		playerScoreboardLocation = new Location(Bukkit.getServer().getWorlds().get(0), BlockUtils.blockCenter(psl.getInt("x")), psl.getInt("y"), BlockUtils.blockCenter(psl.getInt("z")));

		ConfigurationSection tsl = getConfig().getConfigurationSection("team_scoreboard");
		teamScoreboardLocation = new Location(Bukkit.getServer().getWorlds().get(0), BlockUtils.blockCenter(tsl.getInt("x")), tsl.getInt("y"), BlockUtils.blockCenter(tsl.getInt("z")));

		// Commands
		this.getCommand("top").setExecutor(new TopCommand());
		this.getCommand("bingo").setExecutor(new BingoCommand());
		this.getCommand("bingoadmin").setExecutor(new BingoAdminCommand());

		// Score board locations
		MCFCore.getInstance().getHoloScoreboardManager().setPlayerHologramLocation(playerScoreboardLocation);
		MCFCore.getInstance().getHoloScoreboardManager().setTeamHologramLocation(teamScoreboardLocation);

		// set lobby
		this.game.setLobbyServer(getConfig().getString("lobby_server"));

		// Events
		Bukkit.getServer().getPluginManager().registerEvents(this, this);

		Bukkit.getServer().getPluginManager().registerEvents(new PlayerHeadDrop(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new AppleDropListener(), this);

		MCFCore.getInstance().getCustomItemManager().add(BingoBookItem.class);
		
		// Update recipe GUI
		MCFCore.getInstance().getCustomRecipeGUI().update();

		MCFCore.getInstance().setActiveMcfPlugin(this);

		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				Bukkit.getServer().getWorlds().get(0).getWorldBorder().setSize(101);
			}
		}, 100L);
	}

	@Override
	public void onDisable() {
		if (tpCountdown != null) {
			tpCountdown.stop();
		}
		HandlerList.unregisterAll((Plugin) this);
		Bukkit.getScheduler().cancelTasks(this);
	}

	public Location getLobbyLocation() {
		return lobbyLocation;
	}

	public Location getTeamScoreboardLocation() {
		return teamScoreboardLocation;
	}

	public Location getPlayerScoreboardLocation() {
		return playerScoreboardLocation;
	}

	public void forcestart() {
		this.start(true);
	}

	public void start() {
		this.start(false);
	}
	
	public void start(boolean skipCountdown) {
		int startTime = (skipCountdown ? 1 : 30);
		
		if (game.getStage() == Stage.WAITING) {
			if (tpCountdown == null) {
				Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Teleporting to arena in 30 seconds");
				tpCountdown = new ScoreboardTimer(startTime, ChatColor.AQUA + "Starting in: ", 9);
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