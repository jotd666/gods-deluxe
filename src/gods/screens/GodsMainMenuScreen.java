package gods.screens;
import java.awt.*;

import gods.game.*;
import gods.base.DirectoryBase;
import gods.base.GameOptions;
import gods.sys.*;

import java.io.File;

public class GodsMainMenuScreen extends GameState 
{
	private int m_cursor_pos = 0;
	private final static int Y_START = 164;
	private final static int Y_STEP = 66;	
	private static final int ARROW_OFFSET = 60;
	private static final String [] MENU_OPTIONS = { "BEGIN QUEST", "OPTIONS", "EXIT" };
	private StatusBar m_status_bar;
	private boolean m_quit_game = false;
	private GameState m_next_screen = null;
	 private boolean m_start_game = false;
	 
	 

	 public GodsMainMenuScreen()
	{
		set_fadeinout_time(1000,1000,0);
		set_maximum_duration(10000);
	}
	
	 protected GameState get_next_default_screen()
	 {
		 return new GodsHiScoreScreen();
	 }
	 
	 protected void p_update()
	 {
		 m_controls.read();
		 
		 if (m_controls.fire_pressed)
		 {
			 switch (m_cursor_pos)
			 {
			 case 0:
				 fadeout();
				 m_start_game = true;
				 break;
			 case 1:
				 m_next_screen = new GodsOptionsMenuScreen1();
				 fadeout();
			 break;
			 case 2:
				 m_quit_game = true;
				 fadeout();
			 break;
			 }
		 }
		 
		 
		 if (m_controls.key_down)
		 {
			 m_cursor_pos++;
			 if (m_cursor_pos == MENU_OPTIONS.length)
			 {
				 m_cursor_pos = 0;
			 }
		 }

	 
		 
		 if (m_controls.key_up)
		 {		 
			 if (m_cursor_pos == 0)
			 {
				 m_cursor_pos = MENU_OPTIONS.length;
			 }
			 m_cursor_pos--;
		 }
		 
		 if (is_fadeout_done())
		 {
			 if (m_quit_game)
			 {
				 m_game.finish();
			 }
			 else if (m_start_game)
			 {
				
				 try 
				 {
					 GodsLevel gl = GodsLevel.create(null);

					 set_next(gl);
				 } catch (Exception e) {
					 // TODO Auto-generated catch block
					 e.printStackTrace();
				 } 

			 }
			 else
			 {
				 if (m_next_screen == null)
				 {
					 m_next_screen = new GodsHiScoreScreen();
				 }
				 
				 set_next(m_next_screen);
				 

			 }
		 }
		 
		 m_status_bar.update(get_elapsed_time());
	 }
	
	 
	 private String [] m_title_mp3s = null;
	 
		
	 protected void p_init()
	 {
		 m_status_bar = StatusBar.instance(getWidth(),getHeight());
		 
		 m_status_bar.print("gods_written_by_jotd");
		 
		 m_game.stop_music();
		 
		 if (!Mp3Play.is_playing())
		 {
			 if (m_title_mp3s == null)
			 {
				 // scan the directory

				 File f = new File(DirectoryBase.get_mp3_path() + "title");

				 String [] ar = f.list();
				 if (ar != null)
				 {
					 int count = 0;

					 for (String s : ar)
					 {
						 if (s.endsWith("mp3"))
						 {
							 count++;
						 }
					 }
					 if (count > 0)
					 {
						 m_title_mp3s = new String[count];

						 count = 0;

						 for (String s : ar)
						 {
							 if (s.endsWith("mp3"))
							 {
								 m_title_mp3s[count++] = s;
							 }
						 }
					 }
				 }
				 
			 }
			 
			 if (m_title_mp3s != null)
			 {
				 String s = m_title_mp3s[(int)(Math.random()*m_title_mp3s.length)];
				 
				 play_music("title"+File.separator+s);		 

			 }
		 }
	 }
	 
	 private void write(Graphics2D g,GameFont gf)
	 {
		 gf.write_line(g, "G", getWidth()/2, 14, 10, true, false);
	 }
	 
	 protected void p_render(Graphics2D g)
	 {
		 int h = getHeight();
		 int y_limit = h-m_status_bar.get_height()-4;
		 
		 g.drawImage(m_helmet, 0, 0, getWidth(), y_limit, 
				 0,0, getWidth(), y_limit,null);
		 
		 g.setColor(Color.BLACK);
		 g.fillRect(0, y_limit, getWidth(), h);
		 
		 write(g,GODS_GOLDEN_FONT);
		 for (int i = 0; i < MENU_OPTIONS.length; i++)
		 {
			 GOLDEN_BIG_FONT.write_line(g,Localizer.value(MENU_OPTIONS[i]),getWidth()/2,
					 Y_START + Y_STEP * i,-2,true,true);
		 }
		 int ypos = Y_START + Y_STEP * m_cursor_pos;
		 
		 GOLDEN_BIG_FONT.write_line(g,")",ARROW_OFFSET,ypos,0,true,true);
		 GOLDEN_BIG_FONT.write_line(g,"(",getWidth()-ARROW_OFFSET,ypos,0,true,true);
		 
		 m_status_bar.render(g);
	 }
}
