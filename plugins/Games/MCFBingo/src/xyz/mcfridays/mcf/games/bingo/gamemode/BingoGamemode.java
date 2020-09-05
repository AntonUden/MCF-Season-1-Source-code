package xyz.mcfridays.mcf.games.bingo.gamemode;

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
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import xyz.mcfridays.mcf.games.bingo.MCFBingo;
import xyz.mcfridays.mcf.games.bingo.items.BingoBookItem;
import xyz.mcfridays.mcf.mcfcommons.utils.Callback;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.GUI.ReadOnlyGUIInventoryHolder;
import xyz.mcfridays.mcf.mcfcore.Game.EliminationType;
import xyz.mcfridays.mcf.mcfcore.Game.Game;
import xyz.mcfridays.mcf.mcfcore.Teams.Team;
import xyz.mcfridays.mcf.mcfcore.Utils.BlockUtils;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;
import xyz.mcfridays.mcf.mcfcore.Utils.PlayerUtils;
import xyz.mcfridays.mcf.mcfcore.Utils.SlowPlayerSender;
import xyz.mcfridays.mcf.mcfcore.timers.DelayedTask;
import xyz.mcfridays.mcf.mcfcore.timers.ScoreboardTimer;
import xyz.mcfridays.mcf.mcfcore.world.WorldPreGenerator;

public class BingoGamemode extends Game {
	private Stage stage;

	private int worldSizeChunks;
	private int generatorSpeed;

	private HashMap<Integer, Location> teamLocations;

	private ScoreboardTimer gameTimer;

	private WorldPreGenerator preGenerator;
	private int generatorProgressTaskId;

	private World netherWorld;

	private boolean tpNoDamage;

	private ArrayList<ItemStack> targetItems;

	private HashMap<Integer, ArrayList<Integer>> teamCompletedItems;
	private HashMap<Integer, Inventory> teamMenu;

	private int scanTaskId;

	private static final int completeItemScore = 20;
	private static final int completeGameInitialScore = 100;

	private int finishedTeamCount;

	public BingoGamemode(int worldSizeChunks, int generatorSpeed) {
		super(Bukkit.getServer().getWorlds().get(0));
		this.stage = null;

		this.worldSizeChunks = worldSizeChunks;
		this.generatorSpeed = generatorSpeed;

		this.preGenerator = null;
		this.generatorProgressTaskId = -1;

		this.netherWorld = null;

		this.tpNoDamage = false;

		this.teamLocations = new HashMap<Integer, Location>();

		this.gameTimer = null;

		this.scanTaskId = -1;

		this.finishedTeamCount = 0;

		this.targetItems = BingoItemGenerator.generate();
		this.teamMenu = new HashMap<Integer, Inventory>();
		this.teamCompletedItems = new HashMap<Integer, ArrayList<Integer>>();

		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(MCFBingo.getInstance(), new Runnable() {
			@Override
			public void run() {
				if (world != null) {
					for (Player p : Bukkit.getOnlinePlayers()) {
						if (p.getGameMode() == GameMode.SPECTATOR) {
							if (p.getWorld().getName().equalsIgnoreCase("bingo_world")) {
								int x = (int) p.getLocation().getX();
								int z = (int) p.getLocation().getZ();

								if (x < 0) {
									x *= -1;
								}

								if (z < 0) {
									z *= -1;
								}

								if (x > (world.getWorldBorder().getSize() / 2) + 10 || z > (world.getWorldBorder().getSize() / 2) + 10) {
									p.teleport(world.getSpawnLocation());
								}
							}
						}
					}
				}
			}
		}, 20L, 20L);
	}

	public void createInventory(int team) {
		if (!teamCompletedItems.containsKey(team)) {
			teamCompletedItems.put(team, new ArrayList<Integer>());
		}

		Inventory inventory = Bukkit.createInventory(new ReadOnlyGUIInventoryHolder(), 9 * 3, "Bingo");

		ItemStack backgroundItem = new ItemBuilder(Material.STAINED_GLASS_PANE).setName(" ").build();
		for (int i = 0; i < inventory.getSize(); i++) {
			inventory.setItem(i, backgroundItem);
		}

		teamMenu.put(team, inventory);

		updateInventory(team);
	}

	public boolean updateInventory(int team) {
		if (!teamMenu.containsKey(team)) {
			return false;
		}

		Inventory inventory = teamMenu.get(team);

		for (int i = 0; i < 9; i++) {
			int row = (((i) / 3) % 3) + 1;

			int itemSlot = (3 * row) + i + ((row - 1) * 3);

			if (targetItems.size() > i) {
				ItemBuilder iconBuilder = new ItemBuilder(targetItems.get(i).clone());

				if (teamCompletedItems.get(team).contains(i)) {
					iconBuilder.addLore(ChatColor.AQUA + "Completed");
					iconBuilder.addItemFlags(ItemFlag.HIDE_ENCHANTS);
					iconBuilder.addEnchant(Enchantment.DURABILITY, 1, true);
				} else {
					iconBuilder.addLore(ChatColor.AQUA + "Pick one up to complete");
				}

				inventory.setItem(itemSlot, iconBuilder.build());
			}
		}

		return true;
	}

	public ArrayList<ItemStack> getTargetItems() {
		return targetItems;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent e) {
		Player player = e.getPlayer();

		checkItem(e.getItem().getItemStack(), player);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCraftItem(CraftItemEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			Player player = (Player) e.getWhoClicked();

			checkItem(e.getCurrentItem(), player);
		}
	}

	private void checkItem(ItemStack item, Player player) {
		Team team = MCFCore.getInstance().getTeamManager().getPlayerTeam(player);

		if (team != null) {

			for (int i = 0; i < targetItems.size(); i++) {
				if (teamCompletedItems.get(team.getTeamNumber()).contains(i)) {
					// System.out.println(player.getName() + " has already completed " +
					// targetItems.get(i).getType() + " id: " + i);
					continue;
				}

				ItemStack targetItem = targetItems.get(i);

				if (item.getType() == targetItem.getType()) {
					// System.out.println(e.getItem().getItemStack().getData() + " and " +
					// targetItem.getData());
					if (item.getData().equals(targetItem.getData())) {
						teamCompletedItems.get(team.getTeamNumber()).add(i);
						updateInventory(team.getTeamNumber());

						Bukkit.getServer().broadcastMessage(team.getTeamColor() + "" + ChatColor.BOLD + "Team " + team.getTeamNumber() + ChatColor.GREEN + "" + ChatColor.BOLD + " found " + teamCompletedItems.get(team.getTeamNumber()).size() + " / 9 items");

						team.addScore(completeItemScore);
						for (UUID uuid : team.getTeamMembers()) {
							MCFCore.getInstance().getScoreManager().addScore(uuid, (int) Math.ceil(BingoGamemode.completeItemScore / team.getTeamMembers().size()), false);
							Player p2 = Bukkit.getPlayer(uuid);
							if (p2 != null) {
								if (p2.isOnline()) {
									p2.playSound(p2.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
									p2.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "+" + completeItemScore + " points added to you team");
								}
							}
						}

						if (teamCompletedItems.get(team.getTeamNumber()).size() >= 9) {
							int score = BingoGamemode.completeGameInitialScore - (10 * finishedTeamCount);
							if (score < 40) {
								score = 40;
							}
							Bukkit.getServer().broadcastMessage(team.getTeamColor() + "" + ChatColor.BOLD + "Team " + team.getTeamNumber() + ChatColor.GREEN + "" + ChatColor.BOLD + " found all the items. " + ChatColor.GRAY + ChatColor.BOLD + "+" + score + " score");
							team.addScore(score);

							for (UUID uuid : team.getTeamMembers()) {
								MCFCore.getInstance().getScoreManager().addScore(uuid, (int) Math.ceil(score / team.getTeamMembers().size()), false);
								Player p2 = Bukkit.getPlayer(uuid);
								if (p2 != null) {
									if (p2.isOnline()) {
										p2.setGameMode(GameMode.SPECTATOR);
										p2.playSound(p2.getLocation(), Sound.LEVEL_UP, 1F, 1F);
									}
								}

								if (players.contains(uuid)) {
									players.remove(uuid);
								}
							}
							finishedTeamCount++;

							return;
						}
					}
				}
			}
		}
	}

	@Override
	public void onTeamPlace(Team team, int place, int score) {
		// Bingo manages team placement in another way
	}

	@Override
	protected void onLoad() {
		setStage(Stage.GENERATING_WORLD);
	}

	@Override
	protected void onUnload() {
		if (scanTaskId != -1) {
			Bukkit.getScheduler().cancelTask(scanTaskId);
			scanTaskId = -1;
		}
		try {
			MCFCore.getInstance().setServerAsActive(false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public boolean pvpEnabled() {
		return false;
	}

	@Override
	public boolean killRewardEnabled() {
		return false;
	}

	@Override
	public void tpToLobby(Player player) {
		player.setMaxHealth(20);
		player.setHealth(20);
		player.setGameMode(GameMode.ADVENTURE);
		player.teleport(MCFBingo.getInstance().getLobbyLocation());
		player.setFireTicks(0);
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

	private void tpToArenaLocation(Player player, Location location) {
		player.teleport(new Location(world, BlockUtils.blockCenter(location.getBlockX()), location.getY() + 1, BlockUtils.blockCenter(location.getBlockZ())));
		player.setGameMode(GameMode.SURVIVAL);
		player.setMaxHealth(20);
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		player.setFireTicks(0);

		player.getInventory().addItem(MCFCore.getInstance().getCustomItemManager().getItem(BingoBookItem.class));
	}

	public Location tryGetSpawnLocation() {
		int max = (worldSizeChunks * 16) - 50;

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

	public boolean teamDone(int team) {
		if (teamCompletedItems.containsKey(team)) {
			return teamCompletedItems.get(team).size() >= 9;
		}

		return false;
	}

	@Override
	public void tpToSpectator(Player player) {
		player.setGameMode(GameMode.SPECTATOR);
		player.teleport(world.getSpawnLocation());
	}

	@Override
	public boolean autoEndGame() {
		return false;
	}

	@Override
	public void endGame() {
		setStage(Stage.END);
	}

	@Override
	public String getName() {
		return "Bingo";
	}

	private void setStage(Stage stage) {
		this.stage = stage;

		switch (stage) {
		case GENERATING_WORLD:
			String worldName = "bingo_world";

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

			World bingoWorld = Bukkit.getServer().createWorld(worldCreator);

			WorldCreator netherWorldCreator = new WorldCreator(worldName + "_nether");
			netherWorldCreator.environment(Environment.NETHER);
			netherWorldCreator.seed(bingoWorld.getSeed());

			netherWorld = Bukkit.getServer().createWorld(netherWorldCreator);
			
			preGenerator = new WorldPreGenerator(bingoWorld, worldSizeChunks + 10, generatorSpeed, new Callback() {
				@Override
				public void execute() {
					Bukkit.getScheduler().cancelTask(generatorProgressTaskId);
					MCFCore.getInstance().getMcfScoreboardManager().deleteCustomLine(7);
					setStage(Stage.WAITING);
				}
			});

			generatorProgressTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFBingo.getInstance(), new Runnable() {
				@Override
				public void run() {
					MCFCore.getInstance().getMcfScoreboardManager().setCustomLine(7, ChatColor.GOLD + "Loading world " + ChatColor.AQUA + ((int) (((double) preGenerator.getProgress() / (double) preGenerator.getTotal()) * 100)) + "%");
					System.out.println("World generation " + preGenerator.getProgress() + "/" + preGenerator.getTotal() + " " + ((int) (((double) preGenerator.getProgress() / (double) preGenerator.getTotal()) * 100)) + "%");
				}
			}, 20L, 20L);
			preGenerator.start();
			this.world = bingoWorld;
			this.world.getWorldBorder().setCenter(0, 0);
			this.world.getWorldBorder().setSize(worldSizeChunks * 16 * 2);
			this.world.getWorldBorder().setWarningDistance(5);
			
			bingoWorld.setGameRuleValue("keepInventory", "true");
			netherWorld.setGameRuleValue("keepInventory", "true");
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

			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 2400, 0), false);
				for (Achievement achievement : Achievement.values()) {
					if (p.hasAchievement(achievement)) {
						p.removeAchievement(achievement);
					}
				}
			}

			gameTimer = new ScoreboardTimer(2400, ChatColor.AQUA + "Time left: ", 7);

			gameTimer.setCallback(new Callback() {
				@Override
				public void execute() {
					ArrayList<Integer> teamsFailed = new ArrayList<Integer>();
					for (UUID uuid : players) {
						OfflinePlayer p = Bukkit.getServer().getOfflinePlayer(uuid);
						if (p != null) {
							Team team = MCFCore.getInstance().getTeamManager().getPlayerTeam(p);

							if (!teamsFailed.contains(team.getTeamNumber())) {
								teamsFailed.add(team.getTeamNumber());

								Bukkit.getServer().broadcastMessage(team.getTeamColor() + "" + ChatColor.BOLD + "Team " + team.getTeamNumber() + ChatColor.RED + ChatColor.BOLD + " did not find all items");
							}

							if (p.isOnline()) {
								Player player = p.getPlayer();

								player.playSound(player.getLocation(), Sound.WITHER_HURT, 1F, 1F);
							}
						}
					}
					setStage(Stage.END);
				}
			});

			gameTimer.start();

			scanTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFBingo.getInstance(), new Runnable() {
				@Override
				public void run() {
					runCheck();
				}

				private void runCheck() {
					int playerSize = players.size();
					for (UUID uuid : players) {
						Player p = Bukkit.getServer().getPlayer(uuid);

						if (p != null) {
							if (p.isOnline()) {
								for (ItemStack item : p.getInventory().getContents()) {
									if (item != null) {
										checkItem(item, p);
									}
								}
							}
						}

						if (players.size() != playerSize) {
							System.out.println("Player size changed. Calling runCheck() recursively");
							runCheck();
							return;
						}
					}

					if (players.size() == 0) {
						Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "All teams have found all the items");
						setStage(Stage.END);
					}
				}
			}, 20L, 20L);
			break;

		case END:
			try {
				MCFCore.getInstance().setServerAsActive(false);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			if (gameTimer != null) {
				if (!gameTimer.isFinished()) {
					gameTimer.stop();
				}
			}

			Bukkit.getScheduler().scheduleSyncDelayedTask(MCFBingo.getInstance(), new Runnable() {
				@Override
				public void run() {
					for (Player p : Bukkit.getServer().getOnlinePlayers()) {
						p.sendMessage(ChatColor.AQUA + "Sending you to the lobby in 20 seconds");
					}

					Bukkit.getScheduler().scheduleSyncDelayedTask(MCFBingo.getInstance(), new Runnable() {
						@Override
						public void run() {
							new SlowPlayerSender(Bukkit.getServer().getOnlinePlayers(), getLobbyServer()).setCallback(new Callback() {
								@Override
								public void execute() {
									Bukkit.getScheduler().scheduleSyncDelayedTask(MCFBingo.getInstance(), new Runnable() {
										@Override
										public void run() {
											for (Player p : Bukkit.getServer().getOnlinePlayers()) {
												p.kickPlayer(ChatColor.AQUA + "Bingo restarting, Please reconnect");
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

			if (scanTaskId != -1) {
				Bukkit.getScheduler().cancelTask(scanTaskId);
				scanTaskId = -1;
			}
			break;

		default:
			break;
		}
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

	@EventHandler
	public void onPlayerPortal(PlayerPortalEvent event) {
		Player player = event.getPlayer();

		if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
			event.useTravelAgent(true);
			event.getPortalTravelAgent().setCanCreatePortal(true);
			Location location;
			if (player.getWorld() == getWorld()) {
				location = new Location(netherWorld, event.getFrom().getBlockX() / 8, event.getFrom().getBlockY(), event.getFrom().getBlockZ() / 8);
			} else {
				location = new Location(getWorld(), event.getFrom().getBlockX() * 8, event.getFrom().getBlockY(), event.getFrom().getBlockZ() * 8);
			}
			event.setTo(event.getPortalTravelAgent().findOrCreate(location));
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
		boolean isSpectator = false;
		if (MCFCore.getInstance().getTeamManager().getPlayerTeam(player) != null && (stage == Stage.WAITING || stage == Stage.GENERATING_WORLD)) {
			if (!players.contains(player.getUniqueId())) {
				players.add(player.getUniqueId());
			}

			Team team = MCFCore.getInstance().getTeamManager().getPlayerTeam(player);

			if (!teamMenu.containsKey(team.getTeamNumber())) {
				createInventory(team.getTeamNumber());
			}

			player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Joined as player");
		} else {
			if (!players.contains(player.getUniqueId())) {
				player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Joined as spectator");
				isSpectator = true;
			}	
		}

		if (stage == Stage.GENERATING_WORLD || stage == Stage.WAITING) {
			PlayerUtils.clearPlayerInventory(player);
			PlayerUtils.resetPlayerXP(player);

			player.setMaxHealth(20);
			player.setHealth(20);
			player.setFoodLevel(20);
			player.setGameMode(GameMode.ADVENTURE);
			player.setFireTicks(0);
			tpToLobby(player);
		} else {
			if (isSpectator) {
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
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Team team = MCFCore.getInstance().getTeamManager().getPlayerTeam(e.getPlayer());

		if (team != null) {
			if (teamLocations.containsKey(team.getTeamNumber())) {
				e.setRespawnLocation(teamLocations.get(team.getTeamNumber()));
			}
		}
	}

	@Override
	public void onPlayerDeath(Player player, LivingEntity killer) {
		player.spigot().respawn();
		player.setFireTicks(0);
	}

	@EventHandler
	public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
		if (e.getPlayer() == null) {
			return;
		}

		if (e.getPlayer().getGameMode() == GameMode.CREATIVE) {
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
		if(stage == Stage.GENERATING_WORLD || stage == Stage.WAITING) {
			if(players.contains(player.getUniqueId())) {
				players.remove(player.getUniqueId());
			}
		}
	}
	
	public Inventory getTeamMenu(Team team) {
		return this.getTeamMenu(team.getTeamNumber());
	}

	public Inventory getTeamMenu(int team) {
		return teamMenu.get(team);
	}

	@Override
	public boolean eliminatePlayerOnQuit() {
		return stage == Stage.INGAME;
	}

	@Override
	public boolean eliminatePlayerOnDeath() {
		return false;
	}

	@Override
	public EliminationType getEliminationType() {
		return EliminationType.DELAYED;
	}

	@Override
	public int getEliminationDelay() {
		return 300;
	}
}