package gods.sys;

public class WavLoop extends WavSound
{	
	public WavLoop(String file_prefix)
	{
		super(file_prefix);
	}
	
	public void pause()
	{
		current.pause();	
	}
	
	public void play()
	{	
		final WavDataPlayer actual = current;
		if (actual.data != null) {
			if (actual.runnable()) {
				// The data line is free to play
				actual.start(true);
			}
			else {
				// Reuse the running line
				actual.loop();
			}
		}
		else {
			System.err.println("error: no loop to play - " + actual.name);
		}
	}
}
