package gods.screens;
import java.awt.*;

import gods.game.GameState;
import gods.sys.*;


public class GodsTitleScreen extends GameState {

	private int m_state;
	private static final int TEMPO = 1900;
	
	public GodsTitleScreen()
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
	 }
	 
	 protected void p_init()
	 {
		 m_state = 0;
		 

	 }
	 
	 private void write(Graphics2D g,GameFont gf)
	 {
		 gf.write_line(g, "G", getWidth()/2, 36, 10, true, false);
	 }
	 protected void p_render(Graphics2D g)
	 {
		 // 0..1
		 boolean percent_fast_done = false;
		 
		 float state_percent = ((float)(get_state_elapsed_time() - (m_state * TEMPO))) / TEMPO;
		 
		 float percent_fast = state_percent*4;
		 percent_fast_done = (percent_fast > 1);
		 
		 float percent_very_fast = state_percent*8;
		 
		 
		 switch (m_state)
		 {
		 // white letters fade to golden letters
		 case 0:
			 if (!percent_fast_done)
			 {
				 write(g,GODS_WHITE_FONT);
				 set_composite(g,percent_fast,AlphaComposite.SRC_OVER);			  
			 
				 write(g,GODS_GOLDEN_FONT);
			 }
			 break;
			 // background picture fades in, golden letters stand
		 case 1:
			 if (!percent_fast_done)
			 {
				 clear_screen(g);
				 set_composite(g,percent_fast,AlphaComposite.SRC_OVER);			  
				 g.drawImage(m_helmet,0,0,null);
				 clear_composite(g);		  
			 
				 write(g,GODS_GOLDEN_FONT);
			 }
			 break;
			 // background picture stands, golden letter shine briely in white
		 case 2:			 
			 g.drawImage(m_helmet,0,0,null);
			 if (state_percent < 0.125)
			 {
				 write(g,GODS_GOLDEN_FONT);
				 set_composite(g,percent_very_fast,AlphaComposite.SRC_OVER);			  
				 write(g,GODS_WHITE_FONT);
			 }
			 else if (state_percent < 0.25)
			 {
				 write(g,GODS_WHITE_FONT);
				 set_composite(g,(state_percent-0.125f)*8,AlphaComposite.SRC_OVER);			  
				 write(g,GODS_GOLDEN_FONT);				 
			 }
			 else
			 {
				 write(g,GODS_GOLDEN_FONT);				 				 
			 }
			 break;
			
		 case 3:
			 clear_composite(g);
			 g.drawImage(m_helmet,0,0,null);
			 write(g,GODS_GOLDEN_FONT);
			 break;
		 case 4:
			 clear_screen(g);
			 set_composite(g,1-state_percent,AlphaComposite.SRC_OVER);			  
			 g.drawImage(m_helmet,0,0,null);
			 set_composite(g,1,AlphaComposite.SRC_OVER);			  
			 write(g,GODS_GOLDEN_FONT);
			 break;
		 case 5:
			 clear_screen(g);
			 set_composite(g,1-state_percent,AlphaComposite.SRC_OVER);			  
			 write(g,GODS_GOLDEN_FONT);		 
			 break;
		 default:
			 fadeout();
		 	
			set_next(new GodsHistoryScreen(false));
			
		 break;
		 }
	 }
}
