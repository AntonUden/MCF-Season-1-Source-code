package xyz.mcfridays.mcf.games.uhc.customcrafting;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import xyz.mcfridays.mcf.mcfcore.CustomCrafting.CustomRecipe;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class LightAppleRecipe extends CustomRecipe {
	@Override
	public Recipe getRecipe() {
		ItemStack item = new ItemBuilder(Material.GOLDEN_APPLE).setAmount(1).build();
		ShapedRecipe recipe = new ShapedRecipe(item);

		recipe.shape(" A ", "ABA", " A ");
		recipe.setIngredient('A', Material.GOLD_INGOT);
		recipe.setIngredient('B', Material.APPLE);

		return recipe;
	}

	@Override
	public String getName() {
		return "Light apple";
	}

	@Override
	public int getCrafingLimit() {
		return 3;
	}
}