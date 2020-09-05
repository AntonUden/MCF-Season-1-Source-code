package xyz.mcfridays.mcf.mcfcore.Teams;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import xyz.mcfridays.mcf.mcfcore.MCFCore;

public class Team {
	private int teamNumber;
	private int score;

	public Team(int teamNumber, int score) {
		this.teamNumber = teamNumber;
		this.score = score;
	}

	public int getTeamNumber() {
		return teamNumber;
	}

	public int getScore() {
		return score;
	}

	public void setDisplayScore(int score) {
		this.score = score;
	}

	public ArrayList<UUID> getTeamMembers() {
		return MCFCore.getInstance().getTeamManager().getTeamMembers(teamNumber);
	}

	public String getMemberString() {
		return MCFCore.getInstance().getTeamManager().getMembersString(teamNumber);
	}

	public ChatColor getTeamColor() {
		return MCFCore.getInstance().getTeamManager().getTeamColor(this);
	}

	public boolean addScore(int score) {
		return MCFCore.getInstance().getScoreManager().addTeamScore(teamNumber, score);
	}

	public boolean isMember(OfflinePlayer player) {
		return this.isMember(player.getUniqueId());
	}

	public boolean isMember(UUID uuid) {
		return this.getTeamMembers().contains(uuid);
	}

	public static Team getPlayerTeam(OfflinePlayer player) {
		return Team.getPlayerTeam(player.getUniqueId());
	}

	public static Team getPlayerTeam(UUID uuid) {
		return MCFCore.getInstance().getTeamManager().getPlayerTeam(uuid);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Team) {
			if(((Team) obj).getTeamNumber() == this.getTeamNumber()) {
				return true;
			}
		}
		
		return false;
	}
}