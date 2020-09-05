package xyz.mcfridays.mcf.games.uhc.customcrafting;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import xyz.mcfridays.mcf.games.uhc.customitems.AndurilItem;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.CustomCrafting.CustomRecipe;

public class AndurilRecipe extends CustomRecipe {
	@Override
	public Recipe getRecipe() {
		ItemStack item = MCFCore.getInstance().getCustomItemManager().getItem(AndurilItem.class);
		ShapedRecipe recipe = new ShapedRecipe(item);

		recipe.shape("ABA", "ABA", "ACA");
		recipe.setIngredient('A', Material.FEATHER);
		recipe.setIngredient('B', Material.IRON_BLOCK);
		recipe.setIngredient('C', Material.BLAZE_ROD);

		return recipe;
	}

	@Override
	public String getName() {
		return "Andúril";
	}

	@Override
	public int getCrafingLimit() {
		return 1;
	}
}