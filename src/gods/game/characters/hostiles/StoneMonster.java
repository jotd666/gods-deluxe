package gods.game.characters.hostiles;

import gods.game.characters.*;
import gods.base.layer.TileGrid;
import gods.base.*;

public class StoneMonster extends FlyingMonster {

	@Override
	public void set_life_state(LifeState s) 
	{
		if (s == LifeState.EXPLODING && m_life_state == LifeState.ALIVE)
		{
			remove_background();
		}
		super.set_life_state(s);
	}

	@Override
	public void init(HostileParameters p) 
	{

		super.init(p);
		
		// force no appearing animation
		
		m_appearing_animation = false;
		
		// set proper frame
		
		m_frame_counter = 3;

	}

	
	@Override
	protected void move(long elapsed_time) 
	{
		if (m_first_move)
		{
			remove_background();
			m_first_move = false;
		}		

		super.move(elapsed_time);
	}


	private boolean m_first_move = true;
	
	private void remove_background()
	{
		ControlObject l = m_params.location;
		
		GfxFrame blank = m_params.level.get_level_palette().lookup_frame_set("blank").get_first_frame();
		
		TileGrid tg = m_params.level.get_grid();
		
		l.set_x(tg.get_rounded_x(l.get_x()));
		l.set_y(tg.get_rounded_y(l.get_y()));
		
		// remove stone monster background
		tg.set(l.get_x(), l.get_y(),blank);
		tg.set(l.get_x(), l.get_y() + tg.get_tile_height(), blank);
		
		// set proper frame
		
		m_frame_counter = 3;

	}


}
