package gods.base;

import java.awt.Graphics2D;
import java.util.Collection;

public class RenderableWithoutUpdateList implements Renderable 
{
	public Collection<? extends Renderable> items;
	
	public void render(Graphics2D g) 
	{
		for (Renderable r : items)
		{
			r.render(g);
		}
	}

	public void update(long elapsed_time) 
	{
		// do nothing
	}

}
