package gods.game.items;

import gods.base.*;
import gods.base.layer.*;


public class TrapDoor extends Door 
{
	private TileLocation m_tile_location;
	
	private class TileLocation
	{
		GfxFrame closed_frame;
		GfxFrame open_frame;
		String name;
		int x, y, w;
		
		private boolean m_open;
		
		public boolean is_open()
		{
			return m_open;
		}
		
		TileLocation(int x, int y, int w)
		{
			this.x = x;
			this.y = y;
			this.w = w;
			this.closed_frame = m_grid.get(x, y);
			this.name = closed_frame.get_source_set().get_name();

			m_open = name.contains("open");
			
			// create matching open tile and detect open/closed state
			
			String open_name = null;
			
			if (m_open)
			{
				// swap open/closed
				
				open_name = name;
				name = open_name.replace("open", "closed");
				open_frame = closed_frame;

				GfxFrameSet closed_fs = m_grid.get_palette().lookup_frame_set(name);
				if (closed_fs == null)
				{
					closed_fs = closed_frame.get_source_set();
				}
				closed_frame = closed_fs.get_first_frame();
			}
			else
			{
				open_name = name.replace("closed", "open");
				
				GfxFrameSet open_fs = m_grid.get_palette().lookup_frame_set(open_name);
				if (open_fs == null)
				{
					open_fs = closed_frame.get_source_set();
				}
				open_frame = open_fs.get_first_frame();
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
					m_grid.set(x,y,open_frame);
				}
				else
				{
					m_grid.set(x,y,closed_frame);				
				}
			}
		}
	}	
	
	
	@Override
	public void set_open(boolean open)
	{
		m_tile_location.set_open(open);
	}
	
	public TrapDoor(ControlObject parent, TileGrid grid) 
	{
		super(parent,grid);
			
		// get the tile under the control object
		
		int tw = grid.get_tile_width();
		
		m_tile_location = new TileLocation(get_x(),get_y(),tw);



	}

	@Override
	public boolean is_open() 
	{
		return m_tile_location.is_open();
	}

}
