package xyz.mcfridays.mcf.mcfcore.Scoreboard.ScoreData;

import org.bukkit.ChatColor;

import xyz.mcfridays.mcf.mcfcore.Teams.Team;

public class TeamScoreData extends ScoreData {
	private Team team;

	public TeamScoreData(Team team) {
		super(team.getScore());
		this.team = team;
	}
	
	public TeamScoreData(Team team, int score) {
		super(score);
		this.team = team;
	}

	public Integer getTeamNumber() {
		return team.getTeamNumber();
	}

	public Team getTeam() {
		return team;
	}

	@Override
	public String toString() {
		String teamName = "MissingNo";

		if (team.getTeamMembers().size() > 0) {
			teamName = team.getTeamColor() + team.getMemberString();
		} else {
			teamName = team.getTeamColor() + "Team " + team.getTeamNumber();
		}

		return teamName + ChatColor.GOLD + " : " + ChatColor.AQUA + this.getScore();
	}
}