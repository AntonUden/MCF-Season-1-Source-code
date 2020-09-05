package xyz.mcfridays.mcf.mcfcore.Commands;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import xyz.mcfridays.mcf.mcfcommons.database.DBConnection;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Teams.Team;

public class TeamCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (args.length == 0) {
				showHelp(sender);
				return true;
			} else {
				if (args[0].equalsIgnoreCase("help")) {
					showHelp(sender);
					return false;
				}

				if (args[0].equalsIgnoreCase("adminlist")) {
					if (p.hasPermission("mcf.team.admin")) {
						p.sendMessage(ChatColor.GOLD + "==============================");
						for (Integer teamId : MCFCore.getInstance().getTeamManager().getTeams().keySet()) {
							Team team = MCFCore.getInstance().getTeamManager().getTeam(teamId);

							String players = "";

							for (UUID uuid : team.getTeamMembers()) {
								players += (players.length() == 0 ? "" : ", ") + Bukkit.getOfflinePlayer(uuid).getName();
							}
							p.sendMessage(ChatColor.AQUA + "TeamNumber: " + team.getTeamNumber());
							p.sendMessage(ChatColor.AQUA + "TeamColor: " + team.getTeamColor() + team.getTeamColor().name());
							p.sendMessage(ChatColor.AQUA + "Score: " + team.getScore());
							p.sendMessage(ChatColor.AQUA + "Members: " + players);

							p.sendMessage(ChatColor.GOLD + "==============================");
						}
					} else {
						p.sendMessage(ChatColor.DARK_RED + "You don't have permission to use this command");
					}
					return false;
				}

				if (args[0].equalsIgnoreCase("players")) {
					if (p.hasPermission("mcf.team.admin")) {

						ArrayList<UUID> missingPlayers = new ArrayList<UUID>();
						ArrayList<UUID> onlinePlayers = new ArrayList<UUID>();
						ArrayList<UUID> otherPlayers = new ArrayList<UUID>();

						for (UUID uuid : MCFCore.getInstance().getTeamManager().getPlayerTeams().keySet()) {
							missingPlayers.add(uuid);
						}

						for (Player player : Bukkit.getServer().getOnlinePlayers()) {
							if (missingPlayers.contains(player.getUniqueId())) {
								missingPlayers.remove(player.getUniqueId());
							}

							if (MCFCore.getInstance().getTeamManager().getPlayerTeam(player) != null) {
								onlinePlayers.add(player.getUniqueId());
							} else {
								otherPlayers.add(player.getUniqueId());
							}
						}

						String missingPlayersString = "";
						String onlinePlayersString = "";
						String otherPlayersString = "";

						for (UUID uuid : missingPlayers) {
							Team team = MCFCore.getInstance().getTeamManager().getPlayerTeam(uuid);
							missingPlayersString += team.getTeamColor() + DBConnection.getPlayerName(uuid) + " ";
						}

						for (UUID uuid : onlinePlayers) {
							Team team = MCFCore.getInstance().getTeamManager().getPlayerTeam(uuid);
							onlinePlayersString += team.getTeamColor() + DBConnection.getPlayerName(uuid) + " ";
						}

						for (UUID uuid : otherPlayers) {
							otherPlayersString += DBConnection.getPlayerName(uuid) + " ";
						}

						p.sendMessage(ChatColor.GOLD + "==============================");
						p.sendMessage(ChatColor.RED + "Missing players: " + missingPlayers.size());
						p.sendMessage(ChatColor.RED + missingPlayersString);
						p.sendMessage(ChatColor.GREEN + "Online players: " + onlinePlayers.size());
						p.sendMessage(ChatColor.GREEN + onlinePlayersString);
						p.sendMessage(ChatColor.GRAY + "Other players: " + otherPlayers.size());
						p.sendMessage(ChatColor.GRAY + otherPlayersString);
						p.sendMessage(ChatColor.GOLD + "==============================");

					} else {
						p.sendMessage(ChatColor.DARK_RED + "You don't have permission to use this command");
					}
					return false;
				}

				if (args[0].equalsIgnoreCase("import")) {
					if (p.hasPermission("mcf.team.admin")) {
						p.sendMessage(ChatColor.GOLD + "Importing teams...");
						MCFCore.getInstance().getTeamManager().importTeams();
						p.sendMessage(ChatColor.GOLD + "Done");
					} else {
						p.sendMessage(ChatColor.DARK_RED + "You don't have permission to use this command");
					}
					return false;
				}

				if (args[0].equalsIgnoreCase("list")) {
					Team team = MCFCore.getInstance().getTeamManager().getPlayerTeam(p);
					if (team == null) {
						p.sendMessage(ChatColor.RED + "You are not in a team");
					} else {
						String players = "";

						for (UUID uuid : team.getTeamMembers()) {
							players += (players.length() == 0 ? "" : ", ") + Bukkit.getOfflinePlayer(uuid).getName();
						}

						p.sendMessage(ChatColor.AQUA + players + " is in your team");
					}

					return true;
				}

				sender.sendMessage(ChatColor.RED + "Invalid usage. type /team help for help");
				return true;
			}
		}
		return false;
	}

	private void showHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.GOLD + "/team help : show help");
		sender.sendMessage(ChatColor.GOLD + "/team list : list team members");

		if (sender.hasPermission("mcf.team.admin")) {
			sender.sendMessage(ChatColor.GOLD + "/team adminlist : show teams");
			sender.sendMessage(ChatColor.GOLD + "/team players   : show players");
			sender.sendMessage(ChatColor.GOLD + "/team import    : import teams from discord");
		}
	}

}