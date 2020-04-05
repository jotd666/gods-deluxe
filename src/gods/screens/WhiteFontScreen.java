package gods.screens;

import java.awt.*;

import gods.game.GameState;
import gods.sys.Localizer;

/**
 * <p>Titre : </p>
 * <p>Description : </p>
 * <p>Copyright : Copyright (c) 2005</p>
 * <p>Société : </p>
 * @author non attribuable
 * @version 1.0
 */

public abstract class WhiteFontScreen extends GameState
{
    private String m_text;
    private boolean m_goto_menu;
    
  public WhiteFontScreen(String text,int duration)
  {
	  set_fadeinout_time(2000,2000,1000);
	  set_maximum_duration(3800);
	  m_text = text;
  }

  protected abstract GameState get_next_default_screen();
  
  protected void p_render(Graphics2D g)
  {
	  WHITE_BIG_FONT.write_line(g,Localizer.value(m_text),getWidth()/2,getHeight()/2,2,true,true);	
 }

  protected void p_init()
  {
	  //bsLoader.setMaskColor(Color.BLACK);
  }

  protected void p_update()
  {
	  if (is_a_button_pressed())
	  {
		  fadeout();
		  m_goto_menu = true;
	  }
	  if (is_fadeout_done())
      {
		  if (m_goto_menu)
		  {
			  set_next(new GodsMainMenuScreen());
		  }
		  else
		  {
			  set_next(get_next_default_screen());
		  }
      }

  }
}
