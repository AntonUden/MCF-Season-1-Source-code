package xyz.mcfridays.mcf.games.hungergamesv2.Map.MapSelector;

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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import xyz.mcfridays.mcf.games.hungergamesv2.MCFHungergames;
import xyz.mcfridays.mcf.games.hungergamesv2.Map.HungergamesMapData;

@Deprecated
public class MapVoteSystemOld implements Listener {
	private HashMap<UUID, String> votes;
	private HashMap<UUID, Inventory> playerVoteInventory;

	public MapVoteSystemOld() {
		this.votes = new HashMap<UUID, String>();
		this.playerVoteInventory = new HashMap<UUID, Inventory>();
	}

	public HashMap<UUID, String> getVotes() {
		return votes;
	}

	public String getWinner() {
		ArrayList<String> maps = new ArrayList<String>();

		for (HungergamesMapData map : MCFHungergames.getInstance().getAvailableMaps()) {
			if (!map.isEnabled()) {
				continue;
			}
			maps.add(map.getName());
		}

		if (votes.size() > 0) {

			HashMap<String, Integer> mapVoteCount = new HashMap<String, Integer>();

			@SuppressWarnings("unchecked")
			ArrayList<String> noVotes = (ArrayList<String>) maps.clone();

			for (UUID uuid : votes.keySet()) {
				String map = votes.get(uuid);
				if (!mapVoteCount.containsKey(map)) {
					mapVoteCount.put(map, 1);
				} else {
					mapVoteCount.put(map, mapVoteCount.get(map) + 1);
				}
			}

			for (String map1 : mapVoteCount.keySet()) {
				noVotes.remove(map1);

				int voteCount = mapVoteCount.get(map1);
				for (String map2 : mapVoteCount.keySet()) {
					if (map1.equals(map2)) {
						continue;
					}

					if (mapVoteCount.get(map2) > voteCount) {
						maps.remove(map1);
						break;
					}
				}
			}

			for (String map : noVotes) {
				maps.remove(map);
			}
		}

		if (maps.size() > 0) {
			return maps.get(new Random().nextInt(maps.size()));
		}

		System.err.println("[MapVoteSystem.getWinner()] no maps left");
		return null;
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
			for (HungergamesMapData map : MCFHungergames.getInstance().getAvailableMaps()) {
				ItemStack voteItem = new ItemStack(map.isEnabled() ? Material.EMERALD : Material.COAL);

				ItemMeta meta = voteItem.getItemMeta();

				boolean selected = false;

				if (votes.containsKey(p.getUniqueId())) {
					if (map.getName().equals(votes.get(p.getUniqueId()))) {
						selected = true;
					}
				}

				ArrayList<String> lore = map.getDescription();

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
						if (votes.containsKey(p.getUniqueId())) {
							if (votes.get(p.getUniqueId()).equals(mapName)) {
								return;
							}
							votes.remove(p.getUniqueId());
							changed = true;
						}

						votes.put(p.getUniqueId(), mapName);

						p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
						p.sendMessage(ChatColor.GREEN + (changed ? "You changed your vote to " : "You voted for ") + MCFHungergames.getInstance().getMap(mapName).getDisplayName());

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

		if (votes.containsKey(p.getUniqueId())) {
			votes.remove(p.getUniqueId());
		}

		if (playerVoteInventory.containsKey(p.getUniqueId())) {
			playerVoteInventory.get(p.getUniqueId()).clear();
			playerVoteInventory.remove(p.getUniqueId());
		}
	}
}