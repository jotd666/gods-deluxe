package gods.game.levels.jotd;

import gods.base.GfxObject;
import gods.game.GodsLevel;

public class GodsJotdLevel1 extends GodsLevel 
{
	public static final String [] SHOP_CONTENTS_W3_START = { "chicken", "apple", "weapon_arc_wide",
		"weapon_arc_intense", "weapon_arc_standard", "bread", "knife", "freeze",
		"half_energy", "throwing_star", "invincibility", "increase_wp_medium",
		"full_energy", "starburst"};

	public static final String [] SHOP_CONTENTS_W3_END = { "chicken", "apple", "weapon_arc_wide",
		"weapon_arc_intense", "weapon_arc_standard", "bread", "freeze",
		"half_energy", "throwing_star", "invincibility", "increase_wp_medium",
		"full_energy", "starburst", "fire_ball", "magic_axe", "extra_life"};

	private boolean m_w1_allow_secret_room_1 = true;
	private boolean m_w1_secret_pressed = false;
	private boolean m_w2_puzzle_trap_closed = false;
	private boolean m_w2_door_open = false;
	//private boolean m_w1_puzzle_1_done = false;
	private boolean m_w1_puzzle_2_done = false;
	
	private boolean m_w3_secret_1 = false;
	private boolean m_w3_can_open_end_door = false;
	
	public GodsJotdLevel1() throws java.io.IOException
	{
		super(null);
	}
	
	@Override
	protected boolean on_bonus_taken(GfxObject bonus) 
	{
		boolean rval = false;
		if (bonus.get_name().equals("w1_knife_1"))
		{
			m_w1_allow_secret_room_1 = false;
			rval = true;
		}
		else if (bonus.get_name().equals("w1_shortcut_teleport"))
		{
			int nb_knives = m_hero.weapon_power_of("knife");
			
			display("w1_shortcut_bonus");
			
			if (nb_knives < 2)
			{
				// short cut taken and not enough weapons: help the player
				
				display("w1_help_knife_1",false);
			}
			if (nb_knives < 3)
			{
				// short cut taken and not enough weapons: help the player
				
				display("w1_help_knife_2");
				print("help bonus");
			}
		}
		else if (bonus.get_name().equals("w2_shop"))
		{
			summon_shopkeeper(SHOP_CONTENTS_W3_START,false);
			
		}
		else if (bonus.get_name().equals("w3_shop_end"))
		{
			m_w3_can_open_end_door = true;
			m_hero.remove_weapon("lightning_bolt");
			summon_shopkeeper(SHOP_CONTENTS_W3_END,true);
		}
		return rval;
	}

	@Override
	protected void on_boss_death() 
	{
		make_visible("w3_world_door_lever");
	}

	@Override
	protected void on_button_pressed(String button_name) {
		
		if (button_name.equals("w1_secret"))
		{
			m_w1_secret_pressed = true;
			
			display_special_bonus("w1_fire_chrystal");
			
		}
		else if (button_name.equals("w3_secret_1"))
		{
			if (display_special_bonus("w3_fire_chrystal_special_bonus"))
			{
				m_w3_secret_1 = true;
				
				close_door("w3_puzzle_trap_3");
				
				// disable lever below (to avoid accidents :))
				
				disable_lever("w3_close_puzzle_trap_3");
			}
		}
		else if (button_name.equals("w3_secret_2"))
		{
			enable_trigger("w3_display_giant_jump");
		}

	}

	@Override
	protected void on_door_change_state(String door_name, boolean is_open)
	{
		if (!is_open && door_name.equals("w2_trap_2"))
		{
			m_w2_puzzle_trap_closed = true;
		}
		else if (door_name.equals("w2_trap_1"))
		{
			display_speed_bonus("w2_speed_bonus",35);
		}
		else if (door_name.equals("w2_puzzle_door_1"))
		{
			close_door("w2_trap_2");
		}
		else if (door_name.equals("w2_world_trap"))
		{
			display("w2_shop");
		}
		else if (door_name.equals("w3_end_trap_1"))
		{
			close_door("w3_end_trap_2");
		}
		else if (door_name.equals("w2_teleport_trap_1"))
		{
			print("shortcut");
		}
		else if (door_name.equals("w2_teleport_trap_2"))
		{
			print("w1_challenge");
		}
		else if (door_name.equals("w2_teleport_trap_3"))
		{
			print("w1_treasure_key_room");
		}
	}

	@Override
	protected void on_level_loaded() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void on_lever_activated(GfxObject lever,
			LeverActivationState state) 
	{
		String lever_name = lever.get_name();
		
		if (lever_name.equals("w1_puzzle_lever_1"))
		{
			create_hostile("w1_gm_lever_1");
		}
		else if (lever_name.equals("w1_puzzle_lever_2"))
		{
			if (is_lever_activated(state))
			{
				if (is_lever_activated("w1_puzzle_lever_1") &&
					is_lever_activated("w1_puzzle_lever_3"))
				{
					display("w1_chest_key",false);
					display_special_bonus("w1_chest");
					m_w1_puzzle_2_done = true;
				}
			}
		}
		else if (lever_name.equals("w2_open_door_2"))
		{
			if (m_hero.steal_named_object("w2_door_key_2"))
			{
				open_door("w2_door_2");
				m_w2_door_open = true;
			}
			else
			{
				if ((!m_w2_door_open) && (display("w2_emergency_teleport")))
				{
					print("forgot something ?");
				}
			}
		}
		else if (lever_name.equals("w2_open_teleport_trap_1"))
		{
			if (m_hero.owns_named_object("w2_world_key"))
			{
				open_door("w2_teleport_trap_1");
			}
		}
		else if (lever_name.equals("w2_trigger_attacking_monsters"))
		{
			create_hostile("w2_attacking_gm",1500);
		}
		else if (lever_name.equals("w2_health_bonus"))
		{
			display_health_bonus(lever_name);
		}
		else if (lever_name.equals("w2_trigger_spike_1"))
		{
			create_hostile("w2_spike_1");
			disable_lever("w2_open_teleport_trap_4");
		}
		else if (lever_name.equals("w2_open_shutting_door"))
		{
			if (m_hero.owns_named_object("w2_world_key"))
			{
				open_door("w2_shutting_door");
				create_hostile("w2_gm_12");
			}
		}
		else if (lever_name.equals("w2_monster_room_kill_lever"))
		{
			display("w2_monster_room_kill");
		}
		else if (lever_name.equals("w3_display_treasure_teleport"))
		{
			if (m_hero.steal_named_object("w3_treasure_key"))
			{
				display("w3_to_bolt_room");
			}
		}
		else if (lever_name.equals("w3_world_door_lever"))
		{
			if (state == LeverActivationState.FIRST_ACTIVATED)
			{
				display("w3_shop_end");
			}
			else if (m_w3_can_open_end_door)
			{
				open_door("w3_world_door");
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
		if (object.get_name().equals("w2_pot_2"))
		{
			display("w2_teleport_back_secret_pot_room");
		}

	}

	@Override
	protected void on_room_entered(String room_door_name) {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean on_trigger_activated(String trigger_name) 
	{
		
		boolean rval = true;
		
		if (trigger_name.equals("w1_open_secret_1"))
		{
			if (m_w1_allow_secret_room_1)
			{
				print("the reward for the pain");
				move_block("w1_puzzle_1_start", false);
				//m_w1_puzzle_1_done = true;
			}
		}
		else if (trigger_name.equals("w1_open_secret_2"))
		{
			if ((get_clock() < 130) && (m_w1_puzzle_2_done))
			{
				move_block("w1_puzzle_2_start", false);				
			}
		}
		else if (trigger_name.equals("w1_speed_bonus_trigger"))
		{
			if (get_clock() < 50)
			{
				print("speed bonus");
				display("w1_speed_bonus");
			}
		}
		else if (trigger_name.equals("w1_end_bonus"))
		{
			display_health_and_lives_bonus("w1_health_lives_bonus",1,22);
			
			rval = display_help_lives_bonus("w1_help_bonus");
		}
		else if (trigger_name.equals("w1_shortcut_trigger"))
		{
			print("shortcut");
			display("w1_shortcut_teleport");
			display("w1_shortcut_bonus",false);
		}
		// world 2
		
		else if (trigger_name.equals("w2_display_giant_jump"))
		{
			if (m_w1_secret_pressed)
			{
				display("w2_giant_jump");
			}
		}
		else if (trigger_name.equals("w2_check_trap"))
		{
			// check if trap is open: if so display special bonus
			if (!m_w2_puzzle_trap_closed)
			{
				if (display("w2_special_bonus_1",false))
				{
					display_special_bonus("w2_special_bonus_2");
					
				}
			}
		
		}
		else if (trigger_name.equals("w2_open_teleport_wall"))
		{
			// teleport has already been taken: open the wall
			move_block("w2_puzzle_1_start", false);
			
			// lever above won't work anymore
			
			disable_lever("w2_open_door_1_lever");
			
			// display huge bonus
			
			if (display("w2_special_bonus_3",false))
			{
				display_special_bonus("w2_special_bonus_4");
			}
		}
		else if (trigger_name.equals("w2_teleport_monster_room"))
		{
			set_restart_location("w2_teleport_room_restart");
		}
		else if (trigger_name.equals("w2_monster_room_bonus"))
		{
			if (is_lever_activated("w2_monster_room_kill_lever"))
			{
				display("w2_monster_room_extra_life");
				display("w2_monster_room_fire_chrystal",false);
				display("w2_monster_room_teleport_back",false);
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w2_before_end_restart"))
		{
			// no way back from there: too bad for the treasure key if forgotten
			close_door("w2_shutting_door");
		}
		else if (trigger_name.equals("w2_lower_platform"))
		{
			if (m_hero.steal_named_object("w2_pot_4") && m_hero.steal_named_object("w2_pot_3"))
			{
				display_special_bonus("w2_pot_bonus");
			}
			if (m_hero.owns_named_object("w2_world_key"))
			{
				move_block("w2_raising_block_start", true);
			}
			else
			{
				rval = false;
			}
		}

		else if (trigger_name.equals("w2_pot_room_trigger"))
		{
			boolean owns_pot_1 = m_hero.steal_named_object("w2_pot_1");
			boolean owns_pot_2 = m_hero.steal_named_object("w2_pot_2");
			
			rval = owns_pot_1 || owns_pot_2;
			
			if (rval)
			{
				display("w2_world_key");
				if (owns_pot_1 && owns_pot_2)
				{
					display("w2_store_fire_chrystal",false);
					display_special_bonus("w2_store_water_chrystal");
				}
			}
		}
		else if (trigger_name.equals("w2_display_help_bonus_1"))
		{
			display_help_health_bonus("w2_help_bonus_1");
		}
		else if (trigger_name.equals("w2_display_help_bonus_2"))
		{
			display_help_lives_bonus("w2_help_bonus_2");
		}
		else if (trigger_name.equals("w2_activate_trigger_14"))
		{
			enable_trigger("w2_gmt_14");
		}
		else if (trigger_name.equals("w2_shut_door"))
		{
			// check that player has the key (if dropped, player could be stuck)
			if (m_hero.owns_named_object("w2_world_key"))
			{
				close_door("w2_shutting_door");
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w2_teleport_treasure_key_room"))
		{
			set_restart_location("w2_teleport_room_restart");
		}
		// world 3
		else if (trigger_name.equals("w3_speed_bonus_1"))
		{
			display_speed_bonus(trigger_name,30);
		}
		else if (trigger_name.equals("w3_speed_bonus_2"))
		{
			display_speed_bonus(trigger_name,60);
		}
		else if (trigger_name.equals("w3_open_big_block"))
		{
			move_block("w3_bottom_block_start",false);
		}
		else if (trigger_name.equals("w3_display_shield"))
		{
			if (m_w3_secret_1)
			{
				display("w3_spike_shield");
			}
			else
			{
				rval = false;
			}
		}
		
		else if (trigger_name.equals("w3_enable_gmt_23"))
		{
			enable_trigger("w3_gmt_23");
		}
		else if (trigger_name.equals("w3_lives_bonus"))
		{
			display_lives_bonus(trigger_name, 3);
		}
		else if (trigger_name.equals("w3_enable_fmt_40"))
		{
			enable_trigger("w3_fmt_40");
		}
		else if (trigger_name.equals("w3_help_bonus"))
		{
			display_help_health_bonus(trigger_name);
		}
		else if (trigger_name.equals("w3_another_health_help_bonus"))
		{
			display_help_health_bonus(trigger_name);			
		}
		else if (trigger_name.equals("w3_display_boss"))
		{
			display_boss();
		}
		return rval;
	}

	@Override
	protected void on_world_restart(int world_count) {
		switch (world_count)
		{
		case 2:
			// close world 1 door
			close_door("w1_world_door");
			break;
		case 3:
			// close world 1 door
			close_door("w2_world_trap");
			break;
		}
	}

}
