package gods.game;

//import com.golden.gamedev.*;
//import com.golden.gamedev.engine.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import gods.base.DebugOptions;
import gods.base.DirectoryBase;
import gods.base.GameOptions;
import gods.screens.GodsMainMenuScreen;
import gods.screens.JotdScreen;
import gods.sys.GameEngine;
import gods.sys.Localizer;
import gods.sys.SoundService;
import micromod.MicromodPlayer;


public class GodsGame extends GameEngine
{
	private MicromodPlayer m_player = null;
	private GameState m_state = null;
	
	public void finish()
	{
		GameOptions.instance().save();

		super.finish();
	}
	
	public GodsGame(Rectangle useful_bounds, boolean double_display, boolean antialiasing) throws Exception
  	{	  
	  setup(useful_bounds,Localizer.value("window title")+" - "+Localizer.value("version")+" "+Version.current(),
		KeyEvent.VK_F10, Color.BLACK, double_display, antialiasing); 
	  
		List<Image> icons = new ArrayList<Image>();
		File s32 = new File(DirectoryBase.get_images_path()+"helmet32x32.png");
		File s16 = new File(DirectoryBase.get_images_path()+"helmet16x16.png");
		try {
			icons.add(ImageIO.read(s32));
			icons.add(ImageIO.read(s16));
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		get_window().setIconImages(icons);
  	}
	
	public int get_music_pos()
	{
		int r = 0;

		if (m_player != null)
		{
			r = m_player.get_position();
		}
		return r;
	}
	public void stop_music()
	{
		if (m_player != null)
		{
			m_player.stop();
			m_player = null;
		}
	}

	public boolean is_music_playing()
	{
		return m_player != null;
	}
	public void load_music(String music_file)
	{
		load_music(music_file,0,false);
	}
	public void load_music(String music_file, int initial_position, boolean loop)
	{
		if (GameOptions.instance().get_sfx_state())  // also plays when sfx only, like original Amiga game
		{
			String path = gods.base.DirectoryBase.get_mod_path() + music_file;
		  try
		  {
			  stop_music();
			  m_player = new MicromodPlayer(new File(path).toURI().toURL());
			  m_player.setloop(loop);
			  m_player.set_start_offset(initial_position);
			  SoundService.execute(m_player);
		  }
		  catch (Exception e)
		  {
			  //m_state.show_error(path+": "+e.getMessage());
		  }
	  }
	  else
	  {
		  stop_music();
	  }
  }

  public void initResources()
  {
	GameOptions go = GameOptions.instance();
	  
    // System.setProperty("user.dir",gods.base.DirectoryBase.get_root());
 
    hide_cursor();

    //setFPS(go.get_max_fps_value());
    
    if (go.with_intro)
    {
    	m_state = new JotdScreen();
    	//m_state = new GodsHistoryScreen(true);
    	//m_state = new GodsCreditsScreen();
    }
    else
    {
    	if (GameOptions.instance().direct_game)
    	{
    		try 
    		{
    			m_state = GodsLevel.create(null);
    			
    			// TEMP
    			//StatusBar sb = new StatusBar(get_width(), get_height());
    			//gods.game.characters.Hero h = new gods.game.characters.Hero(level.get_data(),null,sb,level.get_sfx_set(),level.get_view_bounds());
    			//h.level_init(null, null);
    			//h.set_health(18);
    			//h.add_money(40000);

    			//m_state = new Shop(GodsClassicLevel1.SHOP_CONTENTS_W3_START,h,32,level.get_data().get_common_palette(),
    			//	level.get_data().get_level_palette(),sb,null);

    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}	
    	}
    	else
    	{
    		m_state = new GodsMainMenuScreen();
    	}
    }

    m_state.init(this);
  }

  public void render(Graphics2D g)
  {
	  m_state.render(g);
  }

  public void update(long elapsed)
  {
	 
	  GameState s = m_state.update(elapsed);

	  if (s != null)
	  {
		  // screen change
		  m_state = s;
		  m_state.init(this);
	  }
  }
 
  
  public boolean return_pressed()
  {
	  return false; // TODO keyPressed(KeyEvent.VK_ENTER);
  }

  
  
	/****************************************************************************/
	/***************************** START-POINT **********************************/
	/****************************************************************************/

	private static void usage()
	{
		System.out.println("Gods -Deluxe- valid public options: ");
		System.out.println("");
		System.out.println("   -no-intro: skips introduction");
		System.out.println("   -direct-game: runs with last selected level");
		System.out.println("   -double-display: runs in a double-sized scale2x window (fast CPU/gfx board required)");
		System.out.println("   -antialiasing: applies bilinear antialiasing on scaling (fast CPU/gfx board required)");
	}

   	public static void main(String[] args) throws Exception
   	{
		GameOptions opts = GameOptions.instance();
		boolean double_display = false;
		Rectangle useful_bounds = new Rectangle(640,400);
		boolean antialiasing = false;

		int i = 0;
		while (i < args.length)
		{
			String arg = args[i];
			if (arg.charAt(0) == '-')
			{
				if (arg.equalsIgnoreCase("-direct-game"))
				{
					opts.with_intro = false;
					opts.direct_game = true;
				}
				else if (arg.equalsIgnoreCase("-no-intro"))
				{
					opts.with_intro = false;
				}
				else if (arg.equalsIgnoreCase("-debug"))
				{
					// L: level end
					// T: pass through walls on/off
					// S: get shield
					
					DebugOptions.debug = true;
					opts.unlock_levels = true;
				}
				else if (arg.equalsIgnoreCase("-unlock-levels"))
				{
					opts.unlock_levels = true;
				}
				else if (arg.equalsIgnoreCase("-double-display"))
				{
					double_display = true;
				}
				else if (arg.equalsIgnoreCase("-antialiasing"))
				{
					antialiasing = true;
				}
				else
				{
					usage();
					System.exit(1);
				}
			}
			i++;
		}
		
		DirectoryBase.check_paths();
		opts.load_settings();

		GodsGame scgame = new GodsGame(useful_bounds,double_display,antialiasing);

		scgame.start();
	}

}
