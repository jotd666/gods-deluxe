package gods.game.characters.hostiles;

import java.awt.Graphics2D;

import gods.game.characters.*;
import gods.base.*;

public class Bat extends FlyingMonster {

	@Override
	protected void animate(long elapsed_time) 
	{
		if (!is_hanging()) 
		{
			super.animate(elapsed_time);
		}
		else
		{
			int dx = m_params.hero.get_x_center() - get_x_center();

			if (dx < 0)
			{
				set_right_left(1);
				switch (m_current_hanging_frame)
				{
				case 1:
					break;
				case 2:
				case 3:
					m_turn_timer += elapsed_time;
					if (m_turn_timer > TURN_TIME)
					{
						m_turn_timer = 0;
						m_current_hanging_frame--;
					}
					break;
				}
			}
			else
			{
				set_right_left(0);
				switch (m_current_hanging_frame)
				{
				case 3:
					break;
				case 2:
				case 1:
					m_turn_timer += elapsed_time;
					if (m_turn_timer > TURN_TIME)
					{
						m_turn_timer = 0;
						m_current_hanging_frame++;
					}
					break;
				}
			}
		}
	}
	
	private boolean is_hanging()
	{
		return m_current_path == null || (m_current_path.get_type() == HostileTrajectory.Type.Custom);
	}
	@Override
	public void render(Graphics2D g) 
	{
		if (!is_hanging() || m_life_state != LifeState.ALIVE)
		{
			super.render(g);
		}
		else
		{
			draw_image(g,m_hanging_frames.get_frame(m_current_hanging_frame).toImage());
		}
	}

	private static final int TURN_TIME = 500;
	private static final int TRIGGER_TIME = 1000;
	private GfxFrameSet  m_hanging_frames;
	private int m_current_hanging_frame = 2;
	private int m_turn_timer = 0;
	private int m_trigger_timer = 0;
	
	@Override
	public void init(HostileParameters p) 
	{

		super.init(p);
		
		m_hanging_frames = m_params.level.get_level_palette().lookup_frame_set("hanging_bat");
		
		m_current_speed[0] = 0.0;
		m_current_speed[1] = 0.0;
		
		m_params.score_points = 500;
	}

	@Override
	protected void move(long elapsed_time) 
	{
		if (!is_hanging())
		{
			super.move(elapsed_time);
		}
		else
		{
			if (m_trigger_timer == 0)
			{			

				int dy = m_params.hero.get_y() - get_y();
				if (dy > m_height)
				{
					// hero below: start countdown for attack

					m_trigger_timer += elapsed_time;
				}
			}
			else
			{
				// don't attack if not well in-screen
				if (is_in_screen(-m_height))
				{
					m_trigger_timer += elapsed_time;
					if (m_trigger_timer > TRIGGER_TIME)
					{

						next_path();
					}
				}
			}
		}
	}
	




	


}
