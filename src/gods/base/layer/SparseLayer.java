package gods.base.layer;


import gods.base.*;
import gods.sys.ParameterParser;

import java.io.IOException;
import java.util.*;
import java.awt.*;

public abstract class SparseLayer<T extends GfxObject> implements Describable, EditorRenderable, Renderable
{	
	private int m_naming_counter = 1;
	private Rectangle m_work_rectangle = new Rectangle();
	
	protected int m_x_resolution, m_y_resolution;
	protected LevelData m_levelData;
	
	protected abstract T add_item(ParameterParser fr, int x_res, int y_res, GfxPalette palette, int scale_factor) throws IOException;

	protected LinkedList<T> m_object_list = new LinkedList<T>();
	
	protected void load(ParameterParser fr, GfxPalette palette, int scale_factor) throws IOException 
	{	
		fr.startBlockVerify(get_block_name());
		
		int nb_items = fr.readInteger("nb_items");
		
		for (int i = 0; i < nb_items; i++)
		{
			T o = add_item(fr,m_x_resolution,m_y_resolution,palette,scale_factor);
			// game mode: generate a unique name for unnamed objects
			// or hash sets & sectors won't work
			if ((scale_factor>1) && (o.get_name().equals("")))
			{
				o.set_name(GfxObject.UNNAMED_PREFIX+m_naming_counter++);
			}
		}
		
		fr.endBlockVerify();
	}

	public Collection<T> get_items()
	{
		return m_object_list;
	}
	
	public void serialize(ParameterParser fw) throws IOException 
	{
		fw.startBlockWrite(get_block_name());
		
		fw.write("nb_items",m_object_list.size());
		
		for (Describable go : m_object_list)
		{
			go.serialize(fw);
		}
		
		fw.endBlockWrite();
		
	}

	
	protected T get(String name)
	{
		T rval = null;
		
		if (!name.equals(""))
		{
			for (T go : m_object_list)
			{
				if (go.get_name().equals(name))
				{
					rval = go;
					break;
				}
			}
		}
		
		return rval;
	}

	protected LinkedList<T> get_items(int x, int y) 
	{
		LinkedList<T> rval = new LinkedList<T>();
		
		for (T go : m_object_list)
		{
			go.get_bounds(m_work_rectangle);
			
			if (m_work_rectangle.contains(x, y))
			{
				rval.add(go);
				
			}
		}
		
		return rval;
	}

	
	protected SparseLayer()
	{
	
	}

	public int get_y_resolution()
	{
		return m_y_resolution;
	}
	
	public void move_objects(int x_len, int y_len)
	{
		for (T go : m_object_list)
		{
			go.set_coordinates(x_len + go.get_x(), y_len + go.get_y());
		}
		
	}
	public void remove_invisible_objects(int width, int height)
	{
		ListIterator<T> it = m_object_list.listIterator();
		
		while (it.hasNext())
		{
			T go = it.next();
			boolean out_x = (go.get_x()+go.get_width() < 0) || (go.get_x() > width);
			
			if ( out_x || ((go.get_y()+go.get_height() < 0) || (go.get_y() > height)))
			{
				// completely off-screen: remove
				it.remove();
			}
		}
		
	}
	public SparseLayer(int x_resolution, int y_resolution)
	{
		m_x_resolution = x_resolution;
		m_y_resolution = y_resolution;
	}
	
	
}