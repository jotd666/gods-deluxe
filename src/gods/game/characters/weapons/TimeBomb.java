package gods.game.characters.weapons;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import gods.base.AnimatedFrames;
import gods.base.LevelData;
import gods.game.MonsterLayer;
import gods.game.SfxSet.Sample;
import gods.game.characters.Hero;
import gods.sys.AngleUtils;

public class TimeBomb extends HeroWeapon 
{

	@Override
	public void set_state(State s) 
	{
		switch (s)
		{
		case HURTING_HOSTILE:
			super.set_state(State.VANISHING); // force for explosion render and enable collision
			break;
			default:
			super.set_state(s);
			break;
		}
	}

	
	@Override
	public boolean can_hurt_hostiles()
	{
		// time bomb only hurts when exploding, not when alive
		
		return m_state == State.VANISHING;
	}
	
	@Override
	public void render(Graphics2D g)
	{		
		if (m_state == State.VANISHING)
		{
			int nb_explosions = 2;
			int delta_y = m_height/2;
			
			int y_start = get_y() - (nb_explosions-1)*delta_y;
			
			for (int i = 0; i < nb_explosions; i++)
			{
				// render another explosion above the current one, with shifted frames

				int next_frame = m_frame_counter+i;

				if (next_frame > 0 && next_frame <= m_source.get_nb_frames())
				{
					g.drawImage(m_source.get_frame(next_frame).toImage(),get_x(), y_start + (i*delta_y),null);
				}
			}
		}
		else
		{
			super.render(g);
		}
	}

	@Override
	public boolean can_destroy_spikes() 
	{
		return true;
	}

	private double m_speed_x,m_speed_y;
	private final static double speed_x = 0.15;
	private final static double speed_y = 0.15;
	private int m_nb_bounces = 0;
	private int m_timeout = 2000;
	private boolean m_start_timer = false;
	private Rectangle m_work_rectangle = new Rectangle();
	private Hero m_hero;
	private boolean m_play_bounce_on_wall_right = true;
	private boolean m_play_bounce_on_wall_left = true;
	
	static private final int MAX_NB_BOUNCES = 4;
	
	@Override
	protected void move(long elapsed_time) 
	{
		if (m_nb_bounces < MAX_NB_BOUNCES)
		{
			double x = m_x;
			double y = m_y;
			m_x += m_speed_x * elapsed_time;
			m_y += m_speed_y * elapsed_time;
			
			m_speed_y += elapsed_time / 2000.0;

			int x_boundary = get_x_boundary((int)x);
			int y_boundary = get_y_boundary((int)y);

			boolean play_bounce = false;
			
			if (is_wall_hit(x_boundary,y_boundary))
			{
				// bounce against wall and symmetrize
				m_speed_x = -m_speed_x;
				m_x = 2*x - m_x;
				
				// avoid crazy repeating sound like a buzzer
				if ((m_speed_x < 0) && m_play_bounce_on_wall_right)
				{
					play_bounce = true;
					m_play_bounce_on_wall_right = false;
				}
				else if ((m_speed_x > 0) && m_play_bounce_on_wall_left)
				{
					play_bounce = true;
					m_play_bounce_on_wall_left = false;
				}
				
			}
			
			
			if (is_ground_hit(x_boundary,y_boundary) && 
					is_ground_hit(get_x_opposite_boundary((int)x),y_boundary) && 
					(m_nb_bounces < MAX_NB_BOUNCES))
			{
				m_play_bounce_on_wall_left = true;
				m_play_bounce_on_wall_right = true;
				
				// fix x

				m_speed_x = 0;

				// bounce

				m_speed_y = -m_speed_y;

				// damp
				m_speed_y *= 0.5;

				m_start_timer = true;
				m_nb_bounces++;

				if (m_nb_bounces >= MAX_NB_BOUNCES)
				{
					m_speed_y = 0;
					// stick to the ground
					m_y = m_level_data.get_grid().get_rounded_y(m_y + m_height) - m_height;

				}

				//m_y = 2*y - m_y;

				y_boundary += m_y - y;

				play_bounce = true;
			}

			if (play_bounce)
			{
				m_sfx_set.play(Sample.bomb_bounce);
			}
		}


		
		if (m_start_timer)
		{
			m_timeout -= elapsed_time;
			if ((m_timeout <= 0) && (m_nb_bounces >= MAX_NB_BOUNCES))
			{
				// adapt to ground explosion
				
				m_y = m_level_data.get_grid().get_rounded_y(m_y + m_height, true) - m_weapon_crash.get_height();
				
				m_x -= (m_weapon_crash.get_width() - m_width) / 2;
				
				// change width & height: those of the explosion
				
				m_width = m_weapon_crash.get_width();
				m_height = m_weapon_crash.get_height();
				
				m_sfx_set.play(Sample.explosion_ground);
				
				// hurt hero a little

				m_hero.get_bounds(m_work_rectangle);

				if (m_work_rectangle.contains(m_x+m_width/2,m_y+m_height/2))
				{
					m_hero.hurt(1);  // resourced from game
				}

				
				// trigger explosion animation
				
				set_state(State.VANISHING);
				
				m_start_timer = false; // lock

			}
		}
	}
	public void init(int shoot_angle, int frame_rate, int power, LevelData level, MonsterLayer ml) 
	{
		super.init(frame_rate, power, level, ml, "time_bomb", AnimatedFrames.Type.FOREVER);
			
		double direction = Math.signum(AngleUtils.cosd(shoot_angle));		
		
		m_speed_x = direction * speed_x;
		m_speed_y = AngleUtils.sind(shoot_angle) * speed_y;
		
		// change weapon crash
		
		m_weapon_crash = level.get_common_palette().lookup_frame_set("ground_enemy_death");

		m_hero = ml.get_hero();
	}
	
}
