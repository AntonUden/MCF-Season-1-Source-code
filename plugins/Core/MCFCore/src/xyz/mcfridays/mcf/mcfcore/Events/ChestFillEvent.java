package xyz.mcfridays.mcf.mcfcore.Events;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import xyz.mcfridays.mcf.mcfcore.Loot.LootTable;
import xyz.mcfridays.mcf.mcfcore.Loot.ChestLoot.ChestType;

public class ChestFillEvent extends Event implements Cancellable {
	private static final HandlerList HANDLERS = new HandlerList();

	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	private Block block;
	private LootTable lootTable;
	private ChestType chestType;

	private boolean cancelled;
	private boolean lootTableChanged;

	public ChestFillEvent(Block block, LootTable lootTable, ChestType chestType) {
		this.block = block;
		this.lootTable = lootTable;
		this.chestType = chestType;

		this.cancelled = false;
		this.lootTableChanged = false;
	}

	public Block getBlock() {
		return block;
	}

	public Location getLocation() {
		return block.getLocation();
	}
	
	public LootTable getLootTable() {
		return lootTable;
	}

	public ChestType getChestType() {
		return chestType;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	public boolean hasLootTableChanged() {
		return lootTableChanged;
	}

	public void setLootTable(LootTable lootTable) {
		this.lootTable = lootTable;
		lootTableChanged = true;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}
}