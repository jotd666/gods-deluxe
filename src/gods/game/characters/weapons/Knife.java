package gods.game.characters.weapons;


public class Knife extends HeroLinearWeapon {

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
