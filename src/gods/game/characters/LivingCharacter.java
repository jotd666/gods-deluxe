package gods.game.characters;

import java.awt.Rectangle;

import gods.base.*;

public abstract class LivingCharacter extends NamedLocatable implements Renderable
{
	protected LifeState m_life_state;
	protected Rectangle m_work_rectangle = new Rectangle();

	protected void debug(String message)
	{
		if (DebugOptions.debug)
		{
		System.out.println(this.getClass().getName()+" ("+toString()+"): "+message);
		}
	}
	
	public void set_life_state(LifeState s)
	{
		m_life_state = s;
	}
	public LifeState get_life_state()
	{
		return m_life_state;
	}
	
	public void set_name(String n) {
		
		
	}	
	public boolean is_in_screen(int margin,Rectangle view_bounds)
	{
		boolean rval = false;
		
		if (margin == 0)
		{
			// optimization
			
			rval = view_bounds.contains(get_x_center(),get_y_center());
		}
		else
		{
			m_work_rectangle.x = view_bounds.x - margin;
			m_work_rectangle.y = view_bounds.y - margin;
			m_work_rectangle.width = view_bounds.width + margin * 2;
			m_work_rectangle.height = view_bounds.height + margin * 2;
			
			rval = m_work_rectangle.contains(get_x_center(),get_y_center());
		}
		return rval;
	}

	public enum LifeState { STAND_BY, DELAYED, APPEARING, EXPLODING, ALIVE, DEAD };
	

	public abstract int get_health(boolean instantaneous);
	public abstract int get_max_health();
	
	public abstract void handle_health(long elapsed_time);
}
