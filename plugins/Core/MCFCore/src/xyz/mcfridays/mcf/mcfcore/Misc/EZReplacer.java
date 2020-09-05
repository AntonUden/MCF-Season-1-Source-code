package xyz.mcfridays.mcf.mcfcore.Misc;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class EZReplacer implements Listener {
	private ArrayList<String> replace;
	private ArrayList<String> replaceWith;

	public EZReplacer() {
		this.replace = new ArrayList<String>();
		this.replaceWith = new ArrayList<String>();

		replace.add("easy");
		replace.add("e.z");
		replace.add("ez");
		replace.add("eazy");
		replace.add("e a s y");
		replace.add("gg ez");
		replace.add("gg easy");
		replace.add("esy");
		replace.add("essy");
		replace.add("ezy");
		replace.add("e z");

		replaceWith.add("r/roastme");
		replaceWith.add("Lasagna is just spaghetti flavored cake");
		replaceWith.add("Surgery is just stabbing someone to life");
		replaceWith.add("Boomerangs are just single player Frisbee");
		replaceWith.add("Ketchup is a liquid made from a fruit and contains 20% sugar, therefore it is a sports drink");
		replaceWith.add("If an ant is at sky scraper height they probably think all the people look like ants");
		replaceWith.add("Friends are like potatoes. If you eat them, they die!");
		replaceWith.add(".hclip 5");
		replaceWith.add(".hclip 1");
		replaceWith.add("The chances of getting killed by a cow are low but never zero");
		replaceWith.add("life is soup, i am fork");
	}

	public ArrayList<String> getReplace() {
		return replace;
	}

	public ArrayList<String> getReplaceWith() {
		return replaceWith;
	}

	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
		for (String text : replace) {
			if (e.getMessage().equalsIgnoreCase(text)) {
				Random random = new Random();
				e.setMessage(replaceWith.get(random.nextInt(replaceWith.size())));
			}
		}
	}
}