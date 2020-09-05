package xyz.mcfridays.mcf.games.bingo.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import xyz.mcfridays.mcf.games.bingo.MCFBingo;
import xyz.mcfridays.mcf.games.bingo.gamemode.BingoItemGenerator;

public class BingoAdminCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (p.hasPermission("mcf.bingo.admin")) {
				if (args.length != 0) {
					if (args[0].equalsIgnoreCase("start")) {
						MCFBingo.getInstance().start();
						return true;
					}

					if (args[0].equalsIgnoreCase("forcestart")) {
						MCFBingo.getInstance().forcestart();
						return true;
					}

					if (args[0].equalsIgnoreCase("gotoworld")) {
						p.teleport(MCFBingo.getInstance().getGame().getWorld().getSpawnLocation());
						return true;
					}

					if (args[0].equalsIgnoreCase("items")) {
						int i = 1;
						for (ItemStack item : MCFBingo.getInstance().getGame().getTargetItems()) {
							p.sendMessage(ChatColor.AQUA + "" + i + " : " + item.getType() + " : data: " + item.getData());

							i++;
						}
						return true;
					}
					
					if (args[0].equalsIgnoreCase("itemtest")) {
						for(ItemStack item : BingoItemGenerator.getPossibleItems()) {
							p.getInventory().addItem(item);
						}
						return true;
					}

					else if (args[0].equalsIgnoreCase("help")) {
						p.sendMessage(ChatColor.GOLD + "/bingoadmin help : show help");
						p.sendMessage(ChatColor.GOLD + "/bingoadmin start : start game");
						p.sendMessage(ChatColor.GOLD + "/bingoadmin forcestart : force start game");
						p.sendMessage(ChatColor.GOLD + "/bingoadmin gotoworld : tp to bingo world");
						p.sendMessage(ChatColor.GOLD + "/bingoadmin items : show items");
						p.sendMessage(ChatColor.GOLD + "/bingoadmin itemtest : get all possible items");
						return true;
					}

					p.sendMessage(ChatColor.DARK_RED + "Invalid usage. use /bingoadmin help for help");
				} else {
					p.sendMessage(ChatColor.DARK_RED + "Invalid usage. use /bingoadmin help for help");
				}
			} else {
				p.sendMessage(ChatColor.DARK_RED + "You dont have permission to use this");
			}
		} else {
			sender.sendMessage(ChatColor.DARK_RED + "This command is only for players");
		}
		return false;
	}
}