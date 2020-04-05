package gods.game.levels.classic;

import gods.base.*;
import gods.game.*;

public class GodsClassicLevel1 extends GodsLevel 
{	
	
	// global level states
	
	private boolean m_short_cut_enabled = false;
	private boolean m_riddle_1_done = false;
	private boolean m_secret_platform_w1_moved = false;
	private boolean m_secret_button_w1_pressed = false;
	private boolean [] m_secret_button_w2_pressed = new boolean[4];
	private boolean m_secret_platform_w1_withdrawn = false;
	private boolean m_disable_giant_jump_w3 = false;
	private boolean m_w3_riddle_1_done = false;
	private boolean m_hidden_passage_open = false;
	private boolean m_w3_platform_arriving_from_right = false;
	private boolean m_w3_platform_arriving_from_left = false;
	private boolean m_boss_dead = false;
	private boolean m_w1_end_secret_trigger = false;
	
	// shop
	
	public static final String [] SHOP_CONTENTS_W3_START = { "chicken", "apple", "weapon_arc_wide",
		"weapon_arc_intense", "weapon_arc_standard", "bread", "knife", "freeze",
		"half_energy", "throwing_star", "invincibility", "increase_wp_medium",
		"full_energy", "starburst"};
	public static final String [] SHOP_CONTENTS_W3_END = { "chicken", "apple", "weapon_arc_wide",
		"weapon_arc_intense", "weapon_arc_standard", "bread", "freeze",
		"half_energy", "throwing_star", "invincibility", "increase_wp_medium",
		"full_energy", "starburst", "fire_ball", "magic_axe", "extra_life"};
			
	// methods & specific code
	
	@Override
	protected void on_level_loaded()
	{				
		for (int i = 0; i < m_secret_button_w2_pressed.length; i++)
		{
			m_secret_button_w2_pressed[i] = false;
		}

		disable_trigger("gmt_20_trigger_back");
		disable_trigger("gmt_21_trigger_back");
		disable_trigger("w3_fmt_back_70");
		
	}

	private class TeleportDisplayTimerEvent extends TimerEvent
	{
		public TeleportDisplayTimerEvent() 
		{
			super(120000); // 120 seconds to solve puzzle from w3
		}
		
		@Override
		protected void on_timeout() 
		{
			if (!m_disable_giant_jump_w3)
			{
				display("w3_start_teleport");
				m_disable_giant_jump_w3 = true;
			}
		}
		
	}
	

	public GodsClassicLevel1() throws java.io.IOException
	{
		super(null);
	}
	
	/*
	private class PlatformTimerEvent extends TimerEvent
	{
		PlatformTimerEvent()
		{
			super(15000);
		}
		
		@Override
		public void on_timeout() 
		{
			m_secret_1.reverse();
			m_secret_1.move();
			
		}
		
	}*/
	
	private boolean withdraw_secret_platform_w1()
	{
		boolean rval = m_secret_button_w1_pressed && !m_secret_platform_w1_withdrawn;
		
		if (rval)
		{
			m_secret_platform_w1_withdrawn = true;
			
			move_block("secret_platform_w1_start",true);

		}
		
		return rval;
	}
	
	@Override
	protected void on_object_drop(GfxObject bonus) 
	{
		
	}
	@Override
	protected boolean on_trigger_activated(String trigger_name) 
	{
		boolean rval = true;
		
		
		if (trigger_name.equals("shortcut_w1"))
		{
			display("shortcut_chrystal");
			display("shortcut_diamond",false);
		}
		else if (trigger_name.equals("secret_platform_w1_remove_trigger") && m_secret_button_w1_pressed)
		{
			move_block("secret_platform_w1_start", true);
		}
		else if (trigger_name.equals("secret_platform_w1_move_2"))
		{
			if (!m_secret_platform_w1_moved)
			{
				// just before world end, there's another chance to
				// get the special bonus
				
				m_w1_end_secret_trigger = true;
			}
		}

		else if (trigger_name.equals("speed_trigger_w1"))
		{
			// 45 seconds to reach speed bonus trigger

			display_speed_bonus("speed_bonus_w1",45);
		}
		else if (trigger_name.equals("w1_extra_life_help_bonus"))
		{
			display_help_bonus(trigger_name,1,12,60);
		}
		else if (trigger_name.equals("health_trigger_w1"))
		{
			display_health_and_lives_bonus("health_and_lives_bonus_w1",1,22);
			
			display_help_bonus("help_health_w1",2,12,160);
			
			display_help_bonus("help_extra_life_w1",2,24,160);
			
		}
		else if (trigger_name.equals("treasure_room_w1_platform_remove"))
		{
			//m_level_data.remove_invisible_block(m_treasure_room_w1_platform);
				
			withdraw_secret_platform_w1();

		}
		else if (trigger_name.equals("secret_platform_w1_remove_trigger"))
		{
			rval = withdraw_secret_platform_w1();			
		}
		// world 2
		
		else if (trigger_name.equals("help_bonus_w2_trigger"))
		{
			display_help_health_bonus("help_health_w2");
		
		}
		else if (trigger_name.equals("w2_world_key_trigger"))
		{
			boolean o1 = m_hero.owns_named_object("pot_for_stores_1");
			boolean o2 = m_hero.owns_named_object("pot_for_stores_2");
			
			rval = false;
			
			if (o1)
			{
				rval = true;
				display("world_key_w2");
				m_hero.steal_named_object("pot_for_stores_1");
			}
			if (o2)
			{
				rval = true;
				display("world_key_w2");
				m_hero.steal_named_object("pot_for_stores_2");
			}
		}
		else if (trigger_name.equals("w2_health_and_lives_bonus"))
		{
			display_health_and_lives_bonus(trigger_name, 1);
		}
		else if (trigger_name.equals("w2_pot_room_special_bonus_trigger"))
		{
			if (m_hero.owns_named_object("pot_for_stores_1") && m_hero.owns_named_object("pot_for_stores_2"))
			{
				display_special_bonus("pot_room_special_bonus");
			}
		}
		else if (trigger_name.equals("special_health_trigger_w2"))
		{
			
			// does not work if flasks are taken instead
			
			boolean o1 = m_hero.owns_named_object("pot_room_pot_1");
			boolean o2 = m_hero.owns_named_object("pot_room_pot_2");
			boolean o3 = !m_hero.owns_named_object("flask_1");
			boolean o4 = !m_hero.owns_named_object("flask_2");
			if (o1 && o2 && o3 && o4)
			{
				display_special_bonus("special_health_bonus_w2");
				m_hero.steal_named_object("pot_room_pot_1");
				m_hero.steal_named_object("pot_room_pot_2");
			}
			else
			{
				rval = false;
			}			
				

		}
		else if (trigger_name.equals("w1_guardian_help"))
		{
			display_help_bonus(trigger_name,1,12);
		}
		else if (trigger_name.equals("w2_last_health_help_bonus"))
		{
			display_help_bonus(trigger_name,1,12,240,35000);
		}
		else if (trigger_name.equals("w2_last_life_help_bonus"))
		{
			display_help_bonus(trigger_name,1,24,240,0);
			
		}
		else if (trigger_name.equals("w2_lives_bonus"))
		{
			if (m_hero.get_nb_lives() >= 2)
			{
				rval = display_lives_bonus("w2_lives_bonus",2);
			}
		}

		else if (trigger_name.equals("gmt_after_teleport_w2_21"))
		{
			rval =  m_hero.owns_named_object("pot_for_stores_2");
			
			if (rval)
			{
				// hero just took pot from teleport room: create monster wave
				
				create_hostile("gm_21");
			}
		}
		else if (trigger_name.equals("health_trigger_w2"))
		{
			if (m_hero.get_score()>40000)
			{
				display_health_and_lives_bonus("health_lives_score_bonus_w2",2);	
			}
		}

		// world 3
		else if (trigger_name.equals("w3_open_traps"))
		{
			m_disable_giant_jump_w3 = true;
			
			open_door("w3_open_trap_1");
			open_door("w3_open_trap_2");
			
			if (m_w3_riddle_1_done)
			{
				display("w3_special_bonus_1");
			}
				
		}
		else if (trigger_name.equals("w3_display_lever_diamonds"))
		{
			// lever must not be reset or diamonds don't appear
			// (funny bonus to counter the players who reset
			// all levers after pulling them in hope for another hidden
			// bonus)
			
			rval =  is_lever_activated("w3_lever_2");
			
			if (rval)
			{
				display("w3_trap_diamond_1");
				display("w3_trap_diamond_2",false);
			}
		}
		else if (trigger_name.equals("w3_lives_bonus_1_trigger"))
		{
			if (m_hero.get_nb_lives() > 2)
			{
				display("w3_lives_bonus_1");
				print("lives bonus");
			}
			create_hostile("gm_45");
		}
		else if (trigger_name.equals("w3_start_clock"))
		{
			// start clock
			
			start_clock();
			
			add_timer_event(new TeleportDisplayTimerEvent());
		}
		else if (trigger_name.equals("w3_score_bonus"))
		{
			if (m_hero.get_score() > 80000)
			{
				display("w3_score_bonus");
				print("score bonus");
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w3_score_bonus"))
		{
			boolean hiscore = (m_hero.get_score() > 80000);

			if (hiscore)
			{
				print("score bonus");
				display("w3_score_bonus");
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w3_speed_bonus_hidden_passage"))
		{
			boolean hiscore = (m_hero.get_score() > 80000);
			
			if (hiscore)
			{

				if ( (get_clock() < 140) && (m_hero.get_nb_lives() > 2))
				{
					print("speed, lives, and score bonus");
					move_block("w3_treasure_block_start",false);
					m_hidden_passage_open = true;
				}
				
			}
		}
		else if (trigger_name.equals("w3_score_and_lives_bonus_top_trigger"))
		{
			if ((m_hero.get_score() > 90000) && (m_hero.get_nb_lives() > 2))
			{
				if (display("w3_score_and_lives_bonus_top"))
				{
					print("score and lives bonus");
				}
				
			}
		}
		else if (trigger_name.equals("move_w3_top_platform_trigger_2"))
		{
			
			m_w3_platform_arriving_from_left = true;
			
			enable_trigger("w3_fmt_back_70");

			if (m_w3_platform_arriving_from_right)
			{
				move_block("w3_top_diamond_platform_start",false);
			}
			else
			{
				move_block("w3_top_moving_platform_start",false);
			}
		}
		else if (trigger_name.equals("w3_help_bonus_1"))
		{
			display_help_lives_bonus("w3_help_bonus_1");
		}		
		else if (trigger_name.equals("w3_help_bonus_2"))
		{
			display_help_health_bonus("w3_help_bonus_2");
		}		
		// above moving platform
		
		else if (trigger_name.equals("move_w3_top_platform_trigger_1"))
		{
			if (!m_w3_platform_arriving_from_left)
			{
				move_block("w3_top_moving_platform_start",false);
			}
			
			m_w3_platform_arriving_from_right = true;
		}
		else if (trigger_name.equals("display_boss_trap_door_key"))
		{
			rval =  m_hero.owns_named_object("world_key_w3");
			
			if (rval)
			{
				display("boss_trap_door_key");
			}
			
			// 90 seconds since restart in the open (to tune)
			
			display_speed_bonus("w3_speed_bonus_2", 90);
		}
		else if (trigger_name.equals("boss_trigger"))
		{
			display_boss();
			close_door("boss_door");
		}
		else if (trigger_name.equals("special_bonus_boss"))
		{
			rval = false;

			if (!m_hero.owns_weapon("lightning_bolt"))
			{
				if ((m_boss_dead) && (display("w3_end_gold_chest")))
				{
					rval = true;

					display_special_bonus("w3_end_chest_key");					
				}
			}
		}
		else if (trigger_name.equals("w3_end_health_bonus"))
		{
			if ((m_hero.get_score() > 120000) && (m_hero.get_nb_lives() > 3))
			{
				if (display("score_and_lives_bonus_w3"))
				{
					print("score and lives bonus");
				}
			}
		}
		return rval;
	}
	
	@Override
	protected void on_boss_death()
	{
		make_visible("level_end_door_lever");
		make_visible("w3_end_shop_token");
		m_boss_dead = true;
	}
	
	@Override
	protected void on_button_pressed(String button_name) 
	{
		if (button_name.equals("secret_button_platform"))
		{
			if (display_special_bonus("riddle_2_bonus"))
			{
				m_secret_button_w1_pressed = true;
			}
		}
		else if (button_name.equals("open_trap_4_secret"))
		{
			m_secret_button_w2_pressed[0] = true;
			
			if (m_hero.owns_named_object("trap_door_key_1_w2") && (m_secret_button_w1_pressed))
			{
				display_special_bonus("giant_jump_w2");
			}
			
		}
		
	}

	@Override
	protected void on_lever_activated(GfxObject lever, LeverActivationState state) 
	{
		String lever_name = lever.get_name();
		
		if (lever_name.equals("spike_kill_lever_1"))
		{
			if (state == LeverActivationState.FIRST_ACTIVATED)
			{
				print("experiment with levers for bonus");	
			}
		} else if (lever_name.equals("spike_kill_lever_2"))
		{
			if (state == LeverActivationState.FIRST_ACTIVATED)
			{
				print("experiment with levers for bonus");
			}
			else if ((state == LeverActivationState.DEACTIVATED) && 
					!is_lever_activated("spike_kill_lever_1"))
			{
				if (!m_short_cut_enabled && m_hero.owns_named_object("treasure_key_w1"))
				{
					m_short_cut_enabled = true;
					print("short cut");
					move_block("shortcut_block_start",false);
				}
			}
		} 
		else if (lever_name.equals("riddle_1_lever_2") && !is_lever_activated("riddle_1_lever_1"))
		{
			kill("spike_3_w1");
			
		}
		
		else if (lever_name.equals("riddle_1_lever_1"))
		{
			if (state == LeverActivationState.FIRST_ACTIVATED)
			{
				// make lever 1 enemy appear
				create_hostile("lever_1_enemy");
			}

			if (is_lever_activated("riddle_1_lever_1") && is_lever_activated("riddle_1_lever_2"))
			{
				if (display("riddle_1_chest"))
				{
					display_special_bonus("riddle_1_chest_key");

					m_riddle_1_done = true;				
				}
				
			}
			
			
		}
		else if (lever_name.equals("riddle_1_lever_3"))
		{
			if (is_lever_activated(state))
			{
				create_hostile("lever_3_enemy");
				
				if ((m_riddle_1_done) && (!is_lever_activated("riddle_1_lever_2")))
				{
					display_special_bonus("riddle_1_bonus");
				}
			}
			
		}
		else if (lever_name.equals("world_door_1_lever"))
		{			
			if (is_lever_activated(state))
			{
				// either 2 minutes to complete full level with secret (no shortcut)
				// or walk almost to the exit and back (if past 2 minutes)
				
				if ((m_riddle_1_done) && ((get_clock() < 120) || m_w1_end_secret_trigger))				
				{
					// secret platform world 1

					move_block("secret_platform_w1_start",false);
					m_secret_platform_w1_moved = true;
					m_riddle_1_done = false; // disable it
				}
			}
		}
		
		// world 2
		
		else if (lever_name.equals("open_trap_w2_underground"))
		{
			if (is_lever_activated(state))
			{
				display_speed_bonus("w2_speed_bonus", 20);
			
				if (m_hero.steal_named_object("trap_door_key_1_w2"))
				{
					open_door("trap_1_w2");
				}
			}
		}
		else if (lever_name.equals("teleport_pot_gem_lever_1"))
		{
			if (state == LeverActivationState.FIRST_ACTIVATED)
			{
				if (is_lever_activated("teleport_pot_gem_lever_2"))
				{
					// open trap to make teleport to extra pot fall
					open_door("trap_teleport_gem_pot");
				}
				else
				{
					// don't open trap, but create spike

					/*Locatable l = */create_hostile("spike_wrong_lever_w2");
					//show_display_animation(l);
				}
			}
		}
		
		// world 3
		
		else if (lever_name.equals("w3_lever_1"))
		{
			// lever must be reset to kill the spike
			
			switch (state)
			{
			case DEACTIVATED:
			{
				destroy("w3_spike_1");
			}
			case FIRST_ACTIVATED:
			{
				create_hostile("w3_ox_1");
			}
			default:
				break;
			}
		}
		
		else if (lever_name.equals("w3_lever_2"))
		{
			if (state == LeverActivationState.FIRST_ACTIVATED)
			{
				if (!m_disable_giant_jump_w3)
				{
					close_door("w3_open_trap_1");
					close_door("w3_open_trap_2");
					destroy("w3_spike_pit");
					m_w3_riddle_1_done = true;				
				}
			}
		}
		else if (lever_name.equals("w3_hidden_passage_lever"))
		{
			if (m_hidden_passage_open)
			{
				open_door("w3_hidden_passage_door");
			}
		}
		else if (lever_name.equals("w3_treasure_room_open_lever"))
		{
			if (is_lever_activated(lever_name))
			{
				if (m_hero.steal_named_object("w3_treasure_key_1"))
				{
					open_door("w3_treasure_room_trap_1");
					open_door("w3_treasure_room_trap_2");
					
					if (!is_lever_activated("w3_trap_3_lever"))
					{
						// riddle done
						display("w3_treasure_shield");
						
					}
				}
			}
		}
		
	}

	protected boolean on_bonus_taken(GfxObject bonus)
	{
		String name = bonus.get_name();
		boolean rval = true;
		
		if (name.equals("w2_knife"))
		{
			enable_trigger("gmt_20_trigger_back");
			enable_trigger("gmt_21_trigger_back");
		}
		else if (name.equals("w3_shop_token"))
		{
			// shop at start of world 3
			
			summon_shopkeeper(SHOP_CONTENTS_W3_START,false);
			rval = false; // display the shopkeeper token message everytime
		}
		else if (name.equals("w3_end_shop_token"))
		{
			// shop at end of world 3
			
			summon_shopkeeper(SHOP_CONTENTS_W3_END,true);

			display("level_end_door_lever");
			
			// remove suppapowerful weapon if taken
			
			m_hero.remove_weapon("lightning_bolt");
			
			rval = false; // display the shopkeeper token message everytime
		}
		else
		{
			// no match for this object
			rval = false;
		}


		return rval;
	}
	@Override
	protected void on_world_restart(int world_count)
	{
		switch (world_count)
		{
		case 2:
			// close world 1 door
			close_door("world_door_1");
			break;
		case 3:
			// close world 1 door
			close_door("world_door_2");
			break;
		}
	}

	@Override
	protected void on_object_picked_up(GfxObject object)
	{
		String object_name = object.get_name();
	
		if (object_name.equals("pot_for_stores_2"))
		{
			display("teleport_back_from_pot_room");
		}
		
	}

	@Override
	protected void on_door_change_state(String door_name, boolean is_open) 
	{
		if (door_name.equals("pot_room_in_door"))
		{
			display_speed_bonus("w2_speed_bonus_pot",140);
		}
		else if (door_name.equals("w3_treasure_room_door"))
		{	
			// display speed bonus
			
			display_speed_bonus("w3_speed_bonus_1", 60);
		}
		else if (door_name.equals("w3_shield_fall_trap"))
		{
			display("w3_shield_fall");
		}
		else if (door_name.equals("w2_treasure_room_trap_4"))
		{
			// open treasure room trap: disable lever (or assoc would close it!)
			disable_lever("w2_treasure_room_trap_4_open_lever");
		}
		else if (door_name.equals("w3_hidden_passage_door"))
		{
			print("w3_hidden_passage");
		}
	}

	@Override
	protected void on_room_entered(String room_door_name) 
	{
		
	}
	


}
