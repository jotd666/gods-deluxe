package gods.game.levels.classic;

import gods.base.ControlObject;
import gods.base.GfxFrame;
import gods.base.GfxFrameSet;
import gods.base.GfxObject;
import gods.base.MovingBlock;
import gods.game.GodsLevel;
import gods.game.SfxSet.Sample;
import gods.game.characters.Hostile;
import gods.game.characters.HostileParameters;

import java.io.IOException;

public class GodsClassicLevel2 extends GodsLevel 
{
	// level state
	private int m_platform_move_counter = 0;
	private boolean m_one_at_a_time_displayed = false;
	private int m_w3_block_1_move = 0;
	private int m_solved_puzzles = 0;
	private boolean m_last_shopkeeper = false;
	private boolean m_w2_corridor_door_open = false;
	
	private MovingBlock m_world_key_platform = null;
	private MovingBlock m_puzzle_block = null;
	private MovingBlock m_w3_platform_1 = null;
	


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

	// methods
	
	private final static String W1_BAIT_KEY = "w1_bait_chest_key";
	
	@Override
	protected void on_boss_death() 
	{
		display("w3_world_key");
	}

	public GodsClassicLevel2() throws IOException 
	{
		super(GodsClassicLevel1.SHOP_CONTENTS_W3_END);
		
		
	}

	@Override
	protected boolean on_bonus_taken(GfxObject bonus) 
	{
		String name = bonus.get_name();
		boolean rval = true;
		
		// world 2
		if (name.equals("w2_end_shopkeeper_token"))
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
				// at start value is 10000, change it
				gfs.get_properties().value = 32500;
			}
			summon_shopkeeper(SHOP_CONTENTS_W2_END,true);
		}
		else if (name.equals("w3_end_shopkeeper_token"))
		{
			summon_shopkeeper(SHOP_CONTENTS_W3_END,false);
			m_last_shopkeeper = true;
		}
		return rval;
	}

	@Override
	protected void on_button_pressed(String button_name) 
	{
		if (button_name.equals("w1_thief_button_2"))
		{
			create_hostile("w1_health_thief");
		}
	}

	@Override
	protected void on_door_change_state(String door_name, boolean is_open) {
		if (door_name.equals("w2_corridor_door_2"))
		{
			m_w2_corridor_door_open = true;
		}
	}
	
	@Override
	protected void on_lever_activated(GfxObject lever,
			LeverActivationState state) {
		String lever_name = lever.get_name();
		
		if (lever_name.equals("w1_gem_room_lever"))
		{
			switch (state)
			{
			case FIRST_ACTIVATED:
				open_door("w1_gem_room_door_back");
				break;
			case ACTIVATED:
				if (open_door("w1_gem_room_trap"))
				{
					print("teleport to bonus section");					
				}
				break;
			}
		}
		else if (lever_name.equals("w1_top_section_door_lever"))
		{
			if (state == LeverActivationState.FIRST_ACTIVATED)
			{
				open_door("w1_to_top_section");
				open_door("w1_thief_riddle_trap_2");
				display("w1_treasure_key");
				enable_trigger("w1_treasure_key_thief_trigger");
			}
		}
		else if (lever_name.equals("w2_bottom_pit_lever_1"))
		{
			if (m_hero.steal_named_object("w2_bottom_room_key_1"))
			{
				open_door("w2_corridor_door_2");
				create_hostile("w2_gm_bottom_1");
			}
			else
			{
				if ((!m_w2_corridor_door_open) && display("w2_teleport_1"))
				{
					print("now get the room key in pit one");
				}
			}
		}
		
		// lever 2 must be activated first, then lever 1
		
		else if (lever_name.equals("w2_riddle_lever_1"))
		{
			if (!is_lever_activated("w2_riddle_lever_2"))
			{
				open_door("w2_trap_1");
			}	
		}
		else if (lever_name.equals("w2_riddle_lever_2"))
		{
			if (is_lever_activated("w2_riddle_lever_1"))
			{
				open_door("w2_trap_2");
			}
		}
		else if (lever_name.startsWith("w3_inside_apothecary_lever"))
		{
			// different from lever/door association: done all the time
			// it may happen that hero dies with door closed: restart outside
			// may also happen when door is open (close trigger only works once)
			// and upper lever is activated: door must remain open and not
			// "toggled" to closed
			
			if (is_lever_activated(lever_name))
			{
				open_door("w3_inside_apothecary_door");
			}
		}
		// world 3
		else if (lever_name.equals("w3_move_block_lever_2"))
		{
			switch (state)
			{
			case FIRST_ACTIVATED:
				create_hostile("w3_start_gm");
				break;
			case DEACTIVATED:
				if (m_hero.steal_named_object("w3_platform_1_key"))
				{
					m_w3_platform_1.move();
				}
				break;
			}
		}
		else if (lever_name.equals("w3_move_block_lever_right"))
		{
			if (m_hero.owns_named_object("w3_door_key_1"))
			{
				switch (m_w3_block_1_move)
				{
				case 0:

					m_puzzle_block.move();
					m_w3_block_1_move++;
					break;
				case 2:
					open_door("w3_puzzle_door");
					m_hero.steal_named_object("w3_door_key_1");
					break;
				}
			}
		}
		else if (lever_name.equals("w3_move_block_lever_up"))
		{
			if (m_hero.owns_named_object("w3_door_key_1") && (m_w3_block_1_move == 1))
			{
				ControlObject l = m_puzzle_block.get_end();
				m_puzzle_block.get_start().set_coordinates(l, false);
				l.set_y(l.get_y()-l.get_height());
				
				m_puzzle_block.move();
				m_w3_block_1_move++;
			}
		}
		else if (lever_name.equals("w3_close_trap_stair_3_and_4"))
		{
			close_door("w3_trap_stair_3");
			close_door("w3_trap_stair_4");
		}
		else if (lever_name.equals("w3_close_trap_stair_5_and_6"))
		{
			close_door("w3_trap_stair_5");
			close_door("w3_trap_stair_6");
		}
		else if (lever_name.equals("w3_trap_lid_lever_2"))
		{
			if ( m_hero.owns_named_object("w3_trap_door_key") && m_hero.owns_named_object("w3_candle_1") && 
					is_lever_activated("w3_trap_lid_lever_2") )
			{
				m_hero.steal_named_object("w3_trap_door_key");
				move_block("w3_lid_2_start", false);
			}
		}
		else if (lever_name.equals("w3_world_end_lever"))
		{
			if (m_hero.steal_named_object("w3_world_key"))
			{
				// shopkeeper token
				
				display("w3_end_shopkeeper_token");
			}
			if (m_last_shopkeeper)
			{
				open_door("w3_level_end");
			}
		}
	}

	@Override
	protected void on_object_drop(GfxObject bonus) 
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void on_object_picked_up(GfxObject object) 
	{
		// TODO Auto-generated method stub

	}


	@Override
	protected void on_room_entered(String room_door_name) 
	{
		if (room_door_name.equals("w2_chalice_room_entry"))
		{
			int nb_chalices = 0;
			if (zone_contains("w2_chalice_room_bounds","iron_chalice")) nb_chalices++;
			if (zone_contains("w2_chalice_room_bounds","steel_chalice")) nb_chalices++;
			if (zone_contains("w2_chalice_room_bounds","gold_chalice")) nb_chalices++;
			
			// if the 3 chalices are left in the chalice room, display the chest & key
			// if only 2 chalices are in the chalice room, only display the chest
			// (and the key is lost forever)
			
			if (nb_chalices > 2)
			{
				if (display_special_bonus("w2_chalice_steel_chest_key"))
				{
					if (nb_chalices == 3)
					{
						// riddle complete
						
						display("w2_chalice_steel_chest",false);
					}
				}
			}

		}
		
		else if (room_door_name.equals("w3_armoury_exit_door"))
		{
			create_hostile("w3_armoury_thief");
		}
		else if (room_door_name.equals("w3_crypt_exit_door"))
		{
			create_hostile("w3_crypt_gm");
		}
		else if (room_door_name.equals("w3_apothecary_exit_door"))
		{
			create_hostile("w3_apothecary_wall_hostile");
		}
	}

	private boolean on_trigger_activated_1(String trigger_name)
	{
		boolean rval = true;

		if (trigger_name.equals("w1_start"))
		{
			display_health_and_lives_bonus("w1_lives_bonus_1",2);

			String otd = null;

			if (m_hero.get_nb_lives() < 2)
			{
				// don't activate treasure key puzzle by hiding the chest key
				// (and if hero does not own this key, only easy route will be possible)

				otd = "w1_help_extra_life";
			}
			else
			{
				// remove extra life (released on the other hostile of the wave, hack but easier
				// this way)
				otd = W1_BAIT_KEY;
			}

			Hostile h = get_hostile("w1_start_gm");

			HostileParameters hp = h.get_params();

			hp.object_to_drop = m_level_data.get_bonus(otd);
			m_level_data.remove_object(hp.object_to_drop);
		}
		else if (trigger_name.equals("w1_gmt_key_1"))
		{
			create_hostile("w1_gm_2");
			create_hostile("w1_gm_3");

			if (m_hero.owns_named_object(W1_BAIT_KEY) && is_button_activated("w1_thief_riddle_button"))
			{
				// trigger extra monsters

				create_hostile("w1_pit_spikes");
				create_hostile("w1_spikes_3");
				enable_trigger("w1_falling_gmt_4");
				enable_trigger("w1_wall_hostile_34_trigger");
			}
			else
			{
				if (!is_button_activated("w1_thief_riddle_button"))
				{
					// simple (although this key is useless because it is impossible
					// to get to the chest: it is in the treasure room) => this key is useless
					// is this a flaw of the Bros design? I don't know.

					display("w1_chest_key_steel_easy_route");
				}
			}
		}
		else if (trigger_name.equals("w1_speed_bonus"))
		{
			display_speed_bonus(trigger_name,20);
		}
		else if (trigger_name.equals("w1_top_section"))
		{	
			int c = 0;

			if (m_hero.steal_named_object(W1_BAIT_KEY)) { c++; }
			//if (m_hero.steal("w1_chest_key_iron_easy_route")) { c++; }
			//if (m_hero.steal("w1_chest_key_steel_easy_route")) { c++; }

			if (c > 0)
			{
				print("keys swooped, don't worry");
				display("w1_chest_key_steel_hard_route");
			}

			disable_trigger("w1_falling_gmt_4");
		}
		else if (trigger_name.equals("w1_lives_bonus_2_trigger"))
		{
			if (m_hero.get_nb_lives() > 2)
			{
				display("w1_lives_bonus_2");
				print("lives bonus");
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w1_gmt_8_ladder_down"))
		{
			rval = m_hero.owns_named_object("ice_gem") && is_hostile_alive("w1_pit_spikes");

			// only enabled if hard route

			if (rval)
			{
				create_hostile("w1_gm_8_ladder_down");
				create_hostile("w1_gm_8b_ladder_down");
			}
		}
		else if (trigger_name.equals("w1_gem_drop_zone"))
		{
			boolean owns_ice_gem = m_hero.owns_named_object("ice_gem");
			boolean owns_water_gem = m_hero.owns_named_object("water_gem");
			boolean owns_fire_gem = m_hero.owns_named_object("fire_gem");

			int counter = 0;

			if (owns_ice_gem) { counter++; }
			if (owns_water_gem) { counter++; }
			if (owns_fire_gem) { counter++; }


			if ((counter > 1) && (!m_one_at_a_time_displayed))
			{
				print("one at a time");
				m_one_at_a_time_displayed = true;
			}
			else if (counter == 1)
			{
				if ( m_hero.steal_named_object("ice_gem") || m_hero.steal_named_object("water_gem") || m_hero.steal_named_object("fire_gem") )
				{
					m_platform_move_counter++;
					if (m_platform_move_counter > 1)
					{
						// lower the platform
						ControlObject start = m_world_key_platform.get_start();
						start.set_coordinates(start.get_x(), start.get_y() + start.get_height());
						ControlObject end = m_world_key_platform.get_end();
						end.set_coordinates(end.get_x(), end.get_y() + end.get_height());

					}
					String light_name = "w1_light_"+m_platform_move_counter;

					m_level_data.get_bonus(light_name).set_current_frame(2);

					m_world_key_platform.move();
				}

			}
			rval = false;
		}
		else if (trigger_name.equals("w1_health_bonus"))
		{
			display_health_bonus("w1_health_bonus");
		}

		return rval;
	}
	
	private boolean on_trigger_activated_2(String trigger_name, boolean rval_in)
	{
		boolean rval = rval_in;

		if (rval)
		{
			// no previous trigger has re-enabled it for next time: not sure a trigger had a match
			
			if (trigger_name.equals("w2_teleport_1_location"))
			{
				rval = display_help_health_bonus("w2_help_bonus_1");			
			}
			else if (trigger_name.equals("w2_chalice_room_treasure_trigger"))
			{
				// only one chance to bring the chalices and get treasure key
				
				if (m_hero.owns_named_object("steel_chalice") && m_hero.owns_named_object("iron_chalice") && m_hero.owns_named_object("gold_chalice"))
				{
					display("w2_treasure_key");
				}
				else
				{					
					rval = false;
				}
			}
			else if (trigger_name.equals("w2_help_bonus_2_trigger"))
			{
				rval = display_help_lives_bonus("w2_help_bonus_2");
			}
			else if (trigger_name.equals("w2_health_bonus_trigger"))
			{
				rval = display_health_bonus("w2_health_bonus_1");
			}
		}
		return rval;
	}
	
	
	@Override
	protected boolean on_trigger_activated(String trigger_name) 
	{
		boolean rval = true;
		
		rval = on_trigger_activated_1(trigger_name);
	
		rval = on_trigger_activated_2(trigger_name,rval);
		
		// world 3
		
		if (trigger_name.equals("complete_puzzle"))
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
		else if (trigger_name.equals("w3_lower_platform_1"))
		{
			if (m_hero.owns_named_object("w3_door_key_1"))
			{
				m_w3_platform_1.reverse();
				m_w3_platform_1.move();
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w3_display_door_key"))
		{
			if (m_hero.owns_named_object("w3_candle_1"))
			{
				display("w3_door_key");
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w3_crypt_display_puzzle"))
		{
			if (m_hero.steal_named_object("skull"))
			{
				display("puzzle_2");
				display("herb",false);
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w3_apothecary_close_door"))
		{
			close_door("w3_inside_apothecary_door");
			create_hostile("w3_apothecary_gm");
		}
		else if (trigger_name.equals("w3_globe_trigger"))
		{
			if (m_hero.steal_named_object("globe"))
			{
				GfxObject globe = m_level_data.get_bonus("globe_support");
				
				globe.set_current_frame(2);
				
				show_display_animation(globe);
				
				display("puzzle_4");

			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w3_show_teleport"))
		{
			if (m_solved_puzzles == 4)
			{
				display("w2_puzzle_teleport");
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w3_health_bonus_trigger"))
		{
			rval = display_health_bonus("w3_health_bonus");
		}
		else if (trigger_name.equals("w3_move_candle_platform"))
		{
			move_block("w3_candle_platform_start", false);
		}
		else if (trigger_name.equals("w3_help_health_bonus"))
		{
			display_help_health_bonus(trigger_name);
		}
		else if (trigger_name.equals("w3_lower_platform_2"))
		{
			if (m_hero.owns_named_object("w3_room_platform_key"))
			{
				move_block("w3_rising_platform_2_start", true);
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w3_help_bonus_trigger"))
		{
			rval = display_help_lives_bonus("w3_help_bonus");
		}
		else if (trigger_name.equals("w3_help_health_bonus"))
		{
			rval = display_help_health_bonus("w3_health_help_bonus");
			
		}
		else if (trigger_name.equals("w3_open_trap_door"))
		{
			open_door("w3_trap_stair_7");
		}
		else if (trigger_name.equals("w3_kill_spikes_cross"))
		{
			if (!is_lever_activated("w3_close_trap_stair_5_and_6") &&
					!is_lever_activated("w3_close_trap_stair_3_and_4") &&
					!is_lever_activated("w3_close_trap_stair_2"))
			{
				destroy("w3_spike_cross_1");
				destroy("w3_spike_cross_2");
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w3_speed_bonus_1"))
		{
			if (get_clock() < 60)
			{
				print("on target for huge speed bonus");
				display("w3_speed_bonus_1");
			}
		}
		else if (trigger_name.equals("w3_speed_bonus_2"))
		{
			// mega-speed bonus if 230 seconds since world 3 part 2 (not sure about
			// # of seconds, maybe it's more, abrasion completed it with all puzzles
			// solved and some mistakes in 210 seconds so I think it's OK)
			//
			// cross must be owned or this does not work (speedrun on Sega version
			// did not show this speed bonus, because cross is not taken)
			
			if ((get_clock() < 230) && (m_hero.owns_named_object("w3_cross")))
			{
				display("w3_speed_and_special_bonus");
				display("w3_special_bonus_1",false);
				display("w3_special_bonus_2",false);
			}
		}
		else if (trigger_name.equals("w3_move_big_room_platform"))
		{
			MovingBlock p = m_level_data.get_moving_block("w3_big_room_platform_start");
			
			// change start position, like in the original game
			p.set_x((p.get_end().get_x() + p.get_start().get_x()) / 2);
			
			p.move();
			
		}
		else if (trigger_name.equals("w3_move_big_room_2_platform"))
		{
			move_block("w3_big_room_platform_2_start", false);
		}
		else if (trigger_name.equals("w3_study_display_door_key"))
		{
			if (m_hero.steal_named_object("w3_candle_1"))
			{
				display("w3_boss_door_key");
				if (m_hero.owns_named_object("w3_cross"))
				{
					display("w3_room_key_diamond");
				}
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w3_chapel_display_gem"))
		{
			if (m_hero.steal_named_object("w3_cross"))
			{
				display("dragon_gem");
			}
		}
		else if (trigger_name.equals("w3_chapel_display_treasure_key"))
		{
			if (m_hero.owns_named_object("candle_1") && m_hero.owns_named_object("candle_2"))
			{
				display("w3_treasure_key");
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w3_close_boss_door"))
		{
			display_boss();
			close_door("w3_boss_door");
			lock_scrolling("boss_2_fix_scroll_top_left");
		}
		return rval;
	}

	@Override
	protected void on_world_restart(int world_count) 
	{
		
		switch (world_count)
		{
		case 3:
			close_door("w2_world_door");
			break;
		}
	}

	// methods & specific code
	
	@Override
	protected void on_level_loaded()
	{
				
		// replace big diamond image by small gem in item holder
		GfxObject go = m_level_data.get_bonus("dragon_gem");
		go.set_miniature_image(m_level_data.get_level_palette().lookup_frame_set("gem").toImage());
				
		disable_trigger("w1_falling_gmt_4");
		disable_trigger("w1_treasure_key_thief_trigger");
		
		// no lever in treasure room of world 2: open the door
		open_door("w2_treasure_room_exit",false);
		
		// world 2
		m_world_key_platform = get_moving_block("w1_world_key_platform_start");
		
		// world 3
		m_puzzle_block = get_moving_block("w3_wall_block_start");
		m_w3_platform_1 = get_moving_block("w3_rising_platform_start");
		
		MovingBlock p = get_moving_block("w3_candle_platform_start");
		p.set_coordinates(m_level_data.get_control_object("w3_candle_platform_start_hidden"));
		
	}
}
