package gods.game.characters.hostiles;

import java.awt.Graphics2D;


import gods.base.AnimatedFrames;
import gods.base.ControlObject;
import gods.base.GfxFrameSet;
import gods.base.HostileWaveParameters;
import gods.game.characters.Hero;
import gods.game.characters.Hostile;
import gods.game.characters.HostileParameters;
import gods.game.characters.weapons.HeroWeapon;

public class BeeHive extends Hostile 
{
	@Override
	public boolean is_in_background() {
		
		return true;
	}

	private GfxFrameSet m_frame_set;
	private int m_frame_counter = 1;
	
	public BeeHive() 
	{

	}

	@Override
	public void collision(Hero h) 
	{
		h.hurt(m_params.health_points);
	}

	@Override
	public boolean hit(HeroWeapon hw)
	{
		boolean rval = false;
		
		if (hw == null)
		{
			set_life_state(LifeState.EXPLODING);
		}
		
		rval = super.hit(hw);
				
		return rval;
	}
	public void render(Graphics2D g) 
	{
		switch (m_life_state)
		{
		case ALIVE:
			g.drawImage(m_frame_set.get_frame(m_frame_counter).toImage(),get_x(),get_y(),null);
			if (!m_appearing_frames.is_done())
			{
				m_appearing_frames.render(g);
			}
		break;
		case EXPLODING:
			g.drawImage(m_exploding_frame_set.get_frame(m_counter/2+1).toImage(), get_x(), get_y(), null);
			break;
		}
		
		
	}
	
	
	private int m_counter;
	private GfxFrameSet  m_exploding_frame_set;
	private AnimatedFrames m_appearing_frames;
	private int m_timer;
	private static final int UPDATE_RATE = 50;
	private static final int SHOOT_PERIOD = 80; // 4 seconds
	
	private int m_delta_height,m_delta_width;
	
	private void spawn_bee()
	{
		HostileWaveParameters hwp = new HostileWaveParameters(new ControlObject(m_params.location),m_params.level);
		
		hwp.class_name = "gods.game.characters.hostiles.FlyingBee";
								
		hwp.direction = HostileWaveParameters.Direction.Random;
		
		hwp.frame_set_name = "bee_side";
		
		hwp.object_to_drop = null;
		
		hwp.health_points = 20;
		
		// speed inherits the speed set from the beehive, this way it can be set using
		// the editor
		
		hwp.move_speed = m_params.move_speed;

		m_params.hostile_set.spawn_wave(hwp);
	}
	public void update(long elapsed_time) 
	{
		if (is_in_screen(0))
		{
			m_timer += elapsed_time;
			while (m_timer > UPDATE_RATE)
			{
				m_timer -= UPDATE_RATE;

				switch (m_life_state)
				{
				case ALIVE:
					if (!m_appearing_frames.is_done())
					{
						m_appearing_frames.update(elapsed_time);
					}

					m_counter++;
					switch (m_counter)
					{
					case SHOOT_PERIOD-8:
					case SHOOT_PERIOD-5:
					case SHOOT_PERIOD-2:
						m_frame_counter++;
						break;
					case SHOOT_PERIOD:
						m_counter = 0;
						m_frame_counter = 1;
						spawn_bee();
						break;
					}
					break;
				case EXPLODING:
					m_counter++;
					if (m_counter/2+1 == m_exploding_frame_set.get_nb_frames())
					{
						m_life_state = LifeState.DEAD;
					}
					break;
				}

			}
		}
	}
	
	public void set_life_state(LifeState s)
	{
		super.set_life_state(s);
		if (s == LifeState.EXPLODING)
		{
			m_counter = 0;
			
			m_x -= (m_exploding_frame_set.toImage().getWidth() - m_width) / 2;
			m_y -= m_delta_height; 
		}
	}
	
	@Override
	public void init(HostileParameters p)
	{
		//p.score_points = 25; // 40 health points = 1000 points
		
		super.init(p);
		
		m_frame_set = m_params.level.get_level_palette().lookup_frame_set(p.frame_set_name);
		
		// adjust height
		
		m_delta_height = p.location.get_height() - m_frame_set.get_height();
		
		m_delta_width = m_width / 3; // sprite has a lot of blank to the right
		
		m_width -= m_delta_width;
		
		m_y += m_delta_height;
		m_height -= m_delta_height;
		
		
		m_appearing_frames = new AnimatedFrames();
		
		m_appearing_frames.init(m_params.level.get_common_palette().lookup_frame_set("bonus_taken_smoke"),
				UPDATE_RATE,AnimatedFrames.Type.REVERSE);
		m_exploding_frame_set = m_params.level.get_common_palette().lookup_frame_set("ground_enemy_death");


		m_appearing_frames.set_coordinates(this);


		m_life_state = LifeState.ALIVE;

	}
}
