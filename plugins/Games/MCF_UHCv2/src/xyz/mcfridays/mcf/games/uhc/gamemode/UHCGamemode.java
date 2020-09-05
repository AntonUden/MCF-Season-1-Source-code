package xyz.mcfridays.mcf.games.uhc.gamemode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import xyz.mcfridays.mcf.games.uhc.MCFUHC;
import xyz.mcfridays.mcf.mcfcommons.utils.Callback;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Events.PlayerEliminatedEvent;
import xyz.mcfridays.mcf.mcfcore.Game.EliminationType;
import xyz.mcfridays.mcf.mcfcore.Game.Game;
import xyz.mcfridays.mcf.mcfcore.Game.CustomItems.Items.CustomCraftingBook;
import xyz.mcfridays.mcf.mcfcore.Teams.Team;
import xyz.mcfridays.mcf.mcfcore.Utils.BlockUtils;
import xyz.mcfridays.mcf.mcfcore.Utils.PlayerUtils;
import xyz.mcfridays.mcf.mcfcore.Utils.SlowPlayerSender;
import xyz.mcfridays.mcf.mcfcore.Worldborder.WorldBorderShrinkMode;
import xyz.mcfridays.mcf.mcfcore.Worldborder.WorldBorderShrinkTask;
import xyz.mcfridays.mcf.mcfcore.timers.DelayedTask;
import xyz.mcfridays.mcf.mcfcore.timers.ScoreboardTimer;
import xyz.mcfridays.mcf.mcfcore.world.WorldPreGenerator;

public class UHCGamemode extends Game {
	private WorldPreGenerator preGenerator;
	private int generatorProgressTaskId;
	private Stage stage;

	private int worldSize;
	private int generatorSpeed;

	private HashMap<Integer, Location> teamLocations;

	private ScoreboardTimer gracePeriodCountdown;

	private boolean gracePeriodActive;
	private int gracePeriodTime;

	private int arenaUpdateTaskId;

	private World netherWorld;

	private int borderShrinkTime;
	
	private boolean tpNoDamage;

	private WorldBorderShrinkTask borderShrinkTask;
	
	public UHCGamemode(int worldSize, int generatorSpeed, int gracePeriodTime, int borderShrinkTime) {
		super(Bukkit.getServer().getWorlds().get(0));

		this.worldSize = worldSize;
		this.generatorSpeed = generatorSpeed;
		this.gracePeriodTime = gracePeriodTime;

		this.preGenerator = null;
		this.generatorProgressTaskId = -1;
		this.gracePeriodActive = false;
		this.gracePeriodCountdown = null;
		this.tpNoDamage = false;
		this.netherWorld = null;
		
		this.borderShrinkTime = borderShrinkTime;

		this.teamLocations = new HashMap<Integer, Location>();

		setKillReward(20);

		this.setWinScore(100, 75, 50);

		this.arenaUpdateTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFUHC.getInstance(), new Runnable() {
			@Override
			public void run() {
				if (stage == Stage.INGAME) {
					ArrayList<Team> teamsLeft = getTeamsLeft();
					if (teamsLeft.size() == 0) {
						if (!hasEnded()) {
							end();
						}
					} else if (teamsLeft.size() == 1) {
						int score = 0;
						if (getWinScore().length > 0) {
							score = getWinScore()[0];
						}
						onTeamPlace(teamsLeft.get(0), 1, score);
					}

				} else if (stage == Stage.WAITING) {
					for (Player p : Bukkit.getServer().getOnlinePlayers()) {
						p.setFoodLevel(20);
					}
				}
			}
		}, 20L, 20L);
	}

	@Override
	protected void onLoad() {
		setStage(Stage.GENERATING_WORLD);
	}

	@Override
	protected void onUnload() {
		endTasks();
	}

	@Override
	public boolean pvpEnabled() {
		if (stage == Stage.INGAME) {
			if (!gracePeriodActive) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean killRewardEnabled() {
		return stage == Stage.INGAME;
	}

	@Override
	public void tpToLobby(Player player) {
		player.teleport(MCFUHC.getInstance().getLobbyLocation());
	}

	@Override
	public void tpToArena(Player player) {
		Team team = MCFCore.getInstance().getTeamManager().getPlayerTeam(player);
		if (team != null) {
			if (teamLocations.containsKey(team.getTeamNumber())) {
				tpToArenaLocation(player, teamLocations.get(team.getTeamNumber()));
				return;
			}
		}

		for (int i = 0; i < 10000; i++) {
			Location location = tryGetSpawnLocation();
			if (location == null) {
				continue;
			}

			if (team != null) {
				teamLocations.put(team.getTeamNumber(), location);
			}

			tpToArenaLocation(player, location);

			return;
		}
		player.sendMessage(ChatColor.RED + "Failed to teleport within 10000 attempts, Sending you to default world spawn");
		tpToArenaLocation(player, world.getSpawnLocation());
	}

	public void randomTp(Player player) {
		for (int i = 0; i < 100; i++) {
			Location location = tryGetSpawnLocation();
			if (location == null) {
				continue;
			}

			tpToArenaLocation(player, location);
			return;
		}
	}

	private void tpToArenaLocation(Player player, Location location) {
		world.loadChunk(location.getBlockX(), location.getBlockZ());
		player.teleport(new Location(world, BlockUtils.blockCenter(location.getBlockX()), location.getY() + 1, BlockUtils.blockCenter(location.getBlockZ())));
		player.setGameMode(GameMode.SURVIVAL);
		player.setMaxHealth(40);
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
	}

	public Location tryGetSpawnLocation() {
		int max = (worldSize * 16) - 50;

		Random random = new Random();
		int x = max - random.nextInt(max * 2);
		int z = max - random.nextInt(max * 2);

		Location location = new Location(world, x, 256, z);

		for (int i = 256; i > 14; i++) {
			location.setY(location.getY() - 1);

			Block b = location.clone().add(0, -1, 0).getBlock();

			if (b.getType() != Material.AIR) {
				if (b.isLiquid()) {
					break;
				}

				if (b.getType().isSolid()) {
					return location;
				}
			}
		}

		return null;
	}

	@Override
	public void onPlayerDeath(Player player, LivingEntity killer) {
		if (stage != Stage.WAITING && stage != Stage.GENERATING_WORLD) {
			Location location = player.getLocation();
			player.spigot().respawn();
			player.teleport(location);
			player.setGameMode(GameMode.SPECTATOR);
		} else {
			player.spigot().respawn();
		}
	}

	@EventHandler
	public void onPlayerEliminated(PlayerEliminatedEvent e) {
		for (UUID uuid : players) {
			Player p2 = Bukkit.getServer().getPlayer(uuid);
			if (p2 != null) {
				if (p2.isOnline()) {
					MCFCore.getInstance().getScoreManager().addScore(uuid, 2, true);
					p2.sendMessage(ChatColor.GRAY + "+2 Participation score");
				}
			}
		}
	}

	@Override
	public void tpToSpectator(Player player) {
		player.setGameMode(GameMode.SPECTATOR);

		player.teleport(world.getSpawnLocation());
	}

	public Stage getStage() {
		return stage;
	}

	public void start() {
		if (stage != Stage.WAITING) {
			return;
		}

		tpNoDamage = true;

		ArrayList<Player> toTeleport = new ArrayList<Player>();
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			if (players.contains(player.getUniqueId())) {
				toTeleport.add(player);
			} else {
				tpToSpectator(player);
			}
		}

		for (Player p : Bukkit.getOnlinePlayers()) {
			PlayerUtils.clearPlayerInventory(p);
			PlayerUtils.clearPotionEffects(p);
			PlayerUtils.resetPlayerXP(p);

			p.getInventory().addItem(MCFCore.getInstance().getCustomItemManager().getItem(CustomCraftingBook.class));

			p.setExp(0);
			p.setLevel(0);
		}

		for (Player p : toTeleport) {
			try {
				this.tpToArena(p);
			} catch (Exception e) {
				p.sendMessage(ChatColor.DARK_RED + "Teleport failed: " + e.getClass().getName() + ". Please contact an admin");
			}
		}

		new DelayedTask(200).setCallback(new Callback() {
			@Override
			public void execute() {
				tpNoDamage = false;
			}
		}).start();

		setStage(Stage.INGAME);
	}

	public void setStage(Stage stage) {
		this.stage = stage;

		switch (stage) {
		case GENERATING_WORLD:
			String worldName = "uhc_world";

			File worldContainer = Bukkit.getServer().getWorldContainer();

			File netherFile = Paths.get(worldContainer.getAbsolutePath() + "/" + worldName + "_nether").toFile();
			if (netherFile.exists()) {
				System.out.println("Deleting old nether world " + worldName + "_nether");
				try {
					FileUtils.deleteDirectory(netherFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			File worldFile = Paths.get(worldContainer.getAbsolutePath() + "/" + worldName).toFile();
			if (worldFile.exists()) {
				System.out.println("Deleting old world " + worldName);
				try {
					FileUtils.deleteDirectory(worldFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Generating world...");

			WorldCreator worldCreator = new WorldCreator(worldName);

			World uhcWorld = Bukkit.getServer().createWorld(worldCreator);

			WorldCreator netherWorldCreator = new WorldCreator(worldName + "_nether");
			netherWorldCreator.environment(Environment.NETHER);
			netherWorldCreator.seed(uhcWorld.getSeed());

			netherWorld = Bukkit.getServer().createWorld(netherWorldCreator);

			preGenerator = new WorldPreGenerator(uhcWorld, worldSize, generatorSpeed, new Callback() {
				@Override
				public void execute() {
					Bukkit.getScheduler().cancelTask(generatorProgressTaskId);
					MCFCore.getInstance().getMcfScoreboardManager().deleteCustomLine(7);
					setStage(Stage.WAITING);
				}
			});

			generatorProgressTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFUHC.getInstance(), new Runnable() {
				@Override
				public void run() {
					MCFCore.getInstance().getMcfScoreboardManager().setCustomLine(7, ChatColor.GOLD + "Loading world " + ChatColor.AQUA + ((int) (((double) preGenerator.getProgress() / (double) preGenerator.getTotal()) * 100)) + "%");
					System.out.println("World generation " + preGenerator.getProgress() + "/" + preGenerator.getTotal() + " " + ((int) (((double) preGenerator.getProgress() / (double) preGenerator.getTotal()) * 100)) + "%");
				}
			}, 20L, 20L);
			preGenerator.start();
			this.world = uhcWorld;
			this.world.getWorldBorder().setCenter(0, 0);
			this.world.getWorldBorder().setSize(worldSize * 16 * 2);
			this.world.getWorldBorder().setWarningDistance(50);

			break;

		case WAITING:
			Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Ready");
			break;

		case INGAME:
			try {
				MCFCore.getInstance().setServerAsActive(true);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			this.world.setTime(1000);
			this.world.setDifficulty(Difficulty.NORMAL);
			this.world.setStorm(false);
			this.world.setGameRuleValue("doFireTick", "false");
			gracePeriodActive = true;

			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 2400, 0), false);
				for (Achievement achievement : Achievement.values()) {
					if (p.hasAchievement(achievement)) {
						p.removeAchievement(achievement);
					}
				}
			}

			gracePeriodCountdown = new ScoreboardTimer(gracePeriodTime, ChatColor.AQUA + "Grace period: ", 7);
			gracePeriodCountdown.setCallback(new Callback() {
				@Override
				public void execute() {
					gracePeriodCountdown = null;
					gracePeriodActive = false;
					for (Player p : Bukkit.getServer().getOnlinePlayers()) {
						p.playSound(p.getLocation(), Sound.NOTE_PLING, 1F, 1F);
						p.setHealth(p.getMaxHealth());
						p.setRemainingAir(p.getMaximumAir());
						p.setFoodLevel(20);
						p.setFireTicks(0);
						p.setFallDistance(0);
					}

					Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Grace period is over");
					Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "All players have been healed");
				}
			});
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				p.playSound(p.getLocation(), Sound.NOTE_PLING, 1F, 1F);
				p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Grace period will end in " + String.format("%02d:%02d", gracePeriodTime / 60, gracePeriodTime % 60));
			}
			gracePeriodCountdown.start();

			Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Remember to find food before you go mining");

			borderShrinkTask = new WorldBorderShrinkTask(this.world, worldSize * 16 * 2, 16, borderShrinkTime, 30, WorldBorderShrinkMode.API);
			borderShrinkTask.start();
			
			break;

		case END:
			try {
				MCFCore.getInstance().setServerAsActive(false);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				p.setMaxHealth(20);
				p.setHealth(p.getMaxHealth());
				p.setFoodLevel(20);
				PlayerUtils.clearPlayerInventory(p);
				PlayerUtils.resetPlayerXP(p);
				p.setGameMode(GameMode.SPECTATOR);
				p.playSound(p.getLocation(), Sound.WITHER_DEATH, 1F, 1F);
			}

			endTasks();

			Bukkit.getScheduler().scheduleSyncDelayedTask(MCFUHC.getInstance(), new Runnable() {
				@Override
				public void run() {
					for (Player p : Bukkit.getServer().getOnlinePlayers()) {
						p.sendMessage(ChatColor.AQUA + "Sending you to the lobby in 20 seconds");
					}

					Bukkit.getScheduler().scheduleSyncDelayedTask(MCFUHC.getInstance(), new Runnable() {
						@Override
						public void run() {
							new SlowPlayerSender(Bukkit.getServer().getOnlinePlayers(), getLobbyServer()).setCallback(new Callback() {
								@Override
								public void execute() {
									Bukkit.getScheduler().scheduleSyncDelayedTask(MCFUHC.getInstance(), new Runnable() {
										@Override
										public void run() {
											for (Player p : Bukkit.getServer().getOnlinePlayers()) {
												p.kickPlayer(ChatColor.AQUA + "UHC restarting, Please reconnect");
											}
											Bukkit.getServer().shutdown();
										}
									}, 40L);
								}
							}).start();
						}
					}, 400);
				}
			}, 100);
			break;

		default:
			break;
		}
	}

	public void setCage(Location location, boolean state) {
		Location tmpLocation = location.clone();
		Material material = state ? Material.GLASS : Material.AIR;

		tmpLocation.add(0, -1, 0);

		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				tmpLocation.clone().add(x, 0, z).getBlock().setType(material);
			}
		}

		for (int y = 0; y < 3; y++) {
			tmpLocation.add(0, 1, 0);
			for (int i = -1; i <= 1; i++) {
				tmpLocation.clone().add(i, 0, -2).getBlock().setType(material);
				tmpLocation.clone().add(i, 0, 2).getBlock().setType(material);
				tmpLocation.clone().add(-2, 0, i).getBlock().setType(material);
				tmpLocation.clone().add(2, 0, i).getBlock().setType(material);
			}
		}

		tmpLocation.add(0, 1, 0);

		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				tmpLocation.clone().add(x, 0, z).getBlock().setType(material);
			}
		}
	}

	public void endTasks() {
		if (arenaUpdateTaskId != -1) {
			Bukkit.getScheduler().cancelTask(arenaUpdateTaskId);
			arenaUpdateTaskId = -1;
		}

		if (gracePeriodCountdown != null) {
			if (!gracePeriodCountdown.isFinished()) {
				gracePeriodCountdown.stop();
			}
		}
		
		if(borderShrinkTask != null) {
			if(borderShrinkTask.isRunning()) {
				borderShrinkTask.cancel();
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();

		if (p.getGameMode() == GameMode.CREATIVE) {
			return;
		}

		if (stage == Stage.WAITING || stage == Stage.GENERATING_WORLD) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerPortal(PlayerPortalEvent event) {
		Player player = event.getPlayer();

		if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
			event.useTravelAgent(true);
			event.getPortalTravelAgent().setCanCreatePortal(true);
			Location location;
			if (player.getWorld() == getWorld()) {
				location = new Location(netherWorld, event.getFrom().getBlockX() / 8, event.getFrom().getBlockY(), event.getFrom().getBlockZ() / 8);

				player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Warning: If your portal is outside the world border when you return to the world you will get insta killed!");
			} else {
				location = new Location(getWorld(), event.getFrom().getBlockX() * 8, event.getFrom().getBlockY(), event.getFrom().getBlockZ() * 8);
			}
			event.setTo(event.getPortalTravelAgent().findOrCreate(location));
		}
	}

	@EventHandler
	public void onEntityRegainHealth(EntityRegainHealthEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getRegainReason() == RegainReason.SATIATED || e.getRegainReason() == RegainReason.REGEN) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getPlayer().getLocation().getY() <= -1) {
			Player p = e.getPlayer();
			if (stage == Stage.WAITING || stage == Stage.GENERATING_WORLD) {
				p.setFallDistance(0);
				tpToLobby(p);
			} else {
				if (p.getGameMode() == GameMode.SPECTATOR) {
					tpToSpectator(p);
				} else {
					e.getPlayer().setHealth(0);
				}
			}
		}
	}

	@Override
	public void onPlayerJoin(Player player, boolean reconnected) {
		if (stage == Stage.GENERATING_WORLD || stage == Stage.WAITING) {
			PlayerUtils.clearPlayerInventory(player);
			PlayerUtils.resetPlayerXP(player);
			player.getInventory().addItem(MCFCore.getInstance().getCustomItemManager().getItem(CustomCraftingBook.class));

			if (MCFCore.getInstance().getTeamManager().getPlayerTeam(player) != null && (stage == Stage.WAITING || stage == Stage.GENERATING_WORLD)) {
				if (!players.contains(player.getUniqueId())) {
					players.add(player.getUniqueId());
				}
				player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Joined as player");
			} else {
				player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Joined as spectator");
			}
			player.setMaxHealth(20);
			player.setHealth(20);
			player.setFoodLevel(20);
			player.setGameMode(GameMode.ADVENTURE);
			tpToLobby(player);
		} else {
			if (reconnected) {
				player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Reconnected");
				player.setGameMode(GameMode.SURVIVAL);
			} else {
				player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Joined as spectator");
				tpToSpectator(player);
			}
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			if (stage == Stage.GENERATING_WORLD || stage == Stage.WAITING) {
				e.setCancelled(true);
				return;
			}
			if (tpNoDamage) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
		if (e.getPlayer() == null) {
			return;
		}

		if (stage == Stage.GENERATING_WORLD || stage == Stage.WAITING) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if (e.getPlayer() == null) {
			return;
		}

		if (e.getPlayer().getGameMode() == GameMode.CREATIVE) {
			return;
		}

		if (stage != Stage.INGAME) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if (e.getPlayer() == null) {
			return;
		}

		if (e.getPlayer().getGameMode() == GameMode.CREATIVE) {
			return;
		}

		if (stage != Stage.INGAME) {
			e.setCancelled(true);
		}
	}

	@Override
	public void onPlayerQuit(Player player) {
		if(stage == Stage.WAITING || stage == Stage.GENERATING_WORLD) {
			if(players.contains(player.getUniqueId())) {
				players.remove(player.getUniqueId());
			}
		}
	}
	
	@Override
	public boolean eliminatePlayerOnDeath() {
		return stage == Stage.INGAME;
	}

	@Override
	public boolean eliminatePlayerOnQuit() {
		return stage == Stage.INGAME;
	}
	
	@Override
	public EliminationType getEliminationType() {
		return EliminationType.DELAYED;
	}

	@Override
	public boolean autoEndGame() {
		return true;
	}

	@Override
	public void endGame() {
		setStage(Stage.END);
	}

	@Override
	public String getName() {
		return "UHC";
	}
}