package gods.game.characters;

import gods.base.*;
import gods.game.*;
import gods.game.SfxSet.Sample;
import gods.base.layer.*;
import gods.sys.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;

import joystick.*;

public class Hero extends LivingCharacter
{
	static private int [] JUMP_Y_TABLE;
	final static int JUMP_WIDTH = 170;
	final static int JUMP_HEIGHT = 64;
	static
	{
		int jw = JUMP_WIDTH-1;
		int jw2 = jw*jw;
		
		JUMP_Y_TABLE = new int[JUMP_WIDTH];

		for (int i = 0; i < JUMP_WIDTH;i++)
		{
			int nt = (i-jw/2);
			JUMP_Y_TABLE[i] = (4*JUMP_HEIGHT*nt*nt)/(jw2)-JUMP_HEIGHT;
		}
		
	}
	
	private static final int [] EXTRA_LIFE_SCORE = { 50000, 160000, 300000 };
	private static final int [] BONUS_GEM_SCORE = { 100000, 250000, 400000 };

	public static final int MAX_HEALTH = 24;

	private static final int REFERENCE_FRAME_RATE = 40;
	private static final int CROUCH_FRAME_RATE = 25;
	private static final int MAX_FALL_SPEED = 7;
	
	private boolean m_shift_jumps;
	private GfxFrame m_current;
	private int m_nb_lives;
	private int m_health;
	private int m_money;
	private int m_targeted_score = 0;
	private int m_score = 0;
	private int m_bonus_score;
	private int m_current_extra_life_score_index = 0;
	private int m_current_bonus_gem_score_index = 0;
	private int m_current_extra_life_score;
	private int m_current_bonus_gem_score;
	private int m_targeted_health = 0;
	private int m_targeted_money = 0;
	private boolean m_shield = false;
	private boolean m_fall_playing = false;
	private int m_max_fall_height_without_damage;
	private Familiar m_familiar = null;
	
	private static final int ITEM_HOLDER_STEP = 1;
	private static final int NB_MAX_HELD_ITEMS = 4;
	private static final int ITEMS_GAP = 16;
	private static final int LATERAL_CYCLE = 2 * REFERENCE_FRAME_RATE;
	private static final int MONEY_TIMER_TICK = 50;
	
	private int lateral_cycle = LATERAL_CYCLE;
	
	private boolean m_keep_items_between_worlds = false; // only for 4 a.m. mix
	// total number of levels since start of the game
	// used to compute difficulty level
	// Start at level 2: worth 1, After 4 levels have been completed & restart at level 1: worth 4, etc...
	
	private int m_total_level_index;
	
	private Joystick m_joystick = null;
	private GameEngine m_engine;
	private GfxPalette m_common_palette;
	private GfxPalette m_hero_palette;
	private LevelData m_level_data;
	private StatusBar m_status_bar;
	private SfxSet m_sfx_set;
	
	private Rectangle m_view_bounds; // reference on current view bounds
	
	private int m_walk_counter = 0;
	
	public static final int LATERAL_STEP = 8;
	private static final int VERTICAL_STEP = 12;
	// too high: cannot reach high lateral jumps, too low: cannot complete classic w1l2 secret room
	private static final int X_AMPLITUDE_OFFSET = 14;
	private static final int CLIMB_RESOLUTION = 3 * REFERENCE_FRAME_RATE;
	private static final int FALL_STEP = 4;
	//private static final int JUMP_HEIGHT = 32;
	private static final int X_LEFT_OFFSET = 8; // was 6
	private static final int X_RIGHT_OFFSET = 12;
	private static final int MAX_LATERAL_FALL_COUNTER = 8 * REFERENCE_FRAME_RATE;
	private static final int GIANT_JUMP_DURATION = 16000;
	private static final int CROUCH_BEFORE_JUMP_TIMER = REFERENCE_FRAME_RATE * 3;
	
	private static int [] shoot_frame_sequence = {2,1,2,3};
	private GfxFrameSet [] m_shoot_frames;

	private int m_vert_resolution;
	
	public enum PositionState { FRONT, BACK, WALK, JUMP, CLIMB, CROUCH, FALL }
	private enum ItemHolderState { HIDDEN, APPEARING, SHOWN, DISAPPEARING }
	
	private int m_giant_jump_timer = 0;
	private int m_invincibility_timer = 0;
	private int m_shield_angle = 0;
	private int m_shield_frame_timer = 0;
	private int m_shield_frame = 1;
	//private int m_tile_height;
	
	private PositionState m_position_state = PositionState.FRONT;
	private GfxFrame m_front;
	private GfxFrame m_back;
	private GfxFrame m_turn_front[];
	private GfxFrame m_turn_back[];
	private GfxFrameSet m_walk[];
	private GfxFrameSet m_crouch[];
	private GfxFrame m_shoot_rest[];
	private GfxFrame m_climb[];
	
	private GfxFrameSet m_rotating_shield;
	private GfxFrameSet m_shoot_walk[];
	private GfxFrameSet m_shoot_jump[];
	private GfxFrameSet m_shoot_ladder[];
	private GfxFrameSet m_death;
	
	private ItemHolder m_item_holder;
	private GfxObject m_item_awarded = null;
	
	private int m_right_left = 0;
	
	private boolean m_controls_frozen = false;
	private int m_nb_walk_frames;
	private boolean m_hurt_from_fall;
	private int m_side_switch_timer;
	private int m_previous_side_switch_index;
	private int m_turn_back_index;
	private int m_crouch_index;
	private int m_crouch_timer;
	private int m_climb_index;
	private int m_climb_timer;
	private int m_lateral_walk_timer;
	private int m_lateral_fall_timer;
	private int m_shoot_counter = -1;
	private int m_shoot_timer = 0;
	private int m_fall_time;
	private int m_fall_y;
	private int m_death_counter = 0;	
	private int [] m_death_y = new int[3];
	private int [] m_death_x_offset = new int[3];
	private boolean m_hurt_2 = false;
	private boolean m_fire_while_standing_up = false;
	
	private HeroWeaponSet m_weapons;
	private static final int [] MIN_WEAPON_LEVEL_TABLE = {0,14,22,34};
		
	private MonsterLayer m_monster_layer;
	// minimum power beyond which the game will "help" the player
	// adding the 8th of the difference between weapon current power and this value
	
	/*public GfxObject fetch_dropped_item()
	{
		GfxObject rval = m_item_holder.over_item;
		m_item_holder.over_item = null;
		return rval;
	}*/
	
	
	public int get_difficulty_level()
	{
		int major = (m_total_level_index/4)*2; // 4 levels = difficulty 2, 8 levels: difficulty 4
		int minor = m_total_level_index  % 4;
		if (minor == 3)
		{
			major++;  // +1 for level 4.
		}
		if (major>4)
		{
			major=4;
		}
		return major;
	}
	

	public boolean is_invincible()
	{
		return m_invincibility_timer > 0;
	}

	public PositionState get_position_state()
	{
		return m_position_state;
	}
	public GfxObject item_picked_up()
	{
		GfxObject rval = m_item_holder.item_taken;
		m_item_holder.item_taken = null;

		return rval;
	}
	public void set_min_weapon_power_diff_level(int power)
	{

		m_weapons.set_min_weapon_power_diff_level(power,get_difficulty_level());
	}
	public HeroWeaponSet get_weapon_set()
	{
		return m_weapons;
	}
	
	public GfxObject item_awarded()
	{
		GfxObject rval = m_item_awarded;
		m_item_awarded = null;
		return rval;		
	}
	public GfxObject item_dropped()
	{
		GfxObject rval = m_item_holder.item_given_back;
		m_item_holder.item_given_back = null;
		return rval;
	}
		
	public boolean owns_named_object(String name)
	{
		return m_item_holder.lookup(name) != null;
	}
	
	public boolean steal_named_object(String name)
	{
		 return m_item_holder.remove(name);
	}
	
	
	public void set_keep_items_between_worlds(boolean p)
	{
		m_keep_items_between_worlds = p;
	}
	public void world_start()
	{
		if (!m_keep_items_between_worlds)
		{
			m_item_holder.remove_obsolete_items();
		}
		
		 m_bonus_score = 0;
	}
	
	public boolean try_to_open_chest(GfxObject chest)
	{
		boolean rval = false;

		if (chest.get_source_set().get_type() == GfxFrameSet.Type.chest)
		{
			GfxObject key = m_item_holder.can_open_chest(chest);
			rval =(key != null);
			if (rval)
			{
				// will open the chest: steal the key
				m_item_holder.remove(key.get_name());
			}
		}
		return rval;
	}

	private class ItemHolder
	{
		private ItemHolderState state = ItemHolderState.HIDDEN;
		private int counter;
		int max_counter;
		int current_pos = NB_MAX_HELD_ITEMS-1;
		GfxObject [] object_array = new GfxObject[NB_MAX_HELD_ITEMS];
		BufferedImage back_image,green_back;
		BufferedImage current_image;
		GfxFrameSet frames;
		GfxObject over_item = null;
		GfxObject item_taken = null;
		GfxObject item_given_back = null;
		private int m_display_item_counter = 0;
		
		private boolean next_item = false;
		
		private void next_current_pos()
		{		
			current_pos++;

			if (current_pos == NB_MAX_HELD_ITEMS)
			{
				current_pos = 0;
			}
			next_item = true;
		}
		
		// we don't care if more than 3 objects are bought: overwritten
		// (should not happen in the shop since there are no more than 3
		// holdable items available at a time: shield, starburst, freeze)
		
		void put_object(GfxObject ob)
		{
			object_array[current_pos] = ob;
			
			next_current_pos();
			
		}
		
		GfxObject can_open_chest(GfxObject chest)
		{
			GfxObject key = null;
			
			String chest_type = chest.get_class_name();
			for (GfxObject object : object_array)
			{
				
				if (object != null)
				{
					GfxFrameSet.Type t = object.get_source_set().get_type();
					
					if (t == GfxFrameSet.Type.key)
					{
						// class names match:
						// gold_chest <=> gold_chest_key, etc..
						if (object.get_class_name().startsWith(chest_type))
						{
							key = object;
							break;
						}
					}
				}
			}

			return key;
		}
		
		void remove_obsolete_items()
		{
			// remove items (not bonuses) contained in item holders
			
			for (int i = 0; i < object_array.length; i++)
			{
				if (object_array[i] != null)
				{
					GfxFrameSet.Type t = object_array[i].get_source_set().get_type();
					
					if ((t == GfxFrameSet.Type.pickable) || (t == GfxFrameSet.Type.key))
					{
						object_array[i] = null;
					}
				}
			}
		}
		
		boolean remove(String name)
		{
			boolean rval = false;
			
			if (name != null)
			{
				for (int i = 0; i < object_array.length; i++)
				{
					GfxObject go = object_array[i];

					if ((go != null) && (go.get_name().equals(name)))
					{
						rval = true;
						object_array[i] = null;
						break;
					}
				}
			}
			return rval;
		}
		GfxObject lookup(String name)
		{
			GfxObject rval = null;
			
			for (GfxObject go : object_array)
			{				
				if ((go != null) && (go.get_name().equals(name)))
				{
					rval = go;
					break;
				}
			}
			return rval;
		}
		void set_counter(int counter)
		{
			this.counter = counter;
		}
		
		void init()
		{
			counter = 0;
			over_item = null;
			item_taken = null;
			item_given_back = null;
			current_pos = 0;
			m_shift_jumps = GameOptions.instance().get_control_type() == GameOptions.JumpType.SHIFT;
			
		}
		
		void handle_release()
		{
			switch (state)
			{
			case HIDDEN:
				break; // cannot happen
			case SHOWN:
			case APPEARING:
			
				// hide item holder

				m_sfx_set.play(Sample.inventory,1);

				state = ItemHolderState.DISAPPEARING;

				// drop any object under the "green" slot

				GfxObject to_release = object_array[current_pos];
				
				if (to_release != null)
				{
					// drop the object
					
					object_array[current_pos] = null;
					
					// give it back to the playfield
					
					item_given_back = to_release;
					
					// with hero coordinates, allowing some bounce
					
					item_given_back.set_coordinates(get_x() + X_LEFT_OFFSET,
							get_y()  + m_height - item_given_back.get_height()*2);
				}
				
				break;
			}
		}
		
		ItemHolder()
		{
			frames = get_item_frames("item_holder");
			
			max_counter = frames.toImage().getHeight();

			BufferedImage black_back = frames.get_first_frame().toImage();
			back_image = new BufferedImage(NB_MAX_HELD_ITEMS * black_back.getWidth(),
					black_back.getHeight(),BufferedImage.TYPE_INT_ARGB);
			current_image = new BufferedImage(back_image.getWidth(), back_image.getHeight(),
					BufferedImage.TYPE_INT_ARGB);
			
			green_back = frames.get_frame(2).toImage();
			
			Graphics g = back_image.getGraphics();
			
			//g.setColor(Color.MAGENTA);
			//g.fillRect(0, 0, m_item_holder_back.getWidth(), m_item_holder_back.getHeight());
			
			for (int i = 0; i < NB_MAX_HELD_ITEMS; i++)
			{
				g.drawImage(black_back, i*black_back.getWidth(), 0, null);
			}
			
			//ImageLoadSave.apply_mask(m_item_holder_back, Color.MAGENTA);
			
		}
		
		void fire_pressed()
		{
			switch (state)
			{
			case APPEARING: 
			case SHOWN:
				m_sfx_set.play(Sample.money_transfer);

				next_current_pos();
				break;
			case HIDDEN:
			case DISAPPEARING:
				m_sfx_set.play(Sample.inventory,0);
				
				state = ItemHolderState.APPEARING;
				// get object close to player
				if (over_item != null)
				{
					// find first free slot
					for (int i = 0; i < object_array.length; i++)
					{
						if (object_array[i] == null)
						{
							object_array[i] = over_item;
							item_taken = over_item;
							over_item = null;
							break;
						}
					}
				}
				break;					
			}
		}
		
		private int m_timer = 0;
		private int m_frame_rate = 20;
		
		void update(long elapsed_time)
		{
			m_timer += elapsed_time;
			
			while (m_timer > m_frame_rate)
			{
				m_timer -= m_frame_rate;

				switch (state)
				{
				case APPEARING:
					if (counter < max_counter)
					{
						counter += ITEM_HOLDER_STEP;
					}
					else
					{
						state = ItemHolderState.SHOWN;
					}
					break;
				case DISAPPEARING:
					if (counter >= 0)
					{
						counter -= 2;
					}
					else
					{
						counter = 0;
						state = ItemHolderState.HIDDEN;
					}
					break;
				case SHOWN:
					GfxObject cobj = object_array[current_pos];

					if ((next_item) && (cobj != null))
					{
						m_display_item_counter++;
					}
					if (m_display_item_counter > 20)
					{
						m_display_item_counter = 0;
						next_item = false;

						// describe the generic object

						if (!print_held_object(cobj))
						{
							// if no description made, describe it using the palette description

							print(cobj.get_properties().toString());
						}
					}
				case HIDDEN:
					break;
				}		
			}
		}
		
		public void render(Graphics2D g, int width, int height)
		{
			if (state != ItemHolderState.HIDDEN)
			{
				Graphics gi = current_image.getGraphics();
				
				gi.drawImage(back_image, 0,0,null);
				int w1 = green_back.getWidth();
				
				gi.drawImage(green_back, current_pos * w1, 0, null);
				
				int w = back_image.getWidth();
				int h = back_image.getHeight();
				int dx1 = (width - w)/2;

				
				int i = 0;
				for (GfxObject go : object_array)
				{
					if (go != null)
					{
						BufferedImage img = go.toMiniatureImage();
						
						int x = i*w1;
						gi.drawImage(img, x + (w1 - ITEMS_GAP - img.getWidth()) / 2, 
								(h - img.getHeight()) / 2, null);
					}
					i++;
				}
				
				switch (state)
				{
				case APPEARING:
				case DISAPPEARING:
					int y_ratio = (h * counter) / max_counter;
					int dy1 = height + (max_counter - y_ratio) / 2;
					int dx2 = dx1 + w;
					int dy2 = (dy1 + y_ratio);
					g.drawImage(current_image, dx1, dy1, dx2, dy2, 0, 0, w, h, null);
					break;
				case SHOWN:
					g.drawImage(current_image, dx1, height, null);
					break;

				}
			}
		}
		
	}
	
	private boolean m_landed;
	private boolean m_try_to_activate = false;
	private boolean m_try_to_enter_door = false;
	private boolean m_previous_fire = false;
	private boolean m_jump_pressed = false;
	
	private void init_fall(boolean from_jump)
	{
		m_jump.set_active(false);
		set_fall_frame();
		m_position_state = PositionState.FALL;
		m_fall_time = 0;
		m_fall_y = get_y();
		m_lateral_fall_timer = 0;
		
		// round it to the lower bound
		
		if (from_jump)
		{
			// round, but not too much
			m_x = (int)((m_x / (LATERAL_STEP))) * LATERAL_STEP;
		}
		else
		{
			// round by tile width
			m_x = m_level_data.get_grid().get_rounded_x(m_x);
		}
		
		//  if no longer falls, then, round it to the higher bound
		// (that should do it!)
		
		if (!may_fall())
		{
			m_x += LATERAL_STEP;
		}
		
		// add y offset already to avoid that the player crosses gap
		// at the same level
		
		m_y += VERTICAL_STEP;
	}
	
	public void get_activable_bounds(Rectangle r)
	{
		get_bounds(r);
		
		// add rectangle above player
		
		r.height = m_vert_resolution * 4;
		r.y -= m_vert_resolution;
		r.x += m_width / 3;
		r.width -= m_width / 3;
	}
	public void get_bounds(Rectangle r)
	{
		int left_offset = X_LEFT_OFFSET;
		int right_offset = X_RIGHT_OFFSET;
		
		/*if (m_right_left == 1)
		{
			int swp = left_offset;
			left_offset = right_offset;
			right_offset = swp;
		}*/
		
		r.x = get_x() + left_offset;
		r.y = (int)m_y;
		r.width = m_width - right_offset;
		r.height = m_height;
		
		if (m_position_state == PositionState.CROUCH)
		{
			// adapt to crouch state
			int y_offset = m_crouch_index >= 1 ? 42 : 20;
			r.y += y_offset;
			r.height -= y_offset;
		}
		
		if (is_invincible())
		{
			// add width
			int x_offset = 4;
			r.x -= x_offset;
			r.width += 2*x_offset;
		}
	}
	
	public boolean try_to_activate()
	{
		return m_try_to_activate;
	}
	public boolean try_to_enter_door()
	{
		return m_try_to_enter_door;
	}

	private boolean may_fall()
	{
		double yh = m_y + m_height;
		boolean blocked_left = is_vertical_way_blocked(m_x + X_RIGHT_OFFSET, yh);
		boolean blocked_right = is_vertical_way_blocked(m_x + m_width - X_RIGHT_OFFSET, yh);
		/*System.out.println("state: "+blocked_left+" "+blocked_right+" "+
				(m_x + X_LEFT_OFFSET)+" "+(m_x  + m_width - X_RIGHT_OFFSET)+
				" "+(m_x + X_LEFT_OFFSET)/64+" "+(m_x  + m_width - X_RIGHT_OFFSET)/64);*/

		return (!blocked_left && !blocked_right);
	}
	
	// lateral move while hero is falling
	
	private void update_fall(long elapsed_time, boolean key_left, boolean key_right)
	{
		m_fall_time += elapsed_time;
		// do not play fall sound at once

		// fall formula, adapted from h = 1/2gt^2 + h0, with a time in millis
		
		int old_y = get_y();
		
		m_y = m_fall_y + (m_fall_time*m_fall_time) / 1400;
		
		// make it linear if too fast
		
		if (m_y - old_y > MAX_FALL_SPEED)
		{
			m_y = old_y + MAX_FALL_SPEED;
		}
		
		// the hero has a limited number of lateral moves while falling
		
		if (m_lateral_fall_timer < MAX_LATERAL_FALL_COUNTER)
		{
			double delta_x = (FALL_STEP * elapsed_time) / 30.0;
			int yh = (int)m_y + m_height;

			if (key_left)
			{
				int x_check = (int)Math.ceil(m_x - delta_x);
				
				if (!is_vertical_way_blocked(x_check + X_LEFT_OFFSET, yh))
				{
					if (!m_level_data.is_lateral_way_blocked(x_check, m_y,m_height))
					{
						m_lateral_fall_timer += elapsed_time;
						m_x -= delta_x;
					}
					m_right_left = 1;
					set_fall_frame();
				}
			}
			else if (key_right)
			{
				int x_check = (int)Math.floor(m_x+ m_width + delta_x);
				
				if (!is_vertical_way_blocked(x_check - X_RIGHT_OFFSET, yh))
				{
					if (!m_level_data.is_lateral_way_blocked(x_check, m_y,m_height)) 
					{
						m_lateral_fall_timer += elapsed_time;
						m_x += delta_x;
					}
					m_right_left = 0;
					set_fall_frame();
				}
			}
		}

		
		
		if (!may_fall())
		{
			/*
			 * not 100% sure my formula does exactly the same, but it's linear
			 * all right, and since I calibrated it using the real game it should be close enough
			 * 
			 * (unlike the damage done by enemies!!)
			 * 
			 * 001152E     move.w  (word_366).w,d0	; fall height
00011532     subi.w  #20,d0
00011536     ble.s   loc_11544
00011538     tst.w   (InvulnerabilityStar_87_90_isActive).w
0001153C     bne.s   loc_11544
0001153E     asr.w   #1,d0
00011540     sub.w   d0,(Player_healthReal).w
			 */
			set_positionState(PositionState.CROUCH);
			
			landed();
			set_crouch_index(1);
			m_y = m_level_data.get_grid().get_rounded_y((int)m_y,false);
			
			double fall_height = m_y - m_fall_y;
			
			if (fall_height > m_max_fall_height_without_damage)
			{
				// damage to the player
				
				double damage = 2 * (fall_height - m_max_fall_height_without_damage) / m_vert_resolution;
				
				m_hurt_from_fall = true;
				
				hurt((int)damage);
			}
		}
		else
		{
			if (m_fall_time>150)
		
		{
			play_fall_sound();
		}
		}
	}
	
	public void heal(int heal_points)
	{
		int v = Math.min(MAX_HEALTH, m_targeted_health + heal_points);
		set_health(v);
		if (m_familiar!=null)
		{
			m_familiar.add_health(4*v);
		}
	}
	
	private long m_last_hurt_sound = 0;
	

	public void hurt(int damage_points)
	{
		if (!is_invincible())
		{
			// less damage when shield has been taken
			
			int actual_damage_points = damage_points;
			
			if (m_shield)
			{
				actual_damage_points /= 2;
				
				if (actual_damage_points == 0)
				{
					actual_damage_points = 1;
				}
			}
			
			int v = Math.max(0, m_targeted_health - actual_damage_points);
			set_health(v);
			
			
			long hurt_time = System.currentTimeMillis();
			
			if (hurt_time - m_last_hurt_sound > 500)
			{
				m_sfx_set.play(SfxSet.Sample.hero_hurt,m_hurt_2 ? 0 : 1);
				
				m_hurt_2 = !m_hurt_2;
				
				m_last_hurt_sound = hurt_time;
			}
		}
	}
	private void set_positionState(PositionState p)
	{
		m_position_state = p;
		
		/*
		switch(p)
		{
		case CROUCH:
			
			break;
		}*/
	}

	
	private class Jump
	{		

		private int m_start_x, m_start_y;
		private int m_index;
		private int m_x_index;
		protected boolean m_active;
		protected int m_direction;
		protected int m_really_elapsed_time;
		protected int m_elapsed_time;
		private boolean m_giant_jump;
		
		public int get_x()
		{
			return (int)Math.round(m_start_x + m_x_index * m_direction);
		}
		
		public int get_y()
		{
			int delta;
			// fix jump out bounds bug
			if (m_index<JUMP_WIDTH)
				{
				delta = JUMP_Y_TABLE[m_index];
				}
			else
			{
				delta = JUMP_Y_TABLE[JUMP_WIDTH-1];
			}
			if (m_giant_jump)
			{
				delta *= 2;
			}
			return m_start_y+delta;
		}
		
		

		
		public boolean is_active()
		{
			return m_active;
		}
		public void set_active(boolean active)
		{
			m_active = active;
		}
		
		private boolean y_way_blocked;
		private boolean allow_shoot;
		private boolean x_way_blocked;
		private int x_jump;
		private boolean from_ladder;
		
		private GfxFrameSet frames[];
		
		// cling to ladder test
		
		int get_y_start()
		{
			return m_start_y;
		}
		
		boolean may_grab_ladder()
		{
			int ha = (JUMP_WIDTH / 4);
			int index = (m_really_elapsed_time * 168)/1000;
			boolean rval = (index > ha);
			// && (blocked_x_index > ha);
			
			if (rval && from_ladder)
			{
				if (x_way_blocked)
				{
					rval = false;
				}
				/*else
				{
					int x_round = m_level_data.get_grid().get_rounded_x((int)m_x,m_right_left != 0);

					System.out.println("x "+m_x+" xround "+x_round+" delta="+Math.abs(x_round - (int)m_x));
					
					rval = x_round != x_jump && Math.abs(x_round - (int)m_x) < m_level_data.get_grid().get_tile_width()/8;
				}*/
			}
			
			return rval;
		}
				
		void init()
		{
			m_sfx_set.play(Sample.jump);
			
			m_jump_pressed = true;

			// store start y
			
			m_really_elapsed_time = 0;
			m_elapsed_time = 0;
			m_start_y = (int)m_y;
			m_start_x = (int)m_x;
			m_direction = get_walk_direction();
			m_giant_jump = m_giant_jump_timer > 0;
			m_active = true;
			
			x_jump = m_level_data.get_grid().get_rounded_x((int)m_x,false);

			y_way_blocked = false;
			allow_shoot = false;
			x_way_blocked = false;
			
			from_ladder = m_position_state == PositionState.CLIMB;

			m_position_state = PositionState.JUMP;
			m_crouch_timer = 0;
			
		}
		
		void end(boolean crouch)
		{
			set_active(false);
			allow_shoot = false;
			m_side_switch_timer = Integer.MAX_VALUE;
			
			if (!may_fall())
			{
				if (crouch)
				{
					set_positionState(PositionState.CROUCH);				
					landed();				
					set_crouch_index(1);
				}
				else
				{
					landed();
				}
				
				m_y = m_level_data.get_grid().get_rounded_y(m_y, false);
			}
			else
			{
				init_fall(true);
				m_fall_time = 100;
			}
		}
		
		void update(long elapsed_time)
		{
			GfxFrameSet fs = frames[m_right_left];
			
			allow_shoot = false;
			
			if (!y_way_blocked)
			{
				if ((m_index < 12) || (m_index > JUMP_WIDTH - 12))
				{
					if (from_ladder)
					{
						if (m_index < 12)
						{
							m_current = m_shoot_ladder[m_right_left].get_frame(3);
						}
						else
						{
							m_current = fs.get_frame(2);
						}
					}
					else
					{
						m_current = m_crouch[m_right_left].get_first_frame();
					}
				}
				else if (m_index < 24)
				{
					m_current = fs.get_first_frame();
				}
				else
				{
					allow_shoot = true;
					if (m_shoot_counter < 0)
					{
						m_current = fs.get_frame(2);
					}
					// else leave shoot manage the frame
				}
			}
			else
			{
				m_current = fs.get_frame(3);
			}
			
			// we consider a parabol starting from player top left
			
			double x, y = m_y;
			
			
			m_elapsed_time += elapsed_time;  // can be altered if way is blocked x-wise
			
	
			m_really_elapsed_time += elapsed_time; // cannot be altered
			
			m_x_index = (m_elapsed_time * 168)/ 1000;
			m_index= (m_really_elapsed_time * 168) / 1000;
					
			x = get_x();
			
			if (m_index >= JUMP_WIDTH)
			{
				m_active = false;
			}

			if (!y_way_blocked)
			{
				y = this.get_y(); // the y value of the parabol method
			}
			
			boolean reach_upper_floor = false;
			
			if (is_lateral_way_blocked_on_boundary(x,m_y))
			{
				// land on the upper platform in some cases (l2 classic bonus/secrets section)

				if ((m_index > JUMP_WIDTH - X_AMPLITUDE_OFFSET) && (!is_lateral_way_blocked_on_boundary(x, m_y - m_vert_resolution)))
				{
					reach_upper_floor = true;
					
					//m_y = y - m_vert_resolution*2;
					m_y = m_level_data.get_grid().get_rounded_y(y, false);
					debug("jump to upper ledge help "+m_y+" "+y);
					if (is_lateral_way_blocked_on_boundary(x,m_y))
					{
						m_y = m_level_data.get_grid().get_rounded_y(m_y-2, false);
						debug("jump to upper ledge big help "+m_y+" "+y);
					}
					x += (get_walk_direction() == 1) ? m_width/8 : -m_width/8;
					end(true);
					
					
					//set_crouch_index(1);
					y = m_y;
				}				
				else
				{

					// don't consider this time lapse as significant for x
					m_elapsed_time -= elapsed_time;

					x = m_x;

					x_way_blocked = true;

				}
			}
			else
			{
				
				if (!m_jump.from_ladder && (m_jump.get_y_start()>m_y+16))// &&(!is_lateral_way_blocked_on_boundary(x, m_y - m_vert_resolution)))
				{
				int y_off = (int)y+m_height+2;
				
				boolean vb1= (is_vertical_way_blocked(x+X_LEFT_OFFSET,y_off));
				boolean vb2=(is_vertical_way_blocked(x+m_width-X_RIGHT_OFFSET, y_off));
				if (vb1!=vb2)
				{
					m_y = ((int)y/16 - 1) * 16;
					debug("jump shorted "+m_y+" "+y);
					//m_y = y - m_vert_resolution;
					x += (get_walk_direction() == 1) ? m_width/4 : -m_width/4;
					end(false);
					y = m_y;
				}
				}
			}
			
			if (!m_landed)
			{
				if (y <= m_y)
				{					
					// dirty
					int x_left_offset = 26;
					int x_right_offset = 10;
					if (m_right_left == 1)
					{
						int swp = x_right_offset;
						x_right_offset = x_left_offset;
						x_left_offset = swp;
					}
					
					// up

					int y_check = (int)y;
					
					if (m_level_data.is_vertical_way_blocked((int)x+x_left_offset,y_check,1) || 
							m_level_data.is_vertical_way_blocked((int)x+m_width-x_right_offset,y_check,1))
					{
						
						// bumped his head on the ceiling: fall down
						
						y_way_blocked = true;					
						y = m_y;
						init_fall(true);
					}
				}
				else
				{
					// dirty
					int x_left_offset = 6;
					int x_right_offset = 12;
					if (m_right_left == 1)
					{
						int swp = x_right_offset;
						x_right_offset = x_left_offset;
						x_left_offset = swp;
					}
					// stop jump when reaching upper ledge, without ledge help

					if (is_vertical_way_blocked(x+x_left_offset,y+m_height) ||
							is_vertical_way_blocked(x+m_width-x_right_offset,y+m_height))
					{			
						m_y = y;
						end(true);
						y = (int)m_y;
					}
				}

				if (is_lateral_way_blocked_on_boundary(x,(int)m_y))
				{			
					x = (int)m_x;
				}

				m_x = x;
				m_y = y;
			}
			
			if (!m_landed && !is_active())
			{
				end(true);
			}
			
			// override current position: when reaching upper floor, it was
			// complex to finialize jump without having a small "fall" frame
			// ok this is not a very clear code, but it works !
			
			if (reach_upper_floor)
			{
				m_current = m_crouch[m_right_left].get_frame(2);
			}

		}
	}
	
	private Jump m_jump = new Jump();
	

	private GfxFrame get_frame(String name)
	{
		return m_hero_palette.lookup_frame_set(name).get_first_frame();
	}
	
	/*private GfxFrameSet get_frames(String name)
	{
		return m_hero_palette.lookup_frame_set(name);
	}*/
	
	private GfxFrameSet get_item_frames(String name)
	{
		return m_common_palette.lookup_frame_set(name);
	}
	
	private GfxFrame [] get_frame_and_symmetric(String name)
	{
		GfxFrame [] rval = new GfxFrame[2];
		rval[0] = get_frame(name);
		rval[1] = left_symmetry(rval[0]);

		return rval;
	}
	
	private GfxFrameSet [] get_frames_and_symmetric(String name)
	{
		GfxFrameSet [] rval = new GfxFrameSet[2];
		rval[0] = m_hero_palette.lookup_frame_set(name);
		rval[1] = left_symmetry(rval[0]);

		return rval;
	}	
	
	private GfxFrame left_symmetry(GfxFrame gf)	
	{
		return m_hero_palette.get_left_frame(gf);
	}
	
	private GfxFrameSet left_symmetry(GfxFrameSet gfs)	
	{
		return m_hero_palette.get_left_frame_set(gfs);
	}
	
	private void define_frames()
	{
		m_front = get_frame("player_front");
		
		m_back = get_frame("player_back");
		m_turn_front = get_frame_and_symmetric("player_face_turn_right");
		m_turn_back = get_frame_and_symmetric("player_back_turn_right");
		m_walk = get_frames_and_symmetric("player_walks_right");
		m_crouch = get_frames_and_symmetric("player_crouches_right");
		m_shoot_rest = get_frame_and_symmetric("player_rests_after_shoot_right");
		m_jump.frames = get_frames_and_symmetric("player_jumps_right");
		m_climb = new GfxFrame[4];
		m_climb[0] = get_frame("player_climbs_right");
		m_climb[1] = get_frame("player_climbs_match");
		m_climb[2] = get_frame("player_climbs_left");
		m_climb[3] = m_climb[1];
		
		m_death = get_item_frames("death");
		
		m_shoot_walk = get_frames_and_symmetric("player_shoots_right");
		m_shoot_jump = get_frames_and_symmetric("player_jumps_shoots_right");
		m_shoot_ladder = get_frames_and_symmetric("player_shoots_from_ladder_right");
		
		m_rotating_shield = get_item_frames("rotating_shield");
		
		m_nb_walk_frames = m_walk[m_right_left].get_nb_frames();

	}
	public int get_walk_direction()
	{
		return ((m_right_left == 0) ? 1 : -1);
	}
	
	private void handle_front_side_turn(long elapsed_time)
	{		

		if (m_position_state == PositionState.WALK)
		{
			int side_switch = m_side_switch_timer / REFERENCE_FRAME_RATE;			
			if (side_switch < 4)
			{
				switch (side_switch)
				{
				case 0:
					m_current = m_turn_front[m_right_left];
					break;
				case 1:					
						m_current = m_front;
					break;
				case 2:
                    //	avoid doing the 1-x thing more than once, bit of a hack I'm afraid					
					if ((m_current == m_front) || (m_previous_side_switch_index==0))
					{
						m_right_left = 1 - m_right_left;
						m_current = m_turn_front[m_right_left];
					}
					break;
				case 3:
					m_walk_counter = 1;
					m_current = m_walk[m_right_left].get_first_frame();
					break;
				}
				m_previous_side_switch_index = side_switch;
				
				m_side_switch_timer += elapsed_time;
			}
			else
			{
				// catch up in case we skipped some steps because CPU was too slow
				switch(m_previous_side_switch_index)
				{
				case 1:
					m_right_left = 1 - m_right_left;
					m_current = m_turn_front[m_right_left];
				case 2:				
					m_walk_counter = 1;
					m_current = m_walk[m_right_left].get_first_frame();
					break;
					default:
						break;
				}
				m_side_switch_timer = Integer.MAX_VALUE;
				m_previous_side_switch_index = Integer.MAX_VALUE;
			}
		}
	}

	private void handle_climb(long elapsed_time, boolean up)
	{
		m_climb_timer += elapsed_time;
		
		if (m_climb_timer >= CLIMB_RESOLUTION)
		{
			m_climb_timer = 0;
		
			m_climb_index++;
			
			boolean pant = false;
			
			if (m_climb_index == m_climb.length)
			{
				m_climb_index = 0;
				pant = true;
				m_sfx_set.play(Sample.step,0);
			}
			else if (m_climb_index == m_climb.length/2)
			{
				m_sfx_set.play(Sample.step,1);				
				pant = true;
			}
			if (pant)
			{
				if (Math.random() > 0.5)
				{
					m_sfx_set.play_random(SfxSet.Sample.panting);
				}
			}
			m_current = m_climb[m_climb_index];
	
			if (up) {m_y -= VERTICAL_STEP;} else {m_y += VERTICAL_STEP;}
		}
	}
	
	private int get_x_boundary(int x)
	{
		int boundary_x = (get_walk_direction() == 1) ?
				(boundary_x = x + m_width - X_LEFT_OFFSET) : x + X_LEFT_OFFSET;

		return boundary_x;
	}
	
	private int m_crouch_frame_counter = 0;
	
	private void set_crouch_index(int i)
	{
		m_crouch_index = i;
		m_crouch_frame_counter = 0;
		m_crouch_timer = 0;
	}
	
	private void next_crouch_index()
	{

		if (m_crouch_index < 0)
		{
			set_crouch_index(m_crouch_index+1);
		}
		else
		{
			m_crouch_frame_counter++;
			if (m_crouch_frame_counter == 4)
			{
				set_crouch_index(m_crouch_index+1);
			}
		}

	}
	
	private void handle_crouch(long elapsed_time, boolean key_down, 
			boolean key_action, boolean key_fire)
	{
		// check controls all the time (to avoid lost keypresses)
		
		if (!m_landed)
		{
			switch (m_crouch_index)
			{
			case 1:

				if (key_down)
				{
					if (key_action)
					{
						m_item_holder.fire_pressed();
					}
				}
				else
				{
					if (key_fire)
					{
						m_fire_while_standing_up = true;
					}
				}

				break;
			case 2:
				if (key_fire)
				{
					m_fire_while_standing_up = true;
				}

				break;

			}
		}

		// now synchronous stuff
		
		m_crouch_timer += elapsed_time;

		if (m_crouch_timer >= CROUCH_FRAME_RATE)
		{
			m_crouch_timer -= CROUCH_FRAME_RATE;
			
			switch (m_crouch_index)
			{
			case -1:
				if (key_down)
				{
					next_crouch_index();
					set_positionState(PositionState.CROUCH);
				}
				break;
			case 0:
				if (m_landed)
				{
					m_current = m_crouch[m_right_left].get_first_frame();
					next_crouch_index();
				}
				else
				{
					if (key_down)
					{
						m_current = m_crouch[m_right_left].get_first_frame();
						next_crouch_index();
					}
					else
					{
						m_current = m_walk[m_right_left].get_first_frame();
						set_crouch_index(-1);
						m_position_state = PositionState.WALK;
					}
				}
				break;
			case 1:
				if (m_landed)
				{
					m_current = m_crouch[m_right_left].get_frame(2);	
					next_crouch_index();
				}
				else
				{
					if (key_down)
					{
						m_current = m_crouch[m_right_left].get_frame(2);
					}
					else
					{
						
						next_crouch_index();
						m_current = m_crouch[m_right_left].get_first_frame();
					}
				}
				break;
			case 2:
				if (m_landed)
				{				
					if (m_hurt_from_fall)
					{
						m_current = m_crouch[m_right_left].get_frame(2);

						// wait till health has reached target

						if (m_targeted_health >= m_health)
						{
							m_hurt_from_fall = false;
						}
					}
					else
					{
						m_landed = false;
						m_current = m_shoot_rest[m_right_left];
					}
				}
				else
				{
					
					m_current = m_walk[m_right_left].get_first_frame();

					set_crouch_index(-1);
					m_position_state = PositionState.WALK;

					m_item_holder.handle_release();
				}
				break;

			}
		}
	}
	
	public boolean is_fire_pressed()
	{
		return m_engine.is_key_down(KeyEvent.VK_CONTROL) || (m_joystick != null && m_joystick.getButtons() == 1);
	}
	private boolean is_lateral_way_blocked_on_boundary(double x, double y)
	{
		return m_level_data.is_lateral_way_blocked(get_x_boundary((int)x),(int)y,m_height);
	}
	private boolean is_vertical_way_blocked(double x, double y)
	{
		return m_level_data.is_vertical_way_blocked((int)x,(int)y,m_width);
	}

	public void init_after_game_completed()
	{
		// keep score & lives, drop the rest
		m_money = 0;
		m_health = 0;
		m_targeted_health = MAX_HEALTH;
		m_life_state = LifeState.ALIVE;
		m_weapons = new HeroWeaponSet();
		m_shield = false;
		
	}
	public Hero(GameEngine ge, Joystick j,int starting_level)
	{
		m_engine = ge;
		m_score = 0;

		m_joystick = j;
		
		
		m_nb_lives = 2;

		init_after_game_completed();
		
		// compute the current world index from starting level + difficulty level *12/2 (after finishing 4 levels, difficulty level is 2)
		// first call to level_init increases it, so initialize to one less (less one more because level is one-indexed)
		m_total_level_index = (starting_level-2) + GameOptions.instance().get_start_difficulty_level().ordinal()*4;
		
		if (starting_level==1)
		{
			m_current_extra_life_score_index = 0;
			m_current_bonus_gem_score_index = 0;
			m_current_extra_life_score = EXTRA_LIFE_SCORE[m_current_extra_life_score_index];
			m_current_bonus_gem_score = BONUS_GEM_SCORE[m_current_bonus_gem_score_index];
			}
			else
			{
				// starting from a level != 1: not the same rules
				// ex: starting from level 2: next extra life will be at 300000
				m_current_extra_life_score_index = 10;
				m_current_bonus_gem_score_index = 10;
				m_current_extra_life_score = (starting_level+2)*100000;
				m_current_bonus_gem_score = m_current_extra_life_score+100000;
				
		}
	}


	public void level_init(LevelData level_data, MonsterLayer ml,
			StatusBar sb, SfxSet sfx, Rectangle view_bounds, int level_index)
	{
		m_level_data = level_data;
		
		m_sfx_set = sfx;
		
		m_status_bar = sb;
		m_common_palette = m_level_data.get_common_palette();
		m_hero_palette = m_level_data.get_hero_palette();
		m_view_bounds = view_bounds;

		if (m_item_holder == null)
		{
			m_item_holder = new ItemHolder();
		}
		
		define_frames();
		
		m_width = m_front.get_width();
		m_height = m_front.get_height();
		
		m_vert_resolution = JUMP_HEIGHT/2;
		
		m_hurt_from_fall = false;
		
		//m_tile_height = m_vert_resolution;
		
		m_max_fall_height_without_damage = m_vert_resolution * 7;
		
		/*if (m_weapons == null)
		{
			m_weapons = new HeroWeaponSet();
		}*/
		
		m_monster_layer = ml;
		
		m_weapons.init(m_common_palette,m_view_bounds,ml,m_level_data,m_sfx_set);


		m_total_level_index++;
		set_min_weapon_power_diff_level(MIN_WEAPON_LEVEL_TABLE[level_index-1]);

		
		//JUMP_WIDTH = (22 * m_level_data.get_grid().get_tile_width()) / 8; // 168
	}
	
	// start/restart
	public void set_position(int x,int y, boolean resurrect)
	{
		m_x = x;
		m_y = y;
		m_position_state = PositionState.FRONT;
		m_current = m_front;
		m_side_switch_timer = Integer.MAX_VALUE;
		m_previous_side_switch_index = Integer.MAX_VALUE;
		m_right_left = 0;
		m_turn_back_index = 0;
		m_crouch_index = -1;
		m_climb_index = -1;
		m_jump.set_active(false);
		m_item_holder.init();
		m_landed = false;
		m_shoot_counter = -1;
		m_lateral_walk_timer = 0;
		m_lateral_fall_timer = 0;
		m_hurt_from_fall = false;
		
		m_life_state = LifeState.ALIVE;

		if (resurrect)
		{
			set_health(Hero.MAX_HEALTH);
			set_invincibility(5);
		}
	}
	
	private int m_money_timer = 0;
	
	public void shop_exited()
	{
		m_sfx_set.pause(SfxSet.Loop.money_transfer);
		m_money = m_targeted_money;
	}
	
	public void handle_money(long elapsed_time)
	{
		m_money_timer += elapsed_time;
		
		while (m_money_timer > MONEY_TIMER_TICK)
		{
			m_money_timer -= MONEY_TIMER_TICK;
			
			int delta = m_targeted_money - m_money;

			if (delta >= 0)
			{
				m_money = m_targeted_money;

				m_sfx_set.pause(SfxSet.Loop.money_transfer);
			}
			else
			{
				delta = -delta;
				int dec = Math.min(500, delta);

				m_money -= dec;
			}
		}
	}

	public boolean is_standing_on_ground()
	{
		boolean standing_on_ground = (m_position_state == PositionState.WALK || 
				m_position_state == PositionState.FRONT || m_position_state == PositionState.BACK);
		return standing_on_ground;
	}
	
	public void handle_health(long elapsed_time)
	{
		if (m_life_state == LifeState.ALIVE)
		{
			int delta = m_targeted_health - m_health;

			if (delta < 0)
			{
				m_health -= 1;
			}
			else if (delta > 0)
			{
				m_health += 1;
			}

			if (m_health <= 0)
			{
				m_nb_lives--;
				debug("nb_lives = "+m_nb_lives);
				m_life_state = LifeState.EXPLODING;

				// stop falling sound if hero was falling at the time
				stop_fall_sound();
				
				m_sfx_set.play(Sample.hero_death);
				m_death_counter = 0;
			}
			
			// also handle score
			
			delta = m_targeted_score - m_score;
			
			if (delta > 0)
			{
				m_score += Math.min(delta, 1000);
				
				if (m_score >= m_current_bonus_gem_score)
				{
					m_current_bonus_gem_score_index++;
			
					String score = m_current_bonus_gem_score/1000+".000";
					String gem_message = Localizer.value("bonus gem for %POINTS% points",true).replace("%POINTS%",
							score);
					print("## "+gem_message+" ##");

					m_sfx_set.play(SfxSet.Sample.bonus_gem,1);

					m_item_awarded = new GfxObject(get_x(),get_y(),1,
							m_level_data.get_grid().get_tile_height(),"fire_chrystal_"+m_current_bonus_gem_score,
							m_level_data.get_level_palette().lookup_frame_set("fire_chrystal"));
					
					m_item_awarded.set_x(get_x_center() - m_item_awarded.get_source_set().get_width()/2);
					m_item_awarded.set_y(get_y() - m_item_awarded.get_source_set().get_height()*2);

					if (m_current_bonus_gem_score_index >= BONUS_GEM_SCORE.length)
					{
						m_current_bonus_gem_score += 200000;
					}
					else
					{
						m_current_bonus_gem_score = BONUS_GEM_SCORE[m_current_bonus_gem_score_index];
					}
					
				}
				if (m_score >= m_current_extra_life_score)
				{
					m_current_extra_life_score_index++;
					
					String score = m_current_extra_life_score/1000+".000";
					String score_message = Localizer.value("extra life for %POINTS% points",true).replace("%POINTS%",score);
					print("## "+score_message+" ##");
					
					m_sfx_set.play(SfxSet.Sample.bonus_gem,0);

					if (m_current_extra_life_score_index >= EXTRA_LIFE_SCORE.length)
					{
						m_current_extra_life_score += 200000;
					}
					else
					{
						m_current_extra_life_score = EXTRA_LIFE_SCORE[m_current_extra_life_score_index];
					}
					
					m_nb_lives++;
				}
			}
		}
	}
	
	public void enable_familiar(Familiar f)
	{
		m_familiar = f;
	}
	
	public Familiar disable_familiar()
	{
		Familiar rval = m_familiar;
		m_familiar = null;
		return rval;
	}
	
	public void update(long elapsed_time)
	{
		if (m_familiar != null)
		{
			m_familiar.update(elapsed_time);
			
			if (m_familiar.get_life_state() == LifeState.DEAD)
			{
				m_familiar = null;
			}
		}
		switch(m_life_state)
		{
		case ALIVE:
			update_alive(elapsed_time);
			break;
		case EXPLODING:
			update_exploding(elapsed_time);
			break;
		default:
			break;
		}
		
	}
	
	
	private void update_exploding(long elapsed_time)
	{
		int frame_counter = m_death_counter / (2*REFERENCE_FRAME_RATE) + 1;
		if (frame_counter < m_death.get_nb_frames())
		{
			m_current = m_death.get_frame(frame_counter);
		}
		
		m_death_counter += elapsed_time;
		
		int counter = m_death_counter / REFERENCE_FRAME_RATE;
				
		int max_counter = 10;
		
		int x0 = max_counter;
		int x_x0 = (x0 - counter);
		
		int y = get_y_center() + 2*(x_x0*x_x0 - x0*x0)/3;
				
		int radius = counter * 6;
				
		m_death_y[0] = y;
		m_death_y[1] = y + (int)(AngleUtils.sind(30)*radius);
		m_death_y[2] = y + (int)(AngleUtils.sind(45)*radius);
		m_death_x_offset[0] = radius;
		m_death_x_offset[1] = (int)(AngleUtils.cosd(30)*radius);
		m_death_x_offset[2] = (int)(AngleUtils.cosd(45)*radius);

		if (frame_counter >= m_death.get_nb_frames())
		{
			m_life_state = LifeState.DEAD;
			m_death_counter = 0;
		}
		
	}
	
	private void print(String m)
	{
		m_status_bar.print(m);
	}
	
	public void turn_to_left()
	{
		if (m_right_left == 0)
		{
			// player is facing right: turn to left
			turn();
		}
	}
	public void turn_to_right()
	{
		if (m_right_left == 1)
		{
			// player is facing left: turn to right
			turn();
		}
	}
	
	private void turn()
	{
		m_side_switch_timer = 0;
		m_previous_side_switch_index = Integer.MAX_VALUE;
		//m_right_left = 1 - m_right_left;
	}
	public void freeze_controls()
	{
		m_controls_frozen = true;
	}
	
	public void at_rest()
	{
		// reset walk frames
		m_walk_counter = 1;
		//m_lateral_walk_timer = 0;
		m_current = m_shoot_rest[m_right_left];
		m_controls_frozen = false;
	}
	public boolean is_switching_sides()
	{
		//return (m_previous_side_switch_index < 3);
		return (m_side_switch_timer != Integer.MAX_VALUE);
	}
	private void update_alive(long elapsed_time)
	{
		boolean lateral_direction_change = false;
		boolean lateral_walk = false;
		
		//if (m_engine.is_key_down(KeyEvent.VK_D)) { set_health(m_health - 30); }
		
		boolean key_left = m_engine.is_key_down(KeyEvent.VK_LEFT);
		boolean key_right = m_engine.is_key_down(KeyEvent.VK_RIGHT);
		boolean key_down = m_engine.is_key_down(KeyEvent.VK_DOWN);
		boolean key_up = m_engine.is_key_down(KeyEvent.VK_UP);
		boolean key_fire = m_engine.is_key_down(KeyEvent.VK_CONTROL);
		boolean key_jump = m_shift_jumps ? m_engine.is_key_down(KeyEvent.VK_SHIFT) : key_up;

		if (m_giant_jump_timer > 0)
		{
			m_giant_jump_timer -= elapsed_time;
		}
		if (m_joystick != null)
		{
			float x_pos = m_joystick.getXPos();
			float y_pos = m_joystick.getYPos();

			if (!key_left)
			{
				key_left = x_pos < -0.5;
			}
			if (!key_right)
			{
				key_right = x_pos > 0.5;
			}
			if (!key_up)
			{
				key_up = y_pos < -0.5;
			}
			if (!key_down)
			{
				key_down = y_pos > 0.5;
			}
			if (!key_fire)
			{
				key_fire = m_joystick.getButtons() == 1;
			}
			if (!key_jump)
			{
				key_jump = m_shift_jumps ? m_joystick.getButtons() == 2 : key_up;

			}
			
		}

		if (m_hurt_from_fall || m_controls_frozen)
		{
			// cancel controls
			key_up = false;
			key_left = false;
			key_fire = false;
			key_right = false;
			key_jump = false;
		}


		// avoid continuous press on fire button
		
		if ((key_fire) && (m_previous_fire))
		{
			key_fire = false;
		}
		else
		{
			m_previous_fire = key_fire;
		}

		boolean key_activate = key_fire;

		boolean on_ladder = is_on_ladder();
		
		boolean may_shoot = ((on_ladder && (key_right||key_left)) ||
				(m_position_state == PositionState.WALK) || (m_position_state == PositionState.FALL) || 
		((m_position_state == PositionState.JUMP) && m_jump.allow_shoot)) && (m_shoot_counter == -1);
		

		if (on_ladder)
		{
			m_item_holder.handle_release();
		}
		
		// check if player can shoot
		
		if (may_shoot && !m_weapons.can_shoot())
		{
			may_shoot = false;
		}
		
		// priority of turn back against shoot
		
		if (may_shoot && m_position_state == PositionState.WALK)
		{
			if ((key_left && m_right_left == 0) || (key_right && m_right_left == 1))
			{
				may_shoot = false;
			}
		}
		if (may_shoot)
		{
			// either fire pressed or fire memorized from standing up
			if (key_fire || m_fire_while_standing_up)
			{
				m_sfx_set.play(Sample.weapon_throw);

				m_shoot_counter = 0;
				m_shoot_frames = null;

				switch (m_position_state)
				{
				case WALK:
					m_shoot_frames = m_shoot_walk;
					break;
				case FALL:
				case JUMP:
					m_shoot_frames = m_shoot_jump;
					break;
				case CLIMB:
					m_right_left = key_right ? 0 : 1;

					m_shoot_frames = m_shoot_ladder;

					break;
				default:
					break;
				}
			}
			
			m_fire_while_standing_up = false;
		}
		
		if (is_invincible())
		{
			m_invincibility_timer -= elapsed_time;
			
			if (!is_invincible())
			{
				m_sfx_set.pause(SfxSet.Loop.invincibility);
			}
			
			m_shield_angle += Math.max(elapsed_time/5,0);
			
			if (m_shield_angle >= 360)
			{
				m_shield_angle -= 360;
			}
			m_shield_frame_timer += elapsed_time;
			
			while (m_shield_frame_timer > REFERENCE_FRAME_RATE)
			{
				m_shield_frame_timer -= REFERENCE_FRAME_RATE;
				
				m_shield_frame++;
				if (m_shield_frame > m_rotating_shield.get_nb_frames())
				{
					m_shield_frame = 1;
				}
			}
		}
		
		boolean shooting = (m_shoot_counter >= 0);
		
		if (shooting) 
		{
			m_shoot_timer += elapsed_time;
			while (m_shoot_timer > REFERENCE_FRAME_RATE)
			{
				m_shoot_timer -= REFERENCE_FRAME_RATE;

				int sequence_counter = m_shoot_counter/2;
				int frame_counter = shoot_frame_sequence[sequence_counter];			

				m_current = m_shoot_frames[m_right_left].get_frame(frame_counter);
				m_shoot_counter++;
				if (m_shoot_counter == 6)
				{
					m_weapons.shoot(get_x_boundary(get_x()), get_y(),get_walk_direction(),m_familiar);				
				}
				else if (sequence_counter == 3)
				{
					m_shoot_counter = -1;
					switch (m_position_state)
					{
					case WALK:
						m_current = m_shoot_rest[m_right_left];
						break;
					case CLIMB:
					{
						// revert to previous climb position

						m_current = m_climb[m_climb_index];
					}
					break;
					case FALL:
						set_fall_frame();
						break;
					}
				}
			}
		}
		
		boolean standing_on_ground = is_standing_on_ground();
		
		boolean on_ground = standing_on_ground || (m_position_state == PositionState.CROUCH);
		
		m_try_to_activate = false;
		m_try_to_enter_door = false;
		
		//handle_health(elapsed_time);
		

		if (!shooting)
		{
			if (standing_on_ground) 
			{
				handle_front_side_turn(elapsed_time);
			}

			if ((on_ground) && m_position_state != PositionState.BACK)
			{
				handle_crouch(elapsed_time,key_down,key_fire,key_fire);
			}
		}
		
		m_item_holder.update(elapsed_time);
		
		int current_left_right = -1;
		
		if (key_left)
		{
			current_left_right = 1;
		}
		else if (key_right)
		{
			current_left_right = 0;
		}
		if (!key_left && !key_right)
		{
			m_jump_pressed = false;
		}
		// jump management
		
		if ((!m_jump_pressed) && (!shooting) && (!m_jump.is_active()) && (key_jump) && (current_left_right != -1) && 
				(standing_on_ground || m_position_state == PositionState.CLIMB))
		{
			// cancel all flags
			
			standing_on_ground = false;
			on_ground = false;
			key_up = false;
			key_down = false;
			key_left = false;
			key_right = false;
			
			m_right_left = current_left_right;
			
			m_jump.init();
			
			
		}
		
			
		// new in v.06: if player not moving & walk position then reset to rest position (Ultron)

		if (!shooting && (current_left_right == -1) && (!is_switching_sides()) && (m_turn_back_index == 0) && (m_position_state == PositionState.WALK))
		{
			at_rest();
		}		
		if (standing_on_ground)
		{
			if (!shooting && m_side_switch_timer == Integer.MAX_VALUE)
			{
				// able to walk left/right

				if (current_left_right != -1)
				{
					lateral_walk = true;
				}
				
				// from back to left/right

				if (m_position_state == PositionState.BACK)
				{
					switch (m_turn_back_index)
					{

					case 0:
					case 2:
						m_current = m_turn_back[m_right_left];
						m_turn_back_index++; // PROBLEM
						break;
					case 1:
						m_current = m_back;
						if (lateral_walk)
						{
							m_turn_back_index++;
							m_right_left = current_left_right;
						}
						break;
					case 3:
						m_walk_counter = 1;
						m_current = m_walk[m_right_left].get_frame(m_walk_counter);
						m_position_state = PositionState.WALK;
						m_turn_back_index = 0;
						break;
					}
				}
				else
				{

					lateral_direction_change = (lateral_walk) && (m_right_left != current_left_right);

					// from front to left/right

					if ((m_position_state == PositionState.FRONT) && (lateral_walk))
					{
						m_position_state = PositionState.WALK;
						m_side_switch_timer = 2 * REFERENCE_FRAME_RATE;
						m_right_left = 1 - current_left_right; // make-up for switch
					}
					else if (lateral_direction_change)
					{
						m_side_switch_timer = 0;
					}
					else if (lateral_walk)
					{
						m_lateral_walk_timer += elapsed_time;

						if (m_lateral_walk_timer > lateral_cycle)
						{
							m_lateral_walk_timer -= lateral_cycle;

							lateral_cycle = (lateral_cycle >= LATERAL_CYCLE) ? lateral_cycle - REFERENCE_FRAME_RATE : lateral_cycle + REFERENCE_FRAME_RATE;


							int wd = get_walk_direction();
							int new_x = (int)(m_x + LATERAL_STEP * wd);

							if (DebugOptions.pass_through_walls || !is_lateral_way_blocked_on_boundary(new_x,m_y))
							{
								m_walk_counter++;
								m_current = m_walk[m_right_left].get_frame(m_walk_counter);


								if (m_walk_counter == m_nb_walk_frames)
								{
									m_walk_counter = 0;
									m_sfx_set.play(Sample.step,0);
								}
								else if (m_walk_counter == m_nb_walk_frames / 2)
								{
									m_sfx_set.play(Sample.step,1);								
								}

								m_x = new_x;
							}
						}
					}
				}

				if (may_fall())
				{
					init_fall(false);
				}
				else
				{
					stop_fall_sound();
				}


			}		
		}

		if (m_position_state == PositionState.JUMP)
		{
			m_crouch_timer += elapsed_time;
			
			if ((m_crouch_timer > CROUCH_BEFORE_JUMP_TIMER) || (m_jump.from_ladder))
			{
				m_jump.update(elapsed_time);
			}
			else
			{
				m_current = m_crouch[m_right_left].get_first_frame();
			}
		}
		if (key_up)
		{			
			TileGrid tg = m_level_data.get_grid();

			if (m_position_state == PositionState.CLIMB)
			{
				if (!is_vertical_way_blocked(m_x,m_y) &&
						(tg.is_over_ladder(get_x(), get_y() + VERTICAL_STEP, m_vert_resolution)))
				{
					
					handle_climb(elapsed_time,true);
				}
	
			}

			// we can have a transition to climb state if on ground or jumping (not at start)
			// and over a ladder
			
			boolean grab_ladder = false;
			if (!on_ground)
			{
				grab_ladder = (m_position_state == PositionState.JUMP) && m_jump.may_grab_ladder();
			}
			
			
			if (on_ground || grab_ladder)
			{				
				int x_test = get_x();
				
				if (on_ground) 
				{
					// center if on ground
					
					x_test += m_width / 2;
				}
				else if (m_right_left == 1)
				{
					// in air, to the left: add width
					x_test += m_width;
				}
					
				boolean over_ladder = tg.is_over_ladder(x_test, get_y(), m_vert_resolution);
				
				int round_x = 0;
				
				if (over_ladder)
				{
					// ladder x position
					
					round_x = tg.get_rounded_x(x_test,false);
					
					if (on_ground) 
					{
						m_y -= VERTICAL_STEP;
					}
					else
					{
						if (m_jump.from_ladder && m_jump.x_jump == round_x)
						{
							// cannot cling to the ladder we jumped from
							over_ladder = false;
						}
						else if (Math.abs(m_x - round_x) > 8)
						{
							// too far from the ladder
							over_ladder = false;
						}
					}
				}
				
				if (over_ladder)
				{
					m_x = round_x;
					m_position_state = PositionState.CLIMB;
					m_climb_index = 0;
					m_climb_timer = 0;
					m_current = m_climb[0];
					m_jump.set_active(false);
					stop_fall_sound();
					m_side_switch_timer = Integer.MAX_VALUE;
					m_landed = false;

				}
				
			}
			
			// from walk to back
			
			if ((m_position_state == PositionState.WALK) && (m_side_switch_timer == Integer.MAX_VALUE))
			{
				m_position_state = PositionState.BACK;
				m_turn_back_index = 0;
			}
		}

		else if (key_down)
		{
			if (m_position_state == PositionState.CLIMB)
			{
				handle_climb(elapsed_time,false);
								
				if (!may_fall())
				{
					// we touch the ground
					
					m_current = m_back;
					m_position_state = PositionState.BACK;
					m_y = m_level_data.get_grid().get_rounded_y(m_y);
				}
				else
				{
					// check if over ladder
					TileGrid tg = m_level_data.get_grid();
					
					if (!tg.is_over_ladder(get_x(), get_y() + m_vert_resolution, m_vert_resolution) && 
					!tg.is_over_ladder(get_x(), get_y() + m_height, m_vert_resolution))
					
					{
						init_fall(false);
						m_current = m_back; // like the original game
					}
				}
			}
			
			if (on_ground)
			{
				TileGrid tg = m_level_data.get_grid();

				// from the right
				int x = get_x() + m_width / 2;
				int y = get_y() + m_height;

				boolean over_ladder = tg.is_over_ladder(x, y, m_vert_resolution);

				if (!over_ladder)
				{
					// from the left
					x = get_x() + m_width / 4;
					
					over_ladder = tg.is_over_ladder(x, y, m_vert_resolution);
				}
				if (over_ladder && !is_vertical_way_blocked(x, y))
				{
					m_x = tg.get_rounded_x(x,false);
					m_y += VERTICAL_STEP;
					m_position_state = PositionState.CLIMB;
					m_climb_index = 0;
					m_climb_timer = 0;
					m_current = m_climb[0];
				}
			}
		}
		
		if (m_position_state == PositionState.BACK)
		{
			if (key_up)
			{
				m_current = m_back;
				m_try_to_enter_door = true;
			}
			if (key_activate)
			{
				m_try_to_activate = true;
			}
		}
		else if (m_position_state == PositionState.FALL)
		{
			update_fall(elapsed_time, key_left, key_right);
		}
		
	}

	public void on_over_item(GfxObject ob)
	{
		if (ob != null)
		{
			switch (ob.get_source_set().get_type())
			{
			case key:
			case pickable:
				m_item_holder.over_item = ob;
				break;
			default:
				break;

			}
		}
		else
		{
			m_item_holder.over_item = null;
		}
		
	}
	
	public void set_invincibility(int seconds)
	{
		m_invincibility_timer = seconds * 1000;
		m_sfx_set.play(SfxSet.Loop.invincibility);
	}

	/*private void object_bought(GfxObject ob)
	{
		m_item_holder.put_object(ob);
	}*/
	
	public void on_take_object(GfxObject ob, boolean from_shop)
	{
		String class_name = ob.get_class_name();
		
		if (!from_shop)
		{
			m_sfx_set.play(SfxSet.Sample.take_bonus);
		}
		
		if (!from_shop && ob.get_source_set().get_type() == GfxFrameSet.Type.money)
		{
			add_money(ob.get_source_set().get_properties().value);
			add_score(ob.get_source_set().get_properties().get_points(),true);
		}
		if (class_name.contains("energy"))
		{
			int health_points = 0;
			
			if (class_name.equals("half_energy"))
			{
				health_points = MAX_HEALTH / 2;
			} 
			else if (class_name.equals("full_energy"))
			{
				health_points = MAX_HEALTH;
			}
			
			heal(health_points);

			int offset = MAX_HEALTH - health_points;
			
			WavSample rval = m_sfx_set.get(SfxSet.Sample.energy);
			
			if (rval != null)
			{
				if (from_shop)
				{
					// from shop, different sound: exact energy recovery by the player
					// during play, sound is always the same

					int max_pos = rval.get_max_sample_position();
					offset = max_pos - (((m_targeted_health - m_health) * max_pos) / MAX_HEALTH);
				}


				rval.play_offset(offset);
			}					
		}
		else if (class_name.equals("chicken"))
		{
			heal(3);
		}
		else if (class_name.equals("apple"))
		{
			heal(4);
		}
		else if (class_name.equals("bread"))
		{
			heal(5);
		}
		else if (class_name.equals("starburst"))
		{
			if (from_shop)
			{
				m_item_holder.put_object(ob);
			}
			else
			{
				//m_weapons.spawn_stardust(ob.get_x(), ob.get_y());
				m_weapons.spawn_stardust(get_x_center(),get_y_center());
			}
		}
		else if (class_name.equals("familiar"))
		{
			if (m_familiar == null)
			{
				m_familiar = new Familiar();
				m_familiar.init(m_level_data, this, m_monster_layer);
			}
			else
			{
				// increase familiar health
				
				m_familiar.add_health(m_familiar.get_max_health());
				m_familiar.add_claws_power(1);
			}
		}
		else if (class_name.equals("familiar_power_claws"))
		{
			if (m_familiar != null)
			{
				m_familiar.add_claws_power(2);
			}
		}
		else if (class_name.equals("familiar_power_wings"))
		{
			if (m_familiar != null)
			{
				m_familiar.set_power_wings();
			}
		}
		else if (class_name.startsWith("weapon_arc_"))
		{
			String watype = class_name.substring(11); // extract wide, intense...
			HeroWeaponSet.Arc value = HeroWeaponSet.Arc.valueOf(watype);
			
			m_weapons.set_arc(value);
		}
		else if (class_name.equals("freeze"))
		{
			if (from_shop)
			{
				m_item_holder.put_object(ob);
			}
			else
			{
				m_monster_layer.freeze();
			}
			
		}
		else if (class_name.equals("invincibility"))
		{			
			if (from_shop)
			{
				m_item_holder.put_object(ob);
			}
			else
			{
				set_invincibility(15);
			}
		}
		else if (class_name.equals("shield"))
		{
			m_shield = true;
		}
		else if (class_name.equals("increase_wp_medium"))
		{
			m_weapons.increase_weapon_power(false);

			m_sfx_set.play(SfxSet.Sample.increase_wp);
		}
		else if (class_name.equals("increase_wp_full"))
		{
			m_weapons.increase_weapon_power(true);
			
			m_sfx_set.play(SfxSet.Sample.increase_wp);

		}
		else if (class_name.equals("extra_life"))
		{
			m_nb_lives++;

			m_sfx_set.play(SfxSet.Sample.bonus_gem,0);

		}
		else if (class_name.equals("giant_jump"))
		{
			m_giant_jump_timer = GIANT_JUMP_DURATION;
		}
		else
		{
			m_weapons.taken(class_name);
		}
	
	}
	
	public void give_weapon(String weapon_name)
	{
		m_weapons.taken(weapon_name);
	}
	public boolean remove_weapon(String weapon_name)
	{
		return m_weapons.removed(weapon_name);
	}
	public boolean owns_weapon(String weapon_name)
	{
		return m_weapons.owned(weapon_name);
	}
	public int weapon_power_of(String weapon_name)
	{
		return m_weapons.power_of(weapon_name);
	}
	public boolean owns_familiar()
	{
		return m_familiar != null;
	}
	
	public Familiar get_familiar()
	{
		return m_familiar;
	}
	// debug only
	public void give_object(GfxObject go)
	{
		m_item_holder.put_object(go);
	}
	
	public boolean print_held_object(GfxObject object)
	{
		boolean rval = false;
		
		GfxFrameSet.Type t = object.get_source_set().get_type();
		
		String object_name = object.get_name();
		
		if (t == GfxFrameSet.Type.key)
		{
			// tell the name to display by the object name
			rval = identify_key(object_name);
			
			if (!rval)
			{
				// no matching object name, try class name
				rval = identify_key(object.get_class_name());
			}
		}
		
		return rval;
	}
	
	public void render(Graphics2D g)
	{
		LifeState life_state = m_life_state;
		// avoid scattered player explosion bug
		if ((life_state == LifeState.EXPLODING) && (m_death_counter == 0))
		{
			life_state = LifeState.ALIVE;
		}
		
		if (m_familiar != null)
		{
			m_familiar.render(g);
		}
		//m_weapons.render(g);
		
		switch (life_state)
		{
		case ALIVE:
			draw_image(g,m_current.toImage());
					
			// draw invincibility
			if (is_invincible())
			{
				double center_x = get_x_center() - 10;//m_x + X_LEFT_OFFSET + (m_width - X_LEFT_OFFSET - X_RIGHT_OFFSET) / 2;
				double center_y = get_y_center();
				
				for (int i = 0; i < 4; i++)
				{
					double radius_x = m_width/1.5 * AngleUtils.cosd(i*90+m_shield_angle);
					double radius_y = m_height/1.5 * AngleUtils.sind(i*90+m_shield_angle);
					
					g.drawImage(m_rotating_shield.get_frame(m_shield_frame).toImage(), (int)(center_x + radius_x),
							(int)(center_y + radius_y), null);
				}
			}
		
		break;
		case EXPLODING:
			// draw 7 death sequences
			
			g.drawImage(m_current.toImage(),get_x(),m_death_y[0],null);
			
			for (int i = 0; i < 3; i++)
			{
				g.drawImage(m_current.toImage(),get_x()+ m_death_x_offset[i],m_death_y[i],null);
				g.drawImage(m_current.toImage(),get_x()- m_death_x_offset[i],m_death_y[i],null);
			}
			break;
		case DEAD:
			break;
		}
		
	}

	public int get_nb_lives() 
	{
		return m_nb_lives;
	}

	/*public void set_nb_lives(int nb_lives) 
	{
		m_nb_lives = nb_lives;
	}*/

	public SfxSet get_sfx_set()
	{
		return m_sfx_set;
	}
	
	public boolean can_shoot()
	{
		return m_weapons.can_shoot();
	}
	
	public int get_max_health()
	{
		return MAX_HEALTH;
	}
	
	public int get_health(boolean instantaneous) 
	{
		return instantaneous ? m_health : m_targeted_health;
	}

	public void set_health(int health) 
	{
		m_targeted_health = health;
		
	}
	
	public int square_distance_to(NamedLocatable go)
	{
		int dx = go.get_x() - get_x();
		int dy = go.get_y() - get_y();
		
		return dx*dx + dy*dy;
	}

	public void render_item_holder(Graphics2D g, int width, int height)
	{
		m_item_holder.render(g, width, height);
	}
	
	public String get_name() 
	{
		return "hero";
	}
	public boolean is_named()
	{
		return true;
	}
	
	public boolean is_on_ladder()
	{
		return m_position_state == PositionState.CLIMB;
	}
	public void steal_money(int money)
	{
		m_money -= money;
		if (m_money < 0)
		{
			m_money = 0;
		}
	}
	
	public void add_money(int money)
	{
		// during game
		m_targeted_money += money;
		m_money = m_targeted_money;
	}
	
	public boolean sub_money(int money)
	{
		boolean rval = (m_targeted_money >= money);
		
		if (rval)
		{
			m_targeted_money -= money;
			
			m_sfx_set.play(SfxSet.Loop.money_transfer);
		}
		
		return rval;
	}
	
	public int get_money()
	{
		return m_money;
	}
	
	public void set_money(int money)
	{
		m_targeted_money = money;
	}
	public int get_score() 
	{
		return m_score;
	}

	public int get_bonus_score()
	{
		return m_bonus_score;
	}
	
	public void reset_bonus_score()
	{
		m_bonus_score = 0;
	}
	public void add_score(int score, boolean add_to_bonus)
	{
		add_score(score, add_to_bonus,false);
	}
	public void add_score(int score, boolean add_to_bonus,boolean direct_add)
	{
		if (direct_add)
		{
			this.m_score += score;
		}
		this.m_targeted_score += score;

		if (add_to_bonus)
		{
			this.m_bonus_score += score;
		}
	}
	
	private void set_fall_frame()
	{
		m_current = m_jump.frames[m_right_left].get_frame(3);
	}
	
	private void play_fall_sound()
	{
		if (!m_fall_playing)
		{
			m_fall_playing = true;
			m_sfx_set.play(Sample.fall);
		}
	}
	private void stop_fall_sound()
	{
		if (m_fall_playing)
		{
			m_fall_playing = false;
			
			m_sfx_set.stop(Sample.fall);
		}
	}
	
	private void landed()
	{
		
		stop_fall_sound();
		
		if (!m_landed)
		{
			m_sfx_set.play(Sample.land);
		}
		m_landed = true;
		m_jump_pressed = false;
	}

	private static final String [] KEY_IDENTS = {"world_key","chest_key","treasure_key","trap_door_key",
		"door_key","room_key","teleport_key" };
	
	private boolean identify_key(String ident)
	{
		boolean rval = true;
		boolean key_found = false;
		
		for (String s : KEY_IDENTS)
		{
			if (ident.contains(s))
			{
				print("# "+Localizer.value(s.replace('_', ' '),true)+" #");
				key_found = true;
				break;
			}
		}

		if (!key_found)
		{
			rval = false;
		}
		
		return rval;
	}


	public boolean get_shield() {
		return m_shield;
	}


	public void set_shield(boolean shield) {
		this.m_shield = shield;
	}

}
