package gods.screens;
import java.awt.*;
import java.awt.image.BufferedImage;

import gods.base.DirectoryBase;
import gods.base.GameOptions;
import gods.game.GameState;
import gods.sys.*;


public class GodsHistoryScreen extends GameState {

	private BufferedImage m_background;
	private GameFont m_gods_small_font;
	private static final long TEXT_DELAY = 10800;
	private static final long TEXT_TIME = TEXT_DELAY;
	private int m_text_index = 0;
	private double m_text_position;
	private String m_text = null;
	private static final int NB_TEXTS = 8;
	private int m_y_offset = 0;
	private boolean m_part_2;
	private int m_text_timer = 0;
	private boolean m_goto_menu = false;
	
	public GodsHistoryScreen(boolean part_2)
	{
		set_fadeinout_time(250, 250,0);
		m_part_2 = part_2;

	}
	
	 protected GameState get_next_default_screen()
	 {
		 return new GameScreen();
	 }
	 
	 protected void p_update()
	 {
		 m_text_timer += get_elapsed_time();
		 if (m_text_timer >= TEXT_DELAY)
		 {
			 m_text_timer = 0;
			 m_text_index++;		 
			 m_text_position = getHeight();
		 }
		 
		 if (is_a_button_pressed())
		 {
			 fadeout();
			 m_goto_menu = true;
		 }
		 
		 if (!is_fadeout())
		 {
			 m_text_position -= (double)(get_elapsed_time() * (getHeight()+m_y_offset+136)) / TEXT_TIME;
		 }
		 
		 if (!m_part_2)
		 {
			 if (m_text_index == NB_TEXTS/2)
			 {		
				 fadeout();
			 }
			 if (is_fadeout_done())
			 {
				 if (m_goto_menu)
				 {
					 set_next(new GodsMainMenuScreen()); 
				 }
				 else
				 {
					 set_next(new GodsHistoryScreen(true));
				 }
			 }
		 }
		 else
		 {
			 if ((m_text_index == NB_TEXTS+1) && m_text_timer > 6000)
			 {
				 fadeout();
			 }
			 if (is_fadeout_done())
			 {
				 if (m_goto_menu)
				 {
					 set_next(new GodsMainMenuScreen()); 
				 }
				 else
				 {
					 set_next(new GodsCreditsScreen());
				 }
			 }
		 }
	 }
	 
	 protected void p_init()
	 {
		 if (!m_part_2)
		 {
			//m_game.load_music("gods.mod",1192464,false);
		 }
		 else
		 {
			 m_text_index = NB_TEXTS/2;
			 m_text_position = getHeight();
			 //m_game.load_music("gods.mod",3127572,false);
		 }		 
		 
		 //System.out.println("pos: "+m_game.get_music_pos());

		 try
		 {
			 //BufferedImage warrior = new ImageScale2x(ImageLoadSave.load_png(DirectoryBase.get_images_path() +"warrior")).getScaledImage();
			 BufferedImage warrior = ImageLoadSave.load_png(DirectoryBase.get_images_path() +"warrior",Color.MAGENTA);
			 BufferedImage warrior_background = ImageLoadSave.load_png(DirectoryBase.get_images_path()+"purple_gradient");
			 m_background = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_RGB);
			 Graphics g = m_background.getGraphics();
			 g.drawImage(warrior_background,0,0,null);
			 g.drawImage(warrior,8,0,null);
			 
			 m_gods_small_font = new GameFont(DirectoryBase.get_font_path()+"small_letters",GameOptions.instance().get_font_gfx_flavor());
			
		 }
		 catch (java.io.IOException e)
		 {
			 
		 }
	 }
	 

	 protected void p_render(Graphics2D g)
	 {
		 // 0..1
		 float state_percent = ((float)(get_state_elapsed_time())) / TEMPO;
		 
		 
		 float percent_very_fast = Math.min(state_percent*8, 1);
		 
		 if (state_percent < 1)
		 {
			 clear_composite(g);		  
			 g.setColor(Color.WHITE);
			 g.fillRect(0,0,getWidth(),getHeight());
			 set_composite(g,percent_very_fast,AlphaComposite.SRC_OVER);
		 }
		 g.drawImage(m_background,0,0,null);
		 
		 if ((m_text_index > 0) && (m_text_index <= NB_TEXTS))
		 {
			 m_text = Localizer.value("INTRO_TEXT_" + m_text_index);
			 m_gods_small_font.write(g, m_text, 490, (int)m_text_position, -2, true, false, 4);
		 }	 

	 }
}
