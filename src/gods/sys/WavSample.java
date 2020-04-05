package gods.sys;


public class WavSample extends WavSound
{
	private volatile boolean m_stop_flag = false;
	
	public WavSample(String file_prefix, double volume)
	{
		super(file_prefix,volume);
	}
	public WavSample(String file_prefix)
	{
		super(file_prefix);
	}

	public void stop()
	{
		m_stop_flag = true;
	}
	public void run()
	{
		if (current.data != null)
		{
		Thread this_thread = Thread.currentThread();
		int frame_size = get_sdl().getFormat().getFrameSize();
		
		float split_seconds = 0.1f;
		long split_wait = (long)(900 * split_seconds); // 900 = 90% of the time WTF does that mean???
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
			get_sdl().start();

			while (play_thread == this_thread)
			{	
				if (m_stop_flag)
				{
					set_sample_position(0);
					get_sdl().flush();
				}
				
				
				synchronized(this)
				{
					wait();
					if (!m_stop_flag)
					{
						get_sdl().flush();
					}
					//m_stop_flag = false;
				}
				if (play_thread != null)
				{
					while (get_sample_position() < get_max_sample_position())
					{
				
						int sp = get_sample_position();
						set_sample_position(sp + write_block);
						
						// other thread can call set_sample_position(0) now
						
						if (m_stop_flag)
						{
							set_sample_position(0);
							get_sdl().flush();
							break;
						}
						write(current.data,sp,Math.min(write_block,current.data.length - sp));

						if ((!m_stop_flag)&& (split_wait < 20))
						{
							Thread.sleep(split_wait);
						}
					}
					m_stop_flag = false;
				}
				
			}
		}
		catch (Exception ex)
		{

		}
		close();
		}
	}


	
		/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		WavSound sp = new WavSample("mine");
		sp.play();

		Thread.sleep(4000);
	}	
	
	}
