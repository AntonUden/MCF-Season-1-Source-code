package xyz.mcfridays.mcf.games.uhc.customcrafting;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import xyz.mcfridays.mcf.mcfcore.CustomCrafting.CustomRecipe;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class VorpalSword extends CustomRecipe {
	@Override
	public Recipe getRecipe() {
		ItemStack item = new ItemBuilder(Material.IRON_SWORD).addEnchant(Enchantment.DAMAGE_UNDEAD, 2).addEnchant(Enchantment.DAMAGE_ARTHROPODS, 2).addEnchant(Enchantment.LOOT_BONUS_MOBS, 1).setName(ChatColor.GREEN + "Vorpal Sword").build();
		ShapedRecipe recipe = new ShapedRecipe(item);

		recipe.shape(" A ", " B ", " C ");
		recipe.setIngredient('A', Material.BONE);
		recipe.setIngredient('B', Material.IRON_SWORD);
		recipe.setIngredient('C', Material.ROTTEN_FLESH);

		return recipe;
	}

	@Override
	public String getName() {
		return "Vorpal sword";
	}

	@Override
	public int getCrafingLimit() {
		return 3;
	}
}