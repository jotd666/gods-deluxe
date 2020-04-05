package gods.game.characters.weapons;

import gods.base.*;
import gods.game.MonsterLayer;
import gods.game.SfxSet;

public abstract class HeroWeapon extends Projectile 
{
	public abstract boolean can_destroy_spikes();

	// empiric period to collide enemies with weapons that go through
	// how this was found: start at level 4 with one 1 axe. In the original game
	// you can kill the firing head with 3 shots (each causing 3 hits): 3*3*12 > 80
	// with FIRST_COLLISION_PERIOD = 133 you need 4 hits. And with lower values the game is just too
	// easy. So 132 seems to do it.
	private static final int FIRST_COLLISION_PERIOD = 132;
	private static final int OTHERS_COLLISIONS_PERIOD = FIRST_COLLISION_PERIOD; // difficulty level
	
	private int m_collision_period = FIRST_COLLISION_PERIOD;
	private int m_collision_timer = 0;
	
	public boolean can_hurt_hostiles()
	{
		return m_state == State.ALIVE;
	}
	
	@Override
	public void set_state(State s) 
	{
		if (s == State.HITTING_WALL)
		{
			m_sfx_set.play(SfxSet.Sample.weapon_crash);
		}
		super.set_state(s);
	}
	protected SfxSet m_sfx_set;
	protected MonsterLayer m_monster_layer;
	
	public final void init(int update_rate, int power, LevelData level, MonsterLayer ml,
			String frame_name,AnimatedFrames.Type type) 
	{
		init(update_rate, power, level, ml, 
				level.get_common_palette().lookup_frame_set(frame_name), type);
		

	}
	public void init(int update_rate, int power, LevelData level, MonsterLayer ml,
			GfxFrameSet frames,AnimatedFrames.Type type) 
	{
		super.init(update_rate, power, level, frames, type);
		
		m_monster_layer = ml;
	}

	public void set_sfx_set(SfxSet s)
	{
		m_sfx_set = s;
	}
	
	
	public void update(long elapsed_time) 
	{
		basic_update(elapsed_time);
	
		m_collision_timer += elapsed_time;
		while (m_collision_timer > m_collision_period)
		{
			m_monster_layer.collision(this);
			m_collision_timer -= m_collision_period;
			// hit once: now make it longer before next collision
			
			m_collision_period = OTHERS_COLLISIONS_PERIOD;
		}
		
	}
		
}
