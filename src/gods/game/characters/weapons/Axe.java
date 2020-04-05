package gods.game.characters.weapons;



public class Axe extends HeroLinearWeapon 
{
	@Override
	public void set_state(State s)
	{
		// do nothing: crosses walls & monsters
	}

	/*
	protected void move(long elapsed_time) 
	{
		super.move(elapsed_time);
		
		if (is_scenery_hit())
		{
			set_state(State.HITTING_WALL);
		}
	}*/

}
