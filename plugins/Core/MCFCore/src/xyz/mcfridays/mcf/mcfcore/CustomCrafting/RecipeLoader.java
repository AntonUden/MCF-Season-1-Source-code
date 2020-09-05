package xyz.mcfridays.mcf.mcfcore.CustomCrafting;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.bukkit.Bukkit;

@Deprecated
public class RecipeLoader {
	@Deprecated
	private static HashMap<String, CustomRecipe> recipes = new HashMap<String, CustomRecipe>();

	@Deprecated
	public static HashMap<String, CustomRecipe> getRecipes() {
		return recipes;
	}

	@Deprecated
	public static boolean addRecipe(Class<? extends CustomRecipe> clazz) {
		try {
			CustomRecipe recipe = (CustomRecipe) clazz.getConstructor().newInstance(new Object[] {});
			return RecipeLoader.addRecipe(recipe);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}

		return false;
	}

	@Deprecated
	public static boolean addRecipe(CustomRecipe recipe) {
		if (recipes.containsKey(recipe.getClass().getName())) {
			return false;
		}

		Bukkit.getServer().addRecipe(recipe.getRecipe());

		return true;
	}

	@Deprecated
	public static boolean isAdded(CustomRecipe recipe) {
		return RecipeLoader.isAdded(recipe.getClass());
	}

	@Deprecated
	public static boolean isAdded(Class<? extends CustomRecipe> clazz) {
		return recipes.containsKey(clazz.getName());
	}

	@Deprecated
	public static void reset() {
		Bukkit.getServer().resetRecipes();
	}
}