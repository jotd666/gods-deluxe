package gods.screens;
import java.awt.*;

import gods.game.*;
import gods.base.GameOptions;
import gods.base.LevelSet;
import gods.sys.*;

public class GodsOptionsMenuScreen1 extends GameState 
{
	private int m_cursor_pos = 0;
	private final static int Y_STEP = 66;	
	private final static int Y_START = 32+Y_STEP;
	private static final int ARROW_OFFSET = 60;

	private String [] MENU_OPTIONS = new String[5];
	private StatusBar m_status_bar;
	private GameState m_next_screen = null;

	private static final int OPT_SET = 0;
	private static final int OPT_LEVEL = 1;
	private static final int OPT_DIFFICULTY = 2;
	private static final int OPT_SOUND = 3;
			
	private GameOptions m_options = GameOptions.instance();
	
	
	public GodsOptionsMenuScreen1()
	{
		set_fadeinout_time(1000,1000,0);


		update_difficulty_option();
		update_level_option();
		update_sound_option();
		update_more_options();
	}

	 private void update_level_option()
	 {
		 LevelSet ls = m_options.get_current_level_set();
		 
		 MENU_OPTIONS[OPT_SET] = Localizer.value("SET",true)+": "+ Localizer.value(ls.get_name().toUpperCase());
		 MENU_OPTIONS[OPT_LEVEL] = Localizer.value("LEVEL",true)+": "+ ls.get_start_level();
	 }
	 private void update_difficulty_option()
	 {
		 MENU_OPTIONS[OPT_DIFFICULTY] = Localizer.value("MODE",true)+": "+
				 Localizer.value(m_options.get_start_difficulty_level().toString().replace('_', ' '),true);
		 
	 }
	 private void update_sound_option()
	 {
		 MENU_OPTIONS[OPT_SOUND] = Localizer.value("SFX",true)+": "+
		 Localizer.value(m_options.get_audio_mode().toString().replace('_', ' '),true);
		 
		 
		 switch (m_options.get_audio_mode())
		 {
		 case FULL_SOUND:
		 case MUSIC_ONLY:
			 if (!Mp3Play.is_playing())
			 {
				 Mp3Play.replay();
			 }
			 break;
		 default:
			 Mp3Play.stop();
		 }
	 }
	 

	 private void update_more_options()
	 {
		 MENU_OPTIONS[4] = Localizer.value("MORE OPTIONS",true) + " )";
	 }

	 protected GameState get_next_default_screen()
	 {
		 return new GodsMainMenuScreen();
	 }
	 

	 protected void p_update()
	 {
		 if (is_a_button_pressed())
		 {
			 fadeout();
		 }
		 
		 m_controls.read();
		 
		 if (m_controls.key_down)
		 {
			 
			 m_cursor_pos++;
			 if (m_cursor_pos == MENU_OPTIONS.length)
			 {
				 m_cursor_pos = 0;
			 }
			 if (m_cursor_pos == 1 && is_mono_level())
			 {
				 m_cursor_pos++;
			 }
			 
		 }
	
	 
		 
		 if (m_controls.key_up)
		 {

			 if (m_cursor_pos == 0)
			 {
				 m_cursor_pos = MENU_OPTIONS.length;
			 }
			 m_cursor_pos--;
			 if (m_cursor_pos == 1 && is_mono_level())
			 {
				 m_cursor_pos--;
			 }
		 }
		
		 
		 if (is_fadeout_done())
		 {
			 GameOptions.instance().save();

			 if (m_next_screen != null)
			 {
				 set_next(m_next_screen);
			 }
			 else
			 {
				 set_next(get_next_default_screen());
			 }
		 }
		 
		 
		 switch (m_cursor_pos)
		 {
		 case OPT_SET:
			 if (m_controls.key_left)
			 {
				 m_options.previous_level_set();
				 update_level_option();
			 }
			 else if (m_controls.key_right)
			 {
				 m_options.next_level_set();				

				 update_level_option();
			 }
			 break;
		 case OPT_LEVEL:
			 if (m_controls.key_left)
			 {
				 m_options.prev_level_in_current_level_set();
				 update_level_option();
			 }
			 else if (m_controls.key_right)
			 {
				 m_options.next_level_in_current_level_set();
				 update_level_option();
			 }
			 break;
		 case OPT_SOUND:			 
			 if (m_controls.key_left)
			 {
				 m_options.prev_audio_mode();
				 update_sound_option();
			 }
			 else if (m_controls.key_right)
			 {
				 m_options.next_audio_mode();
				 update_sound_option();
			 }
			 break;

		 
		 case OPT_DIFFICULTY:
			 if (m_controls.key_left)
			 {
				 m_options.prev_difficulty_level();
				 update_difficulty_option();
			 }
			 else if (m_controls.key_right)
			 {
				 m_options.next_difficulty_level();
				 update_difficulty_option();
			 }
			 
			 break;
		 
		 case 4:
			 if (m_controls.key_left || m_controls.key_right)
			 {
			 m_next_screen = new GodsOptionsMenuScreen2();
			 fadeout();
			 }
	 }
		 m_status_bar.update(get_elapsed_time());
		 
		
		 
	 }
	
	 
	 
	 protected void p_init()
	 {
		 m_status_bar = StatusBar.instance(getWidth(),getHeight());
	 }
	 
	 protected void p_render(Graphics2D g)
	 {
		 int h = getHeight();
		 int y_limit = h-m_status_bar.get_height()-4;
		 
		 g.drawImage(m_helmet, 0, 0, getWidth(), y_limit, 
				 0,0, getWidth(), y_limit,null);
		 
		 g.setColor(Color.BLACK);
		 g.fillRect(0, y_limit, getWidth(), h);
		 
		 for (int i = 0; i < MENU_OPTIONS.length; i++)
		 {
			 if (i!=1 || !is_mono_level())
			 {
			 GOLDEN_BIG_FONT.write_line(g,Localizer.value(MENU_OPTIONS[i]),getWidth()/2,
					 Y_START + Y_STEP * i,-2,true,true);
			 }
		 }
		 int ypos = Y_START + Y_STEP * m_cursor_pos;
		 
		 GOLDEN_BIG_FONT.write_line(g,")",ARROW_OFFSET,ypos,0,true,true);
		 GOLDEN_BIG_FONT.write_line(g,"(",getWidth()-ARROW_OFFSET,ypos,0,true,true);
		 
		 m_status_bar.render(g);
	 }
	 
	 private boolean is_mono_level()
	 {
		 return m_options.get_current_level_set().get_nb_levels() == 1;
	 
	 }
}
