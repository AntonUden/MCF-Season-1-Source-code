package xyz.mcfridays.mcf.games.uhc;

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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import xyz.mcfridays.mcf.games.uhc.commands.TopCommand;
import xyz.mcfridays.mcf.games.uhc.commands.UHCAdminCommand;
import xyz.mcfridays.mcf.games.uhc.customcrafting.AndurilRecipe;
import xyz.mcfridays.mcf.games.uhc.customcrafting.BottleOEnchantingRecipe;
import xyz.mcfridays.mcf.games.uhc.customcrafting.DragonArmor;
import xyz.mcfridays.mcf.games.uhc.customcrafting.FenrirRecipe;
import xyz.mcfridays.mcf.games.uhc.customcrafting.FlaskOfIchor;
import xyz.mcfridays.mcf.games.uhc.customcrafting.LightAnvilRecipe;
import xyz.mcfridays.mcf.games.uhc.customcrafting.LightAppleRecipe;
import xyz.mcfridays.mcf.games.uhc.customcrafting.ObsidianRecipe;
import xyz.mcfridays.mcf.games.uhc.customcrafting.PhilosophersPickaxeRecipe;
import xyz.mcfridays.mcf.games.uhc.customcrafting.PowerBook;
import xyz.mcfridays.mcf.games.uhc.customcrafting.ProtectionBook;
import xyz.mcfridays.mcf.games.uhc.customcrafting.SaddleRecipe;
import xyz.mcfridays.mcf.games.uhc.customcrafting.SharpnessBook;
import xyz.mcfridays.mcf.games.uhc.customcrafting.SteakRecipe;
import xyz.mcfridays.mcf.games.uhc.customcrafting.Tarnhelm;
import xyz.mcfridays.mcf.games.uhc.customcrafting.VorpalSword;
import xyz.mcfridays.mcf.games.uhc.customitems.AndurilItem;
import xyz.mcfridays.mcf.games.uhc.customitems.FenrirItem;
import xyz.mcfridays.mcf.games.uhc.customitems.UnknownRecordItem;
import xyz.mcfridays.mcf.games.uhc.customitems.listeners.AppleDropListener;
import xyz.mcfridays.mcf.games.uhc.customitems.listeners.FenrirListener;
import xyz.mcfridays.mcf.games.uhc.customitems.listeners.InstantSmeltingListener;
import xyz.mcfridays.mcf.games.uhc.gamemode.Stage;
import xyz.mcfridays.mcf.games.uhc.gamemode.UHCGamemode;
import xyz.mcfridays.mcf.mcfcommons.utils.Callback;
import xyz.mcfridays.mcf.mcfcommons.utils.TimerCallback;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Listeners.PlayerHeadDrop;
import xyz.mcfridays.mcf.mcfcore.Utils.BlockUtils;
import xyz.mcfridays.mcf.mcfcore.timers.ScoreboardTimer;

public class MCFUHC extends JavaPlugin implements Listener {
	private static MCFUHC instance;

	private Location lobbyLocation;
	private Location playerScoreboardLocation;
	private Location teamScoreboardLocation;
	
	public static MCFUHC getInstance() {
		return instance;
	}

	private UHCGamemode game;

	public UHCGamemode getGame() {
		return game;
	}

	private ScoreboardTimer tpCountdown;

	@Override
	public void onEnable() {
		instance = this;

		saveDefaultConfig();

		this.game = new UHCGamemode(32, 100, 300, 1800);

		// Set game
		MCFCore.getInstance().getGameManager().setActiveGame(game, this);
		MCFCore.getInstance().getMcfScoreboardManager().setServerString(ChatColor.YELLOW + "" + ChatColor.BOLD + "UHC 2.0");

		// Locations
		ConfigurationSection ll = getConfig().getConfigurationSection("lobby_location");
		lobbyLocation = new Location(Bukkit.getServer().getWorlds().get(0), BlockUtils.blockCenter(ll.getInt("x")), ll.getInt("y"), BlockUtils.blockCenter(ll.getInt("z")), ll.getInt("yaw"), ll.getInt("pitch"));

		ConfigurationSection psl = getConfig().getConfigurationSection("player_scoreboard");
		playerScoreboardLocation = new Location(Bukkit.getServer().getWorlds().get(0), BlockUtils.blockCenter(psl.getInt("x")), psl.getInt("y"), BlockUtils.blockCenter(psl.getInt("z")));

		ConfigurationSection tsl = getConfig().getConfigurationSection("team_scoreboard");
		teamScoreboardLocation = new Location(Bukkit.getServer().getWorlds().get(0), BlockUtils.blockCenter(tsl.getInt("x")), tsl.getInt("y"), BlockUtils.blockCenter(tsl.getInt("z")));

		// Commands
		this.getCommand("uhcadmin").setExecutor(new UHCAdminCommand());
		this.getCommand("top").setExecutor(new TopCommand());

		// Score board locations
		MCFCore.getInstance().getHoloScoreboardManager().setPlayerHologramLocation(playerScoreboardLocation);
		MCFCore.getInstance().getHoloScoreboardManager().setTeamHologramLocation(teamScoreboardLocation);

		// set lobby
		this.game.setLobbyServer(getConfig().getString("lobby_server"));

		// Register compass tracker
		// MCFCore.getInstance().getTrackerCompassManager().setTracker(new UHCPlayerTracker());

		// Events
		Bukkit.getServer().getPluginManager().registerEvents(this, this);

		Bukkit.getServer().getPluginManager().registerEvents(new PlayerHeadDrop(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new FenrirListener(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new AppleDropListener(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new InstantSmeltingListener(), this);

		// Register custom items
		MCFCore.getInstance().getCustomItemManager().add(AndurilItem.class);
		MCFCore.getInstance().getCustomItemManager().add(FenrirItem.class);

		// Register custom crafting recipes
		MCFCore.getInstance().getCustomCraftingManager().addRecipe(SteakRecipe.class);

		MCFCore.getInstance().getCustomCraftingManager().addRecipe(LightAnvilRecipe.class);
		MCFCore.getInstance().getCustomCraftingManager().addRecipe(LightAppleRecipe.class);

		MCFCore.getInstance().getCustomCraftingManager().addRecipe(SaddleRecipe.class);

		MCFCore.getInstance().getCustomCraftingManager().addRecipe(AndurilRecipe.class);
		MCFCore.getInstance().getCustomCraftingManager().addRecipe(BottleOEnchantingRecipe.class);
		MCFCore.getInstance().getCustomCraftingManager().addRecipe(DragonArmor.class);
		MCFCore.getInstance().getCustomCraftingManager().addRecipe(FenrirRecipe.class);
		MCFCore.getInstance().getCustomCraftingManager().addRecipe(FlaskOfIchor.class);
		MCFCore.getInstance().getCustomCraftingManager().addRecipe(PhilosophersPickaxeRecipe.class);
		MCFCore.getInstance().getCustomCraftingManager().addRecipe(Tarnhelm.class);
		MCFCore.getInstance().getCustomCraftingManager().addRecipe(VorpalSword.class);
		MCFCore.getInstance().getCustomCraftingManager().addRecipe(ObsidianRecipe.class);

		MCFCore.getInstance().getCustomCraftingManager().addRecipe(ProtectionBook.class);
		MCFCore.getInstance().getCustomCraftingManager().addRecipe(SharpnessBook.class);
		MCFCore.getInstance().getCustomCraftingManager().addRecipe(PowerBook.class);

		// Update recipe GUI
		MCFCore.getInstance().getCustomRecipeGUI().update();

		// Other
		if (MCFCore.getInstance().getNbsMusicManager() != null) {
			System.out.println("Register UnknownRecordItem.class");
			MCFCore.getInstance().getCustomItemManager().add(UnknownRecordItem.class);
		}

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
		startTpCountdown();
		tpCountdown.setSeconds(0);
	}

	public void startTpCountdown() {
		if (game.getStage() == Stage.WAITING) {
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

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		Scoreboard playerScoreboard = MCFCore.getInstance().getMcfScoreboardManager().getPlayerScoreboard(p);
		if (playerScoreboard != null) {
			if (playerScoreboard.getObjective("health") != null) {
				playerScoreboard.getObjective("health").unregister();
			}

			playerScoreboard.registerNewObjective("health", "health");
			Objective objective = playerScoreboard.getObjective("health");
			objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
		}
	}
}