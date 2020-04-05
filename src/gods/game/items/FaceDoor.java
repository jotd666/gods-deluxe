package gods.game.items;

import gods.base.*;
import gods.base.layer.*;
import java.util.*;
import java.awt.image.BufferedImage;
import java.awt.Graphics;

public class FaceDoor extends Door 
{
	private BufferedImage m_open_image;
	
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
				
		TileLocation(int x, int y, int w, int idx)
		{
			this.x = x;
			this.y = y;
			this.w = w;
			this.frame[0] = m_grid.get(x, y);
			this.name = frame[0].get_source_set().get_name();
			this.frame[1] = m_grid.get(x + w, y);

			// create matching open tile (tricky: recreate tiles, cloned from closed door tiles
			// but with modified parts so it includes open door
			
			for (int i = 0; i < 2; i++)
			{
				open_frame[i] = new GfxFrame(this.frame[i],GfxFrame.SymmetryType.no_op_clone);
				Graphics g = open_frame[i].toImage().getGraphics();
				
				int wdraw = m_open_image.getWidth()/2;
				int hdraw = open_frame[i].get_height();
				
				int dxdraw = (i==0) ? open_frame[i].get_width() - wdraw : 0;
				
				int ydraw = idx * hdraw;
				
				int sxdraw = wdraw * i;
				
				g.drawImage(m_open_image, dxdraw, 0, 
					                      dxdraw + wdraw, hdraw,
						                  sxdraw, ydraw,
						                  sxdraw + wdraw, ydraw + hdraw, null);
				
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
	public FaceDoor(ControlObject parent, TileGrid grid) 
	{
		super(parent,grid);
			
		// get the tiles under the control object
		
		int tw = grid.get_tile_width();
		int th = grid.get_tile_height();
		
		int nb_vertical_tiles = 3;

		Collection<GfxFrameSet> od = grid.get_palette().lookup_by_type(GfxFrameSet.Type.open_face_door);
		
		if ((od != null) && (!od.isEmpty()))
		{
			Iterator<GfxFrameSet> it = od.iterator();
			m_open_image = it.next().toImage();
		}
		else
		{
			warn("Open face door image not found in palette");
			m_open_image = toImage();
		}
		for (int j = 0; j < nb_vertical_tiles; j++)
		{
			TileLocation tl = new TileLocation(get_x(),get_y() + j * th,tw, j);

			m_tile_list.add(tl);
		}

	}

}
