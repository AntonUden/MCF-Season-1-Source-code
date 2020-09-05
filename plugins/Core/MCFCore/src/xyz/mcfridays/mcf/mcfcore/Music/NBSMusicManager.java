package xyz.mcfridays.mcf.mcfcore.Music;

import java.io.File;
import java.util.HashMap;

import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;

public class NBSMusicManager {
	private HashMap<String, Song> songs;

	public NBSMusicManager() {
		songs = new HashMap<String, Song>();
	}

	public boolean loadSong(File file) {
		return this.loadSong(file, null);
	}

	public boolean loadSong(File file, String name) {
		Song song = NBSDecoder.parse(file);

		if (name == null) {
			name = song.getTitle();
		}

		return this.loadSong(song, name);
	}

	public boolean loadSong(Song song, String name) {
		if (songs.containsKey(name)) {
			return false;
		}

		songs.put(name, song);

		return true;
	}

	public boolean hasSong(String name) {
		return songs.containsKey(name);
	}

	public Song getSong(String name) {
		return songs.get(name);
	}
}