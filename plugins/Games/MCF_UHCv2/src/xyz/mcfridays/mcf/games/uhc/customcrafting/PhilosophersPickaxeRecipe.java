package xyz.mcfridays.mcf.games.uhc.customcrafting;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import xyz.mcfridays.mcf.mcfcore.CustomCrafting.CustomRecipe;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class PhilosophersPickaxeRecipe extends CustomRecipe {

	@Override
	public Recipe getRecipe() {
		ItemStack item = new ItemBuilder(Material.DIAMOND_PICKAXE).setAmount(1).addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 2).setDurability((short) 1558).setName(ChatColor.GREEN + "Philosopher's Pickaxe").build();
		ShapedRecipe recipe = new ShapedRecipe(item);

		recipe.shape("ABA", "CDC", " D ");

		recipe.setIngredient('A', Material.IRON_ORE);
		recipe.setIngredient('B', Material.GOLD_ORE);
		recipe.setIngredient('C', Material.LAPIS_BLOCK);
		recipe.setIngredient('D', Material.STICK);

		return recipe;
	}

	@Override
	public String getName() {
		return "Philosopher's Pickaxe";
	}

	@Override
	public int getCrafingLimit() {
		return 1;
	}
}