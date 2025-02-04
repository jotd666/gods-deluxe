package gods.game.characters;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.*;
import gods.base.*;
import gods.game.MonsterLayer;
import gods.game.SfxSet;
import gods.game.characters.LivingCharacter.LifeState;
import gods.game.characters.weapons.*;
import gods.sys.MiscUtils;

public class HeroWeaponSet implements Renderable
{
	private LevelData m_level_data;
	private MonsterLayer m_monster_layer;
	private Rectangle m_view_bounds;
	private Rectangle m_max_bounds = new Rectangle();
	private SfxSet m_sfx_set = null;
	
	// off-screen margin outside which weapons are removed
	
	private static final int FIELD_MARGIN = 40;
	
	public enum Type { knife, throwing_star, spear, mace, axe , magic_axe, 
		hunter, fire_ball, lightning_bolt, time_bomb }
	
	public enum Arc { standard, wide, intense }
	
	private static final int [] m_arc_angle = { 19, 70, 5 };
	
	private int m_min_weapon_power = 0;
	private int m_difficulty_level = 0;

	public void set_min_weapon_power_diff_level(int min_weapon_power,int difficulty_level) {
		this.m_min_weapon_power = min_weapon_power;
		this.m_difficulty_level = difficulty_level;
	}

	public class Slot
	{
		public final Type type;
		public final int unit_power;
		public final int unit_price;
		public final int category;
		public int power_count = 0;
		public GfxFrameSet frames;
		
		private Class<?> c;
		
		public Slot(Type type, int power, int category)
		{
			this.type = type;
			this.unit_power = power;
			
			GfxFrameSet gfs = m_level_data.get_level_palette().lookup_frame_set(type.toString());
			
			int up = -1;
			
			if (gfs != null)
			{
				up = gfs.get_properties().value;
			}
			
			this.unit_price = up;
			this.category = category;
			
			String class_name = MiscUtils.capitalize(type.toString());
			try
			{
				c = Class.forName("gods.game.characters.weapons."+class_name);
			} 
			catch (ClassNotFoundException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private int [] angle_array = new int[3];
		private int [] y_step_table = { 1, 0, 2 };
		
		public void shoot(int x, int y, int direction)
		{
			if (power_count > 0)
			{
				int y_step = 6;
				
				int nb_weapons = 0;
				angle_array[0] = 0;
				
				switch (type)
				{
				case lightning_bolt:
					// special case: only 1 weapon, and only 1 at a time

					nb_weapons = 1;
					break;
				case hunter:
					// 3 simultaneous weapons
					angle_array[2] = 45;
					angle_array[1] = -45;
					
					nb_weapons = Math.min(power_count,3) - get_active_weapons(type);
					
					break;
				case time_bomb:
					// special case: only 1 weapon

					nb_weapons = 1 - get_active_weapons(type);
					break;
				case magic_axe:
					angle_array[2] = m_arc_angle[m_arc.ordinal()];

					// 3 simultaneous weapons
					
					nb_weapons = Math.min(power_count,3) - get_active_weapons(type);
					break;
				default:
					int nb_weapons_on_screen = get_active_weapons(type);
				
				nb_weapons = Math.min(power_count,3);
				
				if (nb_weapons_on_screen / nb_weapons < 3) // 3 * 3
				{	
					angle_array[2] = m_arc_angle[m_arc.ordinal()];
					angle_array[1] = -angle_array[2];
				}
				else
				{
					// no more than 3 salvos of other weapons on screen at the same time
					// (not that the engine couldn't bear it but it would be too easy compared to the original)
					nb_weapons = 0;
				}
					break;
				}
				
				HeroLinearWeapon hlw = null;
				
				if (nb_weapons > 0)
				{
					// I just figured out (in dec-2015!!) that weapon power was bestowed on EACH weapon, not
					// split between them. That checks with l1w2 where monster has 3 HP and is killed by a single knife
					// just because hero picked up 2 knives and a half-assed power-up => power of 3 ... * 3 weapons.
					// not logical but easy to handle
					//
					// and I was not close, because after 3 weapons, the power is just increased by 1 per weapon added, not
					// weapon_power!!!!
					// and there's this "help" from the game when the power is too low. Complex! thanks Kroah for the resourced code!!
					
					int weapon_power = get_power();
												
					for (int i = 0; i < nb_weapons; i++)
					{						
						int y_weapon = y + 40;
						
						HeroWeapon o = null;

						try 
						{
							o = (HeroWeapon)c.newInstance();
						} 
						catch (InstantiationException e) 
						{	
							e.printStackTrace();
						} catch (IllegalAccessException e) {

							e.printStackTrace();
						}

						
						if (o instanceof HeroLinearWeapon) 
						{
							hlw = (HeroLinearWeapon) o;	
							int angle = direction == -1 ? 180 - angle_array[i] : angle_array[i];
							hlw.init(angle,50,weapon_power,m_level_data,m_monster_layer,type.toString());
							
						}
						else if (o instanceof FireBall)
						{
							int angle = direction == -1 ? 180 - angle_array[i] : angle_array[i];
							FireBall fb = (FireBall) o;
							fb.init(angle,100,weapon_power,m_level_data,m_monster_layer);
						}
						else if (o instanceof Hunter)
						{
							int angle = direction == -1 ? 180 - angle_array[i] : angle_array[i];
							Hunter h = (Hunter) o;
							h.init(angle,100,weapon_power,m_level_data,m_monster_layer);
						}
						else if (o instanceof MagicAxe)
						{
							MagicAxe ma = (MagicAxe) o;
							int angle = direction == -1 ? 180 - angle_array[2] : angle_array[2];

							ma.init(angle,100,weapon_power,m_level_data,m_monster_layer);
						}
						else if (o instanceof TimeBomb)
						{
							TimeBomb tb = (TimeBomb) o;
							int angle = direction == -1 ? 180 - angle_array[2] : angle_array[2];

							tb.init(angle,100,weapon_power,m_level_data,m_monster_layer);
							
							// launch higher
							
							y_weapon -= tb.get_height();
						}
						if (o != null)
						{
							int x_weapon = direction < 0 ? (x - o.get_width()) + 10 : x - 10;
							
							o.set_coordinates(x_weapon, y_weapon + y_step * y_step_table[i] - o.get_height()/2);
							o.set_sfx_set(m_sfx_set);
							m_projectile_list.add(o);
						}
					}

				}
			}
		}
		
		private int get_power()
		{
			// after a deep game behaviour + source code analysis, fixed in v0.9
			// that is NOT unit_power * power_count
			// this is true up to power_count = 3. Afterwards it's lower
			int pc_min_3 = power_count-3;
			int rval;
			if (pc_min_3<=0)
			{
				// 0,1,2,3 weapons: apply unit_power: linear
				rval = unit_power * power_count;
			}
			else
			{				
				rval = unit_power * 3 + pc_min_3;
			}
			
			// last "nice touch" from the Bros: if the weapon power is
			// too low compared to a predefined minimum weapon power
			// then add the 8th of this difference to help closing the gap
			//
			// this may prove useful at some levels when you have only one spear
			// or one star
			
			int power_diff = m_min_weapon_power - rval;
			if (power_diff>0)
			{
				rval += power_diff>>3;
			}
			if (m_difficulty_level>0)
			{
				// resourced from game
				if (m_difficulty_level>=4)
				{
					// damage only half the points, if odd, it's more than half:
					// nicer for the player
					// if power 1: doesn't do anything
					rval -= (rval>>1);
				}
				else
				{
					// damage only 75% of the points
					rval -= ((rval>>1) - (rval>>2));
				}
				
			}
			//System.out.println("weapon power adjusted diff level "+rval+" difficulty "+m_difficulty_level);
			return rval;
		}
		
		public int get_price()
		{
			return unit_price * power_count;
		}
		public int get_refund_price()
		{
			return get_price() / 2;
		}
		
		public void increase_wp(boolean full)
		{
			if (power_count>0)
			{
				// this has been fixed in v0.9, increase weapon power was too powerful
				power_count += 1;
				if (full)
				{
					power_count += 1;
				}
			}
		}
		public void taken()
		{
			power_count++;
		}
	}
	
	private HashMap<String,Slot> m_weapon_map = new HashMap<String,Slot>();
	private LinkedList<HeroWeapon> m_projectile_list = new LinkedList<HeroWeapon>();
	private LinkedList<HeroWeapon> m_starburst_list = new LinkedList<HeroWeapon>();

	private GfxFrameSet m_starburst;
	
	private Arc m_arc = Arc.standard;
	
	private int get_active_weapons(Type t)
	{
		int count = 0;
		
		for (HeroWeapon p : m_projectile_list)
		{
			if (p.get_name().equals(t.toString()))
			{
				count++;
			}
		}
		return count;
	}
	private void add_to_map(Slot s)
	{
		if (s.unit_price > 0)
		{
			Slot old_s = m_weapon_map.get(s.type.toString());
			if (old_s != null)
			{
				// get old value for weapon
				s.power_count = old_s.power_count;
			}
			
			m_weapon_map.put(s.type.toString(),s);
		}
	}
	
	public void spawn_stardust(int x, int y)
	{
		StarburstBall.spawn(x,y,m_level_data,m_monster_layer,
							m_starburst,0.15,m_sfx_set,m_starburst_list);
		
	}
	

	public void remove_all()
	{
		for (Slot s : m_weapon_map.values())
		{
			s.power_count = 0;
		}
		
	}
	void init(GfxPalette common_palette, Rectangle view_bounds,MonsterLayer ml, LevelData level, SfxSet sfx_set)
	{
		m_monster_layer = ml;
		m_level_data = level;
		m_sfx_set = sfx_set;
		
		add_to_map(new Slot(Type.knife, 1, 1));
		add_to_map(new Slot(Type.throwing_star, 2, 1));
		add_to_map(new Slot(Type.fire_ball, 5, 3));
		add_to_map(new Slot(Type.magic_axe, 3, 2));
		add_to_map(new Slot(Type.hunter, 5, 2));
		add_to_map(new Slot(Type.time_bomb, 20, 2));
		add_to_map(new Slot(Type.mace, 3, 1));
		add_to_map(new Slot(Type.spear, 6, 1));
		add_to_map(new Slot(Type.axe, 12, 1));
		add_to_map(new Slot(Type.lightning_bolt, 25, 5));
		
		for (Slot s : m_weapon_map.values())
		{
			String n = s.type.toString();
			s.frames = common_palette.lookup_frame_set(n);
		}				
		
		m_starburst = common_palette.lookup_frame_set("starburst");
		
		m_view_bounds = view_bounds;
		
		m_projectile_list.clear();
		m_starburst_list.clear();
	}
	
	
	boolean can_shoot()
	{
		boolean rval = false;
		
		for (Slot s : m_weapon_map.values())
		{
			if (s.power_count != 0)
			{
				rval = true;
				break;
			}
		}				
		
		return rval;
	}

	void shoot(int x, int y, int direction, Familiar familiar)
	{
		// check if only fireball
		
		if ((familiar == null) || (familiar.get_life_state() != LifeState.ALIVE))
		{
			for (Slot s : m_weapon_map.values())
			{
				s.shoot(x,y,direction);
			}
		}
		else
		{
			// familiar: check that not only fireballs
			boolean only_fireball = true;
			for (Slot s : m_weapon_map.values())
			{
				if ((s.type != HeroWeaponSet.Type.fire_ball) && (s.power_count > 0))	
				{
					only_fireball = false;
					break;
				}
			}
			Slot fireball = m_weapon_map.get(HeroWeaponSet.Type.fire_ball.toString());
			
			if (only_fireball)
			{
				// call ourselves without familiar
				fireball.shoot(x,y,direction);
			}
			else
			{
				int fx = direction < 0 ? familiar.get_x() : familiar.get_x() + familiar.get_width();
				
				fireball.shoot(fx,familiar.get_y(),direction);
				
				for (Slot s : m_weapon_map.values())
				{
					if (s != fireball)	
					{
						s.shoot(x,y,direction);
					}
				}
			}
		}				

	}
	
	
	void increase_weapon_power(boolean full)
	{
		for (Slot s : m_weapon_map.values())
		{
			s.increase_wp(full);
		}
	}
	
	boolean owned(String t)
	{
		return power_of(t) != 0;
	}
	
	int power_of(String t)
	{
		Slot s = m_weapon_map.get(t);
		return s.power_count;
	}
	
	
	boolean removed(String t)
	{
		Slot s = m_weapon_map.get(t);
		boolean rval = ((s != null) && (s.power_count > 0));
		
		if (rval)
		{
			s.power_count = 0;
		}
		
		return rval;
	}
	boolean taken(String t)
	{
		Slot s = m_weapon_map.get(t);
		
		if (s != null)
		{
			s.taken();
			
			// weapon taken: remove other weapons from this category
			
			for (Slot so : m_weapon_map.values())
			{
				if ((so != s) && (so.category == s.category))
				{
					so.power_count = 0;
				}
			}
		}
		return s != null;
		
	}
	

	public Arc get_arc() {
		return m_arc;
	}

	void set_arc(Arc arc) 
	{
		this.m_arc = arc;
	}

	public boolean is_weapon(String t)
	{
		return m_weapon_map.get(t) != null;
	}
	
	public Slot refund_on_weapon_take(String t)
	{
		Slot rval = null;
		Slot s = m_weapon_map.get(t);
		
		if (s != null)
		{
			for (Slot so : m_weapon_map.values())
			{
				if ((so.power_count > 0) && (so != s) && (so.category == s.category))
				{
					rval = so;
					break;
				}
			}			
		
		}
		
		return rval;
	}

	public void render(Graphics2D g) 
	{
		for (Projectile p : m_projectile_list)
		{
			p.render(g);
		}
		
	}

	public void update(long elapsed_time)
	{
		if (!m_starburst_list.isEmpty())
		{
			m_projectile_list.addAll(m_starburst_list);
			m_starburst_list.clear();
		}
		
		ListIterator<HeroWeapon> it = m_projectile_list.listIterator();
		
		m_max_bounds.x = m_view_bounds.x - FIELD_MARGIN;
		m_max_bounds.y = m_view_bounds.y - FIELD_MARGIN;
		m_max_bounds.width = m_view_bounds.width + FIELD_MARGIN * 2;
		m_max_bounds.height = m_view_bounds.height + FIELD_MARGIN * 2;
		
		while (it.hasNext())
		{
			Projectile p = it.next();
			
			p.update(elapsed_time);
			
			if ((!m_max_bounds.contains(p.get_x(), p.get_y())) || 
					(p.get_state() == Projectile.State.DEAD))
			{
				it.remove();
			}
		
		}
	}
	
	public Collection<HeroWeapon> get_projectile_list()
	{
		return m_projectile_list;
	}
}
