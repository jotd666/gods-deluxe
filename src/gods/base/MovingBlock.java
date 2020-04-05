package gods.base;

import gods.sys.WavLoop;

import java.awt.Graphics2D;
import java.awt.Rectangle;


public class MovingBlock extends NamedLocatable implements Renderable
{
	protected boolean m_moving = false;
	private int m_nb_rows = 1;
	private int m_nb_cols = 1;
	private int m_frame_width, m_frame_height;
	private WavLoop m_moving_sound;
	
	public void render(Graphics2D g) 
	{
		for (int i = 0; i < m_nb_rows; i++)
		{
			for (int j = 0; j < m_nb_cols; j++)
			{
				g.drawImage(m_frame.toImage(), get_x() + m_frame_width * j, get_y() + m_frame_height * i, null);	
			}
		}
	}

	public void update(long elapsed_time) 
	{
		if (m_moving)
		{
			m_timer += elapsed_time;
			if (m_timer >= m_move_duration)
			{
				m_x = m_end.get_x();
				m_y = m_end.get_y();
				m_moving = false;
				if (m_moving_sound != null)
				{
					m_moving_sound.pause();
				}

			}
			else
			{
				m_x = (int)(((m_end.get_x() - m_start.get_x()) * m_timer) / m_move_duration + m_start.get_x());
				m_y = (int)(((m_end.get_y() - m_start.get_y()) * m_timer) / m_move_duration + m_start.get_y());
			}
		}
	}

	public void swap()
	{
		int x_end = m_end.get_x();
		int y_end = m_end.get_y();
		
		m_start.set_coordinates(m_end);
		m_end.set_coordinates(x_end, y_end);
	}
	public ControlObject get_end()
	{
		return m_end;
	}
	
	public ControlObject get_start()
	{
		return m_start;
	}
	
	public void move()
	{
		if (!m_moving)
		{
			if (m_view_bounds.contains(m_x,m_y) || m_view_bounds.contains(m_x+m_width,m_y+m_height))
			{
				if (m_moving_sound != null)
				{
					m_moving_sound.play();
				}
			}

			m_moving = true;
			m_timer = 0;
		}
	}
	
	public void reverse()
	{
		ControlObject co = m_start;
		m_start = m_end;
		m_end = co;
		m_timer = 0;
	}
	
	public String get_name() 
	{
		return m_name;
	}

	public void set_name(String n) 
	{		
		
	}
	public boolean is_named()
	{
		return !m_name.equals("");
	}
	
	public void set_moving_sound(WavLoop moving_sound)
	{
		m_moving_sound = moving_sound;		
	}
	
	private String m_name;
	private GfxFrame m_frame;
	private ControlObject m_start, m_end;
	private long m_move_duration;
	private long m_timer = 0;
	private Rectangle m_view_bounds;
	

	public void set_move_duration(long move_duration)
	{
		m_move_duration = move_duration;
	}
	
	public void init(GfxFrame tile, String instance_name, 
			LevelData levelData, Rectangle view_bounds,long move_duration)
	{
		
		m_view_bounds = view_bounds;
				
		m_name = instance_name;
		m_frame = tile;

		String end_name = m_name.replace("_start","_end");
		
		m_start = levelData.get_control_object(m_name);
		m_end = levelData.get_control_object(end_name);

		m_move_duration = move_duration;

		m_x = m_start.get_x();
		m_y = m_start.get_y();

		m_frame_width = m_frame.get_width();
		m_frame_height = m_frame.get_height();
		
		m_nb_rows = m_start.get_height() / m_frame_height;
		m_nb_cols = m_start.get_width() / m_frame_width;
		
		m_height = m_frame_height * m_nb_rows;
		m_width  = m_frame_width * m_nb_cols;  

	}
}
