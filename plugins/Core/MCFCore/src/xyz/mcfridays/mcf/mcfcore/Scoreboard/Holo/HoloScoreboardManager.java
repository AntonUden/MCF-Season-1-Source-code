package xyz.mcfridays.mcf.mcfcore.Scoreboard.Holo;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;

import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Scoreboard.TopScore;
import xyz.mcfridays.mcf.mcfcore.Scoreboard.ScoreData.PlayerScoreData;
import xyz.mcfridays.mcf.mcfcore.Scoreboard.ScoreData.TeamScoreData;

public class HoloScoreboardManager {
	private Hologram teamHologram;
	private Hologram playerHologram;

	private int taskId;

	private int lines;

	public HoloScoreboardManager() {
		this.teamHologram = null;
		this.playerHologram = null;

		this.lines = 5;

		this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCFCore.getInstance(), new Runnable() {
			@Override
			public void run() {
				update();
			}
		}, 40L, 40L);
	}

	public void stop() {
		if (taskId != -1) {
			Bukkit.getScheduler().cancelTask(taskId);
			taskId = -1;
		}

		if (teamHologram != null) {
			teamHologram.clearLines();
			teamHologram.delete();
		}

		if (playerHologram != null) {
			playerHologram.clearLines();
			playerHologram.delete();
		}
	}

	public void update() {
		if (playerHologram != null) {
			int lineIndex = 0;

			if (playerHologram.size() <= lineIndex) {
				playerHologram.appendTextLine(ChatColor.GREEN + "" + ChatColor.BOLD + "Top player scores");
			}

			ArrayList<PlayerScoreData> scores = TopScore.getPlayerTopScore(lines);

			for (PlayerScoreData scoreData : scores) {
				lineIndex++;

				if (playerHologram.size() <= lineIndex) {
					playerHologram.appendTextLine("-----------");
				}
				((TextLine) playerHologram.getLine(lineIndex)).setText(ChatColor.YELLOW + "" + lineIndex + ChatColor.GOLD + " : " + scoreData.toString());
			}

			while (playerHologram.size() > lineIndex + 1) {
				playerHologram.removeLine(playerHologram.size() - 1);
			}
		}

		if (teamHologram != null) {
			int lineIndex = 0;

			if (teamHologram.size() <= lineIndex) {
				teamHologram.appendTextLine(ChatColor.GREEN + "" + ChatColor.BOLD + "Top team scores");
			}

			ArrayList<TeamScoreData> scores = TopScore.getTeamTopScore(lines);

			for (TeamScoreData scoreData : scores) {
				lineIndex++;

				if (teamHologram.size() <= lineIndex) {
					teamHologram.appendTextLine("-----------");
				}
				((TextLine) teamHologram.getLine(lineIndex)).setText(ChatColor.YELLOW + "" + lineIndex + ChatColor.GOLD + " : " + scoreData.toString());
			}

			while (teamHologram.size() > lineIndex + 1) {
				teamHologram.removeLine(teamHologram.size() - 1);
			}
		}
	}

	public void setTeamHologramLocation(Location location) {
		if (teamHologram != null) {
			teamHologram.delete();
		}

		teamHologram = HologramsAPI.createHologram(MCFCore.getInstance(), location);
	}

	public void setPlayerHologramLocation(Location location) {
		if (playerHologram != null) {
			playerHologram.delete();
		}

		playerHologram = HologramsAPI.createHologram(MCFCore.getInstance(), location);
	}

	public int getLines() {
		return lines;
	}

	public void setLines(int lines) {
		this.lines = lines;
	}
}