package gods.game.characters.weapons;

import gods.base.*;
import gods.game.characters.Hero;


public class HostileDropWeapon extends HostileWeapon 
{
	private double m_speed_y;
	private GfxFrameSet m_frame_set;
	private int m_time_on_the_ground = 0;
	private boolean m_on_the_ground = false;
	
	public void init(double speed_y, int update_rate, int power,
			LevelData level, Hero hero, GfxFrameSet frame_set) 
	{
		super.init(update_rate, power, level, hero, frame_set,AnimatedFrames.Type.FOREVER);
	
		m_speed_y = speed_y;
		m_frame_set = frame_set;
	}
	public void set_state(State s)
	{
		switch (s)
		{
		case HURTING_HERO:
			init(m_weapon_crash,100,Type.ONCE);
			break;
		}
		
		m_state = s;

	}
	@Override
	protected void move(long elapsed_time) 
	{
		if (m_on_the_ground)
		{
			m_time_on_the_ground += elapsed_time;
			if (m_time_on_the_ground > 1000) // stays 1 second on the ground before disappearing
			{
				set_state(State.DEAD);
				//init(m_frame_set,60,Type.REVERSE);
			}	
		}
		else
		{
			double old_y = m_y;

			m_y += m_speed_y * elapsed_time;
			
			if (is_wall_hit(m_x,m_y + m_height))
			{
				m_y = old_y;

				init(m_frame_set,60,Type.FOREVER);
				m_on_the_ground = true;
				m_time_on_the_ground = 0;
			}
		}

		hero_collision_test();
		
	}

}
