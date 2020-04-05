package gods.screens;
import java.awt.*;

import gods.game.GameState;
import gods.sys.*;
import gods.base.DirectoryBase;
import gods.base.GameOptions;

public class GodsCreditsScreen extends GameState {

	private GameFont m_golden_font;
	private GameFont m_white_font;
	private int m_state;
	private int [] m_y_pos = new int[3];
	private String [] m_text = new String[3];
	private static final int TEMPO = 1900;
	
	public GodsCreditsScreen()
	{
		set_fadeinout_time(0, 0,0);
	}
	
	 protected GameState get_next_default_screen()
	 {
		 return new GameScreen();
	 }
	 
	 protected void p_update()
	 {
		 m_state = (int)(get_state_elapsed_time() / TEMPO);
		 
		 if ((m_state == 14) || is_a_button_pressed())
		 {
			 fadeout();
		 }
		 
		 if (is_fadeout_done())
		 {
			 set_next(new GodsMainMenuScreen());
		 }
	 }
	 
	 protected void p_init()
	 {
		 m_state = 0;
		 
		 try
		 {
			 String gfx_flavor = GameOptions.instance().get_font_gfx_flavor();
			 m_golden_font = new GameFont(DirectoryBase.get_font_path()+"big_letters",gfx_flavor);
			 m_white_font = new GameFont(DirectoryBase.get_font_path()+"big_letters",gfx_flavor,Color.WHITE,0);
			
		 }
		 catch (java.io.IOException e)
		 {
			 
		 }
	 }
	 
	 private void write(Graphics2D g,GameFont gf, int count)
	 {
		 for (int i = 0; i < count; i++)
		 {
		 gf.write_line(g, m_text[i], getWidth()/2, m_y_pos[i], 2, true, false);
		 }
	 }

	 private void draw_helmet(Graphics2D g)
	 {
		 g.drawImage(m_helmet,0,0,null);
		 
	 }
	 protected void p_render(Graphics2D g)
	 {
		 // 0..1
		 float state_percent = ((float)(get_state_elapsed_time() - (m_state * TEMPO))) / TEMPO;
		 float percent_fast = Math.min(state_percent*4, 1);
		 //float percent_very_fast = Math.min(state_percent*8, 1);
		 
		 if (m_state == 0)
		 {
			 clear_screen(g);
			 set_composite(g,percent_fast,AlphaComposite.SRC_OVER);
		 }
		 
		 switch (m_state)
		 {
		 case 0:
			 draw_helmet(g);
			 m_text[0] = Localizer.value("PRODUCTION TEAM");
			 m_y_pos[0] = 160;
			 
			 write(g,m_white_font,1);
			 set_composite(g,percent_fast,AlphaComposite.SRC_OVER);			  
			 write(g,m_golden_font,1);
			 break;
			 
		 case 1:
			 draw_helmet(g);
			 m_text[0] = Localizer.value("ORIGINAL CODE");
			 m_y_pos[0] = 140;
			 m_text[1] = "STEVE TALL";
			 m_y_pos[1] = 200;
			 write(g,m_white_font,2);
			 set_composite(g,percent_fast,AlphaComposite.SRC_OVER);			  
			 write(g,m_golden_font,2);
			break;
		 case 2:
			 draw_helmet(g);
			 m_text[0] = Localizer.value("ORIGINAL GRAPHICS");
			 m_text[1] = "MARK COLEMAN";
			 write(g,m_white_font,2);
			 set_composite(g,percent_fast,AlphaComposite.SRC_OVER);			  
			 write(g,m_golden_font,2);
			break;
		 case 3:
			 draw_helmet(g);
			 m_text[0] = Localizer.value("DESIGN");
			 m_text[1] = "ERIC MATTHEWS";
			 m_text[2] = "STEVE TALL";
			 m_y_pos[2] = 246;
			 write(g,m_white_font,3);
			 set_composite(g,percent_fast,AlphaComposite.SRC_OVER);			  
			 write(g,m_golden_font,3);
			break;
		 case 4:
			 draw_helmet(g);
			 m_text[0] = Localizer.value("MUSIC");
			 m_text[1] = "NATION XII";
			 write(g,m_white_font,2);
			 set_composite(g,percent_fast,AlphaComposite.SRC_OVER);			  
			 write(g,m_golden_font,2);
			break;
		 case 5:
			 draw_helmet(g);
			 m_text[0] = Localizer.value("MUSIC CODE AND SFX");
			 m_text[1] = "RICHARD JOSEPH";
			 write(g,m_white_font,2);
			 set_composite(g,percent_fast,AlphaComposite.SRC_OVER);			  
			 write(g,m_golden_font,2);
			break;
		 case 6:
			 draw_helmet(g);
			 m_text[0] = Localizer.value("REMAKE EXTRAS");
			 m_text[1] = "JAVA CODE AND GFX: JOTD";
			 m_text[2] = "GFX REWORK: Y. BIN QAISER";
			 m_y_pos[2] = 246;
			 write(g,m_white_font,3);
			 set_composite(g,percent_fast,AlphaComposite.SRC_OVER);			  
			 write(g,m_golden_font,3);
			break;
		 case 7:
			 draw_helmet(g);
			 GODS_WHITE_FONT.write_line(g, "G", getWidth()/2, 36, 10, true, false);
			 set_composite(g,percent_fast,AlphaComposite.SRC_OVER);			  
			 GODS_GOLDEN_FONT.write_line(g, "G", getWidth()/2, 36, 10, true, false);
			 break;
		 case 8:
		 case 9:
			 draw_helmet(g);
			 GODS_GOLDEN_FONT.write_line(g, "G", getWidth()/2, 36, 10, true, false);
			 break;
		 case 10:
			 draw_helmet(g);
			 m_text[0] = Localizer.value("INTO THE WONDERFUL");
			 m_y_pos[0] = 200;
			 write(g,m_white_font,1);
			 GODS_WHITE_FONT.write_line(g, "G", getWidth()/2, 36, 10, true, false);
			 set_composite(g,percent_fast,AlphaComposite.SRC_OVER);			  
			 GODS_GOLDEN_FONT.write_line(g, "G", getWidth()/2, 36, 10, true, false);
			 write(g,m_golden_font,1);
			 break;
		 case 11:
			 clear_screen(g);
			 set_composite(g,1-state_percent,AlphaComposite.SRC_OVER);			  
			 draw_helmet(g);
			 clear_composite(g);
			 GODS_GOLDEN_FONT.write_line(g, "G", getWidth()/2, 36, 10, true, false);
			 write(g,m_golden_font,1);
			 break;
		 case 12:
			 GODS_GOLDEN_FONT.write_line(g, "G", getWidth()/2, 36, 10, true, false);
			 write(g,m_golden_font,1);
			 break;
		 case 13:
			 clear_screen(g);
			 set_composite(g,1-state_percent,AlphaComposite.SRC_OVER);			  
			 GODS_GOLDEN_FONT.write_line(g, "G", getWidth()/2, 36, 10, true, false);
			 write(g,m_golden_font,1);
			 break;
		 default:

			
		 break;
		 }
	 }
}
