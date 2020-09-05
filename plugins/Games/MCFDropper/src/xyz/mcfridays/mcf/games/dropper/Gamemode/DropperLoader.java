package xyz.mcfridays.mcf.games.dropper.Gamemode;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.json.JSONArray;
import org.json.JSONObject;

import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Utils.BlockUtils;

public class DropperLoader {
	public static DropperGamemode load(File file) {
		try {
			World world = Bukkit.getWorlds().get(0);

			String data = FileUtils.readFileToString(file, "UTF-8");

			JSONObject jsonData = new JSONObject(data);

			DropperGamemode dgm = new DropperGamemode(world, jsonData.getInt("max_time"));

			JSONObject playerScoreboard = jsonData.getJSONObject("player_leaderboard_location");
			Location playerScoreboardLocation = new Location(world, BlockUtils.blockCenter(playerScoreboard.getInt("x")), playerScoreboard.getInt("y"), BlockUtils.blockCenter(playerScoreboard.getInt("z")));
			MCFCore.getInstance().getHoloScoreboardManager().setPlayerHologramLocation(playerScoreboardLocation);

			JSONObject teamScoreboard = jsonData.getJSONObject("team_leaderboard_location");
			Location teamScoreboardLocation = new Location(world, BlockUtils.blockCenter(teamScoreboard.getInt("x")), teamScoreboard.getInt("y"), BlockUtils.blockCenter(teamScoreboard.getInt("z")));
			MCFCore.getInstance().getHoloScoreboardManager().setTeamHologramLocation(teamScoreboardLocation);

			JSONObject lobbyLocationJson = jsonData.getJSONObject("lobby_location");
			Location lobbyLocation = new Location(world, BlockUtils.blockCenter(lobbyLocationJson.getInt("x")), lobbyLocationJson.getInt("y"), BlockUtils.blockCenter(lobbyLocationJson.getInt("z")), lobbyLocationJson.getFloat("yaw"), lobbyLocationJson.getFloat("pitch"));
			dgm.setLobbyLocation(lobbyLocation);

			JSONArray maps = jsonData.getJSONArray("maps");
			for (int i = 0; i < maps.length(); i++) {
				JSONObject map = maps.getJSONObject(i);

				JSONObject pos1j = map.getJSONObject("pos1");
				Location pos1 = new Location(world, BlockUtils.blockCenter(pos1j.getInt("x")), pos1j.getInt("y"), BlockUtils.blockCenter(pos1j.getInt("z")));

				JSONObject pos2j = map.getJSONObject("pos2");
				Location pos2 = new Location(world, BlockUtils.blockCenter(pos2j.getInt("x")), pos2j.getInt("y"), BlockUtils.blockCenter(pos2j.getInt("z")));

				JSONObject spawnLocationJ = map.getJSONObject("start_location");
				Location spawnLocation = new Location(world, BlockUtils.blockCenter(spawnLocationJ.getInt("x")), spawnLocationJ.getInt("y"), BlockUtils.blockCenter(spawnLocationJ.getInt("z")), spawnLocationJ.getFloat("yaw"), spawnLocationJ.getFloat("pitch"));

				JSONObject spectatorLocationJ = map.getJSONObject("spectator_location");
				Location spectatorLocation = new Location(world, BlockUtils.blockCenter(spectatorLocationJ.getInt("x")), spectatorLocationJ.getInt("y"), BlockUtils.blockCenter(spectatorLocationJ.getInt("z")), spectatorLocationJ.getFloat("yaw"), spectatorLocationJ.getFloat("pitch"));

				MapConfiguration mapConfiguration = new MapConfiguration(world, pos1, pos2, spawnLocation, spectatorLocation);

				dgm.getMaps().add(mapConfiguration);
			}

			return dgm;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}