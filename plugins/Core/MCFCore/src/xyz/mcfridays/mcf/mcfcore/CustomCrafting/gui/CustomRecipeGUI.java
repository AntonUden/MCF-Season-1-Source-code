package xyz.mcfridays.mcf.mcfcore.CustomCrafting.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.CustomCrafting.CustomRecipe;
import xyz.mcfridays.mcf.mcfcore.GUI.GUIClickCallback;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class CustomRecipeGUI implements Listener {
	private ArrayList<String> recipes;

	private HashMap<UUID, Integer> lastPage;

	public CustomRecipeGUI() {
		this.recipes = new ArrayList<String>();
		this.lastPage = new HashMap<UUID, Integer>();
	}

	public void update() {
		recipes.clear();
		for (String name : MCFCore.getInstance().getCustomCraftingManager().getRecipes().keySet()) {
			CustomRecipe customRecipe = MCFCore.getInstance().getCustomCraftingManager().getRecipes().get(name);

			if (customRecipe.showInGUI()) {
				recipes.add(name);
			}
		}
	}

	public boolean show(Player player) {
		int page = 1;

		if (lastPage.containsKey(player.getUniqueId())) {
			page = lastPage.get(player.getUniqueId());
		}

		return this.show(player, page);
	}

	public boolean show(Player player, int page) {
		if (recipes.size() == 0) {
			return false;
		}

		if (page < 1 || page > recipes.size()) {
			page = 1;
		}

		lastPage.put(player.getUniqueId(), page);

		String recipeClassName = recipes.get(page - 1);

		CustomRecipe customRecipe = MCFCore.getInstance().getCustomCraftingManager().getRecipe(recipeClassName);

		if (customRecipe == null) {
			System.err.println("CustomRecipeGUI Error: recipe is null at page " + page + " recipe class name: " + recipeClassName);
		}

		RecipeGUIHolder holder = new RecipeGUIHolder(player.getUniqueId(), page);
		Inventory inventory = Bukkit.createInventory(holder, 27, "Page " + page + " : " + customRecipe.getName());

		for (int i = 0; i < inventory.getSize(); i++) {
			inventory.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE).setName(" ").build());
		}

		ItemBuilder previousPageBuilder = new ItemBuilder(Material.PAPER).setName((hasPreviousPage(page) ? ChatColor.GREEN : ChatColor.RED) + "Previous page");
		if (hasPreviousPage(page)) {
			previousPageBuilder.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
			previousPageBuilder.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
		inventory.setItem(0, previousPageBuilder.build());

		ItemBuilder nextPageBuilder = new ItemBuilder(Material.PAPER).setName((hasNextPage(page) ? ChatColor.GREEN : ChatColor.RED) + "Next page");
		if (hasNextPage(page)) {
			nextPageBuilder.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
			nextPageBuilder.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
		inventory.setItem(8, nextPageBuilder.build());

		holder.addClickCallback(0, new GUIClickCallback() {
			@Override
			public void onClick(HumanEntity whoClicked, Inventory clickedInventory, int slot, InventoryAction action) {
				RecipeGUIHolder holderReal = (RecipeGUIHolder) holder;
				if (holderReal.hasPreviousPage()) {
					holder.openPreviousPage();
				}
			}
		});

		holder.addClickCallback(8, new GUIClickCallback() {
			@Override
			public void onClick(HumanEntity whoClicked, Inventory clickedInventory, int slot, InventoryAction action) {
				RecipeGUIHolder holderReal = (RecipeGUIHolder) holder;
				if (holderReal.hasNextPage()) {
					holder.openNextPage();
				}
			}
		});

		if (customRecipe.isShaped()) {
			ShapedRecipe recipe = (ShapedRecipe) customRecipe.getCachedRecipe();

			int start = 2;

			int line = 0;
			for (int i = 0; i < recipe.getShape().length; i++) {
				String row = recipe.getShape()[i];
				line++;

				for (int j = 0; j < row.length(); j++) {
					char itemChar = row.charAt(j);

					int slot = start + j + ((line - 1) * 9);

					ItemStack item = recipe.getIngredientMap().get(itemChar);

					inventory.setItem(slot, item);
				}
			}
		}

		ItemStack result = customRecipe.getCachedRecipe().getResult();

		if (customRecipe.hasCraftingLimit()) {
			int limit = customRecipe.getCrafingLimit();
			result = new ItemBuilder(result, false).addLore("").addLore(ChatColor.RESET + "" + ChatColor.RED + "You can only craft this item " + limit + " time" + (limit == 1 ? "" : "s")).build();
		}

		inventory.setItem(15, result);

		player.openInventory(inventory);

		return true;
	}

	public boolean hasPreviousPage(int page) {
		return page > 1;
	}

	public boolean hasNextPage(int page) {
		return page < recipes.size();
	}
}