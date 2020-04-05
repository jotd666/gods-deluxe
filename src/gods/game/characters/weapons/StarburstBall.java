package gods.game.characters.weapons;

import gods.base.*;
import gods.game.MonsterLayer;
import gods.game.SfxSet;

import java.util.Collection;

public class StarburstBall extends HeroWeapon {


	@Override
	public boolean can_destroy_spikes() 
	{
		return false;
	}

	@Override
	public void set_state(State s) 
	{
		if (s == State.HURTING_HOSTILE)
		{
			// hostile was encountered: spawn other starbursts
			
			spawn_new_ones();
		}
		m_state = s;

	}

	private void spawn_new_ones()
	{
		spawn(get_x(), get_y(), m_level_data, m_monster_layer, 
				m_gfs, m_spawn_probability / 2,m_sfx_set, m_projectile_list);
	}
	private int m_timer = 0;
	private int m_explosion_timer = 0;
	
	private int m_x_direction;
	private int m_y_direction;
	private float m_x_speed;
	private float m_y_speed;
	private double m_spawn_probability;
	private Collection<HeroWeapon> m_projectile_list;
	
	private GfxFrameSet m_gfs;
	
	private static final int SPEED = 12;
	
	public static void spawn(int x, int y, LevelData ld, MonsterLayer ml, 
			GfxFrameSet fs, double spawn_probability, SfxSet sfx_set, Collection<HeroWeapon> c)
	{
		for (int xd = -1; xd < 2; xd++)
		{
			for (int yd = -1; yd < 2; yd++)
			{
				if ((xd != 0) || (yd != 0))
				{
					StarburstBall s = new StarburstBall(x,y,ld,ml,fs,xd,yd,spawn_probability,c);
					
					s.set_sfx_set(sfx_set);
					
					sfx_set.play(SfxSet.Sample.starburst);
					c.add(s);
				}
			}
		}

	}
	public StarburstBall(int x, int y, LevelData level, MonsterLayer ml, 
			GfxFrameSet gfs, int x_direction, int y_direction, double spawn_probability, 
			Collection<HeroWeapon> c) 
	{
		init(150, Integer.MAX_VALUE, level, ml, gfs, Type.FOREVER);
		set_coordinates(x, y);
			
		m_projectile_list = c;
		m_gfs = gfs;
		
		m_spawn_probability = spawn_probability;
		
		m_x_direction = x_direction;
		m_y_direction = y_direction;
		
		m_x_speed = m_x_direction * SPEED;
		m_y_speed = m_y_direction * SPEED;
		
		m_explosion_timer = (int)(Math.random() * 1000 + 1000);
		
	}

	@Override
	protected void move(long elapsed_time) 
	{
		m_timer += elapsed_time;

		if (m_timer > m_explosion_timer)
		{
			m_explosion_timer = Integer.MAX_VALUE;
			if (Math.random() < m_spawn_probability)
			{
				// kill current
				set_state(State.DEAD);
				// spawn others
				spawn_new_ones();
			}
		}
		m_x += (m_x_speed * elapsed_time) / 50;
		m_y += (m_y_speed * elapsed_time) / 50;

		m_y_speed += (elapsed_time / 200.0);
	}

}
