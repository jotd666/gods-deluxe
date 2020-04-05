package gods.base;

import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.util.LinkedList;

import gods.sys.*;


public class GfxObject extends NamedLocatable implements LayerObject, Comparable<Object>
{
	@Override
	public boolean is_pickable() 
	{
		boolean rval = super.is_pickable();
	
		// avoid that hero picks up object when dropped from inventory
		// (some players like to buy a shield, and drop it somewhere for later use.
		// I did not know this was possible until I saw abrasion longplay video)
		
		if (rval)
		{
			rval = ((m_fall_speed >= 0) || (m_nb_bounces > 0));
		}
		
		return rval;
	}
	public static final String UNNAMED_PREFIX = "_unnamed_";
	
	private static final int MAX_FALL_SPEED = 4;
	
	public int compareTo(Object arg0) 
	{
		return toString().compareTo(arg0.toString());
	}
	
	protected void warn(String m)
	{
		if (DebugOptions.debug)
		{
			System.out.println(this.getClass().getName()+": Warning: "+m);
		}
	}
	private LinkedList<GfxObject> m_linked_boss_bonuses = null;
	
	protected int m_x_resolution, m_y_resolution;
	private GfxFrameSet m_frame_set;
	protected String m_name;
	private int m_current_frame = 1;
	private int m_animation_timer = 0;
	private int m_nb_frames = 1;
	private int m_fall_time = 0;
	private int m_fall_speed_0 = 0;
	private int m_fall_speed = 0;
	private int m_previous_fall_speed = 0;
	private int m_nb_bounces = 0;
	private int m_bounce_index_to_play = 0;
	private boolean m_in_screen = false;
	
	private static final int MAX_NB_BOUNCES = 4;
	
	protected double m_appearing_x = 0, m_appearing_y = 0;
	
	// out of chest move params
	
	private class LinearMove
	{
		int rate = 0;
		int counter;
		int speed;
		int pos_end = 0;
		boolean active = false;
		
		void init(int v_end, int v_speed, int v_rate)
		{
			counter = 0;
			speed = v_speed;
			active = true;
			rate = v_rate;
			pos_end = v_end;
		}
	}
	
	private LinearMove m_move_y = new LinearMove();
	private BufferedImage m_miniature_image = null;
	
	private static final int FRAME_DELAY = 100;
	
	public BufferedImage toImage()
	{
		return m_frame_set == null ? null : m_frame_set.get_frame(m_current_frame).toImage();
	}
	
	public void set_miniature_image(BufferedImage miniature)
	{
		m_miniature_image = miniature;
	}
	
	public BufferedImage toMiniatureImage()
	{
		return m_miniature_image == null ? toImage() : m_miniature_image;
	}
	
	public void set_current_frame(int df)
	{
		if (df <= m_nb_frames)
		{
			m_current_frame = df;
		}
	}
	
	public int get_current_frame()
	{
		return m_current_frame;
	}
	
	
	public void spawn_boss_bonuses(LevelData ld)
	{
		if (m_linked_boss_bonuses != null)
		{
			/*double dx = m_x - m_appearing_x;
			double dy = m_y - m_appearing_y;
			
			int nb_items = m_linked_boss_bonuses.size();
			int i = 0;*/
			
			for (GfxObject go : m_linked_boss_bonuses)
			{
				/*int old_x = go.get_x();
				go.set_coordinates(go.get_x_center()+dx, go.get_y_center()+dy);
				ld.notify_x_move_object(go, old_x);

				int speed_x = (nb_items / 2) - i;

				i++;

				go.set_out_of_chest_move(this,1,speed_x,1,-4);*/

				go.set_visible(true);
			}			
			
		}
	}
	public void spawn_bonuses(LevelData ld,String [] bonus_name_list)
	{
		
			
		int nb_items = bonus_name_list.length;
		int i = 0;
		int x_offset =  -(nb_items * 8);

		for (String class_name : bonus_name_list)
		{
			GfxObject go = ld.create_bonus(this.get_x(), this.get_y(), class_name);
			int y_offset = -32-(int)(32*Math.random());

			go.set_out_of_chest_move(this,x_offset,y_offset,1,-4);
			
			x_offset+=16;
			
			go.set_visible(true);
		}			

		
	}
	
	
	public void link_object(GfxObject go)
	{
		if (m_linked_boss_bonuses == null)
		{
			m_linked_boss_bonuses = new LinkedList<GfxObject>();
		}
		
		m_linked_boss_bonuses.add(go);
	}
	
	@Override
		public boolean set_x(double x)
	{
		double old_x = m_x;
		
		if (m_x_resolution == 1)
		{
			m_x = x;
		}
		else
		{
			m_x = Math.round(x / m_x_resolution) * m_x_resolution;
		}
		
		return (old_x != m_x);
	}

	
	/*
	public boolean set_x_norounding(double x)
	{
		double old_x = m_x;
		m_x = x;
		return (old_x != m_x);
	}*/
	
	@Override
	public boolean set_y(double y)
	{
		double old_y = m_y;
		
		if (m_y_resolution == 1)
		{
			m_y = y;
		}
		else
		{
			m_y = Math.round(y / m_y_resolution) * m_y_resolution;
		}
		
		return (old_y != m_y);
	}
	
	public void set_visible(boolean v)
	{
		if ((!m_visible) && (v))
		{
			m_appearing_x = (int)m_x;
			m_appearing_y = (int)m_y;
		}
		m_visible = v;
	}


	
	
	public GfxFrameSet.Properties get_properties()
	{
		return m_frame_set.get_properties();
	}
	public GfxFrameSet get_source_set()
	{
		return m_frame_set;
	}
	
	public String toString()
	{
		String rval = null;
		
		if (!is_named())
		{
			rval = "unnamed " + get_class_name();
		}
		else
		{
			rval = m_name + " (" + get_class_name() + ")";
		}
		if (!m_visible)
		{
			rval += " (hidden)";
		}
		return rval;
	}
	
	public String get_name()
	{
		return m_name;
	}
	public boolean is_named()
	{
		return !m_name.equals("");
	}	
	public void set_name(String name)
	{
		m_name = name;
	}
	
	public String get_class_name()
	{
		return m_frame_set.get_name();
	}

	protected GfxObject(int x_resolution, int y_resolution)
	{
		m_x_resolution = x_resolution;
		m_y_resolution = y_resolution;
	}
	
	public GfxObject(int x, int y, int x_resolution, int y_resolution, String name, 
			GfxFrameSet frame_set)
	{
		this(x_resolution,y_resolution);
		
		m_frame_set = frame_set;
		m_name = name;
		
		if (m_frame_set != null)
		{
			m_nb_frames = m_frame_set.get_nb_frames();
			BufferedImage im = toImage();
			m_width = im.getWidth();
			m_height = im.getHeight();
		}

		m_appearing_x = x;
		m_appearing_y = y;
		
		set_coordinates(x, y);
		
	}
		
	public int get_bounce_to_play()
	{
		int rval = m_bounce_index_to_play;
		m_bounce_index_to_play = 0;
		return rval;
	}
	
	private boolean may_fall(LevelData level_data)
	{
		int y_round = get_y() + m_height + 1;
		int x_max = (int)(m_x + m_width - 1);
			
		boolean rval = (((level_data.is_vertical_way_blocked(m_x, m_y , m_width)) || 
				(!level_data.is_vertical_way_blocked(m_x, y_round, m_width)))) &&
				(((level_data.is_vertical_way_blocked(x_max, m_y, 1)) || 
						(!level_data.is_vertical_way_blocked(x_max, y_round, 1))));
		
		return rval || level_data.is_vertical_way_blocked(m_x, m_y + m_height / 2 , m_width);
	}
	
	private int m_timer = 0;
	private static final int FALL_UPDATE_RATE = 20;
	private boolean m_forth = true;
	
	public void update(long elapsed_time, LevelData level_data, Rectangle animation_bounds)
	{
		m_timer += elapsed_time;
		
		while (m_timer > FALL_UPDATE_RATE)
		{
			m_timer -= FALL_UPDATE_RATE;

			GfxFrameSet.Type t = get_source_set().get_type();

			boolean in_screen = animation_bounds.contains(m_x,m_y);

			if (in_screen)
			{
				if (is_visible() && 
						(get_source_set().get_properties().animation_type != AnimatedFrames.Type.CUSTOM))
				{
					if (m_nb_frames > 1)
					{
						m_animation_timer += elapsed_time;
						while (m_animation_timer > FRAME_DELAY)
						{
							m_animation_timer -= FRAME_DELAY;
							
							switch (get_source_set().get_properties().animation_type)
							{
							case FOREVER:

								m_current_frame++;
								if (m_current_frame > m_nb_frames)
								{
									m_current_frame = 1;
								}
								break;
							case FOREVER_REVERSE:
								if (m_current_frame == 1)
								{
									m_current_frame = m_nb_frames;
								}
								m_current_frame--;

								break;
							case BACK_AND_FORTH:
								if (m_forth)
								{
									m_current_frame++;
									if (m_current_frame > m_nb_frames)
									{
										m_forth = false;
										m_current_frame = 1;
									}
								}
								else
								{
									if (m_current_frame == 1)
									{
										m_forth = true;
										m_current_frame = m_nb_frames;
									}
									m_current_frame--;
								}
							}
						}
					}
				}



				if (m_move_y.active)
				{
					m_move_y.counter++;

					if (m_move_y.counter == m_move_y.rate)
					{
						m_move_y.counter = 0;

						m_y += m_move_y.speed;

						if (m_y <= m_move_y.pos_end)
						{
							m_y = m_move_y.pos_end;
							m_move_y.active = false;
						}
					}
				}
				else if (is_visible() && (t != GfxFrameSet.Type.activable) && 
						(t != GfxFrameSet.Type.background_item))
				{
					// when entering in screen, make some special items bounce

					if ((!m_in_screen) && 
							((t == GfxFrameSet.Type.pickable) || (t == GfxFrameSet.Type.key)))
					{
						set_initial_fall_speed(-4);
					}

					boolean mf = may_fall(level_data);

					if (mf)
					{	

						m_fall_time += FALL_UPDATE_RATE;

						m_fall_speed = m_fall_time / 50 + m_fall_speed_0;

						if (m_fall_speed > MAX_FALL_SPEED)
						{
							m_fall_speed = MAX_FALL_SPEED;
						}

						// pseudo gravity + bounce

						m_y += m_fall_speed;
					}

					if (m_previous_fall_speed == 0)
					{
						// reset number of bounces if object was not moving

						m_nb_bounces = 0;
					}

					// trick to avoid recomputing the may_fall function if m_y did not change

					if (!mf || !may_fall(level_data))
					{	
						m_nb_bounces++;

						if (m_nb_bounces >= MAX_NB_BOUNCES)
						{
							// stop bouncing
							m_y = level_data.get_grid().get_rounded_y(m_y + m_height) - m_height;
							m_fall_speed = 0;
							m_nb_bounces = MAX_NB_BOUNCES;
						}
						else
						{

							m_y -= m_fall_speed;

							m_fall_speed_0 = (m_fall_speed * (m_nb_bounces - MAX_NB_BOUNCES)) / MAX_NB_BOUNCES;

							m_fall_time = 0;

							m_fall_speed = m_fall_speed_0;

						}
						m_previous_fall_speed = m_fall_speed;
						if (m_fall_speed != 0)
						{
							m_bounce_index_to_play = m_nb_bounces;
						}

					}
				}
			}


			// update in screen flag
			m_in_screen = in_screen;
		}
	}
	public GfxObject(ParameterParser fr, int x_resolution, int y_resolution, 
			GfxPalette palette, int game_scale) throws java.io.IOException
	{		
		if (game_scale > 1)
		{
			// no rounding within the game
			
			m_x_resolution = 1;
			m_y_resolution = 1;
		}
		else
		{
			m_x_resolution = x_resolution;
			m_y_resolution = y_resolution;
		}
		fr.startBlockVerify(get_block_name());
		
		m_name = fr.readString("name");
		
		if (m_name.equals("_unnamed_"))
		{
			m_name = "";			
		}
		
		m_visible = fr.readBoolean("visible");
		
		m_x = fr.readInteger("x");
		m_y = fr.readInteger("y");		


		m_x *= game_scale;
		m_y *= game_scale;

		
		m_appearing_x = m_x;
		m_appearing_y = m_y;

		String frame_set = fr.readString("frame_set");
		
		m_frame_set = palette.lookup_frame_set(frame_set);
		
		m_nb_frames = 0;
		
		if (m_frame_set == null)
		{
			System.out.println("Frame set "+frame_set+ " not found in palette");
		}
		else
		{
			m_nb_frames = m_frame_set.get_nb_frames();

			BufferedImage im = toImage();

			m_width = im.getWidth();
			m_height = im.getHeight();
		}
		
		fr.endBlockVerify();
	}
	
	public String get_block_name() 
	{
		return "OBJECT";
	}

	public void serialize(ParameterParser fw) throws java.io.IOException
	{
		fw.startBlockWrite(get_block_name());
		
		if (m_name.equals(""))
		{
			fw.write("name", "_unnamed_");			
		}
		else
		{
			fw.write("name", m_name);
		}
		
		fw.write("visible", m_visible);
		
		fw.write("x", get_x());
		fw.write("y", get_y());
		
		if (m_frame_set != null)
		{
			fw.write("frame_set",m_frame_set.get_name());
		}
		else
		{
			fw.write("frame_set", ParameterParser.UNDEFINED_STRING);
		}
		
		fw.endBlockWrite();
	}

	// when a chest opens, lateral movement of the objects
	// first, set all the object positions to the start_x position
	// then, set end_x to original object position (so sectors are preserved)
	
	public void set_out_of_chest_move(NamedLocatable chest_position, int x_offset, int y_offset, int y_move_rate, int y_speed)
	{
		m_move_y.init(get_y()+y_offset,y_speed,y_move_rate);
		
		
		m_x = chest_position.get_x()+x_offset;
		m_y = chest_position.get_y();
	}
	public void set_initial_fall_speed(int s)
	{
		m_nb_bounces = 0;
		m_fall_speed_0 = s;
	}
	
	// may be taken only if not getting out of the chest
	
	public boolean may_be_taken()
	{
		return (!m_move_y.active);
	}
}
