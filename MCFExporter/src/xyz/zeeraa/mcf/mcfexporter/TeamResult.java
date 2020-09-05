package xyz.zeeraa.mcf.mcfexporter;

public class TeamResult {
	private int teamId;
	private int score;
	
	public TeamResult(int teamId, int score) {
		this.teamId = teamId;
		this.score = score;
	}
	
	public int getTeamId() {
		return teamId;
	}
	
	public int getScore() {
		return score;
	}
}