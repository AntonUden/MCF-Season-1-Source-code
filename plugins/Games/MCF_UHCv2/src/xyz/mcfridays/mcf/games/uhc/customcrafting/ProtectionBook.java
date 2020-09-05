package xyz.mcfridays.mcf.games.uhc.customcrafting;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import xyz.mcfridays.mcf.mcfcore.CustomCrafting.CustomRecipe;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class ProtectionBook extends CustomRecipe {
	@Override
	public Recipe getRecipe() {
		ItemStack item = new ItemBuilder(Material.ENCHANTED_BOOK).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build();
		ShapedRecipe recipe = new ShapedRecipe(item);

		recipe.shape("   ", " AA", " AB");
		recipe.setIngredient('A', Material.PAPER);
		recipe.setIngredient('B', Material.IRON_INGOT);

		return recipe;
	}

	@Override
	public String getName() {
		return "Sharpness book";
	}

	@Override
	public int getCrafingLimit() {
		return 3;
	}
}