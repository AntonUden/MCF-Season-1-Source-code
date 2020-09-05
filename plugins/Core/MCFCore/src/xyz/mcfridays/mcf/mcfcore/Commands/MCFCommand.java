package xyz.mcfridays.mcf.mcfcore.Commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Log.ErrorLogger;
import xyz.mcfridays.mcf.mcfcore.Loot.LootTable;
import xyz.mcfridays.mcf.mcfcore.Misc.TestException;
import xyz.mcfridays.mcf.mcfcore.Teams.Team;
import xyz.mcfridays.mcf.mcfcore.Utils.BungeecordUtils;
import xyz.mcfridays.mcf.mcfcore.Utils.SlowPlayerSender;

public class MCFCommand implements CommandExecutor {
	private String haltPassword;

	public MCFCommand() {
		haltPassword = UUID.randomUUID().toString().substring(0, 8);
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender.hasPermission("mcf.mcf")) {
			if (args.length == 0) {
				showHelp(sender);
				return true;
			} else {
				if (args[0].equalsIgnoreCase("help")) {
					showHelp(sender);
					return true;
				} else if (args[0].equalsIgnoreCase("halt")) {
					if (args.length == 2) {
						if (args[1].equalsIgnoreCase(haltPassword)) {
							Bukkit.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "MCF Plugin halt initiated by " + sender.getName());
							MCFCore.getInstance().halt();
							return true;
						}
					}
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "---------- WARNING -----------");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "YOU ARE ABOUT TO HALT ALL");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "MCF PLUGIN ACTIVITY.");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "TO CONFIRM MCF HALT USE");
					sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/mcf halt " + haltPassword);
					return false;
				} else if (args[0].equalsIgnoreCase("sendall")) {
					if (args.length == 2) {
						String server = args[1];

						sender.sendMessage(ChatColor.GREEN + "Trying to send all players to " + server);

						new SlowPlayerSender(Bukkit.getServer().getOnlinePlayers(), server).start();
					} else {
						sender.sendMessage(ChatColor.RED + "Useage: /mcf sendall <server>");
					}
					return true;
				} else if (args[0].equalsIgnoreCase("server")) {
					if (sender instanceof Player) {
						if (args.length == 2) {
							String server = args[1];

							sender.sendMessage(ChatColor.GREEN + "Trying to send you to " + server);

							BungeecordUtils.sendToServer((Player) sender, server);
						} else {
							sender.sendMessage(ChatColor.RED + "Useage: /mcf server <server>");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "This command is only for players");
					}
					return true;
				} else if (args[0].equalsIgnoreCase("message")) {
					if (sender instanceof Player) {
						if (args.length > 1) {
							String message = "";
							for (int i = 1; i < args.length; i++) {
								message += args[i] + " ";
							}
							message = ChatColor.translateAlternateColorCodes('&', message);

							for (Player player : Bukkit.getOnlinePlayers()) {
								player.sendMessage(message);
							}
						} else {
							sender.sendMessage(ChatColor.RED + "Useage: /mcf server <server>");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "This command is only for players");
					}
					return true;
				} else if (args[0].equalsIgnoreCase("testloot")) {
					if (sender.hasPermission("mcf.mcf.testloot")) {
						if (sender instanceof Player) {
							if (args.length == 2) {
								String lootTableName = args[1];

								LootTable lootTable = MCFCore.getInstance().getLootTableManager().getLootTable(lootTableName);

								if (lootTable == null) {
									sender.sendMessage(ChatColor.RED + "Could not find loot table named " + lootTableName);
								} else {
									Player p = (Player) sender;

									p.sendMessage(ChatColor.AQUA + "Generating loot...");
									p.getInventory().clear();

									for (ItemStack item : lootTable.generateLoot()) {
										p.getInventory().addItem(item);
									}
								}

							} else {
								sender.sendMessage(ChatColor.RED + "Useage: /mcf testloot <loot table>");
							}
						} else {
							sender.sendMessage(ChatColor.RED + "This command is only for players");
						}
						return true;
					} else {
						sender.sendMessage(ChatColor.DARK_RED + "You don't have permission to use this command");
					}
				} else if (args[0].equalsIgnoreCase("testaddscore")) {
					if (sender.hasPermission("mcf.mcf.testaddscore")) {
						if (sender instanceof Player) {
							Player p = (Player) sender;

							Team team = MCFCore.getInstance().getTeamManager().getPlayerTeam(p);

							p.sendMessage(ChatColor.GOLD + "Add solo only +10");
							MCFCore.getInstance().getScoreManager().addScore(p, 10, false);

							p.sendMessage(ChatColor.GOLD + "Add shared +100");
							MCFCore.getInstance().getScoreManager().addScore(p, 100, true);

							if (team == null) {
								p.sendMessage(ChatColor.RED + "No team to test");
							} else {
								p.sendMessage(ChatColor.GOLD + "Add team +1000");
								team.addScore(1000);
							}
						} else {
							sender.sendMessage(ChatColor.RED + "This command is only for players");
						}
						return true;
					} else {
						sender.sendMessage(ChatColor.DARK_RED + "You don't have permission to use this command");
					}
				} else if (args[0].equalsIgnoreCase("testerrorlog")) {
					if (sender.hasPermission("mcf.mcf.testerrorlog")) {
						ErrorLogger.logException(new TestException("Test Exception message"), "MCFCommand::testerrorlog");
					} else {
						sender.sendMessage(ChatColor.DARK_RED + "You don't have permission to use this command");
					}
				} else if (args[0].equalsIgnoreCase("plist")) {
					if (sender.hasPermission("mcf.mcf.plist")) {
						if (MCFCore.getInstance().getGameManager().getActiveGame() != null) {
							String message = ChatColor.GOLD + "Players in game (" + MCFCore.getInstance().getGameManager().getActiveGame().getPlayers().size() + "): ";
							for (UUID uuid : MCFCore.getInstance().getGameManager().getActiveGame().getPlayers()) {
								OfflinePlayer p = Bukkit.getServer().getOfflinePlayer(uuid);

								message += p.getName() + " ";
							}

							sender.sendMessage(message);
							return true;
						} else {
							sender.sendMessage(ChatColor.DARK_RED + "No active game");
						}
					} else {
						sender.sendMessage(ChatColor.DARK_RED + "You don't have permission to use this command");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Invalid useage. type /mcf help for help");
				}
			}
		} else {
			sender.sendMessage(ChatColor.DARK_RED + "You don't have permission to use this command");
		}
		return false;
	}

	private void showHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.GOLD + "/mcf help : show help");
		sender.sendMessage(ChatColor.GOLD + "/mcf halt : halt mcf plugins");
		sender.sendMessage(ChatColor.GOLD + "/mcf server <server> : send you to a server");
		sender.sendMessage(ChatColor.GOLD + "/mcf sendall <server> : send all players to server");
		sender.sendMessage(ChatColor.GOLD + "/mcf message <message> : send message to all players");
		sender.sendMessage(ChatColor.GOLD + "/mcf testloot <loot table> : test loot table content");
		sender.sendMessage(ChatColor.GOLD + "/mcf testaddscore : test score system");
		sender.sendMessage(ChatColor.GOLD + "/mcf testerrorlog : test error log");
		sender.sendMessage(ChatColor.GOLD + "/mcf plist : show players that are alive in the active game");
	}
}