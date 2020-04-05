package gods.game.characters.weapons;

import gods.base.*;
import gods.game.*;
import gods.sys.AngleUtils;

public class HeroLinearWeapon extends HeroWeapon 
{

	@Override
	public boolean can_destroy_spikes() 
	{
		return false;
	}

	protected double m_cos, m_sin;
	// V0.9 reduced from 0.4, seemed faster than the original game, and thus
	// damage inflicted by weapons was lower
	protected double speed = 0.36;  
	
	public void init(int shoot_angle, int frame_rate, int power, LevelData level, 
			MonsterLayer ml,String frame_name) 
	{
		m_cos = Math.signum(AngleUtils.cosd(shoot_angle)) * speed;
		m_sin = AngleUtils.sind(shoot_angle) * speed;

		// default behaviour when weapon rotates
		
		init(frame_rate, power, level, ml, frame_name, 
				m_cos > 0 ? AnimatedFrames.Type.FOREVER : 
					AnimatedFrames.Type.FOREVER_REVERSE);
	}
	
	@Override
	protected void move(long elapsed_time) 
	{
		double dx = m_cos * elapsed_time;
		double dy = m_sin * elapsed_time;
		
		m_x += dx;
		m_y += dy;
	}

}
