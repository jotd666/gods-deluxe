package gods.game.characters.weapons;

import gods.base.AnimatedFrames;
import gods.base.LevelData;
import gods.game.MonsterLayer;
import gods.sys.AngleUtils;

public class MagicAxe extends HeroWeapon {

	@Override
	public boolean can_destroy_spikes() {
		return false;
	}

	private double m_speed_x,m_speed_y;
	private final static double speed_x = 0.15;
	private final static double speed_y = 0.25;
	private int m_nb_bounces = 0;
	
	@Override
	protected void move(long elapsed_time) 
	{
		int x = get_x();
		int y = get_y();
		m_x += (m_speed_x + Math.signum(m_speed_x) * (Math.random() * 0.05)) * elapsed_time;
		m_y += m_speed_y * elapsed_time;
		
		if (m_state != State.VANISHING)
		{
			m_speed_y += elapsed_time / 1000.0;
			
			// max speed
			
			if (m_speed_y > 0.4)
			{
				m_speed_y = 0.4;
			}
		
		int x_boundary = get_x_boundary(x);
		int y_boundary = get_y_boundary(y);

		// test wall above the ground to avoid that ground is mixed up with wall
		// and the weapon bounces both ways when hitting ground
		
		if (is_wall_hit(x_boundary,y_boundary-m_height/2))
		{		
			// bounce against wall and symmetrize
			m_speed_x = -m_speed_x;
			m_x = 2*x - m_x;
			m_nb_bounces++;

			x_boundary += m_x - x;
		}

		if (is_ground_hit(x_boundary,y_boundary))
		{
			m_speed_y = -m_speed_y;
			if (Math.abs(m_speed_y) > 0.35)
			{
				// damp
				m_speed_y *= (0.6 - Math.random()*0.05);
			}
			m_nb_bounces++;
			m_y = 2*y - m_y;
			
			y_boundary += m_y - y;

		}


		
		
		if (m_nb_bounces > 6)
		{
			m_nb_bounces = 0; // avoid continuous sound
			
			set_state(State.VANISHING);
		}
		}
	}
	public void init(int shoot_angle, int frame_rate, int power, LevelData level, MonsterLayer ml) 
	{
		super.init(frame_rate, power, level, ml, "magic_axe", AnimatedFrames.Type.FOREVER);
			
		double direction = Math.signum(AngleUtils.cosd(shoot_angle));		
		
		m_speed_x = direction * speed_x;
		m_speed_y = AngleUtils.sind(shoot_angle) * speed_y;
		
	}
	
	@Override
	public void update(long elapsed_time) 
	{
		super.update(elapsed_time);
		
		// continue moving when exploding
		
		if (m_state == State.VANISHING)
		{
			move(elapsed_time);
		}
	}
}
