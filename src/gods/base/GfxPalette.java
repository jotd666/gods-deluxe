package gods.base;

import gods.sys.*;

import java.util.*;
import java.io.File;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.*;


public class GfxPalette implements EditorRenderable, EditableData
{
	private static HashMap<String, GfxPalette> m_cache = new HashMap<String, GfxPalette>();
	
	private boolean m_modified = false;
	
	private boolean m_view_defined = true;
	private boolean m_view_undefined = true;

	private BufferedImage m_1x, m_2x = null;
	private TreeSet<GfxFrameSet> m_frame_set_list = new TreeSet<GfxFrameSet>(new GfxFrameSetLocationComparator());
	private HashMap<GfxFrameSet,GfxFrameSet> m_symmetrical_frame_set_list = new HashMap<GfxFrameSet,GfxFrameSet>();
	private String m_project_file;
	
	public static Color BACKGROUND_COLOR = Color.MAGENTA;
	public static Color FRAME_COLOR_1 = new Color(206,206,214);
	public static Color FRAME_COLOR_2 = new Color(202,202,214);
	
	public void export(String s)
	{
		// not implemented yet
	}
	
	public void toggle_view_defined()
	{
		m_view_defined = !m_view_defined;
	}
	public void toggle_view_undefined()
	{
		m_view_undefined = !m_view_undefined;
	}
	
	public void editor_render(Graphics g) 
	{
		if (m_view_defined)
		{
			for (GfxFrameSet gfs : m_frame_set_list)
			{
				gfs.editor_render(g);
			}
		}
		else
		{
			g.setColor(Color.BLACK);
			
			for (GfxFrameSet gfs : m_frame_set_list)
			{
				for (GfxFrame gf : gfs.get_frames())
				{
					Rectangle r = gf.get_bounds();
					
					g.fillRect(r.x, r.y, r.width, r.height);
				}
			}
			
		}
	}
	
	public Collection<GfxFrameSet> get_frame_sets()
	{
		return m_frame_set_list;
	}
	public void add(GfxFrameSet gfs)
	{
		m_frame_set_list.add(gfs);
		m_modified = true;
	}
	
	public void remove(GfxFrameSet gfs)
	{
		m_frame_set_list.remove(gfs);
	}
	
	public GfxFrameSet get_left_frame_set(GfxFrameSet other)
	{
		GfxFrameSet rval = m_symmetrical_frame_set_list.get(other);
		
		// cache to avoid too much image processing during game
		
		if (rval == null)
		{
			rval = new GfxFrameSet(other,GfxFrame.SymmetryType.mirror_left);
			m_symmetrical_frame_set_list.put(other, rval);
		}
		
		return rval;
	}
	
	public GfxFrame get_left_frame(GfxFrame other)
	{
		GfxFrame rval;
		
		rval = new GfxFrame(other,GfxFrame.SymmetryType.mirror_left);
		
		return rval;
	}
	
	public GfxFrameSet lookup_frame_set(int x, int y)
	{
		GfxFrameSet rval = null;
		
		for (GfxFrameSet gfs : m_frame_set_list)
		{
			if (gfs.lookup_frame(x,y) != null)
			{
				rval = gfs;
				break;
			}
		}
		
		return rval;
	}
	
	public GfxFrameSet lookup_frame_set(String name)
	{
		GfxFrameSet rval = null;
		
		for (GfxFrameSet gfs : m_frame_set_list)
		{
			if (gfs.get_name().equals(name))
			{
				rval = gfs;
				break;
			}
		}
		
		return rval;
	}
	
	public Dimension get_tile_dimension()
	{
		Dimension rval = null;
		
		for (GfxFrameSet gfs : m_frame_set_list)
		{
			switch (gfs.get_type())
			{
			case background_tile:
			case foreground_tile:

				GfxFrame gf = gfs.get_frames().elementAt(0);
				rval = new Dimension();
				rval.width = gf.get_width();
				rval.height = gf.get_height();
				break;
			}
			
			if (rval != null) break;
		}
		
		return rval;
	}
	
	public Collection<GfxFrameSet> lookup_by_type(GfxFrameSet.Type [] matching_types)
	{
		Set<GfxFrameSet> rval = new TreeSet<GfxFrameSet>();
		
		for (GfxFrameSet.Type t : matching_types)
		{
			lookup_by_type(t,rval);
		}
		return rval;
	}
	public Collection<GfxFrameSet> lookup_by_type(GfxFrameSet.Type matching_type)
	{
		Set<GfxFrameSet> rval = new TreeSet<GfxFrameSet>();
		lookup_by_type(matching_type,rval);
		
		return rval;
	}
	
	public void lookup_by_type(GfxFrameSet.Type matching_type,Collection<GfxFrameSet> list)
	{
		
		for (GfxFrameSet gfs : m_frame_set_list)
		{
			if (gfs.get_type() == matching_type)
			{
				list.add(gfs);
			}
		}
				
	}
	
	private void reset()
	{
		m_modified = false;
		m_frame_set_list.clear();
		m_project_file = null;
		m_1x = null;
		m_2x = null;
	}
	public void new_project(String image_file)
	{
		reset();
		
		m_project_file = DirectoryBase.rework_path(image_file, DirectoryBase.get_tiles_path() + 
				"1x" + File.separator, true, true);
	}
	
	public BufferedImage get_2x_image()
	{
		if ((m_2x == null) && (m_project_file != null))
		{
			String suffix = GameOptions.instance().get_tileset_type().toString().toLowerCase();
			//if (suffix.equals(""))
			m_2x = ImageLoadSave.load_png(DirectoryBase.get_tiles_path() +"2x"+File.separator+suffix+File.separator+m_project_file,BACKGROUND_COLOR);
		}
		return m_2x;
	}
	
	public BufferedImage get_1x_image()
	{
		if ((m_1x == null) && (m_project_file != null))
		{
			m_1x = ImageLoadSave.load_png(DirectoryBase.get_tiles_path() +"1x"+File.separator+m_project_file, BACKGROUND_COLOR);
		}
		return m_1x;
	}
	
	public String get_project_file()
	{
		return m_project_file;
	}
	
	public static void clear_cache()
	{
		m_cache.clear();
		System.gc();
	}
	
	@SuppressWarnings("unchecked")
	public void load(String project_file, GfxMode gfx_mode) throws java.io.IOException
	{		
		boolean use_cache = (gfx_mode != GfxMode.EDITOR) && GameOptions.instance().with_level_cache;
		
		GfxPalette cached = use_cache ? m_cache.get(project_file) : null;
		
		if (cached == null)
		{
			reset();

			set_project_file(project_file);

			String s = get_project_filename();

			ParameterParser fr = ParameterParser.open(s);

			fr.startBlockVerify(get_block_name());

			int nb_frame_sets = fr.readInteger("nb_frame_sets");

			BufferedImage img = null;

			boolean game_scale = true;
			boolean rework_mosaics = true;

			switch (gfx_mode)
			{
			case EDITOR:
				img = get_1x_image();
				game_scale = false;
				rework_mosaics = false;
				break;
			case ORIGINAL_GAME:
			{
				BufferedImage img_1x = get_1x_image();

				// scale the image

				AffineTransform at = new AffineTransform();
				at.scale(2.0, 2.0);
				BufferedImageOp biop = null;
				img = new BufferedImage(img_1x.getWidth()*2,img_1x.getHeight()*2,BufferedImage.TYPE_INT_ARGB);

				biop = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
				((Graphics2D)img.getGraphics()).drawImage(img_1x, biop, 0, 0);

				rework_mosaics = false;

			}
			break;
			case ORIGINAL_GAME_SCALE_2X:
			{
				BufferedImage img_1x = get_1x_image();

				ImageScale2x is2x = new ImageScale2x(img_1x);

				img = is2x.getScaledImage();
			}
			break;

			case REWORKED_GAME:
				img = get_2x_image();
				break;
			}

			for (int i = 0; i < nb_frame_sets; i++)
			{			
				GfxFrameSet gfs = new GfxFrameSet(img,fr,game_scale,rework_mosaics);

				if (gfx_mode == GfxMode.ORIGINAL_GAME_SCALE_2X)
				{
					// rework to avoid rounded corners

					// remove FRAME_COLOR_1 & FRAME_COLOR_2 from image
					// to avoid that scale2x create spurious dots scale the image
					// -> we replace it by the color of the neighbours

					for (int j = 0; j < gfs.get_nb_frames(); j++)
					{
						BufferedImage tile = gfs.get_frame(j+1).toImage();

						for (int x = 0; x < tile.getWidth(); x+= tile.getWidth()-1)
						{

							for (int y = 0; y < tile.getHeight(); y+= tile.getHeight()-1)
							{
								int c = tile.getRGB(x, y);
								if (c == FRAME_COLOR_1.getRGB() || c == FRAME_COLOR_2.getRGB())
								{
									// get nearest color

									int nx = (x == 0) ? 1 : tile.getWidth()-2;
									int ny = (y == 0) ? 1 : tile.getHeight()-2;

									tile.setRGB(x,y,tile.getRGB(nx, ny));
								}
							}
						}
					}
				}
				add(gfs);
			}

			fr.endBlockVerify();

			if (!game_scale)
			{
				// remove unreachable tiles

				Collection<GfxFrameSet> clone = (Collection<GfxFrameSet>)m_frame_set_list.clone();
				Collection<GfxFrameSet> frame_set_list_clone = clone;

				for (GfxFrameSet gfs : frame_set_list_clone)
				{
					Vector<GfxFrame> gf_clone = (Vector<GfxFrame>)gfs.get_frames().clone();

					for (GfxFrame gf : gf_clone)
					{
						Rectangle r = compute_gfx_frame(gf.get_src_x(), gf.get_src_y());

						if ((r != null) && ( (r.x != gf.get_src_x() || (r.y != gf.get_src_y()))))
						{
							// not reachable: remove
							gfs.remove(gf);
						}
					}

					if (gfs.get_nb_frames() == 0)
					{
						// all frames have been removed: remove frame set

						m_frame_set_list.remove(gfs);
					}
				}
			}
			
			if (use_cache) 
			{
				m_cache.put(project_file, this); 
			}
		}
		else
		{
			// cached: copy
			
			set_project_file(project_file);
			
			m_frame_set_list = cached.m_frame_set_list;
			m_project_file = cached.m_project_file;
			m_1x = cached.m_1x;
			m_2x = cached.m_2x;
		}
		
		m_modified = false;
	}
	
	public String get_block_name()
	{
		return "GFX_PALETTE";
	}
	public boolean save() throws java.io.IOException
	{
		boolean rval = false;
		String s = get_project_filename();
		ParameterParser fw = ParameterParser.create(s);

		try
		{
			serialize(fw);

			fw.close();
			fw = null;
			
			rval = true;
		}
		catch (RuntimeException e)
		{
			if (fw != null)
			{
				fw.close();
			}
		}

		m_modified = false;
		
		return rval;
	}
	
	public void serialize(ParameterParser fw) throws java.io.IOException
	{
		fw.startBlockWrite(get_block_name());
		
		fw.write("nb_frame_sets", m_frame_set_list.size());
		
		for (GfxFrameSet gfs : m_frame_set_list)
		{
			gfs.serialize(fw);
		}
		fw.endBlockWrite();
	}
	
	public boolean is_modified()
	{
		return m_modified;
	}
	
	public void set_modified(boolean m)
	{
		m_modified = m;
	}
	
	public void set_project_file(String project_file)
	{
		m_project_file = DirectoryBase.rework_path(project_file, 
				DirectoryBase.get_tiles_path(), true, true);
	}
	
	public Rectangle compute_gfx_frame(int x, int y)
	{
		BufferedImage img = get_1x_image();
		Rectangle rval = null;
		
		if (img != null)
		{
			int w = img.getWidth();
			int h = img.getHeight();

			int frame_rgb_1 = FRAME_COLOR_1.getRGB();
			int frame_rgb_2 = FRAME_COLOR_2.getRGB();

			// lookup left

			int i;
			boolean bound_found;

			i = x;
			bound_found = false;

			while (i >= 0 && !bound_found)
			{
				int rgb = img.getRGB(i, y);
				if ((rgb == frame_rgb_1)||(rgb == frame_rgb_2))
				{
					bound_found = true;
				}
				else
				{
					i--;
				}
			}

			int min_x = i + 1;

			// lookup right

			i = x;
			bound_found = false;

			while (i < w && !bound_found)
			{
				int rgb = img.getRGB(i, y);
				if ((rgb == frame_rgb_1)||(rgb == frame_rgb_2))
				{
					bound_found = true;
				}
				else
				{
					i++;
				}
			}

			int max_x = i;

			// lookup up

			int j;

			j = y;
			bound_found = false;

			while (j < h && !bound_found)
			{
				int rgb = img.getRGB(x, j);
				if ((rgb == frame_rgb_1)||(rgb == frame_rgb_2))
				{
					bound_found = true;
				}
				else
				{
					j++;
				}
			}

			int max_y = j; 

			// lookup down

			j = y;
			bound_found = false;

			while (j >= 0 && !bound_found)
			{
				int rgb = img.getRGB(x, j);
				if ((rgb == frame_rgb_1)||(rgb == frame_rgb_2))
				{
					bound_found = true;
				}
				else
				{
					j--;
				}
			}			
			
			int min_y = j + 1;
			
			rval = new Rectangle(min_x,min_y,max_x - min_x, max_y - min_y);
		}
		
		return rval;
	}
	
	private String get_project_filename()
	{
		return DirectoryBase.get_tiles_path() + m_project_file + DirectoryBase.GFX_OBJECT_SET_EXTENSION;
	}
}
