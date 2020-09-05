package xyz.mcfridays.mcf.mcfcore.Utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonBookReader {
	public static ItemStack getBook(JSONObject json) {
		ItemStack item = new ItemStack(Material.WRITTEN_BOOK);

		BookMeta meta = (BookMeta) item.getItemMeta();

		JSONArray pagesJson = json.getJSONArray("pages");
		String[] pages = new String[pagesJson.length()];
		for (int i = 0; i < pagesJson.length(); i++) {
			JSONArray page = pagesJson.getJSONArray(i);

			String pageContent = "";

			for (int j = 0; j < page.length(); j++) {
				String content = page.getString(j);

				pageContent += content + "\n";
			}

			pages[i] = pageContent;
		}

		meta.addPage(pages);
		meta.setAuthor(json.getString("author"));
		meta.setTitle(json.getString("title"));

		item.setItemMeta(meta);

		return item;
	}

	public static ItemStack getBook(File file) throws IOException {
		String data = FileUtils.readFileToString(file, "UTF-8");

		JSONObject jsonData = new JSONObject(data);

		return JsonBookReader.getBook(jsonData);
	}
}