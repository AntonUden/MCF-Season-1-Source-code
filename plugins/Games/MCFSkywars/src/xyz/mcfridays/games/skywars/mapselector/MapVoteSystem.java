package xyz.mcfridays.games.skywars.mapselector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import xyz.mcfridays.games.skywars.MCFSkywars;
import xyz.mcfridays.games.skywars.map.SkywarsMapData;

public class MapVoteSystem implements Listener {
	private HashMap<UUID, String> votes;
	private HashMap<UUID, Inventory> playerVoteInventory;

	private String forcedMap;

	public MapVoteSystem() {
		this.votes = new HashMap<UUID, String>();
		this.playerVoteInventory = new HashMap<UUID, Inventory>();

		this.forcedMap = null;
	}

	public HashMap<UUID, String> getVotes() {
		return votes;
	}

	public String getWinner() {
		if (forcedMap != null) {
			System.out.println("Returning forced map " + forcedMap);
			return forcedMap;
		}

		ArrayList<String> randomMapList = new ArrayList<String>();

		for (SkywarsMapData map : MCFSkywars.getInstance().getAvailableMaps()) {
			if (!map.isEnabled()) {
				continue;
			}

			randomMapList.add(map.getName());

			int extraChance = 0;

			for (UUID uuid : votes.keySet()) {
				if (votes.get(uuid).equals(map.getName())) {
					extraChance += 2;
				}
			}

			int playCount = map.getPlayCount();
			extraChance -= playCount * 2;

			System.out.println(map.getName() + " has been played " + playCount + " times");

			System.out.println(map.getName() + " has an extra chance of " + extraChance + " (Negative values wont affect chance at all and will result in the default chance of 1)");

			for (int i = 0; i < extraChance; i++) {
				randomMapList.add(map.getName());
			}
		}

		System.out.println("randomMapList.size() = " + randomMapList.size());

		if (randomMapList.size() == 0) {
			return null;
		}

		Random random = new Random();

		String map = randomMapList.get(random.nextInt(randomMapList.size()));

		randomMapList.clear();

		return map;
	}

	public void show(Player p) {
		if (!playerVoteInventory.containsKey(p.getUniqueId())) {
			Inventory voteInventory = Bukkit.createInventory(new MapVoteInventoryHolder(), 9, ChatColor.GOLD + "" + ChatColor.BOLD + "Vote for map");
			playerVoteInventory.put(p.getUniqueId(), voteInventory);

			updateInventory(p);

		}

		p.openInventory(playerVoteInventory.get(p.getUniqueId()));
	}

	private void updateInventory(Player p) {
		if (playerVoteInventory.containsKey(p.getUniqueId())) {
			Inventory voteInventory = playerVoteInventory.get(p.getUniqueId());
			MapVoteInventoryHolder holder = (MapVoteInventoryHolder) voteInventory.getHolder();

			holder.getMapSlots().clear();

			int slot = 0;
			for (SkywarsMapData map : MCFSkywars.getInstance().getAvailableMaps()) {
				ItemStack voteItem = new ItemStack(map.isEnabled() ? Material.EMERALD : Material.COAL);

				ItemMeta meta = voteItem.getItemMeta();

				boolean selected = false;

				if (votes.containsKey(p.getUniqueId())) {
					if (map.getName().equals(votes.get(p.getUniqueId()))) {
						selected = true;
					}
				}

				@SuppressWarnings("unchecked")
				ArrayList<String> lore = (ArrayList<String>) map.getDescription().clone();

				if (!map.isEnabled()) {
					lore.add("");
					lore.add(ChatColor.RESET + "" + ChatColor.DARK_RED + "Disabled");
				}

				meta.setLore(lore);

				meta.setDisplayName((selected ? ChatColor.GREEN : ChatColor.GOLD) + "" + ChatColor.BOLD + map.getDisplayName());

				if (selected) {
					meta.addEnchant(Enchantment.DURABILITY, 1, false);
					meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				}

				voteItem.setItemMeta(meta);

				voteInventory.setItem(slot, voteItem);

				if (map.isEnabled()) {
					holder.getMapSlots().put(slot, map.getName());
				}

				slot++;
			}
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getInventory().getHolder() instanceof MapVoteInventoryHolder) {
			e.setCancelled(true);
			if (e.getClickedInventory() != null) {
				if (e.getClickedInventory().getHolder() instanceof MapVoteInventoryHolder) {
					MapVoteInventoryHolder holder = (MapVoteInventoryHolder) e.getClickedInventory().getHolder();

					Player p = (Player) e.getWhoClicked();

					if (holder.getMapSlots().containsKey(e.getSlot())) {
						String mapName = holder.getMapSlots().get(e.getSlot());

						boolean changed = false;

						if (e.getClick() == ClickType.MIDDLE) {
							if (p.hasPermission("mcf.hg.forcemap")) {
								if (forcedMap == mapName) {
									forcedMap = null;
									Bukkit.getServer().broadcast(ChatColor.AQUA + "Set forced map to null", "mcf.hg.forcemap");
								} else {
									forcedMap = mapName;
									Bukkit.getServer().broadcast(ChatColor.AQUA + "Set forced map to " + mapName, "mcf.hg.forcemap");
								}
							}
						}

						if (votes.containsKey(p.getUniqueId())) {
							if (votes.get(p.getUniqueId()).equals(mapName)) {
								return;
							}
							votes.remove(p.getUniqueId());
							changed = true;
						}

						votes.put(p.getUniqueId(), mapName);

						p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
						p.sendMessage(ChatColor.GREEN + (changed ? "You changed your vote to " : "You voted for ") + MCFSkywars.getInstance().getMap(mapName).getDisplayName());

						updateInventory(p);
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
			if (e.getItem() != null) {
				if (e.getItem().getType() == Material.EMPTY_MAP) {
					e.setCancelled(true);
					show(e.getPlayer());
				}
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();

		if (playerVoteInventory.containsKey(p.getUniqueId())) {
			playerVoteInventory.get(p.getUniqueId()).clear();
			playerVoteInventory.remove(p.getUniqueId());
		}
	}
}