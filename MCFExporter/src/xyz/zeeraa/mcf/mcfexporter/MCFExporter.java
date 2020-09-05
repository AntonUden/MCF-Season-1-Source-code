package xyz.zeeraa.mcf.mcfexporter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import xyz.mcfridays.mcf.mcfcommons.database.DBConnection;

public class MCFExporter {
	public MCFExporter(String[] args) {
		try {
			if (args.length == 4) {
				int weekNumber = Integer.parseInt(args[3]);

				System.out.println("Week number: " + weekNumber);

				System.out.println("Connecting to MySQL server");
				DBConnection.init("com.mysql.cj.jdbc.Driver", "jdbc:mysql://" + args[0] + "?useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", args[1], args[2], "mcf_mcf");

				if (weekExists(weekNumber)) {
					System.err.println("Error: week " + weekNumber + " already exists in the database");
				} else {
					try {
						DBConnection.connection.setAutoCommit(false);

						ArrayList<TeamResult> teamResult = fetchTeamResult();
						System.out.println(teamResult.size() + " team entries found");
						
						ArrayList<PlayerResult> playerResult = fetchPlayerResult();
						System.out.println(playerResult.size() + " player entries found");
						
						System.out.println("Saving as week " + weekNumber);
						addWeek(weekNumber);
						
						System.out.println("Adding week " + weekNumber + " to week list");
						
						for (TeamResult tr : teamResult) {
							System.out.println("Saving team " + tr.getTeamId() + " with a score of " + tr.getScore());
							saveTeamResult(tr, weekNumber);
						}
						
						for (PlayerResult pr : playerResult) {
							System.out.println("Saving player " + pr.getUuid() + " with a score of " + pr.getScore() + " and " + pr.getKills() + " kills");
							savePlayerResult(pr, weekNumber);
						}
						
						System.out.println("Press enter to commit changes");
						System.in.read();
						System.out.println("Commiting changes...");
						DBConnection.connection.commit();
						System.out.println("Success");
					} catch (Exception e) {
						e.printStackTrace();
						System.err.println("An error occurred during export. trying to rollback");
						DBConnection.connection.rollback();
					}
				}

				DBConnection.close();
			} else {
				System.err.println("Args: <MySQL Host> <MySQL Username> <MySQL Password> <Week number>");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean weekExists(int week) throws SQLException {
		boolean result = false;
		String sql = "SELECT id FROM mcf_results.weeks WHERE week = ?";
		PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

		ps.setInt(1, week);

		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			result = true;
		}

		rs.close();
		ps.close();

		return result;
	}
	
	public void addWeek(int week) throws SQLException {
		String sql = "INSERT INTO mcf_results.weeks (week) VALUES (?)";
		PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

		ps.setInt(1, week);

		ps.executeUpdate();

		ps.close();
	}

	public void saveTeamResult(TeamResult result, int week) throws SQLException {
		String sql = "INSERT INTO mcf_results.team_result (week, team_number, score) VALUES (?, ?, ?)";
		PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

		ps.setInt(1, week);
		ps.setInt(2, result.getTeamId());
		ps.setInt(3, result.getScore());

		ps.executeUpdate();

		ps.close();
	}

	public void savePlayerResult(PlayerResult result, int week) throws SQLException {
		String sql = "INSERT INTO mcf_results.player_result (week, uuid, team_id, score, kills) VALUES (?, ?, ?, ?, ?)";
		PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

		ps.setInt(1, week);
		ps.setString(2, result.getUuid().toString());
		ps.setInt(3, result.getTeamId());
		ps.setInt(4, result.getScore());
		ps.setInt(5, result.getKills());

		ps.executeUpdate();

		ps.close();
	}

	public ArrayList<TeamResult> fetchTeamResult() throws SQLException {
		ArrayList<TeamResult> result = new ArrayList<TeamResult>();
		String sql = "SELECT * FROM mcf_mcf.teams";
		PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			result.add(new TeamResult(rs.getInt("team_number"), rs.getInt("score")));
		}

		rs.close();
		ps.close();

		return result;
	}

	public ArrayList<PlayerResult> fetchPlayerResult() throws SQLException {
		ArrayList<PlayerResult> result = new ArrayList<PlayerResult>();
		String sql = "SELECT * FROM mcf_mcf.players WHERE team_id > -1";
		PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			result.add(new PlayerResult(UUID.fromString(rs.getString("uuid")), rs.getInt("team_id"), rs.getInt("score"), rs.getInt("kills")));
		}

		rs.close();
		ps.close();

		return result;
	}

	public static void main(String[] args) {
		new MCFExporter(args);
	}
}