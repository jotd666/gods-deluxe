package gods.base;

import gods.sys.ParameterParser;

import java.io.IOException;
import java.util.*;

public class HostileTrajectory implements Describable, Cloneable
{
	public enum Type { To_Location, Around_Location_Clockwise, Around_Location_Anti_Clockwise, 
		To_Hero, Drop_To_Hero_Level, Jump_From, Frozen, Custom, Flee, Loop_Back }
	
	private LinkedList<TrajectorySegment> m_segment_list = new LinkedList<TrajectorySegment>();
	private int m_initial_angle = 0;
	private int m_vertical_oscillation_speed = 0;
	
	public HostileTrajectory clone() throws CloneNotSupportedException
	{
		HostileTrajectory rval = (HostileTrajectory)super.clone();
		
		rval.m_segment_list = new LinkedList<TrajectorySegment>();
		
		for (TrajectorySegment ts : m_segment_list)
		{
			rval.m_segment_list.add(ts.clone());
		}
		return rval;
	}
	public Collection<TrajectorySegment> items()
	{
		return m_segment_list;
	}
	public String get_block_name() 
	{
		return "HOSTILE_TRAJECTORY";
	}
	
	public void move(TrajectorySegment ts, boolean down)
	{
		int i = m_segment_list.indexOf(ts);
		
		if (i >= 0)
		{
			int ni = i;
			boolean doit = false;
			
			if (down && i < m_segment_list.size()-1)
			{
				ni++;
				doit = true;
			}
			else if (!down && i > 0)
			{
				ni--;
				doit = true;
			}
			
			if (doit)
			{
				m_segment_list.remove(ts);
				m_segment_list.add(ni, ts);
			}
		}
	}
	
	public HostileTrajectory()
	{
		
	}
	
	public HostileTrajectory(ParameterParser fr, LevelData level) throws IOException
	{		
		fr.startBlockVerify(get_block_name());
		
		m_initial_angle = fr.readInteger("initial_angle");
		m_vertical_oscillation_speed = fr.readInteger("vertical_oscillation_speed");

		int nb_segments = fr.readInteger("nb_segments");
		
		for (int i = 0; i < nb_segments; i++)
		{
			add(new TrajectorySegment(fr,level));
		}
		
		fr.endBlockVerify();		
	}

	public void add(TrajectorySegment s)
	{
		m_segment_list.add(s);
	}
	
	public void remove(TrajectorySegment s)
	{
		m_segment_list.remove(s);
	}
	
	public void serialize(ParameterParser fw) throws IOException 
	{
		fw.startBlockWrite(get_block_name());
		
		fw.write("initial_angle", m_initial_angle);
		fw.write("vertical_oscillation_speed", m_vertical_oscillation_speed);
		
		fw.write("nb_segments", m_segment_list.size());
		
		for (TrajectorySegment ts : m_segment_list)
		{
			ts.serialize(fw);
		}
		fw.endBlockWrite();
		
	}
	public int get_initial_angle() 
	{
		return m_initial_angle;
	}
	
	public void set_initial_angle(int initial_angle) 
	{
		this.m_initial_angle = initial_angle;
	}
	public int get_vertical_oscillation_speed() 
	{
		return m_vertical_oscillation_speed;
	}
	public void set_vertical_oscillation_speed(int vertical_oscillation_speed) 
	{
		this.m_vertical_oscillation_speed = vertical_oscillation_speed;
	}

}
