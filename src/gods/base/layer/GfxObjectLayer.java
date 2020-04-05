package gods.base.layer;

import gods.base.*;

import gods.sys.ParameterParser;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import java.util.*;

public class GfxObjectLayer extends SparseLayer<GfxObject> 
{
	private boolean m_show_visible = true;
	private boolean m_show_hidden = true;
	private LevelData m_levelData = null;
	
	public Collection<GfxObject> get_items(GfxFrameSet.Type [] types)
	{
		LinkedList<GfxObject> rval = new LinkedList<GfxObject>();
		
		for (GfxFrameSet.Type t : types)
		{
			get_items(t,rval);
		}
		
		return rval;
	}
	public Collection<GfxObject> get_items(GfxFrameSet.Type [] types, boolean visible)
	{
		LinkedList<GfxObject> rval = new LinkedList<GfxObject>();
		
		for (GfxFrameSet.Type t : types)
		{
			get_items(t,rval, visible);
		}
		
		return rval;
	}
	
	public void get_items(GfxFrameSet.Type t, Collection<GfxObject> outlist, boolean visible)
	{
		for (GfxObject go : m_object_list)
		{
			if ((go.is_visible() == visible) && (go.get_source_set().get_type() == t))
			{
				outlist.add(go);
			}
		}
		
	}

	public void get_items(GfxFrameSet.Type t, Collection<GfxObject> outlist)
	{
		for (GfxObject go : m_object_list)
		{
			if (go.get_source_set().get_type() == t)
			{
				outlist.add(go);
			}
		}
	}
	
	public void set_view_filter(boolean show_visible, boolean show_hidden)
	{
		m_show_visible = show_visible;
		m_show_hidden = show_hidden;
	}
	
	public void editor_render(Graphics g)
	{
		// do nothing
	}
	@Override
	protected GfxObject add_item(ParameterParser fr, int x_res, int y_res, GfxPalette palette,int scale_factor) throws IOException
	{
		GfxObject go = new GfxObject(fr,x_res,y_res,palette,scale_factor);
		GfxFrameSet gfs = go.get_source_set();
		
		if (gfs != null)
		{
			if (gfs.get_type() == GfxFrameSet.Type.activable)
			{
				// put levers in background: add first in the list
				m_object_list.addFirst(go);
			}
			else
			{
				m_object_list.addLast(go);
			}
		}
		
		return go;
	}

	private Rectangle m_work_rectangle = new Rectangle();
	
	public void update(long elapsed_time)
	{
		// compute a slightly wider rectangle
		Rectangle r = m_levelData.get_view_bounds();
		int tw = m_levelData.get_grid().get_tile_width();
		int th = tw * 2;
		
		m_work_rectangle.x = r.x - tw;
		m_work_rectangle.y = r.y - th;
		m_work_rectangle.width = r.width + 2*tw;
		m_work_rectangle.height = r.height + 2*th;
		
		for (GfxObject go : m_object_list)
		{
			int old_x = go.get_x();
			
			go.update(elapsed_time,m_levelData,m_work_rectangle);
			
			if (go.get_x() != old_x)
			{
				// maybe sector has changed
				
				m_levelData.notify_x_move_object(go,old_x);
			}
		}
	}

	public void render(java.awt.Graphics2D g) 
	{
		for (GfxObject go : m_object_list)
		{
			BufferedImage bi = go.toImage();
			if (bi != null)
			{
				if ((m_show_hidden) && (!go.is_visible()))
				{
					go.draw_image(g,bi);
				}
				if ((m_show_visible) && (go.is_visible()))
				{
					go.draw_image(g,bi);
				}
				
			}
		}
	}

	public String get_block_name() 
	{		
		return "GFX_OBJECT_LAYER";
	}
	
	public GfxObjectLayer(ParameterParser fr, GfxPalette palette, 
			int y_resolution, LevelData levelData, int scale_factor) throws IOException
	{
		this(y_resolution);
		load(fr,palette,scale_factor);
		m_levelData = levelData;
	}
	
	public GfxObjectLayer(int y_resolution)
	{
		super(2,y_resolution);
	}
	
	public GfxObject get(String name)
	{
		return (GfxObject)super.get(name);
	}
	
	public GfxObject get(int x, int y)
	{
		LinkedList<GfxObject> lgo = get_items(x,y);
		GfxObject rval = null;
		
		if (!lgo.isEmpty())
		{
			rval = lgo.getFirst();
		}
		return rval;
	}
	
	public GfxObject set(int x, int y, String name, GfxFrameSet frame_set)
	{
		GfxObject go = get(x,y);
		if (go != null)
		{
			m_object_list.remove(go);
		}
		if (frame_set != null)
		{
			go = new GfxObject(x,y,m_x_resolution,m_y_resolution,name,frame_set);
			m_object_list.add(go);
		}
		
		return go;
	}
}
