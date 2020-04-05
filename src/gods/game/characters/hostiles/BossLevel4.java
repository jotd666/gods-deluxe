package gods.game.characters.hostiles;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import gods.base.*;
import gods.game.SfxSet;
import gods.game.SfxSet.Sample;
import gods.game.characters.*;
import gods.game.characters.weapons.HeroWeapon;
import gods.game.characters.weapons.HostileWeapon;
import gods.game.characters.weapons.Projectile;
import gods.sys.AngleUtils;

public class BossLevel4 extends Hostile implements Boss
{
	private GfxFrameSet m_jaw;
	private GfxFrameSet m_transparent_jaw;
	private GfxFrameSet m_boss_fire_ball;
	private GfxFrameSet m_eyes_fire_ball;
	private GfxFrameSet m_head_top;
	
	private HostileParameters m_worm_template = null;
	
	private enum BossState { STAY, SHOOT, RISE }
		
	private BossState m_state = BossState.RISE;
	
	private int m_boss_state_counter = 0;

	private int m_explosion_timer = 0;
	private int m_starburst_counter = 0;
	
	private BufferedImage m_head;
	private BufferedImage m_neck;
	private double m_y_speed = 0.1;
	private double m_y_end;
	
	private BossLevel4Worm m_current_worm = null;
	private int m_saved_worm_health = -1;
	
	private static final int FIREBALL_PERIOD = 3000;
	private static final int WORM_PERIOD = 6000;
	private int m_worm_timer = -FIREBALL_PERIOD;
	private int m_fireball_period = 0;
	private int m_jaw_index = 0;
	private int m_old_jaw_index = 0;
	private static final int MAX_NB_WORMS = 6;
	private int m_nb_worms = MAX_NB_WORMS;
	private Boss4FireBall m_mouth_fireball = null;
	
	private static final int X_JAW_OFFSET = 32;
	private static final int Y_JAW_OFFSET = 116;
	private static final int X_SKULL_OFFSET = 32;
	public static final int Y_SKULL_OFFSET = 6;

	@Override
	public boolean is_in_background() 
	{
		return true;
	}

	@Override
	public void collision(Hero h) 
	{
		// collision with the boss is lethal, does not hurt him
		h.hurt(Hero.MAX_HEALTH*10);
	}
	
	@Override
	public boolean hit(HeroWeapon hw) 
	{
		if (m_life_state == LifeState.ALIVE)
		{
			if (hw == null)
			{
				m_params.health_points = 0;
				set_life_state(LifeState.EXPLODING);
			}
			else
			{
				// no harm done to the skull
				hw.set_state(Projectile.State.HURTING_HOSTILE);				
			}
		}
		return true;
	}
	@Override
	public Sample get_appearing_sound() {
		return null;
	}

	@Override
	public Sample get_exploding_sound() {
		return null;
	}

	
	public String get_name()
	{
		return "boss";
	}
	
	@Override
	public void die() 
	{
		// nothing happens
	}
	// huge fireball bouncing on the ground
	public class Boss4FireBall extends HostileWeapon 
	{

		public void really_render(Graphics2D g) {
			
			super.render(g);
		}
		@Override
		public void render(Graphics2D g) 
		{
		}

		private double m_x_speed;
		private double m_y_speed;
		
		public Boss4FireBall()
		{
			m_x_speed = -0.25;
			m_y_speed = -0.08;
		}
		
		public void init() 
		{
			super.init(100, 6, m_params.level, m_params.hero,
					m_boss_fire_ball,AnimatedFrames.Type.FOREVER);
			m_weapon_crash = m_params.level.get_level_palette().lookup_frame_set("boss_fireball_blast");
		}

		@Override
		protected void move(long elapsed_time) 
		{
			if (is_ground_hit(m_x,m_y + m_height))
			{
				m_y_speed = -0.1;
				m_x_speed = -0.2;
			}
			
			if (m_level_data.is_lateral_way_blocked(m_x + m_width,m_y,2))
			{
				set_state(State.HITTING_WALL);
			}

			hero_collision_test();
			
			m_x -= m_x_speed * elapsed_time;
			m_y += m_y_speed * elapsed_time;

			m_y_speed += 0.0005 * elapsed_time;

		}

	}

	

	@Override
	public void init(HostileParameters p) 
	{
		super.init(p);
		GfxPalette palette = p.level.get_level_palette();
		GfxFrameSet gfs = palette.lookup_frame_set(p.frame_set_name);
		m_head = gfs.toImage();
		gfs = palette.lookup_frame_set("boss_neck");
		m_neck = gfs.toImage();
		m_boss_fire_ball = palette.lookup_frame_set("boss_fire_ball");
		m_eyes_fire_ball = p.level.get_common_palette().lookup_frame_set("enemy_fireball");
		m_jaw = palette.lookup_frame_set("boss_jaw");
		m_head_top = palette.lookup_frame_set("boss_head_top");
		m_transparent_jaw = palette.lookup_frame_set("boss_jaw_transparent");
		set_life_state(LifeState.ALIVE);
		
		m_y_end = m_y - (m_height * 3)/2;
		
	}



	private void shoot_fireball()
	{
		m_mouth_fireball = new Boss4FireBall();
		m_mouth_fireball.init();
		m_mouth_fireball.set_coordinates(m_x + 24*2,m_y + 64*2);
		
		m_params.weapon_set.add(m_mouth_fireball);
		
		m_params.sfx_set.play(SfxSet.Sample.boss_shoot,2);

	}
	
	public void render(Graphics2D g) 
	{
		if (get_life_state() == LifeState.ALIVE)
		{
			draw_image(g,m_neck,-10,m_height - 10);
			draw_image(g,m_neck,-20,m_height -10 + m_neck.getHeight());
			draw_image(g,m_head);


			switch (m_jaw_index)
			{
			case 1:
			case 3:
				draw_image(g,m_jaw.get_first_frame().toImage(),X_JAW_OFFSET,Y_JAW_OFFSET);
				break;
			case 2:
				draw_image(g,m_jaw.get_frame(2).toImage(),X_JAW_OFFSET,Y_JAW_OFFSET);
				break;
			default:
				break;
			}

			if (m_current_worm != null)
			{
				m_current_worm.really_render(g);
								
				// render top of head to mask worm exit from top of skull
				
				GfxFrame ff = m_head_top.get_first_frame();
				
				g.drawImage(ff.toImage(),(int)m_x + X_SKULL_OFFSET, (int)m_y + Y_SKULL_OFFSET, null);
				g.drawImage(m_head_top.get_frame(2).toImage(),(int)m_x + X_SKULL_OFFSET + ff.get_width(),
						(int)m_y + Y_SKULL_OFFSET + 10, null);
			}
			if ((m_mouth_fireball != null) && (m_mouth_fireball.get_state() != Projectile.State.DEAD))
			{
				m_mouth_fireball.really_render(g);
			}
			
			switch (m_jaw_index)
			{
			case 2:
				draw_image(g,m_transparent_jaw.get_first_frame().toImage(),32*2,56*2);
				draw_image(g,m_transparent_jaw.get_frame(2).toImage(),32*2,56*2 + 32);
				break;
			}
		}
	}
	
	public class EyeFireBall extends HostileWeapon 
	{
		private double m_speed_x = 0, m_speed_y = 0;
		private int m_speed = 0;
		
		public void init(int speed, int update_rate, int power,
				LevelData level, Hero hero, GfxFrameSet frame_set) 
		{
			super.init(update_rate, power, level, hero, frame_set,AnimatedFrames.Type.FOREVER);
			m_speed = speed;
		}
		
		@Override
		public boolean set_coordinates(double x, double y)
		{
			boolean rval = super.set_coordinates(x,y);
					
			// we use initial angle for fireball angle
			
			int angle = m_params.trajectory.get_initial_angle();
			
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
	
	private void shoot_fireball_eyes(int x_offset, int y_offset)
	{
		EyeFireBall hdw  = new EyeFireBall();

		hdw.init(m_params.get_shoot_speed_value(), 80, 2, m_params.level, m_params.hero, m_eyes_fire_ball);
		
		hdw.set_coordinates(m_x + x_offset, m_y + y_offset);
		
		m_params.weapon_set.add(hdw);
	}


	public void update(long elapsed_time) 
	{
		switch (get_life_state())
		{
		case ALIVE:

			if (m_params.hero.get_life_state() == LifeState.DEAD)
			{
				if (m_current_worm != null)
				{
					m_saved_worm_health = m_current_worm.get_health(true);
					m_current_worm.set_life_state(LifeState.DEAD);
					m_worm_timer = 0;
				}
			}
			
			if ((m_current_worm != null) && 
					(m_current_worm.get_life_state() == LifeState.DEAD))
			{
				// dead or went off bounds: reset timers
				
				
				if (m_current_worm.was_shot())
				{
					m_nb_worms--;

					m_params.health_points = (m_params.max_health_points * m_nb_worms) / MAX_NB_WORMS;

					if (m_params.health_points <= 0)
					{
						// explode
						hit(null);
						m_saved_worm_health = -1;
						
					}
				}
				
				m_current_worm = null;
				m_worm_timer = 0;

			}
			
			switch (m_state)
			{
			case RISE:
				m_y -= elapsed_time * m_y_speed;
				if (m_y < m_y_end)
				{
					m_state = BossState.STAY;
				}
				break;
			case STAY:
				if (m_current_worm == null)
				{
					m_worm_timer += elapsed_time;

					if (m_worm_timer > WORM_PERIOD)
					{
						m_worm_timer -= WORM_PERIOD;

						// send the worm

						if (m_worm_template == null)
						{
							// first time: lookup in base

							Hostile h = m_params.hostile_set.lookup_hostile("boss_worm");

							m_worm_template = h.get_params();
						}

						m_current_worm = (BossLevel4Worm)m_params.hostile_set.spawn_hostile(m_worm_template.clone());
						if (m_saved_worm_health>0)
						{
							// restore previous worm health, so even with a high difficulty
							// level, reuse the current health when spawning the worm
							m_current_worm.set_max_health(m_saved_worm_health);
						}
					}
				}
				m_boss_state_counter += elapsed_time;
				m_fireball_period += elapsed_time;
				
				m_jaw_index = 0;
				
				if (m_fireball_period > FIREBALL_PERIOD)
				{
					m_fireball_period -= FIREBALL_PERIOD;
					
					// shoot 2 fireballs
					
					shoot_fireball_eyes(72,56);
					shoot_fireball_eyes(130,56);
					
				}
				else if (m_fireball_period > FIREBALL_PERIOD - 1500)
				{
					m_jaw_index = (FIREBALL_PERIOD - m_fireball_period) / 300;
					if ((m_jaw_index == 2) && (m_old_jaw_index != m_jaw_index))
					{
						shoot_fireball();
					}
					m_old_jaw_index = m_jaw_index;
				}
				break;
			}
			break;
		case EXPLODING:
				if (m_explosion_timer > 4000)
				{
					super.set_life_state(LifeState.DEAD);
				}
				else
				{
					m_starburst_counter += elapsed_time;
					
					if (m_starburst_counter > 0)
					{
						m_starburst_counter -= 1200;
						
						for (int i = 0; i < 2; i++)
						{
							m_params.hero.get_weapon_set().spawn_stardust((int)(get_x_center() + ((Math.random()-0.5) * m_width*2)), 
									(int)(get_y_center() + (Math.random()-0.5) * m_height*2));
						}
					}
					
				}
				m_explosion_timer += elapsed_time;
				
			break;
			
		}
	}
}
