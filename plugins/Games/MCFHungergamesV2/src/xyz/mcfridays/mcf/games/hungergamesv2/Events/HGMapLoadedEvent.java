package xyz.mcfridays.mcf.games.hungergamesv2.Events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import xyz.mcfridays.mcf.games.hungergamesv2.Map.HungergamesMap;

public class HGMapLoadedEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
	public static HandlerList getHandlerList() {
        return HANDLERS;
    }
	
	public HGMapLoadedEvent(HungergamesMap map) {
		this.map = map;
	}
	
	private HungergamesMap map;
	
	public HungergamesMap getMap() {
		return map;
	}
}