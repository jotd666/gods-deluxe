package gods.game.characters.hostiles;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import gods.base.GfxFrame;
import gods.base.GfxFrameSet;
import gods.game.characters.FlyingMonster;
import gods.game.characters.Hero;
import gods.game.characters.Hostile;
import gods.game.characters.HostileParameters;
import gods.sys.AngleUtils;

public class BossLevel4Worm  extends FlyingMonster {

	@Override
	public void set_life_state(LifeState s) 
	{	
		if (s == LifeState.EXPLODING && s != m_life_state)
		{
			m_was_shot = true;
			
			// just exploded: initialize angles
			
			for (int i = 0; i < NB_TAIL_SEGMENTS; i++)
			{
				m_exploding_angle[i] = (int)(Math.random() * 360);
			}
		}
		super.set_life_state(s);
	}
	private GfxFrameSet [] m_head = new GfxFrameSet[2];
	private GfxFrame m_body;
	private int m_mouth_open_frame = 0;
	private static final int NB_TAIL_SEGMENTS = 8;
	private static final int MOUTH_OPEN_PERIOD = 1000;
	private int m_mouth_open_timer = 0;
	private double m_center_distance = 0;
	private Hostile m_boss_head = null;
	private boolean m_was_shot = false;
	private int m_angle_index = 0;
	
	private double [] m_tail_x = new double[NB_TAIL_SEGMENTS];
	private double [] m_tail_y = new double[NB_TAIL_SEGMENTS];
	private int [] m_exploding_angle = new int[NB_TAIL_SEGMENTS];
	
	private static final int DEATH_FRAME_RATE = 50;
	private int m_death_timer = 0;
	private int m_death_frame = 0;
	
	private double m_exploding_speed = 0.15;
	
	public boolean was_shot()
	{
		return m_was_shot;
	}
	
	@Override
	public void init(HostileParameters p) 
	{
		m_appearing_animation = false;

		super.init(p);
		
		m_speed *= 2; // fast but not fast enough
		
		m_head[0] = p.level.get_level_palette().lookup_frame_set("boss_worm_head");
		m_head[1] = p.level.get_level_palette().lookup_frame_set("boss_worm_head_mouth_open");
		m_body = p.level.get_level_palette().lookup_frame_set("boss_worm_body").get_first_frame();
		
		m_center_distance = m_body.get_width() * 0.8;
			
		int angle = m_params.trajectory.get_initial_angle();
		
		// initial params
		
		for (int i = 0; i < NB_TAIL_SEGMENTS; i++)
		{
			m_tail_x[i] = m_x + m_center_distance * (i+1) * AngleUtils.cosd(-angle);
			m_tail_y[i] = m_y + m_center_distance * (i+1) * AngleUtils.sind(-angle);
		}
		
		// square distance
		m_center_distance *= m_center_distance;
		
		
		m_boss_head = m_params.hostile_set.lookup_active_hostile("boss");
		
	}

	@Override
	public void collision(Hero h) 
	{
		// collision with the worm is lethal, does not hurt him
		h.hurt(Hero.MAX_HEALTH*10);
	}

	@Override
	public boolean collision_test(Rectangle other) 
	{
		boolean rval = super.collision_test(other);
		
		if (!rval)
		{
			int margin = m_body.get_width() / 8;
			// no collision with head: check body parts, but be nice
			
			m_work_rectangle.width = m_body.get_width() - margin;
			m_work_rectangle.height = m_body.get_height()- margin;
			
			for (int i = 0; i < NB_TAIL_SEGMENTS && !rval; i++)
			{
				m_work_rectangle.x = (int)m_tail_x[i] + margin;
				m_work_rectangle.y = (int)m_tail_y[i] + margin;
				
				rval = (other.intersects(m_work_rectangle));
			}
		}
		
		return rval;
	}

	@Override
	public boolean is_in_background() 
	{
		return false;
	}

	public void render(Graphics2D g) 
	{
		// we'll render from the boss head (priority/plane issue)
	}
	
	// render is called from the boss (priority problem)
	
	//private static final int [] MOUTH_FRAME_TABLE = { 0, 1, 2, 1 };
	
	void really_render(Graphics2D g) 
	{
		switch (m_life_state)
		{
		case ALIVE:

			int idx = m_mouth_open_frame > 0 ? 1 : 0;

			GfxFrameSet head = m_head[idx];
			
			m_boss_head.get_bounds(m_work_rectangle);

			int x = (int)m_x+head.get_width()/2;
			int y = (int)m_y+head.get_height() - BossLevel4.Y_SKULL_OFFSET;

			if (!m_work_rectangle.contains(x,y))
			{
				/*GfxFrame gf = null;
				if (idx == 0)
				{
					// no open mouth
					
					gf = head.get_frame(m_angle_index + 1);
				}
				else
				{
					// open mouth
					gf = head.get_frame(m_angle_index/2 * MOUTH_FRAME_TABLE[m_mouth_open_frame] + 1);
				}*/
				draw_image(g, head.get_frame(m_angle_index + 1).toImage());

				for (int i = 0; i < NB_TAIL_SEGMENTS; i++)
				{
					x = (int)m_tail_x[i]+m_body.get_width()/2;
					y = (int)m_tail_y[i] - BossLevel4.Y_SKULL_OFFSET;

					if (!m_work_rectangle.contains(x,y))
					{
						g.drawImage(m_body.toImage(),(int)m_tail_x[i],(int)m_tail_y[i],null);
					}
					else
					{
						break; // don't draw the segments anymore
					}
				}
			}
			
			break;
		case EXPLODING:
			BufferedImage b = m_exploding_frame_set.get_frame(m_death_frame + 1).toImage();
			
			for (int i = 0; i < NB_TAIL_SEGMENTS; i++)
			{
				g.drawImage(b,(int)m_tail_x[i],(int)m_tail_y[i],null);
			}
			break;
			
		}
	}

	private void follow(int i, double x, double y)
	{
		double tx = m_tail_x[i];
		double ty = m_tail_y[i];
		double dx = (x - tx);
		double dy = (y - ty);
		
		double sqdist = dx*dx + dy*dy;
		double ratio = Math.sqrt(m_center_distance / sqdist);
		
		// correct dx (homothety so worm segments are at the same distance
		
		m_tail_x[i] = x - dx * ratio;
		m_tail_y[i] = y - dy * ratio;
		
		
	}
	public void update(long elapsed_time) 
	{				
		switch (m_life_state)
		{
		case ALIVE:

			int nb_head_pos = 8;

		m_angle_index = (int)(Math.round(((nb_head_pos * m_head_angle) / 360.0) + nb_head_pos) % nb_head_pos);

		if (m_angle_index % 2 == 0)
		{
			m_mouth_open_timer += elapsed_time;

			while (m_mouth_open_timer > MOUTH_OPEN_PERIOD)
			{
				m_mouth_open_timer -= MOUTH_OPEN_PERIOD;
				
				m_mouth_open_frame++;
				if (m_mouth_open_frame > 3)
				{
					m_mouth_open_frame = 0;
				}

			}
		}
		else
		{
			// no mouth open when not a strict vertical or horizontal
			m_mouth_open_frame = 0;
		}
		// move the head
		super.update(elapsed_time);
		
		// the rest follows
		
		follow(0,m_x,m_y);

		// scroll previous pos
		for (int i = 0; i < NB_TAIL_SEGMENTS - 1; i++)
		{
			follow(i+1,m_tail_x[i],m_tail_y[i]);
		}
		break;
		case EXPLODING:
			// move all segments randomly with death animation
			for (int i = 0; i < NB_TAIL_SEGMENTS; i++)
			{
				m_tail_x[i] += elapsed_time * m_exploding_speed * AngleUtils.cosd(m_exploding_angle[i]);
				m_tail_y[i] += elapsed_time * m_exploding_speed * AngleUtils.sind(m_exploding_angle[i]);
			}
			
			// change exploding frame
			
			m_death_timer += elapsed_time;
			m_death_frame = m_death_timer / DEATH_FRAME_RATE;
			if (m_death_frame >= m_exploding_frame_set.get_nb_frames())
			{
				set_life_state(LifeState.DEAD);
			}
			break;
		}
	}

}
