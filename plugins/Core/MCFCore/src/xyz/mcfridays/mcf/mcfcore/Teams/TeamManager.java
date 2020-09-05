package xyz.mcfridays.mcf.mcfcore.Teams;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import xyz.mcfridays.mcf.mcfcommons.database.DBConnection;
import xyz.mcfridays.mcf.mcfcore.MCFCore;

public class TeamManager implements Listener {
	private HashMap<UUID, Integer> playerTeamNumber;
	private HashMap<Integer, Team> teams;

	private int updateTaskId = -1;

	private ArrayList<ChatColor> teamColor;

	private final int TEAM_COUNT = 12;

	public TeamManager() {
		this.playerTeamNumber = new HashMap<UUID, Integer>();
		this.teamColor = new ArrayList<ChatColor>();
		this.teams = new HashMap<Integer, Team>();

		this.updateTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFCore.getInstance(), new Runnable() {
			@Override
			public void run() {
				updateTeams();
				updatePlayerNames();
			}
		}, 100L, 100L);

		for (int i = 0; i < TEAM_COUNT; i++) {
			teams.put(i + 1, new Team(i + 1, 0));
		}

		teamColor.add(ChatColor.DARK_BLUE); // Team 1
		teamColor.add(ChatColor.DARK_GREEN); // Team 2
		teamColor.add(ChatColor.DARK_AQUA); // Team 3
		teamColor.add(ChatColor.DARK_RED); // Team 4
		teamColor.add(ChatColor.DARK_PURPLE); // Team 5
		teamColor.add(ChatColor.GOLD); // Team 6
		teamColor.add(ChatColor.GRAY); // Team 7
		teamColor.add(ChatColor.BLUE); // Team 8
		teamColor.add(ChatColor.GREEN); // Team 9
		teamColor.add(ChatColor.AQUA); // Team 10
		teamColor.add(ChatColor.RED); // Team 11
		teamColor.add(ChatColor.LIGHT_PURPLE); // Team 12
		teamColor.add(ChatColor.YELLOW); // Team 13 (Not used)
		teamColor.add(ChatColor.WHITE); // Team 14  (Not used)

		updateTeams();
	}

	public void endTask() {
		if (updateTaskId != -1) {
			Bukkit.getScheduler().cancelTask(updateTaskId);
			updateTaskId = -1;
		}
	}

	public void updatePlayerNames() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			updatePlayerName(p);
		}
	}

	public void updatePlayerName(Player p) {
		Team team = getPlayerTeam(p);

		String name = "MissingNo";

		ChatColor color = ChatColor.YELLOW;
		if (team == null) {
			name = color + "No team : " + ChatColor.RESET + p.getName();
		} else {
			if (team.getTeamNumber() >= 1) {
				if (teamColor.size() > team.getTeamNumber() - 1) {
					color = teamColor.get(team.getTeamNumber() - 1);
				}
				name = color + "Team " + team.getTeamNumber() + " : " + ChatColor.RESET + p.getName();
			}
		}

		p.setDisplayName("| " + name);
		p.setPlayerListName(name);
	}

	public ChatColor getTeamColor(Team team) {
		return getTeamColor(team.getTeamNumber());
	}

	public ChatColor getTeamColor(Integer teamNumber) {
		ChatColor color = ChatColor.WHITE;
		if (teamNumber >= 1) {
			if (teamColor.size() > teamNumber - 1) {
				color = teamColor.get(teamNumber - 1);
			}
		}

		return color;
	}

	/**
	 * Check if 2 {@link OfflinePlayer}s is in the same team
	 * 
	 * @param p1 {@link OfflinePlayer}1
	 * @param p2 {@link OfflinePlayer}2
	 * @return <code>true</code> if both player is in the same team
	 */
	public boolean isPlayerInTeamWith(OfflinePlayer p1, OfflinePlayer p2) {
		if (playerTeamNumber.containsKey(p1.getUniqueId()) && playerTeamNumber.containsKey(p2.getUniqueId())) {
			if (playerTeamNumber.get(p1.getUniqueId()) == -1 || playerTeamNumber.get(p2.getUniqueId()) == -1) {
				return false;
			}

			if (playerTeamNumber.get(p1.getUniqueId()) == playerTeamNumber.get(p2.getUniqueId())) {
				return true;
			}
		}
		return false;
	}

	public Team getPlayerTeam(OfflinePlayer player) {
		return getPlayerTeam(player.getUniqueId());
	}

	public Team getPlayerTeam(UUID uuid) {
		if (playerTeamNumber.containsKey(uuid)) {
			if (teams.containsKey(playerTeamNumber.get(uuid))) {
				return teams.get(playerTeamNumber.get(uuid));
			}
		}
		return null;
	}

	public HashMap<UUID, Integer> getPlayerTeams() {
		return playerTeamNumber;
	}

	public ArrayList<UUID> getTeamMembers(Integer teamNumber) {
		ArrayList<UUID> result = new ArrayList<UUID>();
		if (teamNumber != -1) {
			for (UUID uuid : playerTeamNumber.keySet()) {
				if (playerTeamNumber.get(uuid) == teamNumber) {
					result.add(uuid);
				}
			}
		}

		return result;
	}

	public HashMap<Integer, Team> getTeams() {
		return teams;
	}

	public void importTeams() {
		ArrayList<UUID> oldPlayers = new ArrayList<UUID>();
		ArrayList<TeamImportEntry> entries = new ArrayList<TeamImportEntry>();
		try {
			String sql = "SELECT uuid FROM players WHERE team_id > -1";
			PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				oldPlayers.add(UUID.fromString(rs.getString("uuid")));
			}

			rs.close();
			ps.close();
		} catch (Exception ee) {
			ee.printStackTrace();
			return;
		}

		for (Integer i : teams.keySet()) {
			Team t = teams.get(i);

			for (UUID uuid : t.getTeamMembers()) {
				if (!oldPlayers.contains(uuid)) {
					oldPlayers.add(uuid);
				}
			}
		}

		try {
			String sql = "SELECT team_number, uuid, minecraft_username FROM users WHERE team_number > 0";
			PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				entries.add(new TeamImportEntry(rs.getInt("team_number"), UUID.fromString(rs.getString("uuid")), rs.getString("minecraft_username")));
			}

			rs.close();
			ps.close();
		} catch (Exception ee) {
			ee.printStackTrace();
			return;
		}

		for (TeamImportEntry entry : entries) {
			boolean playerExists = false;
			int oldTeamNumber = -1;
			try {
				String sql = "SELECT team_id FROM players WHERE uuid = ?";
				PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

				ps.setString(1, entry.getUuid().toString());

				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					playerExists = true;
					oldTeamNumber = rs.getInt("team_id");
				}

				rs.close();
				ps.close();
			} catch (Exception ee) {
				ee.printStackTrace();
				continue;
			}

			if (oldPlayers.contains(entry.getUuid())) {
				oldPlayers.remove(entry.getUuid());
			}

			if (playerExists) {
				if (oldTeamNumber != entry.getTeamNumber()) {
					try {
						String sql = "UPDATE players SET team_id = ? WHERE uuid = ?";
						PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

						ps.setInt(1, entry.getTeamNumber());
						ps.setString(2, entry.getUuid().toString());

						ps.execute();

						ps.close();
					} catch (Exception ee) {
						ee.printStackTrace();
					}
				}
			} else {
				try {
					String sql = "INSERT INTO players (uuid, name, team_id, `score`, `kills`) VALUES (?, ?, ?, 0, 0)";
					PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

					ps.setString(1, entry.getUuid().toString());
					ps.setString(2, entry.getUsername());
					ps.setInt(3, entry.getTeamNumber());

					ps.execute();

					ps.close();
				} catch (Exception ee) {
					ee.printStackTrace();
				}
			}
		}

		for (UUID uuid : oldPlayers) {
			if (playerTeamNumber.containsKey(uuid)) {
				playerTeamNumber.remove(uuid);
			}
			try {
				String sql = "UPDATE players SET team_id = -1 WHERE uuid = ?";
				PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

				ps.setString(1, uuid.toString());

				ps.execute();

				ps.close();
			} catch (Exception ee) {
				ee.printStackTrace();
			}
		}
	}

	public boolean updateTeams() {
		ArrayList<UUID> removedUsers = new ArrayList<UUID>();

		for (UUID uuid : playerTeamNumber.keySet()) {
			removedUsers.add(uuid);
		}

		ArrayList<Integer> missingTeams = new ArrayList<Integer>();

		for (int i = 0; i < TEAM_COUNT; i++) {
			if (missingTeams.add((Integer) i + 1))
				;
		}

		try {
			String sql = "SELECT team_number, score FROM teams";
			PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int teamNumber = rs.getInt("team_number");

				if (teamNumber > 0 && teamNumber <= TEAM_COUNT) {
					if (teams.containsKey(teamNumber)) {
						teams.get(teamNumber).setDisplayScore(rs.getInt("score"));
					}
				}
			}

			rs.close();
			ps.close();
		} catch (Exception ee) {
			ee.printStackTrace();
		}

		try {
			String sql = "SELECT team_number FROM teams";
			PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int teamNumber = rs.getInt("team_number");
				if (missingTeams.contains((Integer) teamNumber)) {
					missingTeams.remove((Integer) teamNumber);
				}
			}

			rs.close();
			ps.close();
		} catch (Exception ee) {
			ee.printStackTrace();
		}

		for (Integer i : missingTeams) {
			try {
				String sql = "INSERT INTO teams (team_number) VALUES (?)";
				PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

				ps.setInt(1, i);

				ps.execute();

				ps.close();
			} catch (Exception ee) {
				ee.printStackTrace();
			}
		}

		try {
			String sql = "SELECT team_id, uuid FROM players";
			PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				UUID playerUuid = UUID.fromString(rs.getString("uuid"));
				int teamNumber = rs.getInt("team_id");

				if (teamNumber != -1) {
					if (removedUsers.contains(playerUuid)) {
						removedUsers.remove(playerUuid);
					}

					playerTeamNumber.put(playerUuid, teamNumber);
				}
			}

			rs.close();
			ps.close();
		} catch (Exception ee) {
			ee.printStackTrace();
		}

		return false;
	}

	public void sendTeamMessage(String message, Integer teamId) {
		for (UUID uuid : getTeamMembers(teamId)) {
			Player p = Bukkit.getServer().getPlayer(uuid);
			if (p != null) {
				if (p.isOnline()) {
					p.sendMessage(message);
				}
			}
		}
	}

	public Team getTeam(Integer id) {
		if (teams.containsKey(id)) {
			return teams.get(id);
		}

		return null;
	}

	public String getMembersString(Integer teamNumber) {
		if (teamNumber < 0) {
			return "Team " + teamNumber;
		}

		String membersString = "";

		ArrayList<UUID> teamMembers = getTeamMembers(teamNumber);

		for (int i = teamMembers.size(); i > 0; i--) {
			membersString += DBConnection.getPlayerName(teamMembers.get(i - 1)) + (i == 1 ? "" : (i == 2 ? " and " : ", "));
		}

		return membersString;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		updatePlayerName(p);
	}
}