package gods.game;

import java.awt.Graphics2D;
import java.util.*;
import java.awt.Rectangle;

import gods.base.*;
import gods.game.characters.*;
import gods.game.characters.LivingCharacter.LifeState;
import gods.game.characters.hostiles.RotatingSpike;
import gods.game.characters.hostiles.SpikeUpDown;
import gods.game.characters.weapons.HeroWeapon;

public class MonsterLayer implements Renderable
{
	private Rectangle m_wr1 = new Rectangle();
	//private Rectangle m_wr2 = new Rectangle();
		
	private Hero m_hero;
	private SfxSet m_sfx_set;
	private SectoredLevelData m_level;
	private HostileWeaponSet m_hostile_weapon_set;
	private int m_collision_timer = 0;
	
	private LinkedList<Hostile> m_items = new LinkedList<Hostile>();
	private LinkedList<Hostile> m_pending_items = new LinkedList<Hostile>();
	private LinkedList<Hostile> m_active_background_items = new LinkedList<Hostile>();
	private LinkedList<Hostile> m_active_foreground_items = new LinkedList<Hostile>();
	private LinkedList<AnimatedFrames> m_weapon_crashes = new LinkedList<AnimatedFrames>();
	private RenderableWithoutUpdateList m_weapon_crashes_rl = new RenderableWithoutUpdateList();
	private RenderableWithoutUpdateList m_foreground_hostiles_rl = new RenderableWithoutUpdateList();
	private RenderableWithoutUpdateList m_background_hostiles_rl = new RenderableWithoutUpdateList();
	
	private int m_difficulty_level = 1;
	private GfxFrameSet m_weapon_crash;
	
	private static final int COLLISION_RATE = 30;
	
	private int m_freeze_timer = 0;
	
	public Hero get_hero()
	{
		return m_hero;
	}
	
	public void freeze()
	{
		m_freeze_timer = 20000;
	}
	
	public void render(Graphics2D g) 
	{
		m_weapon_crashes_rl.render(g);

	}

	public Renderable get_background_items()
	{
		return m_background_hostiles_rl;
	}
	public Renderable get_foreground_items()
	{
		return m_foreground_hostiles_rl;
	}
	
	public MonsterLayer(SectoredLevelData gl, Hero hero, 
			SfxSet sfx_set, 
			HostileWeaponSet hostile_weapon_set,
			int difficulty_level)
	{
		m_level = gl;
		m_hero = hero;
		m_sfx_set = sfx_set;
		m_hostile_weapon_set = hostile_weapon_set;
		m_weapon_crash = gl.get_common_palette().lookup_frame_set("weapon_crash");
		
		m_background_hostiles_rl.items = m_active_background_items;
		m_foreground_hostiles_rl.items = m_active_foreground_items;
		m_weapon_crashes_rl.items = m_weapon_crashes;

		m_difficulty_level = difficulty_level;
	}
	

	public Collection<Hostile> get_items()
	{
		return m_items;
	}
	public void kill(String name)
	{
		kill(name,m_active_foreground_items);
		kill(name,m_active_background_items);
		
	}
	
	public void cleanup_remaining_monsters()
	{
		ListIterator<Hostile> lit = m_active_foreground_items.listIterator();
		
		while (lit.hasNext())
		{
			Hostile m = lit.next();
			
			if (((m.get_life_state() == LifeState.ALIVE) || (m.get_life_state() == LifeState.APPEARING)
					|| (m.get_life_state() == LifeState.DELAYED)) && 
					!m.get_params().created_at_level_start)
			{	
				lit.remove();
			}
		}		
	}
	// to be called from an hostile
	
	public void spawn_wave(HostileWaveParameters hwp)
	{
		int nb_items = hwp.get_nb_items();
	
		// if only one hostile, use delay
		
		for (int i = 0; i < nb_items; i++)
		{
			Hostile h = create_hostile(hwp, i);
			if (h != null)
			{
				m_pending_items.add(h);
			}
		}		
	}
	
	public Hostile spawn_hostile(HostileParameters hp)
	{
		Hostile h = create_hostile(hp);
		
		m_pending_items.add(h);
		
		return h;
	}

	public void update(long elapsed_time) 
	{
		if (m_freeze_timer > 0)
		{
			m_freeze_timer -= elapsed_time;
		}
		
		ListIterator<AnimatedFrames> it1 = m_weapon_crashes.listIterator();
		
		while (it1.hasNext())
		{
			AnimatedFrames af = it1.next();

			af.update(elapsed_time);

			if (af.is_done())
			{
				it1.remove();
			}
		}

		// if there are some "pending" hostiles, insert them here
		
		if (!m_pending_items.isEmpty())
		{
			for (Hostile h : m_pending_items)
			{
				add_hostile(h);
			}
			
			m_pending_items.clear();
		}
		
		// loop on all active items
		
		process_active_items(m_active_background_items,elapsed_time);
		process_active_items(m_active_foreground_items,elapsed_time);
	}
	
	public Hostile closest_hostile(NamedLocatable l, int in_screen_margin)
	{
		Hostile h = closest_hostile(l,m_active_foreground_items, in_screen_margin);
		
		if (h == null)
		{
		  h = closest_hostile(l,m_active_background_items, in_screen_margin);
		}
		
		return h;
	}
	
	private Hostile closest_hostile(NamedLocatable l, Collection<Hostile> hl, int in_screen_margin)
	{
		Hostile rval = null;
		
		int min_distance = Integer.MAX_VALUE;
		
		for (Hostile h : hl)
		{
			if (h.is_in_screen(in_screen_margin) && (h.get_life_state() == LifeState.ALIVE) && (!is_spike(h)))
			{
				int d = l.square_distance_to(h);
				if (d < min_distance)
				{
					rval = h;
					min_distance = d;
				}
			}
		}
		
		return rval;
	}
	public void collision(HeroWeapon p)
	{
		collision(p,m_active_background_items);
		collision(p,m_active_foreground_items);
	}
	public void collision(HeroWeapon p, LinkedList<Hostile> l)
	{
		p.get_bounds(m_wr1);

		for (Hostile h : l)
		{
			if (h.get_life_state() == Hostile.LifeState.ALIVE)
			{
				if (h.collision_test(m_wr1))
				{
					if (p.can_hurt_hostiles())
					{
						if (h.hit(p))
						{
							if (h.get_life_state() != Hostile.LifeState.ALIVE)
							{
								m_sfx_set.play_random(h.get_exploding_sound());
								
								// killed: give the points to the hero

								m_hero.add_score(h.get_score_points(),false);
							}
							else
							{
								add_impact(h);
							}
						}
					}
				}
			}
		}
	}
	
	private Hostile create_hostile(HostileParameters hp)
	{
		Hostile h = null;
		
		try
		{
			h = (Hostile)Class.forName(hp.class_name).newInstance();
			
			h.init(hp);
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return h;		
	}
	
	public Hostile create_hostile(HostileWaveParameters hwp, int index)
	{
		Hostile h = null;
		
		try
		{
			h = (Hostile)Class.forName(hwp.class_name).newInstance();
			
			HostileParameters hp = new HostileParameters(hwp,m_level,
					this,
					m_hero,m_sfx_set,
					m_hostile_weapon_set,index,m_difficulty_level);

			h.init(hp);
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return h;
			
	}
	
	public Hostile lookup_active_hostile(String name)
	{
		Hostile h = lookup_hostile(name,m_active_foreground_items);
		if (h == null)
		{
			h = lookup_hostile(name,m_active_background_items);
		}
		
		return h;
	}
	
	public Hostile lookup_hostile(String name)
	{
		return lookup_hostile(name,m_items);
	}

	private Hostile lookup_hostile(String name,List<Hostile> l)
	{
		ListIterator<Hostile> it = l.listIterator();
		Hostile rval = null;
		
		while (it.hasNext())
		{
			Hostile h = it.next();
			
			if (h.get_name().equals(name))
			{
				rval = h;
				break;
			}
		}
		return rval;
		
	}
	
	void activate_hostile_wave(String name)
	{
		ListIterator<Hostile> it = m_items.listIterator();
		
		while (it.hasNext())
		{
			Hostile h = it.next();
			
			if (h.get_name().equals(name))
			{
				add_hostile(h);
				it.remove();
			}
		}
	}
	
	void activate_hostile(String name)
	{
		ListIterator<Hostile> it = m_items.listIterator();
		
		while (it.hasNext())
		{
			Hostile h = it.next();
			
			if (h.get_name().equals(name))
			{
				add_hostile(h);
				it.remove();
				break;
			}
		}
		
	}
	
	/*private void debug(String message)
	{
		if (Debug.active)
		{
			System.out.println(this.getClass().getName()+": "+message);
		}
	}*/

	private void add_impact(Hostile h)
	{
		// animated weapon damage
		AnimatedFrames af = new AnimatedFrames();
		af.init(m_weapon_crash,100,AnimatedFrames.Type.ONCE);

		// weapon crash appears in the hostile collision rectangle,
		// at random

		double x_crash = h.get_x() + Math.random() * 0.8 * h.get_width();
		double y_crash = h.get_y() + Math.random() * 0.8 * h.get_height();

		af.set_x(x_crash);
		af.set_y(y_crash);

		m_weapon_crashes.add(af);
	}
	
	private boolean is_spike(Hostile h)
	{
		return (h instanceof SpikeUpDown) || (h instanceof RotatingSpike);
	}
	
	private void process_active_items(LinkedList<Hostile> l, long elapsed_time)
	{
		ListIterator<Hostile> it2 = l.listIterator();
		LinkedList<Hostile> spawned_list = null;
	
		boolean frozen = m_freeze_timer > 0;
	
		boolean spike_visible = false;
		boolean contains_spike = false;
		int nb_collisions = 0;
		
		m_collision_timer += elapsed_time;
				
		if (m_collision_timer >= COLLISION_RATE)
		{
			nb_collisions = m_collision_timer / COLLISION_RATE;
			m_collision_timer = m_collision_timer % COLLISION_RATE;
		}
		
		while (it2.hasNext())
		{
			Hostile m = it2.next();
	
			m.set_frozen(frozen);
	
			m.update(elapsed_time);
	
			boolean is_spike = is_spike(m);
			if (is_spike)
			{
				contains_spike = true;
			}
			
			switch (m.get_life_state())
			{
			case EXPLODING:
			{
				GfxObject go;
				
				// last enemy drops the loot
				
				
				go = m.get_dropped_item();
				if (go != null)
				{
					go.set_visible(true);
					go.set_initial_fall_speed(-4);
					m_level.add_object(go);
				}
	
				go = m.get_held_item();
	
				if (go != null)
				{
					go.set_y(m.get_y()); // thief carries the item lower
					go.set_visible(true);
					go.set_initial_fall_speed(-4);
					m_level.add_object(go);
				}
	
				HostileWaveParameters hwp = m.get_dropped_hostile();
	
				if (hwp != null)
				{
					// create hostiles
					int nb_hostiles = hwp.get_nb_items();
	
					for (int i = 0; i < nb_hostiles; i++)
					{
						Hostile h = create_hostile(hwp,i);
						if (spawned_list == null)
						{
							spawned_list = new LinkedList<Hostile>();
						}
						spawned_list.add(h);
					}
				}
				break;
			}
			case DEAD:
			{
				it2.remove();
				break;
			}
	
			case ALIVE:
			{
				if ((is_spike) && (!spike_visible))
				{
					spike_visible = (m_level.get_view_bounds().contains(m.get_x(),m.get_y()));
				}
				
				Familiar fam = m_hero.get_familiar();
				
				for (int i = 0; i < nb_collisions; i++)
				{
					// hero
					
					if (m_hero.get_life_state() == Hero.LifeState.ALIVE)
					{
						m_hero.get_bounds(m_wr1);
						
						if (m.collision_test(m_wr1))
						{
							m.collision(m_hero);
							
							if (m.get_life_state() == LivingCharacter.LifeState.EXPLODING)
							{
								m_sfx_set.play(m.get_exploding_sound());
							}
						}
						
						// familiar
						
						 if (fam != null && !is_spike)
						 {
							// handle collisions between familiar and hostiles
							fam.get_bounds(m_wr1);
							
							if (m.collision_test(m_wr1))
							{
								m.collision(fam);
								
								add_impact(m);
								
								if (m.get_life_state() == LivingCharacter.LifeState.EXPLODING)
								{
									m_sfx_set.play(m.get_exploding_sound());
								}
							}
						 }
					}
				}
				break;
			}
			}
		}
	
		if (spawned_list != null)
		{
			l.addAll(spawned_list);
		}
		
		if (contains_spike)
		{
			if (spike_visible)
			{
				m_sfx_set.play(SfxSet.Loop.spike);
			}
			else
			{
				m_sfx_set.pause(SfxSet.Loop.spike);			
			}
		}
	}

	private void kill(String name,Collection<Hostile> l)
	{
		
		boolean sound_played = false;
		
		for (Hostile m : l)
		{
			if (m.get_life_state() == LifeState.ALIVE && m.get_name().equals(name))
			{
				if (!sound_played)
				{
					sound_played = true;
	
					m_sfx_set.play(m.get_exploding_sound());
				}
				
				m.die();
			}
			
		}		
	}

	private void add_hostile(Hostile h)
	{
		if (h.is_in_background())
		{
			m_active_background_items.add(h);
		}
		else
		{
			m_active_foreground_items.add(h);
		}
	}
	
	// to be called from within package (i.e. GodsLevel class)
	void create_hostile_wave(HostileWaveParameters hwp)
	{
		int nb_items = hwp.get_nb_items();

		// if only one hostile, use delay
		
		for (int i = 0; i < nb_items; i++)
		{
			Hostile h = create_hostile(hwp, i);
			if (h != null)
			{
				if (hwp.instant_creation)
				{
					add_hostile(h);			
				}
				else
				{
					m_items.add(h);
				}
			}
		}
	}
}
