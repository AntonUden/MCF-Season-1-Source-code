package xyz.mcfridays.mcf.mcfcore.Game;

import java.util.ArrayList;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.connorlinfoot.titleapi.TitleAPI;

import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Events.PlayerEliminatedEvent;
import xyz.mcfridays.mcf.mcfcore.Log.ErrorLogger;
import xyz.mcfridays.mcf.mcfcore.Loot.LootTable;
import xyz.mcfridays.mcf.mcfcore.Teams.Team;

public abstract class Game implements Listener {
	protected ArrayList<UUID> players;
	protected World world;
	protected int killReward;

	private int[] winScore;

	protected String lobbyServer;

	private boolean hasEnded;

	public Game(World world) {
		this.world = world;

		this.killReward = 0;

		this.lobbyServer = null;

		this.hasEnded = false;

		this.winScore = new int[] {};

		players = new ArrayList<UUID>();
	}

	public final void load() {
		this.onLoad();
	}

	public final void unload() {
		this.onUnload();
	}

	public LootTable getChestLootTable() {
		return null;
	}

	public LootTable getEnderChestLootTable() {
		return null;
	}

	public LootTable getDropLootTable() {
		return null;
	}

	/**
	 * Return all players that is still in the game
	 * 
	 * @return {@link ArrayList} with {@link UUID} of players
	 */
	public ArrayList<UUID> getPlayers() {
		return players;
	}

	/**
	 * Check if pvp is enabled
	 * 
	 * @return return false do disable pvp
	 */
	public abstract boolean pvpEnabled();

	/**
	 * Return true if the kill reward is enabled
	 * 
	 * @return true to give players score on kills
	 */
	public abstract boolean killRewardEnabled();

	/**
	 * Teleport player to the lobby. this function should also change the players
	 * gamemode
	 * 
	 * @param player {@link Player} to teleport
	 */
	public abstract void tpToLobby(Player player);

	/**
	 * Teleport player to the arena. this function should also change the players
	 * gamemode
	 * 
	 * @param player {@link Player} to teleport
	 */
	public abstract void tpToArena(Player player);

	/**
	 * Teleport player to the srena spectator location. this function should also
	 * change the players gamemode
	 * 
	 * @param player {@link Player} to teleport
	 */
	public abstract void tpToSpectator(Player player);

	/**
	 * Spawn a loot drop in the arena and announce it to all players
	 * 
	 * @param location
	 */
	public void spawnLootDrop(Location location) {
		this.spawnLootDrop(location, true);
	}

	/**
	 * Spawn a loot drop in the arena
	 * 
	 * @param location
	 * @param announce <code>true</code> to announce the drop to all players
	 */
	public void spawnLootDrop(Location location, boolean announce) {
		MCFCore.getInstance().getLootDropManager().spawnDrop(location, this.getDropLootTable().getName(), announce);
	}

	/**
	 * Refill all chests and announce the refill to all players
	 */
	public void refillChests() {
		this.refillChests(true);
	}

	/**
	 * Refill all chests
	 * 
	 * @param announce <code>true</code> to announce the refill to all players
	 */
	public void refillChests(boolean announce) {
		MCFCore.getInstance().getChestLootManager().refillChests(announce);
	}

	/**
	 * Return the {@link World} that the game is using. if the game uses a separate
	 * world for lobby this should return the arena world
	 * 
	 * @return
	 */
	public World getWorld() {
		return world;
	}

	protected void onUnload() {
	}

	protected void onLoad() {
	}

	/**
	 * Called when a player joins the server
	 * 
	 * @param player      {@link Player} that joined
	 * @param reconnected true if the player reconnected
	 */
	public void onPlayerJoin(Player player, boolean reconnected) {
	}

	/**
	 * Called when a player quits. This will be called on all players including the
	 * ones that are not playing anymore. Called before the code that eliminates
	 * players {@link Game#eliminatePlayer(PlayerEliminationReason, Player, Player)}
	 * 
	 * @param player {@link Player} that quit
	 */
	public void onPlayerQuit(Player player) {
	}

	/**
	 * Called when a player dies. This will be called on all players including the
	 * ones that are not playing anymore. Called before the code that eliminates
	 * players {@link Game#eliminatePlayer(PlayerEliminationReason, Player, Player)}
	 * 
	 * @param player {@link Player} that was killed
	 * @param killer {@link LivingEntity} that killed the {@link Player}, Can be
	 *               <code>null</code>
	 */
	public void onPlayerDeath(Player player, LivingEntity killer) {
	}

	public boolean isPlaying(OfflinePlayer player) {
		return players.contains(player.getUniqueId());
	}

	public WorldBorder getWorldborder() {
		return world.getWorldBorder();
	}

	public void setWorldborderCenter(double x, double z) {
		world.getWorldBorder().setCenter(x, z);
	}

	public void setWorldborderSize(double size) {
		this.setWorldborderSize(size, 0L);
	}

	public void setWorldborderSize(double size, long seconds) {
		world.getWorldBorder().setSize(size, seconds);
	}

	public void setWorldborderDamageBuffer(double blocks) {
		world.getWorldBorder().setDamageBuffer(blocks);
	}

	public void setWorldborderDamageAmount(double damage) {
		world.getWorldBorder().setDamageAmount(damage);
	}

	/**
	 * Get kill reward. Do not {@link Override}
	 * 
	 * @return score to add when a player gets a kill
	 */
	public int getKillReward() {
		return killReward;
	}

	/**
	 * Set reward for kills
	 * 
	 * @param killReward score to add every time a player gets a kill
	 */
	public void setKillReward(int killReward) {
		this.killReward = killReward;
	}

	/**
	 * Check if kill reward is set. Do not {@link Override}
	 * 
	 * @return if kill reward is set
	 */
	public boolean hasKillReward() {
		return killReward != 0;
	}

	/**
	 * Check if kill reward should be added to the team score. Default value: true
	 * 
	 * @return <code>true</code> if kill reward is added to team score
	 */
	public boolean isKillRewardShared() {
		return true;
	}

	/**
	 * Check if player should be eliminated on death. Default value: false
	 * 
	 * @return <code>true</code> if player should be eliminated on death
	 */
	public boolean eliminatePlayerOnDeath() {
		return false;
	}

	/**
	 * Check if player should be eliminated on quit. Default value: false
	 * 
	 * @return <code>true</code> if player should be eliminated on quit
	 */
	public boolean eliminatePlayerOnQuit() {
		return false;
	}

	/**
	 * Get the lobby server name
	 * 
	 * @return the name of the lobby server
	 */
	public String getLobbyServer() {
		return lobbyServer;
	}

	/**
	 * Set the lobby server name
	 * 
	 * @param lobbyServer name of lobby server
	 */
	public void setLobbyServer(String lobbyServer) {
		this.lobbyServer = lobbyServer;
	}

	/**
	 * Check if lobby server is set. Do not {@link Override}
	 * 
	 * @return if lobby server is set
	 */
	public boolean hasLobbyServer() {
		return lobbyServer != null;
	}

	public void setWinScore(int... winScore) {
		this.winScore = winScore;
	}

	/**
	 * Check if win score is set
	 * 
	 * @return true if win score has values
	 */
	public boolean hasWinScore() {
		return winScore.length > 0;
	}

	/**
	 * Get array with win score
	 * 
	 * @return array of score to add
	 */
	public int[] getWinScore() {
		return winScore;
	}

	/**
	 * Get name of place
	 * 
	 * @param place place number
	 * @return string with name
	 */
	public String getPlaceName(int place) {
		switch (place) {
		case 1:
			return "First";

		case 2:
			return "Second";

		case 3:
			return "Third";

		default:
			return null;
		}
	}

	public String getDefaultPlayerEliminationMessage(OfflinePlayer player) {
		return ChatColor.RED + "" + ChatColor.BOLD + "Player eliminated> " + ChatColor.AQUA + ChatColor.BOLD + player.getName();
	}

	/**
	 * Remove player from the game and announce it to the server. this function also
	 * handles kill score and the team place function.
	 * 
	 * Called after {@link Game#onPlayerDeath(Player, Player)} or
	 * {@link Game#onPlayerQuit(Player)}
	 * 
	 * @param reason {@link PlayerEliminationReason} why the player was eliminated
	 * @param player {@link Player} that was eliminated
	 * 
	 * @return <code>true</code> if the player was removed from the game
	 */
	public boolean eliminatePlayer(PlayerEliminationReason reason, OfflinePlayer player) {
		return this.eliminatePlayer(reason, player, null);
	}

	/**
	 * Remove player from the game and announce it to the server. this function also
	 * handles kill score and the team place function.
	 * 
	 * Called after {@link Game#onPlayerDeath(Player, Player)} or
	 * {@link Game#onPlayerQuit(Player)}
	 * 
	 * @param reason {@link PlayerEliminationReason} why the player was eliminated
	 * @param player {@link Player} that was eliminated
	 * @param killer {@link Player} that killed the player, this will be null if the
	 *               player was not killed by another player
	 * 
	 * @return <code>true</code> if the player was removed from the game
	 */
	public boolean eliminatePlayer(PlayerEliminationReason reason, OfflinePlayer player, @Nullable LivingEntity killer) {
		if (!players.contains(player.getUniqueId())) {
			return false;
		}

		players.remove(player.getUniqueId());

		String message = this.getPlayerEliminationReason(reason, player, killer);

		if (message == null) {
			message = reason.getMessage(player, killer);
		}

		if (message == null) {
			message = this.getDefaultPlayerEliminationMessage(player);
		}

		if (killer != null) {
			if (killer instanceof Player) {
				MCFCore.getInstance().getScoreManager().addKill(killer.getUniqueId());
				System.out.println("hasKillReward: " + this.hasKillReward() + " getKillReward: " + this.getKillReward() + " killRewardEnabled " + this.killRewardEnabled());
				if (this.hasKillReward()) {
					if (this.killRewardEnabled()) {
						System.out.println("add kill score for " + killer.getName());
						MCFCore.getInstance().getScoreManager().addScore((Player) killer, this.getKillReward(), true);
					}
				}
			}
		}

		PlayerEliminatedEvent event = new PlayerEliminatedEvent(player, reason);
		Bukkit.getPluginManager().callEvent(event);

		Bukkit.getServer().broadcastMessage(message);

		Team team = MCFCore.getInstance().getTeamManager().getPlayerTeam(player);

		if (team == null) {
			return true;
		}

		ArrayList<Team> teamsLeft = this.getTeamsLeft();

		if (teamsLeft.contains(team)) {
			return true;
		}

		int place = 2;
		int score = 0;
		if (teamsLeft.size() > 1) {
			place = teamsLeft.size() + 1;
		}

		if (hasWinScore()) {
			if ((place - 1) < winScore.length) {
				score = winScore[place - 1];
			}
		}

		try {
			// Eliminated team place
			onTeamPlace(team, place, score);

			if (place == 2) {
				// Place for number 1 team
				ArrayList<Team> teamsLeftList = this.getTeamsLeft();
				if (teamsLeftList.size() == 1) {
					int score2 = 0;
					if (winScore.length > 0) {
						score2 = winScore[0];
					}
					onTeamPlace(teamsLeftList.get(0), 1, score2);
				}
			}
		} catch (Exception e) {
			ErrorLogger.logException(e, "Game:eliminatePlayer()->onTeamPlace()");
		}

		if (player.isOnline()) {
			Player onlinePlayer = (Player) player;

			String title = "";

			if (reason == PlayerEliminationReason.KILLED) {
				String killerName = "";
				if (killer != null) {
					killerName = killer.getName();
				}
				title = ChatColor.RED + "Killed by " + killerName;
			} else if(reason == PlayerEliminationReason.DEATH) {
				title = ChatColor.RED + "You died";
			} else if(reason == PlayerEliminationReason.DISQUALIFIED){
				title = ChatColor.RED + "Disqualified";
			} else {
				title = ChatColor.RED + "Eliminated";
			}

			TitleAPI.sendTitle(onlinePlayer, 10, 60, 10, title, "");
		}

		return true;
	}

	/**
	 * Called when a teams place is decided. This function also adds team score to
	 * the {@link Team} and its member
	 * 
	 * @param team  {@link Team}
	 * @param place the teams place. Starts at 1
	 * @param score score to add to team and members
	 */
	public void onTeamPlace(Team team, int place, int score) {
		if (place > 3) {
			return;
		}

		System.out.println("team " + team.getMemberString() + " place: " + place);

		if (place > 1) {
			String message = ChatColor.RED + "" + ChatColor.BOLD + "Team eliminated> " + team.getTeamColor() + ChatColor.BOLD + team.getMemberString() + ChatColor.GOLD + ChatColor.BOLD + " " + this.getPlaceName(place) + " place";

			if (score > 0) {
				message += ChatColor.GRAY + "" + ChatColor.BOLD + " +" + score + " team points";
			}

			Bukkit.getServer().broadcastMessage(message);
		} else {
			String message = ChatColor.GREEN + "" + ChatColor.BOLD + "GAME OVER> " + ChatColor.GOLD + ChatColor.BOLD + "Winning team: " + ChatColor.AQUA + ChatColor.BOLD + team.getMemberString();
			Bukkit.getServer().broadcastMessage(message);

			if (autoEndGame()) {
				end();
			}
		}

		if (score > 0) {
			int playerScore = Math.round(score / team.getTeamMembers().size());
			for (UUID uuid : team.getTeamMembers()) {
				OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(uuid);
				if (player != null) {
					if (player.isOnline()) {
						MCFCore.getInstance().getScoreManager().addScore(uuid, playerScore, false);
						if (player.isOnline()) {
							player.getPlayer().sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + score + " points added to your team");
						}
					}

				}
			}
			MCFCore.getInstance().getScoreManager().addTeamScore(team, score);
		}
	}

	public boolean hasEnded() {
		return hasEnded;
	}

	/**
	 * Check if game should end when there is only 1 or 0 teams left
	 * 
	 * @return <code>true</code> to enable
	 */
	public abstract boolean autoEndGame();

	/**
	 * Stop the game
	 * 
	 * @return <code>false</code> if game has already ended
	 */
	public boolean end() {
		if (this.hasEnded) {
			return false;
		}
		this.hasEnded = true;
		this.endGame();
		return true;
	}

	/**
	 * This code should be called when the game ends. Automatically called by
	 * {@link Game#onTeamPlace(Team, int, int)} unless it has been overridden
	 */
	public abstract void endGame();

	public ArrayList<Team> getTeamsLeft() {
		ArrayList<Team> teamsLeft = new ArrayList<Team>();
		for (UUID uuid : players) {
			Team team = Team.getPlayerTeam(uuid);
			if (team != null) {
				if (!teamsLeft.contains(team)) {
					teamsLeft.add(team);
				}
			}
		}

		return teamsLeft;
	}

	@Deprecated
	public ArrayList<Team> getTeamsLeftOld() {
		ArrayList<Team> teamsLeft = new ArrayList<Team>();
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			if (players.contains(player.getUniqueId())) {
				Team team = MCFCore.getInstance().getTeamManager().getPlayerTeam(player);
				if (team != null) {
					if (!teamsLeft.contains(team)) {
						teamsLeft.add(team);
					}
				}
			}
		}

		return teamsLeft;
	}

	public abstract String getName();

	/**
	 * Called when a player is eliminated to try to get a message to show in chat
	 * 
	 * @param reason {@link PlayerEliminationReason} why the player was eliminated
	 * @param player {@link Player} that was killed
	 * @param killer {@link LivingEntity} that killed the {@link Player}, Can be
	 *               <code>null</code>
	 * @return {@link String} full message to show in chat
	 */
	public String getPlayerEliminationReason(PlayerEliminationReason reason, OfflinePlayer player, LivingEntity killer) {
		return null;
	}

	public EliminationType getEliminationType() {
		return EliminationType.DELAYED;
	}

	/**
	 * @return Delay in seconds that a player has to reconnect before they are
	 *         eliminated
	 */
	public int getEliminationDelay() {
		return 180;
	}
}