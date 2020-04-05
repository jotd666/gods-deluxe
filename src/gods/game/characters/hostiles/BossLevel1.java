package gods.game.characters.hostiles;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import gods.base.*;
import gods.game.SfxSet.Sample;
import gods.game.characters.*;
import gods.game.characters.weapons.HeroWeapon;
import gods.game.characters.weapons.HostileWeapon;

public class BossLevel1 extends Hostile implements Boss
{
	@Override
	public boolean is_in_background() {
		
		return false;
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
		int old_points = m_params.health_points;
		boolean rval = super.hit(hw);
		if (old_points != m_params.health_points)
		{
			m_params.sfx_set.play(Sample.weapon_crash);
		}
		
		return rval;
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
	
	public class Boss1FireBall extends HostileWeapon 
	{
		private double m_x_speed;
		private double m_y_speed;
		
		public Boss1FireBall(double speed)
		{
			m_x_speed = speed * 1.7;
			m_y_speed = speed * 0.45;
		}
		
		public void init() 
		{
			super.init(100, 2, m_params.level, m_params.hero,
					m_boss_fire_ball,AnimatedFrames.Type.FOREVER);

		}

		@Override
		protected void move(long elapsed_time) 
		{
			if (is_ground_hit(m_x,m_y + m_height))
			{
				// no more ground hit
				m_y = m_params.level.get_grid().get_rounded_y(m_y + m_height) - m_height;
				// bounce
				m_y_speed = - m_y_speed;
			}
			
			if (is_wall_hit(m_x - m_width, m_y))
			{
				set_state(State.HITTING_WALL);
			}

			hero_collision_test();
			
			m_x -= m_x_speed * elapsed_time;
			m_y += m_y_speed * elapsed_time;

			m_y_speed += 0.001 * elapsed_time;
		}

	}

	@Override
	public void get_bounds(Rectangle r) 
	{
		super.get_bounds(r);
		
		r.x += 16;
		r.width -= 16;
	}

	

	private GfxFrameSet m_frames;
	private GfxFrameSet m_boss_fire_ball;
	private static final int WALK_LIMIT = 10;
	private static final int WALK_STEP = 8;
	private BufferedImage m_current;
		
	private enum BossState { WALK, WALK_BACK, SHOOT, WALK_BACK_HALF }
	private static final BossState [] SEQUENCE = { BossState.WALK, BossState.SHOOT, BossState.SHOOT,
		BossState.WALK_BACK, BossState.SHOOT, BossState.WALK_BACK_HALF, BossState.WALK, BossState.WALK, BossState.SHOOT};
		
	private int m_boss_state_counter = 0;
	private int m_boss_sequence = 0;

	private int m_walk_frame_counter = 1;
	private int m_shoot_frame_counter = WALK_LIMIT;
	



	@Override
	public void init(HostileParameters p) 
	{
		super.init(p);
		
		m_frames = p.level.get_level_palette().lookup_frame_set(p.frame_set_name);
		m_boss_fire_ball = p.level.get_common_palette().lookup_frame_set("boss_fire_ball");
		
		set_life_state(LifeState.ALIVE);
	}

	private int m_explosion_timer = 0;
	private int m_starburst_counter = 0;

	public BossLevel1() 
	{
		// TODO Auto-generated constructor stub
	}

	private void shoot_fireball(double speed)
	{
		Boss1FireBall p = new Boss1FireBall(speed);
		p.init();
		p.set_coordinates(m_x,m_y + 40);
		
		m_params.weapon_set.add(p);

	}

	public void render(Graphics2D g) 
	{
		if (get_life_state() == LifeState.ALIVE)
		{
			draw_image(g,m_current);
		}
		
	}
	
	public void update(long elapsed_time) 
	{
		switch (get_life_state())
		{
		case ALIVE:

			m_boss_state_counter += elapsed_time;

			if (m_boss_state_counter >= 120)
			{
				m_boss_state_counter = 0;

				m_current = m_frames.get_frame(m_walk_frame_counter).toImage();

				if (m_boss_sequence == SEQUENCE.length)
				{
					m_boss_sequence = 0;
				}
				// next frame

				switch (SEQUENCE[m_boss_sequence])
				{

				case WALK:
					if (!m_params.level.is_lateral_way_blocked((int)m_x, (int)m_y, m_height))
					{
						m_x -= WALK_STEP;
					}
					m_walk_frame_counter++;

					if (m_walk_frame_counter > WALK_LIMIT)
					{
						m_walk_frame_counter = 1;
					}
					else if (m_walk_frame_counter == WALK_LIMIT)
					{
						m_boss_state_counter = 0;
						m_boss_sequence++;
						m_params.sfx_set.play(Sample.boss_move,0);
					}
					else if (m_walk_frame_counter == WALK_LIMIT/2)
					{
						m_params.sfx_set.play(Sample.boss_move,1);
					}
					break;

				case WALK_BACK:
					// cannot happen unless boss starts very close to the wall
					
					if (!m_params.level.is_lateral_way_blocked(m_x + m_width, m_y, m_height))
					{
						m_x += WALK_STEP;
					}
					
					m_walk_frame_counter--;
					if (m_walk_frame_counter == 0)
					{
						m_walk_frame_counter = WALK_LIMIT;					
					}
					else if (m_walk_frame_counter == 1)
					{
						m_boss_state_counter = 0;
						m_boss_sequence++;
						m_params.sfx_set.play(Sample.boss_move,1);
					}
					else if (m_walk_frame_counter == WALK_LIMIT/2)
					{
						m_params.sfx_set.play(Sample.boss_move,0);
					}
					break;
				case WALK_BACK_HALF:
					m_x += WALK_STEP;
					m_walk_frame_counter--;
					if (m_walk_frame_counter == 0)
					{
						m_walk_frame_counter = WALK_LIMIT;						
					}
					else if (m_walk_frame_counter == WALK_LIMIT/2)
					{
						m_boss_state_counter = 0;
						m_boss_sequence++;
						m_params.sfx_set.play(Sample.boss_move,0);
					}
					break;
				case SHOOT:
					m_shoot_frame_counter++;
					m_current = m_frames.get_frame(m_shoot_frame_counter).toImage();

					if (m_shoot_frame_counter == m_frames.get_nb_frames() - 1)
					{
						m_params.sfx_set.play(Sample.boss_shoot,0);
						
						// shoot 2 fireballs with slightly different speeds
						shoot_fireball(0.1);
						shoot_fireball(0.11);

					}
					else if (m_shoot_frame_counter == m_frames.get_nb_frames())
					{
						m_shoot_frame_counter = WALK_LIMIT;
						m_boss_sequence++;
						m_walk_frame_counter = 1;
					}
					break;
				}

				
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
