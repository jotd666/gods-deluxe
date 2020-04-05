package gods.game.characters;

import gods.base.GfxObject;
import gods.base.HostileWaveParameters;
import gods.base.NamedLocatable;
import gods.base.layer.Sector;

public class GroundThief extends GroundMonster
{
	
	public GroundThief(boolean last_frame_reserved, int [] stolen_object_xy_offset)
	{
		super(last_frame_reserved,stolen_object_xy_offset);
		
	}
	
	@Override
	protected void set_aim(NamedLocatable aim) {
		
		// reset aim met if aim different from previous aim
		
		if (aim != get_aim())
		{
			m_aim_met = false;

			m_params.attack_distance = m_saved_attack_distance;
			
			set_dormant(false);
		
		}
		
		super.set_aim(aim);
		
		/*if (aim != null)
		{
			m_params.attack_distance = m_saved_attack_distance;
			
			set_dormant(false);
			
		}*/
	}

	private HostileWaveParameters.AttackDistance m_saved_attack_distance;
	private boolean m_is_bonus;
	protected String m_object_to_avoid = null;
	
	@Override
	public void init(HostileParameters p) {
		
		super.init(p);
		
		// force steal on the way to false: it has its own steal algorithm
		
		p.steal_on_the_way = false;
		
		m_saved_attack_distance = p.attack_distance;
		
		p.attack_distance = HostileWaveParameters.AttackDistance.Always;
		
	}

		
	@Override
	protected void move(long elapsed_time) 
	{
		update_aim();
		
		super.move(elapsed_time);
		
		//debug("aim met: "+m_aim_met+" dormant: "+is_dormant());
		
	}

	private void update_aim()
	{
		if (m_params.object_held != null)
		{
			// thief holds an item
			
			if (m_is_bonus)
			{
				// check for an item, which would have priority and change aim
				
				Sector<GfxObject>.Distance closest_item = m_params.level.items_sector_set.closest(this);
				if (closest_item.item != null)
				{
					if (closest_item.square_distance < m_max_theft_object_distance)
					{
						set_aim(closest_item.item);
						m_is_bonus = false;
					}
				}
			}
		}
		else
		{
			// thief does not hold any item
			
			int d = m_max_theft_object_distance;
			GfxObject aim = null;
			m_is_bonus = false;
			
			Sector<GfxObject>.Distance closest_bonus = m_params.level.bonus_sector_set.closest(this);
			if (closest_bonus.item != null)
			{
				if (closest_bonus.square_distance < d)
				{
					d = closest_bonus.square_distance;

					aim = closest_bonus.item;
					m_is_bonus = true;
				}
			}

			Sector<GfxObject>.Distance closest_item = m_params.level.items_sector_set.closest(this);
			if (closest_item.item != null)
			{
				if (closest_item.square_distance < d)
				{
					d = closest_item.square_distance;
					aim = closest_item.item;
					m_is_bonus = false;
				}
			}
			if (d < m_max_theft_object_distance)
			{
				set_aim(aim);
				
				get_bounds(m_work_rectangle);
				if (m_work_rectangle.contains(aim.get_x_center(),aim.get_y_center()))
				{
					if (m_object_to_avoid == null || !aim.get_name().equals(m_object_to_avoid))
					{
						// steal the item

						m_params.level.remove_object(aim);

						m_params.object_held = aim;

						switch (m_saved_attack_distance)
						{

						case Always:
						case Close:
						case Closer:

							// attack the hero now that he owns the object

							set_aim(m_params.hero);
							break;

						case Trajectory:
						case None:
						{
							// reset to saved behaviour

							m_params.attack_distance = m_saved_attack_distance;
							set_dormant(true);			
						}			
						break;
						}
					}
				}
			}
		}
		
		handle_attract_monster(); // maybe not necessary
	}

}
