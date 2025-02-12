package gods.sys;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Executes sound tasks using a limited thread pool to avoid excessive spawn.
 */
public class SoundService
{
	static final int MAX_LINES = 8;
	static final ExecutorService executor = Executors.newFixedThreadPool(MAX_LINES);
	
	public static void execute(Runnable task)
	{
		executor.execute(task);
	}
	
	static void shutdown()
	{
		executor.shutdown();
	}
}
