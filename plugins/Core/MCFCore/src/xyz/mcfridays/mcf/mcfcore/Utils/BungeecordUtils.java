package xyz.mcfridays.mcf.mcfcore.Utils;

import org.bukkit.entity.Player;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import xyz.mcfridays.mcf.mcfcore.MCFCore;

public class BungeecordUtils {
	public static void sendToServer(Player player, String server) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();

		out.writeUTF("Connect");
		out.writeUTF(server);

		player.sendPluginMessage(MCFCore.getInstance(), "BungeeCord", out.toByteArray());
	}
}