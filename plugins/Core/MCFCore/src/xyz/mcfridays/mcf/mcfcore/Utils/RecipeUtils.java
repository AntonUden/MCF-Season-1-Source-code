package xyz.mcfridays.mcf.mcfcore.Utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

public class RecipeUtils {
	public static boolean compareRecipe(Recipe r1, Recipe r2) {
		if (r1.getResult().isSimilar(r2.getResult())) {
			if (r1 instanceof ShapedRecipe) {
				if (r2 instanceof ShapedRecipe) {
					ShapedRecipe sr1 = (ShapedRecipe) r1;
					ShapedRecipe sr2 = (ShapedRecipe) r2;
					if (sr1.getShape().length == sr2.getShape().length) {
						for (int i = 0; i < sr1.getShape().length; i++) {
							if (sr1.getShape()[1].length() != sr2.getShape()[1].length()) {
								System.out.println("line " + i + " |" + sr1.getShape()[1] + "|" + sr2.getShape()[i] + "|");
								return false;
							}

							for (int j = 0; j < sr1.getShape()[i].length(); j++) {
								char r1c = sr1.getShape()[i].charAt(j);
								char r2c = sr2.getShape()[i].charAt(j);

								ItemStack i1 = null;
								ItemStack i2 = null;

								if (sr1.getIngredientMap().containsKey(r1c)) {
									i1 = sr1.getIngredientMap().get(r1c);
								}

								if (sr2.getIngredientMap().containsKey(r2c)) {
									i2 = sr2.getIngredientMap().get(r2c);
								}

								// System.out.println("line " + (i + 1) + " compare " + sr1.getShape()[i] + " to
								// " + sr2.getShape()[i] + " c1: " + r1c + " c: " + r2c);
								// System.out.println(i1 + " " + i2);

								if ((i1 == null) != (i2 == null)) {
									return false;
								}

								if (i1 != null && i2 != null) {
									if (!i1.isSimilar(i2)) {
										return false;
									}
								}
							}
						}
						return true;
					}
				}
			}
		}
		return false;
	}
}