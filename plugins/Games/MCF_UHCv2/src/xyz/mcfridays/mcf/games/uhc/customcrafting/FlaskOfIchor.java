package xyz.mcfridays.mcf.games.uhc.customcrafting;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import xyz.mcfridays.mcf.mcfcore.CustomCrafting.CustomRecipe;

public class FlaskOfIchor extends CustomRecipe {
	@SuppressWarnings("deprecation")
	@Override
	public Recipe getRecipe() {
		ItemStack item = new ItemStack(Material.POTION, 1, (short) 16428);

		PotionMeta pm = (PotionMeta) item.getItemMeta();

		pm.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 0, 2), true);
		pm.setMainEffect(PotionEffectType.HARM);

		pm.setDisplayName(ChatColor.GREEN + "Flask of Ichor");

		item.setItemMeta(pm);

		ShapedRecipe recipe = new ShapedRecipe(item);

		MaterialData skull = new MaterialData(Material.SKULL_ITEM);
		skull.setData((byte) 3);

		MaterialData ink = new MaterialData(Material.INK_SACK);
		ink.setData((byte) 0);

		recipe.shape(" A ", "BCB", " D ");
		recipe.setIngredient('A', skull);
		recipe.setIngredient('B', Material.BROWN_MUSHROOM);
		recipe.setIngredient('C', Material.GLASS_BOTTLE);
		recipe.setIngredient('D', ink);

		return recipe;
	}

	@Override
	public String getName() {
		return "Flask of Ichor";
	}

	@Override
	public int getCrafingLimit() {
		return 3;
	}
}