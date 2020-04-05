package gods.game.characters.hostiles;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import gods.base.*;
import gods.game.ParabolicMobileBehaviour;
import gods.game.SfxSet.Sample;
import gods.game.characters.*;
import gods.game.characters.weapons.HeroWeapon;
import gods.game.characters.weapons.HostileWeapon;


public class BossLevel3 extends Hostile implements Boss
{
	private static final int WALK_UPDATE_RATE = 120;
	
	@Override
	public boolean is_in_background() {
		
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
	
	public class Boss3FireBall extends HostileWeapon 
	{
		private double m_x_speed;
		private double m_y_speed;
		private double m_y_limit;
		
		public Boss3FireBall(double speed)
		{
			m_x_speed = speed * 2;
			m_y_speed = speed * 0.45;
		}
		
		public void init() 
		{
			super.init(200, 5, m_params.level, m_params.hero,
					m_boss_fire_ball,AnimatedFrames.Type.FOREVER);

			m_y_limit = BossLevel3.this.get_y() + BossLevel3.this.get_height();
		}

		@Override
		protected void move(long elapsed_time) 
		{			

			hero_collision_test();
			
			m_x -= m_x_speed * elapsed_time;
			m_y += m_y_speed * elapsed_time;

			m_y_speed += 0.001 * elapsed_time;
			
			if (m_y > m_y_limit)
			{
				set_state(State.DEAD);
			}
		}

	}

	@Override
	public void get_bounds(Rectangle r) 
	{
		super.get_bounds(r);
		
		r.x += 16;
		r.width -= 16;
	}

	

	private GfxFrameSet [] m_frames = new GfxFrameSet[2];
	private GfxFrame [] m_dead_frames = new GfxFrame[2];
	private int m_y_dead;
	private GfxFrameSet m_boss_fire_ball;
	private static final int WALK_LIMIT = 8;
	private static final int SHOOT_LIMIT = 11;
	private static final int WALK_STEP = 8;
	private BufferedImage m_current;
	private ParabolicMobileBehaviour m_parabol = new ParabolicMobileBehaviour();
	private BossState m_old_state = null;
	private boolean m_turn_back_on_land = false;
	
	private enum BossState { WALK, SHOOT, WALK_BACK_HALF, WALK_HALF, JUMP, TURN_BACK }
	private static final BossState [] SEQUENCE = { BossState.JUMP, 
		BossState.TURN_BACK, BossState.SHOOT, BossState.JUMP,   
		BossState.TURN_BACK, BossState.WALK, BossState.SHOOT, BossState.WALK_HALF, 
		BossState.SHOOT, BossState.WALK, BossState.SHOOT, BossState.WALK_BACK_HALF,
		BossState.WALK_HALF, BossState.SHOOT, BossState.WALK_HALF,
		BossState.JUMP, 
		BossState.SHOOT, BossState.JUMP, BossState.SHOOT, BossState.JUMP,
		BossState.TURN_BACK, BossState.WALK, BossState.SHOOT, BossState.WALK_HALF, 
		BossState.SHOOT, BossState.WALK, BossState.SHOOT, BossState.WALK_BACK_HALF,
		BossState.WALK_HALF, BossState.SHOOT, BossState.WALK_HALF };
	
	
	private int m_boss_state_counter = 0;
	private int m_sequence = 0;
	private int m_right_left;
	
	private int m_walk_frame_counter = 1;
	private int m_shoot_frame_counter = WALK_LIMIT;
	private int m_jump_height;
	
	@Override
	public void init(HostileParameters p) 
	{
		super.init(p);
		GfxPalette level_palette = p.level.get_level_palette();
		
		m_right_left = (p.direction == HostileWaveParameters.Direction.Left) ? 1 : 0;
				
		GfxFrameSet gfs = level_palette.lookup_frame_set(p.frame_set_name);
		m_frames[1] = gfs;
		m_frames[0] = new GfxFrameSet(gfs,GfxFrame.SymmetryType.mirror_left);
		
		m_dead_frames[1] = level_palette.lookup_frame_set("boss_dead").get_first_frame();
		m_dead_frames[0] = new GfxFrame(m_dead_frames[1],GfxFrame.SymmetryType.mirror_left);
		
		m_boss_fire_ball = level_palette.lookup_frame_set("boss_fire_ball");
		
		m_jump_height = (9 * m_params.level.get_grid().get_tile_height()) / 2;
		
		m_y_dead = p.location.get_y();
		
		set_life_state(LifeState.ALIVE);
	}

	private int m_explosion_timer = 0;
	private int m_starburst_counter = 0;

	public BossLevel3() 
	{
		
	}

	private void shoot_fireball(double speed, int walk_direction)
	{
		Boss3FireBall p = new Boss3FireBall(speed * walk_direction);
		p.init();
		p.set_coordinates(m_x + (walk_direction > 0 ? m_width : 0),m_y + 40);
		
		m_params.weapon_set.add(p);

	}

	public void render(Graphics2D g) 
	{
	
		draw_image(g,m_current);
		
		
	}
	private final int get_walk_direction()
	{
		return ((m_right_left == 0) ? 1 : -1);
	}
	
	private final void move_noise()
	{
		m_params.sfx_set.play(Sample.boss_move,0);
	}
	private final void walk(int sign)
	{
		int wd = sign * get_walk_direction();
		int x_limit = (int)(wd > 0 ? m_x + m_width : m_x);
		
		// only slight check: boss can go through the first platform
		
		if (!m_params.level.is_lateral_way_blocked(x_limit, (int)m_y, 1))
		{
			m_x += WALK_STEP * wd;
		}
	}
	
	private void next_sequence()
	{
		
		m_sequence++;
		
		if (m_sequence == SEQUENCE.length)
		{
			m_sequence = 0;
		}		
	}
	public void update(long elapsed_time) 
	{
		
		switch (get_life_state())
		{
		case ALIVE:
			BossState old_state = m_old_state;
			
			m_boss_state_counter += elapsed_time;

			if (m_boss_state_counter >= WALK_UPDATE_RATE)
			{
				m_boss_state_counter -= WALK_UPDATE_RATE;

				m_current = m_frames[m_right_left].get_frame((m_walk_frame_counter-1) % WALK_LIMIT + 1).toImage();

				// next frame
				old_state = SEQUENCE[m_sequence];

				
				switch (old_state)
				{

				case TURN_BACK:
					m_right_left = 1 - m_right_left;
					next_sequence();
					break;
				case WALK:
					
					walk(1);
					
					m_walk_frame_counter++;

					if (m_walk_frame_counter > WALK_LIMIT * 2)
					{
						m_walk_frame_counter = 1;
					}
					else if (m_walk_frame_counter == WALK_LIMIT * 2)
					{
						m_boss_state_counter = 0;
						next_sequence();
						move_noise();
					}
					else if (m_walk_frame_counter == WALK_LIMIT)
					{
						m_params.sfx_set.play(Sample.boss_move,1);
					}
					break;

				case WALK_BACK_HALF:
					walk(-1);
					
					m_walk_frame_counter--;
					switch (m_walk_frame_counter)
					{
					case 0:
					
						m_walk_frame_counter = WALK_LIMIT * 2;						
						break;
					case WALK_LIMIT:
					{
						m_boss_state_counter = 0;
						next_sequence();
						move_noise();
					}
					break;
					case WALK_LIMIT / 2:
						move_noise();
						break;

					}
					break;
					case WALK_HALF:
					walk(1);
					
					switch (m_walk_frame_counter)
					{
					case WALK_LIMIT * 2:
					{
						m_walk_frame_counter = 0;						
					}
					break;
					case WALK_LIMIT:
					{
						m_boss_state_counter = 0;
						next_sequence();
						move_noise();
					}
					break;
					case WALK_LIMIT/2:
						move_noise();
						break;
					}
					m_walk_frame_counter++;

					break;
				case SHOOT:
					m_shoot_frame_counter++;
					m_current = m_frames[m_right_left].get_frame(m_shoot_frame_counter).toImage();

					if (m_shoot_frame_counter == m_frames[m_right_left].get_nb_frames() - 1)
					{
						m_params.sfx_set.play(Sample.boss_shoot,1);
						
						// shoot fireball
						shoot_fireball(-0.1, get_walk_direction());

					}
					else if (m_shoot_frame_counter == m_frames[m_right_left].get_nb_frames())
					{
						m_shoot_frame_counter = WALK_LIMIT;
						next_sequence();
						m_walk_frame_counter = 1;
					}
					break;
				}

				m_old_state = old_state;
			}
		
			

			if (SEQUENCE[m_sequence] == BossState.JUMP)
			{
				m_current = m_frames[m_right_left].get_frame(SHOOT_LIMIT+1).toImage();
				int direction = get_walk_direction();

				if (m_old_state != BossState.JUMP)
				{
					// state change

					if (!m_parabol.is_active())
					{
						m_parabol.init((int)m_x, (int)m_y, direction, 
								m_width * 2, m_jump_height, 0, 0);
					}
				}
				else
				{
					m_parabol.update(Math.max(1, elapsed_time / 8.0));
					m_y = m_parabol.get_y();
					
					int x_limit = (int)(get_walk_direction() > 0 ? m_parabol.get_x() + m_width : m_parabol.get_x());
					
					m_turn_back_on_land = m_params.level.is_lateral_way_blocked(x_limit, (int)m_y, 1);
					
					if (!m_turn_back_on_land &&
							!m_params.level.is_lateral_way_blocked(x_limit, (int)m_y + m_height - 1, 1))
					{
						m_x = m_parabol.get_x();
					}
					
					if (!m_parabol.is_active())
					{
						// land
						
						move_noise();
						
						// make everything shake
						
						m_params.hero.set_y(m_params.hero.get_y() - m_params.level.get_grid().get_tile_width()/2);
						
						if (m_turn_back_on_land)
						{
						// move to next "turn back"
						while (SEQUENCE[m_sequence] != BossState.TURN_BACK)
						{
							next_sequence();
						}
						}
						else
						{
							next_sequence();
						}
						
						m_turn_back_on_land = false;
						
						int y_round = m_params.level.get_grid().get_rounded_y(m_y+m_height,false);
						m_y = y_round - m_height;
					}
				}
			}

			break;
		case EXPLODING:
			
			m_current = m_dead_frames[m_right_left].toImage();
			
				if (m_explosion_timer > 4000)
				{
					//super.set_life_state(LifeState.DEAD);
				}
				else
				{
					set_y(m_y_dead);
					
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
