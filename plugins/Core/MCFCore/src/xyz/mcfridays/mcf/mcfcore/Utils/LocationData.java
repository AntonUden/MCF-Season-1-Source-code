package xyz.mcfridays.mcf.mcfcore.Utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.json.JSONObject;

public class LocationData {
	private double x;
	private double y;
	private double z;

	private float yaw;
	private float pitch;

	public LocationData(JSONObject json) {
		this.x = json.getDouble("x");
		this.y = json.getDouble("y");
		this.z = json.getDouble("z");

		if (json.has("yaw")) {
			this.yaw = json.getFloat("yaw");
		} else {
			this.yaw = 0F;
		}

		if (json.has("pitch")) {
			this.pitch = json.getFloat("pitch");
		} else {
			this.pitch = 0F;
		}
	}

	public LocationData(double x, double y, double z) {
		this(x, y, z, 0F, 0F);
	}

	public LocationData(double x, double y, double z, float yaw, float pitch) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public Location toLocation(World world) {
		return new Location(world, x, y, z, yaw, pitch);
	}

	public void center() {
		x = BlockUtils.blockCenter((int) x);
		y = BlockUtils.blockCenter((int) y);
		z = BlockUtils.blockCenter((int) z);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public float getYaw() {
		return yaw;
	}

	public float getPitch() {
		return pitch;
	}
}