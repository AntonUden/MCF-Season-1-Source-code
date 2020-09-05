package xyz.mcfridays.mcf.mcfcore.Teams;

import java.util.UUID;

public class TeamImportEntry {
	private int teamNumber;
	private UUID uuid;
	private String username;

	public TeamImportEntry(int teamNumber, UUID uuid, String username) {
		this.teamNumber = teamNumber;
		this.uuid = uuid;
		this.username = username;
	}

	public int getTeamNumber() {
		return teamNumber;
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getUsername() {
		return username;
	}
}