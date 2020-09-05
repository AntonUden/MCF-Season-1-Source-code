package xyz.mcfridays.mcf.mcfcore.CustomCrafting;

import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import xyz.mcfridays.mcf.mcfcore.MCFCore;

public abstract class CustomRecipe {
	private Recipe cachedRecipe = null;

	public abstract Recipe getRecipe();

	public abstract String getName();

	public int getCrafingLimit() {
		return 0;
	}

	public boolean showInGUI() {
		return true;
	}

	public boolean register() {
		return MCFCore.getInstance().getCustomCraftingManager().addRecipe(this);
	}

	public boolean isShaped() {
		return this.getCachedRecipe() instanceof ShapedRecipe;
	}

	public Recipe getCachedRecipe() {
		if (cachedRecipe == null) {
			cachedRecipe = this.getRecipe();
		}

		return cachedRecipe;
	}

	public boolean hasCraftingLimit() {
		return this.getCrafingLimit() > 0;
	}
}