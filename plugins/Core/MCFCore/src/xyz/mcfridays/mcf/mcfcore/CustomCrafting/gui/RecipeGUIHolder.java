package xyz.mcfridays.mcf.mcfcore.CustomCrafting.gui;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.GUI.GUIInventoryHolder;

public class RecipeGUIHolder extends GUIInventoryHolder {
	private UUID player;
	private int page;

	public RecipeGUIHolder(UUID player, int page) {
		this.player = player;
		this.page = page;
	}

	public UUID getPlayer() {
		return player;
	}

	public int getPage() {
		return page;
	}

	public boolean hasNextPage() {
		return getCustomRecipeGUI().hasNextPage(page);
	}

	public boolean hasPreviousPage() {
		return getCustomRecipeGUI().hasPreviousPage(page);
	}

	public boolean openNextPage() {
		return this.openPage(page + 1);
	}

	public boolean openPreviousPage() {
		return this.openPage(page - 1);
	}

	public boolean openPage(int number) {
		Player p = Bukkit.getServer().getPlayer(player);
		if (p != null) {
			if (p.isOnline()) {
				return getCustomRecipeGUI().show(p, number);
			}
		}

		return false;
	}

	private CustomRecipeGUI getCustomRecipeGUI() {
		return MCFCore.getInstance().getCustomRecipeGUI();
	}
}