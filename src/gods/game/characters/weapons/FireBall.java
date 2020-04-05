package gods.game.characters.weapons;

import gods.base.AnimatedFrames;
import gods.base.LevelData;
import gods.game.MonsterLayer;
import gods.sys.AngleUtils;

public class FireBall extends HeroWeapon {



	@Override
	public boolean can_destroy_spikes() 
	{
		
		return false;
	}



	private double m_speed_x,m_speed_y;
	private double speed = 0.45;
	
	// len: 360, monte de 20
	
	//boolean max_reached = false;
	//int start_x, start_y;
	
	@Override
	protected void move(long elapsed_time) 
	{
		m_x += m_speed_x * elapsed_time;
		m_y += m_speed_y * elapsed_time;
		
		m_speed_y += elapsed_time / 900.0;
		
		
		if (is_scenery_hit())
		{
			int x = get_x_center();
			int y = get_y_center();

			// minimal hit to breakable blocks
			
			m_level_data.hit_block(x, y, 1);

			set_state(State.HITTING_WALL);
			//System.out.println("crash x,y: "+(m_x-start_x)+" "+(m_y-start_y));
		}
	}
	
	

	public void init(int shoot_angle, int frame_rate, int power, LevelData level, MonsterLayer ml) 
	{
		super.init(frame_rate, power, level, ml, "fire_ball", AnimatedFrames.Type.FOREVER);
			
		double direction = Math.signum(AngleUtils.cosd(shoot_angle));
		
		int actual_shoot_angle = (int)(shoot_angle - direction * 28);
		
		m_speed_x = direction * speed;
		m_speed_y = AngleUtils.sind(actual_shoot_angle) * speed;
		
		/*start_x = m_x;
		start_y = m_y;
		System.out.println("start x,y "+m_x+" "+m_y); */
		
	}

}
