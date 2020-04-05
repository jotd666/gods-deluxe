package gods.game.characters;

import gods.base.*;
import gods.game.MonsterLayer;

import java.awt.Graphics2D;

public class Familiar extends LivingCharacter 
{
	private int m_health;
	
	@Override
	public int get_health(boolean instantaneous) {
		return m_health;
	}


	@Override
	public int get_max_health()
	{
		return 1000;
	}

	public void add_health(int h)
	{
		m_health += h;
		if (m_health>get_max_health())
		{
			m_health = get_max_health();
		}
	}

	@Override
	public void handle_health(long elapsed_time) {
		
		
	}


	public String get_name() 
	{
		return "familiar";
	}
	public boolean is_named()
	{
		return true;
	}
	
	private int m_right_left = 0;
	private GfxFrameSet [] m_simple_frame_set;
	private GfxFrameSet [] m_armoured_frame_set;
	private GfxFrameSet [] m_frame_set;
	private int m_nb_move_frames;
	private boolean m_power_wings = false;
	private int m_claws_power = 1;
	
	private long m_slow_y_move_timer = 0;
	protected int m_frame_counter = 0;
	protected int m_frame_increment = 1;
	
	private int m_animation_timer = 0;
		
	private Hero m_hero;
	private MonsterLayer m_monster_layer;
	
	protected static final int ANIMATION_FRAME_RATE = 150;


	protected GfxFrameSet [] get_left_right_frame_set(GfxPalette palette, String name)
	{
		GfxFrameSet [] rval = new GfxFrameSet[2];
		
		rval[0] = palette.lookup_frame_set(name);
		rval[1] = palette.get_left_frame_set(rval[0]);
		
		return rval;
	}
	

	public void init(LevelData level, Hero hero, MonsterLayer ml)
	{
		
		GfxPalette common_palette = level.get_common_palette();
	
		m_monster_layer = ml;
		
		m_simple_frame_set = get_left_right_frame_set(common_palette, "familiar");
		m_armoured_frame_set = get_left_right_frame_set(common_palette, "familiar_armoured");
		
		m_hero = hero;
		
		m_width = m_simple_frame_set[0].get_width();
		m_height = m_simple_frame_set[0].get_height();
	

		m_frame_set = m_armoured_frame_set;
		
		m_frame_counter = 1;
		
		m_nb_move_frames = m_frame_set[0].get_nb_frames();
		
		set_life_state(LifeState.ALIVE);
		
		m_x = m_hero.get_x();
		m_y = m_hero.get_y() - m_hero.get_height() * 10;
		
		m_health = get_max_health();

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
		if (s == LifeState.EXPLODING)
		{
			m_frame_counter = 1;
			if (get_life_state() == LifeState.ALIVE)
			{
				super.set_life_state(s);
			}
		}
		else
		{
			super.set_life_state(s);
		}
	}

	
	private static final int MIX_TIME_NORMAL = 200;
	private static final int MIX_TIME_POWER = 100;
	
	public void set_power_wings()
	{
		m_power_wings = true;
	}
	public void add_claws_power(int p)
	{
		m_claws_power += p;
	}
	
	public void update(long elapsed_time) 
	{
		// if hostile in screen, try to match the y coord
		
		int y_aim = m_hero.get_y() - m_hero.get_height();
		
		Hostile h = m_monster_layer.closest_hostile(m_hero,m_width);
		
		
		if (h != null)
		{
			y_aim = h.get_y();
			m_slow_y_move_timer = 0;
		}
		else
		{
			// workaround for the "stuck because of fireballs" bug in classic level 4-2
			if (m_hero.get_position_state()==Hero.PositionState.CROUCH)
			{
				m_slow_y_move_timer = 6000;
			}
			if (m_slow_y_move_timer>0)
			{
				y_aim = m_hero.get_y()+ m_hero.get_height()/4;
				m_slow_y_move_timer -= elapsed_time;
				if (m_slow_y_move_timer<0)
				{
					m_slow_y_move_timer = 0;
				}
				
			}
		}
		double old_x = m_x;
		double old_y = m_y;
		double max_dx = elapsed_time/6.0;
		double max_dy = elapsed_time/6.0;
		
		int mix_time = m_power_wings ? MIX_TIME_POWER : MIX_TIME_NORMAL;
		
		long mix_div = (mix_time + elapsed_time);
		
		m_x = ((m_x*mix_time)+m_hero.get_x()*elapsed_time) / mix_div;
		m_y = ((m_y*mix_time)+y_aim*elapsed_time) / mix_div;
		
		double dx = m_x - old_x;
		double dy = m_y - old_y;
		
		// limit delta x,y
		
		if (Math.abs(dx) > max_dx)
		{
			m_x = old_x + max_dx * Math.signum(dx);
		}
		if (Math.abs(dy) > max_dy)
		{
			m_y = old_y + max_dy * Math.signum(dy);
		}
		
		switch (m_hero.get_walk_direction())
		{
		case -1:
			m_right_left = 1;
			break;
		case 1:
			m_right_left = 0;
			break;

		}
		
		switch (m_life_state)
		{
		case ALIVE:

			animate(elapsed_time);
			break;

		case EXPLODING:
		{
			m_hero.get_weapon_set().spawn_stardust(get_x_center(), get_y_center());
			m_life_state = LifeState.DEAD;
			break;
		}

		}

		
	}
	
	protected void animate(long elapsed_time)
	{
		m_animation_timer += elapsed_time;
		
		while (m_animation_timer > ANIMATION_FRAME_RATE)
		{
			m_animation_timer -= ANIMATION_FRAME_RATE;

			m_frame_counter += m_frame_increment;

			if (m_frame_counter > m_nb_move_frames)
			{
				m_frame_increment = -1;
				m_frame_counter = m_nb_move_frames - 1;
			}
			else if (m_frame_counter < 1)
			{
				m_frame_counter = 2;
				m_frame_increment = 1;
			}
		}
		
	}
	
	public int get_claws_power()
	{
		return m_claws_power;
	}
	
	public void hurt(int damage_points)
	{
		m_health -= damage_points;
		
		//debug("damage "+damage_points*5+", remaining "+m_health);
		
		if (m_health < get_max_health()/2)
		{
			m_frame_set = m_simple_frame_set;
		}
		else	
		{
			m_frame_set = m_armoured_frame_set;			
		}
		if (m_health <= 0)
		{
			set_life_state(LifeState.EXPLODING);
		}
	}
	public void render(Graphics2D g) 
	{
		GfxFrame gf_moving = null;
	
		switch (m_life_state)
		{
		case ALIVE:
			gf_moving = m_frame_set[m_right_left].get_frame(m_frame_counter);
			break;
			
		case EXPLODING:
			//gf_moving = m_exploding_frame_set.get_frame(m_frame_counter);
			break;
		}
		
		if (gf_moving != null)
		{
			draw_image(g,gf_moving.toImage());
		}
		
		
	}

}
