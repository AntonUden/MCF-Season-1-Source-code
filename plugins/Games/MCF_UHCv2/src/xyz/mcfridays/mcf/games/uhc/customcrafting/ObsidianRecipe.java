package xyz.mcfridays.mcf.games.uhc.customcrafting;

import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import xyz.mcfridays.mcf.mcfcore.CustomCrafting.CustomRecipe;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class ObsidianRecipe extends CustomRecipe {
	@Override
	public Recipe getRecipe() {
		ItemBuilder builder = new ItemBuilder(Material.OBSIDIAN);

		ShapedRecipe recipe = new ShapedRecipe(builder.build());

		recipe.shape(" A ", " B ", "   ");
		recipe.setIngredient('A', Material.WATER_BUCKET);
		recipe.setIngredient('B', Material.LAVA_BUCKET);

		return recipe;
	}

	@Override
	public String getName() {
		return "Obsidian";
	}
}