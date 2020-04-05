package gods.game.characters.weapons;

import gods.base.*;

import gods.game.MonsterLayer;
import gods.sys.AngleUtils;


public class LightningBolt extends HeroLinearWeapon 
{
	@Override
	public void set_state(State s)
	{
		// do nothing: crosses walls & monsters
	}
	
	@Override
	public void init(int shoot_angle, int frame_rate, int power, LevelData level, 
			MonsterLayer ml,String frame_name) 
	{
		double direction = Math.signum(AngleUtils.cosd(shoot_angle));
		GfxFrameSet gfs = level.get_common_palette().lookup_frame_set(frame_name);

		gfs = direction > 0 ? gfs : level.get_common_palette().get_left_frame_set(gfs);
		
		init(frame_rate, power, level, ml, gfs, AnimatedFrames.Type.FOREVER);
				
		m_cos = direction * speed;
		m_sin = 0;
		
	}



}
