package gods.game.characters.hostiles;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import gods.base.*;
import gods.game.SfxSet.Loop;
import gods.game.SfxSet.Sample;
import gods.game.characters.*;
import gods.game.characters.weapons.HeroWeapon;
import gods.game.characters.weapons.HostileDirectionalWeapon;
import gods.game.characters.weapons.HostileWeapon;
import gods.sys.AngleUtils;

public class BossLevel2 extends FlyingMonster implements Boss
{
	private class Boss2FireBall extends HostileWeapon
	{
		private boolean m_has_hurt_hero = DEBUG_MODE;
		
		@Override
		protected void hero_collision_test() 
		{
			if (!m_has_hurt_hero)
			{
				m_hero.get_bounds(m_work_rectangle);

				if (m_work_rectangle.contains(m_x+m_width/2,m_y+m_height/2))
				{
					m_hero.hurt(m_power);
					m_has_hurt_hero = true;
				}
			}
		}
		private int m_move_timer = 0;
		
		Boss2FireBall(int x, int y)
		{
			set_coordinates(x,y);
			init(80, 1, m_params.level, m_params.hero,
					m_fire_ball,AnimatedFrames.Type.REVERSE);
			
			
		}
		@Override
		protected void move(long elapsed_time) 
		{
			m_move_timer += elapsed_time;
			
			while (m_move_timer > 5)
			{
				m_x--;
				m_y++;
				m_move_timer -= 5;
				
				hero_collision_test();
			}
			
		}
		
	}
	//@Override
	/*public void get_bounds(Rectangle r) 
	{
		super.get_bounds(r);
		
		r.x += 16;
		r.width -= 16;
	}*/
		
	private static final boolean DEBUG_MODE = false;
	
	private static final int SHOOT_PERIOD = 3000;
	private static final int SHOOT_PERIOD_1 = SHOOT_PERIOD/8;
	private static final int SHOOT_PERIOD_2 = SHOOT_PERIOD/4;
	private static final int SHOOT_PERIOD_3 = 3*SHOOT_PERIOD/4;
	private static final int SHOOT_PERIOD_4 = 7*SHOOT_PERIOD/8;
	private static final int NECK_SPEED_FORWARD = 25;
	private static final int NECK_SPEED_BACK = 30;
	
	private static final int MOVE_PERIOD = 3500;
	private static final int MIN_TAIL_ANGLE = 0;
	private static final int MAX_TAIL_ANGLE = 120;
	private static final int TAIL_RADIUS = 80;
	
	private GfxFrameSet m_fire_ball, m_tail_fire_ball;
	private GfxFrame m_body;
	private GfxFrameSet m_head;
	private GfxFrameSet m_tail;
	private GfxFrameSet m_tail_end;
	private GfxFrameSet m_neck;
	private int m_head_angle;
	private int [] m_tail_angle = new int[10];
	private double m_angle_counter = 0;
	private double m_angle_counter_increment = 0.1;
	private int m_head_index = 1;
	private int m_x_head, m_y_head;
	private int [] m_tail_coords = new int[2];
	private BufferedImage m_tail_frame = null;
	private int m_tail_angle_end;
	private int m_second_fireball_timer = 0;
	private int m_move_timer = 0;
	private int m_gem_max_distance;
	private boolean m_tail_active = true;
	private boolean m_fly_sound_playing = false;
	private AnimatedFrames m_wings;
	
	@Override
	public void collision(Hero h) 
	{
		if (!DEBUG_MODE)
		{
			// collision with the boss is lethal, does not hurt him
			h.hurt(Hero.MAX_HEALTH*10);
		}
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
	public void init(HostileParameters p) 
	{
		super.init(p);
		
		GfxPalette palette = p.level.get_level_palette();
		m_body = palette.lookup_frame_set("boss_body").get_first_frame();
		GfxFrameSet wings = palette.lookup_frame_set("boss_wings");
		m_head = palette.lookup_frame_set("boss_head");
		m_tail = palette.lookup_frame_set("boss_tail");
		m_tail_end = palette.lookup_frame_set("boss_tail_end");
		m_neck = palette.lookup_frame_set("boss_neck");
		m_fire_ball = palette.lookup_frame_set("boss_fireball");
		m_tail_fire_ball = p.level.get_common_palette().lookup_frame_set("enemy_fireball");
		
		m_wings = new AnimatedFrames();
		m_wings.init(wings, 80, AnimatedFrames.Type.BACK_AND_FORTH);
		
		set_life_state(LifeState.ALIVE);
		
		for (int i = 0; i < m_tail_angle.length; i++)
		{
			m_tail_angle[i] = MIN_TAIL_ANGLE;
		}
		
		// compute min and max x and y (without the gem)
		
		compute_y_limit(-1);
		compute_y_limit(1);
		compute_x_limit(-1);
		compute_x_limit(1);
				
		m_aim.set_coordinates(m_x, m_y);
		
		// draw back the monster
		
		int w = m_params.level.get_view_bounds().width;
		
		m_x += w;
		
		m_gem_max_distance = w * w;
		
	}


	private void compute_y_limit(int direction)
	{
		int idx = 1;
		int y_offset = 0;
		int y_current = get_y() - m_head.get_height();
		int w = m_width + m_head.get_width();
		
		int y_step = m_params.level.get_grid().get_tile_height() * direction;
		
		if (direction > 0)
		{
			y_offset = m_height + m_head.get_height();
			idx = 0;
		}
		while (true)
		{
			if (m_params.level.is_vertical_way_blocked((int)m_x, y_current + y_offset, w))
			{
				break;
			}
			
			y_current += y_step;
		}
		
		m_y_limit[idx] = y_current + 2 * y_step * direction;
	}
	private void compute_x_limit(int direction)
	{
		int idx = 0;		
		int x_current = (int)m_x;
		int h = m_height + m_head.get_height();
		int x_offset = 0;
		
		int x_step = m_params.level.get_grid().get_tile_width() * direction;
		
		if (direction > 0)
		{
			x_offset = m_width + m_head.get_width();
			idx = 1;
		}
		while (true)
		{
			if (m_params.level.is_lateral_way_blocked(x_current + x_offset, (int)m_y, h))
			{
				break;
			}
			
			x_current += x_step;
		}
		
		m_x_limit[idx] = x_current + 2 * x_step * direction;
	}
	
	private int [] m_y_limit = new int[2];
	private int [] m_x_limit = new int[2];
	
	private int m_explosion_timer = 0;

	private int m_fire_timer = 0;
	private int m_fire_counter = -1;
	
	
	public BossLevel2() 
	{
		
	}

	private void shoot_fireball()
	{
		m_params.sfx_set.play(Sample.boss_shoot,1);
		Boss2FireBall p = new Boss2FireBall(m_x_head - 10,m_y_head + 46);
		m_params.weapon_set.add(p);
	}

	private void shoot_fireball_tail()
	{
		HostileDirectionalWeapon hdw  = new HostileDirectionalWeapon();

		hdw.init(m_params.get_shoot_speed_value(), 50, 2, m_params.level, m_params.hero, m_params.hero,
				m_tail_fire_ball);
		
		compute_tail_coords(m_tail_frame.getWidth(),m_tail_frame.getHeight(),TAIL_RADIUS, m_tail_angle_end);
		
		hdw.set_coordinates(m_tail_coords[0],m_tail_coords[1]);
		m_params.weapon_set.add(hdw);
	}
	
		
	
	
	private void render_neck(Graphics2D g, int radius)
	{
		BufferedImage img = m_neck.toImage();
		int x_head = (int)(m_x + 54 + radius * AngleUtils.cosd(m_head_angle)) - img.getWidth()/2;
		int y_head = (int)(m_y - radius * AngleUtils.sind(m_head_angle)) - img.getHeight()/2;
		g.drawImage(img, x_head, y_head, null);
	
	}
	
	
	private void compute_tail_coords(int width, int height, int radius, int tail_angle)
	{
		m_tail_coords[0] = (int)(m_x + 128 + radius * AngleUtils.cosd(tail_angle)) - width/2;
		m_tail_coords[1] = (int)(m_y + 70 + radius * AngleUtils.sind(tail_angle)) - height/2;
		
	}
	private void render_tail(Graphics2D g, BufferedImage img, int radius, int tail_angle)
	{
		
		compute_tail_coords(img.getWidth(),img.getHeight(),radius, tail_angle);
		
		g.drawImage(img, m_tail_coords[0], m_tail_coords[1], null);
		
	}
	
	public void render(Graphics2D g) 
	{
		if (get_life_state() == LifeState.ALIVE)
		{
			m_wings.get_bounds(m_work_rectangle);
			m_wings.set_coordinates(m_x + 66, m_y - m_work_rectangle.height);
			
			m_wings.render(g);
			
			GfxFrame head = m_head.get_frame(m_head_index);
			g.drawImage(head.toImage(), m_x_head, m_y_head, null);

			
			for (int i = 1; i  < 4; i++)
			{
				render_neck(g,14*i);
			}
			
			draw_image(g,m_body.toImage());
			
			if (m_tail_active)
			{
				for (int i = 0; i  < m_tail_angle.length; i++)
				{
					render_tail(g,m_tail.toImage(),(TAIL_RADIUS*(i+1))/m_tail_angle.length,m_tail_angle[i]);
				}
				int tail_angle = m_tail_angle[m_tail_angle.length-1];

				if (m_tail_frame != null)
				{
					render_tail(g,m_tail_frame,TAIL_RADIUS,tail_angle);
				}
			}
		}
	}

	
	public void update(long elapsed_time) 
	{

		switch (get_life_state())
		{
		case ALIVE:
			if (!m_fly_sound_playing)
			{
				m_fly_sound_playing = true;
				m_params.sfx_set.play(Loop.boss_2_fly);
			}
			// move
			
			m_move_timer += elapsed_time;
			if ((m_fire_counter < 0) && (m_move_timer > MOVE_PERIOD))
			{
				// recompute new location to go to
				
				m_move_timer -= MOVE_PERIOD;
				
				// check if dragon gem has been dropped nearby
				
				int x_min = m_x_limit[0];
				
				GfxObject gem = m_params.level.get_bonus("dragon_gem");
				if (gem != null)
				{
					if ((gem.is_visible() && gem.square_distance_to(this) < m_gem_max_distance))
					{
						x_min = (int)gem.get_x() + gem.get_width();
					}
				}
				int new_x = (int)(Math.random() * (m_x_limit[1] - x_min)) + x_min;
				int new_y = (int)(Math.random() * (m_y_limit[1] - m_y_limit[0])) + m_y_limit[0];
				
				m_aim.set_coordinates(new_x, new_y);
			}
			move_to_location(m_aim,elapsed_time,true,false);
			
			// tail disappears when boss energy is low
			
			if (get_health(true) < get_max_health() / 6)
			{
				m_tail_active = false;
			}
			// animate
			
			int radius = 4*14;
			m_x_head = (int)(m_x + 20 + radius * AngleUtils.cosd(m_head_angle)) - m_head.toImage().getWidth()/2;
			m_y_head = (int)(m_y + 10 - radius * AngleUtils.sind(m_head_angle)) - m_head.toImage().getHeight()/2;

			m_wings.update(elapsed_time);
			
			if (m_tail_active)
			{
				m_angle_counter += m_angle_counter_increment * elapsed_time;

				if ((m_angle_counter > MAX_TAIL_ANGLE) || (m_angle_counter < MIN_TAIL_ANGLE))
				{
					if (m_angle_counter_increment > 0)
					{
						// shoot fireball
						shoot_fireball_tail();

						m_second_fireball_timer = 250;
					}
					m_angle_counter_increment *= -1; 
					if (m_angle_counter > MAX_TAIL_ANGLE)
					{
						m_angle_counter = MAX_TAIL_ANGLE;
					}
					else
					{
						m_angle_counter = MIN_TAIL_ANGLE;
					}
					
					m_angle_counter += m_angle_counter_increment;
				}
				for (int i = 0; i  < m_tail_angle.length - 1; i++)
				{
					// scroll angle values

					m_tail_angle[i] = m_tail_angle[i+1];				
				}

				if (m_second_fireball_timer > 0)
				{
					m_second_fireball_timer -= elapsed_time;
					if (m_second_fireball_timer <= 0)
					{
						// shoot fireball
						shoot_fireball_tail();
					}
				}
				// compute new master angle

				m_tail_angle[m_tail_angle.length-1] = (int)m_angle_counter; 



				int tail_nb_frames = m_tail_end.get_nb_frames();
				m_tail_angle_end = m_tail_angle[m_tail_angle.length-1];

				int tail_end_frame = (((tail_nb_frames * (m_tail_angle_end*2)) / 360) + tail_nb_frames/2) % tail_nb_frames;

				if (tail_end_frame < 0)
				{
					tail_end_frame += tail_nb_frames;
				}
				m_tail_frame = m_tail_end.get_frame(tail_end_frame+1).toImage();
			}
			
			m_head_angle = 135;
			
			if (!is_moving())
			{
				m_fire_timer += elapsed_time;
				if (m_fire_timer > SHOOT_PERIOD)
				{
					m_fire_counter = 0;
					m_fire_timer = 0;
					m_head_index = 2;

				}

				if (m_fire_timer < SHOOT_PERIOD_1)
				{
					m_head_angle += m_fire_timer/NECK_SPEED_BACK;
				}
				else if (m_fire_timer < SHOOT_PERIOD_2)
				{
					m_head_angle += (2*SHOOT_PERIOD_1-m_fire_timer)/NECK_SPEED_BACK;
				}
				else if (m_fire_timer > SHOOT_PERIOD_3)
				{
					m_head_angle += -(m_fire_timer-SHOOT_PERIOD_3)/NECK_SPEED_FORWARD;
				}
				else if (m_fire_timer > SHOOT_PERIOD_4)
				{
					m_head_angle += (2*SHOOT_PERIOD_4 - m_fire_timer)/NECK_SPEED_FORWARD;
				}
				if (m_fire_counter >= 0)
				{
					int fire_counter = m_fire_timer / 50;
					if (fire_counter == m_fire_counter)
					{
						m_fire_counter++;
						shoot_fireball();

						switch (m_fire_counter)
						{
						case 3:
							m_head_index = 3;
							break;
						case 6:
							m_head_index = 2;
							break;
						case 8:
							m_fire_counter = -1;
							m_head_index = 1;
							break;
						}
					}
				}
			}
			
			break;
		case EXPLODING:
			if (m_fly_sound_playing)
			{
				m_fly_sound_playing = false;
				m_params.sfx_set.pause(Loop.boss_2_fly);
			}
			if (m_explosion_timer > 100)
			{
				super.set_life_state(LifeState.DEAD);
			}
			else
			{
				if (m_explosion_timer % 30 == 0)
				{
					for (int i = 0; i < 3; i++)
					{
						m_params.hero.get_weapon_set().spawn_stardust((int)(get_x_center() + 
								((Math.random()-0.5) * m_width*2)), 
								(int)(get_y_center() + (Math.random()-0.5) * m_height*2));
					}
				}

			}
			m_explosion_timer++;

			break;
			
		}
	}
}
