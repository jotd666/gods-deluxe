package gods.game.levels.classic;

import java.io.IOException;

import gods.base.GfxObject;
import gods.base.NamedLocatable;
import gods.base.MovingBlock;
import gods.game.GodsLevel;
import gods.game.SfxSet.Sample;
import gods.game.characters.Hero;
import gods.game.characters.HeroWeaponSet;

public class GodsClassicLevel3 extends GodsLevel 
{
	private int m_next_drop_sound = 1000;
	
	@Override
	protected void p_update() 
	{
		m_next_drop_sound -= get_elapsed_time();
		
		if (m_next_drop_sound < 0)
		{
			get_sfx_set().play_random(Sample.drops);
			
			m_next_drop_sound = (int)(Math.random() * 2000) + 500;
		}
		super.p_update();
	}

	public static final String [] SHOP_CONTENTS_W3_END = { "chicken", "apple", "weapon_arc_wide",
		"weapon_arc_intense", "weapon_arc_standard", "bread", "freeze",
		"half_energy", "invincibility", "mace",
		"full_energy", "starburst", "fire_ball", "hunter",
		"axe", "increase_wp_medium", "spear", "shield", "extra_life", "familiar", "increase_wp_full" };
	public static final String [] SHOP_CONTENTS_W3_1 = { "chicken", "apple", "weapon_arc_wide",
		"weapon_arc_intense", "weapon_arc_standard", "bread", "freeze",
		"half_energy", "invincibility", "mace",
		"full_energy", "starburst", "fire_ball",
		"axe", "increase_wp_medium", "spear", "shield", "extra_life", "familiar", "increase_wp_full" };

	private boolean m_w1_display_room_key = false;
	private boolean m_w1_shortcut_open = false;
	private boolean m_w1_first_time_treasure_room_lever = true;
	private boolean m_w1_starburst_displayed = false;
	private boolean m_w2_secret_pressed = false;
	private boolean m_w3_secret_pressed = false;
	private boolean m_w2_secret_block_moved = false;
	private boolean m_w2_gem_block_moved_back = false;
	private boolean m_w2_hostile_block_moved = false;
	private boolean m_w2_ladder_block_moved = false;
	private int m_w2_nb_gems = 0;
	private int m_w2_nb_lives_ladder = 0;
	private boolean m_w3_spike_trigger_countdown = false;
	private int m_w3_nb_created_heads = 0;

	public GodsClassicLevel3()
	throws IOException 
	{
		super(GodsClassicLevel2.SHOP_CONTENTS_W3_END);
	}

	@Override
	protected boolean on_bonus_taken(GfxObject bonus) 
	{
		if (bonus.get_name().equals("w1_shop"))
		{
			/*set_item_price("increase_wp_half",17500);
			set_item_price("increase_wp_full",32500);*/
			
			summon_shopkeeper(SHOP_CONTENTS_W3_1,false);
		}
		else if (bonus.get_name().equals("w3_world_end_shop"))
		{
			summon_shopkeeper(SHOP_CONTENTS_W3_END,false);
		}
		return false;
	}

	@Override
	protected void on_boss_death() 
	{
		display("w3_world_key");
	}

	@Override
	protected void on_button_pressed(String button_name) 
	{
		if (button_name.equals("w2_big_treasure_button"))
		{
			m_w2_secret_pressed = true;
		}
		else if (button_name.equals("w3_secret_button_1"))
		{
			m_w3_secret_pressed = true;
		}
		else if (button_name.equals("w3_secret_button_2"))
		{
			// comment line below to quickly test big chest bonus sequence
			if ((m_w2_secret_pressed) && (m_w3_secret_pressed))
			{
				display("w3_big_chest");
				enable_trigger("w3_open_big_chest");
			}
		}

	}

	@Override
	protected void on_door_change_state(String door_name, boolean is_open) 
	{
		if (door_name.equals("w1_shortcut_weaponry"))
		{
			// activate the special bonus for using the shortcut (end of world)
			
			enable_trigger("w1_end_special_bonus_trigger");
		}
		else if (door_name.equals("w1_teleport_door"))
		{
			if (m_w1_shortcut_open)
			{
				display_special_bonus("w1_shortcut_used_special_bonus_1");
			}
		}
		else if (door_name.equals("w1_hive_pit_out_door"))
		{
			m_hive_pit_block.move();
			destroy("w1_5_seconds_spike");
		}
		else if (door_name.equals("w2_gem_monster_trap_1"))
		{
			open_door("w2_gem_monster_trap_2");
		}
		else if (door_name.equals("w1_main_treasure_room_door"))
		{
			if (is_open)
			{
				if (m_hero.get_weapon_set().get_arc() != HeroWeaponSet.Arc.wide)
				{
					// display help weapon arc wide or hero won't be able to reach
					// world key
					
					display("w1_weapon_arc_wide");
				}
			}
		}
		else if (door_name.equals("w2_door_2"))
		{
			if (m_w2_ladder_block_moved)
			{
				// move back last block: no turning back and
				// no enemy attack
				
				move_block("w2_ladder_block_2_start", true);
				
			}
		}
		else if (door_name.equals("w3_trap_2"))
		{
			if (is_open)
			{
				open_door("w3_trap_1");
			}
		}
		else if (door_name.equals("w3_trap_3"))
		{
			enable_trigger("w3_close_trap_4");
			enable_trigger("w3_move_block_1");
		}
	}

	@Override
	protected void on_level_loaded() 
	{	
		set_tile_behind("blocking_wall_3","wall_12");

		m_hive_pit_block = get_moving_block("w1_hive_room_block_start");
	}

	private MovingBlock m_hive_pit_block = null;
	private boolean m_maces_killed = false;
	
	private void on_lever_activated_world_1(String lever_name,LeverActivationState state)
	{
		if (lever_name.equals("w1_starburst_lever"))
		{
			display("w1_starburst");
			m_w1_starburst_displayed = true;
		}
		else if (lever_name.equals("w1_5_seconds_lever"))
		{
			if (state == LeverActivationState.FIRST_ACTIVATED)
			{
				open_door("w1_beehive_trap");
				create_hostile("w1_beehive_1");
			}
		}
		else if (lever_name.equals("w1_weaponry_lever_back"))
		{
			if ((!m_maces_killed) && (state == LeverActivationState.FIRST_ACTIVATED))
			{
				if (!m_hero.owns_named_object("w1_door_key_3"))
				{
					// trap: kill maces to be able to avoid taking maces
					// if player requires it

					move_block("w1_weapon_remove_start", false);
					m_maces_killed = true;
				}

			}
			open_door("w1_weaponry_door_out");
		}
		else if (lever_name.equals("w1_open_trap_bowl"))
		{
			if (is_lever_activated(lever_name))
			{
				if (m_hero.owns_named_object("w1_bowl"))
				{
					open_door("w1_teleport_trap_4");
				}
			}
		}
		else if (lever_name.equals("w1_room_lever_4"))
		{
			if (is_lever_activated(lever_name))
			{
				if (m_w1_display_room_key)
				{
					display("w1_room_key");
					m_w1_display_room_key = false;
				}
			}
		}
		else if (lever_name.equals("w1_treasure_room_lever"))
		{
			if ((state == LeverActivationState.FIRST_ACTIVATED) && 
					(m_w1_first_time_treasure_room_lever))
			{
				m_w1_first_time_treasure_room_lever = false;

				if (m_hero.get_health(true) > Hero.MAX_HEALTH/2)
				{
					display("w1_special_bonus_full_health");
				}
				else
				{
					display("w1_special_bonus_not_full_health");
				}
			}
		}
		else if (lever_name.equals("w1_bee_room_create_hive"))
		{
			create_hostile("w1_beehive_2");
		}
		else if (lever_name.equals("w1_main_treasure_room_lever"))
		{
			if (is_lever_activated(lever_name))
			{
				if (m_hero.steal_named_object("w1_treasure_key_1") || m_hero.steal_named_object("w1_treasure_key_2"))
				{
					open_door("w1_main_treasure_room_door");
				}
			}
		}
	}
	
	private void on_lever_activated_world_2(String lever_name,LeverActivationState state)
	{
		if (lever_name.equals("w2_move_ladder_block_1"))
		{
			enable_trigger("w2_display_ladder_energy");
			
			if (!m_w2_ladder_block_moved)
			{
				m_w2_ladder_block_moved = true;
				move_block("w2_ladder_block_2_start", false);
				move_block("w2_ladder_block_3_start", false);
			}
		}
		else if (lever_name.equals("w2_open_door_3b"))
		{
			open_door("w2_door_3");
		}
		else if (lever_name.equals("w2_open_thief_traps"))
		{
			// reset the lever kills the spike
			
			if (state == LeverActivationState.DEACTIVATED)
			{
				destroy("w2_spike_1");
			}
		}
		else if (lever_name.equals("w2_move_gem_block_back"))
		{
			if ((!m_w2_gem_block_moved_back) && 
					(is_lever_activated(lever_name) && m_hero.owns_named_object("w2_water_gem")))
			{
				move_block("w2_gem_block_start",true);
				m_w2_gem_block_moved_back = true;
			}
		}
		else if (lever_name.equals("w2_open_close_gem_trap"))
		{
			if (state == LeverActivationState.DEACTIVATED)
			{
				close_door("w2_gem_monster_trap_3");
			}
		}
		else if (lever_name.equals("w2_open_water_gem_door"))
		{
			if (state == LeverActivationState.DEACTIVATED)
			{
				destroy("w2_spike_2");
			}
		}
		else if (lever_name.equals("w2_open_door_5"))
		{
			// always open door (can be required twice)
			
			open_door("w2_door_5");
		}
		
		// lever/gem riddle
		
		if (is_lever_activated(lever_name))
		{
			if (lever_name.equals("w2_ice_lever_1"))
			{
				boolean l2 = is_lever_activated("w2_ice_lever_2");
				boolean l3 = is_lever_activated("w2_ice_lever_3");

				open_door("w2_door_5");

				if (!l2 && l3)
				{
					display("w2_secret_passage_shield");
				}
			}
			else if (lever_name.equals("w2_ice_lever_2"))
			{
				boolean l1 = is_lever_activated("w2_ice_lever_1");
				boolean l3 = is_lever_activated("w2_ice_lever_3");

				if (!m_w2_hostile_block_moved && !l1 && !l3)
				{
					m_w2_hostile_block_moved = true;
					move_block("w2_hostile_block_start", false);
				}
				if (!l1 && l3)
				{
					open_door("w2_hostile_trap_1");
				}
			}
			else if (lever_name.equals("w2_ice_lever_3"))
			{
				boolean l1 = is_lever_activated("w2_ice_lever_1");
				boolean l2 = is_lever_activated("w2_ice_lever_2");

				if (!m_w2_secret_block_moved && l1 && l2)
				{
					m_w2_secret_block_moved = true;
					move_block("w2_secret_room_block_start", false);
				}
				else if (!l1 && !l2)
				{
					create_hostile("w2_spike_3");
				}
			}
		}
	}
	
	
	private void on_lever_activated_world_3(String lever_name,LeverActivationState state)
	{
		if (lever_name.equals("w3_lever_1"))
		{
			if (create_hostile("w3_spitting_head_1"))
			{
				m_w3_nb_created_heads++;
			}
		}
		else if (lever_name.equals("w3_lever_2"))
		{
			if (create_hostile("w3_spitting_head_2"))
			{
				m_w3_nb_created_heads++;
			}
		}			
		else if (lever_name.equals("w3_open_close_trap_lever"))
		{
			if (state == LeverActivationState.DEACTIVATED)
			{
				close_door("w3_trap_2");
			}
		}
		else if (lever_name.equals("w3_boss_door_lever"))
		{
			if (is_lever_activated(lever_name))
			{
				if (m_hero.owns_named_object("w3_pot_1") && m_hero.owns_named_object("w3_pot_2"))
				{
					m_hero.steal_named_object("w3_pot_1");
					m_hero.steal_named_object("w3_pot_2");
					
					display("w3_trap_door_key");
				}
			}
			else
			{
				if (m_hero.steal_named_object("w3_boss_key"))
				{
					open_door("w3_boss_door_in");
				}
			}
		}
		else if (lever_name.equals("w3_lever_3"))
		{
			if (!m_w3_spike_trigger_countdown)
			{
				m_w3_spike_trigger_countdown = true;
				add_timer_event(new TriggerEnableTimerEvent("w3_spike_trigger_3",5000));
				add_timer_event(new TriggerEnableTimerEvent("w3_spike_trigger_1",8000));
				add_timer_event(new TriggerEnableTimerEvent("w3_spike_trigger_2",10000));
			}
		}
		else if (lever_name.equals("w3_enable_block_move_lever"))
		{
			enable_trigger("w3_move_platform");
			
			if (m_w3_nb_created_heads == 2)
			{
				// protection mode enabled: move block
				move_block("w3_block_stones_start",false);
			}
		}
	}
	@Override
	protected void on_lever_activated(GfxObject lever,
			LeverActivationState state) 
	{
		String lever_name = lever.get_name();
		
		on_lever_activated_world_1(lever_name,state);
		
		on_lever_activated_world_2(lever_name,state);
		
		on_lever_activated_world_3(lever_name,state);


	}

	@Override
	protected void on_object_drop(GfxObject bonus) 
	{

	}

	@Override
	protected void on_object_picked_up(GfxObject object) 
	{
	
	}

	@Override
	protected void on_room_entered(String room_door_name) {
		if (room_door_name.equals("w1_teleport_door"))
		{
			m_w1_display_room_key = true;
		}

	}

	@Override
	protected boolean on_trigger_activated(String trigger_name) 
	{
		boolean rval = true;

		if (trigger_name.equals("w1_5_seconds_spike_trigger"))
		{
			print("five seconds to exit room");
			// delayed hostile creation
			create_hostile("w1_5_seconds_spike",5000);

		}
		else if (trigger_name.equals("w1_display_shortcut_key"))
		{
			if (m_w1_starburst_displayed)
			{
				display("w1_shortcut_key");
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w1_trigger_shortcut"))
		{
			if (m_hero.steal_named_object("w1_shortcut_key") || get_clock() < 25)
			{
				move_block("w1_shortcut_start", false);
				print("short cut");
				m_w1_shortcut_open = true; // special bonus 1
				create_hostile("w1_gm_shortcut");				
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w1_move_block_1"))
		{
			// change start/end coordinates
			// start coordinates being current coordinates
			
			m_hive_pit_block.get_start().set_coordinates(m_hive_pit_block, false);
			
			// end is above end move of the first part
			NamedLocatable l = m_hive_pit_block.get_end();
			l.set_y(l.get_y()-l.get_height());
			
			// trigger the move (may be a little diagonal)
			
			m_hive_pit_block.move();
		}
		else if (trigger_name.equals("w1_health_and_lives_bonus_trigger_1"))
		{
			rval = display_health_and_lives_bonus("w1_health_and_lives_bonus_1",3); // checked	
		}
		else if (trigger_name.equals("w1_health_and_lives_bonus_trigger_2"))
		{
			rval = display_health_and_lives_bonus("w1_health_and_lives_bonus_2",3); // checked	
		}
		else if (trigger_name.equals("w1_final_help_bonus_trigger"))
		{
			rval = display_help_lives_bonus("w1_final_help_bonus");
		}
		else if (trigger_name.equals("w1_help_health_bonus_trigger"))
		{
			rval = display_help_health_bonus("w1_help_health_bonus");
		}
		else if (trigger_name.startsWith("w1_remove_bomb"))
		{
			if (m_hero.remove_weapon("time_bomb"))
			{
				print("your bomb has been removed");
			}
			else
			{
				// always try to remove bomb when passing nearby,
				// original game has a bug here in the shortcut section
				
				rval = false;
			}
		}
		else if (trigger_name.equals("w1_treasure_room_trigger"))
		{
			close_door("w1_main_treasure_room_door");
			create_hostile("w1_gm_treasure_room_1");
			create_hostile("w1_gm_treasure_room_2");
		}
		else if (trigger_name.startsWith("w1_remove_attract_monster"))
		{
			if (m_hero.steal_named_object("w1_attract_monster"))
			{
				print("your potion has been removed");
			}
			else
			{
				rval = false;
			}
		}
		if (rval)
		{
			rval = on_trigger_activated_world_2(trigger_name);
		}
		if (rval)
		{
			rval = on_trigger_activated_world_3(trigger_name);
		}
		return rval;
	}

	private boolean on_trigger_activated_world_2(String trigger_name)
	{
		boolean rval = true;
		
		if (trigger_name.equals("w2_close_theif_lid"))
		{
			move_block("w2_thief_lockup_room_block_start",false);
			m_w2_nb_lives_ladder = m_hero.get_nb_lives();
		}
		else if (trigger_name.equals("w2_special_bonus_2"))
		{
			// case when hero could kill thief before he locks himself up
			// in the vault and get hold of the fire gem
			
			if (m_hero.owns_named_object("w2_fire_gem"))
			{
				display_special_bonus("w2_special_bonus_2");
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w2_display_treasure_key"))
		{
			// max health and no life lost since bottom of the ladder gives
			// the treasure key
			
			if ( (m_hero.get_nb_lives() >= m_w2_nb_lives_ladder) && 
					(m_hero.get_health(true) == m_hero.get_max_health()))
			{
				display("w2_trap_door_key");
			}
			else
			{
				rval = false; // maybe later?
			}
		}
		else if (trigger_name.equals("w2_open_treasure_trap"))
		{
			if (m_hero.steal_named_object("w2_trap_door_key"))
			{
				display("w2_treasure_shield");
				move_block("w2_treasure_room_lid_start",false);
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w2_close_treasure_trap"))
		{
			move_block("w2_treasure_room_lid_start",true);			
		}
		else if (trigger_name.equals("w2_gem_count"))
		{
			
			if (m_hero.steal_named_object("w2_fire_gem"))
			{
				m_w2_nb_gems++;
				m_level_data.get_bonus("w2_fire_light").set_current_frame(4);
			}
			if (m_hero.steal_named_object("w2_water_gem"))
			{
				m_level_data.get_bonus("w2_water_light").set_current_frame(2);
				m_w2_nb_gems++;
			}
			if (m_hero.steal_named_object("w2_ice_gem"))
			{
				m_level_data.get_bonus("w2_ice_light").set_current_frame(3);
				m_w2_nb_gems++;
			}
			
			if (m_w2_nb_gems == 3)
			{
				open_door("w2_world_door");
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w2_block_exit"))
		{
			move_block("w2_gem_block_start",false);
		}
		else if (trigger_name.equals("w2_remove_potion"))
		{
			rval = false;
			
			if (m_hero.owns_named_object("w2_fire_gem") ||
					m_hero.owns_named_object("w2_ice_gem") ||
					m_hero.owns_named_object("w2_water_gem"))
			{
				rval = m_hero.steal_named_object("w2_attract_monster");
				if (rval)
				{
					print("your potion has been removed");
				}
			}
		}
		/*else if (trigger_name.equals("w2_close_door"))
		{
			close_door("w2_door_5");
		}*/
		else if (trigger_name.equals("w2_help_life_bonus"))
		{
			display_help_lives_bonus(trigger_name);
		}
		return rval;
	}
	
	private boolean on_trigger_activated_world_3(String trigger_name)
	{
		boolean rval = true;
		
		if (trigger_name.equals("w3_move_platform"))
		{
			if (m_w3_nb_created_heads == 2)
			{ 
				// move away platform which protected against heads
				MovingBlock p = get_moving_block("w3_block_stones_start");
				NamedLocatable l_start = p.get_start();
				NamedLocatable l_end = p.get_end();
				// symmetrize
				l_start.set_x(2 * l_end.get_x() - l_start.get_x());
				// move back (actually move left)
				move_block("w3_block_stones_start",true);
			}
			else
			{
				move_block("w3_fire_chrystal_catwalk_start",false);
			}
		}
		else if (trigger_name.equals("w3_enable_spike_start"))
		{
			// no turning back...
			enable_trigger("w3_spike_start_trigger");
		}
		else if (trigger_name.equals("w3_close_trap_4"))
		{
			close_door("w3_trap_4");
		}
		else if (trigger_name.equals("w3_lives_bonus"))
		{
			display_lives_bonus(trigger_name, 3);
		}
		else if (trigger_name.equals("w3_move_block_1"))
		{
			move_block("w3_platform_1_start",false);
			enable_trigger("w3_move_block_2");
			print("get the key in fifteen seconds");
			create_hostile("w3_gm_fall_1");
			create_hostile("w3_gm_fall_2");
			
			add_timer_event(new TriggerEnableTimerEvent("w3_open_trap_6",15000));
		}
		else if (trigger_name.equals("w3_move_block_2"))
		{
			move_block("w3_platform_2_start",false);			
			enable_trigger("w3_move_block_3");
		}
		else if (trigger_name.equals("w3_move_block_3"))
		{
			move_block("w3_platform_3_start",false);			
			
		}
		else if (trigger_name.equals("w3_open_trap_6"))
		{
			open_door("w3_trap_6a");
			open_door("w3_trap_6b",false);
		}
		else if (trigger_name.equals("w3_open_big_chest"))
		{
			GfxObject go = m_level_data.get_bonus("w3_big_chest");
			//go.spawn_linked_objects(m_level_data);
			
			// display mega bonus
			
			NamedLocatable l = m_level_data.get_control_object("w3_huge_bonus_zone");
			int x_step = l.get_width() / 10;
			int y_step = l.get_height() / 3;
			
			for (int i = 0; i < 10; i++)
			{
				create_bonus(l.get_x() + i * x_step, l.get_y(), "diamond_4");
			}
			for (int i = 0; i < 9; i++)
			{
				create_bonus(l.get_x() + i * x_step + x_step / 2, l.get_y() + y_step, "gold_bag");
			}
			for (int i = 0; i < 9; i++)
			{
				create_bonus(l.get_x() + i * x_step + x_step / 2, l.get_y() + y_step * 2, "fire_chrystal");
			}
			
			
		}
		else if (trigger_name.equals("boss_trigger"))
		{
			display_boss();
		}
		
		return rval;
	}
	@Override
	protected void on_world_restart(int world_count) 
	{
		if (world_count == 2)
		{
			close_door("w1_world_door");
		}

	}

}
