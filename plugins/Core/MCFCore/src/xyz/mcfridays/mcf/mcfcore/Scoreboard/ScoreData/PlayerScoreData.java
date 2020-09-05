package xyz.mcfridays.mcf.mcfcore.Scoreboard.ScoreData;

import java.util.UUID;

import org.bukkit.ChatColor;

import xyz.mcfridays.mcf.mcfcommons.database.DBConnection;

public class PlayerScoreData extends ScoreData {
	private UUID uuid;

	public PlayerScoreData(UUID uuid, int score) {
		super(score);
		this.uuid = uuid;
	}

	public UUID getUuid() {
		return uuid;
	}

	@Override
	public String toString() {
		return ChatColor.AQUA + DBConnection.getPlayerName(uuid) + ChatColor.GOLD + " : " + ChatColor.AQUA + this.getScore();
	}
}