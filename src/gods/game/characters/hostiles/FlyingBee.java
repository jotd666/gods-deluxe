package gods.game.characters.hostiles;

import java.awt.Graphics2D;

import gods.base.GfxFrame;
import gods.base.GfxFrameSet;
import gods.game.characters.FlyingMonster;
import gods.game.characters.HostileParameters;

public class FlyingBee extends FlyingMonster 
{

	private enum MoveState { EXIT_HIVE, STAND_BY, NORMAL }
	
	private MoveState m_move_state = MoveState.EXIT_HIVE;
	private int m_exit_time;
	private int m_stand_by_time;
	private int m_normal_move_time;
	private GfxFrameSet m_face_frames;
	private GfxFrame m_current_frame;
	private int m_stand_by_frame = 0;
	private int m_stand_by_animation_counter;
	
	@Override
	protected void move(long elapsed_time) 
	{
		switch (m_move_state)
		{
		case EXIT_HIVE:
			m_exit_time -= elapsed_time;
			m_current_speed[0] = 0.0;
			m_current_speed[1] = -m_speed;
			apply_speed(elapsed_time);
			if (m_exit_time < 0)
			{
				stand_by();
			}
			break;
		case STAND_BY:
			m_stand_by_time -= elapsed_time;
			if (m_stand_by_time < 0)
			{
				m_normal_move_time = (int)(Math.random() * 1000 + 500);
				m_move_state = MoveState.NORMAL;
				
				// pick a location close to the hero
				
				double x_offset = (Math.random() * m_width * 4) - m_width * 2;
				double y_offset = (Math.random() * m_width * 3) - m_width;
				
				m_aim.set_coordinates(m_params.hero.get_x() + x_offset,m_params.hero.get_y() + y_offset);
			}
			break;
		case NORMAL:
			//super.move(elapsed_time);
			move_to_location(m_aim,elapsed_time,false,false);
			m_normal_move_time -= elapsed_time;
			if (m_normal_move_time < 0)
			{
				stand_by();
			}
			break;
		}
	}

	private void stand_by()
	{
		m_stand_by_time = (int)(Math.random() * 1000 + 500);
		m_move_state = MoveState.STAND_BY;
		m_stand_by_animation_counter = 0;
		m_stand_by_frame = 1;
		m_current_frame = m_face_frames.get_first_frame();
	}
	
	@Override
	public void render(Graphics2D g)
	{
		if ((m_move_state == MoveState.NORMAL) || (get_life_state() != LifeState.ALIVE))
		{
			super.render(g);
		}
		else
		{
			if (m_current_frame != null)
			{
				draw_image(g, m_current_frame.toImage());
			}
		}
	}

	@Override
	protected void animate(long elapsed_time) 
	{
		switch (m_move_state)
		{
		case EXIT_HIVE:
			m_current_frame = m_face_frames.get_first_frame();
			break;
		case STAND_BY:
			m_stand_by_animation_counter += elapsed_time;
			while (m_stand_by_animation_counter > 50)
			{
				m_stand_by_animation_counter -= 50;
				m_stand_by_frame++;
				if (m_stand_by_frame > m_face_frames.get_nb_frames())
				{
					m_stand_by_frame = 2;
				}
				m_current_frame = m_face_frames.get_frame(m_stand_by_frame);
			}
			
			break;
		case NORMAL:
			super.animate(elapsed_time);
			break;
		}
	}


	@Override
	public void init(HostileParameters p) 
	{
		m_appearing_animation = false;

		super.init(p);

		m_face_frames = m_params.level.get_level_palette().lookup_frame_set("bee_front");
		
		m_exit_time = (int)(Math.random() * 1000 + 500);
		
		// locate above hive
		
		m_y -= m_height;
		
		m_params.score_points = 500; // fixed
	}

}
