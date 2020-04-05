package gods.base.layer;


import gods.base.*;
import gods.sys.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.awt.*;

public class TileGrid extends GridLayer
{
	private int m_leftover_counter = 0;
	private GfxPalette m_palette = null;
	
	private int [][] m_wall_damage;
	
	public boolean hit_block(int x, int y, int power)
	{
		int i = get_column(x);
		int j = get_row(y);
		boolean rval = false;
		
		try
		{
			GfxFrame gf = m_data[i][j];

			if ((gf != null) && 
					(gf.get_source_set().get_type() == GfxFrameSet.Type.breakable_block))
			{
				m_wall_damage[i][j] -= power;

				if (m_wall_damage[i][j] <= 0)
				{
					m_data[i][j] = gf.get_frame_behind();
					rval = true;
				}

			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			// sometimes happens at least in test levels
		}

		return rval;
	}
	
	@Override
	public void init_grid()
	{
		super.init_grid();
		m_wall_damage = new int[m_nb_cols][];
		
		for (int i = 0; i < m_nb_cols; i++)
		{
			m_wall_damage[i] = new int[m_nb_rows];
			for (int j = 0; j < m_nb_rows; j++)
			{
				m_wall_damage[i][j] = 20; // power for breakable blocks
			}
		}		
	}
	
	public String get_block_name() 
	{		
		return "TILE_GRID";
	}
	
	public GfxPalette get_palette()
	{
		return m_palette;
	}
	
	public TileGrid(ParameterParser fr, GfxPalette palette) throws IOException
	{
		m_leftover_counter = 0;
		m_palette = palette;
		load(fr,palette);
	}
	

	public TileGrid(int tile_width, int tile_height, GfxPalette palette)
	{
		super(tile_width, tile_height, 1, 1);
		m_palette = palette;
		m_leftover_counter = 0;
	}
	

	public int get_rounded_x(double x)
	{
		int rval = (int)(Math.round(x / m_horiz_resolution) * m_horiz_resolution);
				
		return rval;
	}
	public int get_rounded_y(double y)
	{
		int rval = (int)(Math.round(y / m_vert_resolution) * m_vert_resolution);
				
		return rval;
	}

	public int get_rounded_y(double y, boolean ceil)
	{
		double rval = ceil ? Math.ceil(y / m_vert_resolution) : Math.floor(y / m_vert_resolution);
			
		return (int)(rval * m_vert_resolution);
	}
	
	public int get_rounded_x(double x, boolean ceil)
	{
		double rval = ceil ? Math.ceil(x / m_horiz_resolution) : Math.floor(x / m_horiz_resolution);
		
		return (int)(rval * m_horiz_resolution);
	}
	
 	public boolean is_over_ladder(int x, int y, int h)
	{
		boolean rval = x >= 0;
		
		if (rval)
		{
			int nb_tiles = h / m_tile_height;
			
			if (h % m_tile_height != 0)
			{
				nb_tiles++;
			}		
			
			// from top to bottom
			
			for (int i = 0; i < nb_tiles; i++)
			{
				//int yc = from_top ? m_tile_height * i : (nb_tiles - i - 1) * m_tile_height;
				
				int yc = m_tile_height * i;
				
				GfxFrame gf = get(x, y + yc);
				
				if ((gf != null) && 
						(gf.get_source_set().get_type() != GfxFrameSet.Type.ladder))
				{
					rval = false;
					break;
				}
			}
		}
		return rval;		
	}
 	
	public boolean is_vertical_way_blocked(int x, int y)
	{
		boolean rval = y < 0;
		if (!rval)
		{
			/*
			int nb_tiles = w / m_tile_width;

			if (w % m_tile_width != 0)
			{
				nb_tiles++;
			}*/
			int nb_tiles = 1;
			
			for (int i = 0; i < nb_tiles; i++)
			{
				GfxFrame gf = get(x + m_tile_width * i, y);
				GfxFrameSet.Type type = null;
				
				if (gf != null) type = gf.get_source_set().get_type();

				if ((gf == null) || (type == GfxFrameSet.Type.foreground_tile) || 
						(type == GfxFrameSet.Type.breakable_block))
				{
					rval = true;
					break;
				}
			}
		}
		return rval;
		}
	
	
	public boolean is_lateral_way_blocked(int x, int y, int h)
	{
		boolean rval = x < 0;
		if (!rval)
		{
			int nb_tiles = h / m_tile_height;
			if (h % m_tile_height != 0)
			{
				nb_tiles++;
			}		
			for (int i = 0; i < nb_tiles; i++)
			{
				GfxFrame gf = get(x, y + m_tile_height * i);
				GfxFrameSet.Type type = null;
				
				if (gf != null) type = gf.get_source_set().get_type();

				if ((gf == null) || (type == GfxFrameSet.Type.foreground_tile) || 
						(type == GfxFrameSet.Type.breakable_block))
				{
					rval = true;
					break;
				}
			}
		}
		return rval;
	}
	
	private GfxFrame find_matching_tile(BufferedImage tile_buffer, Collection<GfxFrameSet> gfs_list,
			int rgb_tolerance, boolean monochrome, int margin)
	{		
		int transparent = GfxPalette.BACKGROUND_COLOR.getRGB() | 0xFF000000;
		int max_w = (tile_buffer.getWidth()-margin);
		int max_h = (tile_buffer.getHeight()-margin);
		
		for (GfxFrameSet gfs : gfs_list)
		{	
			for (GfxFrame gf : gfs.get_frames())
			{
				
				BufferedImage bi = gf.toImage();
				
				boolean same = true;
				
				if ((bi.getWidth() >= tile_buffer.getWidth()) && (bi.getHeight() >= tile_buffer.getHeight()))
				{					
					for (int i = margin; (i < max_w) && same; i++)
					{
						for (int j = margin; (j < max_h) && same; j++)
						{
							try
							{
								int rgb_buffer = tile_buffer.getRGB(i, j);
								int rgb_tile = bi.getRGB(i, j);

								// transparency test first
								
								same = (rgb_buffer == transparent) && (rgb_tile == 0);
								
								if (!same)
								{
									if (!monochrome)
									{
										int mask = 0xff;
										same = true;
										for (int k = 0; k < 3 && same; k++)
										{
											int shift = k*8;
											int c1 = ((rgb_buffer & mask) >> shift);
											int c2 = ((rgb_tile & mask) >> shift);
											same = (Math.abs(c1-c2) <= rgb_tolerance);

											mask = mask << 8;							
										}
									}
									else
									{
										// try to match using only gray values
										int gray_tile = MiscUtils.get_gray_average_value(rgb_tile);
										int gray_buffer = MiscUtils.get_gray_average_value(rgb_buffer);

										same = Math.abs(gray_tile - gray_buffer) < rgb_tolerance;
									}

								}
							}
							catch (ArrayIndexOutOfBoundsException e)
							{
								System.out.println(gf.toString()+": out of bounds: "+i+","+j);
								throw(e);
							}
						}
					}

					if (same)
					{
						return gf;
					}
				}
			}
		}
		
		return null;
	}
	
	
	private void debug(String m)
	{
		System.out.println(this.getClass().getName()+": "+m);
	}
	
	public BufferedImage import_from_image(BufferedImage map_image, 
			GfxPalette palette, int rgb_tolerance, boolean allow_margin)
	{		
		BufferedImage leftover_tiles = null;
		
		
		m_pixel_width = (map_image.getWidth() / m_tile_width) * m_tile_width;
		m_pixel_height = (map_image.getHeight() / m_tile_height) * m_tile_height;
		
		m_nb_cols = map_image.getWidth() / m_horiz_resolution;
		m_nb_rows = map_image.getHeight() / m_vert_resolution;

		debug("nb_cols = "+m_nb_cols+", nb_rows = "+m_nb_rows);

		init_grid();
		m_leftover_counter = 0;

		Collection<GfxFrameSet> gfs = palette.lookup_by_type(GfxFrameSet.TILES);
		
		Collection<BufferedImage> leftovers = new LinkedList<BufferedImage>();
		
		
		BufferedImage tile_buffer = null;
		
		for (int i = 0; i < m_nb_cols; i++)
		{
			int sx1 = i * m_horiz_resolution;
			int sx2 = sx1 + m_tile_width;
			
			for (int j = 0; j < m_nb_rows; j++)
			{
				int sy1 = j * m_vert_resolution;
				int sy2 = sy1 + m_tile_height;
				
				if (tile_buffer == null)
				{
					tile_buffer = new BufferedImage(m_tile_width,m_tile_height,BufferedImage.TYPE_INT_RGB);
				}
								
				tile_buffer.getGraphics().drawImage(map_image, 0, 0, m_tile_width, m_tile_height, 
						sx1, sy1, sx2, sy2, null);
				
				GfxFrame gf = null;
				
				// increase rgb tolerance progressively to avoid matching the wrong tile
				// this way, tolerance can be high without a risk of mismatch
				
				int tolerance = 0;
				int tolerance_step = rgb_tolerance / 4;
				
				do
				{
					gf = find_matching_tile(tile_buffer, gfs, tolerance, false, 0);
					tolerance += tolerance_step;
				}
				while ((gf == null) && (tolerance <= rgb_tolerance));
				
				
				if (gf != null)
				{
					m_data[i][j] = gf;
				}
				else
				{
					gf = find_matching_tile(tile_buffer, gfs, rgb_tolerance/2, true, 0);

					if (gf != null)
					{
						m_data[i][j] = gf;
					}
					else
					{
						if (allow_margin)
						{
							gf = find_matching_tile(tile_buffer, gfs, rgb_tolerance, false, 1);
						}
						if (gf != null)
						{
							m_data[i][j] = gf;
						}
						else
						{
							debug("unknown tile at x="+sx1+", y="+sy1);
							GfxFrameSet gfs_leftovers = new GfxFrameSet("leftover_"+m_leftover_counter,GfxFrameSet.Type.background_tile);

							m_leftover_counter++;

							gf = new GfxFrame(gfs_leftovers,tile_buffer,
									new Rectangle(0,0,m_tile_width,m_tile_height));

							gfs_leftovers.add(gf);

							gfs.add(gfs_leftovers);

							m_data[i][j] = gf;

							// tile not found: put in in the leftovers list
							leftovers.add(tile_buffer);
							tile_buffer = null; // so it is not written over
						}
					}

				}
			}
		}
		
		int nb_leftovers = leftovers.size();
		
		if (nb_leftovers > 0)
		{
			int nb_pics_per_col = Math.min(nb_leftovers, 3);
			int nb_pics_per_row = (nb_leftovers / nb_pics_per_col);
			int w = nb_pics_per_row * m_tile_width;
			int h = nb_pics_per_col * m_tile_height;
			
			leftover_tiles = new BufferedImage(w+(2*nb_pics_per_row - 1),
					h+(2*nb_pics_per_col - 1),
					BufferedImage.TYPE_INT_RGB);
			
			Graphics g = leftover_tiles.getGraphics();
			
			g.setColor(GfxPalette.FRAME_COLOR_1);
			
			g.fillRect(0,0,leftover_tiles.getWidth(),leftover_tiles.getHeight());
			
			int x = 1, y = 1;
			
			for (BufferedImage bi : leftovers)				
			{
				g.drawImage(bi, x, y, null);
				x += m_tile_width+1;
				if (x >= w)
				{
					x = 1;
					y += m_tile_height+1;
				}
			}
		}
		
		return leftover_tiles;
	}
	


	
}