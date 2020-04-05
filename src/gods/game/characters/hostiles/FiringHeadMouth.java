package gods.game.characters.hostiles;

import gods.game.characters.FiringHead;

public class FiringHeadMouth extends FiringHead {

	@Override
	protected void animate(long elapsed_time)
	{		
		if (m_shooting)
		{
			int shoot_anim = m_shoot_counter / 100;
			// do nothing except when shooting
			switch (shoot_anim)
			{
			case 0:
			case 2:
				m_frame_counter = 2;
				break;
			case 1:
				m_frame_counter = 3;
				break;
			default:
				m_shooting = false;
			m_frame_counter = 1;
			break;
			}
		}
	}
}
