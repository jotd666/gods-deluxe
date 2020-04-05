package gods.game.characters.hostiles;


import gods.base.GfxFrameSet;
import gods.game.SfxSet.Sample;
import gods.game.characters.*;
import gods.game.characters.weapons.HostileDropWeapon;
import gods.game.characters.weapons.Projectile;

public class SpittingHeadLevel2 extends LeftRightHostile 
{
	@Override
	public boolean is_in_background() {
		
		return true;
	}

	private int m_shoot_anim = -1;
	private int m_sound_index = 0;
	
	@Override
	public void init(HostileParameters p)
	{
		super.init(p);
		m_params.nb_move_frames = 1;
	}
	
	private static final int FRAME_RATE = 40;
	private int m_animation_timer = 0;
	
	@Override
	protected void animate(long elapsed_time)
	{
		m_animation_timer += elapsed_time;
		
		while (m_animation_timer > FRAME_RATE)
		{
			m_animation_timer -= FRAME_RATE;

			// do nothing except when shooting
			switch (m_shoot_anim)
			{
			case 0:			
			case 2:			
				m_frame_counter++;
				break;
			case 4:					
				m_frame_counter++;
				really_shoot();
				break;
			case 6:
				m_shoot_anim = -1;
				m_frame_counter = 1;
				break;
			}
			if (m_shoot_anim >= 0)
			{
				m_shoot_anim++;
			}		
		}
	}
	@Override
	protected void move(long elapsed_time) 
	{

	}
	private void really_shoot()
	{
		int y = get_y() + m_height - 12;

		HostileDropWeapon p = new HostileDropWeapon();
		GfxFrameSet gfs = m_params.level.get_level_palette().lookup_frame_set("spitting_head_ball");
		
		p.init(0.05,50,5,m_params.level,m_params.hero,gfs);
		p.set_coordinates(m_x + 22,y);
		m_params.weapon_set.add(p);		
	}

	@Override
	protected Projectile shoot() 
	{
		m_params.sfx_set.play(Sample.spitting_ball,m_sound_index);
		m_sound_index = 1 - m_sound_index;
		m_shoot_anim = 0; // start shoot anim
		
		return null;
	}




}
