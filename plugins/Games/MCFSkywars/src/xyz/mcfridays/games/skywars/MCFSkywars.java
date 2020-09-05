package xyz.mcfridays.games.skywars;

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
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import xyz.mcfridays.games.skywars.commands.SkywarsAdminCommand;
import xyz.mcfridays.games.skywars.gamemode.SkywarsGamemode;
import xyz.mcfridays.games.skywars.loot.SkywarsLootLoader;
import xyz.mcfridays.games.skywars.map.MapLoader;
import xyz.mcfridays.games.skywars.map.SkywarsMap;
import xyz.mcfridays.games.skywars.map.SkywarsMapData;
import xyz.mcfridays.games.skywars.mapselector.MapVoteSystem;
import xyz.mcfridays.games.skywars.tracker.SkywarsPlayerTracker;
import xyz.mcfridays.mcf.mcfcommons.utils.Callback;
import xyz.mcfridays.mcf.mcfcommons.utils.TimerCallback;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Events.ChestFillEvent;
import xyz.mcfridays.mcf.mcfcore.Listeners.DeathEffect;
import xyz.mcfridays.mcf.mcfcore.Listeners.DisableAchievements;
import xyz.mcfridays.mcf.mcfcore.Listeners.NoEnderPearlDamage;
import xyz.mcfridays.mcf.mcfcore.Listeners.PlayerHeadDrop;
import xyz.mcfridays.mcf.mcfcore.Utils.BlockUtils;
import xyz.mcfridays.mcf.mcfcore.timers.ScoreboardTimer;

public class MCFSkywars extends JavaPlugin implements Listener {
	private static MCFSkywars instance;

	private SkywarsGamemode game;

	private MapVoteSystem mapVoteSystem;

	private ArrayList<SkywarsMapData> availableMaps;
	private SkywarsMap activeMap;

	private String lobbyServer;

	private Location lobbyLocation;
	private Location playerScoreboardLocation;
	private Location teamScoreboardLocation;

	private boolean started;

	private ScoreboardTimer startTimer;
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChestFille(ChestFillEvent e) {
		Bukkit.getServer().broadcastMessage("A chest fill event was triggered. Loot table: " + e.getLootTable().getName() + " changed: " + e.hasLootTableChanged() + " canceled: " + e.isCancelled());
	}

	@Override
	public void onEnable() {
		instance = this;
		started = false;
		this.activeMap = null;
		this.availableMaps = new ArrayList<SkywarsMapData>();

		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}

		saveDefaultConfig();

		File lootTableFolder = new File(getDataFolder().getAbsolutePath() + "/loot_tables");
		if (lootTableFolder.exists()) {
			lootTableFolder.mkdirs();
		}
		
		File islandLootTableFolder = new File(getDataFolder().getAbsolutePath() + "/island_loot_tables");
		if (islandLootTableFolder.exists()) {
			islandLootTableFolder.mkdirs();
		}

		File mapsFolder = new File(getDataFolder().getAbsolutePath() + "/maps");
		if (mapsFolder.exists()) {
			mapsFolder.mkdirs();
		}

		try {
			MCFCore.getInstance().getLootTableManager().loadAll(lootTableFolder);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			MCFCore.getInstance().getLootTableManager().loadAllWithLoader(islandLootTableFolder, new SkywarsLootLoader());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			availableMaps = MapLoader.load(mapsFolder);
		} catch (Exception e) {
			e.printStackTrace();
		}

		ConfigurationSection ll = getConfig().getConfigurationSection("lobby_location");
		lobbyLocation = new Location(Bukkit.getServer().getWorlds().get(0), BlockUtils.blockCenter(ll.getInt("x")), ll.getInt("y"), BlockUtils.blockCenter(ll.getInt("z")), ll.getInt("yaw"), ll.getInt("pitch"));

		ConfigurationSection psl = getConfig().getConfigurationSection("player_scoreboard");
		playerScoreboardLocation = new Location(Bukkit.getServer().getWorlds().get(0), BlockUtils.blockCenter(psl.getInt("x")), psl.getInt("y"), BlockUtils.blockCenter(psl.getInt("z")));

		ConfigurationSection tsl = getConfig().getConfigurationSection("team_scoreboard");
		teamScoreboardLocation = new Location(Bukkit.getServer().getWorlds().get(0), BlockUtils.blockCenter(tsl.getInt("x")), tsl.getInt("y"), BlockUtils.blockCenter(tsl.getInt("z")));

		this.lobbyServer = getConfig().getString("lobby_server");

		getCommand("skywarsadmin").setExecutor(new SkywarsAdminCommand());

		this.game = new SkywarsGamemode();
		mapVoteSystem = new MapVoteSystem();

		MCFCore.getInstance().getHoloScoreboardManager().setPlayerHologramLocation(playerScoreboardLocation);
		MCFCore.getInstance().getHoloScoreboardManager().setTeamHologramLocation(teamScoreboardLocation);
		MCFCore.getInstance().enableChestLoot();
		MCFCore.getInstance().getHoloScoreboardManager().setLines(5);
		MCFCore.getInstance().getMcfScoreboardManager().setServerString(ChatColor.YELLOW + "" + ChatColor.BOLD + "Skywars");
		MCFCore.getInstance().setActiveMcfPlugin(this);
		MCFCore.getInstance().getTrackerCompassManager().setTracker(new SkywarsPlayerTracker());

		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getServer().getPluginManager().registerEvents(new DisableAchievements(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new DeathEffect(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new NoEnderPearlDamage(), this);
		Bukkit.getServer().getPluginManager().registerEvents(mapVoteSystem, this);
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerHeadDrop(), this);

		MCFCore.getInstance().getGameManager().setActiveGame(game, this);
		game.setLobbyServer(lobbyServer);
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

			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}

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

	public static MCFSkywars getInstance() {
		return instance;
	}

	public SkywarsGamemode getGame() {
		return game;
	}

	public ArrayList<SkywarsMapData> getAvailableMaps() {
		return this.availableMaps;
	}

	public SkywarsMap getActiveMap() {
		return this.activeMap;
	}

	public boolean hasActiveMap() {
		return this.activeMap != null;
	}

	public SkywarsMapData getMap(String name) {
		for (SkywarsMapData map : availableMaps) {
			if (map.getName() == (name)) {
				return map;
			}
		}

		return null;
	}

	public void setActiveMap(SkywarsMap activeMap) {
		if (hasActiveMap()) {
			throw new IllegalStateException("Map has already been registerd");
		}

		if (activeMap.getMapData().getEnderChestLootTable() != null) {
			MCFCore.getInstance().getChestLootManager().setEnderchestEnabled(true);
		}

		this.activeMap = activeMap;
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

	public boolean hasStarted() {
		return started;
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
			startTimer = new ScoreboardTimer(60, ChatColor.GOLD + "Starting in: " + ChatColor.AQUA, 7);
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

			SkywarsMapData mapData = getMap(mapName);

			Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Map: " + mapData.getDisplayName());

			SkywarsMap map = mapData.init();

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
}