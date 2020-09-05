package xyz.mcfridays.mcf.mcfcore.CustomCrafting.Recipes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import xyz.mcfridays.mcf.mcfcore.CustomCrafting.CustomRecipe;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class IronPackRecipe extends CustomRecipe {
	@Override
	public Recipe getRecipe() {
		ItemStack item = new ItemBuilder(Material.IRON_INGOT).setAmount(9).build();
		ShapedRecipe recipe = new ShapedRecipe(item);

		recipe.shape("AAA", "ABA", "AAA");
		recipe.setIngredient('A', Material.IRON_ORE);
		recipe.setIngredient('B', Material.COAL);

		return recipe;
	}

	@Override
	public String getName() {
		return "Iron pack";
	}

	@Override
	public int getCrafingLimit() {
		return 3;
	}
}