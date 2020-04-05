package gods.game.characters.hostiles;

import gods.base.HostileWaveParameters;
import gods.game.characters.FlyingMonster;
import gods.game.characters.HostileParameters;
import gods.game.characters.weapons.Projectile;

public class FlyingBuddha extends FlyingMonster 
{
	private int m_previous_frame = 0;
	
	@Override
	protected void animate(long elapsed_time) 
	{
		super.animate(elapsed_time);
		
		// shoot when 7th frame is displayed. no parametrizable frequency
		// for this monster
		
		if (m_frame_counter != m_previous_frame)
		{
			if (m_frame_counter == 7)
			{
				Projectile p = shoot();
				
				// adjust to match mouth position
				
				p.set_y(p.get_y()+24);
			}
		}
		
		m_previous_frame = m_frame_counter;
	}

	@Override
	public void init(HostileParameters p) {
		super.init(p);
		// force fire type
		m_params.fire_type = HostileWaveParameters.FireType.Straight;
	}

	protected boolean may_shoot()
	{
		// avoid handling o the shoot by mother class
		return false;
	}

}
