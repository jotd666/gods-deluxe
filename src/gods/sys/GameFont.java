package gods.sys;



import java.awt.image.*;
import java.awt.*;
import java.io.*;
	
public class GameFont
{
	private int m_x_step,m_y_step,m_space_width;
	private static final int NB_LETTERS = 1<<15;
	private Letter[] m_lookup_table = new Letter[NB_LETTERS];
	private boolean m_fixed_font = false;
	
	private class Letter
	{
		private BufferedImage image;
		public int width;
		
		public void draw(Graphics g, int dx, int dy)
		{
			g.drawImage(image, dx, dy, null);
		}
		
		public Letter(BufferedImage source, int x, int y)
		{
			image = new BufferedImage(m_x_step,m_y_step,BufferedImage.TYPE_INT_ARGB);
			
			// copy letter
			image.getGraphics().drawImage(source,0,0,m_x_step,m_y_step,x,y,x+m_x_step,y+m_y_step,null);
			
			// compute best width
			
			width = -1;
			if (!m_fixed_font)
			{
				for (int i = 0; i < m_x_step; i++)
				{
					for (int j = 0; j < m_y_step; j++)
					{
						int rgb = image.getRGB(i,j);
						if (rgb != 0)
						{
							if (i > width)
							{
								width = i;
							}

						}
					}
				}
			}
			if (width == -1)
			{
				width = m_x_step / 2;				
			}
			else
			{
				width++;
			}
		}
	}
	
	private class Row
	{
		public String text;
		public Letter [] letter;
		
		public Row(BufferedImage image, String text, int index)
		{
			int y = index * m_y_step;
			this.text = text;
			
			letter = new Letter[text.length()];
			for (int i = 0;i < letter.length; i++)
			{
				letter[i] = new Letter(image,i*m_x_step,y);
			}
		}
		
		public int get_space_width()
		{
			int rval = m_x_step;
			for (Letter l : letter)
			{
				if (l.width < rval)
				{
					rval = l.width;
				}
			}
			return rval;
		}
		public int lookup(int c)
		{
			return text.indexOf(c);			
		}
	}
	
	
	public GameFont(String prefix, String gfx_flavor) throws IOException
	{
		this(prefix,gfx_flavor,null,null,false);
	}
	public GameFont(String prefix, String gfx_flavor, boolean fixed_font) throws IOException
	{
		this(prefix,gfx_flavor,null,null,fixed_font);
	}

		
	public GameFont(String prefix,String gfx_flavor,Color original_white, Color white_replacement, boolean fixed_font) throws IOException
	{
		File p = new File(prefix);
		
		BufferedImage image = ImageLoadSave.load_png(p.getParent() + File.separator + gfx_flavor + File.separator + p.getName(), Color.BLUE);
		
		m_fixed_font = fixed_font;
		if (white_replacement != null)
		{
			int orig_rgb = original_white.getRGB();
			int replaced_rgb = white_replacement.getRGB();
			
			for (int i = 0; i < image.getWidth(); i++)
			{
				for (int j = 0; j < image.getHeight(); j++)
				{
					int rgb = image.getRGB(i, j);
					
					if (rgb == orig_rgb)
					{
						image.setRGB(i, j, replaced_rgb);
					}
				}
			}
		}
		
		init(prefix,image);
		
	}
	
	public GameFont(String prefix,String gfx_flavor,Color monochrome_replacement,int border_rgb) throws IOException
	{
		m_fixed_font = false;
		
		File p = new File(prefix);
		BufferedImage image = ImageLoadSave.load_png(p.getParent() + File.separator + gfx_flavor + File.separator + p.getName(), Color.BLUE);
		
		int orig_rgb = 0;
		int replaced_rgb = monochrome_replacement.getRGB();

		for (int i = 0; i < image.getWidth(); i++)
		{
			for (int j = 0; j < image.getHeight(); j++)
			{
				int rgb = image.getRGB(i, j);

				if (rgb == border_rgb)
				{
					image.setRGB(i, j, orig_rgb);
				}
				else if (rgb != orig_rgb)
				{
					image.setRGB(i, j, replaced_rgb);
				}
				
			}
		}

		init(prefix,image);
	}
	
	private void init(String prefix, BufferedImage image) throws IOException
	{
		ParameterParser fr = ParameterParser.open(prefix+".fnt");
		fr.startBlockVerify("GAME_FONT");
		int nb_rows = fr.readInteger("nb_rows");
		m_x_step = fr.readInteger("x_step");
		m_y_step = fr.readInteger("y_step");
		
		Row [] rows = new Row[nb_rows];
		
		m_space_width = m_x_step;
		for (int i = 0; i < nb_rows;i++)
		{
			rows[i] = new Row(image,fr.readString("text"),i);
			int w = rows[i].get_space_width();
			if (w < m_space_width)
			{
				m_space_width = w;
			}
		}
		fr.endBlockVerify();
		
		// create table
		
		for (int i = 0; i < NB_LETTERS; i++)
		{
			m_lookup_table[i] = lookup_xy(rows,i);
		}
	}
	private Letter lookup_xy(Row [] rows,int c)
	{
		Letter rval = null;
		int pos = 0;
		int count = 0;
		
		for (Row r : rows)
		{
			pos = r.lookup(c);
			
			if (pos != -1)
			{
				break;
			}
			
			count++;
		}
		
		if (pos != -1)
		{
			rval = rows[count].letter[pos];
		}
		return rval;
	}
	
	public int get_letter_width()
	{
		return m_x_step;
	}
	public int get_letter_height()
	{
		return m_y_step;
	}
	
	public Rectangle text_position(String text, int x, int y, int letter_horizontal_offset, boolean horiz_centered, boolean vertical_centered)
	{
		int start_x = 0;
		int current_x = start_x;
		
		int len = text.length();
		
		for (int i = 0; i < len; i++)
		{			
			int c = text.charAt(i);
			if (c == ' ')
			{
				current_x += m_space_width;
			}
			else
			{
				Letter l = m_lookup_table[c];
				if (l != null)
				{
					current_x += l.width + letter_horizontal_offset;
				}
				else
				{
					current_x += m_space_width;
				}
			}
			
		}		
		
		int w = current_x-start_x;
		int h = m_y_step;
		

		return new Rectangle(horiz_centered ? x-w/2 : x,vertical_centered ? y-h/2 : y,w,h);
	
	}
	
	public Rectangle write_line(Graphics2D g, String text, int x, int y, int letter_horizontal_offset,boolean horiz_centered, boolean vertical_centered)
	{
		Rectangle r = text_position(text, x, y, letter_horizontal_offset, horiz_centered, vertical_centered);
		write_line(g,text,letter_horizontal_offset,r);
		return r;
	}
	
	public int write(Graphics2D g, String text, int x, int y, int letter_horizontal_offset, boolean horiz_centered, boolean vertical_centered, int y_offset)
	{
		String [] lines = text.split("\n");
		Rectangle r2 = new Rectangle(x,y,0,0);
		for (String l : lines)
		{
			r2.y += r2.height;
			r2 = text_position(l, x, r2.y, letter_horizontal_offset, horiz_centered, vertical_centered);
			write_line(g,l,letter_horizontal_offset,r2);
			r2.y += y_offset;
		}
		return r2.y - y;
	}
	
	public void write_line(Graphics2D g, String text, int letter_horizontal_offset, Rectangle r)
	{	
		int current_x = r.x;
		int current_y = r.y;
		
		int len = text.length();
		
		for (int i = 0; i < len; i++)
		{

			int c = text.charAt(i);
			Letter l;
			// Brian Wheeler: fix for when lookup table index failure
			try {
				l = m_lookup_table[c];
			} catch(Exception e) {
				l = null;
			}
			
			if (l != null)
			{
				l.draw(g, current_x, current_y);
			}
			
			// move
			if (c == ' ')
			{
				current_x += m_space_width;
			}
			else
			{
				if (l != null)
				{
					current_x += l.width + letter_horizontal_offset;
				}
				else
				{
					current_x += m_space_width;
				}
			}
			

		}
		
	}
}
