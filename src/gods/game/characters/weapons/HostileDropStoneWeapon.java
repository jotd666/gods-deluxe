package gods.game.characters.weapons;

import gods.base.*;
import gods.game.*;
import gods.game.characters.Hero;
import gods.game.characters.HostileWeaponSet;

public class HostileDropStoneWeapon extends HostileWeapon 
{
	private double m_speed_x;
	private double m_speed_y;
	private SfxSet m_sfx_set;
	private HostileWeaponSet m_weapon_set;
	private GfxFrameSet m_scattering_stone;
	private LevelData m_level;
	private Hero m_hero;
	
	public HostileDropStoneWeapon(HostileWeaponSet weapon_set)
	{
		m_weapon_set = weapon_set;
	}
	public void init(SfxSet sfx_set,double speed_x, double speed_y, int update_rate, int power,
			LevelData level, Hero hero, GfxFrameSet frame_set) 
	{
		super.init(update_rate, power, level, hero, frame_set,AnimatedFrames.Type.FOREVER);
	
		m_sfx_set = sfx_set;
		m_speed_x = speed_x;
		m_speed_y = speed_y;
		
		m_level = level;
		m_hero = hero;
		
		m_scattering_stone = level.get_level_palette().lookup_frame_set("scattering_stone");
		
	}
	public void set_state(State s)
	{
		switch (s)
		{
		case HURTING_HERO:
			init(m_weapon_crash,100,Type.ONCE);
			break;
		}
		
		m_state = s;

	}
	@Override
	protected void move(long elapsed_time) 
	{
		double old_y = m_y;

		m_x += m_speed_x * elapsed_time;
		m_y += m_speed_y * elapsed_time;

		if (is_wall_hit(m_x,m_y + m_height))
		{
			m_y = old_y;

			for (int i = -1; i < 2; i++)
			{
				HostileScatteringStone hss = new HostileScatteringStone();

				// 3 health points per stone (is it accurate?)
				
				hss.init(i, 3, m_level, m_hero, m_scattering_stone);
				hss.set_coordinates(this,true);
				m_weapon_set.add(hss);
			}

			set_state(State.DEAD);

			if (m_sfx_set != null)
			{
				m_sfx_set.play(SfxSet.Sample.spitting_ball,2);
			}
		}


		hero_collision_test();
		
	}

}
