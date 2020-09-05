package xyz.mcfridays.games.skywars.gamemode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import xyz.mcfridays.games.skywars.MCFSkywars;
import xyz.mcfridays.games.skywars.map.SkywarsMap;
import xyz.mcfridays.mcf.mcfcommons.utils.Callback;
import xyz.mcfridays.mcf.mcfcommons.utils.TimerCallback;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Events.ChestFillEvent;
import xyz.mcfridays.mcf.mcfcore.Events.PlayerEliminatedEvent;
import xyz.mcfridays.mcf.mcfcore.Game.EliminationType;
import xyz.mcfridays.mcf.mcfcore.Game.Game;
import xyz.mcfridays.mcf.mcfcore.Log.ErrorLogger;
import xyz.mcfridays.mcf.mcfcore.Loot.LootTable;
import xyz.mcfridays.mcf.mcfcore.Loot.ChestLoot.ChestType;
import xyz.mcfridays.mcf.mcfcore.Teams.Team;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;
import xyz.mcfridays.mcf.mcfcore.Utils.PlayerUtils;
import xyz.mcfridays.mcf.mcfcore.Utils.RandomFireworkEffect;
import xyz.mcfridays.mcf.mcfcore.Utils.SlowPlayerSender;
import xyz.mcfridays.mcf.mcfcore.timers.ScoreboardTimer;

public class SkywarsGamemode extends Game implements Listener {
	private Stage stage;

	private boolean noFallEnabled;

	private ScoreboardTimer countdownTimer;
	private ScoreboardTimer refillTimer;

	private ArrayList<Location> teamStartLocation;

	private int arenaUpdateTaskId;

	private ArrayList<Location> placedBlocks;

	private static final double ISLAND_CHEST_RADIUS = 7;

	public SkywarsGamemode() {
		super(Bukkit.getServer().getWorlds().get(0));

		this.noFallEnabled = false;
		this.teamStartLocation = new ArrayList<Location>();

		this.setKillReward(20);
		this.setWinScore(100, 75, 50);

		this.placedBlocks = new ArrayList<Location>();

		setStage(Stage.WAITING);

		this.arenaUpdateTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFSkywars.getInstance(), new Runnable() {
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

		Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFSkywars.getInstance(), new Runnable() {
			@Override
			public void run() {
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					if (p.getHealth() > 0) {
						if (p.getLocation().getY() <= 0) {
							if (stage == Stage.INGAME || stage == Stage.END) {
								if (p.getGameMode() != GameMode.SPECTATOR && p.getGameMode() != GameMode.CREATIVE) {
									PlayerUtils.clearPlayerInventory(p);
									p.teleport(getMap().getSpectatorLocation());
									p.setHealth(0);
								}
							} else if (stage == Stage.WAITING) {
								p.setFallDistance(0);
								tpToLobby(p);
							}
						}
					}
				}

				if (stage == Stage.INGAME || stage == Stage.END) {
					for (Player p : Bukkit.getServer().getWorlds().get(0).getPlayers()) {
						tpToSpectator(p);
					}
				}
			}
		}, 2L, 2L);
	}

	/**
	 * Changes {@link Stage} from {@link Stage#WAITING} to {@link Stage#COUNTDOWN}
	 */
	public void start() {
		if (stage != Stage.WAITING) {
			return;
		}

		if (!hasMap()) {
			throw new IllegalStateException("Tried to start game but no map has been loaded");
		}

		Collections.shuffle(getMap().getStartLocations());
		System.out.println("Start location: " + getMap().getStartLocations().size());
		for (int i = 0; i < 12; i++) {
			teamStartLocation.add(getMap().getStartLocations().get(i));
		}

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
			PlayerUtils.resetPlayerXP(p);

			p.setExp(0);
			p.setLevel(0);
		}

		for (Location location : getMap().getStartLocations()) {
			setStartCage(location, true);
		}

		for (Player p : toTeleport) {
			try {
				this.tpToArena(p);
			} catch (Exception e) {
				p.sendMessage(ChatColor.DARK_RED + "Teleport failed: " + e.getClass().getName() + ". Please contact an admin");
			}
		}

		setStage(Stage.COUNTDOWN);
	}

	private void setStage(Stage stage) {
		if (stage != Stage.WAITING) {
			if (!hasMap()) {
				throw new IllegalStateException("Tried to change stage to " + stage.name() + " but no map has been loaded");
			}
		}

		this.stage = stage;
		switch (stage) {
		case WAITING:
			break;
		case COUNTDOWN:
			try {
				MCFCore.getInstance().setServerAsActive(true);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			for (UUID uuid : players) {
				Player p = Bukkit.getServer().getPlayer(uuid);
				if (p != null) {
					p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 6000, 0, false, false), true);
				}
			}

			countdownTimer = new ScoreboardTimer(20, ChatColor.GOLD + "Starting in: " + ChatColor.AQUA, 9);

			countdownTimer.setTickCallback(new TimerCallback() {
				@Override
				public void execute(int timeLeft) {

					for (Player p : Bukkit.getServer().getOnlinePlayers()) {
						p.playSound(p.getLocation(), Sound.NOTE_PLING, 0.5F, 1.5F);
					}

					Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "Starting in " + timeLeft);
				}
			});

			countdownTimer.setCallback(new Callback() {
				@Override
				public void execute() {
					for (Location location : getMap().getStartLocations()) {
						setStartCage(location, false);
					}

					for (Player p : Bukkit.getServer().getOnlinePlayers()) {
						p.playSound(p.getLocation(), Sound.NOTE_PLING, 1F, 2F);
					}

					setStage(Stage.INGAME);
				}
			});

			countdownTimer.start();

			break;
		case INGAME:
			for (UUID uuid : players) {
				Player p = Bukkit.getServer().getPlayer(uuid);
				if (p != null) {
					p.getInventory().addItem(new ItemBuilder(Material.IRON_SWORD).build());
					p.getInventory().addItem(new ItemBuilder(Material.IRON_PICKAXE).build());
					p.getInventory().addItem(new ItemBuilder(Material.IRON_AXE).build());
				}
			}

			if (getMap().getMapData().getNoFallDuration() > 0) {
				noFallEnabled = true;
				Bukkit.getScheduler().scheduleSyncDelayedTask(MCFSkywars.getInstance(), new Runnable() {
					@Override
					public void run() {
						noFallEnabled = false;
					}
				}, getMap().getMapData().getNoFallDuration() * 20);
			}

			for (Location location : getMap().getStartLocations()) {
				setStartCage(location, false);
			}

			startRefillTimer();

			break;
		case END:
			try {
				MCFCore.getInstance().setServerAsActive(false);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			for (Location location : getMap().getStartLocations()) {
				Firework fw = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
				FireworkMeta fwm = fw.getFireworkMeta();

				fwm.setPower(2);
				fwm.addEffect(RandomFireworkEffect.randomFireworkEffect());

				fw.setFireworkMeta(fwm);
			}

			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				p.setHealth(p.getMaxHealth());
				p.setFoodLevel(20);
				PlayerUtils.clearPlayerInventory(p);
				PlayerUtils.resetPlayerXP(p);
				p.setGameMode(GameMode.SPECTATOR);
				p.playSound(p.getLocation(), Sound.WITHER_DEATH, 1F, 1F);
			}

			endTasks();

			Bukkit.getScheduler().scheduleSyncDelayedTask(MCFSkywars.getInstance(), new Runnable() {
				@Override
				public void run() {
					for (Player p : Bukkit.getServer().getOnlinePlayers()) {
						p.sendMessage(ChatColor.AQUA + "Sending you to the lobby in 5 seconds");
					}

					Bukkit.getScheduler().scheduleSyncDelayedTask(MCFSkywars.getInstance(), new Runnable() {
						@Override
						public void run() {
							new SlowPlayerSender(Bukkit.getServer().getOnlinePlayers(), getLobbyServer()).setCallback(new Callback() {
								@Override
								public void execute() {
									Bukkit.getScheduler().scheduleSyncDelayedTask(MCFSkywars.getInstance(), new Runnable() {
										@Override
										public void run() {
											for (Player p : Bukkit.getServer().getOnlinePlayers()) {
												p.kickPlayer(ChatColor.AQUA + "Skywars restarting, Please reconnect");
											}
											Bukkit.getServer().shutdown();
										}
									}, 40L);
								}
							}).start();
						}
					}, 110);
				}
			}, 100);
			break;

		}
	}

	@Override
	protected void onUnload() {
		endTasks();
	}

	private void endTasks() {
		if (refillTimer != null) {
			if (!refillTimer.isFinished()) {
				refillTimer.stop();
			}
			refillTimer = null;
		}

		if (arenaUpdateTaskId != -1) {
			Bukkit.getScheduler().cancelTask(arenaUpdateTaskId);
			arenaUpdateTaskId = -1;
		}
	}

	private void startRefillTimer() {
		if (refillTimer != null) {
			if (!refillTimer.isFinished()) {
				refillTimer.stop();
			}
			refillTimer = null;
		}

		refillTimer = new ScoreboardTimer(300, ChatColor.GOLD + "" + ChatColor.BOLD + "Refill in: " + ChatColor.AQUA + "" + ChatColor.BOLD, 9);

		refillTimer.setCallback(new Callback() {
			@Override
			public void execute() {
				if (stage == Stage.INGAME) {
					Bukkit.getScheduler().scheduleSyncDelayedTask(MCFSkywars.getInstance(), new Runnable() {
						@Override
						public void run() {
							startRefillTimer();
						}
					}, 20L);
				}

				refillChests(true);

				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					p.playSound(p.getLocation(), Sound.NOTE_PLING, 1F, 2F);
				}
			}
		});

		refillTimer.start();
	}

	public void setStartCage(Location location, boolean state) {
		Material material = state ? Material.BARRIER : Material.AIR;

		for (int x = -2; x < 3; x++) {
			for (int y = 0; y < 5; y++) {
				for (int z = -2; z < 3; z++) {
					location.clone().add(x, y - 1, z).getBlock().setType(material);
				}
			}
		}

		if (state == true) {
			for (int x = -1; x < 2; x++) {
				for (int y = 0; y < 3; y++) {
					for (int z = -1; z < 2; z++) {
						location.clone().add(x, y, z).getBlock().setType(Material.AIR);
					}
				}
			}
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
	public void onPlayerJoin(Player player, boolean reconnected) {
		if (stage == Stage.WAITING) {
			PlayerUtils.clearPlayerInventory(player);
			PlayerUtils.resetPlayerXP(player);
			
			player.removePotionEffect(PotionEffectType.ABSORPTION);

			ItemStack voteItem = new ItemStack(Material.EMPTY_MAP);

			ItemMeta voteMeta = voteItem.getItemMeta();
			voteMeta.setDisplayName(ChatColor.GREEN + "Vote for map");
			voteItem.setItemMeta(voteMeta);

			player.getInventory().addItem(voteItem);

			if (MCFCore.getInstance().getTeamManager().getPlayerTeam(player) != null) {
				if (!players.contains(player.getUniqueId())) {
					players.add(player.getUniqueId());
				}
				player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Joined as player");
			} else {
				player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Joined as spectator");
			}

			tpToLobby(player);
		} else {
			if (reconnected) {
				player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Reconnected");
				player.setGameMode(GameMode.SURVIVAL);
			} else {
				player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Joined as spectator");
				if (hasMap()) {
					tpToSpectator(player);
				}
			}
		}
	}

	@Override
	public void onPlayerQuit(Player player) {
		if (stage == Stage.WAITING) {
			if (players.contains(player.getUniqueId())) {
				players.remove(player.getUniqueId());
			}
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			if (stage != Stage.INGAME) {
				e.setCancelled(true);
				return;
			}

			if (e.getCause() == DamageCause.FALL) {
				if (noFallEnabled) {
					e.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();

		if (p.getGameMode() == GameMode.CREATIVE) {
			// Allow
			return;
		}

		if (e.getBlock().getType() == Material.CHEST || e.getBlock().getType() == Material.TRAPPED_CHEST || e.getBlock().getType() == Material.ENDER_CHEST) {
			// Deny
			p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You are not allowed to place that block");
			e.setCancelled(true);
			return;
		}

		if (stage == Stage.INGAME) {
			if (hasMap()) {
				if (getMap().hasNoBuildZone()) {
					if (getMap().isInsideNoBuildZone(e.getBlock().getLocation())) {
						// Deny
						e.setCancelled(true);
						p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You can't build here");
						return;
					}
				}
			} else {
				// Deny
				e.setCancelled(true);
				return;
			}
		} else {
			// Deny
			e.setCancelled(true);
		}

		// Allow
		if (!placedBlocks.contains(e.getBlock().getLocation())) {
			placedBlocks.add(e.getBlock().getLocation());
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		/*
		 * Player p = e.getPlayer();
		 * 
		 * if (stage == Stage.INGAME) { if (hasMap()) { if
		 * (getMap().getMapData().getBreakableBlocks().contains(e.getBlock().getType())
		 * || placedBlocks.contains(e.getBlock().getLocation())) { return; } } }
		 * 
		 * e.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD +
		 * "You can't break that block here");
		 * 
		 * if (p.getGameMode() != GameMode.CREATIVE) { e.setCancelled(true); }
		 */
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public boolean hasMap() {
		return MCFSkywars.getInstance().hasActiveMap();
	}

	public SkywarsMap getMap() {
		return MCFSkywars.getInstance().getActiveMap();
	}

	public Stage getStage() {
		return stage;
	}

	@Override
	public boolean pvpEnabled() {
		return stage == Stage.INGAME;
	}

	@Override
	public boolean killRewardEnabled() {
		return stage == Stage.INGAME;
	}

	@Override
	public void tpToLobby(Player player) {
		player.teleport(MCFSkywars.getInstance().getLobbyLocation());
		player.setGameMode(GameMode.ADVENTURE);

	}

	@Override
	public LootTable getChestLootTable() {
		if (MCFSkywars.getInstance().hasActiveMap()) {
			return MCFCore.getInstance().getLootTableManager().getLootTable(MCFSkywars.getInstance().getActiveMap().getMapData().getChestLootTableName());
		}
		return null;
	}

	public LootTable getIslandLootTable() {
		if (MCFSkywars.getInstance().hasActiveMap()) {
			return MCFCore.getInstance().getLootTableManager().getLootTable(MCFSkywars.getInstance().getActiveMap().getMapData().getIslandLootTable());
		}
		return null;
	}

	@Override
	public LootTable getEnderChestLootTable() {
		if (MCFSkywars.getInstance().hasActiveMap()) {
			return MCFCore.getInstance().getLootTableManager().getLootTable(MCFSkywars.getInstance().getActiveMap().getMapData().getEnderChestLootTable());
		}
		return null;
	}

	@Override
	public void tpToArena(Player player) {
		if (hasMap()) {
			Team team = MCFCore.getInstance().getTeamManager().getPlayerTeam(player);

			if (team != null) {
				try {
					Location location = teamStartLocation.get(team.getTeamNumber() - 1);
					tpToArena(player, location);
				} catch (Exception e) {
					ErrorLogger.logException(e, "tpToArena() Player: " + player.getName());
					player.sendMessage(ChatColor.RED + "Tp failure ERR:EXCEPTION");
				}
			} else {
				player.sendMessage(ChatColor.RED + "Tp failure ERR:TEAM_NULL");
			}
		} else {
			System.err.println("tpToArena() called without map");
		}
	}

	/**
	 * Teleport a player to a provided start location
	 * 
	 * @param player   {@link Player} to teleport
	 * @param location {@link Location} to teleport the player to
	 */
	protected void tpToArena(Player player, Location location) {
		player.teleport(location.getWorld().getSpawnLocation());
		PlayerUtils.clearPlayerInventory(player);
		PlayerUtils.clearPotionEffects(player);
		PlayerUtils.resetPlayerXP(player);
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		player.teleport(location);
		player.setGameMode(GameMode.SURVIVAL);
	}

	@Override
	public void tpToSpectator(Player player) {
		if (hasMap()) {
			PlayerUtils.clearPlayerInventory(player);
			PlayerUtils.clearPotionEffects(player);
			PlayerUtils.resetPlayerXP(player);
			player.setGameMode(GameMode.SPECTATOR);
			player.setHealth(player.getMaxHealth());
			player.teleport(getMap().getSpectatorLocation());
		} else {
			System.err.println("tpToSpectator() called without map");
		}
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
		return "Skywars";
	}

	@Override
	public boolean eliminatePlayerOnDeath() {
		return stage == Stage.INGAME || stage == Stage.COUNTDOWN;
	}

	@Override
	public boolean eliminatePlayerOnQuit() {
		return stage == Stage.INGAME || stage == Stage.COUNTDOWN;
	}

	@Override
	public EliminationType getEliminationType() {
		return EliminationType.DELAYED;
	}

	@EventHandler
	public void onChestFill(ChestFillEvent e) {
		if (e.getChestType() == ChestType.CHEST) {
			Location location = e.getLocation();

			for (Location spawnLocation : getMap().getStartLocations()) {
				Location l2 = spawnLocation.clone();

				l2.setY(location.getY());

				if (location.distance(l2) <= ISLAND_CHEST_RADIUS) {
					e.setLootTable(getIslandLootTable());
					break;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		Location dropLocation = p.getLocation().clone();
		
		for (ItemStack stack : p.getInventory().getContents()) {
			if (stack == null) {
				continue;
			}

			dropLocation.getWorld().dropItem(dropLocation, stack);
		}
		
		PlayerUtils.clearPlayerInventory(p);
		
		e.getEntity().teleport(getMap().getSpectatorLocation());
		Bukkit.getScheduler().scheduleSyncDelayedTask(MCFSkywars.getInstance(), new Runnable() {
			@Override
			public void run() {
				p.spigot().respawn();
			}
		}, 2);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		if (stage == Stage.INGAME || stage == Stage.END) {
			tpToSpectator(e.getPlayer());
		}
	}

	public boolean setCages(boolean state) {
		if (!hasMap()) {
			return false;
		}

		for (Location location : getMap().getStartLocations()) {
			setStartCage(location, state);
		}

		return true;
	}
}