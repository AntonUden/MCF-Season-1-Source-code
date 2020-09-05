package xyz.mcfridays.mcf.games.uhc.customcrafting;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import xyz.mcfridays.mcf.mcfcore.CustomCrafting.CustomRecipe;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class LightAnvilRecipe extends CustomRecipe {
	@Override
	public Recipe getRecipe() {
		ItemStack item = new ItemBuilder(Material.ANVIL).setAmount(1).build();
		ShapedRecipe recipe = new ShapedRecipe(item);

		recipe.shape("AAA", " B ", "AAA");
		recipe.setIngredient('A', Material.IRON_INGOT);
		recipe.setIngredient('B', Material.IRON_BLOCK);

		return recipe;
	}

	@Override
	public String getName() {
		return "Light anvil";
	}

	@Override
	public int getCrafingLimit() {
		return 3;
	}
}