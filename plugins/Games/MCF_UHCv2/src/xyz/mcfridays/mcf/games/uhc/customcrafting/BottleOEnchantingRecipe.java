package xyz.mcfridays.mcf.games.uhc.customcrafting;

import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import xyz.mcfridays.mcf.mcfcore.CustomCrafting.CustomRecipe;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class BottleOEnchantingRecipe extends CustomRecipe {

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(new ItemBuilder(Material.EXP_BOTTLE).setAmount(3).build());

		recipe.shape(" A ", "ABA", " A ");

		recipe.setIngredient('A', Material.REDSTONE_BLOCK);
		recipe.setIngredient('B', Material.GLASS_BOTTLE);

		return recipe;
	}

	@Override
	public String getName() {
		return "Bottle o enchanting";
	}
}