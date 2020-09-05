package xyz.mcfridays.mcf.mcfcore.Game;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerQuitEvent;

import xyz.mcfridays.mcf.mcfcore.Log.ErrorLogger;

/**
 * Represents the reason why a player was eliminated. <br>
 * <br>
 * {@link PlayerEliminationReason#DEATH} and
 * {@link PlayerEliminationReason#KILLED} are caused by the players death. <br>
 * {@link PlayerEliminationReason#DISCONNECTED} is caused by
 * {@link PlayerQuitEvent}. <br>
 * {@link PlayerEliminationReason#DISCONNECTED_TIMED_OUT} is caused by a player
 * not reconnecting in time<br>
 * <br>
 * All other reasons should be used for other games, if there is no appropriate
 * value for a game you can add the value here. <br>
 * 
 * 
 * 
 */
public enum PlayerEliminationReason {
	DISCONNECTED_BATTLE, DISCONNECTED_TIMED_OUT, DISCONNECTED, DISQUALIFIED, DEATH, KILLED, OTHER, FAILED;

	public String getMessage(OfflinePlayer player, LivingEntity killer) {
		if (this == DISCONNECTED_BATTLE) {
			return ChatColor.RED + "" + ChatColor.BOLD + "Player eliminated> " + ChatColor.AQUA + ChatColor.BOLD + player.getName() + ChatColor.RED + ChatColor.BOLD + " disconnected during battle";
		} else if (this == DISCONNECTED_TIMED_OUT) {
			return ChatColor.RED + "" + ChatColor.BOLD + "Player eliminated> " + ChatColor.AQUA + ChatColor.BOLD + player.getName() + ChatColor.RED + ChatColor.BOLD + " did not reconnect in time";
		} else if (this == DISCONNECTED) {
			return ChatColor.RED + "" + ChatColor.BOLD + "Player eliminated> " + ChatColor.AQUA + ChatColor.BOLD + player.getName() + ChatColor.RED + ChatColor.BOLD + " disconnected";
		} else if (this == KILLED) {
			String killerName = null;

			try {
				killerName = killer.getName();
			} catch (Exception e) {
				ErrorLogger.logException(e, "PlayerEliminationReason::getMessage KILLED");
			}

			return ChatColor.RED + "" + ChatColor.BOLD + "Player eliminated> " + ChatColor.AQUA + ChatColor.BOLD + player.getName() + ChatColor.RED + ChatColor.BOLD + " killed by " + ChatColor.AQUA + ChatColor.BOLD + killerName;
		} else if (this == DEATH) {
			return ChatColor.RED + "" + ChatColor.BOLD + "Player eliminated> " + ChatColor.AQUA + ChatColor.BOLD + player.getName() + ChatColor.RED + ChatColor.BOLD + " died";
		} else if (this == DISQUALIFIED) {
			return ChatColor.RED + "" + ChatColor.BOLD + "Player eliminated> " + ChatColor.AQUA + ChatColor.BOLD + player.getName() + ChatColor.RED + ChatColor.BOLD + " disqualified";
		}

		return null;
	}
}