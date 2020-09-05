package xyz.mcfridays.mcf.mcfcore.Scoreboard;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;

import me.johnnykpl.scoreboardwrapper.ScoreboardWrapper;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Teams.Team;

public class MCFScoreboardManager implements Listener {
	public static final int LINES = 16;

	private HashMap<UUID, ScoreboardWrapper> scoreboards;
	private HashMap<Integer, String> customLines;
	private String serverString;
	private int taskId;

	public MCFScoreboardManager() {
		this.scoreboards = new HashMap<UUID, ScoreboardWrapper>();
		this.customLines = new HashMap<Integer, String>();

		this.serverString = "null";

		for (Player p : Bukkit.getOnlinePlayers()) {
			createScoreboard(p);
		}

		this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFCore.getInstance(), new Runnable() {
			@Override
			public void run() {
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					updateScoreboard(p);
				}
			}
		}, 5L, 5L);
	}

	public void stop() {
		if (taskId != -1) {
			Bukkit.getScheduler().cancelTask(taskId);
			taskId = -1;
		}
	}

	public void createScoreboard(Player player) {
		if (scoreboards.containsKey(player.getUniqueId())) {
			return;
		}

		ScoreboardWrapper scoreboardWrapper = new ScoreboardWrapper(ChatColor.GREEN + "" + ChatColor.BOLD + "MC Fridays");

		for (int i = 0; i <= LINES; i++) {
			scoreboardWrapper.addLine(ChatColor.GOLD + "TMP " + i);
		}

		for (int i = 0; i < LINES; i++) {
			scoreboardWrapper.setLine(i, ChatColor.GOLD + "");
		}

		scoreboards.put(player.getUniqueId(), scoreboardWrapper);
		player.setScoreboard(scoreboardWrapper.getScoreboard());
	}

	public void updateScoreboard(Player p) {
		if (scoreboards.containsKey(p.getUniqueId())) {
			ScoreboardWrapper scoreboard = scoreboards.get(p.getUniqueId());

			String teamString = ChatColor.RED + "No team";
			String teamScore = "0";

			Integer score = MCFCore.getInstance().getScoreManager().getPlayerScore(p);
			Integer kills = MCFCore.getInstance().getScoreManager().getPlayerKills(p);

			if (score == null) {
				score = 0;
			}

			if (kills == null) {
				kills = 0;
			}

			Team team = MCFCore.getInstance().getTeamManager().getPlayerTeam(p);

			if (team != null) {
				teamString = team.getTeamColor() + "Team " + team.getTeamNumber();
				teamScore = "" + team.getScore();
			}

			if (!hasCustomLine(0)) {
				scoreboard.setLine(0, serverString + "");
			}

			if (!hasCustomLine(2)) {
				scoreboard.setLine(2, ChatColor.GOLD + teamString);
			}

			if (!hasCustomLine(3)) {
				scoreboard.setLine(3, ChatColor.GOLD + "Score: " + ChatColor.AQUA + score);
			}

			if (!hasCustomLine(4)) {
				scoreboard.setLine(4, ChatColor.GOLD + "Team score: " + ChatColor.AQUA + teamScore);
			}

			if (!hasCustomLine(5)) {
				scoreboard.setLine(5, ChatColor.GOLD + "Kills: " + ChatColor.AQUA + kills);
			}

			if (!hasCustomLine(13)) {
				int load = MCFCore.getInstance().getServerLoad().getLoadPercentage();

				ChatColor loadColor = ChatColor.GREEN;

				if (load > 50) {
					loadColor = ChatColor.RED;
				} else if (load > 25) {
					loadColor = ChatColor.YELLOW;
				}

				scoreboard.setLine(13, loadColor + "Server load: " + load + "%");
			}

			if (!hasCustomLine(14)) {
				scoreboard.setLine(14, ChatColor.YELLOW + "http://mcfridays.xyz");
			}

			for (Integer i : customLines.keySet()) {
				scoreboard.setLine(i, customLines.get(i));
			}

			// for (Integer i : customLines.keySet()) {
			// System.out.println("Line " + i + " " + customLines.get(i));
			// }
		}
	}

	/* --- events --- */
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		createScoreboard(p);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();

		if (scoreboards.containsKey(p.getUniqueId())) {
			scoreboards.remove(p.getUniqueId());
		}
	}

	/* --- get and set data --- */

	public HashMap<Integer, String> getCustomLines() {
		return customLines;
	}

	public void setCustomLine(int line, String text) {
		// System.out.println("setCustomLine called " + line + " " + text);
		this.customLines.put(line, text);
	}

	public void deleteCustomLine(int line) {
		// System.out.println("deleteCustomLine called " + line);
		if (customLines.containsKey(line)) {
			customLines.remove(line);
		}

		for (UUID uuid : scoreboards.keySet()) {
			scoreboards.get(uuid).setLine(line, ChatColor.GOLD + "");
		}
	}

	public boolean hasCustomLine(int line) {
		return customLines.containsKey(line);
	}

	public String getServerString() {
		return serverString;
	}

	public void setServerString(String serverString) {
		this.serverString = serverString;
	}

	public HashMap<UUID, ScoreboardWrapper> getScoreboards() {
		return scoreboards;
	}

	public ScoreboardWrapper getPlayerScoreboardWrapper(Player player) {
		return this.getPlayerScoreboardWrapper(player.getUniqueId());
	}

	public ScoreboardWrapper getPlayerScoreboardWrapper(UUID uuid) {
		return this.getScoreboards().get(uuid);
	}

	public Scoreboard getPlayerScoreboard(Player player) {
		return this.getPlayerScoreboard(player.getUniqueId());
	}

	public Scoreboard getPlayerScoreboard(UUID uuid) {
		ScoreboardWrapper sw = this.getPlayerScoreboardWrapper(uuid);
		if (sw == null) {
			return null;
		}
		return sw.getScoreboard();
	}
}