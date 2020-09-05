package xyz.mcfridays.mcf.games.hungergamesv2.Gamemode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import net.badlion.timers.api.Timer;
import xyz.mcfridays.mcf.games.hungergamesv2.MCFHungergames;
import xyz.mcfridays.mcf.games.hungergamesv2.Map.HungergamesMap;
import xyz.mcfridays.mcf.games.hungergamesv2.utils.BigHologramText;
import xyz.mcfridays.mcf.mcfcommons.utils.Callback;
import xyz.mcfridays.mcf.mcfcommons.utils.TimerCallback;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Events.PlayerEliminatedEvent;
import xyz.mcfridays.mcf.mcfcore.Game.EliminationType;
import xyz.mcfridays.mcf.mcfcore.Game.Game;
import xyz.mcfridays.mcf.mcfcore.Loot.LootTable;
import xyz.mcfridays.mcf.mcfcore.Teams.Team;
import xyz.mcfridays.mcf.mcfcore.Utils.PlayerUtils;
import xyz.mcfridays.mcf.mcfcore.Utils.RandomFireworkEffect;
import xyz.mcfridays.mcf.mcfcore.Utils.SlowPlayerSender;
import xyz.mcfridays.mcf.mcfcore.Worldborder.WorldBorderShrinkMode;
import xyz.mcfridays.mcf.mcfcore.Worldborder.WorldBorderShrinkTask;
import xyz.mcfridays.mcf.mcfcore.timers.BasicTimer;
import xyz.mcfridays.mcf.mcfcore.timers.ScoreboardTimer;

public class HungergamesGamemode extends Game {
	private Stage stage;

	private boolean countdownInChat = true;

	private boolean noFallEnabled;

	private int countdown;
	private BasicTimer countdownTimer;

	private int dropTaskId;
	private int refillTaskId;

	private ScoreboardTimer borderShrinkCountdown;

	private int arenaUpdateTaskId;

	private ArrayList<Hologram> countdownHolograms;

	private HashMap<UUID, Location> usedStartLocation;

	private Timer badlionTimer;

	private final boolean randomStartLocation = false;
	
	private WorldBorderShrinkTask borderShrinkTask;

	public HungergamesGamemode(World world) {
		super(world);

		this.dropTaskId = -1;
		this.refillTaskId = -1;

		this.countdown = 0;
		this.countdownHolograms = new ArrayList<Hologram>();

		this.noFallEnabled = false;

		this.usedStartLocation = new HashMap<UUID, Location>();

		this.badlionTimer = null;

		this.borderShrinkTask = null;
		
		setStage(Stage.WAITING);

		this.arenaUpdateTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFHungergames.getInstance(), new Runnable() {
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

		this.setKillReward(20);
		this.setWinScore(100, 75, 50);
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

	public boolean setCages(boolean state) {
		if (!hasMap()) {
			return false;
		}

		for (Location location : getMap().getStartLocations()) {
			setStartCage(location, state);
		}

		return true;
	}

	public void setStartCage(Location location, boolean state) {
		Material material = state ? Material.BARRIER : Material.AIR;

		location.clone().add(1, 0, 0).getBlock().setType(material);
		location.clone().add(-1, 0, 0).getBlock().setType(material);
		location.clone().add(0, 0, 1).getBlock().setType(material);
		location.clone().add(0, 0, -1).getBlock().setType(material);

		location.clone().add(1, 1, 0).getBlock().setType(material);
		location.clone().add(-1, 1, 0).getBlock().setType(material);
		location.clone().add(0, 1, 1).getBlock().setType(material);
		location.clone().add(0, 1, -1).getBlock().setType(material);

		location.clone().add(0, 2, 0).getBlock().setType(material);
	}

	@Override
	protected void onUnload() {
		if (arenaUpdateTaskId != -1) {
			Bukkit.getScheduler().cancelTask(arenaUpdateTaskId);
		}

		endTasks();
	}

	@Override
	public LootTable getChestLootTable() {
		if (MCFHungergames.getInstance().hasActiveMap()) {
			return MCFCore.getInstance().getLootTableManager().getLootTable(MCFHungergames.getInstance().getActiveMap().getMapData().getChestLootTableName());
		}
		return null;
	}

	@Override
	public LootTable getDropLootTable() {
		if (MCFHungergames.getInstance().hasActiveMap()) {
			return MCFCore.getInstance().getLootTableManager().getLootTable(MCFHungergames.getInstance().getActiveMap().getMapData().getDropLootTableName());
		}
		return null;
	}

	@Override
	public LootTable getEnderChestLootTable() {
		if (MCFHungergames.getInstance().hasActiveMap()) {
			return MCFCore.getInstance().getLootTableManager().getLootTable(MCFHungergames.getInstance().getActiveMap().getMapData().getEnderChestLootTable());
		}
		return null;
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
		player.teleport(MCFHungergames.getInstance().getLobbyLocation());
		player.setGameMode(GameMode.ADVENTURE);
	}

	@Override
	public void tpToArena(Player player) {
		if (hasMap()) {
			for (int i = 0; i < getMap().getStartLocations().size(); i++) {
				Location location = getMap().getStartLocations().get(i);

				if (usedStartLocation.containsValue(location)) {
					continue;
				}

				this.tpToArena(player, location);
				return;
			}
			Random random = new Random();
			Location backupLocation = getMap().getStartLocations().get(random.nextInt(getMap().getStartLocations().size()));

			this.tpToArena(player, backupLocation);
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
		usedStartLocation.put(player.getUniqueId(), location);
		PlayerUtils.clearPlayerInventory(player);
		PlayerUtils.clearPotionEffects(player);
		PlayerUtils.resetPlayerXP(player);
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		player.teleport(location);
		player.setGameMode(GameMode.SURVIVAL);
	}

	/**
	 * Teleport a player to the spectator location
	 */
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

	public boolean startBadlionTickTimer(String name, ItemStack item, boolean looping, int ticks) {
		return startBadlionTimer(MCFHungergames.getInstance().getTimerApi().createTickTimer(name, item, looping, ticks));
	}

	public boolean startBadlionTimeTimer(String name, ItemStack item, boolean looping, int time, TimeUnit timeUnit) {
		return startBadlionTimer(MCFHungergames.getInstance().getTimerApi().createTimeTimer(name, item, looping, time, timeUnit));
	}

	public boolean startBadlionTimer(Timer timer) {
		if (badlionTimer != null) {
			return false;
		}

		this.badlionTimer = timer;

		for (Player p : Bukkit.getOnlinePlayers()) {
			badlionTimer.addReceiver(p);
		}

		return true;
	}

	public boolean stopBadlionTimer() {
		if (badlionTimer == null) {
			return false;
		}

		for (Player p : badlionTimer.getReceivers()) {
			badlionTimer.removeReceiver(p);
		}

		badlionTimer = null;

		return true;
	}

	public boolean hasBadlionTimer() {
		return badlionTimer != null;
	}

	public Timer getBadlionTimer() {
		return badlionTimer;
	}

	/**
	 * Get arena {@link Stage}
	 * 
	 * @return active arena {@link Stage}
	 */
	public Stage getStage() {
		return stage;
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

		if (randomStartLocation) {
			Collections.shuffle(getMap().getStartLocations());
			Collections.shuffle(toTeleport);
		} else {
			// i known this code is trash but o don't have time to create a better way right
			// now
			ArrayList<Integer> teamOrder = new ArrayList<Integer>();

			for (int i = 0; i < MCFCore.getInstance().getTeamManager().getTeams().size(); i++) {
				teamOrder.add(i + 1);
			}

			Collections.shuffle(teamOrder);

			ArrayList<Player> toTeleportReal = new ArrayList<Player>();

			for (Integer i : teamOrder) {
				for (Player pt : toTeleport) {
					if (MCFCore.getInstance().getTeamManager().getPlayerTeam(pt).getTeamNumber() == i) {
						toTeleportReal.add(pt);
					}
				}
			}

			toTeleport = toTeleportReal;
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

	/**
	 * Set arena {@link Stage} and run any related actions
	 * 
	 * @param stage new {@link Stage}
	 * @throws IllegalStateException if stage gets changed without a map loaded
	 */
	public void setStage(Stage stage) {
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

			int initialSize = getMap().getMapData().getWorldborderData().getStartSize() + 20;
			getMap().getWorld().getWorldBorder().setCenter(getMap().getMapData().getWorldborderData().getCenterX(), getMap().getMapData().getWorldborderData().getCenterZ());
			getMap().getWorld().getWorldBorder().setSize(initialSize);
			getMap().getWorld().getWorldBorder().setDamageAmount(5);
			getMap().getWorld().getWorldBorder().setDamageBuffer(1);
			Bukkit.getServer().broadcast(ChatColor.YELLOW + "DEBUG: set world border initial size to " + initialSize, "mcf.errorlog");

			getMap().getWorld().setTime(1000);

			for (Entity entity : getMap().getWorld().getEntities()) {
				if (entity instanceof Item) {
					entity.remove();
				}
			}
			
			Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "May the odds be ever in your favor");
			Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Countdown started");
			
			if(getMap().hasNoBuildZone()) {
				Bukkit.getServer().broadcastMessage(getMap().getNoBouldZone().getMassage());
			}

			for (Location location : getMap().getCountdownLocations()) {
				Hologram hologram = HologramsAPI.createHologram(MCFHungergames.getInstance(), location);
				countdownHolograms.add(hologram);
			}

			countdown = MCFHungergames.getInstance().getStartCountdown();

			countdownTimer = new BasicTimer(countdown);

			countdownTimer.setTickCallback(new TimerCallback() {
				@Override
				public void execute(int timeLeft) {
					BigHologramText.setText(countdownHolograms, timeLeft + "", 36, 30, ChatColor.RED, ChatColor.GRAY);

					for (Player p : Bukkit.getServer().getOnlinePlayers()) {
						p.playSound(p.getLocation(), Sound.NOTE_PLING, 1F, 1.5F);
					}

					if (countdownInChat) {
						Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "Starting in " + timeLeft);
					}
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

					BigHologramText.setText(countdownHolograms, countdown + "", 36, 30, ChatColor.RED, ChatColor.GRAY);

					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(MCFHungergames.getInstance(), new Runnable() {
						@Override
						public void run() {
							for (Hologram hologram : countdownHolograms) {
								hologram.clearLines();
								hologram.delete();
							}
						}
					}, 40L);

					setStage(Stage.INGAME);
				}
			});

			countdownTimer.start();

			Bukkit.getScheduler().scheduleSyncDelayedTask(MCFHungergames.getInstance(), new Runnable() {
				@Override
				public void run() {
					Bukkit.getServer().broadcast(ChatColor.YELLOW + "DEBUG: calling resetWorldborder()", "mcf.errorlog");
					resetWorldborder();

					Bukkit.getScheduler().scheduleSyncDelayedTask(MCFHungergames.getInstance(), new Runnable() {
						@Override
						public void run() {
							Bukkit.getServer().broadcast(ChatColor.YELLOW + "DEBUG: Worldborder size is " + getWorldborder().getSize() + " if this number is not close to " + getMap().getMapData().getWorldborderData().getStartSize() + " please set it mannualy with " + ChatColor.AQUA + "/worldborder set " + getMap().getMapData().getWorldborderData().getStartSize(), "mcf.errorlog");
							Bukkit.getScheduler().scheduleSyncDelayedTask(MCFHungergames.getInstance(), new Runnable() {
								@Override
								public void run() {
									Bukkit.getServer().broadcast(ChatColor.YELLOW + "DEBUG: Final check. Worldborder size is " + getWorldborder().getSize() + " if this number is not close to " + getMap().getMapData().getWorldborderData().getStartSize() + " please set it mannualy with " + ChatColor.AQUA + "/worldborder set " + getMap().getMapData().getWorldborderData().getStartSize(), "mcf.errorlog");
								}
							}, 600L);
						}
					}, 20L);
				}
			}, 40L);

			break;

		case INGAME:
			for (Location location : getMap().getStartLocations()) {
				setStartCage(location, false);
			}

			startRandomDropTask();
			startRandomRefillTask();

			if (borderShrinkCountdown != null) {
				if (!borderShrinkCountdown.isFinished()) {
					borderShrinkCountdown.stop();
				}
			}

			startBadlionTimeTimer("Border shrink in", new ItemStack(Material.BARRIER), false, getMap().getMapData().getWorldborderData().getShrinkStartTime(), TimeUnit.SECONDS);
			borderShrinkCountdown = new ScoreboardTimer(getMap().getMapData().getWorldborderData().getShrinkStartTime(), ChatColor.GOLD + "World border: " + ChatColor.AQUA, 7);
			borderShrinkCountdown.setCallback(new Callback() {

				@Override
				public void execute() {
					startBorderShrink();
				}
			});

			borderShrinkCountdown.start();

			if (getMap().getMapData().getNoFallDuration() > 0) {
				noFallEnabled = true;
				for (Player p : Bukkit.getOnlinePlayers()) {
					p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Fall damage disabled for " + getMap().getMapData().getNoFallDuration() + " seconds");
				}

				Bukkit.getScheduler().scheduleSyncDelayedTask(MCFHungergames.getInstance(), new Runnable() {
					@Override
					public void run() {
						noFallEnabled = false;
						for (Player p : Bukkit.getOnlinePlayers()) {
							p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Fall damage enabled");
						}
					}
				}, getMap().getMapData().getNoFallDuration() * 20);
			}
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

			stopBadlionTimer();

			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				p.setHealth(p.getMaxHealth());
				p.setFoodLevel(20);
				PlayerUtils.clearPlayerInventory(p);
				PlayerUtils.resetPlayerXP(p);
				p.setGameMode(GameMode.SPECTATOR);
				p.playSound(p.getLocation(), Sound.WITHER_DEATH, 1F, 1F);
			}

			endTasks();

			Bukkit.getScheduler().scheduleSyncDelayedTask(MCFHungergames.getInstance(), new Runnable() {
				@Override
				public void run() {
					for (Player p : Bukkit.getServer().getOnlinePlayers()) {
						p.sendMessage(ChatColor.AQUA + "Sending you to the lobby in 5 seconds");
					}

					Bukkit.getScheduler().scheduleSyncDelayedTask(MCFHungergames.getInstance(), new Runnable() {
						@Override
						public void run() {
							new SlowPlayerSender(Bukkit.getServer().getOnlinePlayers(), getLobbyServer()).setCallback(new Callback() {
								@Override
								public void execute() {
									Bukkit.getScheduler().scheduleSyncDelayedTask(MCFHungergames.getInstance(), new Runnable() {
										@Override
										public void run() {
											for (Player p : Bukkit.getServer().getOnlinePlayers()) {
												p.kickPlayer(ChatColor.AQUA + "Hungergames restarting, Please reconnect");
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

		default:
			break;
		}
	}

	public void resetWorldborder() {
		getWorldborder().setCenter(getMap().getMapData().getWorldborderData().getCenterX(), getMap().getMapData().getWorldborderData().getCenterZ());
		getWorldborder().setSize(getMap().getMapData().getWorldborderData().getStartSize());
		getWorldborder().setDamageBuffer(1);
		getWorldborder().setDamageAmount(5);
	}

	private void endTasks() {
		if(borderShrinkTask!= null) {
			if(borderShrinkTask.isRunning()) {
				borderShrinkTask.cancel();
			}
		}
		
		if (borderShrinkCountdown != null) {
			if (!borderShrinkCountdown.isFinished()) {
				borderShrinkCountdown.stop();
			}
		}

		if (countdownTimer != null) {
			if (!countdownTimer.isFinished()) {
				countdownTimer.stop();
			}
		}

		if (dropTaskId != -1) {
			Bukkit.getScheduler().cancelTask(dropTaskId);
			dropTaskId = -1;
		}

		if (refillTaskId != -1) {
			Bukkit.getScheduler().cancelTask(refillTaskId);
			refillTaskId = -1;
		}
	}

	private void startRandomDropTask() {
		if (dropTaskId != -1) {
			return;
		}

		if (stage != Stage.INGAME) {
			return;
		}

		int minTime = 200;
		int maxTime = 1000;

		Random random = new Random();

		int time = minTime + random.nextInt(maxTime - minTime);

		System.out.println("Next drop in " + time + " seconds");

		dropTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(MCFHungergames.getInstance(), new Runnable() {
			@Override
			public void run() {
				dropTaskId = -1;

				spawnLootDrop();

				startRandomDropTask();
			}
		}, time * 20);
	}

	private void startRandomRefillTask() {
		if (refillTaskId != -1) {
			return;
		}

		if (stage != Stage.INGAME) {
			return;
		}

		int minTime = 200;
		int maxTime = 1000;

		Random random = new Random();

		int time = minTime + random.nextInt(maxTime - minTime);

		System.out.println("Next refill in " + time + " seconds");

		refillTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(MCFHungergames.getInstance(), new Runnable() {
			@Override
			public void run() {
				refillTaskId = -1;

				refillChests(true);

				startRandomRefillTask();
			}
		}, time * 20);
	}

	public boolean spawnLootDrop() {
		if (getMap().getDropLocations().size() > 0) {
			Random random = new Random();
			for (int i = 0; i < 10; i++) {
				Location location = getMap().getDropLocations().get(random.nextInt(getMap().getDropLocations().size()));
				if (MCFCore.getInstance().getLootDropManager().canSpawnAt(location)) {
					this.spawnLootDrop(location);
					return true;
				}
			}
		}

		return false;
	}

	public void startBorderShrink() {
		stopBadlionTimer();
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "World border is starting to shrink");
		}
		borderShrinkTask = new WorldBorderShrinkTask(getMap().getWorld(), getMap().getMapData().getWorldborderData().getStartSize(), getMap().getMapData().getWorldborderData().getEndSize(), getMap().getMapData().getWorldborderData().getShrinkDuration(), getMap().getMapData().getWorldborderData().getShrinkStepTime(),  WorldBorderShrinkMode.API);
		borderShrinkTask.start();
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public boolean hasMap() {
		return MCFHungergames.getInstance().hasActiveMap();
	}

	public HungergamesMap getMap() {
		return MCFHungergames.getInstance().getActiveMap();
	}

	public void checkTeamPlace(Team team) {
		boolean teamIsAlive = false;
		for (UUID uuid : team.getTeamMembers()) {
			if (players.contains(uuid)) {
				teamIsAlive = true;
				break;
			}
		}

		if (!teamIsAlive) {
			int reward = 0;
			ArrayList<Integer> teamsLeft = new ArrayList<Integer>();
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				if (players.contains(player.getUniqueId())) {
					Team team2 = MCFCore.getInstance().getTeamManager().getPlayerTeam(player);
					if (team2 != null) {
						if (!teamsLeft.contains((Integer) team2.getTeamNumber())) {
							teamsLeft.add((Integer) team2.getTeamNumber());
						}
					}
				}
			}

			String extra = "";

			if (teamsLeft.size() == 1) {
				reward = 60;
				extra = ChatColor.GOLD + "" + ChatColor.BOLD + " Second place!";
			} else if (teamsLeft.size() == 2) {
				reward = 40;
				extra = ChatColor.GOLD + "" + ChatColor.BOLD + " Third place!";
			}

			if (reward > 0) {
				extra += ChatColor.GOLD + "" + ChatColor.BOLD + " +" + reward + " team points";
			}

			for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
				p2.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Team eliminated> " + team.getTeamColor() + ChatColor.BOLD + "Team " + team.getTeamNumber() + " " + extra);
			}

			if (reward > 0) {
				MCFCore.getInstance().getScoreManager().addTeamScore(team, reward);
				for (UUID uuid : team.getTeamMembers()) {
					MCFCore.getInstance().getScoreManager().addScore(uuid, Math.round(reward / team.getTeamMembers().size()), false);
				}
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
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getPlayer().getLocation().getY() <= 0) {
			if (stage == Stage.WAITING) {
				e.getPlayer().teleport(MCFHungergames.getInstance().getLobbyLocation());
			} else {
				e.getPlayer().teleport(getMap().getSpectatorLocation());
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();

		if (p.getGameMode() == GameMode.CREATIVE) {
			return;
		}

		if (stage != Stage.INGAME) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
		Player p = e.getPlayer();

		if (p.getGameMode() != GameMode.CREATIVE) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (stage == Stage.WAITING) {
			Player p = (Player) e.getWhoClicked();
			if (p.getGameMode() != GameMode.CREATIVE) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		if (stage == Stage.WAITING) {
			Player p = e.getPlayer();
			if (p.getGameMode() != GameMode.CREATIVE) {
				e.setCancelled(true);
			}
		}
	}

	@Override
	public void onPlayerJoin(Player player, boolean reconnected) {
		if (badlionTimer != null) {
			badlionTimer.addReceiver(player);
		}

		if (stage == Stage.WAITING) {
			PlayerUtils.clearPlayerInventory(player);
			PlayerUtils.resetPlayerXP(player);

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
		if (badlionTimer != null) {
			if (badlionTimer.getReceivers().contains(player)) {
				badlionTimer.removeReceiver(player);
			}
		}

		if (usedStartLocation.containsKey(player.getUniqueId())) {
			usedStartLocation.remove(player.getUniqueId());
		}

		if (stage == Stage.WAITING) {
			if (players.contains(player.getUniqueId())) {
				players.remove(player.getUniqueId());
			}
		}
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

	@Override
	public void onPlayerDeath(Player player, LivingEntity killer) {
		Location location = player.getLocation();
		player.spigot().respawn();
		player.teleport(location);
		player.setGameMode(GameMode.SPECTATOR);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();

		if (hasMap()) {
			if (getMap().getMapData().getPlaceableBlocks().contains(e.getBlock().getType())) {
				if (getMap().hasNoBuildZone()) {
					if (getMap().isInsideNoBuildZone(e.getBlock().getLocation())) {
						e.setCancelled(true);
						p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You cant build here");
					}
				}
				return;
			}
		}

		if (p.getGameMode() != GameMode.CREATIVE) {
			if (e.getBlock().getType() == Material.WORKBENCH) {
				p.openWorkbench(null, true);
				e.setCancelled(true);
				return;
			}

			e.setCancelled(true);
			p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You cant build with that block in this map");
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();

		if (hasMap()) {
			if (getMap().getMapData().getBreakableBlocks().contains(e.getBlock().getType())) {
				return;
			}
		}

		if (p.getGameMode() != GameMode.CREATIVE) {
			e.setCancelled(true);
		}
	}

	@Override
	public void endGame() {
		this.setStage(Stage.END);
	}

	@Override
	public boolean autoEndGame() {
		return true;
	}

	@Override
	public String getName() {
		return "Hunger games";
	}
}