package gods.game.characters.hostiles;

import gods.base.GfxFrameSet;

public class SpikeUpDownServer {
	public static final int UPDATE_RATE = 50;
	
	private int m_max_counter;
	private int m_min_height;
	private int m_max_height;
	private int m_sign;
	private int m_max_y, m_min_y;
	private int m_relative_y;
	private int m_height;
	private int m_counter;
	private int m_timer;
	
	public int get_height()
	{
		return m_height;
	}
	public int get_min_height()
	{
		return m_min_height;
	}
	
	public int get_counter()
	{
		return m_counter;
	}
	
	
	
	public int get_relative_y()
	{
		return m_relative_y;
	}
	
	public int get_max_height()
	{
		return m_max_height;
	}
	
	public void init(GfxFrameSet gfs, int y)
	{
		m_min_height = gfs.get_frame(2).get_height();
		m_max_height = gfs.get_first_frame().get_height();
		m_counter = 0;
		m_timer = 0;
		m_sign = 1;
		m_max_counter = gfs.get_nb_frames() * 2;
		m_height = m_max_height;
		
		m_relative_y = 0; // y stays fixed

	}

	public void update(long elapsed_time)
	{
		m_timer += elapsed_time;
		while (m_timer > UPDATE_RATE)
		{
			m_timer -= UPDATE_RATE;
		
		m_height -= m_sign;
		m_relative_y += m_sign;
		if ((m_height == m_min_height) || (m_height == m_max_height))
		{
			m_sign *= -1;
		}			

		m_counter++;
		if (m_counter == m_max_counter)
		{
			m_counter = 0;
		}
		}
		
	}
}
