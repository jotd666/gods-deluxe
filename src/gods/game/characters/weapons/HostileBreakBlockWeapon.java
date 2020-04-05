package gods.game.characters.weapons;

import gods.base.*;
import gods.game.characters.Hero;

public class HostileBreakBlockWeapon extends HostileWeapon 
{
	private double m_speed_y;
	
	public void init(double speed_y,LevelData level, Hero hero, GfxFrameSet frame_set) 
	{
		super.init(50, 4, level, hero, frame_set,AnimatedFrames.Type.FOREVER);
	
		m_speed_y = speed_y;
	}
	public void set_state(State s)
	{
		switch (s)
		{
		case HURTING_HERO:
			// hurts hero (set in hero_collision_test) then
			// set dead state since there's no weapon crash animation
			
			m_state = State.DEAD;
			break;
		default:
			m_state = s;
		break;
		}
	}
		
		
	@Override
	protected void move(long elapsed_time) 
	{
		m_y += (m_speed_y * elapsed_time) / 60;

		if (is_wall_hit(m_x,m_y))
		{
			// try to break the block
			m_level_data.hit_block((int)m_x, (int)m_y, m_power);
		}

		hero_collision_test();
		
	}

}
