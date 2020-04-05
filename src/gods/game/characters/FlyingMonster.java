package gods.game.characters;

import java.util.Collection;

import gods.game.SfxSet.Sample;
import gods.game.characters.weapons.*;

import gods.sys.AngleUtils;
import gods.base.*;


public abstract class FlyingMonster extends LeftRightHostile {
	@Override
	public boolean is_in_background() {
		
		return false;
	}

	@Override
	public void update(long elapsed_time) 
	{
		// save previous coordinates
				
		m_previous_position.set_coordinates(m_x,m_y);

		super.update(elapsed_time);
	}

	@Override
	protected void animate(long elapsed_time)
	{
		if ((m_current_path == null) || (m_current_path.get_type() != HostileTrajectory.Type.Frozen
				&& m_current_path.get_type() != HostileTrajectory.Type.Custom))
		{
			super.animate(elapsed_time);
		}
	}


	@Override
	public Sample get_exploding_sound() 
	{
		return Sample.explosion_flying;
	}

	private static final int MAX_AWAY_SQUARE_DISTANCE = 300000;
	private static final int AIM_CHANGE_TIME = 1000;
	protected double [] m_current_speed = new double[2];
	private double [] m_targeted_speed = new double[2];
	
	private int m_flee_speed_angle = Integer.MAX_VALUE;
	//private boolean m_circular_lock;
	private int m_location_sq_distance;
	private int m_radius;
	private int m_angle;
	private NamedLocatable m_circular_aim;
	private NamedLocatable m_previous_position;
	private int m_change_aim_timer;
	private int m_surface;
	private int m_max_square_distance;
	protected int m_speed;

	private void move_towards_hero(long elapsed_time)
	{
		move_to_location(m_params.hero,elapsed_time,false,false);
	}
	
	private NamedLocatable m_drop_location = null;
	
	private void drop_to_hero_level(long elapsed_time)
	{
		if (m_drop_location == null)
		{
			m_drop_location = new SimpleLocation(m_width,m_height);
			
			m_drop_location.set_y(m_params.hero.get_y());
			m_drop_location.set_x(get_x());
			
		}
		move_to_location(m_drop_location,elapsed_time,false,false);
		int dx = m_params.hero.get_x_center() - get_x_center();
		set_right_left(dx > 0 ? 0 : 1);
	}
	
	private void compute_speed(int angle, double [] speed_coords)
	{

		speed_coords[0] = AngleUtils.cosd(angle) * m_speed;
		speed_coords[1] = AngleUtils.sind(angle) * m_speed;		
	}
	
	private void mix_speeds(long elapsed_time)
	{
		for (int i=0;i<2;i++)
		{
			m_current_speed[i] = ((m_current_speed[i]*200)+(m_targeted_speed[i]*elapsed_time)) / (200 + elapsed_time);
		}
	}
	
	protected int m_head_angle;
	
	protected void move_to_location(Locatable aim, long elapsed_time, 
			boolean freeze_when_reached, boolean around)
	{
		int dx = aim.get_x() - get_x();
		int dy = aim.get_y() - get_y();

		m_head_angle = AngleUtils.atan2d(dy,dx);

		int adx = Math.abs(dx);
		int ady = Math.abs(dy);

		int aw = aim.get_width();
		int ah = aim.get_height();
		
		if (around)
		{
			compute_speed(m_head_angle,m_targeted_speed);
		}
		else
		{
			if ((adx > aw/2) || (ady > ah/2))
			{

				compute_speed(m_head_angle,m_targeted_speed);


				if (adx < aw)
				{
					m_targeted_speed[0] *= 0.5;	
				}
				if (ady < ah)
				{
					m_targeted_speed[1] *= 0.5;
				}
			}
			else
			{
				if (freeze_when_reached)
				{
					m_targeted_speed[0] = 0;
					m_targeted_speed[1] = 0;
					m_x = aim.get_x();
					m_y = aim.get_y();
				}
				else
				{
					next_path();
				}			
			}
		}
		
		// mix speed with current speed
		
		mix_speeds(elapsed_time);
		
		// apply the speed to x and y
		
		apply_speed(elapsed_time);
		
	}
	
	protected boolean is_moving()
	{
		return m_targeted_speed[0] != 0.0 || m_targeted_speed[1] != 0.0;
	}
	@Override
	protected void trajectory_change()
	{
		//m_circular_lock = false;
		if (m_current_path != null)
		{
			m_circular_aim = m_current_path.get_location();
		}
	}
	
	protected ControlObject m_aim = new ControlObject(0,0,1,1,"XX",null);
	
	private int m_move_timer = 0;
	private int m_old_y_offset = 0;
	
	@Override
	protected void move(long elapsed_time) 
	{
		boolean oscillate = true;
				
		// oscillations
		
		int ocspd = (m_params.trajectory.get_vertical_oscillation_speed());
		
		if (ocspd > 0)
		{
			m_y -= m_old_y_offset;
		}
		
		if (m_current_path != null)
		{
			
			NamedLocatable original_aim = m_current_path.get_location();
			
			int clockwise = -1;
			
			switch (m_current_path.get_type())
			{
			case To_Hero:
				move_towards_hero(elapsed_time);
				break;
			case Drop_To_Hero_Level:
				drop_to_hero_level(elapsed_time);
				oscillate = false;
				break;
			case Around_Location_Clockwise:
				clockwise = 1;
			case Around_Location_Anti_Clockwise:
				m_location_sq_distance = original_aim.get_height()*original_aim.get_width();

				int sq_distance = square_distance_to(m_circular_aim);

				if (sq_distance < m_location_sq_distance)
				{
					int dy = get_y() - original_aim.get_y();
					int dx = get_x() - original_aim.get_x();

					m_radius = (int)Math.sqrt(m_location_sq_distance) / 2;

					// compute angle
					m_angle = AngleUtils.atan2d(dy, dx);

					double delta_angle = (m_speed / (m_radius * Math.PI)) * 180;
					
					m_angle += delta_angle * clockwise;

					m_aim.set_coordinates((int)(m_radius * AngleUtils.cosd(m_angle) + original_aim.get_x()),
							(int)(m_radius * AngleUtils.sind(m_angle) + original_aim.get_y())); 

					m_circular_aim = m_aim;
					
					move_to_location(m_aim,elapsed_time,false,true);
				}
				else
				{
					move_to_location(original_aim,elapsed_time,false,true);

				}
				
				break;
			case Frozen:
				m_y = m_previous_position.get_y();
				oscillate = false;
				break;
			case Custom:
				oscillate = false;
				break;
			case To_Location:
				move_to_location(original_aim,elapsed_time,false,false);
				break;
			case Flee:
				if (m_flee_speed_angle == Integer.MAX_VALUE)
				{
					if ((m_current_speed[1] == m_current_speed[0]) && m_current_speed[0] == 0.0)
					{
						// trajectory only contains flee because no current speed is set
						m_flee_speed_angle = m_params.trajectory.get_initial_angle();
					}
					else
					{
						m_flee_speed_angle = AngleUtils.atan2d(m_current_speed[1],m_current_speed[0]);
					}
					compute_speed(m_flee_speed_angle,m_targeted_speed);
				}
				mix_speeds(elapsed_time);
				// continue
				apply_speed(elapsed_time);
				
				// kill monsters when far enough from hero
				if (square_distance_to(m_params.hero) > m_max_square_distance)
				{
					// do not call "die" method, directly set dead state

					set_life_state(LifeState.DEAD);
				}
				break;
			default:
				// TODO
				break;
			}
		}
		else
		{
			
			m_change_aim_timer += elapsed_time;
			if (m_change_aim_timer>=AIM_CHANGE_TIME)
			{
				m_change_aim_timer-=AIM_CHANGE_TIME;
				int current_distance = square_distance_to(m_params.hero);

				
				double random100 = Math.random()*100;

				if (m_params.objective_balance > random100)
				{
					// attack directly hero
				
					m_aim.set_coordinates(m_params.hero);
				}
				else
				{
					// random move: either goes far away from objective or closer to it
					// first select an aim at random around the monster
					double random_x_offset=(Math.random()*50)-100;
					double random_y_offset=(Math.random()*50)-100;
					m_aim.set_x(random_x_offset+this.get_x_center());
					m_aim.set_y(random_y_offset+this.get_y_center());
					int predicted_distance = m_aim.square_distance_to(m_params.hero);

					if (predicted_distance<current_distance)

					{
						// would get monster closer: aim the opposite
						m_aim.set_x(-random_x_offset+this.get_x_center());
						m_aim.set_y(-random_y_offset+this.get_y_center());

					}
				}

			}
			// now move to selected location
			move_to_location(m_aim, elapsed_time,false,false);
			
			

		}
		if (oscillate) 
		{
			if (ocspd > 0)
			{
				m_old_y_offset = (int)(AngleUtils.sind((ocspd * m_move_timer) / 40) * m_height/2);
				m_y += m_old_y_offset;
				m_move_timer += elapsed_time;
			}
		}
		
		steal_objects_on_the_way();
		
		shoot_avoidance(elapsed_time);
	}

	private int closest_weapon(NamedLocatable l)
	{
		int rval = Integer.MAX_VALUE;
		
		Collection<HeroWeapon> wc = m_params.hero.get_weapon_set().get_projectile_list();
		 
		for (HeroWeapon c : wc)
		{
			int d = c.square_distance_to(l);
			
			if (d < rval)
			{
				rval = d;
			}
		}
		
		return rval;
	}
	
	private void shoot_avoidance(long elapsed_time)
	{
		if (m_params.avoid_shoot)
		{
			// find the closest hero weapon
			
			int current_closest = closest_weapon(this);
			
			if (current_closest < m_surface * 2)
			{
				// a weapon is coming by: compute the distance compared to the old position

				int previous_closest = closest_weapon(m_previous_position);

				// if new distance is inferior to the old distance, go backwards
			
				if (current_closest < previous_closest)
				{
					//m_x = 2 * m_previous_position.get_x() - m_x;
					m_targeted_speed[0] = -m_targeted_speed[0];
					//m_y = 2 * m_previous_position.get_y() - m_y;
					m_y = m_previous_position.get_y() + (m_current_speed[1] * elapsed_time) * SPEED_FACTOR;
				}
			}
		}
	}
	
	class SimpleLocation extends NamedLocatable
	{
		SimpleLocation(int width, int height)
		{
			super(0,0,width,height);
		}
	
		public String get_name() { return FlyingMonster.this.get_name(); }
		public void set_name(String s) {}
		public boolean is_named()
		{
			return !get_name().equals("");
		}
	};
	
	@Override
	public void init(HostileParameters p) 
	{
		super.init(p);
		
		m_change_aim_timer = AIM_CHANGE_TIME;
		
		m_max_square_distance = m_params.view_bounds.width * m_params.view_bounds.width;
		
		m_exploding_frame_set = m_appearing_frame_set;
		
		// no appearing sequence

		compute_speed(p.trajectory.get_initial_angle(), m_current_speed);
		
		m_previous_position = new SimpleLocation(m_width,m_height);
		
		m_surface = m_width * m_height;
		
		m_speed = m_params.get_flying_speed_value();
		
		m_head_angle = p.trajectory.get_initial_angle();
		
	}

	private static final double SPEED_FACTOR = 1/60.0;
	
	protected void apply_speed(long elapsed_time)
	{
		m_x += (m_current_speed[0] * elapsed_time) * SPEED_FACTOR;
		m_y += (m_current_speed[1] * elapsed_time) * SPEED_FACTOR;
		
		// leave "as-is" if speed is rigirously equal to 0 (which can be the case
		// when there are special moves such as "drop to hero")
		
		if (m_current_speed[0] > 1e-4)
		{
			set_right_left(0);
		}
		else if (m_current_speed[0] < -1e-4)
		{
			set_right_left(1);
		}
	}

}
