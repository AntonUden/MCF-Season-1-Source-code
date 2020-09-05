package xyz.mcfridays.mcf.mcfcore.Score;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import xyz.mcfridays.mcf.mcfcore.Teams.Team;

public class ScoreManager implements Listener {
	private HashMap<UUID, Integer> playerScore;
	private HashMap<UUID, Integer> playerKills;
	private int updateTaskId;

	private Path queryLog;
	private Path errorLog;

	public ScoreManager() {
		this.playerScore = new HashMap<UUID, Integer>();
		this.playerKills = new HashMap<UUID, Integer>();

		this.errorLog = Paths.get(MCFCore.getInstance().getDataFolder().getPath() + "/score_error_log.txt");
		this.queryLog = Paths.get(MCFCore.getInstance().getDataFolder().getPath() + "/score_query_fix_log.txt");

		try {
			if (!errorLog.toFile().exists()) {
				errorLog.toFile().createNewFile();
			}

			if (!queryLog.toFile().exists()) {
				queryLog.toFile().createNewFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.updateTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFCore.getInstance(), new Runnable() {
			@Override
			public void run() {
				try {
					String sql = "SELECT score, kills, uuid FROM players";
					PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

					ResultSet rs = ps.executeQuery();
					while (rs.next()) {
						UUID uuid = UUID.fromString(rs.getString("uuid"));
						int score = rs.getInt("score");
						int kills = rs.getInt("kills");

						playerScore.put(uuid, score);
						playerKills.put(uuid, kills);
					}

					rs.close();
					ps.close();
				} catch (Exception ee) {
					ee.printStackTrace();
					return;
				}
			}
		}, 10L, 10L);
	}

	public void logError(String error, String query) {
		try (BufferedWriter writer = Files.newBufferedWriter(errorLog, StandardOpenOption.APPEND)) {
			writer.write(error + System.lineSeparator());
		} catch (IOException ioe) {
			System.err.format("IOException: %s%n", ioe);
		}
		try (BufferedWriter writer = Files.newBufferedWriter(queryLog, StandardOpenOption.APPEND)) {
			writer.write(query + System.lineSeparator());
		} catch (IOException ioe) {
			System.err.format("IOException: %s%n", ioe);
		}
	}

	public void stop() {
		if (updateTaskId != -1) {
			Bukkit.getScheduler().cancelTask(updateTaskId);
			updateTaskId = -1;
		}
	}

	public boolean addScore(OfflinePlayer player, int score, boolean updateTeamScore) {
		return this.addScore(player.getUniqueId(), score, updateTeamScore);
	}

	public boolean addScore(UUID uuid, int score, boolean updateTeamScore) {
		try {
			String sql = "UPDATE players SET score = score + ? WHERE uuid = ?";
			PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

			ps.setInt(1, score);
			ps.setString(2, uuid.toString());

			int count = ps.executeUpdate();

			ps.close();

			if (count == 0) {
				return false;
			}

			if (updateTeamScore) {
				if (MCFCore.getInstance().getTeamManager().getPlayerTeam(uuid) != null) {
					this.addTeamScore(MCFCore.getInstance().getTeamManager().getPlayerTeam(uuid), score);
				}
			}

			return true;
		} catch (Exception ee) {
			ee.printStackTrace();

			String message = "!!!Score update failure!!! Player with uuid: " + uuid.toString() + " failed to add " + score + " score";
			String query = "UPDATE players SET score = score + " + score + " WHERE uuid = '" + uuid.toString() + "';";

			Bukkit.broadcast(ChatColor.RED + message, "mcf.showerrors");
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + message);

			logError(message, query);
		}
		return false;
	}

	public boolean addKill(OfflinePlayer player) {
		return this.addKill(player.getUniqueId());
	}

	public boolean addKill(UUID uuid) {
		try {
			String sql = "UPDATE players SET kills = kills + 1 WHERE uuid = ?";
			PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

			ps.setString(1, uuid.toString());

			int count = ps.executeUpdate();

			ps.close();

			if (count == 0) {
				return false;
			}

			return true;
		} catch (Exception ee) {
			ee.printStackTrace();
		}
		return false;
	}

	public boolean addTeamScore(Team team, int score) {
		return this.addTeamScore(team.getTeamNumber(), score);
	}

	public boolean addTeamScore(int teamId, int score) {
		try {
			String sql = "UPDATE teams SET score = score + ? WHERE team_number = ?";
			PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

			ps.setInt(1, score);
			ps.setInt(2, teamId);

			int count = ps.executeUpdate();

			ps.close();

			if (count == 0) {
				return false;
			}
			return true;
		} catch (Exception ee) {
			ee.printStackTrace();
			String message = "!!!Score update failure!!! Team with id: " + teamId + " failed to add " + score + " score";
			String query = "UPDATE teams SET score = score + " + score + " WHERE team_number = " + teamId + ";";

			Bukkit.broadcast(ChatColor.RED + message, "mcf.showerrors");
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + message);

			logError(message, query);
		}

		return false;
	}

	public HashMap<UUID, Integer> getPlayerScore() {
		return playerScore;
	}

	public Integer getPlayerScore(OfflinePlayer player) {
		return this.getPlayerScore(player.getUniqueId());
	}

	public Integer getPlayerScore(UUID uuid) {
		return playerScore.get(uuid);
	}

	public Integer getPlayerKills(OfflinePlayer player) {
		return this.getPlayerKills(player.getUniqueId());
	}

	public Integer getPlayerKills(UUID uuid) {
		return playerKills.get(uuid);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		if (!playerScore.containsKey(p.getUniqueId())) {
			playerScore.put(p.getUniqueId(), 0);
		}
	}
}