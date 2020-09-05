package xyz.mcfridays.mcf.games.hungergamesv2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.badlion.timers.api.Timer;
import net.badlion.timers.api.TimerApi;
import xyz.mcfridays.mcf.games.hungergamesv2.Commands.HGAdminCommand;
import xyz.mcfridays.mcf.games.hungergamesv2.Commands.HGDebugCommand;
import xyz.mcfridays.mcf.games.hungergamesv2.Gamemode.HungergamesGamemode;
import xyz.mcfridays.mcf.games.hungergamesv2.Map.HungergamesMap;
import xyz.mcfridays.mcf.games.hungergamesv2.Map.HungergamesMapData;
import xyz.mcfridays.mcf.games.hungergamesv2.Map.MapLoader;
import xyz.mcfridays.mcf.games.hungergamesv2.Map.MapSelector.MapVoteSystem;
import xyz.mcfridays.mcf.games.hungergamesv2.utils.HungergamesPlayerTracker;
import xyz.mcfridays.mcf.mcfcommons.utils.Callback;
import xyz.mcfridays.mcf.mcfcommons.utils.TimerCallback;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Listeners.DeathEffect;
import xyz.mcfridays.mcf.mcfcore.Listeners.DisableAchievements;
import xyz.mcfridays.mcf.mcfcore.Listeners.NoEnderPearlDamage;
import xyz.mcfridays.mcf.mcfcore.Listeners.PlayerHeadDrop;
import xyz.mcfridays.mcf.mcfcore.Utils.BlockUtils;
import xyz.mcfridays.mcf.mcfcore.timers.ScoreboardTimer;

public class MCFHungergames extends JavaPlugin implements Listener {
	private static MCFHungergames instance;

	private HungergamesGamemode game;

	private MapVoteSystem mapVoteSystem;

	private ArrayList<HungergamesMapData> availableMaps;
	private HungergamesMap activeMap;

	private Location lobbyLocation;
	private Location playerScoreboardLocation;
	private Location teamScoreboardLocation;

	private TimerApi timerApi;

	private Timer tpToArenaTimer;

	private int startCountdown;
	private int tpCountdown;
	private boolean started;

	private ScoreboardTimer startTimer;

	private String lobbyServer;

	public static MCFHungergames getInstance() {
		return instance;
	}

	public static MCFCore getMCFCore() {
		return MCFCore.getInstance();
	}

	/**
	 * @return Instance of bad lion {@link TimerApi}
	 */
	public TimerApi getTimerApi() {
		return timerApi;
	}

	@Override
	public void onEnable() {
		this.started = false;
		this.tpCountdown = 60;

		this.timerApi = TimerApi.getInstance();

		instance = this;
		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}

		saveDefaultConfig();

		File lootTableFolder = new File(getDataFolder().getAbsolutePath() + "/loot_tables");
		if (lootTableFolder.exists()) {
			lootTableFolder.mkdirs();
		}

		File mapsFolder = new File(getDataFolder().getAbsolutePath() + "/maps");
		if (mapsFolder.exists()) {
			mapsFolder.mkdirs();
		}

		this.tpToArenaTimer = null;

		this.activeMap = null;
		this.availableMaps = new ArrayList<HungergamesMapData>();

		try {
			MCFCore.getInstance().getLootTableManager().loadAll(lootTableFolder);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			availableMaps = MapLoader.load(mapsFolder);
		} catch (Exception e) {
			e.printStackTrace();
		}

		MCFCore.getInstance().enableChestLoot();

		MCFCore.getInstance().getHoloScoreboardManager().setLines(5);

		MCFCore.getInstance().getTrackerCompassManager().setTracker(new HungergamesPlayerTracker());

		MCFCore.getInstance().getMcfScoreboardManager().setServerString(ChatColor.YELLOW + "" + ChatColor.BOLD + "Hunger Games");

		mapVoteSystem = new MapVoteSystem();

		this.game = new HungergamesGamemode(Bukkit.getWorlds().get(0));

		MCFCore.getInstance().getGameManager().setActiveGame(game, this);

		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getServer().getPluginManager().registerEvents(new DisableAchievements(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new DeathEffect(), this);
		Bukkit.getServer().getPluginManager().registerEvents(mapVoteSystem, this);
		Bukkit.getServer().getPluginManager().registerEvents(new NoEnderPearlDamage(), this);

		this.getCommand("hgadmin").setExecutor(new HGAdminCommand());
		this.getCommand("hgdebug").setExecutor(new HGDebugCommand());

		ConfigurationSection ll = getConfig().getConfigurationSection("lobby_location");
		lobbyLocation = new Location(Bukkit.getServer().getWorlds().get(0), BlockUtils.blockCenter(ll.getInt("x")), ll.getInt("y"), BlockUtils.blockCenter(ll.getInt("z")), ll.getInt("yaw"), ll.getInt("pitch"));

		ConfigurationSection psl = getConfig().getConfigurationSection("player_scoreboard");
		playerScoreboardLocation = new Location(Bukkit.getServer().getWorlds().get(0), BlockUtils.blockCenter(psl.getInt("x")), psl.getInt("y"), BlockUtils.blockCenter(psl.getInt("z")));

		ConfigurationSection tsl = getConfig().getConfigurationSection("team_scoreboard");
		teamScoreboardLocation = new Location(Bukkit.getServer().getWorlds().get(0), BlockUtils.blockCenter(tsl.getInt("x")), tsl.getInt("y"), BlockUtils.blockCenter(tsl.getInt("z")));

		MCFCore.getInstance().getHoloScoreboardManager().setPlayerHologramLocation(playerScoreboardLocation);
		MCFCore.getInstance().getHoloScoreboardManager().setTeamHologramLocation(teamScoreboardLocation);

		this.lobbyServer = getConfig().getString("lobby_server");

		this.game.setLobbyServer(getLobbyServer());

		this.startCountdown = 20;

		Bukkit.getWorlds().get(0).setTime(1000);
		Bukkit.getWorlds().get(0).setStorm(false);

		Bukkit.getServer().getPluginManager().registerEvents(new PlayerHeadDrop(), this);

		MCFCore.getInstance().setActiveMcfPlugin(this);
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				Bukkit.getServer().getWorlds().get(0).getWorldBorder().setCenter(0.5, 0.5);;
				Bukkit.getServer().getWorlds().get(0).getWorldBorder().setSize(101);
			}
		}, 100L);
	}

	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll((Plugin) this);
		MCFCore.getInstance().getGameManager().disable();
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}

		if (hasActiveMap()) {
			String worldName = getActiveMap().getWorld().getName();
			Bukkit.getServer().unloadWorld(worldName, true);

			File targetFile = Paths.get(Bukkit.getServer().getWorldContainer().getAbsolutePath() + "/" + worldName).toFile();
			if (targetFile.exists()) {
				System.out.println("Deleting old world " + worldName);
				try {
					FileUtils.deleteDirectory(targetFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public HungergamesGamemode getGame() {
		return game;
	}

	public HungergamesMapData getMap(String name) {
		for (HungergamesMapData map : availableMaps) {
			if (map.getName() == (name)) {
				return map;
			}
		}

		return null;
	}

	public void setActiveMap(HungergamesMap activeMap) {
		if (hasActiveMap()) {
			throw new IllegalStateException("Map has already been registerd");
		}

		if (activeMap.getMapData().getEnderChestLootTable() != null) {
			MCFCore.getInstance().getChestLootManager().setEnderchestEnabled(true);
		}

		this.activeMap = activeMap;
	}

	public String getLobbyServer() {
		return lobbyServer;
	}

	public Location getLobbyLocation() {
		return this.lobbyLocation;
	}

	public Location getTeamScoreboardLocation() {
		return teamScoreboardLocation;
	}

	public Location getPlayerScoreboardLocation() {
		return playerScoreboardLocation;
	}

	public int getStartCountdown() {
		return this.startCountdown;
	}

	public ArrayList<HungergamesMapData> getAvailableMaps() {
		return this.availableMaps;
	}

	public HungergamesMap getActiveMap() {
		return this.activeMap;
	}

	public boolean hasActiveMap() {
		return this.activeMap != null;
	}

	/**
	 * Check if game has been started
	 * 
	 * @return <code>true</code> if countdown or game has started
	 */
	public boolean hasStarted() {
		return this.started;
	}

	/**
	 * Start the game with countdown
	 * 
	 * @return <code>false</code> if game has already been started
	 */
	public boolean start() {
		return this.start(true);
	}

	/**
	 * Start the game with optional countdown
	 * 
	 * @param countdown <code>false</code> to skip countdown
	 * @return <code>false</code> if game has already been started
	 */
	public boolean start(boolean countdown) {
		if (started) {
			return false;
		}

		started = true;

		if (countdown) {
			startTimer = new ScoreboardTimer(tpCountdown, ChatColor.GREEN + "Starting in: "+ ChatColor.AQUA, 7);
			startTimer.setTickCallback(new TimerCallback() {
				@Override
				public void execute(int timeLeft) {
					if (timeLeft <= 10) {
						for (Player p : Bukkit.getServer().getOnlinePlayers()) {
							p.playSound(p.getLocation(), Sound.NOTE_PLING, 1F, 1F);
						}
						Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Starting in: " + timeLeft);
					}
				}
			});

			startTimer.setCallback(new Callback() {
				@Override
				public void execute() {
					startGame();
				}
			});

			startTimer.start();
		} else {
			startGame();
		}

		return true;
	}

	/**
	 * Starts game and teleport players to arena
	 */
	private void startGame() {
		Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Loading map...");

		try {
			String mapName = mapVoteSystem.getWinner();

			System.out.println("Map name: " + mapName);

			HungergamesMapData mapData = getMap(mapName);

			Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Map: " + mapData.getDisplayName());

			HungergamesMap map = mapData.init();

			Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Teleporting to arena");

			setActiveMap(map);

			Thread.sleep(50);

			System.out.println(activeMap);
			System.out.println("has map: " + hasActiveMap());
			
			map.increasePlayCount();

			getGame().start();
		} catch (Exception e) {
			e.printStackTrace();

			String stackTrace = "";

			for (int i = 0; i < e.getStackTrace().length; i++) {
				stackTrace += e.getStackTrace()[i] + "\n";
			}

			Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED + "Failed to start\n" + e.getClass().getName() + "\n" + e.getMessage() + "\n" + stackTrace);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (tpToArenaTimer != null) {
			tpToArenaTimer.addReceiver(p);
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if (tpToArenaTimer != null) {
			if (tpToArenaTimer.getReceivers().contains(p)) {
				tpToArenaTimer.removeReceiver(p);
			}
		}
	}
}