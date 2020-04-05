package gods.screens;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import gods.base.GameOptions;
import gods.base.GfxPalette;
import gods.base.LevelSet;
import gods.game.GameState;
import gods.game.GodsLevel;
import gods.game.StatusBar;
import gods.game.characters.Hero;
import gods.sys.*;


public class GodsEndScreen extends GameState {

	private BufferedImage m_background;
	private boolean m_first_message_printed = false;
	private boolean m_second_message_printed = false;
	private int m_second_message_print_timer = 0;
	private StatusBar m_status_bar;
	private Hero m_hero;
	
	public GodsEndScreen(GfxPalette common_palette, Hero hero)
	{
		set_fadeinout_time(250, 250,0);
		m_background = common_palette.lookup_frame_set("winner").get_first_frame().toImage();
		m_hero = hero;
	}
	
	 protected GameState get_next_default_screen()
	 {
		 GameState gs=null;
		 //return new GodsMainMenuScreen();
		 try {
			 // reinit hero
			 //m_hero.init_after_game_completed();
			gs = GodsLevel.create(m_hero);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return gs;
	 }
	 
	 protected void p_update()
	 { 
		 if (is_a_button_pressed())
		 {
			 fadeout();
		 }
		 if (is_fadeout_done())
		 {
			 Mp3Play.stop();
			 
			 set_next(get_next_default_screen());
		 }
		 
		 if (!m_first_message_printed)
		 {
			 m_status_bar.print("WIN_TEXT_2");
			 m_first_message_printed = true;
		 }
		 else
		 {
			 m_second_message_print_timer += get_elapsed_time();
			 if ((m_second_message_print_timer>25000) && !m_second_message_printed)
			 {
				 m_second_message_printed = true;
				 m_status_bar.print("WIN_TEXT_3");
			 }
			 
		 }
		 m_status_bar.update(get_elapsed_time());
		 
	 }
	 
	 protected void p_init()
	 {
		 m_status_bar = StatusBar.instance(getWidth(),getHeight());

		 
			play_music("misc" + File.separator + "win.mp3");
		 
	 }
	 

	 protected void p_render(Graphics2D g)
	 {
		 g.drawImage(m_background,0,0,null);
		 m_status_bar.render(g);
	 }
}
