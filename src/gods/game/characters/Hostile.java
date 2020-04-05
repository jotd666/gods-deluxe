package gods.game.characters;

import gods.base.*;
import gods.game.SfxSet.Sample;
import gods.game.characters.weapons.*;
import java.awt.Rectangle;

public abstract class Hostile extends LivingCharacter
{
	public abstract boolean is_in_background();
		
	public HostileParameters get_params()
	{
		return m_params;
	}
	
	protected HostileParameters m_params;
	protected Rectangle m_work_rectangle = new Rectangle();
	protected Rectangle m_work_rectangle_2 = new Rectangle();
	protected boolean m_frozen = false;
	protected boolean m_appearing_animation = true;
	protected double m_appearing_x, m_appearing_y;

	public boolean is_in_screen(int margin)
	{
		return is_in_screen(margin,m_params.view_bounds);
	}
	
	
	protected boolean may_shoot()
	{
		return is_in_screen(m_width);
	}
	
	public String toString()
	{
		return get_name();
	}
	public void handle_health(long elapsed_time)
	{
		
	}

	public ControlObject get_location()
	{
		return m_params.location;
	}
	
	public Sample get_exploding_sound() 
	{
		return Sample.explosion_ground;
	}
	
	protected Sample get_appearing_sound() 
	{
        // stereo
		int dx = get_x() - m_params.hero.get_x();

		return (dx < 0 ? Sample.hostile_appearing_left : Sample.hostile_appearing_right);
	}		
	
	public int get_max_health()
	{
		return m_params.max_health_points;
	}
	// also allows to reduce enemy current health
	// handy on boss level 4 with the worm
	public void set_max_health(int mh)
	{
		m_params.max_health_points = mh;
		if (m_params.health_points > mh)
		{
			m_params.health_points = mh;
		}
	}
	public int get_health(boolean instantaneous)
	{
		return m_params.health_points;
	}
	
	public HostileWaveParameters get_dropped_hostile()
	{
		HostileWaveParameters rval = m_params.hostile_to_drop;
		
		if (rval != null)
		{
			ControlObject co = rval.location;
			co.set_coordinates(this,true);
			
			m_params.hostile_to_drop = null;
		}
		
		return rval;
	}
	public void set_frozen(boolean frozen)
	{
		m_frozen = frozen;
	}
	public GfxObject get_dropped_item()
	{
		GfxObject rval = m_params.object_to_drop;
		
		if (rval != null)
		{
			rval.set_coordinates(this,true);
			m_params.object_to_drop = null;
		}
		
		return rval;
	}
	
	public GfxObject get_held_item()
	{
		GfxObject rval = m_params.object_held;
		
		if (rval != null)
		{
			rval.set_coordinates(this,true);
			m_params.object_held = null;
		}
		
		return rval;
	}
	

	
	public String get_name()
	{
		return m_params.location.get_name();
	}
	
	public boolean is_named()
	{
		return !get_name().equals("");
	}
	
	public void collision(Familiar f)
	{
		int familiar_damage = f.get_claws_power();
		f.hurt(1);

		m_params.health_points -= familiar_damage;
		if (m_params.health_points <= 0)
		{
			die();
		}

	}
	
	public boolean collision_test(Rectangle other)
	{
		get_bounds(m_work_rectangle);
		return (other.intersects(m_work_rectangle));
	}
	public void collision(Hero h) 
	{

		/* difficult to figure out the relation between enemy remaining health and
		 * damage done without the 68k disassembly
		 * 0000D9F8     move.w  d2,d3
				0000D9FA     addq.w  #1,d3
				0000D9FC     asr.w   #5,d3
				0000D9FE     addq.w  #5,d3*/

		h.hurt(((1+m_params.health_points)>>5) + 5);


		h.add_score(m_params.score_points, false);
		die();
	}	

	public int get_score_points()
	{
		return m_params.score_points;
	}
	
	public void init(HostileParameters p)
	{
		m_params = p;
		
		//m_appearing_animation = true; //p.view_bounds.contains(m_x,m_y);
		
		if (p.appearing_delay == 0)
		{
			m_life_state = m_appearing_animation ? LifeState.APPEARING : LifeState.ALIVE;
		}
		else
		{
			m_life_state = LifeState.DELAYED;
		}

		GfxObject o = p.object_to_drop;
		
		// try to remove object if was in the level data
		// (will be re-inserted on hostile death)
		
		if (o != null)
		{
			p.level.remove_object(o);
		}
				
		m_x = p.location.get_x();
		m_y = p.location.get_y();
		m_appearing_x = m_x;
		m_appearing_y = m_y;
		
		// default width and height, most of the time overridden
		
		m_width = p.location.get_width();
		m_height = p.location.get_height();
		
		// hp => points (thanks Kroah for your wonderful level viewer)
		
		
		p.score_points = 100 * (p.original_health_points/2 + 1);
		
	}
	
	public void die()
	{
		hit(null);
	}
	
	/*protected void disable_weapon(HeroWeapon hw)
	{
		m_received_blows.add(hw);
	}*/
	
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
				double damage = hw.get_power();

				m_params.health_points -= damage;
				
				if (m_params.health_points <= 0)
				{
					set_life_state(LifeState.EXPLODING);
				}				
			
				hw.set_state(Projectile.State.HURTING_HOSTILE);				
			}
		}
		return true;
	}
}
