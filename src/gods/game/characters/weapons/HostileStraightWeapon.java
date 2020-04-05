package gods.game.characters.weapons;

import gods.base.*;
import gods.game.characters.Hero;

public class HostileStraightWeapon extends HostileWeapon 
{
	private boolean m_to_left;
	private int m_speed;
	private int m_move_counter = 0;
	private GfxFrameSet m_frame_set;
	
	public void init(int speed, int update_rate, int power,
			boolean to_left, LevelData level, Hero hero, GfxFrameSet frame_set) 
	{
		super.init(update_rate, power, level, hero, frame_set,AnimatedFrames.Type.CUSTOM);
	
		m_speed = speed;
		m_frame_set = frame_set;
		m_to_left = to_left;
	}
	public void set_state(State s)
	{
		switch (s)
		{
		case HITTING_WALL:
			init(m_frame_set,60,Type.REVERSE);
			break;
		case HURTING_HERO:
			init(m_weapon_crash,100,Type.ONCE);
			break;
		}
		
		m_state = s;

	}
	
	private int m_timer = 0;
	private static final int FRAME_RATE = 40;
	
	@Override
	protected void move(long elapsed_time) 
	{
		m_timer += elapsed_time;
		
		while (m_timer > FRAME_RATE)
		{
			m_timer -= FRAME_RATE;

			m_move_counter++;
			if ((m_move_counter < 5) && (m_move_counter % 2 == 0))
			{
				m_frame_counter++;
			}

			int previous_x = get_x();

			m_x += m_to_left ? -m_speed : m_speed;

			if (is_wall_hit(get_x_boundary(previous_x),m_y))
			{
				set_state(State.HITTING_WALL);
			}
			else 
			{
				hero_collision_test();
			}
		}
	}

}
