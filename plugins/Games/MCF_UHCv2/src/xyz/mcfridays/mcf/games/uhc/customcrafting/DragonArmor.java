package xyz.mcfridays.mcf.games.uhc.customcrafting;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import xyz.mcfridays.mcf.mcfcore.CustomCrafting.CustomRecipe;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class DragonArmor extends CustomRecipe {

	@Override
	public Recipe getRecipe() {
		ItemBuilder bulder = new ItemBuilder(Material.DIAMOND_CHESTPLATE);
		
		bulder.setName(ChatColor.GREEN + "Dragon Armor");
		bulder.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
		
		ShapedRecipe recipe = new ShapedRecipe(bulder.build());

		recipe.shape(" A ", " B ", "CDC");

		recipe.setIngredient('A', Material.MAGMA_CREAM);
		recipe.setIngredient('B', Material.DIAMOND_CHESTPLATE);
		recipe.setIngredient('C', Material.OBSIDIAN);
		recipe.setIngredient('D', Material.ANVIL);

		return recipe;
	}

	@Override
	public String getName() {
		return "Dragon Armor";
	}

	@Override
	public int getCrafingLimit() {
		return 1;
	}
}