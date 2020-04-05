package gods.game.characters.hostiles;


import gods.game.characters.*;


public class GroundStick extends GroundMonster
{

	@Override
	public void init(HostileParameters p) 
	{
		super.init(p);
		
		m_animation_frame_rate = 400 / m_speed;
	}

	@Override
	protected void move(long elapsed_time) 
	{
		
	}

	protected void lateral_move(long elapsed_time)
	{
		m_x += 30 * get_walk_direction();
	}
	
	private int m_animation_frame_rate;
	
	protected void animate(long elapsed_time)
	{
		m_animation_timer += elapsed_time;
		
		while (m_animation_timer > m_animation_frame_rate)
		{
			m_animation_timer -= m_animation_frame_rate;

			m_frame_counter ++;

			if (m_frame_counter > m_params.nb_move_frames)
			{
				int x = common_move(elapsed_time);

				if (is_lateral_way_blocked())
				{
					set_right_left(1 - m_right_left);
					m_x = x;
				}
				if (!is_vertical_way_blocked())
				{
					set_right_left(1 - m_right_left);
					m_x = x;
				}

				m_frame_counter = 1;
			}
		}
		
	}

	public GroundStick()
	{
		super(false,null);
	}

}
