package gods.game.characters.hostiles;

import gods.game.characters.FiringHead;

public class FiringHeadGun extends FiringHead {

	@Override
	protected void animate(long elapsed_time)
	{		
		if (m_shooting)
		{
			int shoot_anim = m_shoot_counter / 100;
			// do nothing except when shooting
			
			if (shoot_anim > 3)
			{
				m_shooting = false;
				m_frame_counter = 1;
			}
			else
			{
				m_frame_counter = shoot_anim+1;
			}
		}
	}
}
