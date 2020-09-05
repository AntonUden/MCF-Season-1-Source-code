package xyz.mcfridays.games.skywars.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import xyz.mcfridays.games.skywars.map.SkywarsMap;

public class SkywarsMapLoadedEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	private SkywarsMap map;

	public SkywarsMapLoadedEvent(SkywarsMap map) {
		this.map = map;
	}

	public SkywarsMap getMap() {
		return map;
	}
}