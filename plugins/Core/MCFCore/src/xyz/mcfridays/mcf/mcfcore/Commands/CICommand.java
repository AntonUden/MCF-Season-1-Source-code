package xyz.mcfridays.mcf.mcfcore.Commands;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.GUI.GUIClickCallback;
import xyz.mcfridays.mcf.mcfcore.GUI.GUIInventoryHolder;
import xyz.mcfridays.mcf.mcfcore.Game.CustomItems.CustomItem;

public class CICommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			
			if(p.hasPermission("mcf.ci")) {
				GUIInventoryHolder holder = new GUIInventoryHolder();
				Inventory inventory = Bukkit.getServer().createInventory(holder, 54, "Custom items");
				
				HashMap<String, CustomItem> items = MCFCore.getInstance().getCustomItemManager().getCustomItems();
				
				int slot = 0;
				
				for(String key : items.keySet()) {
					inventory.setItem(slot, items.get(key).getItem(p));
					
					holder.addClickCallback(slot, new GUIClickCallback() {
						@Override
						public void onClick(HumanEntity whoClicked, Inventory clickedInventory, int slot, InventoryAction action) {
							p.getInventory().addItem(MCFCore.getInstance().getCustomItemManager().getItem(items.get(key).getClass(), p));
						}
					});
					
					slot++;
				}
				
				p.openInventory(inventory);
			}
		}
		return false;
	}
}