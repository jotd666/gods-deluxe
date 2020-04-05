package gods.game;


import gods.base.DirectoryBase;
import gods.sys.*;


public class SfxSet
{
	private final static String sound_dir = DirectoryBase.get_sound_path()			;
	
	public enum Sample { take_bonus, hero_death, appearing_bonus, step, jump, 
		open_close, hero_hurt, weapon_throw, 
		weapon_crash, land, lever_activated,
		hostile_appearing_left,
		bonus_display,
		hostile_appearing_right, explosion_ground, 
		explosion_flying, starburst,
		panting, money_transfer,
		increase_wp,
		bonus_gem,
		energy,
		teleport,
		inventory,
		boss_move,
		boss_shoot,
		fall,
		spitting_ball,
		shop_move, puzzle, bomb_bounce, 
		bounce, drops }
	
	public enum Loop { invincibility, spike, score_countdown, money_transfer, platform_move, boss_2_fly }
	
	public SfxSet(boolean active)
	{
		if (active)
		{
			int nb_sounds = Sample.values().length;
			
			m_sample_array = new WavSample[nb_sounds];
			
			for (Sample s : Sample.values())
			{
				m_sample_array[s.ordinal()] = new WavSample(sound_dir + s.toString());
			}
			
			nb_sounds = Loop.values().length;
			
			m_loop_array = new WavLoop[nb_sounds];
			
			for (Loop s : Loop.values())
			{
				m_loop_array[s.ordinal()] = new WavLoop(sound_dir + s.toString());
			}
			
		}
	}
	
	public void dispose()
	{
		if (m_sample_array != null)
		{
			for (int i = 0; i < m_sample_array.length; i++)
			{
				WavSample ws = m_sample_array[i];
				ws.stop();
				ws.end();
				m_sample_array[i] = null;
			}
			m_sample_array = null;
		}		
		if (m_loop_array != null)
		{
			for (int i = 0; i < m_loop_array.length; i++)
			{
				WavSound ws = m_loop_array[i];
				ws.end();
				m_loop_array[i] = null;
			}
			m_loop_array = null;
		}		
		System.gc();
	}
	
	public void play(Sample s, int index)
	{
		if ((m_sample_array != null) && (s != null))
		{
			m_sample_array[s.ordinal()].play(index);
		}
	}
	public void play_random(Sample s)
	{
		if ((m_sample_array != null) && (s != null))
		{
			m_sample_array[s.ordinal()].play_random();
		}
	}
	public WavLoop get(Loop s)
	{
		WavLoop rval = null;
		
		if ((m_loop_array != null) && (s != null))
		{
			rval = m_loop_array[s.ordinal()];
		}

		return rval;
	}

	public WavSample get(Sample s)
	{
		WavSample rval = null;
		
		if ((m_sample_array != null) && (s != null))
		{
			rval = m_sample_array[s.ordinal()];
		}

		return rval;
	}
	public void play(Sample s)
	{
		if ((m_sample_array != null) && (s != null))
		{
			m_sample_array[s.ordinal()].play();
		}
	}
	
	public void stop(Sample s)
	{
		if ((m_sample_array != null) && (s != null))
		{
			m_sample_array[s.ordinal()].stop();
		}
	}
	
	public void play(Loop s)
	{
		if ((m_loop_array != null) && (s != null))
		{
			m_loop_array[s.ordinal()].play();
		}
	}
	public void pause(Loop s)
	{
		if ((m_loop_array != null) && (s != null))
		{
			m_loop_array[s.ordinal()].pause();
		}
	}
	
	public void pause_all_loops()
	{
		if (m_loop_array!=null)
		{
			for (WavLoop w : m_loop_array)
			{
				w.pause();
			}
		}

	}
	
	private WavSample [] m_sample_array = null;
	private WavLoop [] m_loop_array = null;

	
}
