package xyz.mcfridays.mcf.games.uhc.customitems;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import com.xxmicloxx.NoteBlockAPI.songplayer.PositionSongPlayer;

import xyz.mcfridays.mcf.games.uhc.MCFUHC;
import xyz.mcfridays.mcf.mcfcore.MCFCore;
import xyz.mcfridays.mcf.mcfcore.Game.CustomItems.CustomItem;
import xyz.mcfridays.mcf.mcfcore.Game.CustomItems.CustomItemManager;
import xyz.mcfridays.mcf.mcfcore.Utils.ItemBuilder;

public class UnknownRecordItem extends CustomItem implements Listener {
	private ArrayList<PositionSongPlayer> songPlayers;

	public UnknownRecordItem() {
		File file = new File(MCFUHC.getInstance().getDataFolder().getAbsolutePath() + "/unknownrecord.nbs");
		if (file.exists()) {
			MCFCore.getInstance().getNbsMusicManager().loadSong(file, "kgpxCWF6s8ecY4eg");
		} else {
			Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Warning]: UnknownRecordItem cant find nbs file");
		}

		songPlayers = new ArrayList<PositionSongPlayer>();
	}

	@Override
	public ItemStack getItem(Player player) {
		return new ItemBuilder(Material.RECORD_4).setName(ChatColor.DARK_RED + "Unknown record").build();
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
			e.setCancelled(true);

			Player p = e.getPlayer();

			if (CustomItemManager.isType(p.getItemInHand(), this)) {
				p.setItemInHand(null);
			} else {
				return;
			}

			if (MCFCore.getInstance().getNbsMusicManager().hasSong("kgpxCWF6s8ecY4eg")) {
				PositionSongPlayer psp = new PositionSongPlayer(MCFCore.getInstance().getNbsMusicManager().getSong("kgpxCWF6s8ecY4eg"));
				for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
					psp.addPlayer(pl);
				}

				psp.setTargetLocation(p.getLocation());
				psp.setDistance(20);
				psp.setPlaying(true);

				songPlayers.add(psp);
			}
		}
	}

	@Override
	public void onAdded() {
		ShapedRecipe recipe = new ShapedRecipe(MCFCore.getInstance().getCustomItemManager().getItem(this.getClass()));

		recipe.shape(" A ", "ABA", " A ");

		recipe.setIngredient('A', Material.COAL_BLOCK);
		recipe.setIngredient('B', Material.REDSTONE);

		Bukkit.getServer().addRecipe(recipe);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		for (PositionSongPlayer sp : songPlayers) {
			if (sp.isPlaying()) {
				sp.addPlayer(p);
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerQuitEvent e) {
		Player p = e.getPlayer();

		for (PositionSongPlayer sp : songPlayers) {
			if (sp.getPlayerUUIDs().contains(p.getUniqueId())) {
				sp.removePlayer(p);
			}
		}
	}
}