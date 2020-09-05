package xyz.mcfridays.mcf.mcfcore.CustomCrafting.Recipes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import xyz.mcfridays.mcf.mcfcore.CustomCrafting.CustomRecipe;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class GoldPackRecipe extends CustomRecipe {
	@Override
	public Recipe getRecipe() {
		ItemStack item = new ItemBuilder(Material.GOLD_INGOT).setAmount(9).build();
		ShapedRecipe recipe = new ShapedRecipe(item);

		recipe.shape("AAA", "ABA", "AAA");
		recipe.setIngredient('A', Material.GOLD_ORE);
		recipe.setIngredient('B', Material.COAL);

		return recipe;
	}

	@Override
	public String getName() {
		return "Gold pack";
	}

	@Override
	public int getCrafingLimit() {
		return 3;
	}
}