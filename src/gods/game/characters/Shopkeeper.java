package gods.game.characters;


import java.awt.Rectangle;

import gods.base.*;


public class Shopkeeper extends AnimatedFrames {

	private GfxFrameSet [] m_frames = new GfxFrameSet[2];
	private int m_walk_counter = 0;
	private boolean m_met;
	private static final int LATERAL_STEP = 12;
	
	private Hero m_hero;
	
	public String get_name() {
		// TODO Auto-generated method stub
		return "shopkeeper";
	}

	public void set_name(String n) {
		// TODO Auto-generated method stub
		
	}

	public Shopkeeper(GfxPalette palette, Hero hero) 
	{
		m_frames[0] = palette.lookup_frame_set("shopkeeper_right");		
		m_frames[1] = palette.get_left_frame_set(m_frames[0]);
		m_hero = hero;
	}
	
	public void init(Rectangle screen_bounds)
	{
		int wd = m_hero.get_walk_direction();
		
		GfxFrameSet current = m_frames[1];
		if (wd < 0)
		{
			current = m_frames[0];
		}
				
		init(current,0,Type.CUSTOM);
		if (wd < 0)
		{
			set_coordinates(screen_bounds.x - m_width,m_hero.get_y());
		}
		else
		{
			set_coordinates(screen_bounds.x + screen_bounds.width + m_width,m_hero.get_y());			
		}
		m_walk_counter = 0;
		m_met = false;
	}
	
	public boolean hero_met()
	{
		return m_met;
	}
	
	public void update(long elapsed_time) 
	{
		if (!m_met)
		{
			
			m_walk_counter+=elapsed_time;

			while (m_walk_counter > 100)
			{
				m_walk_counter -= 100;
				
				next_frame(1);

				int wd = m_hero.get_walk_direction();

				if (wd == 1)
				{
					if (m_x > m_hero.get_x() + m_hero.get_width())
					{
						m_x -= wd * LATERAL_STEP;
					}
					else
					{
						m_met = true;
						break;
					}
				}
				else
				{
					if (m_x + m_width < m_hero.get_x())
					{
						m_x -= wd * LATERAL_STEP;
					}
					else
					{
						m_met = true;
						break;
					}
				}
			}
		}
	}

}
