package gods.base;

import java.awt.Rectangle;

public interface LayerObject extends Nameable<Object>, Describable
{
	public void get_bounds(Rectangle r);
}
