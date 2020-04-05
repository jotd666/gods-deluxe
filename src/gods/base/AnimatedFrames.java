package gods.base;



import java.awt.Graphics2D;

public class AnimatedFrames extends NamedLocatable implements Renderable
{
	private int m_refresh_rate;
	private int m_time_counter = 0;
	private boolean m_back_and_forth;
	private int m_increment;
	protected GfxFrameSet m_source;
	private boolean m_done = false;
	private Type m_type;
	private Object m_user_data = null;

	protected int m_frame_counter = 0;

	public enum Type { ONCE, BACK_AND_FORTH, FOREVER, FOREVER_REVERSE, REVERSE, CUSTOM }
	
	public void init(GfxFrameSet source,int refresh_rate, Type t, Object user_data)
	{
		init(source,refresh_rate,t);
		
		m_user_data = user_data;
	}
	



	// change data on the fly
	
	public void init(GfxFrameSet source,int refresh_rate, Type t)
	{
		if (m_source != source)
		{
			m_frame_counter = 0;
		}
		
		m_source = source;
		m_refresh_rate = refresh_rate;
		m_type = t;
		m_back_and_forth = (t == Type.BACK_AND_FORTH);
		
		m_done = false;
	
		switch (t)
		{
		case FOREVER_REVERSE:
		case REVERSE:
			m_increment = -1;
			m_frame_counter = m_source.get_nb_frames() - 1;
			break;
		case CUSTOM:
			m_increment = 0;
			break;
		default:
			m_increment = 1;
		break;
		}
		
		m_width = m_source.get_width();
		m_height = m_source.get_height();
		
	}
	
	public void play()
	{
		m_done = false;
	}
	
	public boolean is_done()
	{
		return m_done;
	}
	
	
	public void set_name(String s)
	{
	}
	public String get_name()
	{
		return m_source.get_name();
	}
	public boolean is_named()
	{
		return m_source.is_named();
	}
	public int next_frame(int increment)
	{
		int new_increment = increment;
		
		m_frame_counter += increment;
		
		if (m_frame_counter == m_source.get_nb_frames())
		{
			if (m_back_and_forth)
			{
				m_frame_counter--;
				new_increment = -1;
			}
			else
			{
				m_frame_counter = 0;
				
				if (m_type == Type.ONCE)
				{
					m_done = true; 
				}
			}
		}
		else if (m_frame_counter < 0)
		{
			m_frame_counter = 0;
			
			if (m_back_and_forth)
			{
				new_increment = 1;
			}
			else
			{
				if (m_type == Type.REVERSE) 
				{
					m_done = true;
				}
				else if (m_type == Type.FOREVER_REVERSE)
				{
					m_frame_counter = m_source.get_nb_frames() - 1;
				}
			}
		}		
		
		// update width and height in case it differs from
		// one frame to another
		
		GfxFrame current = m_source.get_frame(m_frame_counter + 1);
		
		m_width = current.get_width();
		m_height = current.get_height();

		return new_increment;
	}
	public void update(long elapsed_time)
	{
		if (m_increment != 0)
		{
			m_time_counter += elapsed_time;
			while (m_time_counter > m_refresh_rate)
			{
				m_time_counter -= m_refresh_rate;

				m_increment = next_frame(m_increment);
			}
		}
	}
	
	public void render(Graphics2D g)
	{
		g.drawImage(m_source.get_frame(m_frame_counter+1).toImage(), (int)m_x, (int)m_y, null);
	}

	public Object get_user_data() 
	{
		return m_user_data;
	}
	
}
