package gods.game.characters.weapons;

import gods.base.AnimatedFrames;
import gods.base.GfxFrameSet;
import gods.base.LevelData;
import gods.game.MonsterLayer;
import gods.sys.AngleUtils;

public class Spear extends HeroLinearWeapon 
{
	private int m_direction = 0;
	
	@Override
	public void set_state(State s)
	{
		// do nothing: crosses walls & monsters
	}
	
	@Override
	public void init(int shoot_angle, int frame_rate, int power, LevelData level, 
			MonsterLayer ml,String frame_name) 
	{
		m_direction = (int)Math.signum(AngleUtils.cosd(shoot_angle));
		GfxFrameSet gfs = level.get_common_palette().lookup_frame_set(frame_name);

		gfs = m_direction > 0 ? gfs : level.get_common_palette().get_left_frame_set(gfs);
		
		init(frame_rate, power, level, ml, gfs, AnimatedFrames.Type.CUSTOM);
				
		m_cos = m_direction * speed;
		m_sin = AngleUtils.sind(shoot_angle) * speed;
		
		
	}
}
