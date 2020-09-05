package xyz.mcfridays.mcf.mcfcore.Utils;

import org.json.JSONObject;

public class WorldborderData {
	private int startSize;
	private int endSize;

	private double centerX;
	private double centerZ;

	private int shrinkStartTime;
	private int shrinkDuration;

	private int shrinkStepTime;
	
	public static WorldborderData fromJson(JSONObject json) {
		JSONObject center = json.getJSONObject("border_center");
		
		int shrinkStepTime = 30;
		
		if(json.has("shrink_step_time")) {
			shrinkStepTime = json.getInt("shrink_step_time");
		}
		
		return new WorldborderData(json.getInt("border_start_size"), json.getInt("border_end_size"), center.getDouble("x"), center.getDouble("z"), json.getInt("border_shrink_start"), json.getInt("border_shrink_duration") ,shrinkStepTime);
	}

	public WorldborderData(int startSize, int endSize, double centerX, double centerZ, int shrinkStartTime, int shrinkDuration) {
		this(startSize, endSize, centerX, centerZ, shrinkStartTime, shrinkDuration, 30);
	}
	
	public WorldborderData(int startSize, int endSize, double centerX, double centerZ, int shrinkStartTime, int shrinkDuration, int shrinkStepTime) {
		this.startSize = startSize;
		this.endSize = endSize;
		this.centerX = centerX;
		this.centerZ = centerZ;
		this.shrinkStartTime = shrinkStartTime;
		this.shrinkDuration = shrinkDuration;
		this.shrinkStepTime = shrinkStepTime;
	}

	/**
	 * @return Initial size for the world border
	 */
	public int getStartSize() {
		return startSize;
	}

	/**
	 * @return Final size of for the world border
	 */
	public int getEndSize() {
		return endSize;
	}

	/**
	 * @return Get center X of world border
	 */
	public double getCenterX() {
		return centerX;
	}

	/**
	 * @return Get center X of world border
	 */
	public double getCenterZ() {
		return centerZ;
	}

	/**
	 * @return Get time in seconds before the world border starts to shrink
	 */
	public int getShrinkStartTime() {
		return shrinkStartTime;
	}

	/**
	 * @return Get time in seconds that the world border will take to shrink
	 */
	public int getShrinkDuration() {
		return shrinkDuration;
	}
	
	public int getShrinkStepTime() {
		return shrinkStepTime;
	}
}