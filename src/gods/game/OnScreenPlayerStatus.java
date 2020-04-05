package gods.game;

// on screen display
// sorry the WorldEndDisplay state machine is a bit confusing, and the "demo mode" (where lives & health don't count)
// certainly does not help (but it works!)

import java.awt.*;
import java.io.IOException;
import java.awt.image.BufferedImage;

import joystick.Joystick;
import gods.base.*;
import gods.sys.*;
import gods.game.characters.*;
import gods.game.items.StaminaDisplay;

public class OnScreenPlayerStatus implements Renderable
{
	private GfxFrameSet m_item_holder;
	
	private StaminaDisplay m_hero_stamina;
	private StaminaDisplay m_boss_stamina;
	private GameOptions m_game_options = GameOptions.instance();
	private Hero m_hero;
	private int TIME_BEFORE_BONUS_DISPLAY = 5000;
	private static final int HEALTH_X = 34;
	private static final int HEALTH_Y = 322;
	private static final int ITEMS_Y = 318;
	private static final int SCORE_X = 448;
	private static final int SCORE_Y = 336;
	
	private int m_global_timer = 0;
	private int m_width;
	private GameFont m_lives_and_score_font;
	private BufferedImage m_lives_icon;
	private BufferedImage m_gold_bag_icon;
	private BufferedImage m_score_image;
	private SfxSet m_sfx_set;
	
	private boolean m_world_end = false;
	private GameFont m_big_font;
	private WorldEndDisplay m_world_end_osd;
	private Hostile m_boss = null;
	private int m_level_index = 1;
	private boolean m_demo_mode = false;
	
	private static final String [] ONETWOTHREEFOUR = {"ONE", "TWO", "THREE", "FOUR"};
	
	public void init_world_end()
	{
		m_world_end = true;
		m_world_end_osd.init(EndType.WORLD_END);			
	}
	public void init_game_end()
	{
		m_world_end = true;
		m_world_end_osd.init(EndType.GAME_END);
	}
	public void init_demo_end()
	{
		m_world_end = true;
		m_world_end_osd.init(EndType.DEMO_END);
	}

	public void init_level_end(int level_index)
	{
		m_world_end = true;
		m_level_index = level_index;
		m_world_end_osd.init(EndType.LEVEL_END);
		//m_hero.init_level_start(level_index); now done in hero level init
	}
	public void set_demo_mode()
	{
		TIME_BEFORE_BONUS_DISPLAY = 1000;
		m_demo_mode = true;
	}

	public boolean is_world_end()
	{
		return m_world_end;
	}

	
	// constructor
	
	public OnScreenPlayerStatus(GfxPalette palette, GfxFrame lives_icon, 
			GfxFrame gold_bag_icon, Hero hero, Hostile boss, SfxSet sfx_set, int width)
	{
		
		m_hero_stamina = new StaminaDisplay(palette,hero);
		m_sfx_set = sfx_set;
		
		if (boss != null)
		{
			m_boss_stamina = new StaminaDisplay(palette,boss);
		}
		
		m_item_holder = palette.lookup_frame_set("item_holder");
		
		m_boss = boss;
		
		GfxFrame score_frame = palette.lookup_frame_set("score_frame").get_first_frame();
		m_score_image = new BufferedImage(score_frame.get_width(),
				score_frame.get_height(),BufferedImage.TYPE_INT_RGB);
		Graphics g = m_score_image.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0,0,score_frame.get_width(),score_frame.get_height());
		g.drawImage(score_frame.toImage(),0,0,null);
		m_lives_icon = lives_icon.toImage();
		m_gold_bag_icon = gold_bag_icon.toImage();
		m_hero = hero;
	
		
		m_width = width;
		
		m_world_end_osd = new WorldEndDisplay();
		
		try 
		{
			m_lives_and_score_font = new GameFont(DirectoryBase.get_font_path() + "small_letters", GameOptions.instance().get_font_gfx_flavor(), true);
			m_big_font = new GameFont(DirectoryBase.get_font_path() + "big_letters", GameOptions.instance().get_font_gfx_flavor() );
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void update(long elapsed_time)
	{
		m_global_timer += elapsed_time;
		
		m_hero_stamina.update(elapsed_time);
		
		if ((m_boss != null) && (m_boss.get_life_state() == LivingCharacter.LifeState.ALIVE))
		{
			m_boss_stamina.update(elapsed_time);
		}
		
		if (m_world_end)
		{
			m_world_end_osd.update(elapsed_time);
		}
	}
	

	private void render_score(Graphics2D g)
	{
		String score_str = MiscUtils.zero_pad(m_hero.get_score(),8);
		
		g.drawImage(m_score_image,SCORE_X,SCORE_Y,null);
		m_lives_and_score_font.write_line(g, score_str, SCORE_X + m_score_image.getWidth()/2, 
				SCORE_Y + m_score_image.getHeight()/2, 0, true, true);
		
	}

	public void render(Graphics2D g)
	{
		m_hero_stamina.set_coordinates(HEALTH_X,HEALTH_Y);
		m_hero_stamina.render(g);
	
		if ((m_boss != null) && (m_boss.get_life_state() == LivingCharacter.LifeState.ALIVE))
		{
			m_boss_stamina.set_coordinates(m_width - HEALTH_X - m_boss_stamina.get_width(),HEALTH_Y);
			m_boss_stamina.render(g);
		}
		else
		{
			render_score(g);
		}
		
		g.drawImage(m_lives_icon, 0, 0, null);
		
		m_lives_and_score_font.write_line(g, "X"+Math.max(0,m_hero.get_nb_lives()),
				m_lives_icon.getWidth(),6,0,false,false);
		
		// clock / timer
		
		if (DebugOptions.debug)
		{
			int seconds = (m_global_timer / 1000) % 60;
			String time_skill = "D:"+m_hero.get_difficulty_level()+" T:" + (m_global_timer / 60000) + ":" + ((seconds < 10) ? "0"+seconds : seconds);

			m_lives_and_score_font.write_line(g, time_skill, m_width-180, 0, 0, false, false);
			
		}
		
		m_hero.render_item_holder(g, m_width, ITEMS_Y);
		
		if (m_world_end)
		{
			m_world_end_osd.render(g);
		}
	}
	
	public enum EndType { LEVEL_END, WORLD_END, GAME_END, DEMO_END }
	
	public class WorldEndDisplay implements Renderable 
	{
		private int m_state_counter;
		//private boolean m_previous_fire_pressed;
		private int m_transfer_unit;
		private int m_world_end_timer;
		private int m_world_count = 0;
		private EndType m_level_end;
		private int m_bonus_value;
		private boolean m_score_countdown;
		private int m_previous_line_state;
		private String m_end_message;
		
		public void init(EndType end_type)
		{
			//m_previous_fire_pressed = false;
			m_level_end = end_type;
			m_transfer_unit = 100;
			
			if (end_type != EndType.WORLD_END)
			{
				m_world_count = 0;
			}
			else
			{
				m_world_count++;
			}
			
			m_state_counter = 0;
			m_bonus_value = 0;
			m_world_end_timer = Integer.MAX_VALUE;
			m_previous_line_state = 0;
			m_score_countdown = false;
			
			switch (m_level_end)
			{
			case WORLD_END:
				m_end_message = Localizer.value("WORLD",true)+" "+Localizer.value(ONETWOTHREEFOUR[m_world_count-1],true);
				break;
			case LEVEL_END:
				m_end_message = Localizer.value("LEVEL",true)+" "+Localizer.value(ONETWOTHREEFOUR[m_level_index - 1],true);
				break;
			case GAME_END:
				m_end_message = Localizer.value("GAME",true);
				break;
			case DEMO_END:
				m_end_message = Localizer.value("DEMO",true);
				break;
			}
		}
		


		public void update(long elapsed_time) 
		{
			m_state_counter += elapsed_time;
			
			
			
			if (m_state_counter > TIME_BEFORE_BONUS_DISPLAY + 4000)
			{		
				if (m_hero.is_fire_pressed())
				{
					m_transfer_unit = 1000000;
				}
				int transfer_unit = m_transfer_unit;
				
				if (m_bonus_value > 0)
				{
					if (!m_score_countdown)
					{
						m_score_countdown = true;
						m_sfx_set.play(SfxSet.Loop.score_countdown);
					}
					
					if (m_bonus_value > 0)
					{
						while (m_bonus_value < transfer_unit)
						{
							transfer_unit /= 10;
						}

						m_bonus_value -= transfer_unit;
						m_hero.add_score(transfer_unit,false,true);
					}
					
					if (m_bonus_value <= 0)
					{
						m_hero.add_score(m_bonus_value,false,true);
						m_bonus_value = 0;
						m_world_end_timer = m_state_counter + 1000;
						
						m_sfx_set.pause(SfxSet.Loop.score_countdown);
					}
				}
				else if ((m_bonus_value == 0) && m_demo_mode)
				{
					m_world_end_timer = 0;
					m_sfx_set.pause(SfxSet.Loop.score_countdown);
				}
				
				if (m_world_end_timer < m_state_counter)
				{
					// stop displaying bonus stuff
					
					m_world_end = false;
				}
			}			
			else
			{
				int line_state = (m_state_counter - TIME_BEFORE_BONUS_DISPLAY) / 1000;

				int line_state_start = 0;
				
				if ((line_state >= line_state_start) && (line_state < 4))
				{
					if (m_previous_line_state != line_state)
					{
						m_previous_line_state = line_state;
												
						// demo mode: don't play 3 times the sound
						if (!m_demo_mode || line_state == 3)
						{
							m_sfx_set.play(SfxSet.Sample.bonus_display);
						}
					}
					m_bonus_value = 0;

					int mult = 1;
					int value = 0;

					for (int i = 0; i < line_state; i++)
					{
						switch (i)
						{
						case 0:
							mult = m_hero.get_nb_lives();
							value = m_demo_mode ? 0 : 2000; // points per life. demo mode: no points
							break;
						case 1:
							mult = m_hero.get_health(false);
							value = m_demo_mode ? 0 : 80; // points per health. demo mode: no points
							break;
						case 2:
							mult = 1;
							value = m_hero.get_bonus_score();
							break;
						}


						m_bonus_value += mult * value;						
					}
				}
			}

		}

		public void render(Graphics2D g)
		{
			int x = m_width/2;
			int x1 = 98;
			int lw = m_big_font.get_letter_width()-2;
			
			m_big_font.write_line(g,m_end_message,x,36, -2, true, false);
			
			m_big_font.write_line(g,Localizer.value("COMPLETED",true),x,76, -2, true, false);
			m_big_font.write_line(g,Localizer.value("BONUS",true),x1,274, -2, false, false);

			int x3 = 500;
			
			
			if (m_state_counter > TIME_BEFORE_BONUS_DISPLAY)
			{
				int x2 = 220;
				int line_state = (m_state_counter - TIME_BEFORE_BONUS_DISPLAY) / 1000;
				int nb_lines = Math.min(line_state,3);

				int ihh = m_item_holder.get_height();
				int ihw = ihh;

				int mult = 1;
				int value = 0;
				int line_state_start = m_demo_mode ? 2 : 0;
				
				for (int i = line_state_start; i < nb_lines; i++)
				{
					int y2 = 118 + 50 * i;
					g.drawImage(m_item_holder.toImage(),x1,118 + 50 * i,null);
					BufferedImage bi = null;
					int biw_offset=0;
					switch (i)
					{
					case 0:
						bi = m_lives_icon;
						mult = m_hero.get_nb_lives();
						value = 2000; // points per life
						break;
					case 1:
						//bi = m_stamina.toImage();
						//biw_offset = bi.getWidth() - bi.getHeight(); // square it
						
						m_hero_stamina.set_coordinates(x1,y2);
						m_hero_stamina.render(g);
						mult = m_hero.get_health(false);
						value = 80; // points per health
						break;
					case 2:
						bi = m_gold_bag_icon;
						mult = 1;
						value = m_hero.get_bonus_score();
						break;
					}
					if (bi != null)
					{
						g.drawImage(bi,x1 + (ihw - bi.getWidth() + biw_offset)/2,y2 + (ihh - bi.getHeight())/2,null);
					}
					String mult_str = mult+"";
					String value_str = value+"";
					int x2_offset = (mult_str.length() - 1) * lw;
					int x3_offset = (value_str.length() - 1) * lw;
					
					m_big_font.write_line(g, mult_str, x2 - x2_offset, y2+10, -2, false, false);
					m_big_font.write_line(g, value_str, x3 - x3_offset, y2+10, -2, false, false);
					m_big_font.write_line(g, "X", 306, y2+10, -2, false, false);
					
				}
			}


			String bonus_string = MiscUtils.zero_pad(m_bonus_value,6);

			m_big_font.write_line(g,bonus_string,x3 - lw*5,274, -2, false, false);


		}
	}

	
}
