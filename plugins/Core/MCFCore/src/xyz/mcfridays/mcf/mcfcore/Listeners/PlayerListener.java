package xyz.mcfridays.mcf.mcfcore.Listeners;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import xyz.mcfridays.mcf.mcfcommons.database.DBConnection;

public class PlayerListener implements Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		boolean playerFound = false;

		try {
			String sql = "SELECT id FROM players WHERE uuid = ?";
			PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

			ps.setString(1, p.getUniqueId().toString());

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				playerFound = true;
			}

			rs.close();
			ps.close();
		} catch (Exception ee) {
			ee.printStackTrace();
			p.kickPlayer(ChatColor.DARK_RED + ee.getClass().getName() + "\n\n" + ee.getMessage());
			return;
		}

		if (!playerFound) {
			try {
				String sql = "INSERT INTO players (uuid, name) VALUES (?, ?)";
				PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

				ps.setString(1, p.getUniqueId().toString());
				ps.setString(2, p.getName());

				ps.executeUpdate();

				ps.close();
			} catch (Exception ee) {
				ee.printStackTrace();
				p.kickPlayer(ChatColor.DARK_RED + ee.getClass().getName() + "\n\n" + ee.getMessage());
				return;
			}
		}

		try {
			String sql = "UPDATE players SET name = ? WHERE uuid = ?";
			PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

			ps.setString(1, p.getName());
			ps.setString(2, p.getUniqueId().toString());

			ps.executeUpdate();

			ps.close();
		} catch (Exception ee) {
			ee.printStackTrace();
			p.kickPlayer(ChatColor.DARK_RED + ee.getClass().getName() + "\n\n" + ee.getMessage());
			return;
		}
		
		if(!playerFound) {
			p.kickPlayer(ChatColor.AQUA + "Please reconect");
		}
	}
}