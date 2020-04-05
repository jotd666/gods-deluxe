package gods.game.levels.jotd;

import java.io.IOException;

import gods.base.ControlObject;
import gods.base.GfxObject;
import gods.game.GodsLevel;
import gods.sys.Localizer;

public class GodsJotdLevel4 extends GodsLevel 
{

	//private boolean m_w3_mace_trap_enabled = false;
	private int m_nb_lives_taken = 0;
	private boolean m_w2_pestle_platform_start_moved = false;
	private boolean m_w2_allow_show_giant_jump = false;
	private boolean m_bird_message_printed = false;
	
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
	
	
	public GodsJotdLevel4()
	throws IOException 
	{
		super(GodsJotdLevel3.SHOP_CONTENTS_W3_END);
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
			
			summon_shopkeeper(GodsJotdLevel3.SHOP_CONTENTS_W3_END,true);
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
		if (button_name.equals("w1_move_platform"))
		{
			// no more special bonus when button pressed
			disable_trigger("w1_special_bonus");
		}
		else if (button_name.equals("w1_display_bomb"))
		{
			display("w1_time_bomb_1");
			display("w1_time_bomb_2",false);
			enable_trigger("w1_display_hunters");
		}
		else if (button_name.equals("w2_pestle_secret"))
		{
			m_w2_allow_show_giant_jump = true;
			display("w2_attract_monster");
			enable_trigger("w2_fmt_14");
		}
		else if (button_name.equals("w3_secret_1"))
		{
			display("w3_treasure_key");
		}
	}

	@Override
	protected void on_door_change_state(String door_name, boolean is_open) 
	{
		if (is_open && door_name.equals("w2_treasure_out"))
		{
			// secret stuff
			move_block("w2_wall_start", false);
		}
		if (door_name.equals("w3_thief_trap_1"))
		{
			open_door("w3_thief_trap_2");
		}
		if (door_name.equals("w3_door_10"))
		{
			// when pulling lever, if has treasure key, teleport to treasure room
			if (m_hero.steal_named_object("w3_treasure_key"))
			{
				display("w3_treasure_teleport");				
			}
			else
			{
				display("w3_cave_teleport");
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
		
		if (lever_name.equals("w1_treasure_out_lever") && state == LeverActivationState.DEACTIVATED)
		{
			display("w1_shield_1");
		}
		else if (lever_name.equals("w2_shortcut_lever"))
		{
			open_door("w2_shortcut_trap");
			if (m_hero.owns_named_object("w2_door_key_1"))
			{
				if  (state == LeverActivationState.FIRST_ACTIVATED)
				{
					// small hint
					print("w2_you_dont_need_door_key_anymore");				
				}
			}
			else
			{
				// gain access to treasure key
				open_door("w2_treasure_key_trap");
			}
				
		
		}
		else if (lever_name.equals("w2_pestle_lever"))
		{
			if (is_lever_activated(lever_name))
			{
				if (m_w2_allow_show_giant_jump)
				{
					display("w2_giant_jump");
				}

				if (get_clock() > 90 && !m_hero.owns_named_object("w2_pestle"))

				{
					// fallback to unblock player
					if (!m_w2_pestle_platform_start_moved)
					{
						print("w2_fallback_solution");
						m_w2_pestle_platform_start_moved = true;
						move_block("w2_pestle_platform_start", false);
					}
				}
			}
		}
		else if (lever_name.equals("w2_exit_pestle_lever"))
		{
			if (m_hero.owns_named_object("w2_pestle"))
			{
				if (open_door("w2_pestle_door"))
				{
					move_block("w2_way_down_start", false);
				}
			}
		}
		else if (lever_name.equals("w2_familiar_lever"))
		{
			if (m_hero.steal_named_object("w2_familiar_lamp"))
			{
				display("w2_familiar");
			}
			else
			{
				if (state == LeverActivationState.FIRST_ACTIVATED && !m_bird_message_printed)
				{
					m_bird_message_printed = true;
					print("w2_release_the_birds");
				}
			}
		}
		else if (lever_name.equals("w3_thief_lever_3"))
		{
			if (is_lever_activated("w3_thief_lever_1"))
			{
				// trap doors are open: we can open the door
				open_door("w3_thief_side_door");
			}
		}
		else if (lever_name.equals("w3_part_2_lever"))
		{
			if (m_hero.owns_weapon("spear"))
			{
				// open door
				open_door("w3_part_2_door");
			}
			else
			{
				if (state == LeverActivationState.FIRST_ACTIVATED)
				{
					print("you need the spear to progress");
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
	protected void on_room_entered(String room_door_name) 
	{
		if (room_door_name.equals("w1_part_2"))
		{
			start_clock(); // for speed bonus
		}
	}
	
	private boolean on_trigger_activated_world_2(String trigger_name)
	{
		boolean rval = true;
		
		if (trigger_name.equals("w2_platform_trigger"))
		{
			// leap of faith block
			move_block("w2_platform_start", false);
		
		}
		else if (trigger_name.equals(("w2_speed_bonus")))
		{
			display_speed_bonus(trigger_name, 30);
		}
		else if (trigger_name.equals("w2_pestle_special_bonus"))
		{
			if (m_hero.owns_named_object("w2_pestle") && !m_w2_pestle_platform_start_moved)
			{
				// could get pestle without fallback solution: bonus
				display_special_bonus(trigger_name);
			}
			else
			{
				rval = false;
			}
				
		}
		else if (trigger_name.equals("w2_remove_attract_potion"))
		{
			if (m_hero.steal_named_object("w2_attract_monster"))
			{
				print("your potion has been removed");
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w2_alchemist_block"))
		{
			if (owns_alchemist_tools())
			{
				move_block("w2_ladder_block_start", false);
			}
			else
			{
				rval = false;
				
			}
		}
		else if (trigger_name.equals("w2_display_world_key"))
		{
			if (owns_alchemist_tools())
			{
				m_hero.steal_named_object("w2_pestle");
				m_hero.steal_named_object("w2_lamp");
				m_hero.steal_named_object("w2_gold_bowl");

				display("w2_world_key");
			}
			else
			{
				rval = false;
			}
		}
		else
		{
			rval = false;
		}
		
		return rval;
	}
	private boolean on_trigger_activated_world_1(String trigger_name)
	{
		boolean rval = true;
		
		if (trigger_name.equals("w1_display_hunters"))
		{
			display("w1_hunter_1",false);
			display("w1_hunter_2",false);
		}
		else if (trigger_name.equals("w1_remove_bomb"))
		{
			if (m_hero.remove_weapon("time_bomb"))
			{
				print("your bomb has been removed");
			}
		}
		if (trigger_name.equals("w1_speed_bonus"))
		{
			display_speed_bonus(trigger_name, 30);
		}
		else if (trigger_name.equals("w1_lives_bonus"))
		{
			rval = display_lives_bonus(trigger_name, 3);
		}
		else if (trigger_name.equals("w1_help_bonus"))
		{
			rval = display_help_health_bonus(trigger_name);
		}
		else if (trigger_name.equals("w1_speed_bonus_2"))
		{
			if (get_clock() < 120)
			{
				print("speed bonus");
				move_block("w1_speed_bonus_1_start",false);
				move_block("w1_speed_bonus_2_start",false);
			}
			else
			{
				debug("missed speed bonus "+get_clock());
			}
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
		else if (trigger_name.equals("w3_move_block_above"))
		{
			move_block("w3_ladder_block_start", false);
			if (m_hero.owns_weapon("mace"))
			{
				display_special_bonus("w3_mace_special_bonus");
			}
				move_block("w3_block_axes_start", false);
			
		}
		else if (trigger_name.equals("w3_lives_bonus"))
		{
			rval = display_lives_bonus(trigger_name, 3);
		}
		else if (trigger_name.equals("w3_move_secret_block"))
		{
			// avoid getting an increase power if already has axe by breaking the blocks above
			if (m_hero.owns_named_object("w3_world_key") && !m_hero.owns_weapon("axe"))
			{
				move_block("w3_secret_block_start", false);
			}
			else
			{
				rval = false;
			}
		}
		else if (trigger_name.equals("w3_close_door_10") && m_hero.owns_named_object("w3_world_key"))
		{
			// close door only if have key: avoids getting stuck if key missing
			close_door("w3_door_10");
		}
		else if (trigger_name.equals("w3_remove_attract"))
		{
			rval = m_hero.steal_named_object("w3_attract_monster_1");
			if (rval)
			{
				 print("your potion has been removed");
			}
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
