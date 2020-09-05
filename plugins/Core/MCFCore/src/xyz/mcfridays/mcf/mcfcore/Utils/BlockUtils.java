package xyz.mcfridays.mcf.mcfcore.Utils;

public class BlockUtils {
	public static double blockCenter(int block) {
		if (block >= 0) {
			return ((double) block) + 0.5;
		}
		if (block < 0) {
			return ((double) block) - 0.5;
		}
		return block;
	}
}