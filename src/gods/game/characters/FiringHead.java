package gods.game.characters;


import gods.base.HostileWaveParameters;
import gods.game.characters.weapons.HostileStraightWeapon;
import gods.game.characters.weapons.Projectile;

public abstract class FiringHead extends LeftRightHostile 
{

	
	protected boolean m_shooting = false;
	
	@Override
	public boolean is_in_background() {
		
		return false;
	}	
	
	@Override
	public void init(HostileParameters p)
	{
		// force firetype to straight
		p.fire_type = HostileWaveParameters.FireType.Straight;
		
		super.init(p);
		m_params.nb_move_frames = 1;
	}
	

	@Override
	protected void move(long elapsed_time) 
	{

	}

	@Override
	protected Projectile shoot() 
	{
		boolean to_left = m_right_left != 0;
		int x = get_x() + (to_left ? 20 : (m_width - 20));

		HostileStraightWeapon p = new HostileStraightWeapon();
		p.init(12,50,2,to_left,
				m_params.level,m_params.hero,m_fire_frames[m_right_left]);
		
		p.set_coordinates(x,m_y + 24);
		
		m_params.weapon_set.add(p);
		
		m_shooting = true;
		
		return p;
	}




}
