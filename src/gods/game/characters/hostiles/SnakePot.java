package gods.game.characters.hostiles;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import gods.base.AnimatedFrames;
import gods.base.ControlObject;
import gods.base.GfxFrameSet;
import gods.base.GfxPalette;
import gods.base.HostileWaveParameters;
import gods.game.characters.HostileParameters;
import gods.game.characters.LeftRightHostile;
import gods.game.characters.weapons.HostileStraightWeapon;
import gods.game.characters.weapons.Projectile;

public class SnakePot extends LeftRightHostile {

	@Override
	public boolean is_in_background() {
		
		return false;
	}
	private int m_shoot_anim = -1;
	private int m_shoot_timer = 0;

	private enum HeadState { IN_POT, RISING, OUT, TURNING }
	
	private HeadState m_head_state;
	
	private int m_y_pot, m_y_ground, m_head_state_counter = 0;
	private GfxFrameSet m_pot, m_rising_head_frames;
	private AnimatedFrames m_rising_head;
	
	public SnakePot() 
	{
		
	}

	@Override
	public void init(HostileParameters p)
	{
		// force firetype to straight
		p.fire_type = HostileWaveParameters.FireType.Straight;

		// tricky: define dropped snakes now (because it would be very tedious in the editor for each
		// snake pot)
		
		HostileWaveParameters hwp = new HostileWaveParameters(new ControlObject(p.location),p.level);
		
		hwp.class_name = "gods.game.characters.hostiles.MovingSnake";
		
		p.hostile_to_drop = hwp;
		
		/*ObjectToDrop otd = new ObjectToDrop();
		otd.class_name = "gem";*/
		
		// 4 snakes
		
		hwp.object_to_drop = null;
		
		hwp.direction = HostileWaveParameters.Direction.Random;
		
		hwp.nb_enemies = 3;
		
		hwp.frame_set_name = "moving_snake";
		
		// kind of shoot shield, but without the score increase
		// (MovingSnake is initialised later with only 1000 points, and 10 points of damage)
		
		hwp.health_points = 100;
		hwp.damage_points = 6;  // exact value ripped from game
		
		// speed inherits the speed set for the snake pot, this way it can be set using
		// the editor
		
		hwp.move_speed = p.move_speed;
		
		super.init(p);
		
		// make it touch the ground if control object touches the ground
		
		GfxPalette level_palette = m_params.level.get_level_palette();
		
		m_pot = level_palette.lookup_frame_set("snake_pot");
		m_rising_head_frames = level_palette.lookup_frame_set("snake_rising_head");
		
		
		m_y_pot = get_y() + m_params.location.get_height() - m_pot.get_height();
		m_y = m_y_pot - m_frame_set[0].get_height();
		
		m_y_ground = m_y_pot + m_pot.get_height();
		
		m_rising_head = new AnimatedFrames();
		m_rising_head.init(m_rising_head_frames, 100, AnimatedFrames.Type.ONCE);
		m_rising_head.set_coordinates(this);
		
		m_head_state = HeadState.IN_POT;
		
	}
	

	
	@Override
	public void render(Graphics2D g) 
	{
		switch (m_life_state)
		{
		case EXPLODING:
			int y_save = (int)m_y;
			m_y = m_y_ground - m_exploding_frame_set.get_height();
			super.render(g);
			m_y -= m_exploding_frame_set.get_height();
			super.render(g);
			m_y = y_save;
			break;
		case ALIVE:
			// render pot

			g.drawImage(m_pot.toImage(), (int)m_x, m_y_pot, null);

			switch (m_head_state)
			{
			case IN_POT:
				m_rising_head.render(g);
				break;

			case RISING:
				m_rising_head.render(g);
				break;

			case TURNING:
			case OUT:
				super.render(g);
				break;
			}
			break;

		default:
			super.render(g);
		    break;
		}


	}

	private int m_animation_timer = 0;
	private static final int FRAME_RATE = 100;
	
	@Override
	protected void animate(long elapsed_time)
	{
		m_animation_timer += elapsed_time;
		
		while (m_animation_timer < FRAME_RATE)
		{
			m_animation_timer -= FRAME_RATE;

			if (m_head_state == HeadState.OUT)
			{
				m_shoot_timer++;

				if (m_shoot_timer > 2)
				{
					m_shoot_timer = 0;
					//m_shoot_counter++;
				}

				// do nothing except when shooting
				switch (m_shoot_anim)
				{
				case 0:			
					m_frame_counter++;
					break;
				case 2:
					m_frame_counter--;
					break;
				case 4:
					m_shoot_anim = -1;
					break;
				}
				if (m_shoot_anim >= 0)
				{
					m_shoot_anim++;
				}	
			}
		}
	}
	@Override
	protected void move(long elapsed_time) 
	{
		if (is_in_screen(0))
		{
			switch (m_head_state)
			{
			case IN_POT:
				m_head_state_counter += elapsed_time;
				if (m_head_state_counter > 3000)
				{
					m_head_state = HeadState.RISING;
					m_head_state_counter = 0;
				}
				break;
			case RISING:
				m_rising_head.update(elapsed_time);
				if (m_rising_head.is_done())
				{
					m_head_state = HeadState.TURNING;
					m_head_state_counter = 0;
					m_frame_counter = 1;
				}
				break;
			case TURNING:
				m_head_state_counter += elapsed_time;
				if (m_head_state_counter > 200)
				{
					m_head_state = HeadState.OUT;
					m_frame_counter = 2;
					m_head_state_counter = 0;
				}
			case OUT:
				int dx = m_params.hero.get_x() - get_x();
				if ((dx < 0) && (m_right_left == 0))						
				{
					m_head_state = HeadState.TURNING;
					m_right_left = 1;
				}
				else if ((dx > 0) && (m_right_left == 1))
				{
					m_head_state = HeadState.TURNING;					
					m_right_left = 0;
				}
				break;
			}
		}
	}
	@Override
	public void get_bounds(Rectangle r) 
	{
		// special case here: we'll reduce width so detection is less frustrating
		// just like the original game
		int w2 = 4;
		r.x = get_x()+4;
		r.y = get_y();
		r.width = m_width-w2*2;
		r.height = m_height;
	}
	@Override
	protected Projectile shoot() 
	{
		boolean to_left = m_right_left != 0;
		int x = get_x();// + (to_left ? 0 : (m_width));
		int power = 4;
		
		HostileStraightWeapon p = new HostileStraightWeapon();
		p.init(12,50,power,to_left,
				m_params.level,m_params.hero,m_fire_frames[m_right_left]);
		
		p.set_coordinates(x,m_y + 8);
		
		m_params.weapon_set.add(p);

		m_shoot_anim = 0; // start shoot anim
		
		return p;
	}

	protected boolean may_shoot()
	{
		return super.may_shoot() && (m_head_state == HeadState.OUT);
	}


}

