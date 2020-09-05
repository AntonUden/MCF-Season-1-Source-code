package xyz.mcfridays.mcf.bungeecord;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import xyz.mcfridays.mcf.mcfcommons.database.DBConnection;

public class MCFBungeecord extends Plugin implements Listener {
	private Configuration configuration;

	private static MCFBungeecord instance;
	
	public static MCFBungeecord getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		instance = this;
		
		try {
			if (!getDataFolder().exists()) {
				getDataFolder().mkdir();
			}

			File cfgfile = new File(getDataFolder(), "config.yml");

			if (!cfgfile.exists()) {
				try (InputStream in = getResourceAsStream("config.yml")) {
					Files.copy(in, cfgfile.toPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));

			getProxy().getPluginManager().registerListener(this, this);

			DBConnection.init(configuration.getString("mysql.driver"), configuration.getString("mysql.host"), configuration.getString("mysql.username"), configuration.getString("mysql.password"), configuration.getString("mysql.database"));
			
			ProxyServer.getInstance().getPluginManager().registerListener(this, this);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		ProxyServer.getInstance().getPluginManager().unregisterListeners((Plugin) this);
		
		try {
			DBConnection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onChat(ChatEvent e) {
		if (e.getSender() instanceof ProxiedPlayer) {
			ProxiedPlayer player = (ProxiedPlayer) e.getSender();
			
			try {
				String sql = "INSERT INTO `chat_log` (`uuid`, `username`, `server`, `timestamp`, `content`, `type`) VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";
				PreparedStatement ps = DBConnection.connection.prepareStatement(sql);

				ps.setString(1, player.getUniqueId().toString());
				ps.setString(2, player.getName());
				ps.setString(3, player.getServer().getInfo().getName());
				ps.setString(4, e.getMessage());
				ps.setString(5, (e.getMessage().startsWith("/") ? "COMMAND" : "CHAT"));

				ps.executeUpdate();
				ps.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}