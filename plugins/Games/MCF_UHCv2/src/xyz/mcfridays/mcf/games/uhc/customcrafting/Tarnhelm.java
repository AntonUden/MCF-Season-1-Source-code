package xyz.mcfridays.mcf.games.uhc.customcrafting;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import xyz.mcfridays.mcf.mcfcore.CustomCrafting.CustomRecipe;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class Tarnhelm extends CustomRecipe {
	@Override
	public Recipe getRecipe() {
		ItemBuilder builder = new ItemBuilder(Material.DIAMOND_HELMET);

		builder.setName(ChatColor.GREEN + "Tarnhelm");
		builder.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		builder.addEnchant(Enchantment.PROTECTION_FIRE, 1);
		builder.addEnchant(Enchantment.OXYGEN, 3);

		ShapedRecipe recipe = new ShapedRecipe(builder.build());

		recipe.shape("ABA", "ACA", "   ");
		recipe.setIngredient('A', Material.DIAMOND);
		recipe.setIngredient('B', Material.IRON_INGOT);
		recipe.setIngredient('C', Material.REDSTONE_BLOCK);

		return recipe;
	}

	@Override
	public String getName() {
		return "Tarnhelm";
	}

	@Override
	public int getCrafingLimit() {
		return 1;
	}
}