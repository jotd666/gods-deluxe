package gods.game;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import gods.base.DirectoryBase;
import gods.base.GameOptions;
import gods.sys.*;
import joystick.*;



public abstract class GameState
{
	public static GameFont WHITE_BIG_FONT = null;
	public static GameFont GOLDEN_BIG_FONT = null;
	public static GameFont GOLDEN_SMALL_FONT = null;
	public static GameFont GODS_GOLDEN_FONT = null;
	public static GameFont GODS_WHITE_FONT = null;

	protected static final long TEMPO = 1900; // so music is synchronized with credits/history intro screens
	
	protected static final BufferedImage m_helmet = ImageLoadSave.load_png(DirectoryBase.get_images_path() + "helmet");
	
	private boolean m_fake_fadein;
	private long m_fadein_time;
	private long m_fadeout_time;
	private long m_faded_out_delay;
	private long m_maximum_duration;
	private long m_fadeout_start_time;
	private long m_state_elapsed;
	private long m_elapsed_time;
	private GameState m_next_state;
	private boolean m_fadeout, m_fadeout_done;
	private boolean m_escaped;
	protected GodsGame m_game;
	protected Color m_background_color;
	private String m_error_message = null;
	private boolean m_error_displayed = false;
	private float m_color_percent;
	protected OptionControls m_controls = new OptionControls();
	protected Rectangle m_display_bounds;
		
	protected abstract void p_update();
	protected abstract void p_init();
	protected abstract void p_render(Graphics2D g);

	 public Joystick get_joystick()
	 {
		 return m_controls.m_joystick;
	 }
	 public class OptionControls
	 {
		 public boolean key_up = false;
		 public boolean key_down = false;
		 public boolean key_left = false;
		 public boolean key_right = false;
		 public boolean fire_pressed = false;
		 
		 private float last_x_pos = 0;
		 private float last_y_pos = 0;
		 private int last_button_state = 0;
		 
		 private Joystick m_joystick = Joystick.create(0);
		 

		 boolean is_a_button_pressed()
		 {
			 boolean rval = m_game.is_key_pressed(KeyEvent.VK_CONTROL);
			 if (!rval && m_joystick != null)
			 {
				 int button_state = m_joystick.getButtons();
				 
				 if (button_state != last_button_state)
				 {
					 //rval = (button_state & Joystick.BUTTON1) != 0;
					 // all buttons work
					 rval = button_state != 0;
				 
					 last_button_state = button_state;
				 }
			 }
			 
			 return rval;
		 }

		 public void read()
		 {
			 key_up = m_game.is_key_pressed(KeyEvent.VK_UP);
			 key_down = m_game.is_key_pressed(KeyEvent.VK_DOWN);
			 key_left = m_game.is_key_pressed(KeyEvent.VK_LEFT);
			 key_right = m_game.is_key_pressed(KeyEvent.VK_RIGHT);

			 fire_pressed = is_a_button_pressed();

			 if (m_joystick != null)
			 {
				 
				 float y_pos = m_joystick.getYPos();
				 float x_pos = m_joystick.getXPos();

				 if (!key_left)
				 {
					 key_left = last_x_pos > -0.5 && x_pos < -0.5;
				 }
				 if (!key_right)
				 {
					 key_right = last_x_pos < 0.5 && x_pos > 0.5;
				 }
				 if (!key_up)
				 {
					 key_up = last_y_pos > -0.5 && y_pos < -0.5;
				 }
				 if (!key_down)
				 {
					 key_down = last_y_pos < 0.5 && y_pos > 0.5;
				 }

				 last_y_pos = y_pos;
				 last_x_pos = x_pos;
			 }		 
		 }
	 }
	protected static final String pad_string(String s, 
			int pad_length, boolean left)
	{
		String ps = "";

		for (int i = 0; i < pad_length - s.length(); i++)
		{
			ps += " ";
		}
		if (left)
		{
			ps += s;
		}
		else
		{
			ps = s + ps;
		}

		return ps;
	}

	
	protected boolean is_a_button_pressed()
	{
		return m_controls.is_a_button_pressed();
	}
  protected void draw_string(Graphics2D g,String s,double height_ratio, GameFont gf)
  {
	
	gf.write_line(g, s, (int)(getWidth() / 2.0),
			(int)(getHeight() * height_ratio),0,true,false);
  }
  protected void draw_localized_string(Graphics2D g,String s,int x, int y, GameFont gf)
  {
	  draw_string(g,Localizer.value(s),x,y,gf);
  }
  
  protected void draw_string(Graphics2D g,String s,int x, int y, GameFont gf)
  {
	
	gf.write_line(g, s, x, y, 0, false, false);
  }
  /**
   * draw an horizontally centered string, multiline,
   * with color selection
   * @param g
   * @param s
   * @param height_ratio
   */
  protected void draw_multiline_string(Graphics2D g,String s,double height_ratio, GameFont gf )
  {
	  draw_multiline_string(g, s, height_ratio, 0,gf);
  }
  
  protected void draw_multiline_string(Graphics2D g,String s,double height_ratio, int y_offset, GameFont gf)
  {
		
		gf.write(g, s, (int)(getWidth() / 2.0),
				(int)(getHeight() * height_ratio),0,true,false,y_offset);
  }



  protected long get_elapsed_time()
  {
    return m_elapsed_time;
  }
  protected long get_state_elapsed_time()
  {
    return m_state_elapsed;
  }
  protected void reset_state_elapsed_time()
  {
	  m_state_elapsed = 0;
  }
  
  protected void stop_music()
  {
	  Mp3Play.stop();
  }
  
  protected void play_music(String music_file)
  {
	  if (GameOptions.instance().get_music_state())
	  {
		  Mp3Play.play(DirectoryBase.get_mp3_path() + music_file);
	  }
  }
  protected void no_elapsed_time()
  {
	m_state_elapsed -= m_elapsed_time;  
  }
  
  protected void set_next(GameState ns)
  {
	  m_next_state = ns;
  }

  public boolean is_escaped()
  {
    return m_escaped;
  }
  public void init_fonts()
  {
	  try
	  {    
	  WHITE_BIG_FONT = new GameFont(DirectoryBase.get_font_path()+"big_letters",GameOptions.instance().get_font_gfx_flavor(), Color.WHITE,0xFF393925);
	  GOLDEN_BIG_FONT = new GameFont(DirectoryBase.get_font_path()+"big_letters",GameOptions.instance().get_font_gfx_flavor() );
	  GOLDEN_SMALL_FONT = new GameFont(DirectoryBase.get_font_path()+"small_letters",GameOptions.instance().get_font_gfx_flavor() );
	  GODS_GOLDEN_FONT = new GameFont(DirectoryBase.get_font_path()+"gods_letters",GameOptions.instance().get_font_gfx_flavor() );
	  GODS_WHITE_FONT = new GameFont(DirectoryBase.get_font_path()+"gods_letters",GameOptions.instance().get_font_gfx_flavor(), Color.WHITE,0);
	  }
	  catch (IOException e)
	  {
	  }
  }
  protected GameState() 
  {
	  if (WHITE_BIG_FONT == null)
	  {
		  init_fonts();
	  }
	  
	  m_fadein_time = 0;
	  m_fadeout_time = 0;
	  m_faded_out_delay = 0;
	  m_maximum_duration = Long.MAX_VALUE;

  }


  protected void set_fadeinout_time(long fadein_time, long fadeout_time, long faded_out_delay) {
    m_fadein_time = fadein_time;
    m_fadeout_time = fadeout_time;
    m_faded_out_delay = faded_out_delay;
  }

  protected void set_maximum_duration(long maximum_duration) {
    m_maximum_duration = maximum_duration;

 }

  protected int getWidth() {
    return m_display_bounds.width;
  }

  protected int getHeight() {
    return m_display_bounds.height;
  }

 protected void fadeout() 
 {
    if (!m_fadeout)
    {
      m_fadeout_done = false;
      m_fadeout = true;
      m_fadeout_start_time = m_state_elapsed;
    }
  }

 protected void end_fadeout()
 {
	 m_fadeout = false;
	 m_fadeout_done = false;
 }
 protected void fadein()
 {
	 m_state_elapsed = 0;
	 m_fadeout = false;
	 m_fadeout_done = false;
 }
 
 protected boolean is_fadeout()
 {
	 return m_fadeout;
 }
  protected boolean is_fadein()
  {
    boolean rval = (m_fadein_time >= m_state_elapsed);
    if ((!rval) && m_fake_fadein)
    {
    	rval = true;
    	m_fake_fadein = false;
    }
    
    return rval;
  }


  public boolean is_fadeout_done() 
  {
    return m_fadeout_done;
  }



  public void init(GodsGame g)
  {

     m_game = g;
     m_background_color = Color.BLACK;
     m_display_bounds = g.get_display_bounds();
     m_fadeout = false;
     m_fadeout_done = false;
      m_escaped = false;
     m_next_state = null;
     m_color_percent = 0;
     if (m_fadein_time == 0)
     {
    	 m_color_percent = 1;
     }
     p_init();
     m_elapsed_time = 0;
     m_state_elapsed = 0;
 }
  
  protected void create_snapshot()
  {
 	 BufferedImage snapshot_image;
 	 String suffix = "view";
 	 
 	 Rectangle r = m_game.get_display_bounds();
 	 snapshot_image = new BufferedImage(r.width,r.height,BufferedImage.TYPE_INT_RGB);
 	 
 	 // hack to make believe a fadein
 	
 	 m_fake_fadein = true;
 	 render((Graphics2D)snapshot_image.getGraphics());
 	
 	 
 	 save_snapshot(snapshot_image,suffix);
  }
  
  protected void save_snapshot(BufferedImage snapshot_image, String suffix)
  {
 	 // now save the image
 		
 	 try
 	 {
 		File snapshots_dir = get_snapshots_dir();
		// Calculate max snapshot count by listing current files
 		String [] filelist = snapshots_dir.list((file, name) -> name.startsWith(suffix));
		int index = filelist.length;
		if (filelist.length > 0)
		{
			Arrays.sort(filelist);
			String last_item = filelist[filelist.length - 1];
			int dotidx = last_item.lastIndexOf('.');
			int uscidx = last_item.lastIndexOf('_');
			try
			{
				String s = last_item.substring(uscidx+1,dotidx);
				index = Integer.parseInt(s) + 1;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		// Pad snapshot count
 		String idx = "0"+index;
 		while (idx.length() < 4)
 		{
 			idx = "0" + idx;
 		}

 		ImageLoadSave.save_png(snapshot_image,snapshots_dir.getPath()+
 			File.separator+suffix+"_"+idx+".png");
 	 }
 	 catch (Exception e)
 	 {
 		 e.printStackTrace();
 	 }
  }

  public GameState update(long elapsed)
  {
	  m_elapsed_time = elapsed;
	  m_state_elapsed += elapsed;
	  
	  if (m_state_elapsed > m_maximum_duration - m_fadeout_time)
	  {
		  fadeout();
	  }
	  else if (m_game.is_key_pressed(KeyEvent.VK_F1))
	  {
		  create_snapshot();
	  }
	  
	  
	  if (m_error_message == null)
	  {
		  p_update();
	  }
	  
	  // avoid out of mem problem
	  GameState s = m_next_state;
	  m_next_state = null;
	  
	  return s;
  }
  protected void show_error(String msg)
  {
    m_error_message = msg;
  }

  private final static AlphaComposite NO_COMPOSITE = AlphaComposite.SrcOver;
  
  protected void clear_composite(Graphics2D g)
  {
	  g.setComposite(NO_COMPOSITE);
  }
  protected void set_composite(Graphics2D g, float percent, int type)
  {
	 g.setComposite(AlphaComposite.getInstance(type, percent));
  }
  protected void clear_screen(Graphics2D g)
  {
	  g.setColor(Color.BLACK);
	  g.fillRect(0,0,getWidth(),getHeight());
  }
  
  protected void set_color_ratio(float percent)
  {
	  if (!java.lang.Float.isNaN(percent))
	  {
		  m_color_percent = percent;
		  if (m_color_percent < 0)
		  {
			  m_color_percent = 0;
		  }
		  else if (m_color_percent > 1)
		  {
			  m_color_percent = 1;
		  }
	  }
  }
  
  public void render(Graphics2D g) 
  {
    if (m_error_message == null) 
    {
   	
    	if (m_fadeout) 
    	{
    		long delta_denom = m_fadeout_time - m_faded_out_delay;
    		if (delta_denom>0)
    		{
    			set_color_ratio(1 - ( (m_state_elapsed - m_fadeout_start_time) /
    					(float) (delta_denom)));
    		}
    		
    		if ((m_state_elapsed - m_fadeout_start_time) >= m_fadeout_time)
    		{
    			m_fadeout_done = true;
    		}


    		clear_screen(g);
    		set_composite(g, m_color_percent, AlphaComposite.SRC_OVER);

    	}
    	else
    	{
    		if (m_state_elapsed < m_fadein_time) 
    		{

    			// during fade-in
    			set_color_ratio((float) m_state_elapsed / m_fadein_time);
    			set_composite(g, m_color_percent, AlphaComposite.SRC_OVER);
    		}
    		else
    		{
    			if (m_color_percent < 1)
    			{
    				clear_screen(g);
    				set_composite(g, Math.max(m_color_percent, 0f), AlphaComposite.SRC_OVER);
    			}
    			else
    			{
    				clear_composite(g);
    			}
    		}


    	}
    	p_render(g);

    }

    if (m_error_message != null) 
    {
      if (!m_error_displayed) 
      {
        m_error_displayed = true;
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.RED);
        g.setFont(Font.decode("Arial-BOLD-14"));
        
        if (m_error_message.length() > 50)
        {
        	m_error_message = m_error_message.substring(0,50) + "\n" + m_error_message.substring(50);
        }
        m_error_message += "\n\nPress F10 to quit";
        /*g.setColor(Color.RED);
        GfxUtils.centered_draw_multiline_string(g, 
            m_error_message, getWidth() / 2,
            getHeight() / 2,0);*/
        System.err.println(m_error_message);
      
      }
    }
  }

  private File get_snapshots_dir()
  {
	File snapshots_dir = new File(DirectoryBase.get_data_path() + "snapshots");
	if (!snapshots_dir.exists())
	{
		snapshots_dir.mkdirs();
	}
	return snapshots_dir;
  }
}