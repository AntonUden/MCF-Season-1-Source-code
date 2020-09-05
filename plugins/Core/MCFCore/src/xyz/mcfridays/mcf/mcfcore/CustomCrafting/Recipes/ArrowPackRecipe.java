package xyz.mcfridays.mcf.mcfcore.CustomCrafting.Recipes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import xyz.mcfridays.mcf.mcfcore.CustomCrafting.CustomRecipe;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class ArrowPackRecipe extends CustomRecipe {
	@Override
	public Recipe getRecipe() {
		ItemStack item = new ItemBuilder(Material.ARROW).setAmount(20).build();
		ShapedRecipe recipe = new ShapedRecipe(item);

		recipe.shape("AAA", "BBB", "CCC");
		recipe.setIngredient('A', Material.FLINT);
		recipe.setIngredient('B', Material.STICK);
		recipe.setIngredient('C', Material.FEATHER);

		return recipe;
	}

	@Override
	public String getName() {
		return "Arrow pack";
	}

	@Override
	public int getCrafingLimit() {
		return 3;
	}
}