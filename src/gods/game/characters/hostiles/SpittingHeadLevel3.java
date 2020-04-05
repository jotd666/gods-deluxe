package gods.game.characters.hostiles;


import gods.base.GfxFrameSet;
import gods.game.SfxSet.Sample;
import gods.game.characters.*;
import gods.game.characters.weapons.HostileDropStoneWeapon;
import gods.game.characters.weapons.Projectile;

public class SpittingHeadLevel3 extends LeftRightHostile 
{

	@Override
	public boolean is_in_background() {
		
		return true;
	}

	private int m_shoot_anim = -1;
	
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
				m_frame_counter++;
				break;
			case 2:			
				m_frame_counter++;
				really_shoot();
				break;
			case 4:					
				m_frame_counter--;
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
		int y = get_y() + 10;
		double y_speed = 0.15;
		double x_speed = 0.05;
		
		GfxFrameSet gfs = m_params.level.get_level_palette().lookup_frame_set("stone");
		
		for (int i = -1; i < 2; i+=2)
		{
			HostileDropStoneWeapon p = new HostileDropStoneWeapon(m_params.weapon_set);
			p.init(i == -1 ? m_params.sfx_set : null,x_speed * i,y_speed,100,5,m_params.level,m_params.hero,gfs);
			p.set_coordinates(get_x_center() - p.get_x_center(),y);
			m_params.weapon_set.add(p);		
		}
	}

	@Override
	protected Projectile shoot() 
	{
		m_params.sfx_set.play(Sample.spitting_ball,3);
		m_shoot_anim = 0; // start shoot anim
		
		return null;
	}




}
