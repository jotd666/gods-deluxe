package gods.sys;


public class WavLoop extends WavSound
{
	private byte [] resampled = null;
	private int buffer_length = 0;
	
	
	private volatile boolean m_playing = true;
	
	/*private void handle_resampling()
	{
		if (do_resample)
		{
			do_resample = false;
			// resample the data
			
			float us_ratio = buffer_length / (float)current.data.length;
			
			// undersample
			for (int i = 0; i < current.data.length; i++)
			{
				int j = Math.round(i*us_ratio);
				resampled[j] = current.data[i];
			}
		}
			
	}*/
	
	public WavLoop(String file_prefix)
	{
		super(file_prefix);
		if (current.data != null)
		{
			buffer_length = current.data.length;

			resampled = new byte [current.data.length * 2];
			for (int i = 0; i < current.data.length; i++)
			{
				resampled[i] = current.data[i];
			}		
		}
	}

	public void pause()
	{
		synchronized(this)
		{
			m_playing = false;
			//notify();
		}
	}
	
	public void end()
	{
		synchronized(this)
		{
			m_playing = false;
			notify();
			play_thread = null;
		}
	}
	public void play()
	{
		synchronized(this)
		{
			m_playing = true;
			notify();
		}
	}
	
	public void set_resampling_ratio(float ratio)
	{
		int frame_size = get_sdl().getFormat().getFrameSize();
		int new_length = Math.round(current.data.length / (ratio * frame_size)) * frame_size;
		if (new_length > resampled.length)
		{
			new_length = resampled.length;
		}

		if (buffer_length != new_length)
		{
			buffer_length = new_length;
			//do_resample = true;
		}		
	}
	public void run()
	{
		if (current.data != null)
		{
		Thread this_thread = Thread.currentThread();
		int frame_size = get_sdl().getFormat().getFrameSize();
		//float sampling_rate = get_sdl().getFormat().getSampleRate();
		
		float split_seconds = 0.1f;
		long split_wait = (long)(900 * split_seconds); // 90% of the time
		long nb_split_bytes = Math.round(current.sound_length / split_seconds);
		
		if (nb_split_bytes == 0)
		{
			nb_split_bytes = 1;
		}

		int write_block = current.data.length / (int)nb_split_bytes;		
		
		// must be a multiple of the frame size
		
		write_block = (write_block / frame_size) * frame_size;

		try
		{			
			synchronized(this)
			{
				wait();
			}

			get_sdl().start();

			if (current.data != null)
			{
				while (play_thread == this_thread)
				{		
					set_sample_position(0);
					
					while (get_sample_position() < current.data.length)
					{
						int sp = get_sample_position();
						set_sample_position(sp + write_block);


						if (!m_playing)
						{
							synchronized(this)
							{
								get_sdl().flush();
								wait();
							}
						}
						
						if (play_thread != null)
						{
							//handle_resampling();

							write(current.data,sp,Math.min(write_block,current.data.length - sp));

							// wait 90% of the theorical playing time
							// so when the loop stops there is not a lot of buffered sound
							// to play

							if (split_wait < 40)
							{
								Thread.sleep(split_wait);
							}
						}
					}
				}	
			}

		}
		catch (Exception ex)
		{

		}
		close();
		}
	}


}
