package gods.game.levels.classic;

import java.io.IOException;

import gods.base.ControlObject;
import gods.base.GfxObject;
import gods.base.MovingBlock;
import gods.game.GodsLevel;
import gods.sys.Localizer;

public class GodsClassicLevel4 extends GodsLevel 
{
	private boolean m_w1_health_block_movable = true;
	private boolean m_w1_block_moved_back = false;
	private boolean m_w2_part_2_blocks_moved = false;
	private boolean m_w2_pestle_block_moved = false;
	private boolean m_w2_display_move_trap_key = false;
	private boolean m_w2_forbid_display_move_trap_key = false;
	private boolean m_w2_spike_2_killed = false;
	private boolean m_w2_pestle_timer_added = false;
	private boolean m_w2_release_the_birds_message = false;
	private boolean m_w3_block_moved = false;
	private boolean m_w3_spear_message_printed = false;
	//private boolean m_w3_mace_trap_enabled = false;
	private boolean m_w3_key_block_moved = false;
	private boolean m_w3_enable_go_back = false;
	private boolean m_w3_mace_trap_open = false;
	private int m_nb_lives_taken = 0;
	private boolean m_w3_cave_moving_block_moved = false;
	
	private void display_ten_lives()
	{
		ControlObject lives_zone = m_level_data.get_control_object("w3_ten_lives_zone");
		int w = lives_zone.get_width();
		int x = lives_zone.get_x();
		int y = lives_zone.get_y();
		int nb_lives = 10;
		
		int delta_x = w/nb_lives;
		
		for (int i = 0; i < nb_lives; i++)
		{
			create_bonus(x, y, "extra_life");
			x += delta_x;
		}

	}
	private class GameEndDisplayLivesEvent extends TimerEvent
	{
		GameEndDisplayLivesEvent(int timeout)
		{
			super(timeout * 1000);
		}
		
		@Override
		protected void on_timeout() 
		{
			display_ten_lives();
		}
	}
	
	private class GameEndMessageEvent extends TimerEvent
	{
		private String m_message;
		
		GameEndMessageEvent(int timeout, String message)
		{
			super(timeout * 1000);
			m_message = message;
		}
		
		@Override
		protected void on_timeout() 
		{
			print(m_message);
		}

	}
	
	private class MoveBlockTimerEvent extends TimerEvent
	{

		MoveBlockTimerEvent()
		{
			super(60000);
		}
		@Override
		protected void on_timeout() 
		{
			m_w2_display_move_trap_key = true;
			
		}
		
	}
	public GodsClassicLevel4()
	throws IOException 
	{
		super(GodsClassicLevel3.SHOP_CONTENTS_W3_END);
	}

	@Override
	protected boolean on_bonus_taken(GfxObject bonus) 
	{
		if (bonus.get_class_name().equals("extra_life"))
		{
			m_nb_lives_taken++;
			if (m_nb_lives_taken == 10)
			{
				game_end(false);
			}
		}
		else if (bonus.get_name().equals("w2_shop"))
		{
			// same shop contents as before
			
			summon_shopkeeper(GodsClassicLevel3.SHOP_CONTENTS_W3_END,false);
		}

		return false;
	}

	@Override
	protected void on_boss_death() 
	{
		game_end(true);
		
		int timer = 15;
		m_nb_lives_taken = 0;
		add_timer_event(new GameEndMessageEvent(timer,Localizer.value("WIN_TEXT_1",true)));
		add_timer_event(new GameEndDisplayLivesEvent(timer+18));
	}

	@Override
	protected void on_button_pressed(String button_name) 
	{
		if (button_name.equals("w2_button_1"))
		{
			// I don't know why the bonus appears
			// it does on all my plays, but not on abrasion video
			
			display("w2_special_bonus_1");
			
			if (m_hero.steal_named_object("w2_treasure_key"))
			{
				open_door("w2_treasure_room_door");
			}
		}
		else if (button_name.equals("w3_axe_secret_button"))
		{
			if (m_hero.steal_named_object("w3_trap_door_key"))
			{
				move_block("w3_axe_moving_block_start",false);
			}
		}

	}

	@Override
	protected void on_door_change_state(String door_name, boolean is_open) 
	{
		if (door_name.equals("w1_door_1") && is_open)
		{
			m_w1_health_block_movable = false;
		}
		else if (door_name.equals("w2_pestle_out_door"))
		{
			// disable block move
			m_w2_forbid_display_move_trap_key = true;
		}
		else if (door_name.equals("w3_pit_trap"))
		{
			if (is_open)
			{
				// enable go back to part 2
				m_w3_enable_go_back = true;
			}
			else
			{
				// trap closed back: enable open mace trap
				//m_w3_mace_trap_enabled = true;
			}
		}
		else if (door_name.equals("w3_pit_out_door"))
		{
			// check if hero has treasure key
			if (m_hero.steal_named_object("w3_treasure_key"))
			{
				change_destination_door(door_name,"w3_treasure_room");
			}
		}
		

	}

	@Override
	protected void on_level_loaded() 
	{
		//open_door("w3_boss_door");
		set_tile_behind("fragile_block","stone_22");

	}

	@Override
	protected void on_lever_activated(GfxObject lever,
			LeverActivationState state) 
	{
		String lever_name = lever.get_name();
		
		if (lever_name.equals("w1_open_door_1") && (m_w1_health_block_movable))
		{
			m_w1_health_block_movable = false;
			move_block("w1_moving_block_1_start",false);
		}
		else if (lever_name.equals("w1_close_trap_1"))
		{
			switch (state)
			{
			case FIRST_ACTIVATED:
				create_hostile("w1_gm_4");
				break;
			case DEACTIVATED:
				close_door("w1_trap_1");
				break;
	
			}
		}
		else if (lever_name.equals("w1_move_block_3"))
		{
			if (display("w1_starburst"))
			{
				move_block("w1_moving_block_3_start",false);
				print("something moves above you");
				create_hostile("w1_gm_5");
			}
		}
		else if (lever_name.equals("w1_move_back_world_key_lever"))
		{
			if (state == LeverActivationState.FIRST_ACTIVATED)
			{
				if (!m_w1_block_moved_back)
				{
					m_w1_block_moved_back = true;
					
					MovingBlock p = get_moving_block("w1_world_key_block_start");

					// invert
					
					p.reverse();
					
					p.get_start().set_coordinates(p);

					p.move();
				}
			}
			else
			{
				close_door("w1_trap_room_trap_1");
			}
		}
		else if (lever_name.equals("w1_trap_room_close_trap"))
		{
			if ((state == LeverActivationState.DEACTIVATED) && m_hero.owns_named_object("w1_world_key"))
			{
				if (m_hero.steal_named_object("w1_trap_door_key_1"))
				{
					close_door("w1_trap_room_trap_3");
				}
			}
		}
		else if (lever_name.equals("w1_close_trap_room_trap_4"))
		{
			if (state == LeverActivationState.DEACTIVATED)
			{
				if (m_hero.steal_named_object("w1_trap_door_key_1"))
				{
					close_door("w1_trap_room_trap_6");
				}
			}
		}
		else if (lever_name.equals("w2_move_block_above_spike"))
		{
			if (display("w2_moving_block_health"))
			{
				move_block("w2_moving_block_1a_start",false);
				move_block("w2_moving_block_1b_start",false);
			}
		}
		else if (lever_name.equals("w2_familiar_lever"))
		{
			if (!m_w2_release_the_birds_message && 
					state == LeverActivationState.FIRST_ACTIVATED)
			{
				m_w2_release_the_birds_message = true;
				print("release the birds");
			}
			if (is_lever_activated(lever_name))
			{
				if (m_hero.steal_named_object("w2_oil_lamp"))
				{
					display("w2_familiar");
				}
			}
		}
		else if (lever_name.equals("w2_open_part_2_blocks"))
		{
			if (m_hero.owns_named_object("w2_pestle") && m_hero.owns_named_object("w2_gold_bowl"))
			{
				if (!m_w2_part_2_blocks_moved)
				{
					m_w2_part_2_blocks_moved = true;
					move_block("w2_part_2_block_1_start", false);
					move_block("w2_part_2_block_2_start", false);
				}
			}
		}

		else if (lever_name.equals("w2_create_pestle_thief"))
		{
			switch (state)
			{
			case FIRST_ACTIVATED:
			case ACTIVATED:
				if (m_w2_spike_2_killed && create_hostile("w2_pestle_thief"))
				{
					print("careful, that thief can help");
				}
				break;
			case DEACTIVATED:
				if (m_w2_display_move_trap_key && !m_w2_forbid_display_move_trap_key)
				{
					display("w2_move_block_pestle_trap_door_key");
				}
			break;
			}
		}
		else if (lever_name.equals("w2_open_pestle_out_door"))
		{
			if (is_lever_activated(lever_name))
			{
				if (m_hero.steal_named_object("w2_move_block_pestle_trap_door_key"))
				{
					move_block("w2_pestle_block_start", false);
					m_w2_pestle_block_moved = true;
				}
				else
				{
					if (m_hero.owns_named_object("w2_pestle"))
					{
						open_door("w2_pestle_out_door");
					}
				}
			}
			else
			{
				if (m_w2_pestle_block_moved)
				{
					move_block("w2_pestle_block_2_start", false);
					m_w2_pestle_block_moved = false; // reuse flag to avoid multi-move
				}
			}
		}
		else if (lever_name.equals("w2_kill_spike_2"))
		{
			m_w2_spike_2_killed = true;
			if (!m_w2_pestle_timer_added && state == LeverActivationState.DEACTIVATED)
			{
				m_w2_pestle_timer_added = true;
				add_timer_event(new MoveBlockTimerEvent());
			}
		}
		else if (lever_name.equals("w3_spear_lever"))
		{
			boolean spear_owned = m_hero.owns_weapon("spear");
			boolean activated = is_lever_activated(lever_name);

			if (spear_owned)
			{
				if (activated)
				{
					if (!m_w3_block_moved)
					{
						m_w3_block_moved = true;
						move_block("w3_moving_block_2_start",false);
						//kill("w3_gt_1"); // too soon: no treasure key!
					}
				}
				else
				{
					open_door("w3_part_2_door");	
				}
			}
			else
			{
				if (!m_w3_spear_message_printed)
				{
					m_w3_spear_message_printed = true;
					print("you need the spear to progress");
				}
			}

		}	
		else if (lever_name.equals("w3_riddle_lever_1"))
		{
			switch (state)
			{
			case FIRST_ACTIVATED:
				open_door("w3_pit_trap");
				break;
			case DEACTIVATED:
				if (!m_w3_mace_trap_open)
				{
					close_door("w3_pit_trap");
				}
				break;
			case ACTIVATED:
				if (!m_w3_mace_trap_open && m_hero.owns_weapon("mace"))
				{
					m_w3_mace_trap_open = true;
					open_door("w3_pit_trap");
				}
				break;
			}
		}
		else if (lever_name.equals("w3_riddle_lever_2"))
		{
			if (!m_w3_key_block_moved)
			{
				m_w3_key_block_moved = true;
				move_block("w3_key_fall_start",false);
			}
			if (m_w3_mace_trap_open && state == LeverActivationState.ACTIVATED)
			{
				close_door("w3_pit_trap");
			}
		}
		else if (lever_name.equals("w3_riddle_lever_3"))
		{
			if (is_lever_activated(lever_name))
			{
				GfxObject k = m_level_data.get_bonus("w3_trap_door_key");
				// try to see if key has fallen through the trap
				if ((k != null) && 
						(k.get_y() > lever.get_y() + lever.get_height() + m_hero.get_height()))
				{
					if (!is_door_open("w3_pit_trap"))
					{
						open_door("w3_thief_trap");
					}
				}
			}
		}
		else if (lever_name.equals("w3_open_part_3_out"))
		{
			if ((m_w3_enable_go_back) && is_lever_activated(lever_name))
			{
				open_door("w3_part_3_door_out");
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
		if (room_door_name.equals("w3_cave_location"))
		{
			if (!m_w3_cave_moving_block_moved)
			{
				m_w3_cave_moving_block_moved = true;
				move_block("w3_cave_moving_block_start",false);
			}
		}
		else if (room_door_name.equals("w3_treasure_room"))
		{
			print("treasure room");
		}
		else if (room_door_name.equals("w2_mortar_in_door"))
		{
			display_speed_bonus("w2_speed_bonus", 250);
		}

	}

	private boolean on_trigger_activated_world_1(String trigger_name)
	{
		boolean rval = true;
		
		if (trigger_name.equals("w1_move_spike_platform"))
		{
			display_special_bonus("w1_special_bonus_2");
			move_block("w1_moving_block_2_start",false);
		}
		/*else if (trigger_name.equals("w1_close_door_1"))
		{
			close_door("w1_door_1");
		}*/
		else if (trigger_name.equals("w1_speed_bonus_trigger"))
		{
			display_speed_bonus("w1_speed_bonus", 100);
		}
		else if (trigger_name.equals("w1_move_world_key_block"))
		{
			move_block("w1_world_key_block_start", false);
		}
		else if (trigger_name.equals("w1_open_trap_room_trap"))
		{
			// make the fire chrystal fall
			open_door("w1_trap_room_trap_5");
		}
		else if (trigger_name.equals("w1_remove_bomb"))
		{
			if (m_hero.remove_weapon("time_bomb"))
			{
				print("your bomb has been removed");
				// make up for the lost hunters
				display("w1_trap_hunter_1",false);
				display("w1_trap_hunter_2",false);
			}
		}
		else if (trigger_name.equals("w1_move_block_4"))
		{
			rval = m_hero.steal_named_object("w1_trap_door_key_4");
			
			if (rval)
			{
				move_block("w1_moving_block_4_start", false);
			}
		}
		else if (trigger_name.equals("w1_full_health_special_bonus"))
		{
			if (m_hero.get_health(true) == m_hero.get_max_health())
			{
				display_special_bonus(trigger_name);
			}
		}
		else if (trigger_name.equals("w1_close_trap_3"))
		{
			if (m_hero.owns_weapon("time_bomb"))
			{
				// avoid that the player falls in the pit
				// and does not die (because bomb can kill the trap)
				
				close_door("w1_trap_room_trap_3");
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w2_restart_0") && m_hero.owns_named_object("w2_bonus_room_chest_key"))
		{
			m_hero.steal_named_object("w2_bonus_room_chest_key");
			print("you don't need that key anymore");
		}
		else if (trigger_name.equals("w2_gmt4") && !m_hero.owns_named_object("w2_room_key_1"))
		{
			create_hostile("w2_gm4");
		}
		else if (trigger_name.equals("w1_help_end"))
		{
			rval = display_help_bonus(trigger_name, 0, 20); // extra life help if < 1 lifes & 20 health
		}
		
		else if (trigger_name.equals("w3_l2_help_bonus") || trigger_name.equals("w3_help_bonus_life"))
		{
			rval = display_help_bonus(trigger_name, 1); // extra life help if < 1 lives
		}
		else if (trigger_name.equals("w31_extra_help"))
		{
			rval = display_help_bonus(trigger_name, 2); // extra life help if < 2 lives
		}
		else if (trigger_name.equals("w2_end_help_bonus"))
		{
			rval = display_help_bonus(trigger_name, 2, 11); // extra health help if < 2 lives & 11 energy
		}
		else if (trigger_name.equals("w3_guardian_help"))
		{
			rval = display_help_bonus(trigger_name, 1, 11); // ultimate extra health help if < 1 lives & 11 energy
		}
		
		else if (trigger_name.equals("w1_help_starburst"))
		{
			// original help bonus: starburst
			
			rval = display_help_bonus(trigger_name, 1);  // starburst if < 2 lives
		}
		else
		{
			rval = false;
		}
		return rval;
	}
	
	private boolean on_trigger_activated_world_2(String trigger_name)
	{
		boolean rval = true;
		
		if (trigger_name.equals("w2_life_help_bonus"))
		{
			rval = display_help_lives_bonus(trigger_name);
		}
		else if (trigger_name.equals("w2_disable_gm_4"))
		{
			disable_trigger("w2_gmt_4");
		}
		else if (trigger_name.equals("w2_display_world_key"))
		{
			rval = owns_alchemist_tools();
			
			if (rval)
			{
				m_hero.steal_named_object("w2_pestle");
				m_hero.steal_named_object("w2_lamp");
				m_hero.steal_named_object("w2_gold_bowl");
				display("w2_world_key");
			}
			
		}
		else if (trigger_name.equals("w2_lives_bonus"))
		{
			rval = display_lives_bonus(trigger_name, 3); // confirmed
		}
		else if (trigger_name.equals("w2_display_trap_door_key_end"))
		{
			rval = owns_alchemist_tools();

			if (rval)
			{
				display("w2_trap_door_key_end");
			}
		}
		else if (trigger_name.equals("w2_remove_potion"))
		{
			rval = m_hero.steal_named_object("w2_attract_monster");
			if (rval)
			{
				print("your potion has been removed");
			}
		}
		else if (trigger_name.equals("w2_enable_fmt_15"))
		{
			enable_trigger("w2_fmt_15");
		}
		else
		{
			rval = false;
		}
		
		return rval;
	}
	
	private boolean owns_alchemist_tools()
	{
		return m_hero.owns_named_object("w2_pestle") && m_hero.owns_named_object("w2_lamp") && m_hero.owns_named_object("w2_gold_bowl");
	}
	private boolean on_trigger_activated_world_3(String trigger_name)
	{
		boolean rval = true;
		
		if (trigger_name.equals("w3_speed_bonus"))
		{
			display_speed_bonus(trigger_name, 60);
		}
		else if (trigger_name.equals("w3_close_boss_door"))
		{
			display_boss();
			close_door("w3_boss_door");
			lock_scrolling("boss_fix_scroll_top_left");
		}
		else
		{
			rval = false;
		}
		return rval;
	}

	@Override
	protected boolean on_trigger_activated(String trigger_name) {
		
		boolean rval = on_trigger_activated_world_1(trigger_name);
		
		if (!rval)
		{
			rval = on_trigger_activated_world_2(trigger_name);
		}
		if (!rval)
		{
			rval = on_trigger_activated_world_3(trigger_name);
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
		else if (world_count == 3)
		{
			close_door("w2_world_door");
		}

	}

}
