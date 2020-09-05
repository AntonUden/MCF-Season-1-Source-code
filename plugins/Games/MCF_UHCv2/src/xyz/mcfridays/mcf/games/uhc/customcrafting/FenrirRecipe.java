package xyz.mcfridays.mcf.games.uhc.customcrafting;

import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import xyz.mcfridays.mcf.games.uhc.customitems.FenrirItem;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.CustomCrafting.CustomRecipe;

public class FenrirRecipe extends CustomRecipe {

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(MCFCore.getInstance().getCustomItemManager().getItem(FenrirItem.class));

		recipe.shape("AAA", "BCB", "AAA");

		recipe.setIngredient('A', Material.LEATHER);
		recipe.setIngredient('B', Material.BONE);
		recipe.setIngredient('C', Material.EXP_BOTTLE);

		return recipe;
	}

	@Override
	public String getName() {
		return "Fenrir";
	}

	@Override
	public int getCrafingLimit() {
		return 3;
	}
}