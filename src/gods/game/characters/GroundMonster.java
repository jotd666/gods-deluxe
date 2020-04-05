package gods.game.characters;

import gods.base.GfxObject;
import gods.base.HostileWaveParameters;
import gods.base.LevelData;
import gods.base.NamedLocatable;
import gods.base.layer.TileGrid;
import gods.game.ParabolicMobileBehaviour;



public abstract class GroundMonster extends LeftRightHostile
{
	protected int m_tile_height;
	protected int m_tile_width;
	protected int m_jump_x_wall_distance;
	protected int m_max_jump_x;
	protected int m_max_jump_y;
	private int m_close_to_wall_nb_iterations;
	private NamedLocatable m_aim;
	private boolean m_dormant = true;
	private LevelData m_level;
	private TileGrid m_grid;
	private boolean m_last_frame_reserved;
	private boolean m_allow_homing_flip = true;
	protected int m_speed;
	private int [] m_x_flip = new int[2];
	private int [] m_y_flip = new int[2];
	private boolean m_break_monotony = false;
	
	@Override
	public boolean is_in_background() {
		
		return false;
	}	
	private class PossibleJumpParams
	{
		public int dx, dy;
		public boolean jump_on_aim;
		
		public void copy_from(PossibleJumpParams other)
		{
			dx = other.dx;
			dy = other.dy;
			jump_on_aim = other.jump_on_aim;
		}
		
		public void check_jump()
		{
			if (jump_on_aim)
			{
				// aim is above: try to jump
				int jump_height = -dy + m_tile_height; //-(3*dy)/2;
				int jump_width = Math.min(m_max_jump_x,Math.abs(dx));
		
				m_simulated_jump.init(jump_width,jump_height,dx);
		
				if (!m_simulated_jump.successful_jump())
				{
					debug("jump simulation failed "+dx);
					jump_on_aim = false;
				}
				else
				{
					debug("jump simulation OK");
				}
					
			}
		}
		public void jump_if_high_platform_found(int pdx, int pdy, int x_check, int x_dist, int wd)
		{
			dx = pdx;
			dy = pdy;
			
			jump_on_aim = false;

			int y_check = m_grid.get_rounded_y(m_y - m_tile_height*2);

			//	compute best jump height
			// starting from lowest to highest until we find a free passage

			int y_step = m_tile_height;

			int max_dy = Math.max(-m_max_jump_y, dy - m_height);

			dy = 0;//-m_height;

			while (dy > max_dy)
			{
				y_check = m_grid.get_rounded_y(m_y + dy);

				// check first platform to jump on

				if ((m_level.is_vertical_way_blocked(x_check,y_check + m_tile_width,m_width)) &&
						(!m_level.is_lateral_way_blocked(x_check,y_check,m_height)))
				{
					jump_on_aim = true;
					// delta x is minimum of max x jump and x_distance (but not less than tile width)
					// add sign here too (hell this is not clear!!)
					dx = Math.max(x_dist,m_tile_width) * wd;
					// dx = Math.min(dx,Math.max(x_dist,m_tile_width)) * wd;  // this causes too many failed jumps (test 19)
					
					break;
				}
				else
				{
					dy -= y_step;
				}
			}
			debug("jump (high platform): "+jump_on_aim+",dx = "+dx+",dy = "+dy+",max_dy = "+m_max_jump_y);
			
			// avoid ridiculously low jumps (hero at the same level than the hostile, case where dy = 0)
			
			if (dy > -m_height)
			{
				dy = -m_height;
			}
		}
	
		
		public void jump_if_wall_close_enough(int pdx, int pdy, int px_check, int x_dist, int wd)
		{
			int h_check = m_height + m_tile_height*2;
			int x_check = px_check;
			
			dx = pdx;
			dy = pdy;
			
			boolean very_close_to_wall = (Math.abs(dx) < m_speed);
			
			if (very_close_to_wall)
			{
				// stuck to the wall: round x_check and add/sub 1
				
				x_check = m_grid.get_rounded_x(x_check) + wd;
			}
			
			int y_check = m_grid.get_rounded_y(m_y - m_tile_height*2);
			jump_on_aim = false;
			
			if (m_level.is_lateral_way_blocked(x_check,y_check,h_check))
			{
				// wall in front of hostile: compute best jump height
				// starting from lowest to highest until we find a free passage

				int y_step = m_tile_height;

				if (very_close_to_wall)
				{
					// hero just above hostile: check if way up is clear till we reach the
					// hero, even if jump won't necessarily be enough to get him

					int max_dy = dy;

					// restore x_check rounded value

					x_check -= wd;

					dy = -m_height;

					jump_on_aim = true;

					while (dy > max_dy)
					{
						y_check = m_grid.get_rounded_y(m_y + dy);

						if (m_level.is_vertical_way_blocked(x_check,y_check,m_width))
						{
							break;
						}
						else
						{
							dy -= y_step;
						}
					}
					
					if (jump_on_aim)
					{
						// no block found on the way: jump is possible
						
						// set delta x to zero
						
						dx = 0;

						// bound dy to max jump height
						
						dy = Math.max(-m_max_jump_y, dy);
					}
				}
				else
				{

					int max_dy = Math.max(-m_max_jump_y, dy - m_height);

					dy = -m_height;

					while (dy > max_dy)
					{
						y_check = m_grid.get_rounded_y(m_y + dy);

						if (!m_level.is_lateral_way_blocked(x_check,y_check,m_height))
						{
							jump_on_aim = true;
							dx = Math.max(x_dist,m_tile_width) * wd;
							break;
						}
						else
						{
							dy -= y_step;
						}
					}
					
				}
				
				debug("jump (close to wall) from "+ (wd < 0 ? "left" : "right") +": "+jump_on_aim+",dx = "+dx+",dy = "+dy+
						",max_dy = "+m_max_jump_y+",x = "+m_x+",x_check = "+x_check);
			}
			
			
		}
	}
	
	private PossibleJumpParams m_jump_left = new PossibleJumpParams();
	private PossibleJumpParams m_jump_right = new PossibleJumpParams();
	private PossibleJumpParams m_jump_best = new PossibleJumpParams();
	
	protected GroundMonster(boolean last_frame_reserved, int [] stolen_object_xy_offset)
	{
		m_last_frame_reserved = last_frame_reserved;
		m_stolen_object_xy_offset = stolen_object_xy_offset;
	}
	
	protected NamedLocatable get_aim()
	{
		return m_aim;
	}
	

	public boolean is_dormant() {
		return m_dormant;
	}


	public void set_dormant(boolean dormant) 
	{
		this.m_dormant = dormant;
	}
	
	protected void set_aim(NamedLocatable aim)
	{
		if (m_aim != null)
		{
			if ((m_aim != aim) && (aim != null))
			{
				debug("change aim to "+aim.toString());
			}
		}
		
		m_aim = aim;
		
	}
	
	@Override
	protected void on_land()
	{
		// reset to dormant state once landed if attack mode is "close" or "closer"
		
		switch (m_params.attack_distance)
		{
		case Close:
		case Closer:
			set_dormant(true);
			break;
		}
		
		// recompute falling flag in case the hostile lands on a hole
		
		update_falling_flag();

	}

	private void elect_jump_left(int wd)
	{
		int idx = 0;

		// to left

		for (int i = 0; i <= m_close_to_wall_nb_iterations; i++)
		{
			idx = i;

			int x_dist = i*m_width;
			int x_check = (int)m_x - x_dist/2;

			// is the wall close enough to jump?

			m_jump_left.jump_if_wall_close_enough(m_jump_best.dx,m_jump_best.dy,x_check,x_dist,-1);

			if (m_jump_left.jump_on_aim)
			{
				// jump is possible with those parameters

				debug("jump possible to left: "+m_jump_left.dx+" "+m_jump_left.dy);

				break;
			}
		}

		if ((wd > 0) && (idx < m_close_to_wall_nb_iterations))
		{
			// cancel the jump because opposite and too close to the wall
			m_jump_left.jump_on_aim = false;
			debug("cancel jump left: "+idx);
		}
		
		m_jump_left.check_jump();
	}
	
	
	private void elect_jump_right(int wd)
	{
		int idx = 0;

		for (int i = 0; i <= m_close_to_wall_nb_iterations; i++)
		{
			idx = i;

			int x_dist = i*m_width;

			int x_check = (int)m_x + m_width + x_dist/2;

			// is the wall close enough to jump?

			m_jump_right.jump_if_wall_close_enough(m_jump_best.dx,m_jump_best.dy,x_check,x_dist,1);

			if (m_jump_right.jump_on_aim)
			{
				// jump is possible with those parameters

				debug("jump possible to right: "+m_x+","+m_y+": "+m_jump_right.dx+" "+m_jump_right.dy);

				break;
			}
		}

		
		if ((wd < 0) && (idx < m_close_to_wall_nb_iterations))
		{
			// cancel the jump because opposite and too close to the wall
			
			m_jump_right.jump_on_aim = false;
			debug("cancel jump right at iteration "+idx);
		}	
	
		m_jump_right.check_jump();
			
	}
	private void elect_jump_left_from_ledge()
	{
		int x_dist = m_jump_x_wall_distance;
		int x_check = (int)m_x - x_dist/2;

		// is the wall close enough to jump?

		m_jump_left.jump_if_high_platform_found(m_jump_best.dx,m_jump_best.dy,x_check,x_dist,-1);

		if (m_jump_left.jump_on_aim)
		{
			// jump is possible with those parameters

			debug("jump from ledge possible to left: "+m_x+","+m_y+": "+m_jump_right.dx+" "+m_jump_right.dy);

			
		}

		m_jump_left.check_jump();
			
	}

	private void elect_jump_right_from_ledge()
	{
		int x_dist = m_jump_x_wall_distance;
		int x_check = (int)m_x + m_width + x_dist/2;

		// is the wall close enough to jump?

		m_jump_right.jump_if_high_platform_found(m_jump_best.dx,m_jump_best.dy,x_check,x_dist,1);

		if (m_jump_right.jump_on_aim)
		{
			// jump is possible with those parameters

			debug("jump from ledge possible to right: "+m_x+","+m_y+": "+m_jump_right.dx+" "+m_jump_right.dy);	
		}

		m_jump_right.check_jump();
			
	}
	private void init_best_jump(int pdx, int pdy)
	{
		m_jump_best.dx = pdx;
		m_jump_best.dy = pdy;
		m_jump_best.jump_on_aim = false;
	}
	
	private void update_right_left_index(int wd)
	{
		set_right_left(wd > 0 ? 0 : 1);
	}
	
	private void elect_best_jump(int pdx, int pdy)
	{
		init_best_jump(pdx,pdy);

		// do not perform short distance test if looking for a jump
		// in the opposed walk direction

		// aim above hostile, same sign, check for a wall to jump over
		// starting from close to the wall to a further distance
		// if hostile is stuck against a wall, it still jumps

		int wd = get_walk_direction();
		
		// to left
		
		elect_jump_left(wd);
		
		// to right
		
		elect_jump_right(wd);

		// will jump if left or right

		m_jump_best.jump_on_aim = m_jump_left.jump_on_aim || m_jump_right.jump_on_aim;

		if (m_jump_left.jump_on_aim)
		{
			if (m_jump_right.jump_on_aim)
			{
				// if there's too much difference, 
				// choose the shortest jump in height (don't forget dy is negative)

				/*int delta_dy = m_jump_right.dy - m_jump_left.dy;
				
				if (delta_dy > -m_tile_height)
				{
					// right jump is the best

					m_jump_best.copy_from(m_jump_right);
				}
				else if (delta_dy < m_tile_height)
				{
					// left jump is the best
					
					m_jump_best.copy_from(m_jump_left);					
				}
				else*/
				{
					// roughly same height for both: choose the side according to dx

					if (pdx > 0)
					{
						m_jump_best.copy_from(m_jump_right);
					}
					else
					{
						m_jump_best.copy_from(m_jump_left);						
					}

				}
			}
			else
			{
				//	left jump is the only option

				m_jump_best.copy_from(m_jump_left);						

			}
		}
		else
		{
			if (m_jump_right.jump_on_aim)
			{
				// right jump is the only option

				m_jump_best.copy_from(m_jump_right);						

			}
		}
	}
	/**
	 * check if L-path is reachable
	 * @param dx
	 * @param dy
	 * @return true if no L path (up+lateral or lateral+up) or (lateral+down) is found from monster to aim
	 */
	private boolean blocked_l_path(int dx, int dy)
	{
		boolean rval = true;

		if (dy < 0)
		{
			rval = (blocked_path_lateral_up(dx,dy) && blocked_path_up_lateral(dx, dy));
		}
		else
		{
			rval = blocked_path_lateral_down(dx,dy);
		}
		return rval;
	}	
	private boolean blocked_path_up_lateral(int dx, int dy)
	{
		boolean rval = (-dy > m_max_jump_y);

		if (!rval)
		{
			// x-centered because we want to look for times above (otherwise problems on ledges)
			int current_x = get_x() + m_width/2; 
			int x_sign = dx < 0 ? -1 : 1;


			int current_y = get_y() + m_height;
			int current_dy = dy - m_height;
			int current_dx = dx;

			// aim above: start by vertical step
			while (!rval && current_dy < 0)
			{
				rval = m_level.is_vertical_way_blocked(current_x, current_y - m_tile_height, m_width);
				if (!rval)
				{
					// free
					current_dy += m_tile_height;
					current_y -= m_tile_height;
				}
			}
			// then lateral step
			while (!rval && current_dx * x_sign > 0)
			{
				rval = m_level.is_lateral_way_blocked(current_x, current_y - m_height, m_height);
				if (!rval)
				{
					// free
					current_dx -= m_tile_width * x_sign;
					current_x += m_tile_width * x_sign;
				}
			}

		}

		return rval;
	}

	private boolean blocked_path_lateral_up(int dx, int dy)
	{
		// aim is above: check if jump is possible, else consider path blocked

		boolean rval = -dy > m_max_jump_y;

		if (!rval)
		{
		int x_sign = dx < 0 ? -1 : 1;
		
		int current_y = get_y();
		int current_x = get_x();
		int current_dy = dy;
		int current_dx = dx;

		if (x_sign > 0)
		{
			current_x += m_width;
		}

		// first lateral step
		while (!rval && current_dx * x_sign > 0)
		{
			rval = m_level.is_lateral_way_blocked(current_x, current_y, m_height);
			if (!rval)
			{
				// free
				current_dx -= m_tile_width * x_sign;
				current_x += m_tile_width * x_sign;
			}
		}
		
		// round it so it matches the encountered wall
		
		if (!rval)
		{
			current_x = m_grid.get_rounded_x(current_x, (x_sign < 0));
		}
		
		// aim above: vertical step
		while (!rval && current_dy < 0)
		{
			rval = m_level.is_vertical_way_blocked(current_x, current_y - m_tile_height, m_width);
			if (!rval)
			{
				// free
				current_dy += m_tile_height;
				current_y -= m_tile_height;
			}
		}

		}
		return rval;
	}
		
	private boolean blocked_path_lateral_down(int dx, int dy)
	{
		boolean rval = false;

		// check blocked path only on lowest half of the hostile
		int half_height = m_height / 2;
		int current_x = get_x();
		int current_y = get_y() + m_height + half_height;
		int current_dy = dy;
		int x_sign = dx < 0 ? -1 : 1;
		// more tolerant
		int current_dx = dx - x_sign * m_tile_width;
				
		// lateral step
		while (!rval && current_dx * x_sign > 0)
		{
			rval = m_level.is_lateral_way_blocked(current_x, current_y - m_height, half_height);
			if (!rval)
			{
				// free
				current_dx -= m_tile_width * x_sign;
				current_x += m_tile_width * x_sign;
			}
		}
		// then vertical step
		while (!rval && current_dy > 0)
		{
			rval = m_level.is_vertical_way_blocked(current_x, current_y - m_height, m_width);
			if (!rval)
			{
				// free
				current_dy -= m_tile_height;
				current_y += m_tile_height;
			}
		}
		
		return rval;
	}

	protected boolean m_aim_met = false;
	
	protected void smart_move(long elapsed_time) 
	{		
		common_move(elapsed_time);
		
		handle_attract_monster();
		

		boolean jump_on_aim = false;

		int dx = m_aim.get_x_center() - get_x_center();
		int abs_dx = Math.abs(dx);

		// corrected dy to get 0 if walking on a platform of the same level, rounded on even values
		// to avoid +1/-1 delta (because some objects have an odd y value because of the fine
		// resolution of the vertical fall move): I want to avoid a too small jump

		int dy = (((m_aim.get_y() + m_aim.get_height()) - (get_y() + m_height)) / 2) * 2;
		int abs_dy = Math.abs(dy);
		// we backup dx,dy because dx/dy are also used to initialize jump w/h
		// not very clear sorry, but this is the first change in the AI for months/years! (v0.7a)
		// and I don't want to break everything
		int aim_dy = dy;
		int aim_dx = dx;
		
		if (!m_aim_met)
		{
			m_aim_met = abs_dx < m_aim.get_width() && abs_dy < m_aim.get_height();
		}

		if (m_aim_met && m_params.attack_distance == HostileWaveParameters.AttackDistance.Trajectory)
		{
			// we have met the aim (case of predefined trajectory)
			// -> don't move from the current platform anymore until
			// something else appears (attract monster? trap opens?)
			set_dormant(true);
			
			m_params.attack_distance = HostileWaveParameters.AttackDistance.None;
		}
		
		boolean lateral_way_blocked = is_lateral_way_blocked();

		int wd = get_walk_direction();

		boolean on_ledge = false;
		boolean aim_below_not_facing = false;
		
		boolean faces_aim = dx * wd > 0;

		// not falling: jump or walk?
		//
		// extra test to avoid that hostile turns back immediately when landing on
		// a platform when turning the back to the aim

		if (!is_falling() && (faces_aim || is_vertical_way_blocked((int)m_x + m_width/2)))
		{
			if (abs_dy < m_tile_height)
			{
				// flip to meet target

				if (m_allow_homing_flip)
				{
					set_right_left(dx < 0 ? 1 : 0);
					m_allow_homing_flip = false;
				}

				if (m_aim_met)
				{
					// allows to move around aim if aim was met once

					m_allow_homing_flip = abs_dx > m_tile_width;
				}
			}
			else
			{
				m_allow_homing_flip = true;
			}


			if (!is_vertical_way_blocked() && !lateral_way_blocked)  { // on ledge 

				on_ledge = true;

				if (aim_dy >= m_tile_height/2)
				{
					// aim is below
					
					// do nothing: fall down
					// unless jump is better

					// check if hole ahead of hostile: in that case, it would have to jump

					int y_hole = get_y() + m_height + m_tile_height;
					int y_hole_max = m_aim.get_y() + m_aim.get_height() + m_tile_height/2;

					int x_hole = 0;
					if (wd == 1)
					{
						x_hole = m_grid.get_rounded_x(m_x + m_width,false);
					}
					else
					{
						x_hole = m_grid.get_rounded_x(m_x,true) - m_tile_width;
					}

					boolean hole_ahead = true;

					while (hole_ahead && y_hole <= y_hole_max)
					{
						hole_ahead = !m_level.is_vertical_way_blocked(x_hole,y_hole,m_width);
						if (hole_ahead)
						{
							y_hole += m_tile_height;
						}
					}

					if (hole_ahead)
					{
						debug("hole found: nb_tiles="+(m_y+m_height-y_hole)/m_tile_height+" aim tiles="+(y_hole_max-y_hole)/m_tile_height);
						// avoid that the hostile jumps in the air while there's some
						// ceiling above
					}
					else
					{
						// jump to avoid being stuck

						if (m_break_monotony)
						{
							debug("break monotony by jumping");
							m_break_monotony = false;
							hole_ahead = true;
						}
					}

					if (hole_ahead)
					{
						double x_check = wd == 1 ? m_x : m_x - m_tile_width;

						jump_on_aim = !m_params.level.is_vertical_way_blocked(x_check, m_y - m_tile_height, m_tile_width*2);
						if (jump_on_aim)
						{
						// default: small jump (will be corrected later if necessary)
						m_jump_best.dy = -m_height / 4;
						m_jump_best.dx = m_max_jump_x * wd;
						dx = m_jump_best.dx;
						dy = m_jump_best.dy;
						}
					}
					
				}
				else if (aim_dy <= 0)
				{
					// aim is above
					// try to find the best jump

					init_best_jump(m_max_jump_x * wd, -m_max_jump_y);

					// try to see if monster is on a small platform: ledges on both sides
					// compute x_limit and round it

					/*
					int x_opposite_limit = 0;

					if (m_right_left == 1)
					{
						// to the left
						x_opposite_limit = m_grid.get_rounded_x(m_x + m_width,true);
					}
					else
					{
						// to the right
						x_opposite_limit = m_grid.get_rounded_x(m_x,false);
					}

					// cancel on_ledge flag if ledge on the other side too
					// -> no priority given to the walk direction

					on_ledge = is_vertical_way_blocked(x_opposite_limit);

					if (!on_ledge)
					{ 
						// sometimes dual ledge is detected, but that's not really the case since there's a
						// wall blocking like this:
						//                           WALL
						//                           WALL
						//                           WALL
						//                      LEDGE
						//
						// so if we don't test the opposite wall, our test is not very relevant!
						//
						// (I'd really like to see the original code as mine sounds really overcomplicated)

						on_ledge = m_level.is_lateral_way_blocked(x_opposite_limit, m_y, m_height);
					}
					 */
				}
				if (on_ledge)
				{
					
					// if facing, then OK, jump
					// if not facing, check if a simple L-path exists to aim (up/down)
					// if not, then jump because it does not lower the chances of
					// finding aim "in the dark"

					int wd_copy = wd;
					
					if (faces_aim || blocked_l_path(aim_dx, aim_dy))
					{
						if (aim_dy <= 0)
						{
							
							// aim is above or same level: compute best jump
							if (wd_copy == 1)
							{
								elect_jump_right_from_ledge();
								jump_on_aim = m_jump_right.jump_on_aim;
								dy = m_jump_right.dy;
								dx = m_jump_right.dx;
							}
							else
							{
								elect_jump_left_from_ledge();
								jump_on_aim = m_jump_left.jump_on_aim;
								dy = m_jump_left.dy;
								dx = m_jump_left.dx;
							}
						}
						else
						{
							// else: aim is below: forget about it, leave old params set
							// by the if (hole_ahead) test
							dy = m_jump_best.dy;
							dx = m_jump_best.dx;
						}
						if (jump_on_aim)
						{
							debug("elect ledge jump: (dx="+dx+",dy="+dy+")");
						}
					}
					else
					{
						// L-path is clear and does not faces the aim
						if (aim_dy < 0)
						{
							// aim higher: let it turn back
							debug("no ledge jump because aim high, does not face aim and reachable in L-path");
							aim_below_not_facing = true;
						}
						else
						{
							// aim lower: let it turn back so it will fall/jump down when facing on the other ledge
							debug("no ledge jump because aim lower, does not face aim and reachable in L-path");
							aim_below_not_facing = true;
						}
					}


				}
			}

			if (!on_ledge)
			{
				// aim is above, monster not on ledge

				if (lateral_way_blocked)
				{
					
					// default: try a small jump (see test 4)
					m_jump_best.dy = -m_height;
					m_jump_best.dx = m_max_jump_x * wd;
					jump_on_aim = !blocked_l_path(m_jump_best.dx,m_jump_best.dy-m_height);
					if (jump_on_aim)
					{					
						dx = m_jump_best.dx;
						dy = m_jump_best.dy;
					}
				}

				if (aim_dy < -m_tile_height/2)
				{
					// aim above, but consider delta y compared to target in order to avoid jumping too high
					// unnecessarily (but we add a little extra because sometimes target is below the platform
					// to reach and it would not work)
					//
					// we don't have the problem on ledge because on a ledge there is no other option but to jump the highest
					// possible (kind of "shoot in the dark"). We cannot allow that when not on ledge because monsters would jump
					// for no reason

					elect_best_jump(dx, dy - m_tile_height);

					jump_on_aim = m_jump_best.jump_on_aim;

					if ((jump_on_aim) && (m_jump_best.dx * dx < 0))
					{
						// jump decided, opposed direction to the target: maybe the way is wrong
						//
						// check if target not reachable easily with n "ups" and m "rights" or "lefts"

						
						jump_on_aim = blocked_path_up_lateral(dx,dy);
						if (jump_on_aim)
						{
							// check if target not reachable by m "rights" or "lefts" and n "ups"
							jump_on_aim = blocked_path_lateral_up(dx,dy);
						}
					}

					if (jump_on_aim)
					{
						dy = m_jump_best.dy;
						dx = m_jump_best.dx;
					}
				}
				else
				{
					// aim at same level or below: check if way is blocked
					if (lateral_way_blocked)
					{
						int saved_dx = dx;
						int saved_dy = dy;

						init_best_jump(dx, -m_max_jump_y);
						if (wd == 1)
						{
							elect_jump_right(wd);
							jump_on_aim = m_jump_right.jump_on_aim;
							dy = m_jump_right.dy;
							dx = m_jump_right.dx;
							debug("elect wayblock jump right: "+jump_on_aim);
						}
						else
						{
							elect_jump_left(wd);
							jump_on_aim = m_jump_left.jump_on_aim;
							dy = m_jump_left.dy;
							dx = m_jump_left.dx;

							debug("elect wayblock jump left: "+jump_on_aim);
						}

						debug("elect wayblock jump: "+jump_on_aim);

						if (jump_on_aim)
						{
							// jump decided but is it wise?

							if (saved_dx * dx < 0)
							{
								// jump decided but in the opposite direction to the aim
								// only is justified if direct path down to aim is blocked

								jump_on_aim = blocked_path_lateral_down(saved_dx, saved_dy);
								debug("blockedpathdown: "+jump_on_aim+" saveddx "+saved_dx);
							}
						}
					}
				}
			}
		}

		boolean do_flip = false;

		if ((on_ledge && m_aim_met && m_params.object_held == null) || aim_below_not_facing)
		{
			// monster "graviting" around target (kind of locked on it but
			// unable to take it)
			// -> don't jump, just flip as if monster were dormant
			//
			// added "aim below and not facing" variable to avoid jumping/falling the other way
			// (happened in classic w3 l1, and jotd w1 l4)
			
			do_flip = true;
			jump_on_aim = false;
			
		}
		if (jump_on_aim)
		{

			// aim is above: try to jump
			int jump_height = -dy + m_tile_height/4;
			int jump_width = Math.min(m_max_jump_x,Math.abs(dx));

			init_jump(jump_width, jump_height, dx);

		}
		else
		{
			// another feature added to avoid that if hero is killed and teleported
			// back to another far position (example: classic level 3 world 1)
			// hostiles wait for him instead of falling on the ledges

			// no jump, on ledge, and aim well above: turn back from the ledge

			if ((aim_dy < -m_level.get_view_bounds().height) && on_ledge)
			{
				do_flip = true;
			}
		}
		if (!is_in_air())
		{
			if (lateral_way_blocked)
			{
				// facing a wall, not in air: flip

				do_flip = true;
			}
		}

		if (do_flip)
		{
			flip(get_x());
		}
	}
		
	
	private void init_jump(int jump_width,int jump_height, int dx)
	{
		update_right_left_index(dx); // turn back if necessary

		m_actual_jump.init(jump_width, jump_height, JUMP_SPEED);
		set_jump(m_actual_jump);
		
		
	}
	@Override
	public void init(HostileParameters p) {
		
		super.init(p);
		
		// shortcuts
		
		m_level = m_params.level;
		m_grid = m_level.get_grid();
		m_speed = m_params.get_ground_move_speed_value();
				
		m_aim = m_params.hero;
	
		// last frame reserved
		
		if (m_last_frame_reserved)
		{
			m_params.nb_move_frames--;
		}
		
		m_tile_width = m_grid.get_tile_width();
		m_tile_height = m_grid.get_tile_height();
		m_max_jump_y = m_tile_height * HostileWaveParameters.JUMP_HEIGHT_TABLE[m_params.jump_height.ordinal()];
		m_max_jump_x = (m_tile_width * HostileWaveParameters.JUMP_WIDTH_TABLE[m_params.jump_width.ordinal()]) / 2;
		m_close_to_wall_nb_iterations = HostileWaveParameters.JUMP_WALL_THRESHOLD_TABLE[m_params.jump_threshold.ordinal()];
		m_jump_x_wall_distance = 3 * m_tile_width;
	}
	
	protected boolean m_falling = false;
	private int m_fall_speed = 0;
	
	protected boolean is_in_air()
	{
		return m_falling || is_jumping();
	}
	
	protected boolean is_falling()
	{
		return m_falling;
	}
	
	// jump object used to move the monster
	
	protected BaseJump m_actual_jump = new BaseJump();
	
	// jump object used to test if jump will work till the end
	
	protected SimulatedJump m_simulated_jump = new SimulatedJump();
	
	protected static final int JUMP_SPEED = 8;
	private static final int SIMULATION_JUMP_SPEED = JUMP_SPEED / 2; // smaller to be sure to check
	

	@Override
	protected Jump create_jump()
	{
		m_actual_jump.init(m_width * 2, m_height, JUMP_SPEED);
		
		return m_actual_jump;
	}
	
	protected class BaseJump extends Jump
	{
		private ParabolicMobileBehaviour m_parabol = new ParabolicMobileBehaviour();
		private int m_vertical_speed = 0;
		private boolean m_ceiling_hit = false;
		private double m_step;
		
		public void init(int width, int height, int jump_speed)
		{
			if (width == 0)
			{
				// round x
				
				round_x(get_x());
			}
			
			m_parabol.init(get_x(), get_y(), get_walk_direction(),width, height, height /* min width if zero width */, 0);
			
			
			// a minimum of update points are required (or there's a risk for the hostile
			// to cross a wall by "tunnel effect")
			
			m_vertical_speed = jump_speed;

			
			if (m_vertical_speed == 0)
			{
				m_vertical_speed = 2;
			}
			// calibrate to get a correct speed on small jumps too
			if (height < m_tile_height * 3)
			{
				m_step = m_vertical_speed / 40.0;
			}
			else
			{
				m_step = m_vertical_speed / 60.0;
			}
			
			m_ceiling_hit = false;
		}
		
		@Override
		void end()
		{
			m_parabol.set_active(false);
		}
		// true: end of jump
		
		protected boolean is_active()
		{
			return m_parabol.is_active();
		}
		
		protected boolean is_ceiling_hit()
		{
			return m_ceiling_hit;
		}
		
		boolean update(long elapsed_time)
		{
			boolean rval = false;
			int y = get_y();
			
			// update x according to elapsed time, with a divider at 60
			
			double step = m_step * elapsed_time;
			
			m_parabol.update(step);
			                                                   
			m_y = m_parabol.get_y();
			m_x = m_parabol.get_x(m_x);
			
			int delta_y = y - get_y();
			
			
			if (m_parabol.is_active())
			{
				// first, avoid that parabol encounters a wall
				
				if (is_lateral_way_blocked())
				{
					round_x_self();
				}
				
				if (delta_y < 0)
				{
					// going down: landed on a platform test

					/*if (m_level.is_vertical_way_blocked(get_x_boundary(), 
							m_y + m_height, m_width))*/
					if (    m_level.is_vertical_way_blocked(m_x,m_y + m_height, m_width) ||
							m_level.is_vertical_way_blocked(m_x + m_width - 1,m_y + m_height, 1))
							
					{
						//m_y = m_grid.get_rounded_y(m_y);

						m_y = y;

						rval = true; // end of jump
					}
				}
				else 
				{
					// going up
					// bump-on-the-ceiling-test, but with a big margin as well for height
					// as for width, allowing monsters to pass in diagonals, such as in
					// world 1 level 3
					
					int y_min = get_y() + m_height / 2;
					int x_margin = m_width/4;
					
					if (m_level.is_vertical_way_blocked(m_x + x_margin,y_min, m_width) ||
						m_level.is_vertical_way_blocked(m_x + 3*x_margin ,y_min, 1))
					{
						// hit a wall

						m_ceiling_hit = true;
						m_y = y;
						rval = true; // end of jump
						m_fall_speed = 1;
					}
				}

			}
			else
			{				
				// simple end of jump, no wall hit
				m_fall_speed = 8;
				
				// in case the way is blocked, round x
				if (is_lateral_way_blocked())
				{
					round_x_self();
				}
				
				// update falling flag

				// recompute falling flag in case the hostile lands on a hole
				
				update_falling_flag();
				
				rval = true;
			}
				
			return rval;
		}
	
	}
	
	private void update_falling_flag()
	{
		m_falling = !is_vertical_way_blocked();
	}
	protected class SimulatedJump extends BaseJump
	{		
		private double m_saved_x, m_saved_y;
		private int m_saved_right_left;
		private boolean m_vertical_jump;
		private boolean m_last_jump_was_vertical = false;
		
		public boolean successful_jump()
		{
			boolean rval = (!m_last_jump_was_vertical || !m_vertical_jump);
		
			if (rval)
			{
				// perform a fake update with 10 milliseconds (with 50 it does not work)

				while (!this.update(10))
				{

				}

				// success if 
				// 1) jump ends without hitting the ceiling
				// OR
				// 2) destination y is different from start y

				//boolean rval = (m_y < m_saved_y) || (m_fall_speed == 0 && m_y != m_saved_y);
				rval = (!this.is_ceiling_hit()) || ((m_y != m_saved_y) && (m_fall_speed == 0));

				debug("jump simulation: "+rval+", rightleft= "+m_right_left+" oldy: "+m_saved_y+" newy: "+m_y+" fall speed: "+m_fall_speed);
				// restores previous values

				set_right_left(m_saved_right_left);
				m_x = m_saved_x;
				m_y = m_saved_y;
				m_fall_speed = 0;
				m_falling = false;
				
				if (rval)
				{
					m_last_jump_was_vertical = m_vertical_jump;
				}
			}
			else
			{
				debug("more than 1 vertical jump in a row forbidden");
			}
			return rval;
		}
		
		public void init(int width, int height, int dx)
		{
			m_saved_x = m_x;
			m_saved_y = m_y;
			m_saved_right_left = m_right_left;
			
			m_vertical_jump = width == 0;
			
			if (m_vertical_jump)
			{
				// round x

				round_x_self();
			}

			update_right_left_index(dx);

			// check more points when simulated to avoid tunnel effect

			super.init(width,height,SIMULATION_JUMP_SPEED);
		}

	}
	@Override
	protected void animate(long elapsed_time) 
	{
		if (is_in_air())
		{
			if (m_last_frame_reserved)
			{
				// last frame
				m_frame_counter = m_params.nb_move_frames+1;
			}
		}
		else
		{
			super.animate(elapsed_time);
		}
	}
	

	
	private void round_x_self()
	{
		m_x = m_grid.get_rounded_x((int)m_x);
	}
	
	private void round_x(int x)
	{
		m_x = m_grid.get_rounded_x(x);
	}
	private void flip(int x)
	{
		round_x(x);

		// check if did not flip recently
		// and if so, set the flag not to fall but jump instead
		
		int xr = m_grid.get_rounded_x(m_x);
		int yr = m_grid.get_rounded_y(m_y);
		
		m_break_monotony = ((xr == m_x_flip[m_right_left]) && (yr == m_y_flip[m_right_left]));
		
		// store flip position
		m_x_flip[m_right_left] = xr;
		m_y_flip[m_right_left] = yr;
		
		// revert
		
		set_right_left(1 - m_right_left);
		
		
	}
	
	
	/*
	private void old_flip(int x)
	{
		// round x coord before flipping so hostile has a chance to match
		// fall condition
		
		int round_x = m_grid.get_rounded_x(x);
		if (round_x != x)
		{
			m_x = round_x;
		}
		else
		{
			m_right_left = 1 - m_right_left;
			m_x = x;		
		}
	}*/
	
	private int m_walk_timer = 0;
	private int m_fall_timer = 0;
	
	protected void lateral_move(long elapsed_time)
	{
		m_walk_timer += elapsed_time;
		
		while (m_walk_timer > ANIMATION_FRAME_RATE)
		{
			m_walk_timer -= ANIMATION_FRAME_RATE;
			m_x += m_speed * get_walk_direction();
		}
	}
	

	protected int common_move(long elapsed_time)
	{
		int x = get_x();
		
		if (!is_falling())
		{
			lateral_move(elapsed_time);
		}

		
		if (may_fall())
		{
			if (!m_falling)
			{
				m_falling = true;
				m_fall_speed = 0;
			}
			else
			{
				m_x = x;
			}
			
			m_fall_timer += elapsed_time;
			
			// handle the fall
			
			while (m_fall_timer > 20)
			{
				m_fall_timer -= 20;

				m_fall_speed++;

				if (m_fall_speed > 24)
				{
					m_fall_speed = 24;
				}

				m_y += m_fall_speed/2;


				if (is_vertical_way_blocked())
				{						
					m_y = m_grid.get_rounded_y(m_y,false);
					m_falling = false;
					if (m_jump != null)
					{
						m_jump.end();
					}

					on_land();
					m_fall_speed = 0;
					
					// exit loop
					m_fall_timer = 0;
				}
			}

		}
		else
		{
			m_falling = false;
			m_y = m_grid.get_rounded_y(m_y,false);
		}

		
		return x;
	}

	protected void basic_move(long elapsed_time)
	{
		int x = common_move(elapsed_time);
		
		if (!is_in_air() && is_lateral_way_blocked())
		{
			flip(x);
		}
		
		if (!m_falling && !is_vertical_way_blocked())
		{
			if (!has_trajectory())
			{
				flip(x);
			}
		}
		
	}
	
	private int compute_dx()
	{
		return m_aim.get_x_center() - get_x_center();
	}
	
	protected void handle_attract_monster()
	{
		// absolute priority: attract_monster potion if not already owning it
		
		GfxObject go = m_params.level.get_visible_bonus_by_type("attract_monster");
		
		if ((go != null) && (m_params.object_held != go)) 
		{
			set_aim(go);
		}
	}
	@Override
	protected void move(long elapsed_time)
	{
		if (is_dormant())
		{
			basic_move(elapsed_time);
			
			int dx = Math.abs(compute_dx());
		
			switch (m_params.attack_distance)
			{
			case Always:
				set_dormant(false);
				break;
			case Close:
				set_dormant(dx > (m_tile_width*4));
				break;
			case Closer:				
				set_dormant(dx > (m_tile_width*3)/2);
				break;
			case Trajectory:
				if ((m_current_path != null) && (m_current_path.get_location() != null))
				{
					set_aim(m_current_path.get_location());
					dx = compute_dx();
					
					// turn back immediately to head to new aim
					
					set_right_left(dx < 0 ? 1 : 0);
					
				}
				break;
			}
		}
		else
		{
			smart_move(elapsed_time);
		}
		

		steal_objects_on_the_way();
		
	}
	
	protected boolean is_vertical_way_blocked(int x_limit)
	{
		return m_level.is_vertical_way_blocked(x_limit, m_y + m_height, 1);
	}

	protected boolean is_vertical_way_blocked()
	{
		 return is_vertical_way_blocked(get_x_boundary());
	}
	protected boolean may_fall()
	{
		int delta_x = m_speed;
		double y_test = m_y + m_height;
		
		int bound_left = get_x() + delta_x;
		int bound_right = get_x() + m_width - delta_x;
		
		 return !m_level.is_vertical_way_blocked(bound_left , y_test, m_width)
		 && !m_level.is_vertical_way_blocked(bound_right , y_test, m_width);
	}

	protected int get_x_boundary()
	{
		return m_right_left == 0 ? get_x() + m_width : get_x();
	}
	

	protected boolean is_lateral_way_blocked()
	{
		return m_level.is_lateral_way_blocked(get_x_boundary(), m_y,m_height);
	}


	

	/*@Override
	protected void move(long elapsed_time) 
	{
	
		super.move(elapsed_time);
	}*/
}
