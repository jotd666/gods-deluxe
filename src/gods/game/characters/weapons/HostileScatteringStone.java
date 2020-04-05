package gods.game.characters.weapons;

import gods.base.AnimatedFrames;
import gods.base.GfxFrameSet;
import gods.base.LevelData;
import gods.game.characters.Hero;

public class HostileScatteringStone extends HostileWeapon {

	private double x_speed, y_speed;
	private int m_time_to_live = 1000;
	
	public void init(int x_direction, int power, LevelData level, Hero hero, GfxFrameSet frame_set) 
	{		
		super.init(100, power, level, hero, frame_set, AnimatedFrames.Type.FOREVER);
		
		x_speed = x_direction * 0.05;
		
		if (x_direction == 0)
		{
			y_speed = -0.3;
		}
		else
		{
			y_speed = -0.25;
		}
	}

	@Override
	protected void move(long elapsed_time) 
	{
		m_time_to_live -= elapsed_time;
		
		if (m_time_to_live < 0)
		{
			set_state(State.VANISHING);
		}
		m_x += x_speed * elapsed_time;
		
		y_speed += elapsed_time / 1200.0;
		
		m_y += y_speed * elapsed_time;
		
		if (get_state() == State.ALIVE)
		{
			hero_collision_test();
		}
	}

}
