package xyz.mcfridays.mcf.mcfcore.Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import xyz.mcfridays.mcf.mcfcommons.utils.UUIDCallback;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Log.ErrorLogger;

public class GameManager implements Listener {
	private Game activeGame;

	private int taskId;

	private boolean deathMessagesEnabled;

	private HashMap<UUID, Integer> combatTagCooldown;
	private HashMap<UUID, EliminationTask> eliminationTasks;
	private HashMap<UUID, Location> disconnectLocation;

	private HashMap<UUID, ArrayList<ItemStack>> inventoryContent;

	public GameManager() {
		this.combatTagCooldown = new HashMap<UUID, Integer>();
		this.eliminationTasks = new HashMap<UUID, EliminationTask>();
		this.disconnectLocation = new HashMap<UUID, Location>();
		this.inventoryContent = new HashMap<UUID, ArrayList<ItemStack>>();

		this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFCore.getInstance(), new Runnable() {
			@Override
			public void run() {
				ArrayList<UUID> finnished = new ArrayList<UUID>();
				for (UUID uuid : combatTagCooldown.keySet()) {
					int val = combatTagCooldown.get(uuid);

					if (val > 0) {
						combatTagCooldown.put(uuid, val - 1);
					} else {
						finnished.add(uuid);
					}
				}

				for (UUID uuid : finnished) {
					combatTagCooldown.remove(uuid);
				}

				finnished.clear();
			}
		}, 2L, 2L);
	}

	public void setActiveGame(Game activeGame, Plugin plugin) {
		this.activeGame = activeGame;
		this.deathMessagesEnabled = false;
		Bukkit.getServer().getPluginManager().registerEvents(activeGame, plugin);
		activeGame.load();
	}

	public boolean isDeathMessagesEnabled() {
		return deathMessagesEnabled;
	}

	public void setDeathMessagesEnabled(boolean deathMessagesEnabled) {
		this.deathMessagesEnabled = deathMessagesEnabled;
	}

	public Game getActiveGame() {
		return activeGame;
	}

	public boolean hasActiveGame() {
		return activeGame != null;
	}
	
	public boolean isPlaying(OfflinePlayer player) {
		if(hasActiveGame()) {
			return getActiveGame().isPlaying(player);
		}
		
		return false;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		if (activeGame != null) {
			boolean reconnected = false;

			try {
				if (eliminationTasks.containsKey(p.getUniqueId())) {
					eliminationTasks.get(p.getUniqueId()).cancel();

					reconnected = true;

					Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + p.getName() + ChatColor.GREEN + ChatColor.BOLD + " reconnected in time");

					inventoryContent.get(p.getUniqueId()).clear();
					inventoryContent.remove(p.getUniqueId());

					p.teleport(disconnectLocation.get(p.getUniqueId()));
					p.setFallDistance(0);

					eliminationTasks.remove(p.getUniqueId());

					disconnectLocation.remove(p.getUniqueId());
				}

				activeGame.onPlayerJoin(p, reconnected);
			} catch (Exception ex) {
				Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Failed to respawn " + p.getName() + ". A moderator will help with respawning as soon as possible");
				ErrorLogger.logException(ex, "GameManager::onPlayerJoin");
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();

		if (activeGame != null) {
			try {
				activeGame.onPlayerQuit(p);
			} catch (Exception ex) {
				ErrorLogger.logException(ex, "GameManager::onPlayerQuit activeGame.onPlayerQuit(p)");
			}

			if (activeGame.getPlayers().contains(p.getUniqueId())) {
				if (activeGame.eliminatePlayerOnQuit()) {
					if (activeGame.getEliminationType() == EliminationType.INSTANT) {
						for (ItemStack stack : p.getInventory().getContents()) {
							if (stack == null) {
								continue;
							}

							p.getLocation().getWorld().dropItem(p.getLocation(), stack);
						}
						try {
							activeGame.eliminatePlayer(PlayerEliminationReason.DISCONNECTED, p);
						} catch (Exception ex) {
							ErrorLogger.logException(ex, "GameManager::onPlayerQuit eliminatePlayer DISCONNECTED");
						}
					} else {
						if (combatTagCooldown.containsKey(p.getUniqueId())) {
							try {
								for (ItemStack stack : p.getInventory().getContents()) {
									if (stack == null) {
										continue;
									}

									p.getLocation().getWorld().dropItem(p.getLocation(), stack);
								}
								activeGame.eliminatePlayer(PlayerEliminationReason.DISCONNECTED_BATTLE, p);
							} catch (Exception ex) {
								ErrorLogger.logException(ex, "GameManager::onPlayerQuit eliminatePlayer DISCONNECTED_BATTLE");
							}
						} else {
							Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + p.getName() + ChatColor.RED + ChatColor.BOLD + " disconnected. They have " + (activeGame.getEliminationDelay() / 60) + " minute" + ((activeGame.getEliminationDelay() / 60) == 1 ? "" : "s") + ((activeGame.getEliminationDelay() % 60 == 0) ? "" : " and " + (activeGame.getEliminationDelay() % 60) + " seconds") + " to reconnect");
							ArrayList<ItemStack> items = new ArrayList<ItemStack>();

							for (ItemStack item : p.getInventory().getContents()) {
								if (item == null) {
									continue;
								}

								items.add(item);
							}

							inventoryContent.put(p.getUniqueId(), items);

							disconnectLocation.put(p.getUniqueId(), p.getLocation());
							eliminationTasks.put(p.getUniqueId(), new EliminationTask(p.getUniqueId(), p.getName(), activeGame.getEliminationDelay(), new UUIDCallback() {
								@Override
								public void execute(UUID uuid) {
									try {
										activeGame.eliminatePlayer(PlayerEliminationReason.DISCONNECTED_TIMED_OUT, Bukkit.getServer().getOfflinePlayer(uuid));
									} catch (Exception ex) {
										ErrorLogger.logException(ex, "GameManager::onPlayerQuit eliminatePlayer DISCONNECTED_TIMED_OUT");
									}
									eliminationTasks.remove(uuid);

									Location location = disconnectLocation.get(uuid);

									ArrayList<ItemStack> items = inventoryContent.get(uuid);

									for (ItemStack item : items) {
										location.getWorld().dropItem(location, item);
									}

									inventoryContent.get(uuid).clear();
									inventoryContent.remove(uuid);

									disconnectLocation.remove(uuid);
								}
							}));
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();

		if (activeGame != null) {
			LivingEntity killer = p.getKiller();

			if (!isDeathMessagesEnabled()) {
				e.setDeathMessage(null);
			}

			try {
				activeGame.onPlayerDeath(p, killer);
			} catch (Exception ex) {
				ErrorLogger.logException(ex, "GameManager::onPlayerQuit activeGame.onPlayerDeath()");
			}

			if (activeGame.eliminatePlayerOnDeath()) {
				PlayerEliminationReason reason = killer == null ? PlayerEliminationReason.DEATH : PlayerEliminationReason.KILLED;
				try {
					activeGame.eliminatePlayer(reason, p, killer);
				} catch (Exception ex) {
					ErrorLogger.logException(ex, "GameManager::onPlayerQuit eliminatePlayer " + reason.name());
				}
			}
		}

	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player) {
			if (activeGame != null) {
				Player player = (Player) e.getEntity();
				Player damager = null;

				if (e.getDamager() instanceof Player) {
					damager = (Player) e.getDamager();
				} else if (e.getDamager() instanceof Arrow) {
					Arrow arrow = (Arrow) e.getDamager();

					if (arrow.getShooter() instanceof Player) {
						damager = (Player) arrow.getShooter();
					}
				} else if (e.getDamager() instanceof Snowball) {
					Snowball snowball = (Snowball) e.getDamager();

					if (snowball.getShooter() instanceof Player) {
						damager = (Player) snowball.getShooter();
					}
				} else if (e.getDamager() instanceof Egg) {
					Egg egg = (Egg) e.getDamager();

					if (egg.getShooter() instanceof Player) {
						damager = (Player) egg.getShooter();
					}
				}

				if (damager != null) {
					if (MCFCore.getInstance().getTeamManager().isPlayerInTeamWith(player, damager)) {
						e.setCancelled(true);
					} else {
						if (activeGame.pvpEnabled()) {
							combatTagCooldown.put(player.getUniqueId(), 80);
						} else {
							e.setCancelled(true);
						}
					}
				}
			}
		}
	}

	public void disable() {
		if (taskId != -1) {
			Bukkit.getScheduler().cancelTask(taskId);
			taskId = -1;
		}

		for (UUID uuid : eliminationTasks.keySet()) {
			eliminationTasks.get(uuid).cancel();
		}

		for (UUID uuid : inventoryContent.keySet()) {
			inventoryContent.get(uuid).clear();
		}

		eliminationTasks.clear();
		disconnectLocation.clear();
		combatTagCooldown.clear();
		inventoryContent.clear();

		if (hasActiveGame()) {
			activeGame.unload();
			activeGame = null;
		}
	}
}