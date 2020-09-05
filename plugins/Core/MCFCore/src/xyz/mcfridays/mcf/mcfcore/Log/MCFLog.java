package xyz.mcfridays.mcf.mcfcore.Log;

import java.sql.PreparedStatement;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import xyz.mcfridays.mcf.mcfcommons.database.DBConnection;
import xyz.mcfridays.mcf.mcfcore.MCFCore;

public class MCFLog {
	public static boolean log(LogAction action, int teamNumber, Player player, LivingEntity killer, int score, int place) {
		try {
			String sql = "INSERT INTO event_log (`action`, `round_number`, `game_name`, `team_number`, `player_uuid`, `player_name`, `killer_uuid`, `killer_name`, `score`, `place`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
			PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

			ps.setString(1, action.name());
			ps.setInt(2, Integer.parseInt(DBConnection.getData("round_number")));
			
			if(MCFCore.getInstance().getGameManager().hasActiveGame()) {
				ps.setString(3, MCFCore.getInstance().getGameManager().getActiveGame().getName());
			} else {
				ps.setString(3, null);
			}
			
			ps.setInt(4, teamNumber);
			
			if(player != null) {
				ps.setString(5, player.getUniqueId().toString());
				ps.setString(6, player.getName());
			} else {
				ps.setString(5, null);
				ps.setString(6, null);
			}
			
			if(killer != null) {
				ps.setString(7, killer.getUniqueId().toString());
				ps.setString(8, killer.getName());
			} else {
				ps.setString(7, null);
				ps.setString(8, null);
			}
			
			ps.setInt(9, score);
			ps.setInt(10, place);

			ps.executeUpdate();

			ps.close();
		} catch (Exception ee) {
			ee.printStackTrace();
			return false;
		}
		return true;
	}
}