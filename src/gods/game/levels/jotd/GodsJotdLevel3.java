package gods.game.levels.jotd;

import java.io.IOException;

import gods.base.GfxObject;
import gods.base.NamedLocatable;
import gods.base.MovingBlock;
import gods.game.GodsLevel;
import gods.game.characters.HeroWeaponSet;


public class GodsJotdLevel3 extends GodsLevel {

	private int m_nb_lever_wriggles = 0;
	private int m_nb_level_wriggles_with_flask = 0;
	private boolean m_w1_cell_lever_activated = false;
	private boolean m_w2_special_bonus_1_displayed = false;
	private int m_w3_nb_gems = 0;
	private boolean m_w3_gem_puzzle_done = false;
	private boolean m_w3_traps_open = false;
	private boolean m_w3_push_gem_block_moved = false;
	private boolean m_w3_puzzle_3_done = false;
	private boolean m_w3_gem_puzzle_failed = false;
	private boolean m_w2_secret_pressed = false;
	
	public static final String [] SHOP_CONTENTS_W3_1 = { "chicken", "apple", "weapon_arc_wide",
		"weapon_arc_intense", "weapon_arc_standard", "bread", "freeze",
		"half_energy", "invincibility", "mace",
		"full_energy", "starburst", "fire_ball",
		"axe", "increase_wp_medium", "spear", "shield", "extra_life", "familiar", "increase_wp_full" };
	public static final String [] SHOP_CONTENTS_W3_END = { "chicken", "apple", "weapon_arc_wide",
		"weapon_arc_intense", "weapon_arc_standard", "bread", "freeze",
		"half_energy", "invincibility", "mace",
		"full_energy", "starburst", "fire_ball", "hunter",
		"axe", "increase_wp_medium", "spear", "shield", "extra_life", "familiar", "increase_wp_full" };

	public GodsJotdLevel3() throws IOException 
	{
		super(GodsJotdLevel2.SHOP_CONTENTS_W3_END);
	}	
	
	
	private class TeleportDisplayTimerEvent extends TimerEvent
	{
		public TeleportDisplayTimerEvent() 
		{
			super(60000); // 60 seconds to reach safety point
		}
		
		@Override
		protected void on_timeout() 
		{
			display("w2_help_teleport");
			
			disable_trigger("w2_display_special"); // no bonus					
			
			
		}
		
	}
	private class OpenJailTrapsTimer extends TimerEvent
	{
		public OpenJailTrapsTimer() 
		{
			super(5000);
		}
		
		@Override
		protected void on_timeout() 
		{
			print("w1_let_sentence_begin");
			open_door("w1_jail_trap_1");
			open_door("w1_jail_trap_2",false);
			open_door("w1_jail_trap_3",false);
		}
		
	}
	


	@Override
	protected void on_boss_death()
	{
		display("w3_world_key");
	}

	@Override
	protected void on_button_pressed(String button_name) {
		if (button_name.equals("w1_secret_1"))
		{
			enable_trigger("w1_display_giant_jump");
		}
		else if (button_name.equals("w2_secret_1"))
		{
			move_block("w2_block_1_start",false);
		}
		else if (button_name.equals("w2_secret_2"))
		{
			m_w2_secret_pressed = true;
		}
		else if (button_name.equals("w3_secret_1"))
		{
			if (m_w2_secret_pressed)
			{
				display("w3_big_chest");
				enable_trigger("w3_open_big_chest");
			}
		}

	}

	@Override
	protected void on_door_change_state(String door_name, boolean is_open) 
	{
		if (door_name.equals("w1_door_1"))
		{
			// add timer to open the traps
			
			print("w1_weapons_removed");
			m_hero.steal_money(50000);
			add_timer_event(new OpenJailTrapsTimer());
			m_hero.get_weapon_set().remove_all();
		}
		else if (!is_open && door_name.equals("w1_door_2"))
		{
			print("welcome to your cell");
			move_block("w1_block_door_2_start", false);
		}
		else if (!is_open && door_name.equals("w1_bonus_1_room_door"))
		{			
			open_door("w1_jail_trap_6");
			open_door("w1_jail_trap_7",false);
		}
		else if (is_open && door_name.equals("w1_guard_room_door"))
		{
			open_door("w1_jail_trap_4");
			open_door("w1_jail_trap_5");
		}
		else if (!is_open && door_name.equals("w1_door_7"))
		{
			if (!is_lever_activated("w1_open_door_7"))
			{
				// if lever is reset then close both traps
				close_door("w1_jail_trap_4");
				close_door("w1_jail_trap_5");
			}
		}
		else if (door_name.equals("w1_door_10"))
		{
			if (m_hero.get_weapon_set().get_arc() != HeroWeaponSet.Arc.wide)
			{
				display("w1_arc_wide");
			}
		}
		else if (!is_open)
		{
			if (door_name.equals("w2_stair_trap_1"))
			{
				print("get the key in fifteen seconds");
				add_timer_event(new TriggerEnableTimerEvent("w2_open_traps",10000));
				enable_trigger("w2_close_stair_trap_2");
			}
			else if (door_name.equals("w2_stair_trap_2"))
			{
				enable_trigger("w2_close_stair_trap_3");
			}
		}
		else if (door_name.equals("w2_door_10_out"))
		{
			create_hostile("w2_hive_3a", 1000);
			create_hostile("w2_hive_3b", 2000);
		}
		else if (door_name.equals("w3_door_1"))
		{
			print("shortcut");
			open_door("w3_gem_trap_5");
			open_door("w3_gem_trap_6",false);
		}
		else if (door_name.equals("w3_to_boss_0"))
		{
			// allow access to lower treasure room (if player comes back)
			
			move_block("w3_treasure_block_start",false);
		}
		else if (door_name.equals("w3_door_2"))
		{
			close_door("w3_gem_trap_5");
			close_door("w3_gem_trap_6");
			
		}
	}

	@Override
	protected void on_level_loaded() 
	{
		set_tile_behind("blocking_wall_3","wall_20");

	}

	@Override
	protected void on_lever_activated(GfxObject lever,
			LeverActivationState state) 
	{
		String lever_name = lever.get_name();
		
		if (lever_name.equals("w1_wriggle_lever"))
		{
			if ((state == LeverActivationState.ACTIVATED) != m_w1_cell_lever_activated)
			{
				m_w1_cell_lever_activated = !m_w1_cell_lever_activated;
				
			if (!m_hero.owns_named_object("w1_cell_flask"))
			{
				m_nb_lever_wriggles++;
				if (m_nb_lever_wriggles == 15)
				{
					print("the door finally opens");
					open_door("w1_cell_door_1");
				}
				
			}
			else
			{
				m_nb_level_wriggles_with_flask++;
				if (m_nb_level_wriggles_with_flask == 30)
				{
					print("don't steal government property");
				}
			}
			}
		}
		else if (lever_name.equals("w1_reopen_bonus_door"))
		{
			open_door("w1_bonus_1_room_door");
		}
		else if (state == LeverActivationState.FIRST_ACTIVATED && lever_name.equals("w1_close_bonus_door"))
		{
			close_door("w1_bonus_1_room_door");
		}
		else if (lever_name.equals("w1_world_lever"))
		{
			if (m_hero.owns_named_object("w1_world_key"))
			{
				if (!display("w1_shop"))
				{
					if (state == LeverActivationState.DEACTIVATED)
					{
						open_door("w1_world_door");
					}
				}
			}
		}
		else if (lever_name.equals("w2_create_hive"))
		{
			create_hostile("w2_hive_1");
			create_hostile("w2_hive_2");
		}
		else if (lever_name.equals("w3_gem_puzzle_lever_1"))
		{
			if (state == LeverActivationState.FIRST_ACTIVATED)
			{
				// drop the gem & the monster
				open_door("w3_gem_trap_1");
				open_door("w3_gem_trap_2",false);
				open_door("w3_gem_trap_3",false);
				m_w3_traps_open = true;
			}
		}
		else if (lever_name.equals("w3_gem_puzzle_lever_2"))
		{
			if (is_lever_activated(lever_name))
			{
				if (is_lever_activated("w3_gem_puzzle_lever_1"))
				{
					if (!m_w3_gem_puzzle_done)
					{
					// check if gem in zone: if not, not fallen yet: open zone
					if (!zone_contains("w3_gem_bounds","w3_fire_gem"))
					{
						move_block("w3_block_gem_start", false);
						// reward the player for solving the puzzle
						display_special_bonus("w3_gem_special_bonus");
					}
					else
					{
						m_w3_gem_puzzle_failed = true;
					}
					m_w3_gem_puzzle_done = true;
					}
					
					
				}
			}
			
			if (!m_w3_push_gem_block_moved)
			{
				if ((m_w3_gem_puzzle_done && m_w3_traps_open && m_hero.owns_named_object("w3_fire_gem")) || 
						(m_w3_gem_puzzle_failed && zone_contains("w3_gem_bounds","w3_fire_gem")))
				{
					// drop platform on the block so gem falls below / enables treasure key access
					move_block("w3_push_gem_start",false);
					m_w3_push_gem_block_moved = true;
				}
			}
			
		}
		else if (lever_name.equals("w3_lever_3") && is_lever_activated(lever_name))
		{
			// only opens if hero has no gem
			
			if (!m_hero.owns_named_object("w3_fire_gem") && !m_hero.owns_named_object("w3_ice_gem"))
			{
				open_door("w3_door_3");
			}
		}
		else if (lever_name.equals("w3_gem_puzzle_lever_3"))
		{
			// both levers must be reset to gain access to bonus
			if (!is_lever_activated("w3_gem_puzzle_lever_1") &&
					!is_lever_activated("w3_gem_puzzle_lever_2"))
			{
				if (!m_w3_puzzle_3_done)
				{
					MovingBlock mblk = get_moving_block("w3_block_gem_start");
					mblk.swap();
					
					mblk.get_end().set_coordinates(mblk.get_x(),mblk.get_y()-mblk.get_height());

					mblk.set_move_duration(1000);
					
					mblk.move();

					m_w3_puzzle_3_done = true;
				}
			}
			else
			{
				// condemn access
				create_hostile("w3_spike_4");
			}
		}
	}

	@Override
	protected void on_object_drop(GfxObject bonus) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void on_object_picked_up(GfxObject object) 
	{
	}
	
	@Override
	protected boolean on_bonus_taken(GfxObject object)
	{
		if (object.get_name().equals("w2_help_teleport"))
		{
			close_door("w2_part_2_in",false); // avoid re-entry
			
			if (m_w2_special_bonus_1_displayed)
			{
				// part 2 completed & re-entry using teleport
				display_special_bonus("w2_special_bonus_2");
			}
		}
		else if (object.get_name().equals("w1_shop"))
		{
			boolean door_open = is_door_open("w1_door_8");
			// comes from left or right depending on door open or not
			summon_shopkeeper(SHOP_CONTENTS_W3_1,door_open);
		}
		else if (object.get_name().equals("w3_shop"))
		{
			summon_shopkeeper(SHOP_CONTENTS_W3_END,true);
		}
		return false;
	}

	@Override
	protected void on_room_entered(String room_door_name) {
		if (room_door_name.equals("w2_part_2"))
		{
			add_timer_event(new TeleportDisplayTimerEvent());
		}

	}

	@Override
	protected boolean on_trigger_activated(String trigger_name) 
	{
		boolean rval = true;
		
		// avoid that hero skips the world key, uses the secret passage
		// and remains stuck at world door
		
		if (trigger_name.equals("w1_open_ladder"))
		{
			if (m_hero.owns_named_object("w1_world_key"))
			{
				move_block("w1_block_start", false);
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w1_display_standard_arc"))
		{
			// safety: don't display standard arc if world key has
			// not retrieved
			
			if (m_hero.owns_named_object("w1_world_key"))
			{
				display("w1_arc_standard");
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w1_display_speed_bonus_1"))
		{
			display_speed_bonus("w1_speed_bonus_1",30);
		}
		else if (trigger_name.equals("w1_display_speed_bonus_2"))
		{
			display_speed_bonus("w1_speed_bonus_2",90);
			close_door("w1_guard_room_door");
		}
		else if (trigger_name.equals("w1_help_bonus"))
		{
			display_help_health_bonus(trigger_name);
			display_health_bonus("w1_health_bonus");
			rval = false;
		}	
		else if (trigger_name.equals("w1_lives_bonus"))
		{
			display_lives_bonus(trigger_name,3);
		}
		else if (trigger_name.equals("w2_close_shield_trap"))
		{
			close_door("w2_shield_trap");
			rval = false; // always try to close
		}
		else if (trigger_name.equals("w2_help_bonus"))
		{
			display_help_lives_bonus(trigger_name);
			rval = false;
		}
		else if (trigger_name.equals("w2_move_block_3"))
		{	
			move_block("w2_block_3_start",false);
		}
		else if (trigger_name.equals("w2_display_teleport"))
		{
			int nb_bowls = 0;
			if (m_hero.steal_named_object("w2_bowl_1"))
			{
				nb_bowls++;
			}
			if (m_hero.steal_named_object("w2_bowl_2"))
			{
				nb_bowls++;
			}
			switch (nb_bowls)
			{
			case 0:
				rval = false; // revive trigger
				break;
			case 1:
				display("w2_teleport_part_3_1");
				break;
			case 2:
				display_special_bonus("w2_teleport_part_3_2");
				break;				
			}
		}
		else if (trigger_name.equals("w2_move_block_2"))
		{
			move_block("w2_block_2_start",false);
		}
		else if (trigger_name.equals("w2_open_traps"))
		{
			open_door("w2_trap_2a");
			open_door("w2_trap_2b");
		}
		else if (trigger_name.equals("w2_display_special"))
		{
			m_w2_special_bonus_1_displayed = true;
			
			display_special_bonus("w2_special_bonus_1");
		}
		else if (trigger_name.equals("w2_move_block_2"))
		{
			move_block("w2_block_2_start",false);
		}
		else if (trigger_name.equals("w3_spike_bonus"))
		{
			if (is_hostile_alive("w3_spike_1"))
			{
				// reward the player for reaching this area with the spike on
				destroy("w3_spike_1");
				display_special_bonus(trigger_name);
			}
		}
		else if (trigger_name.equals("w3_enable_gmt_19"))
		{
			if (m_hero.owns_named_object("w3_fire_gem"))
			{
			// encounter monsters on the way back
			enable_trigger("w3_gmt_19a");
			enable_trigger("w3_gmt_19b");
			}
			else
			{
				move_block("w3_push_gem_start", true);
			}
		}
		else if (trigger_name.equals("w3_gem_test"))
		{
			if (m_hero.steal_named_object("w3_fire_gem"))
			{
				m_w3_nb_gems++;
				m_level_data.get_bonus("w3_fire_light").set_current_frame(4);
			}
			if (m_hero.steal_named_object("w3_water_gem"))
			{
				m_level_data.get_bonus("w3_water_light").set_current_frame(2);
				m_w3_nb_gems++;
				close_door("w3_door_2");
			}
			if (m_hero.steal_named_object("w3_ice_gem"))
			{
				m_level_data.get_bonus("w3_ice_light").set_current_frame(3);
				m_w3_nb_gems++;
			}
			
			if (m_w3_nb_gems == 3)
			{
				open_door("w3_gem_door");
			}
			else
			{
				rval = false;
			}
			
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
		else if (trigger_name.equals("w3_help_bonus"))
		{
			display_help_health_bonus(trigger_name);
		}
		else if (trigger_name.equals("w3_lives_bonus"))
		{
			rval = display_lives_bonus(trigger_name, 3);
		}
		else if (trigger_name.equals("boss_trigger"))
		{
			display_boss();
		}
		return rval;
	}

	@Override
	protected void on_world_restart(int world_count) {
		// TODO Auto-generated method stub

	}

}
