package xyz.mcfridays.mcf.mcfcore.GUI;

import javax.annotation.Nullable;

public interface CloseCallbackHolder {
	/**
	 * Get close callback of GUI
	 * 
	 * @return {@link GUICloseCallback} or <code>null</code> if there is no callback
	 */
	@Nullable
	public GUICloseCallback getCloseCallback();
}
