package gods.game.characters;

import gods.base.*;
import gods.base.layer.Sector;
import gods.game.characters.weapons.HostileDirectionalWeapon;
import gods.game.characters.weapons.HostileStraightWeapon;
import gods.game.characters.weapons.Projectile;

import java.util.*;
import java.awt.Graphics2D;

public abstract class LeftRightHostile extends Hostile 
{
	protected int m_right_left = 0;
	protected GfxFrameSet [] m_fire_frames;
	protected GfxFrameSet [] m_frame_set;
	protected GfxFrameSet m_appearing_frame_set;
	protected GfxFrameSet m_exploding_frame_set;
	protected int m_max_theft_object_distance;
	protected int [] m_stolen_object_xy_offset = null;
	protected int m_frame_counter;
	protected Jump m_jump = null;
	protected int m_shoot_counter = 0;

	private int m_segment_age = 0;
	private int m_timer = 0;
	protected int m_animation_timer = 0;
	
	protected int m_shoot_period;
	
	private int m_appearing_counter = 0;
	private boolean m_appearing_sound_played = false;
	private static final int APPEARING_FRAME_LIMIT_2 = 9;
	private static final int APPEARING_FRAME_LIMIT_3 = 11;
	private static final int APPEARING_FRAME_LIMIT_1 = 14;
	
	protected static final int ANIMATION_FRAME_RATE = 100;
	protected static final int APPEARING_ANIMATION_FRAME_RATE = 50;


	
	protected boolean has_trajectory()
	{
		return !m_params.trajectory.items().isEmpty();
	}
	protected void set_jump(Jump j)
	{
		m_jump = j;
	}
	
	private Iterator<TrajectorySegment> m_path_iterator;
	protected TrajectorySegmentConstant m_current_path = null;
	


	protected abstract class Jump
	{
		abstract boolean update(long elapsed_time);
		abstract void end();
	}
	

	protected abstract void move(long elapsed_time);
	
	protected Projectile shoot() 
	{
		boolean to_left = m_right_left != 0;
		int x = get_x() + (to_left ? 0 : m_width);
		Projectile p = null;
		int speed = m_params.get_shoot_speed_value();

		switch (m_params.fire_type)
		{
		case None:
			break;
		case Directional:
		case Fuzzy:
		{		
			HostileDirectionalWeapon hdw  = new HostileDirectionalWeapon();

			Locatable aim = new Locatable();
			aim.set_coordinates(m_params.hero);

			
			if (m_params.fire_type == HostileWaveParameters.FireType.Fuzzy)
			{
				// add randomness (easier for the player)
				// original game computes a random point in a 64x64 square
				// centered on the player
				int rndx = (int)((Math.random()-0.5)*128);
				int rndy = (int)((Math.random()-0.5)*128);
				aim.add_x(rndx);
				aim.add_y(rndy);
			}
			
			hdw.init(speed, 50, 2, m_params.level, m_params.hero, aim, m_fire_frames[0]);
			p = hdw;
			p.set_coordinates(this,true);
			break;			
		}
		case Straight:
		{			
			HostileStraightWeapon hsw = new HostileStraightWeapon();
			hsw.init(speed,50,2,to_left,
					m_params.level,m_params.hero,m_fire_frames[m_right_left]);
			p = hsw;
			p.set_coordinates(x,m_y);
			break;
		}

		}
		if (p != null)
		{
			m_params.weapon_set.add(p);
		}

		return p;

	}	
	protected Jump create_jump()
	{
		return null;
	}
	
	protected void trajectory_change()
	{
		
	}
	

	protected void steal_objects_on_the_way()
	{
		if ((m_params.steal_on_the_way) && (m_params.object_held == null))
		{
			int d = m_max_theft_object_distance;
			GfxObject closest_object = null;
			
			Sector<GfxObject>.Distance closest_bonus = m_params.level.bonus_sector_set.closest(this);
			if (closest_bonus.item != null)
			{
				if (closest_bonus.square_distance < d)
				{
					d = closest_bonus.square_distance;

					closest_object = closest_bonus.item;
				}
			}

			Sector<GfxObject>.Distance closest_item = m_params.level.items_sector_set.closest(this);
			if (closest_item.item != null)
			{
				if (closest_item.square_distance < d)
				{
					d = closest_item.square_distance;
					closest_object = closest_item.item;
				}
			}
			if (d < m_max_theft_object_distance)
			{
				get_bounds(m_work_rectangle);
				if (m_work_rectangle.contains(closest_object.get_x_center(),closest_object.get_y_center()))
				{
					// steal the item

					m_params.level.remove_object(closest_object);
					
					m_params.object_held = closest_object;
				}
			}
		}
	}
	
	protected GfxFrameSet [] get_left_right_frame_set(GfxPalette palette, String name)
	{
		GfxFrameSet [] rval = new GfxFrameSet[2];
		
		rval[0] = palette.lookup_frame_set(name);
		rval[1] = palette.get_left_frame_set(rval[0]);
		
		return rval;
	}
	
	protected boolean next_path()
	{
		boolean rval = (m_path_iterator.hasNext());
		
		if (rval)
		{
			m_current_path = m_path_iterator.next();
			
			// special case of the loopback
			
			if (m_current_path.get_type() == HostileTrajectory.Type.Loop_Back)
			{
				m_path_iterator = m_params.trajectory.items().iterator();
				m_current_path = m_path_iterator.next();
			}
			trajectory_change();
			
		}
		
		return rval;
	}
	@Override
	public void init(HostileParameters p)
	{
		super.init(p);
		
		GfxPalette common_palette = m_params.level.get_common_palette();
		GfxPalette level_palette = m_params.level.get_level_palette();
			
		m_frame_set = get_left_right_frame_set(level_palette, m_params.frame_set_name);
		
		m_path_iterator = m_params.trajectory.items().iterator();
		
		m_appearing_counter = 0;
		
		m_width = m_frame_set[0].get_width();
		m_height = m_frame_set[0].get_height();
	
		 // we pick only from frame 17 to 10
		m_appearing_frame_set = common_palette.lookup_frame_set("death");
		
		m_exploding_frame_set = common_palette.lookup_frame_set("ground_enemy_death");
		
		
		m_appearing_counter = m_appearing_frame_set.get_nb_frames();

		m_frame_counter = 1;
		
		
		switch (m_params.direction)
		{
			case Left:
				m_right_left = 1;
				break;
			case Right:
				m_right_left = 0;
				break;
			case Random:
				m_right_left = Math.random() > 0.5 ? 0 : 1;
				break;
		}		
		// shoot period is multiple of move frames
		
		m_shoot_period = m_params.get_shoot_period_value();
		
		// compute the frame offset so hostiles of a same wave don't fire at the same
		// time but with a shift
		
		if (m_params.total_nb_hostiles_of_wave > 1)
		{
			m_shoot_counter = (m_shoot_period * m_params.index);

			m_shoot_period *= m_params.total_nb_hostiles_of_wave;
		}
		else
		{
			m_shoot_counter = 0;
		}
		
		next_path();
		
		switch (p.fire_type)
		{
		case None:
			break;
		case Directional:
		case Fuzzy:
			m_fire_frames = new GfxFrameSet[2];
			m_fire_frames[0] = common_palette.lookup_frame_set("enemy_fireball");
			m_fire_frames[1] = m_fire_frames[0]; // no symmetry required
			break;
		case Straight:
			m_fire_frames = get_left_right_frame_set(common_palette,"enemy_straight_shoot");
			break;
		}
				
		int w = p.level.get_view_bounds().width;
		
		m_max_theft_object_distance = w * w;
	}
	
	protected boolean is_jumping()
	{
		return m_jump != null;
	}
	protected int get_walk_direction()
	{
		return ((m_right_left == 0) ? 1 : -1);
	}
	
	protected void set_right_left(int new_value)
	{
		m_right_left = new_value;
	}

	public void set_life_state(LifeState s)
	{
		super.set_life_state(s);
		if (s == LifeState.EXPLODING)
		{
			m_frame_counter = 1;
			m_params.compute_object_to_drop();
		}
	}

	
	private void handle_path(long elapsed_time)
	{
		if (m_current_path != null)
		{
			int duration = m_current_path.get_duration();
			
			// duration = 0 means forever: next_path() must be triggered by hostile itself
			
			if (duration > 0)
			{
				m_segment_age += elapsed_time;

				if (m_segment_age > duration)
				{
					m_segment_age = 0;
					next_path();
				}
			}
		}

	}
	protected void on_land()
	{
		// do nothing
	}

	private void handle_jump(long elapsed_time)
	{
		if (m_jump == null)
		{
			if ((m_current_path != null) && (m_current_path.get_type() == HostileTrajectory.Type.Jump_From))
			{
				// force jump when encountering a zone
				get_bounds(m_work_rectangle);
				get_bounds(m_work_rectangle_2);
				m_current_path.get_location().get_bounds(m_work_rectangle_2);
				if (m_work_rectangle.intersects(m_work_rectangle_2))
				{
					m_jump = create_jump();
					if (!next_path())
					{
						m_current_path = null;
					}
				}
			}
		}
		else
		{
			if (m_jump.update(elapsed_time))
			{
				// jump is over
				m_jump = null;
				
				on_land();
			}
		}
	}
	public void update(long elapsed_time) 
	{		
		switch (m_life_state)
		{
		case DELAYED:
			if (!m_frozen)
			{
				m_params.appearing_delay -= elapsed_time;

				if (m_params.appearing_delay <= 0)
				{
					if (m_appearing_animation)
					{
						m_life_state = LifeState.APPEARING;							
					}
					else
					{
						m_life_state = LifeState.ALIVE;
						m_appearing_counter = APPEARING_FRAME_LIMIT_2;  // no appearing animation
						m_frame_counter = 1;
					}
				}
			}
			break;
		case ALIVE:
			if ((is_in_screen(m_width) || !m_params.created_at_level_start))
			{
				boolean move_it = m_appearing_counter < APPEARING_FRAME_LIMIT_3;
				
				m_timer += elapsed_time;
				while (m_timer > APPEARING_ANIMATION_FRAME_RATE)
				{
					m_timer -= APPEARING_ANIMATION_FRAME_RATE;
					
					// appearing animation is the end of death animation
					// played reverse
					
					if (m_appearing_counter > APPEARING_FRAME_LIMIT_2)
					{
						m_appearing_counter--;
					}
				}
				if (!m_frozen && move_it)
				{
					handle_path(elapsed_time);

					if (!is_jumping())
					{
						// CASE FALL TO HANDLE
						move(elapsed_time);
					}

					handle_jump(elapsed_time);

					if (m_shoot_counter >= m_shoot_period)
					{
						// shoot if shoot enabled
						m_shoot_counter = 0;

						if (may_shoot())
						{
							shoot();
						}
					}

					m_shoot_counter += elapsed_time;

					animate(elapsed_time);

					GfxObject owned_item = m_params.object_held;

					if (owned_item != null)
					{
						// position the object, centered/bottom by default

						int x = get_x() - owned_item.get_width()/2;
						int y = get_y() + m_height-owned_item.get_height();

						if (m_stolen_object_xy_offset != null)
						{
							int idx = (m_frame_counter-1) * 2;

							int x_offset = m_stolen_object_xy_offset[idx];
							int y_offset = m_stolen_object_xy_offset[idx+1];

							y += y_offset;

							if (m_right_left == 0)
							{
								x += x_offset;
							}
							else
							{
								x += m_width - x_offset;
							}

						}
						else
						{
							x += m_width/2;
						}

						owned_item.set_coordinates(x, y);
					}
				}
			}
			break;

		case APPEARING:
			if (!m_appearing_sound_played /*&& is_in_screen(m_width*2)*/)
			{
				m_params.sfx_set.play_random(get_appearing_sound());
				m_appearing_sound_played = true;
			}

			m_timer += elapsed_time;
			while (m_timer > APPEARING_ANIMATION_FRAME_RATE)
			{
				m_timer -= APPEARING_ANIMATION_FRAME_RATE;
				
				// appearing animation is the end of death animation
				// played reverse
				
				if (m_appearing_counter > APPEARING_FRAME_LIMIT_1)
				{
					m_appearing_counter--;
				}
				else
				{
					m_life_state = LifeState.ALIVE;
				}
			}
			break;
		case EXPLODING:
			m_timer += elapsed_time;
			while (m_timer > ANIMATION_FRAME_RATE)
			{
				m_timer -= ANIMATION_FRAME_RATE;
				m_frame_counter++;
				if (m_frame_counter > m_exploding_frame_set.get_nb_frames())
				{
					m_life_state = LifeState.DEAD;
				}
			}
			
			break;
		}

		
	}
	
	protected void animate(long elapsed_time)
	{
		m_animation_timer += elapsed_time;
		
		while (m_animation_timer > ANIMATION_FRAME_RATE)
		{
			m_animation_timer -= ANIMATION_FRAME_RATE;

			m_frame_counter ++;

			if (m_frame_counter > m_params.nb_move_frames)
			{
				m_frame_counter = 1;
			}
		}
		
	}
	public void render(Graphics2D g) 
	{
		GfxFrame gf_moving = null;
		GfxFrame gf_appear = null;
	
		switch (m_life_state)
		{
		case ALIVE:
		case APPEARING:
			if (m_appearing_counter < APPEARING_FRAME_LIMIT_1)
			{
				// display the hostile
				
				gf_moving = m_frame_set[m_right_left].get_frame(m_frame_counter);
			}
			if (m_appearing_counter > APPEARING_FRAME_LIMIT_2)
			{
				// display appearing animation
				
				gf_appear = m_appearing_frame_set.get_frame(m_appearing_counter);
			}
			if (m_params.debug_mode)
			{
				get_bounds(m_work_rectangle);
				g.setColor(java.awt.Color.RED);
				g.drawRect(m_work_rectangle.x, m_work_rectangle.y, 
						m_work_rectangle.width,
						m_work_rectangle.height);
			}
		break;
			
		case EXPLODING:
			gf_moving = m_exploding_frame_set.get_frame(m_frame_counter);
			break;
		}
		
		if (gf_moving != null)
		{
			draw_image(g,gf_moving.toImage());
		}
		if (gf_appear != null)
		{
			g.drawImage(gf_appear.toImage(), (int)m_appearing_x, (int)m_appearing_y, null);
		}
		
		GfxObject owned_item = m_params.object_held;

		if (owned_item != null)
		{
			// render the object

			owned_item.draw_image(g, owned_item.toImage());
		}
	}

}
