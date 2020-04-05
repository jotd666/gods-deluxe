package gods.game;

import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.IOException;

import gods.sys.*;
import gods.base.*;
import gods.sys.GameFont;

public class StatusBar implements Renderable
{
	private BufferedImage m_image;
	private BufferedImage m_text_image;
	private int m_y, m_text_y;
	private GameFont m_font = null;
	private int m_fade_in_counter = 0;
	private int m_stay_counter = 0;
	private double m_x = 0;
	private int m_text_width;
	private String m_text;
	private State m_state = State.INACTIVE;
	
	private enum State { INACTIVE, FADE_IN, STAY, SCROLL }
	
	private static final int FADE_IN_TIME = 500;
	private static final int STAY_TIME = 1000;
	private static final double SCROLL_SPEED = 0.1;

	 private final static AlphaComposite NO_COMPOSITE = AlphaComposite.SrcOver;
	 private static StatusBar m_status_bar = null;
	 
	 private void clear_composite(Graphics2D g)
	 {
		 g.setComposite(NO_COMPOSITE);
	 }
	 private void set_composite(Graphics2D g, float percent)
	 {
		 g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, percent));
	 }
	  
	 public static final StatusBar instance(int width, int height)
	 {
		 if (m_status_bar == null)
		 {
			 m_status_bar = new StatusBar(width,height);
		 }
		 
		 return m_status_bar;
	 }
	 
	 public void init_font()
	 {
		 try 
			{			
				m_font = new GameFont(DirectoryBase.get_font_path() + "small_letters",GameOptions.instance().get_font_gfx_flavor());
				
			} catch (IOException e) 
			{
				e.printStackTrace();
			}
	 }
	private StatusBar(int width, int height)
	{
		m_image = ImageLoadSave.load_png(DirectoryBase.get_images_path()+"status_bar");
		init_font();
		m_y = height - m_image.getHeight();

	}
	
	public int get_height()
	{
		return m_image.getHeight();
	}
	
	public void print(String text)
	{
		if (text.length()>0)
		{
			m_state = State.FADE_IN;
			m_fade_in_counter = 0;
			m_stay_counter = 0;
			m_text = Localizer.value(text,true).toUpperCase();
			Rectangle r = m_font.text_position(m_text, m_image.getWidth()/2, m_y - 3, 0, true, false);
			m_x = r.x;
			m_text_y = r.y;
			m_text_width = r.width;
			if (m_text_width >= m_image.getWidth())
			{
				m_x = 10;
			}
			m_text_image = new BufferedImage(r.width,r.height,BufferedImage.TYPE_INT_ARGB);

			Graphics2D g = (Graphics2D)m_text_image.getGraphics();

			m_font.write_line(g, m_text, 0, 0, -2, false, false);

			int nbh = 4;

			int h4 = r.height / nbh;

			g.setColor(Color.BLACK);

			set_composite(g,0.5f);

			g.fillRect(0, 0, r.width, r.height / 3);
			g.fillRect(0, (nbh-1)*h4, r.width, h4);


			for (int x = 0; x < m_text_image.getWidth(); x++)
			{
				for (int y = 0; y < m_text_image.getHeight(); y++)
				{
					int rgb = m_text_image.getRGB(x,y);
					// if alpha channeled black, set alpha channel to zero
					if ((rgb & 0xFFFFFF) == 0)
					{
						m_text_image.setRGB(x, y, 0);
					}

				}
			}
		}
		else
		{
			m_text_image = null;
		}
	}
	
	public void update(long elapsed_time)
	{
		switch (m_state)
		{
		case INACTIVE:
			break;
		case FADE_IN:
		
				m_fade_in_counter += elapsed_time;
				if (m_fade_in_counter >= FADE_IN_TIME)
				{
					m_state = State.STAY;
				}
			break;
		case STAY:
			m_stay_counter += elapsed_time;
			if (m_stay_counter >= STAY_TIME)
			{
				m_state = State.SCROLL;
			}
			break;
		case SCROLL:
			m_x -= SCROLL_SPEED * elapsed_time;
			if (m_x + m_text_width < 0)
			{
				m_state = State.INACTIVE;
			}
			break;
		}
			
	}
	
	public void render(Graphics2D g)
	{
		Composite c = g.getComposite();
		
		g.drawImage(m_image,0,m_y,null);
		switch (m_state)
		{
		case INACTIVE:
			break;
		case STAY:
		case SCROLL:
			if (m_text_image != null)
			{
			clear_composite(g);
			g.drawImage(m_text_image,(int)m_x, m_text_y,null);
			}
			break;
		case FADE_IN:
			if (m_text_image != null)
			{
			set_composite(g,m_fade_in_counter / (float)FADE_IN_TIME);
			g.drawImage(m_text_image,(int)m_x, m_text_y,null);
			}
			break;
		}
		
		g.setComposite(c);
	}
}
