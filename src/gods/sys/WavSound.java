package gods.sys;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public abstract class WavSound
{
	protected WavDataPlayer[] sound_array;
	protected WavDataPlayer current = null;
	
	public WavSound(String file_prefix)
	{
		File dir_tried = new File(file_prefix);
		
		if (dir_tried.isDirectory())
		{
			List<CompletableFuture<WavDataPlayer>> files = new ArrayList<>();
			try {
				// Load folder files in parallel
				files = Files.list(dir_tried.toPath())
					.filter(f -> f.toFile().getName().endsWith(".wav"))
					.sorted()
					.map(f -> CompletableFuture.supplyAsync(() -> new WavDataPlayer(f.toFile())))
					.collect(Collectors.toList());
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
			if (files.size() > 0)
			{
				sound_array = new WavDataPlayer[files.size()];
				for (int i = 0; i < sound_array.length; i++)
				{	
					try {
						sound_array[i] = files.get(i).get();
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				}
				
				current = sound_array[0];
			}
			
		}
		else
		{
			sound_array = new WavDataPlayer[]{ new WavDataPlayer(new File(file_prefix+".wav")) };
			current = sound_array[0];
		}
	}
	
	public int get_max_sample_position()
	{
		return current.data.length;
	}
	
	public void set_sample_position(int sp)
	{
		current.seek(sp);
	}
	
	public void end()
	{
		for (int i = 0; i < sound_array.length; i++)
		{
			sound_array[i] = null;
		}
		sound_array = null;
		current.data = null;
	}
	
	
	public void play(int index)
	{
		if (index < sound_array.length)
		{
			current = sound_array[index];
			play();
		}
	}
	
	public void play_random()
	{
		int index = (int)(Math.random() * sound_array.length);
		
		play(index);
	}
	
	public void play()
	{	
		final WavDataPlayer actual = current;
		if (actual.data != null) {
			if (actual.runnable()) {
				// The data line is free to play
				actual.start(false);
			}
			else {
				// Reuse the running line
				actual.rewind();
			}
		}
		else {
			System.err.println("error: no clip to play - " + actual.name);
		}
	}

	public void stop()
	{
		for (int i = 0; i < sound_array.length; i++) {
			WavDataPlayer actual = sound_array[i];
			actual.stop();
		}
	}

	protected void close()
	{
		for (int i = 0; i < sound_array.length; i++) {
			WavDataPlayer actual = sound_array[i];
			actual.close();
		}
	}
}