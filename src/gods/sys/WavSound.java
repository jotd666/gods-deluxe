package gods.sys;

import javax.sound.sampled.*;

import java.io.File;

public abstract class WavSound implements Runnable
{
	protected class Data
	{
		public byte [] data = null;
		public double sound_length;
		public volatile int sample_position;
		
		Data(String file_prefix)
		{
			try
			{

				File fileIn = new File(file_prefix);
				if (!fileIn.exists())
				{ System.err.println("error: file does not exist: "+fileIn); }
				else
				{
				AudioInputStream audioInputStream = 
					AudioSystem.getAudioInputStream(fileIn);
				long frameLength = audioInputStream.getFrameLength();
				AudioFormat audioFormat = audioInputStream.getFormat();
				
				// source data line is the same for all samples of the group
				
				if (sdl == null)
				{
					sdl = AudioSystem.getSourceDataLine(audioFormat);
				}
				// in a .wav file, this is the length 
				// of the audio data in bytes

				data = new byte[(int)frameLength * audioFormat.getFrameSize()];
				sdl.open(audioFormat,data.length);

				audioInputStream.read(data);


				frame_size = sdl.getFormat().getFrameSize();
				
				sound_length = (audioInputStream.getFrameLength() / audioFormat.getSampleRate());
				}
			}
			catch (Exception ex)
			{
				data = null;
			}	
		}
	}
	
	protected Data [] sound_array;
	protected Data current = null;
	
	protected volatile Thread play_thread;
	private SourceDataLine sdl = null;
	private int frame_size;

	protected SourceDataLine get_sdl()
	{
		return sdl;
	}
	
	public int get_max_sample_position()
	{
		return current.data.length;
	}
	synchronized int get_sample_position()
	{
		return current.sample_position;
	}
	
	synchronized void set_sample_position(int sp)
	{
		if (current.data != null)
		{
			// round it
			current.sample_position = (sp / frame_size) * frame_size;
		}
	}
	
	public WavSound(String file_prefix)
	{
		this(file_prefix,1.0);
	}
	public WavSound(String file_prefix, double volume)
	{
		File dir_tried = new File(file_prefix);
		if (dir_tried.isDirectory())
		{
			String [] files = dir_tried.list();
			
			if (files.length > 0)
			{
				sound_array = new Data[files.length];

				for (int i = 0; i < sound_array.length; i++)
				{
					sound_array[i] = new Data(dir_tried.getPath() + File.separator + files[i]);
				}
				
				current = sound_array[0];
			}
			
		}
		else
		{
			sound_array = new Data[1];
			sound_array[0] = new Data(file_prefix+".wav");
			current = sound_array[0];
		}
	
		play_thread = new Thread(this);

		play_thread.start();

		Thread.yield();

	}
	

	
	public void end()
	{
		play_thread = null;
		for (int i = 0; i < sound_array.length; i++)
		{
			sound_array[i] = null;
		}
		sound_array = null;
		current.data = null;
		
		synchronized(this)
		{
			notify();
		}
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
		if (current.data != null)
		{
			set_sample_position(0);

			synchronized(this)
			{
				notify();
			}
		}
	}
	
	public void play_offset(int index)
	{	
		set_sample_position(index);
		
		synchronized(this)
		{
			notify();
		}
	}
	
	protected synchronized void close()
	{
		if (sdl != null)
		{
			sdl.stop();
			sdl.flush();
			sdl.close();
			sdl = null;
		}		
	}
	protected void write(byte [] data, int offset, int len)
	{
		sdl.write(data,offset,len);
	}

}