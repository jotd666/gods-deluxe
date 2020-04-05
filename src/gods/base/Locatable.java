package gods.base;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Locatable
{
	protected double m_x = 0;
	protected double m_y = 0;
	protected int m_width = 0;
	protected int m_height = 0;
	
	protected boolean m_visible = true;
	
	public final int get_x() { return (int)m_x; }
	public final int get_y() { return (int)m_y; }
	public final void add_x(double dx)
	{
		m_x += dx;
	}
	public final void add_y(double dy)
	{
		m_y += dy;
	}
	
	public final int get_x_center() { return (int)m_x + m_width/2; }
	public final int get_y_center() { return (int)m_y + m_height/2; }
	
	public final int get_width() { return m_width; }
	public final int get_height() { return m_height; }
	
	public Locatable()
	{
	
	}
	
	public int surface()
	{
		return m_width * m_height;
	}
	
	public int compareTo(Object arg0) 
	{
		return toString().compareTo(arg0.toString());
	}
	public Locatable(int x, int y, int width, int height)
	{
		init(x,y,width,height);
	}
	
	public void set_coordinates(Locatable l)
	{
		set_coordinates(l.m_x,l.m_y);
	}
	
	public void set_coordinates(Locatable l, boolean center)
	{
		if (center)
		{
			set_coordinates(l.m_x - (m_width - l.m_width) / 2,
					l.m_y - (m_height - l.m_height) / 2);	
		}
		else
		{
			set_coordinates(l.m_x,l.m_y);
		}
	}

	public boolean set_coordinates(double x, double y)
	{
		boolean xc = set_x(x);
		boolean yc = set_y(y);
		
		return xc || yc;
	}
	
	public void draw_image(Graphics g, BufferedImage img)
	{
		g.drawImage(img,(int)m_x, (int)m_y,null);
	}
	
	public void draw_image(Graphics g, BufferedImage img, int x_offset, int y_offset)
	{
		g.drawImage(img,(int)m_x+x_offset, (int)m_y+y_offset,null);
	}
	
	public boolean set_x(double x)
	{
		double old_x = m_x;

		m_x = x;

		return (old_x != m_x);
	}

	public boolean set_y(double y)
	{
		double old_y = y;

		m_y = y;


		return (old_y != m_y);
	}

	public void init(int x, int y, int width, int height)
	{
		m_x = x;
		m_y = y;
		m_width = width;
		m_height = height;
	}
	public void get_bounds(Rectangle r)
	{
		r.x = (int)m_x;
		r.y = (int)m_y;
		r.width = m_width;
		r.height = m_height;
	}
	
	public void get_bounds(Rectangle r, int x_margin, int y_margin)
	{
		r.x = (int)m_x - x_margin;
		r.y = (int)m_y - y_margin;
		r.width = m_width + x_margin * 2;
		r.height = m_height + y_margin * 2;
	}
	
	public boolean is_visible()
	{
		return m_visible;
	}
	
	public boolean is_pickable()
	{
		return m_visible;
	}

	public int square_distance_to(Locatable go)
	{
		int dx = go.get_x_center() - get_x_center();
		int dy = go.get_y_center() - get_y_center();

		return dx*dx + dy*dy;
	}

}
