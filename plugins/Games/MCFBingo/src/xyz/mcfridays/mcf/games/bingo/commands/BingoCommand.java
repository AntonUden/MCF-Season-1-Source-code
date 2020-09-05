package xyz.mcfridays.mcf.games.bingo.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import xyz.mcfridays.mcf.games.bingo.MCFBingo;
import xyz.mcfridays.mcf.games.bingo.gamemode.Stage;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Teams.Team;

public class BingoCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			
			if(MCFBingo.getInstance().getGame().getStage() == Stage.INGAME) {
				if(MCFBingo.getInstance().getGame().getPlayers().contains(player.getUniqueId())) {
					Team team = MCFCore.getInstance().getTeamManager().getPlayerTeam(player);
					
					if(team != null) {
						player.openInventory(MCFBingo.getInstance().getGame().getTeamMenu(team));
					} else {
						player.sendMessage(ChatColor.RED + "ERR:TEAM_NULL");
					}
				} else {
					player.sendMessage(ChatColor.RED + "You are not playing");
				}
			} else {
				player.sendMessage(ChatColor.RED + "No game in progress");
			}
			return true;
		} else {
			sender.sendMessage(ChatColor.RED + "Only players can use this command");
		}
		return false;
	}
}