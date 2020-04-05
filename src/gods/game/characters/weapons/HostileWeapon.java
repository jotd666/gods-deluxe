package gods.game.characters.weapons;

import java.awt.Rectangle;

import gods.base.AnimatedFrames;
import gods.base.GfxFrameSet;
import gods.base.LevelData;
import gods.base.Locatable;
import gods.game.characters.Hero;


public abstract class HostileWeapon extends Projectile {
	private Rectangle m_work_rectangle = new Rectangle();
	
	public void init(int update_rate, int power,
			LevelData level, Hero hero, GfxFrameSet frame_set,AnimatedFrames.Type anim_type) 
	{
		super.init(update_rate, power, level, frame_set, anim_type);
		
		m_hero = hero;
	}
	
	protected Hero m_hero;
	protected Locatable m_aim;
	
	protected void hero_collision_test()
	{
		if (!m_hero.is_invincible())
		{
			m_hero.get_bounds(m_work_rectangle);

			if (m_work_rectangle.contains(m_x+m_width/2,m_y+m_height/2))
			{
				m_hero.hurt(m_power);  // resourced from game

				set_state(State.HURTING_HERO);
			}
		}
	}
}
