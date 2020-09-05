package xyz.mcfridays.mcf.mcfcore.Scoreboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Scoreboard.ScoreData.PlayerScoreData;
import xyz.mcfridays.mcf.mcfcore.Scoreboard.ScoreData.TeamScoreData;

public class TopScore {
	public static ArrayList<PlayerScoreData> getPlayerTopScore(int maxEntries) {
		ArrayList<PlayerScoreData> result = new ArrayList<PlayerScoreData>();

		for (UUID uuid : MCFCore.getInstance().getScoreManager().getPlayerScore().keySet()) {
			if (MCFCore.getInstance().getTeamManager().getPlayerTeam(uuid) == null) {
				continue;
			}

			PlayerScoreData scoreData = new PlayerScoreData(uuid, MCFCore.getInstance().getScoreManager().getPlayerScore(uuid));
			result.add(scoreData);
		}

		Collections.sort(result);

		while (result.size() > maxEntries) {
			result.remove(result.size() - 1);
		}

		return result;
	}

	public static ArrayList<TeamScoreData> getTeamTopScore(int maxEntries) {
		ArrayList<TeamScoreData> result = new ArrayList<TeamScoreData>();

		for (Integer i : MCFCore.getInstance().getTeamManager().getTeams().keySet()) {
			if (MCFCore.getInstance().getTeamManager().getTeam(i).getTeamMembers().size() == 0) {
				continue;
			}

			TeamScoreData scoreData = new TeamScoreData(MCFCore.getInstance().getTeamManager().getTeam(i));
			result.add(scoreData);
		}

		Collections.sort(result);

		while (result.size() > maxEntries) {
			result.remove(result.size() - 1);
		}

		return result;
	}
}