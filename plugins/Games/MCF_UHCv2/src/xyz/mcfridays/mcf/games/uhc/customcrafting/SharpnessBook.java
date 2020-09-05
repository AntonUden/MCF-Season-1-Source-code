package xyz.mcfridays.mcf.games.uhc.customcrafting;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import xyz.mcfridays.mcf.mcfcore.CustomCrafting.CustomRecipe;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class SharpnessBook extends CustomRecipe {
	@Override
	public Recipe getRecipe() {
		ItemStack item = new ItemBuilder(Material.ENCHANTED_BOOK).addEnchant(Enchantment.DAMAGE_ALL, 1).build();
		ShapedRecipe recipe = new ShapedRecipe(item);

		recipe.shape("A  ", " BB", " BC");
		recipe.setIngredient('A', Material.FLINT);
		recipe.setIngredient('B', Material.PAPER);
		recipe.setIngredient('C', Material.IRON_SWORD);

		return recipe;
	}

	@Override
	public String getName() {
		return "Protection book";
	}

	@Override
	public int getCrafingLimit() {
		return 3;
	}
}