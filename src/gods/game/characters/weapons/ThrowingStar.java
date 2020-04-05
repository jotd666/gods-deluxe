package gods.game.characters.weapons;


public class ThrowingStar extends HeroLinearWeapon {

	public void set_state(State s)
	{
		super.set_state(s);
				
		if (m_state == State.HURTING_HOSTILE)
		{
			// throwing star goes through hostiles
			m_state = State.ALIVE;
		}
	}
	
	@Override
	protected void move(long elapsed_time) 
	{
		super.move(elapsed_time);
		
		if (is_scenery_hit())
		{
			set_state(State.HITTING_WALL);
		}
	}

}
