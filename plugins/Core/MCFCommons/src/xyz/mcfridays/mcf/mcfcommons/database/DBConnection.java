package xyz.mcfridays.mcf.mcfcommons.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class DBConnection {
	/**
	 * Instance of the {@link Connection} to the database
	 */
	public static Connection connection;

	/**
	 * Connects to the database
	 * 
	 * @param credentials {@link DBCredentials} for the connection
	 * @return <code>true</code> on success or <code>false</code> on failure
	 */
	public static boolean init(DBCredentials credentials) {
		return init(credentials.getDriver(), credentials.getHost(), credentials.getUsername(), credentials.getPassword(), credentials.getDatabase());
	}

	/**
	 * Connects to the database
	 * 
	 * @param driver   driver to use
	 * @param host     host to connect to
	 * @param user     user name of the MySQL user
	 * @param pass     password of the MySQL user
	 * @param database name of the database to use
	 * @return <code>true</code> on success or <code>false</code> on failure
	 */
	public static boolean init(String driver, String host, String user, String pass, String database) {
		try {
			if (connection != null) {
				if (!connection.isClosed()) {
					return false;
				} else {
					connection = null;
				}
			}
			Class.forName(driver);
			connection = DriverManager.getConnection(host, user, pass);
			connection.setCatalog(database);
			return true;
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Close the {@link Connection} to the database
	 * 
	 * @return <code>true</code> if the {@link Connection} was closed
	 * @throws SQLException
	 */
	public static boolean close() throws SQLException {
		if (connection == null) {
			return false;
		}

		if (connection.isClosed()) {
			return false;
		}

		connection.close();
		return true;
	}

	public static boolean isConnected() throws SQLException {
		if (connection != null) {
			return !connection.isClosed();
		}
		return false;
	}

	public static boolean testQuery() {
		try {
			String sql = "SELECT 1";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.executeQuery();
			ps.close();
		} catch (Exception ee) {
			ee.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Get user name string for a user by {@link UUID}
	 * 
	 * @param uuid {@link UUID} of user to get name for
	 * @return user name or <code>null</code> if the player does not exist in the
	 *         database
	 */
	public static String getPlayerName(UUID uuid) {
		String result = null;
		try {
			String sql = "SELECT name FROM players WHERE uuid = ?";
			PreparedStatement ps = connection.prepareStatement(sql);

			ps.setString(1, uuid.toString());

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getString("name");
			}

			rs.close();
			ps.close();
		} catch (Exception ee) {
			ee.printStackTrace();
		}

		return result;
	}

	public static String getData(String key) {
		String result = null;
		try {
			String sql = "SELECT data_value FROM mcf_data WHERE data_key = ?";
			PreparedStatement ps = connection.prepareStatement(sql);

			ps.setString(1, key);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getString("data_value");
			}

			rs.close();
			ps.close();
		} catch (Exception ee) {
			ee.printStackTrace();
		}

		return result;
	}

	public static boolean setData(String key, String value) {
		try {
			String sql = "UPDATE mcf_data SET data_value = ? WHERE data_key = ?";
			PreparedStatement ps = connection.prepareStatement(sql);

			ps.setString(1, value);
			ps.setString(2, key);

			ps.executeUpdate();

			ps.close();
		} catch (Exception ee) {
			ee.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean setActiveServer(String server) {
		Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "Changing active server to " + server);
		return setData("active_server", server);
	}

	public static String getActiveServer() {
		return getData("active_server");
	}
}