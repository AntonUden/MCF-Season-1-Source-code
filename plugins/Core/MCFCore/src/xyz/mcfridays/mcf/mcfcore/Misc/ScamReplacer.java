package xyz.mcfridays.mcf.mcfcore.Misc;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ScamReplacer implements Listener {
	private ArrayList<String> replaceWith;

	public ScamReplacer() {
		this.replaceWith = new ArrayList<String>();
		replaceWith.add("Im getting slightly angry over a sandbox video game that was created by Markus \"Notch\" Persson in the Java programming language and was released as a public alpha for personal computers in 2009 before officially releasing in November 2011, with Jens Bergensten taking over development around then. It has since been ported to various platforms and is the best-selling video game of all time, with 200 million copies sold across all platforms and 126 million monthly active users as of 2020");
		replaceWith.add("This server is better than anime");
		replaceWith.add("I am a little upset");
		replaceWith.add("This server is the best");
		replaceWith.add("Im getting angry over a game that is all about mining stuff to craft with and crafting stuff to mine with");
	}

	public ArrayList<String> getReplaceWith() {
		return replaceWith;
	}

	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
		if (e.getMessage().replace(" ", "").replace(".", "").toLowerCase().contains("scam")) {
			Random random = new Random();
			e.setMessage(replaceWith.get(random.nextInt(replaceWith.size())));
		}
	}
}
