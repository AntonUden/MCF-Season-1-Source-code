package xyz.mcfridays.mcf.mcfcore.Events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import xyz.mcfridays.mcf.mcfcore.Game.PlayerEliminationReason;

public class PlayerEliminatedEvent extends Event {
	private OfflinePlayer player;
	private PlayerEliminationReason reason;

	private static final HandlerList HANDLERS = new HandlerList();

	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
    
    public PlayerEliminatedEvent(OfflinePlayer player2, PlayerEliminationReason reason) {
    	this.player = player2;
    	this.reason = reason;
	}
    
    public PlayerEliminationReason getReason() {
		return reason;
	}
    
    public OfflinePlayer getPlayer() {
		return player;
	}
}