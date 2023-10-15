package gods.game;

//import com.golden.gamedev.*;
//import com.golden.gamedev.engine.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.*;

import javax.imageio.ImageIO;

import micromod.*;
import gods.base.DebugOptions;
import gods.base.DirectoryBase;
import gods.base.GameOptions;
import gods.sys.*;
import gods.screens.*;


public class GodsGame extends GameEngine
{
	private MicromodPlayer m_player = null;
	private GameState m_state = null;
	private int m_screen_height;
	private int m_screen_width;
	private boolean m_double_display;
	
	// used for double screen scaling
	private BufferedImage m_temp_render = null;
	private BufferedImage m_double_image = null;
	private RawScale2x m_scaler;

	public void finish()
	{
		GameOptions.instance().save();

		super.finish();
	}
	
	public GodsGame(Rectangle window_bounds, Rectangle useful_bounds, boolean full_screen, 
			boolean double_buffering,boolean double_display) throws Exception
  	{

		
	  m_screen_height = window_bounds.height;
	  m_screen_width = window_bounds.width;
	  
	  m_double_display = double_display;
	  if (double_display)
	  {

		  m_screen_height *= 2;
		  m_screen_width *= 2;

		  m_temp_render = new BufferedImage(useful_bounds.width, useful_bounds.height, BufferedImage.TYPE_INT_RGB);
		  m_double_image = new BufferedImage(useful_bounds.width*2,useful_bounds.height*2,BufferedImage.TYPE_INT_RGB);
		  int[] imgData = ((DataBufferInt)m_temp_render.getRaster().getDataBuffer()).getData();

		  m_scaler = new RawScale2x(imgData,useful_bounds.width,useful_bounds.height);

	  }
	  setup(window_bounds,useful_bounds,Localizer.value("window title")+
		" - "+Localizer.value("version")+" "+Version.STRING_VALUE,
		KeyEvent.VK_F10,
		Color.BLACK,
		full_screen,double_buffering,double_display); 
	  
		java.util.List<Image> icons = new java.util.ArrayList<Image>();
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
			  new Thread(m_player).start();
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
	  if (m_screen_height > 0)
	  {
		  // first time
		  g.setColor(Color.BLACK);
		  g.fillRect(0,0,m_screen_width,m_screen_height);
		  m_screen_height = 0;
	  }
	  if (m_double_display)
	  {
		  int width=m_temp_render.getWidth(),height=m_temp_render.getHeight();
		  
		  // render in temp buffer
		  m_state.render((Graphics2D)m_temp_render.getGraphics());

		  //m_temp_render.getRGB(0,0,width,height,m_src_data,0,width);              
		  m_double_image.setRGB(0,0,width*2,height*2,m_scaler.getScaledData(),0,width*2);

		  g.drawImage(m_double_image,0,0,null);
	  }
	  else
	  {
		  m_state.render(g);
	  }
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
		System.out.println("   -full-screen: runs the game in full screen");
		System.out.println("   -no-intro: skips introduction");
		System.out.println("   -direct-game: runs with last selected level");
		System.out.println("   -double-display: runs in a double-sized scale2x window (fast CPU/gfx board required)");
		System.out.println("   -wh: forces window/screen height (default: windowed: 400, fullscreen: 480");  
	}

   	public static void main(String[] args) throws Exception
   	{
		GameOptions opts = GameOptions.instance();
		boolean double_buffering = true;
		boolean double_display = false;
		Rectangle useful_bounds = new Rectangle(640,400);
		Rectangle window_bounds = (Rectangle)useful_bounds.clone();

		int y_res = 0;

		int i = 0;
		while (i < args.length)
		{
			String arg = args[i];
			String nextarg = (i < args.length - 1 ? args[i+1] : "");
			if (arg.charAt(0) == '-')
			{
				if (arg.equalsIgnoreCase("-direct-game"))
				{
					opts.with_intro = false;
					opts.direct_game = true;
				}
				else if (arg.equalsIgnoreCase("-wh"))
				{
					try
					{
						y_res = Integer.parseInt(nextarg);
						i++;
					}
					catch (Exception e)
					{
						System.out.println("Invlaid height " + nextarg);
					}
				}
				else if (arg.equalsIgnoreCase("-full-screen"))
				{
					opts.full_screen = true;
					window_bounds.height = 480;
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
					double_buffering = false; // the way double display is handled means double buffered
				}
				else
				{
					usage();
					System.exit(1);
				}
			}
			i++;
		}
		
		if (y_res > 0)
		{
			window_bounds.height = y_res;
		}
		
		DirectoryBase.check_paths();
		opts.load_settings();

		GodsGame scgame = new GodsGame(window_bounds,useful_bounds,opts.full_screen,double_buffering,double_display);

		scgame.start();
	}

}
