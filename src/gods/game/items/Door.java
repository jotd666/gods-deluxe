package gods.game.items;

import gods.base.*;
import gods.base.layer.*;


public abstract class Door extends ControlObject 
{
	protected TileGrid m_grid;
	
	public abstract boolean is_open();
	
	
	public abstract void set_open(boolean open);
	
	public Door(ControlObject parent, TileGrid grid) 
	{
		super(parent);
	
		m_grid = grid;
		
	}
	

}
