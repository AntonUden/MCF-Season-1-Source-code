package xyz.mcfridays.mcf.mcfcore;

import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import xyz.mcfridays.mcf.mcfcommons.database.DBConnection;
import xyz.mcfridays.mcf.mcfcommons.database.DBCredentials;
import xyz.mcfridays.mcf.mcfcore.Commands.BackCommand;
import xyz.mcfridays.mcf.mcfcore.Commands.CICommand;
import xyz.mcfridays.mcf.mcfcore.Commands.DatabaseCommand;
import xyz.mcfridays.mcf.mcfcore.Commands.FlyCommand;
import xyz.mcfridays.mcf.mcfcore.Commands.HWICommand;
import xyz.mcfridays.mcf.mcfcore.Commands.InvseeCommand;
import xyz.mcfridays.mcf.mcfcore.Commands.MCFCommand;
import xyz.mcfridays.mcf.mcfcore.Commands.PlayerLocationCommand;
import xyz.mcfridays.mcf.mcfcore.Commands.RecipesCommands;
import xyz.mcfridays.mcf.mcfcore.Commands.RespawnPlayerCommand;
import xyz.mcfridays.mcf.mcfcore.Commands.SudoCommand;
import xyz.mcfridays.mcf.mcfcore.Commands.TeamCommand;
import xyz.mcfridays.mcf.mcfcore.CustomCrafting.CustomCraftingManager;
import xyz.mcfridays.mcf.mcfcore.CustomCrafting.Recipes.ArrowPackRecipe;
import xyz.mcfridays.mcf.mcfcore.CustomCrafting.Recipes.EnchantedGoldenAppleRecipe;
import xyz.mcfridays.mcf.mcfcore.CustomCrafting.Recipes.GoldPackRecipe;
import xyz.mcfridays.mcf.mcfcore.CustomCrafting.Recipes.GoldenHeadRecipe;
import xyz.mcfridays.mcf.mcfcore.CustomCrafting.Recipes.IronPackRecipe;
import xyz.mcfridays.mcf.mcfcore.CustomCrafting.gui.CustomRecipeGUI;
import xyz.mcfridays.mcf.mcfcore.GUI.GUIListener;
import xyz.mcfridays.mcf.mcfcore.Game.GameManager;
import xyz.mcfridays.mcf.mcfcore.Game.CustomItems.CustomItemManager;
import xyz.mcfridays.mcf.mcfcore.Game.CustomItems.Items.CustomCraftingBook;
import xyz.mcfridays.mcf.mcfcore.Game.CustomItems.Items.GoldenHeadItem;
import xyz.mcfridays.mcf.mcfcore.Listeners.EdibleHeads;
import xyz.mcfridays.mcf.mcfcore.Listeners.PlayerListener;
import xyz.mcfridays.mcf.mcfcore.Loot.LootTableLoader;
import xyz.mcfridays.mcf.mcfcore.Loot.LootTableManager;
import xyz.mcfridays.mcf.mcfcore.Loot.ChestLoot.ChestLootManager;
import xyz.mcfridays.mcf.mcfcore.Loot.LootDrop.LootDropManager;
import xyz.mcfridays.mcf.mcfcore.Loot.LootTables.V2.LootTableLoaderV2R2;
import xyz.mcfridays.mcf.mcfcore.Misc.EZReplacer;
import xyz.mcfridays.mcf.mcfcore.Misc.ScamReplacer;
import xyz.mcfridays.mcf.mcfcore.Music.NBSMusicManager;
import xyz.mcfridays.mcf.mcfcore.Score.ScoreManager;
import xyz.mcfridays.mcf.mcfcore.Scoreboard.MCFScoreboardManager;
import xyz.mcfridays.mcf.mcfcore.Scoreboard.Holo.HoloScoreboardManager;
import xyz.mcfridays.mcf.mcfcore.Teams.TeamManager;
import xyz.mcfridays.mcf.mcfcore.TrackerCompass.TrackerCompassManager;
import xyz.mcfridays.mcf.mcfcore.Utils.ServerLoad;
import xyz.mcfridays.mcf.mcfcore.Whitelist.MCFWhitelist;

public class MCFCore extends JavaPlugin implements Listener {
	private LootTableManager lootTableManager;
	private TeamManager teamManager;
	private GameManager gameManager;
	private ChestLootManager chestLootManager;
	private LootDropManager lootDropManager;
	private ScoreManager scoreManager;
	private HoloScoreboardManager holoScoreboardManager;
	private TrackerCompassManager trackerCompassManager;
	private MCFScoreboardManager mcfScoreboardManager;
	private MCFWhitelist mcfWhitelist;
	private ServerLoad serverLoad;
	private CustomItemManager customItemManager;
	private CustomCraftingManager customCraftingManager;
	private CustomRecipeGUI customRecipeGUI;

	private NBSMusicManager nbsMusicManager;

	private static MCFCore instance;

	private DBCredentials dbCredentials;

	private Plugin activeMcfPlugin;

	private String serverName;

	private ArrayList<Plugin> disableOnHalt;

	public static MCFCore getInstance() {
		return instance;
	}

	public TeamManager getTeamManager() {
		return teamManager;
	}

	public LootTableManager getLootTableManager() {
		return lootTableManager;
	}

	public LootDropManager getLootDropManager() {
		return lootDropManager;
	}

	public ChestLootManager getChestLootManager() {
		return chestLootManager;
	}

	public GameManager getGameManager() {
		return gameManager;
	}

	public ScoreManager getScoreManager() {
		return scoreManager;
	}

	public HoloScoreboardManager getHoloScoreboardManager() {
		return holoScoreboardManager;
	}

	public TrackerCompassManager getTrackerCompassManager() {
		return trackerCompassManager;
	}

	public MCFScoreboardManager getMcfScoreboardManager() {
		return mcfScoreboardManager;
	}

	public ServerLoad getServerLoad() {
		return serverLoad;
	}

	public CustomCraftingManager getCustomCraftingManager() {
		return customCraftingManager;
	}

	public CustomRecipeGUI getCustomRecipeGUI() {
		return customRecipeGUI;
	}

	public CustomItemManager getCustomItemManager() {
		return customItemManager;
	}

	public NBSMusicManager getNbsMusicManager() {
		return nbsMusicManager;
	}
	
	public boolean reconnectDatabase() throws SQLException {
		DBConnection.close();

		return DBConnection.init(dbCredentials);
	}

	@Override
	public void onEnable() {
		instance = this;
		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}

		saveDefaultConfig();

		this.serverName = getConfig().getString("server_name");

		dbCredentials = new DBCredentials(getConfig().getString("mysql.driver"), getConfig().getString("mysql.host"), getConfig().getString("mysql.username"), getConfig().getString("mysql.password"), getConfig().getString("mysql.database"));

		if (!DBConnection.init(dbCredentials)) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Failed to connect to database! disabling...");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		serverLoad = new ServerLoad();

		LootTableLoader loader = new LootTableLoaderV2R2();

		lootTableManager = new LootTableManager(loader);

		teamManager = new TeamManager();

		lootDropManager = new LootDropManager();

		gameManager = new GameManager();

		scoreManager = new ScoreManager();

		holoScoreboardManager = new HoloScoreboardManager();

		trackerCompassManager = new TrackerCompassManager();

		mcfScoreboardManager = new MCFScoreboardManager();

		mcfWhitelist = new MCFWhitelist();

		customItemManager = new CustomItemManager();

		customCraftingManager = new CustomCraftingManager();

		customRecipeGUI = new CustomRecipeGUI();

		disableOnHalt = new ArrayList<Plugin>();
		
		if (Bukkit.getPluginManager().isPluginEnabled("NoteBlockAPI")) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "NoteBlockAPI found. Creating NBSMusicManager");
			nbsMusicManager = new NBSMusicManager();
		}

		this.getCommand("team").setExecutor(new TeamCommand());
		this.getCommand("mcf").setExecutor(new MCFCommand());
		this.getCommand("sudo").setExecutor(new SudoCommand());
		this.getCommand("database").setExecutor(new DatabaseCommand());
		this.getCommand("recipes").setExecutor(new RecipesCommands());
		this.getCommand("fly").setExecutor(new FlyCommand());
		this.getCommand("invsee").setExecutor(new InvseeCommand());
		this.getCommand("respawnplayer").setExecutor(new RespawnPlayerCommand());
		this.getCommand("back").setExecutor(new BackCommand());
		this.getCommand("hwi").setExecutor(new HWICommand());
		this.getCommand("playerlocation").setExecutor(new PlayerLocationCommand());
		this.getCommand("ci").setExecutor(new CICommand());

		this.getServer().getPluginManager().registerEvents(gameManager, this);
		this.getServer().getPluginManager().registerEvents(teamManager, this);
		this.getServer().getPluginManager().registerEvents(scoreManager, this);
		this.getServer().getPluginManager().registerEvents(trackerCompassManager, this);
		this.getServer().getPluginManager().registerEvents(mcfScoreboardManager, this);
		this.getServer().getPluginManager().registerEvents(mcfWhitelist, this);
		this.getServer().getPluginManager().registerEvents(customItemManager, this);
		this.getServer().getPluginManager().registerEvents(customCraftingManager, this);
		this.getServer().getPluginManager().registerEvents(customRecipeGUI, this);

		this.getServer().getPluginManager().registerEvents(new EdibleHeads(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		this.getServer().getPluginManager().registerEvents(new EZReplacer(), this);
		this.getServer().getPluginManager().registerEvents(new ScamReplacer(), this);
		this.getServer().getPluginManager().registerEvents(new GUIListener(), this);

		this.getServer().getPluginManager().registerEvents(this, this);

		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		customItemManager.add(GoldenHeadItem.class);
		customItemManager.add(CustomCraftingBook.class);

		customCraftingManager.addRecipe(ArrowPackRecipe.class);
		customCraftingManager.addRecipe(IronPackRecipe.class);
		customCraftingManager.addRecipe(GoldPackRecipe.class);

		customCraftingManager.addRecipe(GoldenHeadRecipe.class);
		customCraftingManager.addRecipe(EnchantedGoldenAppleRecipe.class);

		customRecipeGUI.update();
	}

	@Override
	public void onDisable() {
		if (mcfScoreboardManager != null) {
			mcfScoreboardManager.stop();
		}

		if (gameManager.hasActiveGame()) {
			gameManager.getActiveGame().unload();
		}

		if (trackerCompassManager != null) {
			trackerCompassManager.stop();
		}

		if (holoScoreboardManager != null) {
			holoScoreboardManager.stop();
		}

		if (scoreManager != null) {
			scoreManager.stop();
		}

		if (teamManager != null) {
			getTeamManager().endTask();
		}

		if (lootDropManager != null) {
			lootDropManager.destroy();
		}

		if (serverLoad != null) {
			serverLoad.stop();
		}

		HandlerList.unregisterAll((Plugin) this);
		Bukkit.getScheduler().cancelTasks(this);

		if (DBConnection.connection != null) {
			try {
				DBConnection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		if (activeMcfPlugin != null) {
			activeMcfPlugin = null;
		}
	}

	public void addDisableOnHalt(Plugin plugin) {
		disableOnHalt.add(plugin);
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerAsActive(boolean active) {
		DBConnection.setActiveServer(active ? getServerName() : null);
	}

	public void enableChestLoot() {
		if (chestLootManager == null) {
			chestLootManager = new ChestLootManager();
			this.getServer().getPluginManager().registerEvents(chestLootManager, this);
		}
	}

	public void disableChestLoot() {
		if (chestLootManager != null) {
			HandlerList.unregisterAll(chestLootManager);
			chestLootManager.refillChests();
			chestLootManager = null;
		}
	}

	public void setActiveMcfPlugin(Plugin activeMcfPlugin) {
		this.activeMcfPlugin = activeMcfPlugin;
	}

	/**
	 * Halts all MCF plugin activity. This will disable all both mcf core and any
	 * active minigame plugin. Warning, this can not be undone
	 */
	public void halt() {
		for (Plugin plugin : disableOnHalt) {
			Bukkit.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Attempting to unregister " + plugin.getName());
			try {
				HandlerList.unregisterAll(plugin);
				Bukkit.getScheduler().cancelTasks(plugin);

				Bukkit.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Attempting to disable " + plugin.getName());
				Bukkit.getPluginManager().disablePlugin(plugin);
			} catch (Exception e) {
				Bukkit.getServer().broadcastMessage(ChatColor.RED + e.getClass().getName() + " " + e.getMessage());
			}
		}

		if (activeMcfPlugin != null) {
			Bukkit.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Attempting to unregister " + activeMcfPlugin.getName());
			try {
				HandlerList.unregisterAll(activeMcfPlugin);
				Bukkit.getScheduler().cancelTasks(activeMcfPlugin);
			} catch (Exception e) {
				Bukkit.getServer().broadcastMessage(ChatColor.RED + e.getClass().getName() + " " + e.getMessage());
			}
		}

		Bukkit.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Attempting to unregister MCF Core");
		try {
			HandlerList.unregisterAll((Plugin) this);
			Bukkit.getScheduler().cancelTasks(this);
		} catch (Exception e) {
			Bukkit.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + e.getClass().getName() + " " + e.getMessage());
		}

		for (int i = 0; i < MCFScoreboardManager.LINES; i++) {
			getMcfScoreboardManager().setCustomLine(i, ChatColor.RED + "" + ChatColor.BOLD + "Disabled");
		}

		if (activeMcfPlugin != null) {
			Bukkit.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Attempting to disable " + activeMcfPlugin.getName());
			try {
				Bukkit.getServer().getPluginManager().disablePlugin(activeMcfPlugin);
			} catch (Exception e) {
				Bukkit.getServer().broadcastMessage(ChatColor.RED + e.getClass().getName() + " " + e.getMessage());
			}
		}

		Bukkit.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Attempting to disable MCF Core");
		try {
			Bukkit.getServer().getPluginManager().disablePlugin(this);
		} catch (Exception e) {
			Bukkit.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + e.getClass().getName() + " " + e.getMessage());
		}
	}
}