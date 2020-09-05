package xyz.mcfridays.mcf.mcfcore.CustomCrafting.Recipes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.CustomCrafting.CustomRecipe;
import xyz.mcfridays.mcf.mcfcore.Game.CustomItems.Items.GoldenHeadItem;

public class GoldenHeadRecipe extends CustomRecipe {
	@SuppressWarnings("deprecation")
	@Override
	public Recipe getRecipe() {
		ItemStack item = MCFCore.getInstance().getCustomItemManager().getItem(GoldenHeadItem.class);
		ShapedRecipe recipe = new ShapedRecipe(item);

		MaterialData skull = new MaterialData(Material.SKULL_ITEM);

		skull.setData((byte) 3);

		recipe.shape("AAA", "ABA", "AAA");
		recipe.setIngredient('A', Material.GOLD_INGOT);
		recipe.setIngredient('B', skull);

		return recipe;
	}

	@Override
	public String getName() {
		return "Golden head";
	}
}