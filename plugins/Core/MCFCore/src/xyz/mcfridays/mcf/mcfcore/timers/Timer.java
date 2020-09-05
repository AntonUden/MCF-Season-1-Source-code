package xyz.mcfridays.mcf.mcfcore.timers;

import xyz.mcfridays.mcf.mcfcommons.utils.Callback;

public interface Timer {
	public boolean start();

	public boolean stop();

	public Timer setCallback(Callback callback);

	public Callback getCallback();

	public boolean hasCallback();

	public boolean isFinished();
}