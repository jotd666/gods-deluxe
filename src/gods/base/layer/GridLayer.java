package gods.base.layer;

import gods.base.*;

import gods.sys.ParameterParser;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public abstract class GridLayer implements Describable
{
	
	protected GfxFrame [][] m_data;
	protected int m_tile_width = 0;
	protected int m_tile_height = 0;
	protected int m_horiz_resolution = 0;
	protected int m_vert_resolution = 0;
	protected int m_nb_rows = 0;
	protected int m_nb_cols = 0;
	protected int m_pixel_width = 0;
	protected int m_pixel_height = 0;
	
	protected void init_grid()
	{
		m_data = new GfxFrame[m_nb_cols][];
	
		for (int j = 0; j < m_nb_cols; j++)
		{
			m_data[j] = new GfxFrame[m_nb_rows];
			for (int i = 0; i < m_nb_rows; i++)
			{
				m_data[j][i] = null;
			}
		}
	}
	
	public void set_dimension(int nb_rows, int nb_cols, 
			boolean on_left, boolean on_top, GfxPalette palette)
	{
		if ((nb_rows != m_nb_rows) || (nb_cols != m_nb_cols))
		{
			
			int old_nb_rows = m_nb_rows;
			int old_nb_cols = m_nb_cols;
			
			GfxFrame [][] old_data = m_data;
			
			m_nb_rows = nb_rows;
			m_nb_cols = nb_cols;
	
			int row_offset = on_top ? old_nb_rows - nb_rows : 0;
			int col_offset = on_left ? old_nb_cols - nb_cols : 0;
						
			
			int min_rows = Math.min(old_nb_rows, nb_rows);
			int min_cols = Math.min(old_nb_cols, nb_cols);
			
			init_grid();
			
			if (old_data != null)
			{
				for (int j = 0; j < m_nb_cols; j++)
				{
					if (j < min_cols)
					{
						for (int i = 0; i < m_nb_rows; i++)
						{
							if (i < min_rows)
							{
								int j_src = j + col_offset;
								int i_src = i + row_offset;
								
								if ((j_src >= 0) && (i_src >= 0))
								{
									m_data[j][i] = old_data[j_src][i_src];
								}
							}
						}
					}
				}
			}
			
			init_dim_params(palette);
			
		}
		
	}
	private void init_dim_params(GfxPalette palette)
	{
		Dimension d = palette.get_tile_dimension();
		
		m_tile_width = d.width;
		m_tile_height = d.height;
		m_horiz_resolution = m_tile_width;
		m_vert_resolution = m_tile_height;
		
		m_pixel_width =  m_nb_cols * m_tile_width;
		m_pixel_height = m_nb_rows * m_tile_height;
	}
	protected void load(ParameterParser fr, GfxPalette palette) throws IOException
	{
		fr.startBlockVerify(get_block_name());
		m_nb_rows = fr.readInteger("nb_rows");
		m_nb_cols = fr.readInteger("nb_cols");
	
		init_dim_params(palette);
		init_grid();

		String [] items = new String[m_nb_cols];
		
		for (int i = 0; i < m_nb_rows; i++)
		{
			int j = 0;
			
			fr.readVector("row", items);
			for (; j < m_nb_cols; j++)
			{

				String tile_name = items[j];

				if (tile_name != "null")
				{
					GfxFrameSet gfs = palette.lookup_frame_set(tile_name);
					if (gfs != null)
					{					
						m_data[j][i] = gfs.get_frame(1);
					}
				}
			}
		}



		fr.endBlockVerify();
	}

	protected GridLayer()
	{

	}

	public int get_tile_width()
	{
		return m_tile_width;
	}
	
	public int get_tile_height()
	{
		return m_tile_height;
	}
	

	public GridLayer(int tile_width, int tile_height, int nb_items_per_tile_h, int nb_items_per_tile_v)
	{
		m_tile_width = tile_width;
		m_tile_height = tile_height;
		m_horiz_resolution = tile_width / nb_items_per_tile_h;
		m_vert_resolution = tile_height / nb_items_per_tile_v;
	}

	// in-editor render
	
	public BufferedImage render(BufferedImage img_arg, Rectangle bounds_arg)
	{
		BufferedImage img = img_arg;
		Rectangle bounds = bounds_arg;
		
		if ((img == null) || 
				(img.getWidth() < m_pixel_width) || 
				(img.getHeight() < m_pixel_height))
		{
			if ((m_pixel_width > 0) && (m_pixel_height > 0))
			{
				img = new BufferedImage(m_pixel_width,m_pixel_height,BufferedImage.TYPE_INT_ARGB);
			}
		}
		
		if (bounds == null)
		{
			bounds = new Rectangle(0,0,m_pixel_width,m_pixel_height);
		}
		
		if (img != null)
		{
			Graphics g = img.getGraphics();
			g.setColor(Color.MAGENTA);
			g.fillRect(0, 0, img.getWidth(), img.getHeight());

			render(g,bounds,0,null);
		}
		
		return img;

	}
	
	// in-game tile render
	
	public void render(Graphics g, Rectangle bounds, int y_offset, BufferedImage copper_data)
	{

		int start_x = (bounds == null) ? 0 : (int)(bounds.x / m_horiz_resolution);
		int end_x = (bounds == null) ? m_nb_cols : (int)(Math.ceil((bounds.x+bounds.width) / (double)m_horiz_resolution));
		int start_y = (bounds == null) ? 0 : (int)(bounds.y / m_vert_resolution);
		int end_y = (bounds == null) ? m_nb_rows : (int)(Math.ceil((bounds.y+bounds.height) / (double)m_vert_resolution));

		if (end_y > m_nb_rows) { end_y = m_nb_rows; }
		if (end_x > m_nb_cols) { end_x = m_nb_cols; }
		if (start_x < 0) { start_x = 0; }
		if (start_y < 0) { start_y = 0; }
		
		for (int i = start_x; i < end_x; i++)
		{
			int sx1 = i * m_horiz_resolution;
			
			for (int j = start_y; j < end_y; j++)
			{
				int sy1 = j * m_vert_resolution;
				
				GfxFrame gf = m_data[i][j];
				
				if (gf != null)
				{
					if (gf.has_transparence() && (copper_data != null))
					{
						// draw copper stuff
						//  int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
						int y_src = sy1 - y_offset;
						if (y_src < 0)
						{
							y_src = 0;
						}
						else if (y_src >= copper_data.getHeight() - m_vert_resolution)
						{
							y_src = copper_data.getHeight() - m_vert_resolution;
						}
						
						g.drawImage(copper_data, sx1, sy1, sx1 + m_horiz_resolution, sy1 + m_vert_resolution,
								0, y_src, m_horiz_resolution, y_src + m_vert_resolution, null);
					}
					g.drawImage(gf.toImage(), sx1, sy1, null);
				}
			}
		}
	}
	
	public int get_nb_rows()
	{
		return m_nb_rows;
	}
	
	public int get_nb_cols()
	{
		return m_nb_cols;
	}
	
	/* (non-Javadoc)
	 * @see gods.base.grid.FrameLayer#serialize(gods.sys.ParameterParser)
	 */
	public void serialize(ParameterParser fw) throws IOException 
	{
		fw.startBlockWrite(get_block_name());
		fw.write("nb_rows", m_nb_rows);
		fw.write("nb_cols", m_nb_cols);
		String [] items = new String[m_nb_cols];
		
		for (int j = 0; j < m_nb_rows; j++)
		{
			
			for (int i = 0; i < m_nb_cols; i++)
			{
				GfxFrame gf = m_data[i][j];
				
				String tile_name = gf == null ? "null" : gf.get_source_set().get_name();
				
				items[i] = tile_name;
			}
			fw.write("row",items);
			
		}
		
		fw.endBlockWrite();
		
	}

	public int get_column(int x)
	{
		return x / m_horiz_resolution;
	}
	
	public int get_row(int y)
	{
		return y / m_vert_resolution;
	}
	

	public GfxFrame get(int x, int y)
	{
		int i = x / m_horiz_resolution;
		int j = y / m_vert_resolution;
		
		GfxFrame rval = null;
		
		if ((i < m_nb_cols) && (j < m_nb_rows) && (i >= 0) && (j >= 0))
		{
			rval = m_data[i][j];
		}
		
		return rval;
	}
	

	public void set(int x, int y,GfxFrame gf)
	{
		int i = x / m_horiz_resolution;
		int j = y / m_vert_resolution;

		if ((i < m_nb_cols) && (j < m_nb_rows) && (i >= 0) && (j >= 0))
		{
			m_data[i][j] = gf;
		}
	}
	
	
}
