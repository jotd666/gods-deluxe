package gods.game.characters.weapons;

import gods.base.LevelData;
import gods.game.MonsterLayer;


public class Mace extends HeroLinearWeapon 
{
	
	@Override
	public void init(int shoot_angle, int frame_rate, int power, LevelData level, MonsterLayer ml, String frame_name) {
		
		super.init(shoot_angle, frame_rate, power, level, ml, frame_name);
		

	}
	public void set_state(State s)
	{
		super.set_state(s);
				
		if (m_state == State.HURTING_HOSTILE)
		{
			// maces goes through hostiles
			m_state = State.ALIVE;
		}
	}


	@Override
	protected void move(long elapsed_time) 
	{
		super.move(elapsed_time);
		
		if (is_scenery_hit())
		{
			int x = get_x_center();
			int y = get_y_center();

			m_level_data.hit_block(x, y, m_power);


			set_state(State.HITTING_WALL);
		}
	}

}
