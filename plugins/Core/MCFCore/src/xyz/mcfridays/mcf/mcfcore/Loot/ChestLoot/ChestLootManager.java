package xyz.mcfridays.mcf.mcfcore.Loot.ChestLoot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Events.ChestFillEvent;
import xyz.mcfridays.mcf.mcfcore.Loot.LootTable;

public class ChestLootManager implements Listener {
	private ArrayList<Location> chests;
	private HashMap<Location, Inventory> enderChests;
	
	private BlockFace chestBlockFaces[] = {BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH};

	boolean enderchestEnabled;

	public ChestLootManager() {
		this.enderchestEnabled = false;
		this.enderChests = new HashMap<Location, Inventory>();
		this.chests = new ArrayList<Location>();
	}

	public void refillChests() {
		refillChests(false);
	}

	public void refillChests(boolean announce) {
		enderChests.clear();
		chests.clear();
		if (announce) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.playSound(p.getLocation(), Sound.NOTE_PLING, 1F, 1F);
				p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Chests have been refilled");
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getClickedBlock().getType() == Material.CHEST || e.getClickedBlock().getType() == Material.TRAPPED_CHEST) {
				fillChest(e.getClickedBlock());
			} else if (e.getClickedBlock().getType() == Material.ENDER_CHEST) {
				if (isEnderchestEnabled()) {
					e.setCancelled(true);
					Player p = e.getPlayer();

					if (e.getClickedBlock() != null) {
						if (!enderChests.containsKey(e.getClickedBlock().getLocation())) {
							Inventory inventory = Bukkit.createInventory(new EnderChestHolder(), 27, "Ender chest");

							LootTable lootTable = MCFCore.getInstance().getGameManager().getActiveGame().getEnderChestLootTable();

							if (lootTable == null) {
								lootTable = MCFCore.getInstance().getLootTableManager().getPlaceholderLootTable();
							}
							
							ChestFillEvent event = new ChestFillEvent(e.getClickedBlock(), lootTable, ChestType.ENDERCHEST);
							
							Bukkit.getServer().getPluginManager().callEvent(event);
							
							if(event.isCancelled()) {
								return;
							}
							
							if(event.hasLootTableChanged()) {
								lootTable = event.getLootTable();
							}

							inventory.clear();

							ArrayList<ItemStack> loot = lootTable.generateLoot();

							inventory.clear();

							while (loot.size() > inventory.getSize()) {
								loot.remove(0);
							}

							while (loot.size() > 0) {
								Random random = new Random();

								int slot = random.nextInt(inventory.getSize());

								if (inventory.getItem(slot) == null) {
									ItemStack item = loot.remove(0);
									inventory.setItem(slot, item);
								}
							}

							enderChests.put(e.getClickedBlock().getLocation(), inventory);
						}

						p.openInventory(enderChests.get(e.getClickedBlock().getLocation()));
					}
				}
			}
		}
	}
	
	private void fillChest(Block block) {
		if (block.getState() instanceof Chest) {
			if (MCFCore.getInstance().getGameManager().hasActiveGame()) {
				if (!chests.contains(block.getLocation())) {
					LootTable lootTable = MCFCore.getInstance().getGameManager().getActiveGame().getChestLootTable();

					if (lootTable == null) {
						lootTable = MCFCore.getInstance().getLootTableManager().getPlaceholderLootTable();
					}

					chests.add(block.getLocation());

					Chest chest = (Chest) block.getState();
					
					Inventory inventory = chest.getBlockInventory();

					ChestFillEvent event = new ChestFillEvent(block, lootTable, ChestType.CHEST);
					
					Bukkit.getServer().getPluginManager().callEvent(event);
					
					if(event.isCancelled()) {
						return;
					}
					
					if(event.hasLootTableChanged()) {
						lootTable = event.getLootTable();
					}
					
					inventory.clear();

					ArrayList<ItemStack> loot = lootTable.generateLoot();

					inventory.clear();

					while (loot.size() > inventory.getSize()) {
						loot.remove(0);
					}

					while (loot.size() > 0) {
						Random random = new Random();

						int slot = random.nextInt(inventory.getSize());

						if (inventory.getItem(slot) == null) {
							ItemStack item = loot.remove(0);
							inventory.setItem(slot, item);
						}
					}
					
					for(BlockFace face : chestBlockFaces) {
						Block nextBlock = block.getRelative(face);
						
						if(nextBlock.getType() == Material.CHEST || nextBlock.getType() == Material.TRAPPED_CHEST) {
							if (!chests.contains(nextBlock.getLocation())) {
								fillChest(nextBlock);
							}
						}
					}
				}
			}
		}
	}

	public boolean isEnderchestEnabled() {
		return enderchestEnabled;
	}

	public void setEnderchestEnabled(boolean enderchestEnabled) {
		this.enderchestEnabled = enderchestEnabled;
	}
}