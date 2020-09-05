package xyz.mcfridays.mcf.mcfcore.Whitelist;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import net.md_5.bungee.api.ChatColor;
import xyz.mcfridays.mcf.mcfcommons.database.DBConnection;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Teams.Team;

public class MCFWhitelist implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent e) {
		Player p = e.getPlayer();

		Team team = MCFCore.getInstance().getTeamManager().getPlayerTeam(p);

		if (team != null) {
			return;
		}

		boolean allow = false;
		try {
			String sql = "SELECT id FROM whitelist WHERE LOWER(username) = ?";
			PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

			ps.setString(1, p.getName().toLowerCase());

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				allow = true;
			}

			rs.close();
			ps.close();

			ps.close();
		} catch (Exception ee) {
			ee.printStackTrace();
			e.disallow(Result.KICK_OTHER, ChatColor.RED + "An error occurred. Please send a screenshot of this to an admin\n\n" + ee.getClass().getName() + " : " + ee.getMessage());
			return;
		}

		if (!allow) {
			try {
				String sql = "SELECT id FROM users WHERE uuid = ? AND discord_id IS NOT NULL";
				PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

				ps.setString(1, p.getUniqueId().toString());

				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					allow = true;
				}

				rs.close();
				ps.close();

				ps.close();
			} catch (Exception ee) {
				ee.printStackTrace();
				e.disallow(Result.KICK_OTHER, ChatColor.RED + "An error occurred. Please send a screenshot of this to an admin\n\n" + ee.getClass().getName() + " : " + ee.getMessage());
				return;
			}
		}

		if (allow) {
			return;
		}

		e.disallow(Result.KICK_WHITELIST, ChatColor.RED + "To join MCF you must first link your minecraft account to your discord account\n\nIf you have been assigned a team but did not get a message from the bot\nplease contact an admin");
	}
}