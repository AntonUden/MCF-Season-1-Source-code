package xyz.mcfridays.mcf.mcfcore.Utils;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.darkblade12.particledemo.particle.ParticleEffect;

public enum ColoredParticle {

	SPELL_MOB("SPELL_MOB"), SPELL_MOB_AMBIENT("SPELL_MOB_AMBIENT"), REDSTONE("REDSTONE");

	private ColoredParticle(String name) {
		this.name = name;
	}

	String name;

	public void send(Location location, List<Player> players, int r, int g, int b) {
		ParticleEffect.valueOf(name).display(r / 255, g / 255, b / 255, 1, 0, location, players);
	}

	public void send(Location location, int Distance, int r, int g, int b) {
		ParticleEffect.valueOf(name).display(r / 255, g / 255, b / 255, 1, 0, location, Distance);
	}
}