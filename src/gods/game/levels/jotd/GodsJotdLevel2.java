package gods.game.levels.jotd;

import java.io.IOException;

import gods.base.ControlObject;
import gods.base.GfxFrame;
import gods.base.GfxFrameSet;
import gods.base.GfxObject;
import gods.game.GodsLevel;
import gods.game.SfxSet.Sample;

public class GodsJotdLevel2 extends GodsLevel 
{
	private boolean m_w1_first_button_1_pressed = false;
	private boolean m_w2_marker_2 = false;
	private boolean m_w2_marker_1 = false;
	
	private int m_w1_nb_gems = 0;
	private int m_solved_puzzles = 0;
	private boolean m_w3_shortcut_moved = false;
	private boolean m_w2_disable_teleport = false;
	private boolean m_w3_catch_fall = true;
	private boolean m_last_shopkeeper = false;
	
	private boolean m_w3_is_crypt_door_closed = true;

	public static final String [] SHOP_CONTENTS_W2_END = { "chicken", "apple", "weapon_arc_wide",
		"weapon_arc_intense", "weapon_arc_standard", "bread", "freeze",
		"half_energy", "throwing_star", "invincibility", 
		"full_energy", "starburst", "fire_ball", "magic_axe",
		"spear", "increase_wp_medium", "shield", "extra_life" };
	
	public static final String [] SHOP_CONTENTS_W3_END = { "chicken", "apple", "weapon_arc_wide",
		"weapon_arc_intense", "weapon_arc_standard", "bread", "freeze",
		"half_energy", "throwing_star", "invincibility", 
		"full_energy", "starburst", "fire_ball",
		"spear", "increase_wp_medium", "shield", "extra_life", 
		"familiar", "familiar_power_claws", "familiar_power_wings", "increase_wp_full" };

	public GodsJotdLevel2() throws IOException 
	{
		super(GodsJotdLevel1.SHOP_CONTENTS_W3_END);
	}

	@Override
	protected boolean on_bonus_taken(GfxObject bonus) {
		String name = bonus.get_name();
		boolean rval = true;
		
		// world 2
		if (name.equals("w3_start_shop"))
		{
			GfxFrameSet gfs = m_level_data.get_level_palette().lookup_frame_set("increase_wp_medium");
			if (gfs != null)
			{
				// at start value is 6000, change it
				gfs.get_properties().value = 17500;
			}
			gfs = m_level_data.get_level_palette().lookup_frame_set("increase_wp_full");
			if (gfs != null)
			{
				// at start value for "increase weapon power - full" is 10000
				// change it
				gfs.get_properties().value = 32500;
			}
			summon_shopkeeper(SHOP_CONTENTS_W2_END,false);
		}
		else if (name.equals("w3_end_shop"))
		{
			summon_shopkeeper(SHOP_CONTENTS_W3_END,true);
			m_last_shopkeeper = true;
		}
		return rval;	}

	@Override
	protected void on_boss_death() 
	{
		display("w3_world_key");
	}

	@Override
	protected void on_button_pressed(String button_name) 
	{
		if (button_name.equals("w1_secret_1"))
		{
			if (!m_w1_first_button_1_pressed)
			{
				m_w1_first_button_1_pressed = true;
				move_block("w1_platform_start", false);
				// a little harder
				create_hostile("w1_spikes_1");
				
				enable_trigger("w1_special_bonus_shortcut");
			}
		}
		else if (button_name.equals("w1_secret_2"))
		{
			enable_trigger("w1_open_block_2");
		}
		else if (button_name.equals("w2_secret_1"))
		{
			enable_trigger("w2_move_shortcut_block");
		}
		else if (button_name.equals("w3_secret_1"))
		{		
			// enable pit shortcut
			
			enable_trigger("w3_kill_spikes_2");
			
			// level will be a little harder in the end
			
			enable_trigger("w3_sht_5");
			create_hostile("w3_spikes_7");
		}
		else if (button_name.equals("w3_secret_2"))
		{
			open_door("w3_cross_trap");
		}
		else if (button_name.equals("w3_secret_3"))
		{
			// brings only problems ...
			create_hostile("w3_candle_thief");
		}
	}

	@Override
	protected void on_door_change_state(String door_name, boolean is_open) 
	{
		if (door_name.equals("w3_crypt_side_door"))
		{
			// disable crypt special bonus if open
			m_w3_is_crypt_door_closed = !is_open;
		}
		if (is_open && door_name.equals("w3_study_door"))
		{
			add_timer_event(new MoveStudyFloorEvent());
		}
	}

	@Override
	protected void on_level_loaded() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void on_lever_activated(GfxObject lever,
			LeverActivationState state) {
		
		String lever_name = lever.get_name();
		
		if (lever_name.equals("w1_open_door_3"))
		{
			if (m_hero.owns_named_object("w1_water_gem") || m_hero.owns_named_object("w1_fire_gem"))
			{
				display("w1_door_key_3");
			}
		}
		else if (lever_name.equals("w2_open_pit_door"))
		{
			// prevent hero from opening door (he would be blocked afterwards)
			
			if (m_hero.owns_named_object("w2_world_key"))
			{
				open_door("w2_pit_door");
			}
		}
		else if (lever_name.equals("w3_open_shortcut"))
		{
			if ((!m_w3_shortcut_moved) && (m_hero.steal_named_object("w3_candle")))
			{
				m_w3_shortcut_moved = true;
				print("short cut");
				move_block("w3_shortcut_start", false);
				display("w3_shortcut_bonus_1");
				display("w3_shortcut_bonus_2",false);
				create_hostile("w3_spikes_4");
			}
		}
		else if (lever_name.equals("w3_open_door_11"))
		{
			if (is_lever_activated(lever_name))
			{
				if (m_hero.steal_named_object("w3_candle"))
				{
					display("w3_door_key_11");
				}
			}
			else
			{
				if (m_hero.steal_named_object("w3_door_key_11"))
				{
					open_door("w3_door_11");
				}
			}
		}
		else if (lever_name.equals("w3_kill_spike_6"))
		{
			destroy("w3_spike_6");
			
			// no more secure fall
			
			m_w3_catch_fall = false;
			
			// no more bonus
		}
		else if (lever_name.equals("w3_world_door_lever"))
		{
			if (m_hero.owns_named_object("w3_world_key"))
			{
				if (m_last_shopkeeper)
				{
					open_door("w3_world_end");
				}
				else
				{
					display("w3_end_shop");
				}
			}
		}
	}

	@Override
	protected void on_object_drop(GfxObject bonus) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void on_object_picked_up(GfxObject object) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void on_room_entered(String room_door_name) {
		if (room_door_name.equals("w1_treasure_key_lair"))
		{
			if (m_hero.steal_named_object("w1_door_key_4"))
			{
				print("keys swooped, don't worry");
			}
		}
		else if (room_door_name.equals("w2_part_3_door"))
		{
			// check if all chalices are on the "table"
			
			int nb_chalices = 0;
			if (zone_contains("w2_display_key_zone","w2_iron_chalice")) nb_chalices++;
			if (zone_contains("w2_display_key_zone","w2_steel_chalice")) nb_chalices++;
			if (zone_contains("w2_display_key_zone","w2_gold_chalice")) nb_chalices++;

			if (nb_chalices == 3)
			{
				display("w2_treasure_key");
			}
			
		}
	}
	
	private class MoveStudyFloorEvent extends TimerEvent
	{
		public MoveStudyFloorEvent() 
		{
			super(2000); // 80 seconds to solve puzzle from w2
		}
		
		@Override
		protected void on_timeout() 
		{
			move_block("w3_chapel_platform_start", false);
		}
		
	}
	private class TeleportDisplayTimerEvent extends TimerEvent
	{
		public TeleportDisplayTimerEvent() 
		{
			super(80000); // 80 seconds to solve puzzle from w2
		}
		
		@Override
		protected void on_timeout() 
		{
			if (!m_w2_disable_teleport)
			{
				display("w2_help_teleport");
				
				// open trap in case hero is below and does not know
				// about the hard ladder jump
				
				open_door("w2_trap_1");
				
				m_w2_disable_teleport = true;
			}
		}
		
	}
	@Override
	protected boolean on_trigger_activated(String trigger_name) 
	{
		boolean rval = true;
		
		debug(trigger_name);
		
		if (trigger_name.equals("w1_close_door_2"))
		{
			close_door("w1_side_door_2");
			display_speed_bonus("w1_speed_bonus", 70);
		}
		else if (trigger_name.equals("w1_lives_bonus"))
		{
			display_lives_bonus(trigger_name, 3);
		}
		else if (trigger_name.equals("w1_special_fall"))
		{
			if (display(trigger_name))
			{
				print("special bonus");
			}
		}
		else if (trigger_name.equals("w1_open_block_2"))
		{
			// check that hero owns the 2 gems already
			
			if (m_hero.owns_named_object("w1_water_gem") && m_hero.owns_named_object("w1_fire_gem"))
			{
				move_block("w1_block_2_start", false);
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w1_gem_zone"))
		{
			check_gem("w1_ice_gem");
			check_gem("w1_fire_gem");
			check_gem("w1_water_gem");
			
			rval = false;
			
		}
		else if (trigger_name.equals("w1_enable_back_hostiles"))
		{
			enable_trigger("w1_gmt_15");
			enable_trigger("w1_gmt_16");
		}
		else if (trigger_name.equals("w1_special_bonus_shortcut"))
		{
			display(trigger_name+"_1");
			display(trigger_name+"_2");
			display("w1_shortcut_teleport",false);
			print("short cut");
		}
		else if (trigger_name.equals("w1_help_lives_bonus"))
		{
			rval = display_help_lives_bonus(trigger_name);
		}
		else if (trigger_name.equals("w1_help_health_bonus"))
		{
			rval = display_help_health_bonus(trigger_name);
		}
		else if (trigger_name.equals("w2_start_teleport_timer"))
		{
			add_timer_event(new TeleportDisplayTimerEvent());
		}
		else if (trigger_name.equals("w2_disable_teleport"))
		{
			// no way back
			close_door("w2_trap_1");
			create_hostile("w2_spikes_2a");
			create_hostile("w2_spikes_2b");
			
			if (!m_w2_disable_teleport)
			{
				m_w2_disable_teleport = true;
				display_special_bonus("w2_giant_jump_bonus_1");
				
				// extra bonus if hero has key
				if (m_hero.steal_named_object("w2_close_trap_door_1"))
				{
					display("w2_giant_jump_bonus_2",false);
				}
			}
		}
		else if (trigger_name.equals("w2_open_side_door_1"))
		{
			if (!m_hero.owns_named_object("w2_steel_chalice"))
			{
				// avoid being stuck without the chalice with the door above closed
				
				open_door("w2_side_door_1");
			}
		}
		else if (trigger_name.equals("w2_close_side_door_1"))
		{
			if (m_hero.owns_named_object("w2_steel_chalice"))
			{
				// no way back
				
				close_door("w2_side_door_1");
			}
		}
		else if (trigger_name.equals("w2_marker_1"))
		{
			if (!m_w2_marker_2)
			{
				display_special_bonus("w2_shortcut_bonus");
				close_door("w2_trap_1");
				m_w2_marker_1 = true;
			}
		}
		else if (trigger_name.equals("w2_marker_2"))
		{
			m_w2_marker_2 = true;
		}
		else if (trigger_name.equals("w2_marker_3"))
		{
			if (m_w2_marker_1)
			{
				display_special_bonus("w2_special_extra_life");
				open_door("w2_trap_1");
			}
		}
		else if (trigger_name.equals("w2_display_part_2_key"))
		{
			// can proceed to part 2 only if owns chalice
			
			if (m_hero.owns_named_object("w2_iron_chalice"))
			{
				display("w2_part_2_door_key");
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w2_lives_bonus"))
		{
			display_lives_bonus(trigger_name, 3);
		}
		else if (trigger_name.equals("w2_move_shortcut_block"))
		{
			if (!m_hero.owns_named_object("w2_door_key_1"))
			{
				move_block("w2_top_platform_start",false);
			}
		}
		else if (trigger_name.equals("w2_spike_shield"))
		{
			// if lever has been reset then show invincibility
			if (!is_lever_activated("w2_open_door_1"))
			{
				display(trigger_name);
			}
		}
		else if (trigger_name.equals("w2_speed_bonus_2"))
		{
			// huge speed bonus
			
			if (display_speed_bonus("w2_speed_bonus_2_1", 200))
			{
				display("w2_speed_bonus_2_2",false);
			}
			
		}
		else if (trigger_name.equals("w2_display_key_zone"))
		{
			if (m_hero.owns_named_object("w2_gold_chalice") && m_hero.owns_named_object("w2_steel_chalice") 
					&& m_hero.owns_named_object("w2_iron_chalice"))
			{
				display("w2_world_key");
			}
			else
			{
				rval = false;
			}
		}
		// world 3
		
		else if (trigger_name.equals("complete_puzzle"))
		{
			for (int i = 1; i <= 4; i++)
			{
				String p = "puzzle_"+i;

				if (m_hero.steal_named_object(p))
				{
					ControlObject co = m_level_data.get_control_object(p);
					
					if (co != null)
					{
						GfxFrame gfs = m_level_data.get_grid().get(co.get_x(), co.get_y());
						
						String new_name = gfs.get_source_set().get_name().replace("_hole", "");
						
						GfxFrameSet new_gfs = m_level_data.get_level_palette().lookup_frame_set(new_name);
						
						if (new_gfs != null)
						{
							get_sfx_set().play(Sample.puzzle);
							
							m_level_data.get_grid().set(co.get_x(), co.get_y(), new_gfs.get_first_frame());
							m_solved_puzzles++;
						}
					}
				}
			}
			
			rval = false;
		}
		else if (trigger_name.equals("w3_crypt_special_bonus"))
		{
			if (m_w3_is_crypt_door_closed && m_hero.owns_named_object("w3_herb"))
			{
				display_special_bonus(trigger_name);
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w3_lower_platform_block"))
		{
			if (m_hero.owns_named_object("puzzle_4"))
			{
				move_block("w3_platform_up_start", true);
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w3_globe_trigger"))
		{
			if (m_hero.steal_named_object("w3_globe"))
			{
				GfxObject globe = m_level_data.get_bonus("w3_globe_support");
				
				globe.set_current_frame(2);
				
				show_display_animation(globe);
				
				display("puzzle_4");

			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w3_display_puzzle_2"))
		{
			if (m_hero.steal_named_object("w3_skull"))
			{
				display("w3_herb",false);
				display("puzzle_2");
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w3_display_part_2_key"))
		{
			if (m_solved_puzzles == 4)
			{
				display("w3_part_2_door_key");
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w3_kill_spikes_2"))
		{
			if (m_hero.owns_named_object("w3_candle"))
			{
				destroy("w3_spikes_2");
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w3_raise_catch_platform"))
		{	
			// kill spikes, useful if hero falls without the key
			
			destroy("w3_spikes_6");
			
			// enable other monsters for a less monotonous return :)
			
			enable_trigger("w3_gmt_12");
			enable_trigger("w3_gmt_17");
			
			if (m_hero.owns_named_object("w3_door_key_4"))
			{
				if (m_w3_catch_fall)
				{
					display_special_bonus("w3_platform_special_bonus");
					move_block("w3_catch_platform_start", false);
				}
			}
			else
			{
				rval = false;
			}
			
			
		}
		else if (trigger_name.equals("w3_speed_bonus_1"))
		{
			display_speed_bonus(trigger_name, 40);
		}
		else if (trigger_name.startsWith("w3_help_bonus"))
		{
			display_help_lives_bonus(trigger_name);
		}
		else if (trigger_name.startsWith("w3_help_health_bonus"))
		{
			display_help_health_bonus(trigger_name);
		}
		
		else if (trigger_name.equals("w3_open_teleport_trap"))
		{
			// if player did not touch the kill spike lever (best)
			// or player reset the kill spike lever (average)
			// then give access to teleport to the study
			
			if (!is_lever_activated("w3_kill_spikes_6"))
			{
				open_door("w3_teleport_trap");
			}
		}
		else if (trigger_name.equals("w3_lives_bonus"))
		{
			display_lives_bonus(trigger_name, 3);
		}
		else if (trigger_name.equals("w3_close_boss_door"))
		{
			display_boss();
			close_door("w3_boss_door");
			lock_scrolling("boss_2_fix_scroll_top_left");
		}
		
		return rval;
	}

	private boolean check_gem(String gem_name)
	{
		boolean rval = (m_hero.steal_named_object(gem_name));
		
		if (rval)
		{
			m_level_data.get_bonus(gem_name+"_light").set_current_frame(2);
			
			m_w1_nb_gems++;
			
			if (m_w1_nb_gems == 3)
			{
				open_door("w1_world_door");
			}
		}
		
		return rval;
	}
	@Override
	protected void on_world_restart(int world_count) {
		// TODO Auto-generated method stub

	}

}
