package gods.game.characters.hostiles;

import gods.base.GfxFrameSet;
import gods.game.characters.FlyingMonster;
import gods.game.characters.HostileParameters;
import gods.game.characters.weapons.HostileBreakBlockWeapon;
import gods.game.characters.weapons.Projectile;

public class FlyingBlockKiller extends FlyingMonster 
{
	@Override
	public void init(HostileParameters p) 
	{	
		super.init(p);
		m_fire_frames = new GfxFrameSet[1];
		m_fire_frames[0] = m_params.level.get_level_palette().lookup_frame_set("breaking_block_shot");
	}

	@Override
	protected Projectile shoot() 
	{
		int x = get_x_center();
		Projectile p = null;
		int speed = m_params.get_shoot_speed_value();
	
		HostileBreakBlockWeapon hbbw = new HostileBreakBlockWeapon();
		hbbw.init(speed,m_params.level,m_params.hero,m_fire_frames[0]);
		p = hbbw;
		p.set_coordinates(x,m_y+m_height);

		if (p != null)
		{
			m_params.weapon_set.add(p);
		}
	
		return p;
	}

}
