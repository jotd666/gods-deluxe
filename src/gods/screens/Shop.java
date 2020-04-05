package gods.screens;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import gods.game.GameState;
import gods.game.SfxSet;
import gods.game.StatusBar;
import gods.game.characters.Hero;
import gods.game.characters.HeroWeaponSet;
import gods.game.items.StaminaDisplay;
import gods.sys.Localizer;
import gods.sys.MiscUtils;
import gods.base.*;
import java.util.*;

public class Shop extends GameState 
{
	private GfxFrame m_background;
	private GfxFrameSet m_shop_item_background;
	private StaminaDisplay m_stamina;
	private Hero m_hero;
	private int m_buy_index = 0;
	private HeroWeaponSet.Slot m_refund = null;
	
	private static final int NB_COLS = 6;
	private static final int NB_ROWS = 3;
	private static final int NB_SLOTS = NB_COLS * NB_ROWS;
	
	private boolean m_previous_fire_state = false;
	private int m_current_index = 0;
	private int m_info_index = -1;
	private StatusBar m_status_bar;
	private GameState m_next_screen;
	private Vector<GfxFrame> m_all_sold_items = new Vector<GfxFrame>();
	private Vector<GfxFrame> m_sold_items = new Vector<GfxFrame>();
	private int m_object_y_resolution;
	private GfxFrame [] m_weapon_arc = new GfxFrame[3];
	
	public Shop(String [] all_items, Hero hero,
			int object_y_resolution,
			GfxPalette common_palette, 
			GfxPalette level_palette, 
			StatusBar status_bar,
			GameState next_screen)
	{
		set_fadeinout_time(500, 500, 0);
		
		m_object_y_resolution = object_y_resolution;
		
		m_background = common_palette.lookup_frame_set("shop").get_first_frame();
		m_status_bar = status_bar;
		m_next_screen = next_screen;
		
		m_hero = hero;
		
		for (String s : all_items)
		{
			GfxFrame gf = level_palette.lookup_frame_set(s).get_first_frame();
			m_all_sold_items.add(gf);
		}
		
		m_weapon_arc[HeroWeaponSet.Arc.wide.ordinal()] = level_palette.lookup_frame_set("weapon_arc_wide").get_first_frame();
		m_weapon_arc[HeroWeaponSet.Arc.standard.ordinal()] = level_palette.lookup_frame_set("weapon_arc_standard").get_first_frame();
		m_weapon_arc[HeroWeaponSet.Arc.intense.ordinal()] = level_palette.lookup_frame_set("weapon_arc_intense").get_first_frame();
		
		m_shop_item_background = common_palette.lookup_frame_set("shop_item_background");
		
		m_stamina = new StaminaDisplay(common_palette,m_hero);
		
		m_stamina.set_coordinates(32,286);
	}
	
	@Override
	protected void p_init() 
	{
		play_music("misc" + File.separator + "shop.mp3");
	}
	
	private void update_sellable_list()
	{
		int previous_sold_items = m_sold_items.size();
		
		m_sold_items.clear();
		
		Iterator<GfxFrame> it = m_all_sold_items.iterator();
		
		while (it.hasNext())
		{
			GfxFrame item_found = null;
			GfxFrame gf = it.next();

			// how much money left if item is bought

			int hero_money = m_hero.get_money() - gf.get_source_set().get_properties().value;

			// how much money refund in case of a weapon purchase
			// (half of the money only is refund)
			
			HeroWeaponSet.Slot refund = m_hero.get_weapon_set().refund_on_weapon_take(gf.get_source_set().get_name());
			
			if (refund != null)
			{
				hero_money += refund.get_refund_price();
			}

			// display item only if hero money would be positive

			if (hero_money >= 0)
			{
				// don't show any "increase weapon power" tokens if hero has
				// no weapons

				if (gf.get_source_set().get_name().startsWith("increase_wp"))
				{
					if (m_hero.can_shoot())
					{
						item_found = gf;
					}
				}
				// show only familiar options if familiar exists
				else if (gf.get_source_set().get_name().startsWith("familiar_"))
				{
					if (m_hero.owns_familiar())
					{
						item_found = gf;
					}
				}
				else if (gf.get_source_set().get_name().equals("familiar"))
				{
					// only proposes familiar if not already there
					if (!m_hero.owns_familiar())
					{
						item_found = gf;
					}
				}
				else
				{
					item_found = gf;
				}
			}
	
			if (item_found != null)
			{
				m_sold_items.add(item_found);
			}			
		}

		// remove cheapest items until all expensive items are displayed
		
		while (m_sold_items.size() >= NB_SLOTS)
		{
			m_sold_items.removeElementAt(0);
		}
		if (m_sold_items.isEmpty() && previous_sold_items != 0)
		{
			// locate the selection on the exit
			m_current_index = NB_SLOTS - 1;
		}
	}

	@Override
	protected void p_render(Graphics2D g) 
	{		
		// draw the background
		
		g.drawImage(m_background.toImage(),0,0,null);
		
		// draw the items
		
		int count = 0; 
		
		m_stamina.render(g);
				
		//int current_row = m_current_index / NB_COLS;
		
		for (int j = 0; j < NB_ROWS; j++)
		{
			int y = j * 64 + 104;
			for (int i = 0; i < NB_COLS; i++)
			{
				int x = i * 64 + 196;

				int frame_index = 1;

				count++;
				if (count == NB_SLOTS)
				{
					frame_index = 3;
				}
				
				if (count == m_current_index + 1)
				{
					frame_index++;
				}
				
				GfxFrame gf_bg = m_shop_item_background.get_frame(frame_index);
				
				g.drawImage(gf_bg.toImage(),x,y,null);
				
				// lookup next displayable item
				
				if (count <= m_sold_items.size())
				{
					GfxFrame gf = m_sold_items.elementAt(count-1);
					
					BufferedImage bi = gf.toImage();

					int x_offset = (gf_bg.get_width() - bi.getWidth()) / 2;
					int y_offset = gf_bg.get_height() - bi.getHeight();

					g.drawImage(bi,x + x_offset,y + y_offset,null);
				}
			}
		}
		// money
		
		String m = MiscUtils.zero_pad(m_hero.get_money(),7);
		
		GOLDEN_SMALL_FONT.write_line(g, m, 32, 348, -2, false, false);
		
		// status bar
		
		m_status_bar.render(g);
		
		// currently owned weapons & arc configuration
		int weapon_x = 4;
		HeroWeaponSet hws = m_hero.get_weapon_set();
		BufferedImage arci = m_weapon_arc[hws.get_arc().ordinal()].toImage();
		g.drawImage(arci,weapon_x,4,null);
		weapon_x += arci.getWidth()+2;
	}

	@Override
	protected void p_update() 
	{
		if (is_fadeout_done())
		{
			m_hero.shop_exited();
			
			// so level music can play again
			stop_music();
			
			set_next(m_next_screen);
		}
		
		long elapsed_time = get_elapsed_time();
		
		m_stamina.update(elapsed_time);
		
		m_status_bar.update(elapsed_time);
		
		m_hero.handle_money(elapsed_time);

		update_sellable_list();

		int old_index = m_current_index;

		m_controls.read();

		boolean fire_pressed = m_controls.fire_pressed;

		if (fire_pressed)
		{
			if (m_previous_fire_state)
			{
				fire_pressed = false;
			}

			m_previous_fire_state = true;
		}
		else
		{
			 m_previous_fire_state = false;
		 }

		 
		if (m_controls.key_left)
		{
			m_current_index--;

		}
		else if (m_controls.key_right)
		{
			m_current_index++;
		}
		else if (m_controls.key_down)
		{
			m_current_index += NB_COLS;
		}
		else if (m_controls.key_up)
		{
			m_current_index -= NB_COLS;
		}
		else if (fire_pressed)
		{
			if (m_current_index == NB_SLOTS - 1)
			{
				fadeout();
			}
			else if (m_current_index < m_sold_items.size())
			{
				GfxFrame gf = m_sold_items.elementAt(m_current_index);
				GfxFrameSet.Properties props = gf.get_source_set().get_properties();
				
				if (m_current_index == m_info_index)
				{
					boolean can_buy = (m_refund != null);
					String item_name = gf.get_source_set().get_name();
					
					// info already displayed on this item
					
					if (!can_buy)
					{
						m_refund = m_hero.get_weapon_set().refund_on_weapon_take(item_name);
						
						if (m_refund != null)
						{
							String message = Localizer.value("this weapon replaces the %WEAPON%",true);
							String weapon_name = m_refund.type.toString().replace("_", " ");
							
							m_status_bar.print("# "+Localizer.value("warning",true)+" # "+
									message.replace("%WEAPON%",Localizer.value(weapon_name,true)));
						}
						else
						{
							can_buy = true;
						}
					}
					
					if (can_buy)
					{
						// buy it

						int price = props.value;
						
						if (m_refund != null)
						{
							price -= m_refund.get_refund_price();
						}
						
						if (m_hero.sub_money(price))
						{
							GfxObject ob = new GfxObject(0,0,1,m_object_y_resolution,"shop_bought_"+m_buy_index,
									gf.get_source_set());

							m_hero.on_take_object(ob, true);
							m_buy_index++;

							if (!m_hero.get_weapon_set().is_weapon(item_name))
							{
								// remove from global list
								m_all_sold_items.remove(gf);
								m_info_index = -1; // so info is displayed next time
							}
							m_refund = null;
						}
					}
				}
				else
				{
					m_info_index = m_current_index;
					m_refund = null;

					m_status_bar.print(props.toString());
				}
			}
		}
		
		m_current_index = (m_current_index + NB_SLOTS) % NB_SLOTS;
		
		if (m_current_index != old_index)
		{
			m_info_index = -1;
			m_hero.get_sfx_set().play(SfxSet.Sample.shop_move);
		}
		
	}


}
