package xyz.mcfridays.mcf.games.dropper.Gamemode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import xyz.mcfridays.mcf.games.dropper.MCFDropper;
import xyz.mcfridays.mcf.mcfcommons.utils.Callback;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Game.Game;
import xyz.mcfridays.mcf.mcfcore.Log.ErrorLogger;
import xyz.mcfridays.mcf.mcfcore.Scoreboard.ScoreData.TeamScoreData;
import xyz.mcfridays.mcf.mcfcore.Teams.Team;
import xyz.mcfridays.mcf.mcfcore.Utils.PlayerUtils;
import xyz.mcfridays.mcf.mcfcore.Utils.SlowPlayerSender;

public class DropperGamemode extends Game implements Listener {
	private Location lobbyLocation;
	private Stage arenaStage;

	private int activeMapNumber;
	private ArrayList<MapConfiguration> maps;

	private int locationCheckTaskId;
	private int timerTaskId;

	private int mapMaxTime;
	private int mapCountdown;

	private boolean roundActive;

	private ArrayList<UUID> completed;

	private HashMap<Integer, Integer> teamScore;

	public DropperGamemode(World world, int mapMaxTime) {
		super(world);

		this.mapMaxTime = mapMaxTime;
		this.mapCountdown = mapMaxTime;

		this.arenaStage = Stage.WAITING;
		this.activeMapNumber = 0;
		this.maps = new ArrayList<MapConfiguration>();
		this.completed = new ArrayList<UUID>();
		this.roundActive = false;

		this.teamScore = new HashMap<Integer, Integer>();

		this.timerTaskId = -1;

		world.setDifficulty(Difficulty.PEACEFUL);
		world.setTime(1000);
		world.setStorm(false);
		;
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
		player.setHealth(player.getMaxHealth());
		player.removePotionEffect(PotionEffectType.INVISIBILITY);
		PlayerUtils.clearPlayerInventory(player);
		player.setGameMode(GameMode.ADVENTURE);
		player.teleport(lobbyLocation);
		player.setFallDistance(0);
	}

	@Override
	public void tpToArena(Player player) {
		MapConfiguration map = getActiveMap();

		if (map == null) {
			return;
		}
		if (players.contains(player.getUniqueId())) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 1, false, false), true);
			player.setGameMode(GameMode.ADVENTURE);
			player.setHealth(10);
			player.setMaxHealth(10);
			player.setFireTicks(0);
			player.teleport(map.getSpawnLocation());
			player.setFallDistance(0);
		} else {
			this.tpToSpectator(player);
		}
	}

	@Override
	public void tpToSpectator(Player player) {
		MapConfiguration map = getActiveMap();

		if (map == null) {
			return;
		}

		player.setGameMode(GameMode.SPECTATOR);
		player.teleport(map.getSpectatorLocation());
	}

	@Override
	protected void onLoad() {
		setArenaStage(Stage.WAITING);
		world.setDifficulty(Difficulty.PEACEFUL);

		this.locationCheckTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFDropper.getInstance(), new Runnable() {

			@Override
			public void run() {
				if (arenaStage == Stage.INGAME) {

					MapConfiguration map = getActiveMap();

					if (map == null) {
						return;
					}

					for (Player p : Bukkit.getServer().getOnlinePlayers()) {
						if (!map.isInsideArena(p.getLocation())) {
							if (completed.contains(p.getUniqueId())) {
								tpToSpectator(p);
							} else {
								tpToArena(p);
							}
						}
					}

					ChatColor color;

					if (mapCountdown > mapMaxTime / 2) {
						color = ChatColor.GREEN;
					} else if (mapCountdown > mapMaxTime / 3) {
						color = ChatColor.YELLOW;
					} else {
						color = ChatColor.RED;
					}

					MCFCore.getInstance().getMcfScoreboardManager().setCustomLine(8, color + "Time left: " + String.format("%02d:%02d", mapCountdown / 60, mapCountdown % 60));
				} else {
					MCFCore.getInstance().getMcfScoreboardManager().setCustomLine(8, ChatColor.DARK_GRAY + "Time left: --:--");
				}
				MCFCore.getInstance().getMcfScoreboardManager().setCustomLine(1, ChatColor.DARK_GRAY + "Map " + (arenaStage == Stage.WAITING ? "--" : (activeMapNumber + 1)) + "/" + (arenaStage == Stage.WAITING ? "--" : maps.size()));
			}
		}, 5L, 5L);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		PlayerUtils.clearPlayerInventory(p);

		if (MCFCore.getInstance().getTeamManager().getPlayerTeam(p) != null) {
			if (!getPlayers().contains(p.getUniqueId())) {
				getPlayers().add(p.getUniqueId());
			}
			p.sendMessage(ChatColor.GREEN + "Joined as player");
			if (arenaStage == Stage.INGAME) {
				tpToArena(p);
			} else {
				tpToLobby(p);
			}
		} else {
			p.sendMessage(ChatColor.GREEN + "Joined as spectator");
			if (arenaStage == Stage.INGAME) {
				tpToSpectator(p);
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();

		if (players.contains(p.getUniqueId())) {
			players.remove(p.getUniqueId());
		}

		checkPlayersLeft();
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (roundActive) {
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (e.getClickedBlock().getType() == Material.SIGN_POST || e.getClickedBlock().getType() == Material.WALL_SIGN) {
					if (e.getPlayer().getGameMode() != GameMode.SPECTATOR) {
						if (e.getClickedBlock().getState() instanceof Sign) {
							Sign sign = (Sign) e.getClickedBlock().getState();
							if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[Dropper]")) {
								MapConfiguration map = getActiveMap();

								if (map == null) {
									return;
								}

								Player p = e.getPlayer();

								if (completed.contains(p.getUniqueId())) {
									return;
								}

								Team pt = MCFCore.getInstance().getTeamManager().getPlayerTeam(p);
								ChatColor pc = ChatColor.YELLOW;

								if (pt == null) {
									return;
								}

								pc = pt.getTeamColor();

								int score = (completed.size() < 5 ? 10 - completed.size() : 5);

								completed.add(p.getUniqueId());
								p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 1);

								p.setGameMode(GameMode.SPECTATOR);
								p.teleport(map.getSpawnLocation());

								Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Completed> " + pc + ChatColor.BOLD + p.getName() + ChatColor.AQUA + ChatColor.GOLD + " +" + score + " dropper points");

								// MCFCore.getInstance().getScoreManager().addScore(p, score, true);
								int oldScore = 0;
								if (this.teamScore.containsKey(pt.getTeamNumber())) {
									oldScore = this.teamScore.get(pt.getTeamNumber());
								}
								this.teamScore.put(pt.getTeamNumber(), oldScore + score);

								checkPlayersLeft();
							}
						}
					}
				}
			}
		}
	}

	private void checkPlayersLeft() {
		boolean allCompleted = true;
		for (UUID uuid : getPlayers()) {
			if (!completed.contains(uuid)) {
				allCompleted = false;
				break;
			}
		}

		if (allCompleted) {
			mapCountdown = 0;
		}
	}

	public ArrayList<TeamScoreData> getTopTeams(int maxEntries) {
		ArrayList<TeamScoreData> result = getTopTeams();
		while (result.size() > maxEntries) {
			result.remove(result.size() - 1);
		}

		return result;
	}

	public ArrayList<TeamScoreData> getTopTeams() {
		ArrayList<TeamScoreData> result = new ArrayList<TeamScoreData>();

		for (Integer i : teamScore.keySet()) {

			TeamScoreData scoreData = new TeamScoreData(MCFCore.getInstance().getTeamManager().getTeam(i), teamScore.get(i));
			result.add(scoreData);
		}

		Collections.sort(result);

		return result;
	}

	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		if (e.getLine(0).equalsIgnoreCase("[dropper]")) {
			e.setLine(0, ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "[Dropper]");
			e.setLine(1, ChatColor.DARK_GREEN + "Finish");
			e.setLine(2, "");
			e.setLine(3, "");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			if(arenaStage == Stage.WAITING) {
				e.setCancelled(true);
				return;
			}
			
			Player p = (Player) e.getEntity();
			if (e.getDamage() >= p.getHealth()) {
				e.setCancelled(true);
				if (arenaStage == Stage.INGAME) {
					tpToArena(p);
				}
			}
		}
	}

	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e) {
		if (e.toWeatherState() == true) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		e.getEntity().spigot().respawn();
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();

		if (arenaStage == Stage.INGAME) {
			tpToArena(p);
		}
	}

	public void setArenaStage(Stage stage) {
		arenaStage = stage;
		Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "Set stage to " + stage.name());

		switch (stage) {
		case WAITING:

			break;

		case INGAME:
			try {
				MCFCore.getInstance().setServerAsActive(true);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			activeMapNumber = 0;
			startRound();
			break;

		case END:
			try {
				MCFCore.getInstance().setServerAsActive(false);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			ArrayList<TeamScoreData> result = getTopTeams();

			HashMap<Integer, Integer> finalTeamScore = new HashMap<Integer, Integer>();

			Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "-=-=-= Results =-=-=-");
			for (int i = 0; i < result.size(); i++) {
				try {
					int scoreVal = 0;
					switch (i) {
					case 0:
						scoreVal = 100;
						break;

					case 1:
						scoreVal = 75;
						break;

					case 2:
						scoreVal = 50;
						break;

					default:
						scoreVal = 25;
						break;
					}

					TeamScoreData teamResult = result.get(i);

					finalTeamScore.put(teamResult.getTeamNumber(), scoreVal);

					Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "" + ChatColor.BOLD + (i + 1) + ": " + teamResult.getTeam().getTeamColor() + ChatColor.BOLD + teamResult.getTeam().getMemberString() + ChatColor.GOLD + ChatColor.BOLD + " : " + teamResult.getScore() + " " + ChatColor.AQUA + "" + ChatColor.BOLD + "+" + scoreVal + " points");

					teamResult.getTeam().addScore(scoreVal);

					ArrayList<Player> participants = new ArrayList<Player>();
					for (UUID member : teamResult.getTeam().getTeamMembers()) {
						Player p = Bukkit.getServer().getPlayer(member);

						if (p != null) {
							if (p.isOnline()) {
								participants.add(p);
							}
						}
					}

					if (participants.size() > 0) {
						int playerScore = (int) Math.ceil(scoreVal / participants.size());

						for (Player p : participants) {
							MCFCore.getInstance().getScoreManager().addScore(p, playerScore, false);
						}
					}
				} catch (Exception e) {
					ErrorLogger.logException(e, "DropperGamemode:setArenaStage(Stage.END)");
				}
			}

			try {
				for (Integer i : finalTeamScore.keySet()) {
					Team team = MCFCore.getInstance().getTeamManager().getTeam(i);

					if (team != null) {
						for (UUID uuid : team.getTeamMembers()) {
							Player p = Bukkit.getServer().getPlayer(uuid);

							if (p != null) {
								p.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "Your team received " + finalTeamScore.get(i) + " points");
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			Bukkit.getScheduler().scheduleSyncDelayedTask(MCFDropper.getInstance(), new Runnable() {
				@Override
				public void run() {
					for (Player p : Bukkit.getServer().getOnlinePlayers()) {
						p.sendMessage(ChatColor.AQUA + "Sending you to the lobby in 5 seconds");
					}

					Bukkit.getScheduler().scheduleSyncDelayedTask(MCFDropper.getInstance(), new Runnable() {
						@Override
						public void run() {
							new SlowPlayerSender(Bukkit.getServer().getOnlinePlayers(), getLobbyServer()).setCallback(new Callback() {
								@Override
								public void execute() {
									Bukkit.getScheduler().scheduleSyncDelayedTask(MCFDropper.getInstance(), new Runnable() {
										@Override
										public void run() {
											for (Player p : Bukkit.getServer().getOnlinePlayers()) {
												p.kickPlayer(ChatColor.AQUA + "Dropper restarting, Please reconnect");
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

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (arenaStage == Stage.WAITING) {
			if (e.getTo().getBlockY() < 10) {
				e.getPlayer().setFallDistance(0);
				tpToLobby(e.getPlayer());
			}
		}
	}

	@Override
	protected void onUnload() {
		if (locationCheckTaskId != -1) {
			Bukkit.getScheduler().cancelTask(locationCheckTaskId);
		}
	}

	public void start() {
		if (arenaStage == Stage.WAITING) {
			setArenaStage(Stage.INGAME);
		}
	}

	public boolean startRound() {
		mapCountdown = mapMaxTime;
		MapConfiguration map = getActiveMap();
		if (map == null) {
			return false;
		}
		completed.clear();
		mapCountdown = mapMaxTime;
		if (timerTaskId == -1) {
			timerTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFDropper.getInstance(), new Runnable() {
				@Override
				public void run() {
					mapCountdown--;
					if (mapCountdown <= 0) {
						Bukkit.getScheduler().cancelTask(timerTaskId);
						timerTaskId = -1;

						roundActive = false;

						for (UUID uuid : players) {
							Player p = Bukkit.getPlayer(uuid);
							Team pt = MCFCore.getInstance().getTeamManager().getPlayerTeam(p);

							if (pt != null) {

								ChatColor pc = ChatColor.YELLOW;

								if (pt != null) {
									pc = pt.getTeamColor();
								}

								if (!completed.contains(uuid)) {
									p.playSound(p.getLocation(), Sound.WITHER_HURT, 1, 1);

									tpToSpectator(p);

									Bukkit.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Failed> " + pc + ChatColor.BOLD + p.getName());
								} else {
									p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 1);
								}
							}
						}

						if (!hasNextMap()) {
							setArenaStage(Stage.END);
							return;
						}

						Bukkit.getScheduler().scheduleSyncDelayedTask(MCFDropper.getInstance(), new Runnable() {
							@Override
							public void run() {
								nextRound();
							}
						}, 40);
					}
				}
			}, 20L, 20L);
		}

		for (Player p : Bukkit.getOnlinePlayers()) {
			tpToArena(p);
		}

		roundActive = true;

		return true;
	}

	public void nextRound() {
		if (hasNextMap()) {
			activeMapNumber++;

			startRound();
		}
	}

	public boolean hasNextMap() {
		return !(activeMapNumber >= maps.size() - 1);
	}

	public MapConfiguration getActiveMap() {
		if (activeMapNumber >= maps.size()) {
			return null;
		}

		return maps.get(activeMapNumber);
	}

	public int getActiveMapNumber() {
		return activeMapNumber;
	}

	public ArrayList<MapConfiguration> getMaps() {
		return maps;
	}

	public void setLobbyLocation(Location lobbyLocation) {
		this.lobbyLocation = lobbyLocation;
	}

	public Location getLobbyLocation() {
		return lobbyLocation;
	}

	public Stage getArenaStage() {
		return arenaStage;
	}

	@Override
	public boolean autoEndGame() {
		return false;
	}

	@Override
	public void endGame() {
		setArenaStage(Stage.END);
	}

	@Override
	public String getName() {
		return "Dropper";
	}
}