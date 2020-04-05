package gods.game;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

import gods.screens.*;
import gods.sys.*;
import gods.base.*;
import gods.base.layer.*;
import gods.base.associations.*;
import gods.game.SfxSet.Sample;
import gods.game.characters.*;
import gods.game.characters.LivingCharacter.LifeState;
import gods.game.items.*;

import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public abstract class GodsLevel extends GameState
{	
	private int m_bonus_index = 1;
	/////////////////////////////////////////////////////////////////
	// START API
	/////////////////////////////////////////////////////////////////	
	
	protected class BonusPosition
	{
		String name;
		int x;
		int y;
		
		public BonusPosition(String name, int x, int y) 
		{
			this.name = name;
			this.x = x;
			this.y = y;
		}
		
	}
	protected abstract class TimerEvent
	{
		protected long m_timeout;
		protected long m_counter = 0;
		
		protected TimerEvent(long timeout)
		{
			m_timeout = timeout;
		}
		boolean timeout()
		{
			return m_counter >= m_timeout;
		}
		public void update(long elapsed_time)
		{
			m_counter += elapsed_time;
			if (timeout())
			{
				on_timeout();
			}
		}
		protected abstract void on_timeout();
	}
	
	private class MusicTimerEvent extends TimerEvent
	{
		public MusicTimerEvent()
		{
			super(2000); // restart sound is played first
		}
		protected void on_timeout()
		{
			play_music();
		}
	}
	protected class TriggerEnableTimerEvent extends TimerEvent
	{
		private String m_trigger_name;
		
		public TriggerEnableTimerEvent(String trigger_name, long timeout)
		{
			super(timeout);
			m_trigger_name = trigger_name;
		}
		@Override
		protected void on_timeout() 
		{
			enable_trigger(m_trigger_name);			
		}
		
	}

	// for debug purposes
	protected void give_to_hero(String item_name)
	{
		GfxObject go = m_level_data.get_object_layer().get(item_name);
		if (go != null)
		{
			m_level_data.remove_object(go);
			m_hero.give_object(go);
		}
	}
	protected class HostileSpawnTimerEvent extends TimerEvent
	{
		private String m_hostile_name;
		
		public HostileSpawnTimerEvent(String hostile_name, long timeout)
		{
			super(timeout);
			m_hostile_name = hostile_name;
		}
		@Override
		protected void on_timeout() 
		{
			create_hostile(m_hostile_name);			
		}
		
	}
	protected abstract void on_boss_death();
	
	// auto-taken objects
	protected abstract boolean on_bonus_taken(GfxObject bonus);
	// picked objects
	protected abstract void on_object_picked_up(GfxObject object);
	
	protected abstract void on_room_entered(String room_door_name);
	protected abstract void on_world_restart(int world_count);
	protected abstract void on_lever_activated(GfxObject lever, LeverActivationState state);
	protected abstract void on_button_pressed(String button_name);
	protected abstract void on_object_drop(GfxObject bonus);
	protected abstract void on_door_change_state(String door_name, boolean is_open);
	protected abstract void on_level_loaded();
	
	// return true if trigger has worked and can be removed
	protected abstract boolean on_trigger_activated(String trigger_name);
	
	protected enum LeverActivationState { INITIAL_DEACTIVATED, DEACTIVATED, FIRST_ACTIVATED, ACTIVATED }
	protected enum ButtonActivationState { NOT_PRESSED, PRESSED_1, PRESSED_2, PRESSED_3, PRESSED_4, PRESSED_5 }
	
	protected SectoredLevelData m_level_data = new SectoredLevelData();
	protected Hero m_hero;
	static final String [] gold_chest_contents = {"small_gold_bag","small_gold_bag","gold_bag","gold_bag","coin","coin","coin","coin","necklace"};
	static final String [] steel_chest_contents = {"coin","coin","coin","coin","gold_bag","gold_mask","diamond_3"}; // unconfirmed
	static final String [] iron_chest_contents = {"small_gold_bag","gold_bag","coin","coin","gold_mask","coin","coin"};

	protected void disable_lever(String lever_name)
	{
		ObjectAssociation oa = m_level_data.get_association_set().get(lever_name);
		if (oa != null)
		{
			m_level_data.remove_association(oa);
		}
	}
	protected void start_clock()
	{
		m_start_clock_value = System.currentTimeMillis();
	}
	
	protected long get_clock()
	{
		return (System.currentTimeMillis() - m_start_clock_value) / 1000;
	}
	
	protected void lock_scrolling(String corner_name)
	{
		NamedLocatable top_left_corner = m_level_data.get_control_object(corner_name);
		m_scroll_lock_top_corner = top_left_corner;
	}
	
	protected void unlock_scrolling()
	{
		m_scroll_lock_top_corner = null;
	}
	
	protected void summon_shopkeeper(String [] items, boolean from_left)
	{
		// do not set shopkeeper state yet because hero may be jumping
		// on the token
		
		m_summon_shopkeeper = true;
		
		m_hero.freeze_controls();   // not allowed to move now that shopkeeper has been called
		
		int nb_items = items.length;
		boolean skip_shield = false;
		
		for (int i = 0; i<nb_items; i++)
		{
			if (items[i].equals("shield") && m_hero.get_shield())
			{
				// don't sell it if already has it (applies to mid-level shops only)
				skip_shield = true;
				break;
			}
		}
		if (skip_shield)
		{
			// don't sell the shield
			m_sold_items = new String[nb_items-1];
			int j=0;
			for (int i = 0; i<nb_items; i++)
			{
				if (items[i].equals("shield"))
				{
				}
				else
				{
					m_sold_items[j++] = items[i];
				}
			}
				
		}
		else
		{
			m_sold_items = items;
		}
		
		if (from_left)
		{
			m_hero.turn_to_left();
		}
		else
		{
			m_hero.turn_to_right();
		}

	}
	
	protected boolean display_special_bonus(String bonus_name)
	{
		boolean rval = (display(bonus_name));
		
		if (rval)
		{
			print("special bonus");
		}
		
		return rval;
	}
	protected boolean display_health_bonus(String bonus_name)
	{
		boolean rval = (m_hero.get_health(false) >= (m_hero.get_max_health())/2);
		
		if (rval)
		{
			rval = display(bonus_name);
			print("health bonus");
		}
		
		return rval;		
	}
	
	protected void game_over()
	{
		m_sfx_set.dispose();
		set_state(State.GAME_OVER);
		stop_music();
		print("");
		m_timer_events.clear();
		fadeout();
	}
	
	protected boolean display_health_and_lives_bonus(String bonus_name,int min_nb_lives)
	{
		return display_health_and_lives_bonus(bonus_name,min_nb_lives,22);
	}
	protected boolean display_health_and_lives_bonus(String bonus_name,int min_nb_lives,int min_health)
	{
		boolean rval = ((m_hero.get_nb_lives() > min_nb_lives) && 
				(m_hero.get_health(false) > min_health));
		if (rval)
		{
			rval = display(bonus_name);
			print("health and lives bonus");
		}
		
		return rval;
	}
	protected boolean display_lives_bonus(String bonus_name,int min_nb_lives)
	{
		boolean rval = (m_hero.get_nb_lives() >= min_nb_lives);
		
		if (rval)
		{
			rval = display(bonus_name);
			print("lives bonus");
		}
		
		return rval;
	}
	
	protected void set_hero_position(NamedLocatable location, boolean see_above,
			boolean resurrect, TimerEvent event)
	{
		if (event != null)
		{
			m_timer_events.add(event);
		}
		m_hero.set_position(location.get_x(), location.get_y(),resurrect);

		if (see_above)
		{
			// hack scroll system so the player sees above, independently from
			// previous position

			m_progressive_scroll = false;
			update_view_bounds(1);
			m_targeted_view_bounds.y -= m_targeted_view_bounds.height;
			m_progressive_scroll = false;
			update_view_bounds(1);
		}
	}
	protected void set_tile_behind(String before, String behind)
	{
		GfxFrameSet gfs_before = m_level_data.get_level_palette().lookup_frame_set(before);
		GfxFrameSet gfs_behind = m_level_data.get_level_palette().lookup_frame_set(behind);
		
		if ((gfs_before != null) && (gfs_behind != null))
		{
			GfxFrame gf_before = gfs_before.get_first_frame();
			GfxFrame gf_behind = gfs_behind.get_first_frame();
			
			gf_before.set_frame_behind(gf_behind);
		}
	}
	protected boolean zone_contains(String zone_name, String object_name)
	{
		ControlObject co = m_level_data.get_control_object(zone_name);
		boolean rval = false;

		if (co != null)
		{
			co.get_bounds(m_work_rectangle_3);
			GfxObject go = m_level_data.get_bonus(object_name);
			if (go != null)
			{
				rval = (m_work_rectangle_3.contains(go.get_x_center(),go.get_y_center()));

			}


		}
		else
		{
			warn("zone "+zone_name+" not found");
		}
		
		return rval;
	}
	
	protected void link_objects_around_boss(String location, int distance)
	{
		ControlObject co = m_level_data.get_control_object(location);

		m_level_data.link_objects_around_boss(co,distance);

	}

	
	protected void disable_trigger(String trigger_name)
	{
		ControlObject co = m_control_layer.get(trigger_name);
		if (co != null)
		{
			co.set_visible(false);
		}
		else
		{
			warn("trigger "+trigger_name+" not found");
		}
	}
	
	protected void enable_trigger(String trigger_name)
	{
		ControlObject co = m_control_layer.get(trigger_name);
		if (co != null)
		{
			co.set_visible(true);
		}
		else
		{
			warn("trigger "+trigger_name+" not found");
		}	
		}
	
	// return true if now open
	protected boolean toggle_door_state(String door_name)
	{
		Door d = m_level_data.doors_hash_set.get(door_name);
		if (d.is_open())
		{
			close_door(d,true);
		}
		else
		{
			open_door(d,true);
		}
		
		return d.is_open();
	}
	
	protected void change_destination_door(String door_name, String destination_door)
	{
		ObjectAssociation oa = m_level_data.get_association_set().get(door_name);
		if (door_name != null)
		{
			FaceDoorLocationAssociation fdla = (FaceDoorLocationAssociation)oa;
			ControlObject d = m_level_data.get_control_object(destination_door);
			
			fdla.get_face_door();
			
			fdla.set_object(1,d);
		}
	}
	
	protected boolean is_door_open(String door_name)
	{
		Door d = m_level_data.doors_hash_set.get(door_name);
		boolean rval = false;
		
		if (d != null)
		{
			rval = d.is_open();
		}
		return rval;
	}
	protected boolean open_door(String door_name)
	{
		return open_door(door_name,true);
	}
	
	protected boolean open_door(String door_name, boolean play_sfx)
	{
		Door d = m_level_data.doors_hash_set.get(door_name);
		boolean rval = false;
		
		if (d != null)
		{
			rval = open_door(d, play_sfx);
		}
		else
		{
			warn("door "+door_name+" not found");
		}
		return rval;
	}
	
	protected boolean close_door(String door_name)
	{
		return close_door(door_name,true);
	}
	
	protected boolean close_door(String door_name, boolean play_sfx)
	{
		boolean rval = false;
		Door d = m_level_data.doors_hash_set.get(door_name);
		if (d != null)
		{
			rval = close_door(d,play_sfx);
		}
		
		return rval;
	}
	
	protected boolean display_help_health_bonus(String bonus_name)
	{
		return display_help_bonus(bonus_name,0,m_hero.get_max_health() / 3,0);
	}

	// legacy method
	protected boolean display_help_lives_bonus(String bonus_name)
	{
		return display_help_bonus(bonus_name,0,24,0);
	}
	
	protected boolean display_help_bonus(String bonus_name,int min_nb_lives)
	{
		return display_help_bonus(bonus_name,min_nb_lives,24,0);
	}
	protected boolean display_help_bonus(String bonus_name,int min_nb_lives,int min_health)
	{
		return display_help_bonus(bonus_name,min_nb_lives,min_health,0);
	}
	protected boolean display_help_bonus(String bonus_name,int at_most_nb_lives,int at_most_health,int min_time,int at_most_score)
	{
		boolean rval = m_hero.get_score() < at_most_score;
		if (!rval)
		{
			rval = display_help_bonus(bonus_name, at_most_nb_lives, at_most_health, min_time);
		}
		return rval;
	}

	protected boolean display_help_bonus(String bonus_name,int at_most_nb_lives,int at_most_health,int min_time)
	{
		boolean rval = false;
		
		if ((m_hero.get_nb_lives() < at_most_nb_lives) && (m_hero.get_health(false) < at_most_health) && (get_elapsed_time() > min_time))
		{
			display(bonus_name);
			print("help bonus");
			rval = true;
		}
		
		return rval;
	}
	
	protected void debug(String message)
	{
		if (DebugOptions.debug)
		{
			System.out.println(this.getClass().getName()+": "+message);
		}
	}
	
	protected Hostile get_hostile(String hostile_name)
	{
		return m_monster_layer.lookup_hostile(hostile_name);
	}
	protected boolean is_hostile_alive(String hostile_name)
	{
		Hostile h = m_monster_layer.lookup_active_hostile(hostile_name);
		
		return (h != null) && (h.get_life_state() == LifeState.ALIVE); 
	}
	
	protected void set_demo_mode()
	{
		m_hero.set_keep_items_between_worlds(true);
		m_osd.set_demo_mode();
		m_demo_mode = true;
	}
	protected boolean display_speed_bonus(String bonus_name, int seconds_limit)
	{
		long clock = get_clock();
		boolean rval = (clock < seconds_limit);
		
		if (rval)
		{
			display(bonus_name);
			print("speed bonus");
		}
		else
		{
			debug("failed speed bonus: "+clock+"s vs max "+seconds_limit+"s");
		}
		return rval;
	}
	protected void show_display_animation(NamedLocatable l)
	{
		AnimatedFrames af = new AnimatedFrames();
		af.init(m_bonus_taken_smoke,
				50,AnimatedFrames.Type.ONCE,null);
		af.set_coordinates(l);
		
		m_bonus_appearing_list.add(af);
	}
	
	protected boolean make_visible(String object_name)
	{
		GfxObject go = m_level_data.get_bonus(object_name);
		boolean rval = (go != null) && (!go.is_visible());
		
		if (rval)
		{
			go.set_visible(true);
		}
		return rval;
	}
	protected boolean display(String object_name)
	{
		return display(object_name,true);
	}
	protected boolean display(String object_name, boolean with_sound)
	{
		GfxObject go = m_level_data.get_bonus(object_name);
		/*if (go == null)
		{
			warn("Object \""+object_name+"\" not found");
		}*/
		return display(go,with_sound);
	}
	
	protected boolean display(GfxObject go, boolean with_sound)
	{
		boolean rval = (go != null) && (!go.is_visible());
		
		if (rval)
		{
			go.set_visible(true);
			
			if (with_sound)
			{
				m_sfx_set.play(Sample.appearing_bonus);
			}
			
			AnimatedFrames af = new AnimatedFrames();
			af.init(m_bonus_taken_smoke,
					50,AnimatedFrames.Type.ONCE,go);
			
			af.set_coordinates(go);
			
			m_bonus_appearing_list.add(af);
		}
		return rval;
	}
	
	protected void print(String name)
	{
		m_status_bar.print(name);
	}
	
	protected GfxObject create_bonus(int x, int y, String class_name)
	{

		String name = "_bonus_"+m_bonus_index;
		m_bonus_index++;

		GfxFrameSet frame_set = m_level_data.get_level_palette().lookup_frame_set(class_name);

		GfxObject rval = new GfxObject(x,y,1,m_level_data.get_grid().get_tile_height(),name,frame_set);

		m_level_data.add_object(rval);
		
		return rval;

	}

	
	protected void create_hostile(String map_location, long delay)
	{
		add_timer_event(new HostileSpawnTimerEvent(map_location,delay));
	}

	protected boolean create_hostile(String map_location)
	{
		boolean rval = false;
		
		ControlObject co = m_level_data.get_control_object(map_location);
		if (co != null)
		{
			if (co.is_visible())
			{
				co.set_visible(false);
				
				rval = true;
				m_monster_layer.activate_hostile_wave(co.get_name());
			}
		}
		else
		{
			warn("Hostile "+map_location+" not found");
		}
		return rval;
		//return co;
	}


	

	protected void kill(String hostile_name)
	{
		m_monster_layer.kill(hostile_name);
	}
	protected void destroy(String hostile_name)
	{
		m_monster_layer.kill(hostile_name);
	}
	protected void teleport_to(String teleport_location)
	{
		teleport_to(m_control_layer.get(teleport_location));		
	}
	protected void add_timer_event(TimerEvent evt)
	{
		m_timer_events.add(evt);
	}
	
	protected static boolean is_lever_activated(LeverActivationState state)
	{
		return state == LeverActivationState.FIRST_ACTIVATED || state == LeverActivationState.ACTIVATED;
	}
	
	protected boolean is_lever_activated(String lever_name)
	{
		boolean rval = false;
		LeverActivationState las = get_lever_state(lever_name);
		
		if (las != null)
		{
			switch (las)
			{
			case FIRST_ACTIVATED:
			case ACTIVATED:
				rval = true;
				break;
			}
		}
		else
		{
			debug("lever \""+lever_name+"\" not found");
		}
		return rval;
	}
	protected LeverActivationState get_lever_state(String lever_name)
	{
		return m_level_data.lever_states.get(m_level_data.get_bonus(lever_name));
	}
	protected LeverActivationState get_lever_state(GfxObject lever)
	{
		return m_level_data.lever_states.get(lever);
	}
	protected boolean is_button_activated(String button_name)
	{
		return m_level_data.button_states.get(m_level_data.get_object_layer().get(button_name)).activated;
	}
	
	
	protected void move_block(String instance, boolean back)
	{
		MovingBlock p = m_level_data.get_moving_block(instance);

		if (p != null)
		{
			if (back)
			{
				p.reverse();
			}
			p.move();
		}
		else
		{
			warn("no associated platform to "+instance);
		}
	}
	
	protected GfxFrame get_tile(String tile_name)
	{
		return m_level_data.get_level_palette().lookup_frame_set(tile_name).get_first_frame();
	}
	protected MovingBlock get_moving_block(String instance)
	{
		MovingBlock p = m_level_data.get_moving_block(instance);
		
		return p;
	}
	protected void set_restart_location(String location)
	{
		ControlObject co = m_level_data.get_control_object(location);
		if (co != null)
		{
			m_restart_location = co;
		}
	}
	
	protected void set_item_price(String item, int price)
	{
		GfxFrameSet go = m_level_data.get_level_palette().lookup_frame_set(item);
		if (go != null)
		{
			go.get_properties().value = price;
		}
		else
		{
			warn("item \""+item+"\" not found in palette");
		}
		
	}
	/////////////////////////////////////////////////////////////////
	// END API
	/////////////////////////////////////////////////////////////////
			
	private static String [] KEY_NAMES = {"trap_door", "teleport", "treasure", "world"};
	
	private SfxSet m_sfx_set = new SfxSet(GameOptions.instance().get_sfx_state());
		
	private	Hostile m_boss = null;

	private BufferedImage m_copper_effect = null;
	private Shopkeeper m_shopkeeper;
	
	private File m_locale_file = null;
	
	private HostileWeaponSet m_hostile_weapon_set;

	private String [] m_sold_items = null;
	
	private enum State { INITIAL, LOADING, LOADED, RUNNING, SHOPKEEPER, PAUSED, FADEINOUT, 
		WORLD_END, LEVEL_END, GAME_END, GAME_OVER, PRESS_FIRE }
	
	private boolean m_progressive_scroll = false;
	
	private boolean m_on_boss_death_called = false;
	private long m_start_clock_value = 0;
	private String m_display_message = null;
	private ControlObject m_restart_location = null;
	private boolean m_player_start_entered = false;
	private StatusBar m_status_bar;
	private ActivableItems m_lever_activated = new ActivableItems();
	private boolean m_game_given_up = false;
	
	private OnScreenPlayerStatus m_osd;
	private Familiar m_saved_familiar = null;
	
	private State m_old_state = State.INITIAL;
	private State m_state = State.INITIAL;
	private int m_world_count = 0;
	private String m_level_music_file = null;
	private String m_current_music_file = null;
	
	private HashMap<String,String> m_lever_message_hash_set = new HashMap<String,String>();
	private MonsterLayer m_monster_layer;
	
	private DisappearingItems m_disappearing_items = new DisappearingItems();
	private AppearingItems m_appearing_items = new AppearingItems();
	private TimerEvents m_timer_events = new TimerEvents();
	
	private LevelSet m_level_set;

	private ControlObjectLayer m_control_layer;
	private Rectangle m_view_bounds = new Rectangle();
	private Rectangle m_targeted_view_bounds = new Rectangle();
	private int m_max_x, m_max_y;
	private int m_fourth_width, m_3_fourth_width, m_fourth_height, m_3_fourth_height;
	private AffineTransform m_translation = AffineTransform.getTranslateInstance(0,0);
		
	private Rectangle m_work_rectangle = new Rectangle();
	private Rectangle m_work_rectangle_2 = new Rectangle();
	private Rectangle m_work_rectangle_3 = new Rectangle();
	
	private GfxFrameSet m_bonus_taken_smoke;
	private boolean m_demo_mode = false;
	
	private LinkedList<AnimatedFrames> m_bonus_taken_list = new LinkedList<AnimatedFrames>();
	private LinkedList<AnimatedFrames> m_bonus_appearing_list = new LinkedList<AnimatedFrames>();
	private LinkedList<Renderable> m_scrolled_renderable = new LinkedList<Renderable>();
	private LinkedList<Renderable> m_fixed_renderable = new LinkedList<Renderable>();
		
	private boolean m_summon_shopkeeper = false;
	private boolean m_pre_end_message = false; // flag set just before "level end" and "game end" appears
	
	private NamedLocatable m_scroll_lock_top_corner = null;
	
	public GodsLevel(String [] start_shop_items) throws IOException
	{
		set_fadeinout_time(500, 1000, 0);
		
		m_sold_items = start_shop_items;
		
	}

	private void create_hostile_wave(ControlObject location)
	{
		if (location.is_visible())
		{
			// do it only once unless reset of the location
			
			HostileWaveParameters hwp = m_level_data.get_hostile_params().get(location);

			if (hwp == null)
			{
				warn("monster location "+location+" not found");
			}
			else
			{	
				m_monster_layer.activate_hostile_wave(location.get_name());
			}
			location.set_visible(false);
		}
	}
	


	
	private void create_hostiles()
	{
		Collection<HostileWaveParameters> l = m_level_data.get_hostile_params().items();
	
		for (HostileWaveParameters hwp : l)
		{
			m_monster_layer.create_hostile_wave(hwp);
		}
		
	}

	
	private void play_music()
	{
		if (GameOptions.instance().get_music_state())
		{
			if (m_current_music_file != null)
			{
				
				File f = new File(DirectoryBase.get_mp3_path() + m_current_music_file);
				if (f.isFile())
				{
					Mp3Play.play(f.getAbsolutePath());
				}
			}
		}
	}
	
	private boolean is_boss_alive()
	{
		return (m_boss != null) && (m_boss.get_life_state() == LifeState.ALIVE);
	}
	
	// Level Update method
	
	private void update_hero()
	{
		switch (m_hero.get_life_state())
		{
		case DEAD:
			if (m_hero.get_nb_lives() < 0)
			{			
				m_display_message = Localizer.value("GAME  OVER",true);
				m_sfx_set.dispose();
				play_mp3_sound("game_over");
				m_hero.set_life_state(LifeState.STAND_BY);
				set_state(State.PRESS_FIRE);
			}
			else
			{
				m_hero.set_life_state(LifeState.STAND_BY);
				
				// resume play after death
				
				m_progressive_scroll = false;
				
				add_timer_event(new RestartFadeOutEvent());
			}
			break;
		case EXPLODING:
			stop_music();
			
			break;
		case ALIVE:
			if (DebugOptions.debug)
			{
				// special cheat keys
				if (m_game.is_key_pressed(KeyEvent.VK_L))
				{
					// level ends
					level_end();
				}
				
				if (m_game.is_key_pressed(KeyEvent.VK_T))
				{
					// through walls
					DebugOptions.pass_through_walls = !DebugOptions.pass_through_walls;
				}
				if (m_game.is_key_pressed(KeyEvent.VK_S))
				{
					// invincible
					m_hero.set_invincibility(15);
				}
			}
			if (m_game.is_key_pressed(KeyEvent.VK_ESCAPE))
			{				
				m_display_message = Localizer.value("QUIT  GAME",true);
				m_game_given_up = true;
				game_over();
			}
			Sector<GfxObject>.Distance closest_object = null,closest_alternate_object = null;
			GfxObject go = null;
			
			if (!is_boss_alive())
			{
			// check if bonus close to hero (auto-picked category)
	
				closest_object = m_level_data.bonus_sector_set.closest(m_hero,null);
	
				go = closest_object.item;
			
				if (go != null)
				{
					go.get_bounds(m_work_rectangle_2);

					// cannot take object if bouncing up (allows the player to actually see
					// which object he his about to take)

					if (go.may_be_taken())
					{

						if (m_work_rectangle.intersects(m_work_rectangle_2))
						{								
							// remove object from map & sector set

							//closest_object.source.remove(go);				
							if (m_level_data.remove_object(go))
							{
								debug("removed object "+go.get_name());
							}
							else
							{
								debug("unable to remove object "+go.get_name());
							}
							

							AnimatedFrames af = new AnimatedFrames();
							af.init(m_bonus_taken_smoke,
									50,AnimatedFrames.Type.REVERSE);
							af.set_coordinates(go);

							m_bonus_taken_list.add(af);

							GfxObject ob = closest_object.item;

							m_hero.on_take_object(ob,false);


							if ((ob != null) && ob.get_source_set().get_name().equals("advice"))
							{
								print_hint(ob.get_name());
							}
							else
							{
								if (!on_bonus_taken(ob))
								{
									print_bonus_message(ob);
								}
							}

							ObjectAssociation oa = m_level_data.get_association_set().get(ob.get_name());

							// check association with object (teleport gem)

							if ((oa != null) && (oa instanceof TeleportLocationAssociation))
							{
								TeleportLocationAssociation tla = (TeleportLocationAssociation)oa;
								teleport_to(tla.get_location());
							}
						}
					}
				}

			}

			go = m_hero.item_picked_up();

			if (go != null)
			{
				// hero just picked up an object: remove from playfield

				if (m_level_data.remove_object(go))
				{
					debug("removed object "+go.get_name());
				}
				else
				{
					debug("could not remove object "+go.get_name());
				}

				m_hero.print_held_object(go);

				// call callback for user defined function
				
				on_object_picked_up(go);
			}
	
			go = m_hero.item_dropped();
	
			if (go != null)
			{
				// hero just released an object: re-insert in the playfield
				go.set_initial_fall_speed(-4);
				
				m_level_data.drop_object(go);
	
				on_object_drop(go);
			}
			
			go = m_hero.item_awarded();

			if (go != null)
			{
				show_display_animation(go);
				
				// hero just been awarded an object: insert in the playfield
				//go.set_initial_fall_speed(-4);
				
				m_level_data.add_object(go);
			}
			
			
			// check if item close to hero (pickable & chests)
	
			closest_object = m_level_data.items_sector_set.closest(m_hero,null);
	
			go = closest_object.item;

			if (go != null)
			{
				go.get_bounds(m_work_rectangle_2);

				if (m_work_rectangle.intersects(m_work_rectangle_2))
				{
					if (go.get_source_set().get_type() == GfxFrameSet.Type.chest)
					{
						// chest: cannot be picked
						handle_chest_key_open(go);
						// find another object to pick (avoids that chest mask a key)
						closest_alternate_object = m_level_data.items_sector_set.closest(m_hero,go);
						GfxObject other_go = closest_alternate_object.item;
						if (other_go!=null)
						{
							other_go.get_bounds(m_work_rectangle_2);

							if (m_work_rectangle.intersects(m_work_rectangle_2))
							{
								go = other_go; // show the other object
							}
							else
							{
								go = null; // chest is alone in the intersection areas
							}
						}
					}
					if (go!=null)
					{
						m_hero.on_over_item(go);
					}

					
				}
				else
				{
					m_hero.on_over_item(null);
				}
			}
	
			handle_levers();
			break;
		default:
			break;
		}
	}
	
	
	private void print_hint(String hint)
	{
		print("# "+Localizer.value("hint",true)+" # "+Localizer.value(hint,true));
	}
	
	private void handle_levers()
	{
		// find the closest activable object

		Sector<GfxObject>.Distance closest_object = m_level_data.activable_sector_set.closest(m_hero);

		GfxObject activable = closest_object.item;

		if (activable != null)
		{
			m_hero.get_activable_bounds(m_work_rectangle);

			activable.get_bounds(m_work_rectangle_2);

			if (m_work_rectangle.intersects(m_work_rectangle_2))
			{
				boolean just_activated = m_hero.try_to_activate();
				
				if (just_activated)
				{
					m_sfx_set.play(Sample.lever_activated);
				}
				
				if (activable.get_source_set().get_name().equals("lever"))
				{
					// under a lever: get state
					
					LeverActivationState state = m_level_data.lever_states.get(activable);
					
					boolean first_activated = false;
					boolean activated = false;
					
					// hero pressed activate: change state
					
					if (just_activated)
					{						
						switch(state)
						{
						case FIRST_ACTIVATED:
						case ACTIVATED:							
							state = LeverActivationState.DEACTIVATED;
							break;
						case INITIAL_DEACTIVATED:
							first_activated = true;
							activated = true;
							state = LeverActivationState.FIRST_ACTIVATED;
							break;
						case DEACTIVATED:
							activated = true;
							state = LeverActivationState.ACTIVATED;
							break;						
						}
					}
					else
					{
						// not pressed, just updating activated status
						switch (state)
						{
						case FIRST_ACTIVATED:
						case ACTIVATED:							
							activated = true;
							break;
						case INITIAL_DEACTIVATED:
						case DEACTIVATED:
							activated = false;
							break;						
						
						}
					}
					
					if (activated)
					{
						// handle associations

						ObjectAssociation oa = m_level_data.get_association_set().get(activable.get_name());

						if (oa != null)
						{
							if (oa instanceof LeverDoorKeyAssociation)
							{
								// open when lever is triggered and the correct key is handed

								LeverDoorKeyAssociation ldka = (LeverDoorKeyAssociation)oa;
								String key_name = ldka.get_key().get_name();

								if ((ldka.get_key() != null) && !m_hero.owns_named_object(key_name))
								{
									if (m_lever_message_hash_set.get(key_name) == null)
									{
										if (just_activated)
										{
											for (String s : KEY_NAMES)
											{
												if (key_name.contains(s))
												{
													String m = Localizer.value("you need the %KEY%",true);
													s = Localizer.value(s.replace('_', ' ')+" key",true);
													print(m.replace("%KEY%", s));
													break;
												}
											}
											m_lever_message_hash_set.put(key_name, key_name);
										}
									}
								}
								else
								{
									if ((ldka.get_key() == null) || m_hero.owns_named_object(key_name))
									{
										// a key is required and the hero owns it

										ControlObject door = ldka.get_door();
										String door_name = door.get_name();
										toggle_door_state(door_name);
										
											m_hero.steal_named_object(key_name);
											m_lever_message_hash_set.put(key_name, key_name);
										
										
									}
								}
							}
							else if (oa instanceof LeverPlatformKeyAssociation)
							{
								// open when lever is triggered and the correct key is handed
								LeverPlatformKeyAssociation ldka = (LeverPlatformKeyAssociation)oa;
								String key_name = ldka.get_key().get_name();


								if ((ldka.get_key() == null) || m_hero.steal_named_object(key_name))
								{
									// a key is required and the hero owns it

									ControlObject platform = ldka.get_platform();
									String platform_name = platform.get_name();
									move_block(platform_name,false);
								}
							}
							else if (oa instanceof LeverPlatformAssociation)
							{								
								// open when lever is triggered
								LeverPlatformAssociation lpa = (LeverPlatformAssociation)oa;

								ControlObject platform = lpa.get_platform();
								String platform_name = platform.get_name();
								move_block(platform_name,false);

								// can be done only once
								
								m_level_data.remove_association(oa);

							}
							else if ((first_activated) && (oa instanceof LeverMonsterKillAssociation))
							{
								LeverMonsterKillAssociation lmka = (LeverMonsterKillAssociation)oa;

								for (int i = 0; i < lmka.get_nb_monsters(); i++)
								{
									String monster_name = lmka.get_monster(i).get_name();
									kill(monster_name);
								}

							} else if ((first_activated) && (oa instanceof LeverItemDisplayAssociation))
							{
								LeverItemDisplayAssociation lida = (LeverItemDisplayAssociation)oa;

								display(lida.get_item_to_display(),true);
								
								m_level_data.remove_association(lida);

							} else if ((first_activated) && (oa instanceof LeverDoorAssociation))
							{
								// open when lever is triggered

								LeverDoorAssociation lda = (LeverDoorAssociation)oa;
								ControlObject door = lda.get_door();
								String door_name = door.get_name();
								toggle_door_state(door_name);
								
								
								m_level_data.remove_association(lda);

							}
						}
					}
					if (state != LeverActivationState.INITIAL_DEACTIVATED)
					{
						m_level_data.lever_states.put(activable,state);

						on_lever_activated(activable,state);
					}

				}
				else
				{
					// secret button

					SectoredLevelData.ButtonState state = m_level_data.button_states.get(activable);
					
					if (just_activated)
					{
						state.press_state = ButtonActivationState.PRESSED_1;

						ObjectAssociation oa = m_level_data.get_association_set().get(activable.get_name());

						if (oa != null)
						{
							if (!state.activated)
							{
								if (oa instanceof LeverDoorAssociation)
								{
									// open when button is pushed
									LeverDoorAssociation lda = (LeverDoorAssociation)oa;
									String door_name = lda.get_door().get_name();
									toggle_door_state(door_name);

								}
								else if (oa instanceof LeverPlatformAssociation)
								{								
									// open when button is pushed
									LeverPlatformAssociation lpa = (LeverPlatformAssociation)oa;

									ControlObject platform = lpa.get_platform();
									String platform_name = platform.get_name();
									move_block(platform_name,false);

									// can be done only once

									m_level_data.remove_association(oa);

								}
							}
						}
						state.activated = true;
					}

					if (state.activated)
					{
						on_button_pressed(activable.get_name());
					}
				}
			}
		}

	}



	private void handle_chest_key_open(GfxObject go)
	{

		if (m_hero.try_to_open_chest(go))
		{
			// print the chest description

			print_bonus_message(go);

			// open the chest

			go.set_current_frame(2);

			// play the "open chest" sound

			m_sfx_set.play(Sample.open_close,0);

			// "drop the chest" effect

			go.set_y(go.get_y()-go.get_height());


			// add points due to chest opening

			m_hero.add_score(go.get_properties().get_points(),true);
			m_hero.add_money(go.get_properties().value);

			// make all hidden bonus appear if they are close enough to the chest

			String [] chest_contents = null;

			if (go.get_class_name().equals("gold_chest"))
			{
				// gold chest: 3 small, 3 coins, 1 necklace
				chest_contents = gold_chest_contents;

			}
			else if (go.get_class_name().equals("steel_chest"))
			{
				chest_contents = steel_chest_contents;

			}
			else if (go.get_class_name().equals("iron_chest"))
			{
				// iron chest: 1 small gold bag, 2 gold bags, 1 mask, 2 coins
				chest_contents = iron_chest_contents;
			}

			// steel chest: 2 coins, 1 gold bag, 1 mask, 1 diamond3
			go.spawn_bonuses(m_level_data,chest_contents);

		}
	}



	private String m_loading_message = null;

	@Override
	protected void p_render(Graphics2D g) 
	{		
		/*if (m_clear_screen_requested)
		{
			clear_screen(g);
		}*/
		
		switch (m_state)
		{
		case INITIAL:
		case LOADING:
		case LOADED:
		
			// render helmet
			g.drawImage(m_helmet,0,0,null);
			
			if (m_loading_message == null)
			{
				// toUpperCase does not work properly when Locale is Turkish for example
				// (i are capitalized to I with an accent on it!) hence the parameter to force
				// the english locale
				
				String level_name = Localizer.value(m_level_set.get_name().toUpperCase(Locale.ENGLISH).replace('_',' '));
				
				if (m_level_set.get_nb_levels() > 1)
				{
					level_name += " " + Localizer.value("LEVEL")+" "+m_level_set.get_level_index();
				}
				m_loading_message = Localizer.value("LOADING")+"\n"+level_name;
			}
			
			GOLDEN_BIG_FONT.write(g, m_loading_message, getWidth()/2, 142, 0,
					true, false, 20);

			m_status_bar.render(g);
		
			break;
		default:
			// set clip
			
			g.setClip(0,0,m_view_bounds.width,m_view_bounds.height);

			// set translation

			m_translation.setToTranslation(-m_view_bounds.x, -m_view_bounds.y);

			// set sky color effect

			m_level_data.set_copper_data(m_view_bounds.y, m_copper_effect);

			g.transform(m_translation);

			//clear_composite(g);

			for (Renderable r : m_scrolled_renderable)
			{				
				r.render(g);
			}

			if (m_state == State.SHOPKEEPER)
			{
				m_shopkeeper.render(g);
			}
			// restore translation

			m_translation.setToTranslation(m_view_bounds.x, m_view_bounds.y);

			g.transform(m_translation);
			g.setClip(null);

			for (Renderable r : m_fixed_renderable)
			{
				r.render(g);
			}

			if (m_display_message != null)
			{
				GOLDEN_BIG_FONT.write(g, m_display_message, getWidth()/2, 142, 0,
						true, false, 20);
			}
		}
	}
	
	private void update_view_bounds(long elapsed_time)
	{
		if ((m_hero.is_on_ladder() || m_scroll_lock_top_corner != null))
		{
			// smooth y scrolling
			update_view_bounds_xy(elapsed_time);
		}
		else
		{
			// y-following scrolling
			update_view_bounds_x(elapsed_time);				
		}
	}
	
	private void update_view_bounds_x(long elapsed_time)
	{
		if (m_scroll_lock_top_corner != null)
		{
			m_targeted_view_bounds.x = m_scroll_lock_top_corner.get_x();
			m_targeted_view_bounds.y = m_scroll_lock_top_corner.get_y();
		}
		else
		{
			int hx = m_hero.get_x()+m_hero.get_width()/2;
			int hy = m_hero.get_y()+m_hero.get_height()/2;

			int x_min = hx - m_targeted_view_bounds.x;
			int x_max = x_min;
			int y_min = hy - m_targeted_view_bounds.y;
			int y_max = y_min;

			x_min = Math.min(x_min, m_fourth_width);
			x_max = Math.max(x_max, m_3_fourth_width);
			y_min = Math.min(y_min, m_fourth_height);
			y_max = Math.max(y_max, m_3_fourth_height);


			if (x_max > m_3_fourth_width)
			{
				m_targeted_view_bounds.x = hx - m_3_fourth_width;
			}
			if (x_min < m_fourth_width)
			{
				m_targeted_view_bounds.x = hx - m_fourth_width;
			}
			if (y_max > m_3_fourth_height)
			{
				m_targeted_view_bounds.y = hy - m_3_fourth_height;
			}
			if (y_min < m_fourth_height)
			{
				m_targeted_view_bounds.y = hy - m_fourth_height;
			}
		}
		// limit on min and max bounds

		if (m_targeted_view_bounds.x < 0)
		{
			m_targeted_view_bounds.x = 0;
		}
		else if (m_targeted_view_bounds.x + m_targeted_view_bounds.width > m_max_x)
		{
			m_targeted_view_bounds.x = m_max_x - m_targeted_view_bounds.width;
		}

		if (m_targeted_view_bounds.y < 0)
		{
			m_targeted_view_bounds.y = 0;
		}
		else if (m_targeted_view_bounds.y + m_targeted_view_bounds.height > m_max_y)
		{
			m_targeted_view_bounds.y = m_max_y - m_targeted_view_bounds.height;
		}

		if (m_progressive_scroll)
		{
			// si petit delta, essayer de ratrapper en 2 coups, sinon scroll
			// régulier
			
			int dx = m_targeted_view_bounds.x - m_view_bounds.x;
			int x_sign = dx < 0 ? -1 : 1;
			
			if (dx * x_sign > 4)
			{
				dx /= 2;
			}
			
			int max_delta_x = (int)(elapsed_time / 10) + 1;
			
			m_view_bounds.x += Math.min(x_sign * dx, max_delta_x) * x_sign;
			
			 m_view_bounds.y = m_targeted_view_bounds.y; // no progressive scroll for y
		}
		else
		{
			m_view_bounds.setBounds(m_targeted_view_bounds);
		}
		
		m_progressive_scroll = true;
	}
	
	private void update_view_bounds_xy(long elapsed_time)
	{
		if (m_scroll_lock_top_corner != null)
		{
			m_targeted_view_bounds.x = m_scroll_lock_top_corner.get_x();
			m_targeted_view_bounds.y = m_scroll_lock_top_corner.get_y();
		}
		else
		{
			int hx = m_hero.get_x()+m_hero.get_width()/2;
			int hy = m_hero.get_y()+m_hero.get_height()/2;

			int x_min = hx - m_targeted_view_bounds.x;
			int x_max = x_min;
			int y_min = hy - m_targeted_view_bounds.y;
			int y_max = y_min;

			x_min = Math.min(x_min, m_fourth_width);
			x_max = Math.max(x_max, m_3_fourth_width);
			y_min = Math.min(y_min, m_fourth_height);
			y_max = Math.max(y_max, m_3_fourth_height);


			if (x_max > m_3_fourth_width)
			{
				m_targeted_view_bounds.x = hx - m_3_fourth_width;
			}
			if (x_min < m_fourth_width)
			{
				m_targeted_view_bounds.x = hx - m_fourth_width;
			}
			if (y_max > m_3_fourth_height)
			{
				m_targeted_view_bounds.y = hy - m_3_fourth_height;
			}
			if (y_min < m_fourth_height)
			{
				m_targeted_view_bounds.y = hy - m_fourth_height;
			}
		}
		// limit on min and max bounds

		if (m_targeted_view_bounds.x < 0)
		{
			m_targeted_view_bounds.x = 0;
		}
		else if (m_targeted_view_bounds.x + m_targeted_view_bounds.width > m_max_x)
		{
			m_targeted_view_bounds.x = m_max_x - m_targeted_view_bounds.width;
		}

		if (m_targeted_view_bounds.y < 0)
		{
			m_targeted_view_bounds.y = 0;
		}
		else if (m_targeted_view_bounds.y + m_targeted_view_bounds.height > m_max_y)
		{
			m_targeted_view_bounds.y = m_max_y - m_targeted_view_bounds.height;
		}

		if (m_progressive_scroll)
		{
			// si petit delta, essayer de ratrapper en 2 coups, sinon scroll
			// régulier
			
			int dx = m_targeted_view_bounds.x - m_view_bounds.x;
			int dy = m_targeted_view_bounds.y - m_view_bounds.y;
			int x_sign = dx < 0 ? -1 : 1;
			int y_sign = dy < 0 ? -1 : 1;
			
			if (dx * x_sign > 4)
			{
				dx /= 2;
			}
			if (dy * y_sign > 4)
			{
				dy /= 2;
			}
			
			int max_delta_x = (int)(elapsed_time / 10) + 1;
			int max_delta_y = (int)(elapsed_time / 5) + 1;
			
			m_view_bounds.x += Math.min(x_sign * dx, max_delta_x) * x_sign;
			m_view_bounds.y += Math.min(y_sign * dy, max_delta_y) * y_sign;
		}
		else
		{
			m_view_bounds.setBounds(m_targeted_view_bounds);
		}
		
		m_progressive_scroll = true;
	}
	
	private void handle_pause()
	{
		if (m_game.is_key_pressed(KeyEvent.VK_P))
		{
			set_state(State.PAUSED);
		}
	}
	private void add_fixed_renderable(Renderable r)
	{
		m_fixed_renderable.add(r);
	}
	
	private void add_scrolled_renderable(Renderable r)
	{
		m_scrolled_renderable.add(r);
	}

	protected void level_end()
	{
		m_sfx_set.play(SfxSet.Sample.teleport,1);

		add_timer_event(new LevelEndEvent());

	}
	
	protected void game_end(boolean first_pass)
	{
		if (first_pass)
		{
			add_timer_event(new GameEndEvent());
		}
		else
		{
			set_state(State.GAME_END);
			fadeout();
		}

	}

	private void teleport_to(ControlObject teleport_location)
	{
		// stop fall sound if was playing
		m_sfx_set.stop(Sample.fall);
		
		m_sfx_set.play(SfxSet.Sample.teleport,0);

		m_timer_events.add(new TeleportTimerEvent(teleport_location));	
	}
	
	private class LevelEndEvent extends TimerEvent
	{
		LevelEndEvent()
		{
			super(1000);
			set_state(State.LEVEL_END);
			m_pre_end_message = true;
		}

		@Override
		protected void on_timeout() 
		{
			m_sfx_set.pause_all_loops();
			play_mp3_sound("level_end");

			if (m_demo_mode)
			{
				m_osd.init_demo_end();
			}
			else
			{
				m_osd.init_level_end(get_level_index());
			}
			m_pre_end_message = false;
		}
		
	}

	private class GameEndEvent extends TimerEvent
	{
		GameEndEvent()
		{
			super(5000);
			m_pre_end_message = true;
		}

		@Override
		protected void on_timeout() 
		{
			play_mp3_sound("level_end");

			m_osd.init_game_end();
			m_pre_end_message = false;
		}
		
	}
	
	private class FadeInEvent extends TimerEvent
	{
		FadeInEvent(long timeout)
		{
			super(timeout);
			
			set_state(State.FADEINOUT);
			
		}
		@Override
		public void update(long elapsed_time)
		{
			set_color_ratio(m_counter / (float)m_timeout);
			
			super.update(elapsed_time);
			
		}		
		@Override
		protected void on_timeout() {

			set_color_ratio(1);
			
			set_state(State.RUNNING);

		}
		
	}
	private class FadeOutEvent extends TimerEvent
	{
		FadeOutEvent(long timeout)
		{
			super(timeout);
			
			set_state(State.FADEINOUT);
			
		}
		@Override
		public void update(long elapsed_time)
		{
			super.update(elapsed_time);

			set_color_ratio((m_timeout - m_counter) / (float)m_timeout);			
		}

		@Override
		protected void on_timeout() 
		{
			set_color_ratio(0);	
		}
		
	}
	private class TeleportFadeInEvent extends FadeInEvent
	{
		public TeleportFadeInEvent() 
		{
			super(500);
		}
	}
	private class RestartFadeInEvent extends FadeInEvent
	{
		public RestartFadeInEvent() 
		{
			super(500);
		}
	}
	
	private class RestartFadeOutEvent extends FadeOutEvent
	{
		public RestartFadeOutEvent() 
		{
			super(300);
		}
		
		@Override
		protected void on_timeout()
		{
			super.on_timeout();
			set_hero_position(m_restart_location,false,true,new RestartFadeInEvent());
			
			play_start_sound();
			
			m_progressive_scroll = false;
			update_view_bounds(1);
		}
	}

	


	private class TeleportTimerEvent extends FadeOutEvent
	{
		private ControlObject m_location;
		
		TeleportTimerEvent(ControlObject teleport_location)
		{
			super(500);
			m_location = teleport_location;
		}

		@Override
		protected void on_timeout() 
		{
			super.on_timeout();
			
			if (m_location != null)
			{
				if (m_location.get_type() == ControlObject.Type.Restart)
				{
					m_restart_location = m_location;
				}

				set_hero_position(m_location,true,false,new TeleportFadeInEvent());				
			}
			else
			{
				warn("null teleport location");
			}
			
		}
		
		
	}
	

	
	private class TimerEvents
	{
		private LinkedList<TimerEvent> m_list = new LinkedList<TimerEvent>();
		private LinkedList<TimerEvent> m_pending_list = new LinkedList<TimerEvent>();
		
		public void clear()
		{
			m_pending_list.clear();
			m_list.clear();
		}
		public void add(TimerEvent t)
		{
			m_pending_list.add(t);
		}
		
		public void update(long elapsed_time)
		{
			ListIterator<TimerEvent> it = m_list.listIterator();
			
			while (it.hasNext())
			{
				TimerEvent t = it.next();
				
				t.update(elapsed_time);
				
				if (t.timeout())
				{
					it.remove();
				}
			}

			m_list.addAll(m_pending_list);
			m_pending_list.clear();
		}
	}
	
	private class AppearingItems implements Renderable
	{
		public void render(Graphics2D g)
		{
			for (AnimatedFrames af : m_bonus_appearing_list)
			{
				af.render(g);
			}
		}
		public void update(long elapsed_time)
		{
			ListIterator<AnimatedFrames> it = m_bonus_appearing_list.listIterator();
			while (it.hasNext())
			{
				AnimatedFrames af = it.next();
				af.update(elapsed_time);
				if (af.is_done())
				{
					it.remove();
					GfxObject go = (GfxObject)af.get_user_data();
					if (go != null)
					{
						go.set_visible(true);
					}
				}
			}
		}	
	}

	private class DisappearingItems implements Renderable
	{
		public void render(Graphics2D g)
		{
			for (AnimatedFrames af : m_bonus_taken_list)
			{
				af.render(g);
			}
		}
		public void update(long elapsed_time)
		{
			ListIterator<AnimatedFrames> it = m_bonus_taken_list.listIterator();
			while (it.hasNext())
			{
				AnimatedFrames af = it.next();
				af.update(elapsed_time);
				if (af.is_done())
				{
					it.remove();
				}
			}
		}	
	}
	
	private static final int BUTTON_FRAME_RATE = 80;
	private class ActivableItems implements Renderable
	{
		private int m_timer = 0;
		
		public void render(Graphics2D g) 
		{
			// nothing done
			
		}

		public void update(long elapsed_time) 
		{
			
			for (Map.Entry<GfxObject,LeverActivationState> es : m_level_data.lever_states.entrySet())
			{
				int current_frame = 1;
				GfxObject a = es.getKey();
				
				switch (es.getValue())
				{
				case ACTIVATED:
				case FIRST_ACTIVATED:
					current_frame = 2;
					break;
				}
				
				a.set_current_frame(current_frame);

			}			
			for (Map.Entry<GfxObject,SectoredLevelData.ButtonState> es : m_level_data.button_states.entrySet())
			{
				GfxObject a = es.getKey();
				SectoredLevelData.ButtonState bs = es.getValue();
				ButtonActivationState bas = bs.press_state;
						
				// 3 4 2 4 3 1
				int current_frame = 1;
				switch (bas)
				{
				case PRESSED_1:
				case PRESSED_5:
					current_frame = 3;
					break;
				case PRESSED_2:
				case PRESSED_4:
					current_frame = 4;
					break;
				case PRESSED_3:
					current_frame = 2;
					break;
				}

				a.set_current_frame(current_frame);
			
				if (bas != ButtonActivationState.NOT_PRESSED)
				{
					
					m_timer += elapsed_time;
					if (m_timer > BUTTON_FRAME_RATE)
					{
						m_timer -= BUTTON_FRAME_RATE;
						
						// state change
						
						if (bas == ButtonActivationState.PRESSED_5)
						{
							bs.press_state = ButtonActivationState.NOT_PRESSED;
							m_timer = 0;
						}
						else
						{
							bs.press_state = ButtonActivationState.values()[bas.ordinal()+1];
						}
						es.setValue(bs);
					
					}
				}

			}			
		}
		
	}
	
	private int m_music_loop_timer = 0;
	
	private void handle_music_loop(long elapsed_time)
	{
		if (m_state != State.GAME_OVER)
		{
			m_music_loop_timer += elapsed_time;
			
			if (m_music_loop_timer > 200)
			{
				m_music_loop_timer = 0;
				
				if (!Mp3Play.is_playing())
				{
					Mp3Play.replay();
				}
			}
		}
	}

	private void unload_locale()
	{
		if (m_locale_file != null)
		{
			Localizer.unload(m_locale_file);
		}
	}
	@Override
	protected void p_update() 
	{
		long elapsed_time = get_elapsed_time();
				
		switch (m_state)
		{
		case INITIAL:
			if (!is_fadein())
			{
				m_state = State.LOADING;
			}
			break;
		case LOADING:
			try 
			{	
				load_level();
				set_state(State.LOADED);

			}
			catch (IOException e1) 
			{
				e1.printStackTrace();
				set_state(State.PAUSED);
			}
			break;
		case LOADED:
		{
			fadeout();
			if (is_fadeout_done())
			{
				set_state(State.RUNNING);
				fadein();
			}
			break;
		}
		case SHOPKEEPER:
			m_shopkeeper.update(elapsed_time);
			m_disappearing_items.update(elapsed_time);
			m_status_bar.update(elapsed_time);
			
			m_hero.get_weapon_set().update(elapsed_time);
			
			if (m_shopkeeper.hero_met())
			{
				fadeout();
				if (is_fadeout_done())
				{	
					show_shop();
				}
			}
			break;
		case RUNNING:
			if (m_old_state == State.LOADED)
			{
				play_start_sound();
				set_state(State.RUNNING);
			}
			
			handle_music_loop(elapsed_time);
			handle_pause();
			
			if (m_summon_shopkeeper)
			{
				
				if (m_hero.is_standing_on_ground() && !m_hero.is_switching_sides())
				{
					set_state(State.SHOPKEEPER);
					m_summon_shopkeeper = false;
					m_hero.at_rest();
					m_shopkeeper.init(m_view_bounds);
				}
			}
			
			update_view_bounds(elapsed_time);
			
			if ((m_boss != null) && 
					(!m_on_boss_death_called) && 
					(m_boss.get_life_state() == LifeState.EXPLODING))
			{
				stop_music();
				
				m_hero.enable_familiar(m_saved_familiar);
				
				on_boss_death();
				
				m_current_music_file = m_level_music_file;
				
				play_music();
				
				// this is not very smart, but it works
				
				ControlObject boss_co = m_level_data.get_control_object("boss");
				
				boss_co.set_coordinates(m_boss,false);
				boss_co.spawn_boss_bonuses(m_level_data);
				
				m_on_boss_death_called = true;

				// 10000 * level index
				
				int boss_score = get_level_index() * 10000;
				
				m_hero.add_score(boss_score, false);
			}
			

			{
	
				for (Renderable r : m_scrolled_renderable)
				{
					r.update(elapsed_time);
				}
				for (Renderable r : m_fixed_renderable)
				{
					r.update(elapsed_time);
				}
	
				handle_bouncing_sound();
				
				m_timer_events.update(elapsed_time);
	
				// do NOT change order of the handle functions (because of some hero boundaries updated
				// only in the first one)
				
				m_hero.get_bounds(m_work_rectangle);

				// restart points
				
				handle_restarts();
				
				// triggers
				
				handle_triggers();
				
				// face doors
								
				handle_face_doors();
				
				// hero
				
				update_hero();
	
			}
			break;
		case PAUSED:
			if (m_game.is_key_pressed(KeyEvent.VK_P))
			{
				set_state(m_old_state);
			}
			break;
		case FADEINOUT:
			m_disappearing_items.update(elapsed_time);
			handle_pause();
			m_timer_events.update(elapsed_time);
			break;
		case PRESS_FIRE:
			m_status_bar.update(elapsed_time);
			
			if (is_a_button_pressed())
			{
				game_over();
			}
			break;
		case GAME_OVER:
			if (is_fadeout_done())
			{
				unload_locale();
				
				m_level_data = null;
				m_level_set = null;
				
				
				goto_next_screen();
				
			}
			break;
		case WORLD_END:
		case LEVEL_END:
		case GAME_END:
				
			if (is_fadeout_done())
			{
				
				if (m_demo_mode)
				{
					if (m_state != State.WORLD_END)
					{
						m_display_message = Localizer.value("GAME  OVER",true);
						m_sfx_set.dispose();
						m_hero.set_life_state(LifeState.STAND_BY);
						set_state(State.PRESS_FIRE);
						fadein();
					}
				}
				else
				{
					boolean completed = GameOptions.instance().unlock_next_level_in_current_level_set();

					if (completed)
					{

						m_sfx_set.dispose();

						unload_locale();

						set_next(new GodsEndScreen(m_level_data.get_common_palette(),m_hero));

					}
					else
					{
						try 
						{
							set_next(create(m_hero));
						} 
						catch (Exception e) 
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				System.gc();

			}
			else if (m_state != State.GAME_END)
			{
				m_timer_events.update(elapsed_time);

				m_status_bar.update(elapsed_time);

				m_osd.update(elapsed_time);

				if (!m_pre_end_message && !m_osd.is_world_end())
				{
					if (m_state == State.LEVEL_END)
					{
						m_pre_end_message = true;
						next_level();
					}
					else
					{
						// kill all alive monsters

						m_monster_layer.cleanup_remaining_monsters();

						set_state(State.RUNNING);
						m_hero.world_start();
						start_clock();
						on_world_restart(m_world_count+1);

						play_music();
					}
				}
			}
			break;
		}
	}
	
	private void goto_next_screen()
	{
		GameState next_screen = null;
		
		// uncomment hiscore input screen test 
		//GameState next_screen = new GodsHiScoreEntryScreen(m_hero.get_score());
		

		if (next_screen == null)
		{

			if (!m_game_given_up)
			{
				RecordScore hiscores = new RecordScore();

				hiscores.load();

				if (hiscores.is_high_score(m_hero.get_score()))
				{
					// high score reached: next screen is high score entry

					next_screen = new GodsHiScoreEntryScreen(m_hero.get_score());
				}
			}
		}

		if (next_screen == null)
		{
			// simply go back to menu
			
			next_screen = new GodsMainMenuScreen();
		}
		
		set_next(next_screen);
		
	}
	@Override
	protected void p_init() 
	{
		m_status_bar = StatusBar.instance(getWidth(),getHeight());

		

		switch(m_state)
		{
		case SHOPKEEPER:
		{
			set_state(State.RUNNING);
			// avoid the fade-in then go-black effect when returning from
			// the shop (I'm sorry the fade-in/fade-out management is a bit
			// clumsy as there is the main fadein/out management (when exiting
			// a game "State" (game over, screen change, ...) and there's an in-game
			// fade in/out management (teleport, death, ...) without GameState change
			
			set_color_ratio(1);
			
			// also, restart playing level music
			
			play_music();
			
		}
		break;
		}
		
		// we handle fadein/fadeout within this GameState instance
		
	}
	
	private void handle_restarts()
	{
		// check if restart close to hero

		Sector<ControlObject>.Distance closest_restart = m_level_data.restart_sector_set.closest(m_hero);
		ControlObject closest = closest_restart.item;
		
		if (closest != null)
		{
			closest.get_bounds(m_work_rectangle_2);

			if (m_work_rectangle.intersects(m_work_rectangle_2))
			{
				m_restart_location = closest;

				// for speed bonus and other stuff
				if ((!m_player_start_entered) && 
						(closest.get_name().equals("player_start") || closest.get_name().equals("temp_start")))
				{
					m_player_start_entered = true;
					// same init for world 1 (remove items, reset bonus ...)

					m_hero.world_start();

					// speed bonus is given mostly at world 1, so we need the clock

					start_clock();
				}
			}
		}
	}
	
	private void handle_triggers()
	{
		// check if trigger close to hero

		Sector<ControlObject>.Distance closest_trigger = m_level_data.trigger_sector_set.closest(m_hero);

		ControlObject closest = closest_trigger.item;
		
		if (closest != null)
		{
			closest.get_bounds(m_work_rectangle_2);


			if (m_work_rectangle.intersects(m_work_rectangle_2))
			{	
				// lookup trigger in association set

				ObjectAssociation oa = m_level_data.get_association_set().get(closest.get_name());

				if (oa != null)
				{
					switch (oa.get_type())
					{
					case Trigger_Monster:
					{
						TriggerMonsterAssociation tma = (TriggerMonsterAssociation)oa;

						// create the hostile according to the parameters

						for (int i = 0; i < tma.get_nb_monsters(); i++)
						{
							create_hostile_wave(tma.get_monster(i));
						}
						closest.set_visible(false);
					}
					break;
					case Trigger_Item_Display_2:
					{
						TriggerItemDisplay2Association tid = (TriggerItemDisplay2Association)oa;
						display(tid.get_item_to_display().get_name());
						closest.set_visible(false);
					}
					break;
					case Trigger_Item_Display_Special:
					{
						// same as above, but with "special bonus" message
						TriggerItemDisplaySpecialAssociation tid = (TriggerItemDisplaySpecialAssociation)oa;
						display_special_bonus(tid.get_item_to_display().get_name());
						closest.set_visible(false);
					}
					break;
					case Trigger_Close_Door:
					{
						TriggerCloseDoorAssociation tid = (TriggerCloseDoorAssociation)oa;
						// only disable trigger if door is really open
						if (close_door(tid.get_door_to_close().get_name()))
						{
							closest.set_visible(false);
						}
					}
					break;
					case Trigger_Item_Display:
						TriggerItemDisplayAssociation tid = (TriggerItemDisplayAssociation)oa;

						if (m_hero.steal_named_object(tid.get_must_own().get_name()))
						{
							display(tid.get_item_to_display().get_name());
							closest.set_visible(false);
						}
						break;
					}
					
					//m_trigger_sector_set.remove(closest);
				}
				else 
				{

					// trigger without an association

					switch (closest.get_type())
					{
					case World_End_Trigger:
						m_restart_location = closest;
						m_world_count++;
						set_state(State.WORLD_END);
						m_osd.init_world_end();	
						
						// stop invincibility
						m_sfx_set.pause_all_loops();
						play_mp3_sound("world_end");
						closest.set_visible(false);
						break;
					case Reset_Clock_Trigger:
						start_clock();
						break;
					case Message_Trigger:
						print(closest.get_name());
						closest.set_visible(false);
						break;
					default:
						if (on_trigger_activated(closest.get_name()))
						{
							m_level_data.trigger_sector_set.remove(closest);
						}
					break;
					}

				}
			}
		}
	}
	private void handle_face_doors()
	{
		if (m_hero.try_to_enter_door())
		{
			Sector<ControlObject>.Distance closest_face_door = m_level_data.face_door_sector_set.closest(m_hero);

			ControlObject door = closest_face_door.item;
			
			if (door != null)
			{
				int max_distance = door.get_width() * door.get_width() / 4;
				
				if (closest_face_door.square_distance < max_distance)

				{
					// lookup door state

					String door_name = door.get_name();

					Door fd = m_level_data.doors_hash_set.get(door_name);

					if (fd.is_open())
					{
						// get association for this door

						ObjectAssociation oa = m_level_data.get_association_set().get(door_name);
						if (oa != null)
						{
							FaceDoorLocationAssociation fdla = (FaceDoorLocationAssociation)oa;
							ControlObject loc = fdla.get_location();

							if (loc.is_visible() && door_name.equals(fdla.get_location().get_name()))
							{
								loc.set_visible(false);

								// special case: end of level: door connected to itself

								level_end();
							}
							else
							{
								AssociationProperty ap = fdla.get_location_property();

								if ((ap != null) && (!ap.get_name().equals(ParameterParser.UNDEFINED_STRING)))
								{
									print(ap.get_name());
									ap.set_name(ParameterParser.UNDEFINED_STRING);
								}
								teleport_to(fdla.get_location());

								on_room_entered(fdla.get_location().get_name());
							}
						}
					}

				}
			}
		}
	}
	private void load_level() throws IOException
	{
		// garbage collect first
		System.gc();
		
		// first, load the locale file
		m_locale_file = Localizer.load(DirectoryBase.get_levels_path() + m_level_set.get_name(), "_"+m_level_set.get_level_index());

		// then load the level
		
		String level_file = m_level_set.get_name() + File.separator + m_level_set.get_level_index();
		m_level_data.load(level_file, m_sfx_set.get(SfxSet.Loop.platform_move),m_view_bounds);

		m_level_music_file = m_level_data.get_level_music();
		m_current_music_file = m_level_music_file;
		
		boolean is_first_level = m_hero == null;

		// create hero, note that view bounds reference is passed to hero
		
		if (is_first_level)
		{
			m_hero = new Hero(m_game,get_joystick(),m_level_set.get_level_index());
		}
		
		// reset bounds so no edge effect between levels
		
		m_view_bounds.x = 0;
		m_view_bounds.y = 0;
		
		m_shopkeeper = new Shopkeeper(m_level_data.get_common_palette(),m_hero);

		m_hostile_weapon_set = new HostileWeaponSet();

		m_monster_layer = new MonsterLayer(m_level_data, m_hero,
				m_sfx_set,m_hostile_weapon_set,m_level_set.get_level_index());

		// tries to match original scrolling

		m_control_layer = m_level_data.get_control_layer();
		m_fourth_width = m_level_data.get_grid().get_tile_width() * 3 + 8;
		m_3_fourth_width = getWidth() - m_fourth_width;
		m_fourth_height = m_level_data.get_grid().get_tile_height() * 3 + 8;
		m_3_fourth_height = getHeight() - (m_level_data.get_grid().get_tile_height() * 4 + 8);

		m_max_x = m_level_data.get_width();
		m_max_y = m_level_data.get_height();

		m_view_bounds.setBounds(Math.max(m_hero.get_x() - getWidth()/2, 0),
				Math.max(m_hero.get_y() - getHeight()/2 - 24, 0),getWidth(),getHeight() - 24);
		
		m_bonus_taken_smoke = m_level_data.get_common_palette().lookup_frame_set("bonus_taken_smoke");

		// player start position
		ControlObject player_start = m_control_layer.get("temp_start");
		boolean temp_start = player_start != null && player_start.is_visible();
		
		if (!temp_start)
		{
			player_start = m_control_layer.get("player_start");
		}
		
		
		m_hero.level_init(m_level_data,m_monster_layer,m_status_bar,m_sfx_set,m_view_bounds,get_level_index());

		
		if (temp_start)
		{
			m_hero.add_money(80000);
			for (int i = 0; i < 3; i++)
			{
				//m_hero.give_weapon("knife");
				//m_hero.give_weapon("throwing_star");
				//m_hero.give_weapon("throwing_star");
				m_hero.give_weapon("mace");
				//m_hero.give_weapon("mace");
				m_hero.give_weapon("magic_axe");
				//m_hero.give_weapon("fire_ball");
				m_hero.give_weapon("fire_ball");
				//m_hero.give_weapon("spear");
				
				/*m_hero.give_weapon("axe");
				m_hero.give_weapon("axe");
				m_hero.give_weapon("axe");
				m_hero.give_weapon("fire_ball");
				m_hero.give_weapon("fire_ball");
				m_hero.give_weapon("fire_ball");
				m_hero.give_weapon("fire_ball");
				m_hero.give_weapon("fire_ball");*/
				//
				//m_hero.give_weapon("hunter");
				//m_hero.give_weapon("hunter");
				//m_hero.give_weapon("time_bomb");
				//m_hero.give_weapon("axe");
			}
		}
				
		GfxFrame lives_icon = m_level_data.get_level_palette().lookup_frame_set("extra_life").get_first_frame();
		GfxFrame gold_bag_icon = m_level_data.get_level_palette().lookup_frame_set("gold_bag").get_first_frame();

		// create hostiles declared as created at once

		create_hostiles();

		// locate boss
		
		Collection<Hostile> items = m_monster_layer.get_items();
		
		for (Hostile h : items)
		{
			if (h.get_name().equals("boss"))
			{
				m_boss = h;
				m_boss.set_life_state(LifeState.STAND_BY);

				// link all objects around boss, starting with boss

				ControlObject co = h.get_location();
				
				m_level_data.link_objects_around_boss(co, co.get_height());
				break;
			}
		}

		m_osd = new OnScreenPlayerStatus(m_level_data.get_common_palette(),lives_icon,gold_bag_icon,
				m_hero,m_boss,m_sfx_set,getWidth());

		// 	add rendering layers, with increasing priority
		
		add_scrolled_renderable(m_level_data);
		add_scrolled_renderable(m_lever_activated);
		add_scrolled_renderable(m_monster_layer.get_background_items());
		add_scrolled_renderable(m_level_data.get_object_layer());
		add_scrolled_renderable(m_disappearing_items);
		add_scrolled_renderable(m_appearing_items);
		add_scrolled_renderable(m_hero);
		add_scrolled_renderable(m_hostile_weapon_set);
		add_scrolled_renderable(m_monster_layer.get_foreground_items());
		add_scrolled_renderable(m_monster_layer);
		// hero weapon set not updated/rendered
		// at the same time as the hero because it has drawing priority
		// over monsters
		add_scrolled_renderable(m_hero.get_weapon_set());

		add_fixed_renderable(m_status_bar);
		add_fixed_renderable(m_osd);

		if (!temp_start)
		{
			if (is_first_level)
			{
				if (m_sold_items != null)
				{
					set_state(State.SHOPKEEPER);
					m_hero.add_money(80000);
					show_shop();
					m_sold_items = null;
				}
			}
			
		}
		
		
		try {
			String copper_class_name = m_level_data.get_copperbar_class();
			CopperBar cb = (CopperBar)Class.forName(copper_class_name).newInstance();
			
			int step= GameOptions.instance().get_gfx_type()==GameOptions.GfxType.ORIGINAL ? 4 : 1;
			create_copper_effect(cb.create(step));
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
		
		m_targeted_view_bounds.setBounds(m_view_bounds);

		if (player_start != null)
		{
			set_hero_position(player_start,true,false,new RestartFadeInEvent());
		}
						
		on_level_loaded();
	}

	private void play_start_sound()
	{
		play_mp3_sound("restart");
		
		add_timer_event(new MusicTimerEvent());
	}
	
	private void play_mp3_sound(String name)
	{
		if (GameOptions.instance().get_sfx_state())
		{
			// play once, no repeat like music
			Mp3Play.play(DirectoryBase.get_sound_path()+name+".mp3",false);
		}
	}
	

	private boolean open_door(Door d, boolean play_sfx)
	{
		boolean rval = (!d.is_open());
		
		if (rval && play_sfx)
		{
			m_sfx_set.play(Sample.open_close,2); // open sound
		}
		d.set_open(true);
		if (rval)
		{
			// was closed
			on_door_change_state(d.get_name(),true);
		}
		
		return rval;
	}
	private boolean close_door(Door d, boolean play_sfx)
	{		
		boolean rval = (d.is_open());
		
		if (rval && play_sfx)
		{
			m_sfx_set.play(Sample.open_close,1); // close sound
			
			if (d.get_type() == ControlObject.Type.Trap)
			{
				// a trap door has just closed. Check view bounds and if trap door
				// is below, display the message

				if (d.get_y() >= m_view_bounds.height + m_view_bounds.y)
				{
					print("a trap door closes beneath you");
				}
			}
		}
		d.set_open(false);

		if (rval)
		{
			// was open
			on_door_change_state(d.get_name(),false);
		}
		
		
		return rval;
	}

	private void show_shop()
	{
		Mp3Play.stop();
		
		int y_res = m_level_data.get_object_layer().get_y_resolution();

		// stop fall sound if was playing
		m_sfx_set.stop(Sample.fall);

		set_next(new Shop(m_sold_items,m_hero,y_res,m_level_data.get_common_palette(),
				m_level_data.get_level_palette(),m_status_bar,this));
		
		
	}
	private int get_level_index()
	{
		return GameOptions.instance().get_current_level_set().get_level_index();
	}

	private void next_level()
	{
		m_sfx_set.dispose();
		m_hero.set_shield(false);  // each next level you have to buy a shield
		stop_music();
		set_state(State.LEVEL_END);
		m_timer_events.clear();
		fadeout();
	}
	private void warn(String m)
	{
		System.out.println("Warning: "+m);
	}
	private void print_bonus_message(GfxObject go)
	{
		GfxFrameSet.Properties p = go.get_source_set().get_properties();
		
		if (p.first_appearance)
		{
			print(p.toString());
			p.first_appearance = false;
		}
	}
	
	private void handle_bouncing_sound()
	{
		GfxObjectLayer gol = m_level_data.get_object_layer();
		
		for (GfxObject go : gol.get_items())
		{
			int bounce_to_play = go.get_bounce_to_play();
			if (bounce_to_play > 0)
			{
				m_sfx_set.play(Sample.bounce,bounce_to_play-1);
			}
		}
	}
	private void create_copper_effect(int [] rgb_array)
	{
		if (rgb_array != null)
		{
			int th = m_level_data.get_grid().get_tile_height();

			BufferedImage bi = new BufferedImage(m_level_data.get_grid().get_tile_width(),
					rgb_array.length + th * 2,
					BufferedImage.TYPE_INT_RGB);

			Graphics g = bi.getGraphics();

			g.setColor(new Color(rgb_array[0]));
			for (int i = 0; i < th; i++)
			{
				g.drawLine(0, i, bi.getWidth(), i);
			}

			g.setColor(new Color(rgb_array[rgb_array.length - 1]));
			for (int i = 0; i < th; i++)
			{
				g.drawLine(0, i + th + rgb_array.length, bi.getWidth(), i + th + rgb_array.length);
			}

			for (int i = 0; i < rgb_array.length; i++)
			{
				g.setColor(new Color(rgb_array[i]));

				g.drawLine(0, i + th, bi.getWidth(), i + th);
			}	

			m_copper_effect = bi;
		}
		
	}

	private void set_state(State s)
	{
		m_old_state = m_state;
		m_state = s;
	}
	
	public static GodsLevel create(Hero h) throws Exception
	{
		 GameOptions go = GameOptions.instance();
		 
		 LevelSet ld = go.get_current_level_set();
		 
		 String level_name = ld.get_name() + File.separator + ld.get_level_index()+DirectoryBase.LEVEL_EXTENSION;
		 
		 // load start of file to get class name
		 String level_class = LevelData.get_level_class(level_name);
		 
		 GodsLevel gl = (GodsLevel)Class.forName(level_class).newInstance();
		 
		 gl.m_level_set = ld;
		 gl.m_hero = h;
		 
		 return gl;
	}

	public void display_boss()
	{
		m_saved_familiar = m_hero.disable_familiar();
		
		// remove all other monsters when entering
		m_monster_layer.cleanup_remaining_monsters();
		
		m_boss.set_life_state(LifeState.ALIVE);
		m_monster_layer.activate_hostile("boss");
		
		
		m_current_music_file = m_level_data.get_boss_music();
		
		play_music();
	}

	public Rectangle get_view_bounds()
	{
		return m_view_bounds;
	}

	public Hero get_hero()
	{
		return m_hero;
	}

	/*public void insert(GfxObject go)
	{
	
	}*/
	
	public LevelData get_data()
	{
		return m_level_data;
	}

	public SfxSet get_sfx_set()
	{
		return m_sfx_set;
	}

	public GameEngine get_engine()
	{
		return m_game;
	}

	public HostileWeaponSet get_hostile_weapon_set()
	{
		return m_hostile_weapon_set;
	}
}
