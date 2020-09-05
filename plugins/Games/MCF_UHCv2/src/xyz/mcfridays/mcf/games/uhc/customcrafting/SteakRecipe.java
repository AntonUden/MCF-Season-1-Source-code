package xyz.mcfridays.mcf.games.uhc.customcrafting;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import xyz.mcfridays.mcf.mcfcore.CustomCrafting.CustomRecipe;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class SteakRecipe extends CustomRecipe {
	@Override
	public Recipe getRecipe() {
		ItemStack item = new ItemBuilder(Material.COOKED_BEEF).setAmount(10).build();
		ShapedRecipe recipe = new ShapedRecipe(item);

		recipe.shape("AAA", "ABA", "AAA");
		recipe.setIngredient('A', Material.RAW_BEEF);
		recipe.setIngredient('B', Material.COAL);

		return recipe;
	}

	@Override
	public String getName() {
		return "Steak";
	}

	@Override
	public int getCrafingLimit() {
		return 3;
	}
}