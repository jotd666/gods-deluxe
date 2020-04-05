package gods.game.items;

import gods.base.*;
import gods.base.layer.*;
import java.util.*;


public class SideDoor extends Door 
{

	private class TileLocation
	{
		GfxFrame [] frame = new GfxFrame[2];
		GfxFrame [] open_frame = new GfxFrame[2];
		String name;
		int x, y, w;
		
		private boolean m_open = false;
		
		public boolean is_open()
		{
			return m_open;
		}
				
		TileLocation(int x, int y, int w)
		{
			this.x = x;
			this.y = y;
			this.w = w;
			this.frame[0] = m_grid.get(x, y);
			this.name = frame[0].get_source_set().get_name();
			this.frame[1] = m_grid.get(x + w, y);

			// create matching open tile
			
			String open_name = name.replace("closed", "open");
			
			GfxFrameSet open_fs = m_grid.get_palette().lookup_frame_set(open_name);
			if (open_fs == null)
			{
				open_fs = frame[0].get_source_set();
			}
			open_frame[0] = open_fs.get_frame(1);
			if (open_fs.get_nb_frames() < 2)
			{
				warn("misplaced door location at ("+x/2+","+y/2+") for ("+name+","+open_name+")");
				open_frame[1] = open_frame[0];
			}
			else
			{
				open_frame[1] = open_fs.get_frame(2);
			}
		}
		
		void set_open(boolean open)
		{
			if (open != m_open)
			{
				// something to do
				
				m_open = open;
				
				if (open)
				{
					m_grid.set(x,y,open_frame[0]);
					m_grid.set(x+w,y,open_frame[1]);
				}
				else
				{
					m_grid.set(x,y,frame[0]);
					m_grid.set(x+w,y,frame[1]);					
				}
			}
		}
	}
	private LinkedList<TileLocation> m_tile_list = new LinkedList<TileLocation>();
		
	@Override
	public void set_open(boolean open)
	{	
		for (TileLocation tl : m_tile_list)
		{
			tl.set_open(open);
		}
	}
	
	public boolean is_open()
	{
		return m_tile_list.getFirst().is_open();
	}
	public SideDoor(ControlObject parent, TileGrid grid) 
	{
		super(parent,grid);
			
		// get the tiles under the control object
		
		int tw = grid.get_tile_width();
		int th = grid.get_tile_height();
		
		int nb_vertical_tiles = 3;

		for (int j = 0; j < nb_vertical_tiles; j++)
		{
			TileLocation tl = new TileLocation(get_x(),get_y() + j * th,tw);

			m_tile_list.add(tl);
		}

	}

}
