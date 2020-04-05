package gods.base;

import gods.sys.ParameterParser;

import java.io.IOException;

import java.util.*;

public class HostileWaveParameters implements Describable, Cloneable
{
	public enum Direction { Left, Right, Random }
	
	// not used by flying creatures
	
	public enum AttackDistance { None, // ground monsters: move back & forth
		Close, // ground monster attacks when close to the player
		Closer, // ground monster attacks when less close to the player
		Always, // ground monster attacks hero
		Trajectory // follow trajectory if any (only used for thieves ATM)
		}
	
	private void debug(String m)
	{
		if (DebugOptions.debug)
		{
			if (location != null)
			{
				System.out.println(this.getClass().getName()+" ("+location+"): "+m);
			}
			else
			{
				System.out.println(this.getClass().getName()+": "+m);
			}
		}
	}
	
	public String get_block_name() 
	{
		return "HOSTILE_WAVE_PARAMETERS";
	}
	
	public HostileWaveParameters clone() throws CloneNotSupportedException
	{
		HostileWaveParameters rval = (HostileWaveParameters)super.clone();
		
		rval.object_to_drop = object_to_drop.clone();
		
		
		if (location != null) rval.location = (ControlObject)location.clone();
		if (class_name != null) rval.class_name = new String(class_name);
		if (frame_set_name != null) rval.frame_set_name = new String(frame_set_name);

		if (trajectory != null) rval.trajectory = trajectory.clone();
		
		return rval;
	}

	public int get_nb_items()
	{
		return nb_enemies;
	}
	
	public HostileWaveParameters(ControlObject co, LevelData ld)
	{
		level = ld;
		location = co;
	}
	
	public HostileWaveParameters(ParameterParser fr, LevelData ld) throws IOException
	{
		fr.startBlockVerify(get_block_name());
		
		debug("loading general params (location)s");
		
		level = ld;
		location = ld.get_control_object(fr.readString("location"));
		debug("loading general params");
		class_name = fr.readString("class_name",true);
		frame_set_name = fr.readString("frame_set_name",true);
		fire_type = FireType.valueOf(fr.readString("fire_type"));
		appearing_delay_type = AppearingDelay.valueOf(fr.readString("appearing_delay"));
		if (appearing_delay_type.equals(AppearingDelay.Custom))
		{
			appearing_delay = fr.readInteger("tenth_of_seconds");
		}
		else
		{
			appearing_delay = APPEARING_DELAY_TABLE[appearing_delay_type.ordinal()];
		}
		
		health_points = fr.readInteger("health_points");
		direction = Direction.valueOf(fr.readString("direction"));
		attack_distance = AttackDistance.valueOf(fr.readString("attack_distance"));
		move_speed = Speed.valueOf(fr.readString("speed"));
		shoot_speed = Speed.valueOf(fr.readString("shoot_speed"));
		shoot_frequency = Speed.valueOf(fr.readString("shoot_frequency"));
		instant_creation = fr.readBoolean("instant_creation");
		//shoot_shield = 
		fr.readBoolean("shoot_shield");
		steal_on_the_way = fr.readBoolean("steal_on_the_way");
		jump_height = JumpHeight.valueOf(fr.readString("jump_height"));
		jump_width = JumpWidth.valueOf(fr.readString("jump_width"));
		jump_threshold = JumpThreshold.valueOf(fr.readString("jump_threshold"));
		avoid_shoot = fr.readBoolean("avoid_shoot");
		objective_balance = fr.readInteger("objective_balance");

		nb_enemies = fr.readInteger("count");
		
		if (!class_name.contains("spike") && (nb_enemies > 1) && (appearing_delay == 0))
		{
			// multiple non-spike monsters appearing at the same time: correct the bug
			debug("multiple monsters with 0 appearing delay, fixed");
			appearing_delay = APPEARING_DELAY_TABLE[AppearingDelay.Short.ordinal()];
		}
		if (nb_enemies>1)
		{
			debug("loading "+nb_enemies+" objects to drop @ "+fr.getCurrentLine());
		}
		
		object_to_drop = new ObjectToDrop(fr);
		
		debug("loading trajectory @ "+fr.getCurrentLine());
		
		trajectory = new HostileTrajectory(fr,ld);

		fr.endBlockVerify();
		debug("OK");
	}
	
	public void serialize(ParameterParser fw) throws IOException 
	{
		fw.startBlockWrite(get_block_name());
		
		fw.write("location",location == null ? ParameterParser.UNDEFINED_STRING : location.get_name());
		fw.write("class_name", class_name);
		fw.write("frame_set_name",frame_set_name);
		fw.write("fire_type",fire_type.toString());
		fw.write("appearing_delay",appearing_delay_type.toString());
		if (appearing_delay_type.equals(AppearingDelay.Custom))
		{
			fw.write("tenth_of_seconds",appearing_delay);
		}
		fw.write("health_points",health_points);
		fw.write("direction", direction.toString());
		fw.write("attack_distance", attack_distance.toString());
		fw.write("speed", move_speed.toString());
		fw.write("shoot_speed", shoot_speed.toString());
		fw.write("shoot_frequency", shoot_frequency.toString());
		fw.write("instant_creation", instant_creation);
		fw.write("shoot_shield", false); // TEMP
		fw.write("steal_on_the_way", steal_on_the_way);
		fw.write("jump_height", jump_height.toString());
		fw.write("jump_width", jump_width.toString());
		fw.write("jump_threshold", jump_threshold.toString());
		fw.write("avoid_shoot", avoid_shoot);
		fw.write("objective_balance",objective_balance);
		
		fw.write("count",nb_enemies);
		
		
			if (object_to_drop != null)
			{
				object_to_drop.serialize(fw);
			}
			else
			{
				fw.startBlockWrite("NO_OBJECT");
				fw.endBlockWrite();
			}
				
		
		get_trajectory().serialize(fw);
		
		fw.endBlockWrite();
		
	}
	
	
	
	public void set(String drop_class_name,String drop_instance_name, String drop_hostile_name)
	{
		

		if (drop_class_name != null)
		{

			object_to_drop.class_name = drop_class_name;
			object_to_drop.instance_name = drop_instance_name;
			object_to_drop.hostile_name = drop_hostile_name;
		}
		
		
	}
	public LevelData level;
	
	// highly sensitive values. Changing one of those (specially jump-related parameters)
	// can make some serious damages in some parts of the game (puzzles with smart ground monsters)
	
	public static final int [] GROUND_SPEED_TABLE = {4,6,7,10,13};
	public static final int [] FLYING_SPEED_TABLE = {3,8,10,12,14};
	public static final int [] SHOOT_SPEED_TABLE = {3,8,10,14,18};
	public static final int [] PERIOD_TABLE = {6400,3200,2400,1600,800}; // milli-seconds
	public static final int [] JUMP_HEIGHT_TABLE = {0,4,7};
	public static final int [] JUMP_WIDTH_TABLE = {2,5,6,7};   // maximum values
	public static final int [] JUMP_WALL_THRESHOLD_TABLE = {1,3,5};
	private static final int [] APPEARING_DELAY_TABLE = { 0, 6, 10, 12, 16 }; // 10th of second
	
	public enum Speed { Very_Slow, Slow, Normal, Fast, Very_Fast }
	public enum FireType { None, Straight, Directional, Fuzzy }
	public enum JumpHeight { None, Normal, High }
	public enum JumpWidth { Very_Short, Short, Normal, Wide }
	public enum JumpThreshold { Close, Normal, Far }
	public enum AppearingDelay { None, Very_Short, Short, Normal, Long, Custom }
	
	public ObjectToDrop object_to_drop = new ObjectToDrop();
	public int nb_enemies = 1;
	public ControlObject location;
	public String class_name;
	public String frame_set_name;

	public AppearingDelay appearing_delay_type = AppearingDelay.None;
	public Direction direction = Direction.Right;
	public FireType fire_type = FireType.None;
	
	public boolean instant_creation = false;
	public int appearing_delay = 0;
	public int health_points = 1;
	public int damage_points = 0;
	public Speed move_speed = Speed.Normal;
	public Speed shoot_speed = Speed.Normal;
	public Speed shoot_frequency = Speed.Normal;
	public AttackDistance attack_distance = AttackDistance.None;
	public JumpHeight jump_height = JumpHeight.Normal;
	public JumpWidth jump_width = JumpWidth.Normal;
	public JumpThreshold jump_threshold = JumpThreshold.Normal;
	public int objective_balance = 100;
	public boolean steal_on_the_way = false;
	public boolean avoid_shoot = false;
	
	public HostileTrajectory get_trajectory()
	{
		if (trajectory == null)
		{
			trajectory = new HostileTrajectory();
		}
		return trajectory;
	}
	private HostileTrajectory trajectory = null;
}