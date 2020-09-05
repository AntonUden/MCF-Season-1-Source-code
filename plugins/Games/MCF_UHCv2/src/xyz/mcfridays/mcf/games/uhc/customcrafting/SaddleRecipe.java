package xyz.mcfridays.mcf.games.uhc.customcrafting;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import xyz.mcfridays.mcf.mcfcore.CustomCrafting.CustomRecipe;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class SaddleRecipe extends CustomRecipe {
	@Override
	public Recipe getRecipe() {
		ItemStack item = new ItemBuilder(Material.SADDLE).setAmount(1).build();
		ShapedRecipe recipe = new ShapedRecipe(item);

		recipe.shape("AAA", "BAB", "C C");
		recipe.setIngredient('A', Material.LEATHER);
		recipe.setIngredient('B', Material.STRING);
		recipe.setIngredient('C', Material.IRON_INGOT);

		return recipe;
	}

	@Override
	public String getName() {
		return "Saddle";
	}
}