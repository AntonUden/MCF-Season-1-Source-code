package xyz.mcfridays.mcf.lobby;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import me.rayzr522.jsonmessage.JSONMessage;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import xyz.mcfridays.mcf.lobby.Listeners.AutoRespawn;
import xyz.mcfridays.mcf.lobby.Listeners.DenyDrop;
import xyz.mcfridays.mcf.lobby.Listeners.FreeRodSign;
import xyz.mcfridays.mcf.lobby.Listeners.KOTL;
import xyz.mcfridays.mcf.lobby.Listeners.NoBlockBreak;
import xyz.mcfridays.mcf.lobby.Listeners.NoDamage;
import xyz.mcfridays.mcf.lobby.NPC.Traits.MerchantTrait;
import xyz.mcfridays.mcf.lobby.other.FireworksCommand;
import xyz.mcfridays.mcf.lobby.other.YeetCommand;
import xyz.mcfridays.mcf.mcfcommons.database.DBConnection;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Listeners.DeathEffect;
import xyz.mcfridays.mcf.mcfcore.Listeners.DisableAchievements;
import xyz.mcfridays.mcf.mcfcore.Utils.BlockUtils;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;
import xyz.mcfridays.mcf.mcfcore.Utils.JsonBookReader;
import xyz.mcfridays.mcf.mcfcore.Utils.PlayerUtils;

public class MCFLobby extends JavaPlugin implements Listener {
	private static MCFLobby instance;

	private Location spawnLocation;

	private ItemStack infoBook;

	private KOTL kotl;

	private boolean devMode;

	public static MCFLobby getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		instance = this;

		saveDefaultConfig();

		devMode = getConfig().getBoolean("dev_mode");

		ConfigurationSection psb = getConfig().getConfigurationSection("player_scoreboard");
		ConfigurationSection tsb = getConfig().getConfigurationSection("team_scoreboard");

		ConfigurationSection sl = getConfig().getConfigurationSection("spawn_location");

		Location psbl = new Location(Bukkit.getServer().getWorlds().get(0), BlockUtils.blockCenter(psb.getInt("x")), psb.getInt("y") + 2.5, BlockUtils.blockCenter(psb.getInt("z")));
		Location tsbl = new Location(Bukkit.getServer().getWorlds().get(0), BlockUtils.blockCenter(tsb.getInt("x")), tsb.getInt("y") + 2.5, BlockUtils.blockCenter(tsb.getInt("z")));

		spawnLocation = new Location(Bukkit.getServer().getWorlds().get(0), BlockUtils.blockCenter(sl.getInt("x")), sl.getInt("y"), BlockUtils.blockCenter(sl.getInt("z")), (float) sl.getDouble("yaw"), (float) sl.getDouble("pitch"));

		MCFCore.getInstance().getHoloScoreboardManager().setPlayerHologramLocation(psbl);
		MCFCore.getInstance().getHoloScoreboardManager().setTeamHologramLocation(tsbl);

		MCFCore.getInstance().getHoloScoreboardManager().setLines(8);

		Bukkit.getServer().getPluginManager().registerEvents(this, this);

		Bukkit.getServer().getPluginManager().registerEvents(new AutoRespawn(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new DenyDrop(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new DisableAchievements(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new NoBlockBreak(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new NoDamage(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new DeathEffect(), this);
		
		Bukkit.getServer().getPluginManager().registerEvents(new FreeRodSign(), this);

		Bukkit.getWorlds().get(0).setTime(1000);
		Bukkit.getWorlds().get(0).setDifficulty(Difficulty.EASY);
		Bukkit.getWorlds().get(0).setStorm(false);
		Bukkit.getWorlds().get(0).setGameRuleValue("doDaylightCycle", "false");
		
		this.getCommand("yeet").setExecutor(new YeetCommand());
		this.getCommand("fireworks").setExecutor(new FireworksCommand());

		try {
			infoBook = JsonBookReader.getBook(new File(getDataFolder().getAbsolutePath() + "/info_book.json"));
		} catch (Exception e) {
			e.printStackTrace();
			infoBook = new ItemStack(Material.WRITTEN_BOOK);

			BookMeta meta = (BookMeta) infoBook.getItemMeta();

			String[] page = { "roses are red\nviolets are blue\ni think i deleted system 32\n\nRead error: " + ChatColor.RED + e.getClass().getName() };

			meta.addPage(page);

			meta.setTitle("Error");
			meta.setAuthor("Elon musk");

			infoBook.setItemMeta(meta);
		}

		MCFCore.getInstance().getMcfScoreboardManager().setServerString(ChatColor.YELLOW + "" + ChatColor.BOLD + "Lobby");

		MCFCore.getInstance().setActiveMcfPlugin(this);

		if (devMode) {
			MCFCore.getInstance().getMcfScoreboardManager().setCustomLine(1, ChatColor.DARK_RED + "" + ChatColor.BOLD + "Dev mode");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Dev mode enabled");
		}

		ConfigurationSection kotlConfig = getConfig().getConfigurationSection("kotl");
		if (kotlConfig.getBoolean("enabled")) {
			int x = kotlConfig.getInt("x");
			int z = kotlConfig.getInt("z");
			int topY = kotlConfig.getInt("top_y");

			double radius = kotlConfig.getDouble("radius");

			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "KOTL Enabled x: " + x + " z: " + z + " top_y: " + topY + " radius: " + radius);
			kotl = new KOTL(x, z, topY, radius, Bukkit.getServer().getWorlds().get(0));
			Bukkit.getPluginManager().registerEvents(kotl, this);
		}
		
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				for(Player player : Bukkit.getServer().getOnlinePlayers()) {
					player.setFoodLevel(20);
				}
			}
		}, 20L, 20L);
		
		CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(MerchantTrait.class).withName("MerchantTrait"));
	}

	@EventHandler
	public void onInventoryInteract(InventoryInteractEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			Player player = (Player) e.getWhoClicked();

			if (player.getGameMode() != GameMode.CREATIVE) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			Player player = (Player) e.getWhoClicked();

			if (player.getGameMode() != GameMode.CREATIVE) {
				e.setCancelled(true);
			}
		}
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll((Plugin) this);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		PlayerUtils.clearPlayerInventory(p);
		p.teleport(spawnLocation);
		p.setGameMode(GameMode.ADVENTURE);
		p.setHealth(p.getMaxHealth());
		p.getInventory().addItem(infoBook.clone());
		p.setFoodLevel(20);
		
		if (devMode) {
			p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Developer mode enabled!\n" + ChatColor.RESET + ChatColor.RED + "We are currently developing and testing new features!");
		}
		
		String activeServer = DBConnection.getActiveServer();
		
		if(activeServer != null) {
			JSONMessage.create("A game is in progress!").color(ChatColor.GOLD).style(ChatColor.BOLD).send(p);
			JSONMessage.create("Use /reconnect or click ").color(ChatColor.GOLD).style(ChatColor.BOLD).then("[Here]").color(ChatColor.GREEN).tooltip("Click to reconnect").runCommand("/reconnect").style(ChatColor.BOLD).then(" to reconnect").color(ChatColor.GOLD).style(ChatColor.BOLD).send(p);
		}
		
		if(p.getUniqueId().toString().equalsIgnoreCase("5457678e-c69d-4438-be87-a986f351f6d0")) {
			p.getInventory().addItem(new ItemBuilder(Material.MINECART).setName(p.getName()+"'s minecart").setAmount(1).build());
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getPlayer().getLocation().getY() <= 10) {
			e.getPlayer().teleport(spawnLocation);
		}
	}

	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e) {
		if (e.toWeatherState()) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onExplosionPrime(ExplosionPrimeEvent e) {
		//e.setCancelled(true);
	}
}