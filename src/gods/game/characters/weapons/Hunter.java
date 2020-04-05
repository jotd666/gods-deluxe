package gods.game.characters.weapons;

import gods.base.AnimatedFrames;
import gods.base.LevelData;
import gods.base.NamedLocatable;
import gods.game.MonsterLayer;
import gods.sys.AngleUtils;



public class Hunter extends HeroWeapon 
{
	@Override
	public boolean set_coordinates(double x, double y) {

		boolean rval =  super.set_coordinates(x, y);

		double direction = Math.signum(AngleUtils.cosd(m_shoot_angle));

		int d = m_width * 4;
		
		m_target.set_coordinates(m_x + d * direction,
				m_y + AngleUtils.sind(m_shoot_angle) * d);

		
		m_targeted_speed[0] = direction * m_speed;
		m_targeted_speed[1] = AngleUtils.sind(m_shoot_angle) * m_speed;

		return rval;
	}

	private double m_speed = 10;
	protected double [] m_current_speed = new double[2];
	private double [] m_targeted_speed = new double[2];
	private int m_shoot_angle;
	
	public void init(int shoot_angle, int frame_rate, int power, LevelData level, MonsterLayer ml) 
	{
		super.init(frame_rate, power, level, ml, "hunter", AnimatedFrames.Type.FOREVER);
			
		m_shoot_angle = shoot_angle;
	}
	

	private class AimPoint extends NamedLocatable
	{

		public String get_name() {
			
			return "aim";
		}
		public boolean is_named()
		{
			return true;
		}
		public void set_name(String n) {
			
			
		}
		
	}
	private NamedLocatable m_target = new AimPoint();
	private boolean m_hostile_locked = false;
	private int m_hostile_lock_timeout = 500;
	
	@Override
	public boolean can_destroy_spikes() 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void move(long elapsed_time) 
	{
		move_to_target(elapsed_time);
		/*if (!m_hostile_locked)
		{
			m_hostile_lock_timeout -= elapsed_time;
			
			if (m_hostile_lock_timeout > 0)
			{
				// linear move until aim is reached
				
			}
		}*/
		
	}
	
	private static final double SPEED_FACTOR = 1/30.0;
	
	private void compute_speed(int angle, double [] speed_coords)
	{

		speed_coords[0] = AngleUtils.cosd(angle) * m_speed;
		speed_coords[1] = AngleUtils.sind(angle) * m_speed;		
	}
	
	private static final int SPEED_MIX = 50;
	
	private void mix_speeds(long elapsed_time)
	{
		for (int i=0;i<2;i++)
		{
			m_current_speed[i] = ((m_current_speed[i]*SPEED_MIX)+(m_targeted_speed[i]*elapsed_time)) / (SPEED_MIX + elapsed_time);
		}
	}

	private void apply_speed(long elapsed_time)
	{
		m_x += (m_current_speed[0] * elapsed_time) * SPEED_FACTOR;
		m_y += (m_current_speed[1] * elapsed_time) * SPEED_FACTOR;

	}
		
	long m_oscillate_elapsed = 0;
	private void move_to_target(long elapsed_time)
	{
		int dx = m_target.get_x() - get_x();
		int dy = m_target.get_y() - get_y();

		int angle = AngleUtils.atan2d(dy,dx);

		int adx = Math.abs(dx);
		int ady = Math.abs(dy);

		
		int y_offset = 0;
		
		if ((adx > 4) || (ady > 4))
		{
			compute_speed(angle,m_targeted_speed);
		}
		else
		{
			if (m_hostile_lock_timeout < 0)
			{
				if (!m_hostile_locked)
				{
					// no hostile on screen: destroy ourselves

					set_state(State.VANISHING);
				}
				else
				{
					m_oscillate_elapsed += elapsed_time;
					
					y_offset = (int)(AngleUtils.sind((int)(m_oscillate_elapsed / 20.0)) * 10);
					m_y -= y_offset;
					
				}
			}
			else
			{
				m_hostile_lock_timeout -= elapsed_time;
			}
			
			m_targeted_speed[0] = 0;
			m_targeted_speed[1] = 0;
			m_x = m_target.get_x();
			m_y = m_target.get_y();

		}

		m_y += y_offset;
		
		// mix speed with current speed

		mix_speeds(elapsed_time);

		// apply the speed to x and y

		apply_speed(elapsed_time);
		
		if (m_hostile_lock_timeout < 0)
		{
			// update aim
			
			NamedLocatable l = m_monster_layer.closest_hostile(this,m_width*2);
			
			m_hostile_locked = (l != null);
			
			if (m_hostile_locked)
			{
				m_target = l;
			}
		}

	}

}
