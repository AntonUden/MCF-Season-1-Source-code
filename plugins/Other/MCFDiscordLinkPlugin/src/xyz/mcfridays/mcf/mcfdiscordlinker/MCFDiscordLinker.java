package xyz.mcfridays.mcf.mcfdiscordlinker;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import xyz.mcfridays.mcf.mcfcommons.database.DBConnection;
import xyz.mcfridays.mcf.mcfdiscordlinker.Utils.IDGenerator;

public class MCFDiscordLinker extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}

		saveDefaultConfig();

		if (!DBConnection.init(getConfig().getString("mysql.driver"), getConfig().getString("mysql.host"), getConfig().getString("mysql.username"), getConfig().getString("mysql.password"), getConfig().getString("mysql.database"))) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Failed to connect to database! disabling...");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		this.getServer().getPluginManager().registerEvents(this, this);

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				// send MySQL query to keep the connection alive
				try {
					System.out.println("Sending query to MySQL to keep connection alive");
					String sql = "SELECT 1";
					PreparedStatement ps = DBConnection.connection.prepareStatement(sql);
					ps.executeQuery();
					ps.close();
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}, 72000L, 72000L);
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll((Plugin) this);
		Bukkit.getScheduler().cancelTasks(this);
		try {
			DBConnection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void kick(Player p, Exception e) {
		kick(p, ChatColor.RED + "An internal server error occurred\nPlease send a screenshot of this message to an admin\n\n" + e.getClass().getName() + " : " + e.getMessage());
	}

	public void kick(Player p, String message) {
		p.kickPlayer(ChatColor.AQUA + "-=-=-=-= MCF Discord =-=-=-=-\n\n" + message);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		e.setJoinMessage(null);

		Player p = e.getPlayer();

		boolean playerFound = false;

		boolean isLinked = false;

		String linkCode = "";

		String message = "";

		try {
			String sql = "SELECT link_code, discord_id FROM users WHERE uuid = ?";
			PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

			ps.setString(1, p.getUniqueId().toString());

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				playerFound = true;

				linkCode = rs.getString("link_code");

				if (rs.getString("discord_id") != null) {
					isLinked = true;
				}
			}

			rs.close();
			ps.close();
		} catch (Exception ee) {
			ee.printStackTrace();
			kick(p, ee);
			return;
		}

		if (!playerFound) {
			linkCode = IDGenerator.generateId();
			try {
				String sql = "INSERT INTO users (uuid, link_code, minecraft_username) VALUES (?, ?, ?)";
				PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

				ps.setString(1, p.getUniqueId().toString());
				ps.setString(2, linkCode);
				ps.setString(3, e.getPlayer().getName());

				ps.executeUpdate();

				ps.close();
			} catch (Exception ee) {
				ee.printStackTrace();
				kick(p, ee);
				return;
			}
		}

		if (isLinked) {
			message = ChatColor.RED + "You have already linked your account";
		} else {
			message = ChatColor.GREEN + "Send this message to MC FRIDAYS bot DM's. " + ChatColor.GOLD + " !link " + linkCode;
		}

		kick(p, message);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		e.setQuitMessage(null);
	}
}