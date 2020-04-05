package gods.base;

import gods.base.HostileTrajectory.Type;
import gods.sys.ParameterParser;

import java.io.IOException;

public class TrajectorySegment implements Describable, Cloneable, TrajectorySegmentConstant
{
	public Type get_type() 
	{
		return type;
	}

	public TrajectorySegment clone() throws CloneNotSupportedException
	{
		return (TrajectorySegment)super.clone();
	}
	public String toString()
	{
		String rval = "<new>";
		
		if (type != null)
		{
			rval = type.toString().replace('_', ' ');


			if (to_location())
			{
				rval += " \"" + location.get_name() +"\"";
			}
			if (duration > 0)
			{
				rval += " during "+duration/1000.0+"s";
			}
		}
		return rval;
	}
	
	public String get_block_name() 
	{
		return "SEGMENT";
	}
	
	public TrajectorySegment()
	{
		
	}
	
	public TrajectorySegment(ParameterParser fr, LevelData level) throws IOException
	{
		fr.startBlockVerify(get_block_name());
		
		type = Type.valueOf(fr.readString("type"));
		duration = fr.readInteger("duration");
		if (to_location())
		{
			location = level.get_control_object(fr.readString("location"));
		}
		
		fr.endBlockVerify();
	}
	/* (non-Javadoc)
	 * @see gods.base.TrajectorySegmentConstant#to_location()
	 */
	public boolean to_location()
	{
		return (to_location(type));
	}
	public static boolean to_location(Type t)
	{
		boolean rval = (t != null);
		
		if (rval)
		{
			switch (t)
			{
			case Frozen:
			case Flee:
			case To_Hero:
			case Loop_Back:
			case Custom:
			case Drop_To_Hero_Level:
				rval = false;
				break;
			}
		}
		return rval;
	}
	public void serialize(ParameterParser fw) throws IOException 
	{
		fw.startBlockWrite(get_block_name());
		fw.write("type", type.toString());
		fw.write("duration", duration);
		if (to_location())
		{
			fw.write("location", location.get_name());
		}
		fw.endBlockWrite();
	}
	public Type type = null;
	public NamedLocatable location = null;
	public int duration = 0;
	
	/* (non-Javadoc)
	 * @see gods.base.TrajectorySegmentConstant#get_location()
	 */
	public NamedLocatable get_location()
	{
		return location;
	}
	/* (non-Javadoc)
	 * @see gods.base.TrajectorySegmentConstant#get_duration()
	 */
	public int get_duration()
	{
		return duration;
	}
}