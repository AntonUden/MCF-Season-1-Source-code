package xyz.mcfridays.mcf.mcfcore.timers;

import xyz.mcfridays.mcf.mcfcommons.utils.Callback;
import xyz.mcfridays.mcf.mcfcommons.utils.TimerCallback;

public interface CountdownTimer {
	public boolean start();

	public boolean stop();

	public Timer setCallback(Callback callback);

	public Callback getCallback();

	public Timer setTickCallback(TimerCallback callback);

	public TimerCallback getTimerCallback();

	public boolean hasTimerCallback();

	public boolean isFinished();
}