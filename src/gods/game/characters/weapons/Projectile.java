package gods.game.characters.weapons;

import gods.base.*;

public abstract class Projectile extends AnimatedFrames
{
	//private int m_start_x, m_start_y;
	protected int m_power;
	protected LevelData m_level_data;

	protected GfxFrameSet m_weapon_crash;
	
	protected State m_state = State.ALIVE;
	
	public enum State { ALIVE, HURTING_HOSTILE, HURTING_HERO, HITTING_WALL, VANISHING, DEAD }
	
	protected abstract void move(long elapsed_time);
	
	public void set_state(State s)
	{
		boolean state_change = m_state != s;
		
		if (state_change)
		{
			m_state = s;

			switch (m_state)
			{
			case HITTING_WALL:
			case HURTING_HERO:
			case VANISHING:
			{
				init(m_weapon_crash,100,Type.ONCE);
			}
			break;
			}
		}
	}
	
	public State get_state()
	{
		return m_state;
	}
	

	
	protected int get_x_boundary(int previous_x)
	{
		return (int)((previous_x > m_x) ? m_x : m_x + m_width);
	}
	protected int get_x_opposite_boundary(int previous_x)
	{
		return (int)((previous_x < m_x) ? m_x : m_x + m_width);
	}
	protected int get_y_boundary(int previous_y)
	{
		return (int)((previous_y > m_y) ? m_y : m_y + m_height);
	}
	
	protected boolean is_scenery_hit()
	{
		int x = get_x_center();
		int y = get_y_center();
		
		return is_wall_hit(x, y) || is_ground_hit(x, y);
	}
	protected boolean is_wall_hit(double x, double y)
	{
		return m_level_data.is_lateral_way_blocked(x, y, m_height);
	}
	protected boolean is_ground_hit(double x, double y)
	{
		return m_level_data.is_vertical_way_blocked(x, y, m_width);
	}
	
	public void init(int update_rate, int power, LevelData level, 
			GfxFrameSet frame_set, AnimatedFrames.Type anim_type)
	{
		init(frame_set,update_rate,anim_type);
		
		//m_start_x = x;
		//m_start_y = y;
		m_power = power;
		m_level_data = level;
		
		m_weapon_crash = level.get_common_palette().lookup_frame_set("weapon_crash");
	}

	public int get_power()
	{
		return m_power;
	}
	
	@Override
	public void update(long elapsed_time) 
	{
		basic_update(elapsed_time);
	}
	
	protected void basic_update(long elapsed_time) 
	{
		super.update(elapsed_time);

		switch (m_state)
		{
		case ALIVE:
		
			move(elapsed_time);
			
			break;
			
		case HURTING_HOSTILE:
			m_state = State.DEAD;			
			break;
		case HURTING_HERO:			
		case HITTING_WALL:
		case VANISHING:
			if (is_done())
			{
				m_state = State.DEAD;
			}
			break;
		}
	
	}
}
