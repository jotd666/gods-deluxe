package gods.screens;
import java.awt.*;
import java.awt.event.KeyEvent;

import gods.game.*;
import gods.base.AnimatedFrames;
import gods.base.GfxFrameSet;
import gods.base.GfxPalette;
import gods.base.EditableData.GfxMode;
import gods.sys.*;

import java.io.File;
import java.io.IOException;

public class GodsHiScoreEntryScreen extends GameState 
{	
	private final static int Y_START = 16;
	private final static int X_SCORE_START = 80;
	private final static int Y_SCORE_START = 80;
	private final static int X_LETTER_SPACING = 64;
	private final static int Y_LETTER_SPACING = 48;
	private final static int Y_NAME_ENTRY = 352;
	private final static int NB_COLS = 8;
	private final static int NB_CHARS_IN_NAME = 16;
	
	private StatusBar m_status_bar;
	private String m_name = "", m_name_to_display;
	
	private int m_x = 0;
	private int m_y = 0;
	private AnimatedFrames m_cursor;
	private int m_score = 0;
	
	private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
			"._0123456789(§";
	private final static int NB_ROWS = CHARACTERS.length() / NB_COLS;
	
	public GodsHiScoreEntryScreen(int score)
	{
		set_fadeinout_time(1000,1000,0);
		
		m_score = score;
	}
	
	private void pad_name()
	{
		m_name_to_display = m_name;
		
		while (m_name_to_display.length() < NB_CHARS_IN_NAME)
		{
			m_name_to_display += "_";
		}
	}

	 protected GameState get_next_default_screen()
	 {
		 return new GodsHiScoreScreen(m_name,m_score);
	 }
	 
	 private void handle_keyboard_input()
	 {
		 for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; i++)
		 {
			 if (add_key_char(i))
			 {
				return;
			 }

		 }
		 add_key_char(KeyEvent.VK_SPACE);
		 
		 if (m_game.is_key_pressed(KeyEvent.VK_BACK_SPACE))
		 {
			 del_char();return;
		 }
		 if (m_game.is_key_pressed(KeyEvent.VK_ENTER))
		 {
			 fadeout();return;
		 }
		 
		 for (int i = KeyEvent.VK_A; i <= KeyEvent.VK_Z; i++)
		 {
			 if (add_key_char(i))
			 {
				 return;
			 }

		 }
	 }

	 private boolean add_key_char(int i)
	 {
		 
		 boolean rval = (m_game.is_key_pressed(i));
		 
		 if (rval)
		 {
			add_char((char)i);			 
			
		 }
		 
		 return rval;

	 }
	 private void add_char(char c)
	 {
		 if (m_name.length() < NB_CHARS_IN_NAME)
		 {
			 m_name = m_name + c;
			 pad_name();
		 }
	 }
	 private  void del_char()
	 {
		 if (m_name.length() > 0)
		 {
			 m_name = m_name.substring(0,m_name.length()-1);
			 pad_name();
		 }
	 }
	 protected void p_update()
	 {
		 m_controls.read();

		 if (m_controls.fire_pressed)
		 {
			 if (m_y == NB_ROWS - 1)
			 {
				 if (m_x == NB_COLS - 1)
				 {
					 fadeout();
				 }
				 else if (m_x == NB_COLS - 2)
				 {
					 // delete
				 }
			 }
		 }
		 if (m_controls.key_down)
		 {
			 m_y++;
		 }
		 else if (m_controls.key_up)
		 {
			 m_y--;
		 }

		 m_y = (m_y + NB_ROWS) % NB_ROWS;
		 
		 if (m_controls.key_right)
		 {
			 m_x++;
		 }
		 else if (m_controls.key_left)
		 {
			 m_x--;
		 }
		 m_x = (m_x + NB_COLS) % NB_COLS;
		 
		 handle_keyboard_input();
		 
		 if (m_controls.fire_pressed)
		 {
			 // which letter is on the cursor
			 int idx = m_x + m_y * NB_COLS;
			 
			 char c = CHARACTERS.charAt(idx);
			 
			 switch (c)
			 {
			 case '§':
				 fadeout();
				 break;
			 case '(':
				 del_char();
				 
				 break;
			 default:
				 add_char(c);
				 break;
					 
			 }
			 
		 }
		 if (is_fadeout_done())
		 {
			 Mp3Play.stop();
			 set_next(get_next_default_screen());
		 }
		  
		 m_status_bar.update(get_elapsed_time());
		 m_cursor.update(get_elapsed_time());
		 
		 update_cursor_pos();
	 }
	
	 private void update_cursor_pos()
	 {
		 int x = m_x * X_LETTER_SPACING + X_SCORE_START;
		 int y = m_y * Y_LETTER_SPACING + Y_SCORE_START;
		 
		 m_cursor.set_coordinates(x, y);
	 }
	 
	 protected void p_init()
	 {
		 GfxPalette p = new GfxPalette();
		 
		 pad_name();
		 
		 try 
		 {
			 p.load("hiscore_cursor", GfxMode.REWORKED_GAME);
		 } 
		 catch (IOException e1) 
		 {

		 }

		 GfxFrameSet cursor = p.lookup_frame_set("cursor");

		 m_cursor = new AnimatedFrames();
		 
		 m_cursor.init(cursor, 50, AnimatedFrames.Type.FOREVER);
		 
		 m_status_bar = StatusBar.instance(getWidth(),getHeight());
		 
	
		play_music("misc" + File.separator + "hiscore.mp3");
			


	 }
	 
	 protected void p_render(Graphics2D g)
	 {
		 int h = getHeight();
		 int y_limit = h-m_status_bar.get_height()-4;
		 
		 g.drawImage(m_helmet, 0, 0, getWidth(), y_limit, 
				 0,0, getWidth(), y_limit,null);
		 
		 g.setColor(Color.BLACK);
		 g.fillRect(0, y_limit, getWidth(), h);
			
		GOLDEN_BIG_FONT.write_line(g,Localizer.value("ENTER YOUR NAME HERO"),getWidth()/2,Y_START,0,true,false);
		 
		int nb_in_row = 0;
		
		 for (int i = 0; i < CHARACTERS.length(); i++)
		 {		 
			 GOLDEN_BIG_FONT.write_line(g, ""+CHARACTERS.charAt(i), 
					 X_SCORE_START + X_LETTER_SPACING * nb_in_row, 
					 Y_SCORE_START + (i / NB_COLS) * Y_LETTER_SPACING, 0, false, false);
			 
			 nb_in_row++;
			 if (nb_in_row == NB_COLS)
			 {
				 nb_in_row = 0;
			 }
		 }
		 
		 GOLDEN_BIG_FONT.write_line(g, m_name_to_display, getWidth()/2, Y_NAME_ENTRY, 4, true, false);
		 m_status_bar.render(g);
		 
		 // render cursor
		 
		 m_cursor.render(g);
		 
		 
	 }
	 
}
