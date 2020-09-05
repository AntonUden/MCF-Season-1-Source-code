package xyz.mcfridays.mcf.mcfcore.TrackerCompass;

import org.bukkit.entity.Player;

public interface CustomTrackerMessage {
	public String getMessage(Player user, Player target);

	public String getNoTargetMessage(Player player);
}