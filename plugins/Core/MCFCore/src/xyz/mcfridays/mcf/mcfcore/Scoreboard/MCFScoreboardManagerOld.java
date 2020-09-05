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
@Deprecated
public class MCFScoreboardManagerOld implements Listener {
	private HashMap<UUID, ScoreboardWrapper> scoreboards;

	private int taskId;

	private String serverString;

	public static final int LINES = 16;

	public MCFScoreboardManagerOld() {
		this.scoreboards = new HashMap<UUID, ScoreboardWrapper>();

		this.serverString = "null";

		for (Player p : Bukkit.getOnlinePlayers()) {
			initScoreboard(p);
		}

		this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFCore.getInstance(), new Runnable() {
			@Override
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()) {
					updateScoreboard(p);
				}
			}
		}, 10L, 10L);
	}

	public Scoreboard getPlayerScoreboard(Player player) {
		if (scoreboards.containsKey(player.getUniqueId())) {
			return scoreboards.get(player.getUniqueId()).getScoreboard();
		}

		return null;
	}

	public void stop() {
		if (taskId != -1) {
			Bukkit.getScheduler().cancelTask(taskId);
			taskId = -1;
		}
	}

	public void setLine(int line, String content) {
		for (UUID uuid : scoreboards.keySet()) {
			scoreboards.get(uuid).setLine(line, content);
		}
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

			scoreboard.setLine(0, serverString + "");
			scoreboard.setLine(2, ChatColor.GOLD + teamString);
			scoreboard.setLine(3, ChatColor.GOLD + "Team score: " + ChatColor.AQUA + teamScore);
			scoreboard.setLine(5, ChatColor.GOLD + "Score: " + ChatColor.AQUA + score);
			scoreboard.setLine(6, ChatColor.GOLD + "Kills: " + ChatColor.AQUA + kills);

			int load = MCFCore.getInstance().getServerLoad().getLoadPercentage();

			ChatColor loadColor = ChatColor.GREEN;

			if (load > 50) {
				loadColor = ChatColor.RED;
			} else if (load > 25) {
				loadColor = ChatColor.YELLOW;
			}

			scoreboard.setLine(14, loadColor + "Server load: " + load + "%");

		}
	}

	private void initScoreboard(Player p) {
		if (!scoreboards.containsKey(p.getUniqueId())) {
			ScoreboardWrapper scoreboardWrapper = new ScoreboardWrapper(ChatColor.GREEN + "" + ChatColor.BOLD + "MC Fridays");

			for (int i = 0; i < LINES; i++) {
				scoreboardWrapper.addLine("");
			}

			for (int i = 0; i < LINES; i++) {
				scoreboardWrapper.setLine(i, ChatColor.GOLD + "");
			}

			scoreboards.put(p.getUniqueId(), scoreboardWrapper);

			p.setScoreboard(scoreboardWrapper.getScoreboard());

			updateScoreboard(p);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		initScoreboard(p);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();

		if (scoreboards.containsKey(p.getUniqueId())) {
			scoreboards.remove(p.getUniqueId());
		}
	}

	public String getServerString() {
		return serverString;
	}

	public void setServerString(String serverString) {
		this.serverString = serverString;
	}
}