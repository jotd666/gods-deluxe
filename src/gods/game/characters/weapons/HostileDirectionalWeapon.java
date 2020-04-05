package gods.game.characters.weapons;

import gods.base.*;
import gods.game.characters.Hero;
import gods.sys.AngleUtils;

public class HostileDirectionalWeapon extends HostileWeapon 
{
	private double m_speed_x = 0, m_speed_y = 0;
	private int m_speed = 0;
	
	public void init(int speed, int update_rate, int power,
			LevelData level, Hero hero, Locatable aim, GfxFrameSet frame_set) 
	{
		super.init(update_rate, power, level, hero, frame_set,AnimatedFrames.Type.FOREVER);
		m_speed = speed;
		m_aim = aim;
	}
	private Locatable m_aim;
	
	@Override
	public boolean set_coordinates(double x, double y)
	{
		boolean rval = super.set_coordinates(x,y);
		
		double dx = m_aim.get_x_center() - x;
		double dy = m_aim.get_y() + m_aim.get_height()/4 - y;
		
		int angle = AngleUtils.atan2d(dy, dx);
		
		m_speed_x = m_speed * AngleUtils.cosd(angle) / 40;
		m_speed_y = m_speed * AngleUtils.sind(angle) / 40;
		
		return rval;
	}

	@Override
	protected void move(long elapsed_time) 
	{
		m_x += m_speed_x * elapsed_time;
		m_y += m_speed_y * elapsed_time;
		
		if (is_wall_hit(m_x,m_y))
		{
			set_state(State.HITTING_WALL);
		}
		else 
		{
			hero_collision_test();
		}
	}

}
