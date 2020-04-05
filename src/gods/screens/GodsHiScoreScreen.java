package gods.screens;
import java.awt.*;

import gods.game.*;
import gods.base.GameOptions;
import gods.sys.*;

public class GodsHiScoreScreen extends GameState 
{	
	private final static int Y_START = 16;
	private final static int Y_SCORE_START = 80;
	private final static int Y_SCORE_OFFSET = 26;
	private final static int X_LETTER_OFFSET = 2;
	
	private StatusBar m_status_bar;
	private RecordScore m_hiscores = null;
	
	public GodsHiScoreScreen(String name, int score)
	{
		this();
		
		
		if (m_hiscores.insert(name, score))
		{
			m_hiscores.save();
		}
		
	}
	public GodsHiScoreScreen()
	{
		set_fadeinout_time(1000,1000,0);
		set_maximum_duration(10000);
		m_hiscores = new RecordScore();
		m_hiscores.load();
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
		 

	
		 
		 if (is_fadeout_done())
		 {
			 GameOptions.instance().save();
			 
			 set_next(get_next_default_screen());
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
			
		GOLDEN_BIG_FONT.write_line(g,Localizer.value("TOP  TEN  GODS"),getWidth()/2,Y_START,0,true,false);
		 
		 for (int i = 0; i < RecordScore.NB_SCORES; i++)
		 {
			 int y = Y_SCORE_START + Y_SCORE_OFFSET * i;
			 ScoreEntry se = m_hiscores.get_entries()[i];
			 
			 GOLDEN_SMALL_FONT.write(g, se.player, 40, y, X_LETTER_OFFSET, false, false, 0);
			 GOLDEN_SMALL_FONT.write(g, hiscore(se.score), 440, y, X_LETTER_OFFSET, false, false, 0);
		 }
		 m_status_bar.render(g);
	 }
	 
	 private String hiscore(int score)
	 {
		 String rval = "" + score;
		 
		 while (rval.length() < 8)
		 {
			 rval = "0" + rval;
		 }
		 
		 return rval;
	 }
}
