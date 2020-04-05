package gods.base;

import java.awt.Graphics2D;

public interface Renderable 
{
	public void update(long elapsed_time);
	public void render(Graphics2D g);
}
