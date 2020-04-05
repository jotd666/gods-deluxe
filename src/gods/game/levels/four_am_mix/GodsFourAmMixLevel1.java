package gods.game.levels.four_am_mix;

import java.io.IOException;

import gods.base.GfxObject;
import gods.game.GodsLevel;


public class GodsFourAmMixLevel1 extends GodsLevel 
{
	public static final String [] SHOP_CONTENTS_W1_END = { "chicken", "weapon_arc_wide",
		"weapon_arc_intense", "weapon_arc_standard", "apple", "bread", "knife",
		"half_energy", "invincibility", "increase_wp_medium",
		"full_energy", "starburst","extra_life", "increase_wp_full", "fire_ball",  };

	
	private boolean m_w1_shortcut = false;
	private boolean m_w2_secret_1_pressed = false;
	private boolean m_w2_secret_2_pressed = false;
	private boolean m_w2_hidden_block_start_moved = false;
	private boolean m_w1_spikes_12_killed = false;
	private boolean m_w1_end_lever_activated = false;
	private boolean m_w1_platform_moved = false;
	
	public GodsFourAmMixLevel1() throws IOException
	{
		super(null);
	}
	
	@Override
	protected boolean on_bonus_taken(GfxObject bonus) 
	{
		if (bonus.get_name().equals("shop"))
		{
			summon_shopkeeper(SHOP_CONTENTS_W1_END,true);
		}
		return false;
	}

	@Override
	protected void on_boss_death() {
		// nothing to do
	}

	@Override
	protected void on_button_pressed(String button_name) 
	{
		if (button_name.equals("w1_secret"))
		{
			display_special_bonus("w1_secret_bonus");
		}
		else if (button_name.equals("w2_secret_1"))
		{
			m_w2_secret_1_pressed = true;
		}
		else if (button_name.equals("w2_secret_2"))
		{
			m_w2_secret_2_pressed = true;
		}
		
		if (!m_w2_hidden_block_start_moved && m_w2_secret_1_pressed && m_w2_secret_2_pressed)
		{
			m_w2_hidden_block_start_moved = true;
			move_block("w2_hidden_block_start",false);
			enable_trigger("w2_oxt_1");
		}
	}

	@Override
	protected void on_door_change_state(String door_name, boolean is_open) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void on_lever_activated(GfxObject lever,
			LeverActivationState state) 
	{
		String lever_name = lever.get_name();
		if (lever_name.equals("w1_armoury_door_lever") && state == LeverActivationState.FIRST_ACTIVATED)
		{
			print("the door to the armoury");
		}
		else if (lever_name.equals("w1_treasure_room_lever") && state == LeverActivationState.FIRST_ACTIVATED)
		{
			print("the door to the treasure room");
		}
		else if (lever_name.equals("w1_kill_spikes12") && 
				(is_lever_activated(state)) && (!is_lever_activated("w1_show_chest_lever")))
		{
			destroy("w1_spikes_1");
			destroy("w1_spikes_2");
			m_w1_spikes_12_killed = true;
		}
		else if (lever_name.equals("w1_show_chest_lever"))
		{
			boolean l1 = is_lever_activated(state);
			boolean l2 = is_lever_activated("w1_kill_spikes12");

			if (l1)
			{
				create_hostile("w1_gm_8");
			}
			if (!l1 && l2)
			{
				destroy("w1_spikes_1");
				destroy("w1_spikes_2");
				m_w1_spikes_12_killed = true;
			}
			if (l1 && l2 && m_w1_spikes_12_killed)
			{
				display("w1_chest");
				display("w1_chest_key");

			}
		}
		
		else if (lever_name.equals("show_w1_gm_10"))
		{
			create_hostile("w1_gm_10");
		}
		else if (lever_name.equals("w1_kill_spikes5") && state == LeverActivationState.DEACTIVATED && 
				!is_lever_activated("w1_kill_spikes34") && m_hero.owns_named_object("w1_treasure_key"))
		{
			if (!m_w1_shortcut)
			{
				m_w1_shortcut = true;
				print("short cut");
				move_block("w1_shortcut_start", false);
			}
		}
		
		else if (lever_name.equals("w1_create_gm_10"))
		{
			create_hostile("w1_gm_10");
		}
		else if (lever_name.equals("w2_open_teleport_urn_trap"))
		{
			if (is_lever_activated(state) && 
					(m_hero.owns_named_object("w2_attract_monster_2") || m_hero.owns_named_object("w2_attract_monster")))
			{
				open_door("w2_teleport_urn_trap");
			}
		}
		else if (lever_name.equals("w2_return_from_urn_lever"))
		{
			if (is_lever_activated(state) && m_hero.owns_named_object("w2_urn"))
			{
				open_door("w2_urn_trap");
			}
		}
		else if (lever_name.equals("w2_open_3_traps"))
		{
			open_door("w2_trap_room_trap_1");
			open_door("w2_trap_room_trap_2");
			open_door("w2_trap_room_trap_3");
		}
		else if (lever_name.equals("w2_trap_room_lever_2"))
		{
			if (is_lever_activated(state) && is_lever_activated("w2_trap_room_lever_1") && 
					!is_lever_activated("w2_open_3_traps"))
			{
				open_door("w2_trap_room_trap_4");
			}
		}
		else if (lever_name.equals("w1_end_lever"))
		{
			// you don't need the key to open the door!!!! bug?
			//if (m_hero.steal_named_object("w1_door_key"))
			if (!m_w1_end_lever_activated)
				{
					open_door("w1_end_door");
					m_w1_end_lever_activated = true;
				}
				else if (!m_w1_platform_moved && (state == LeverActivationState.DEACTIVATED))
				{
						move_block("w1_secret_platform_start", false);
						m_w1_platform_moved = true;
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
	protected void on_room_entered(String room_door_name) 
	{
		
		

	}

	@Override
	protected boolean on_trigger_activated(String trigger_name) 
	{
		boolean rval = true; // default: disable trigger on exit
		
		if (trigger_name.equals("w2_help_bonus"))
		{
			display_help_health_bonus("w2_help_bonus_1");
			display_help_lives_bonus("w2_help_bonus_2");
			rval = false;
			
		}
		else if (trigger_name.equals("w1_speed_bonus"))
		{
			display_speed_bonus(trigger_name, 45);
		}
		else if (trigger_name.equals("w2_lower_platform"))
		{
			move_block("w2_hidden_platform_start",true);
		}
		else if (trigger_name.equals("w2_secret_room_bonus"))
		{
			if (m_hero.steal_named_object("w2_bonus_pot"))
			{
				display("w2_extra_life_1",false);
				display("w2_extra_life_2",false);
				display("w2_extra_life_3",false);
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w2_lives_and_health_bonus"))
		{
			display_health_and_lives_bonus(trigger_name, 2);
		}
		else if (trigger_name.equals("w2_lives_bonus"))
		{
			rval = display_lives_bonus(trigger_name, 3);
		}
		else if (trigger_name.equals("w2_attract_clever_bonus"))
		{
			if (m_hero.owns_named_object("w2_attract_monster_2") || m_hero.owns_named_object("w2_attract_monster"))
			{
				print("clever bonus");
				display("w2_attract_clever_bonus");
			}
		}
		else if (trigger_name.equals("w2_enable_attract_clever_bonus"))
		{
			enable_trigger("w2_attract_clever_bonus");
		}
		else if (trigger_name.equals("w2_show_crypt_key"))
		{
			if (owns_3_items())
			{
				display("w2_crypt_key");
			}
			rval = false;
		}
		else if (trigger_name.equals("w2_crypt_bonus"))
		{
			rval = false;
			
			if (owns_3_items())
			{
				move_block("w2_treasure_block_start",false);
				m_hero.steal_named_object("w2_urn");
				m_hero.steal_named_object("w1_skull");
				m_hero.steal_named_object("w1_gold_cross");
			}
			
			if (m_hero.get_score() > 80000)
			{
				
				if (display("w2_crypt_chest"))
				{
					print("score bonus");
					display("w2_crypt_chest_key",false);
				}
				
			}
			
		}
		else if (trigger_name.equals("w2_display_attract_monster"))
		{
			if (zone_contains("w2_trap_room_area", "w2_attract_monster_2"))			
			{
				// unblock the player who used the attract monster potion in the trap room
				display("w2_attract_monster");				
			}
			else
			{
				rval = false;
			}
			
		}
		else if (trigger_name.equals("game_over"))
		{
			level_end();
		}
		
		return rval;
	}

	private boolean owns_3_items()
	{
		return m_hero.owns_named_object("w2_urn") && m_hero.owns_named_object("w1_skull") && m_hero.owns_named_object("w1_gold_cross");
	}
	@Override
	protected void on_world_restart(int world_count) 
	{
		if (world_count == 3)
		{
			game_over();
		}
	}
	
	@Override
	protected void on_level_loaded()
	{
		/*give_to_hero("w1_skull");
		give_to_hero("w1_gold_cross");
		give_to_hero("w2_urn");*/
		
		// demo level: we keep items between levels
		
		set_demo_mode();
	}


}
