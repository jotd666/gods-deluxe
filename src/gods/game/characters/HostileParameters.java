package gods.game.characters;

import java.awt.Rectangle;

import gods.base.*;
import gods.base.HostileWaveParameters.*;
import gods.game.MonsterLayer;
import gods.game.SectoredLevelData;
import gods.game.SfxSet;

public class HostileParameters implements Cloneable
{	
	@Override
	public HostileParameters clone()
	{
		HostileParameters hp = null;
		try {
			hp = (HostileParameters)super.clone();
			
			hp.location = location.clone();
			
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return hp;
	}

	public String class_name = null;
	
	public boolean debug_mode = false;
	public String frame_set_name;
	public SectoredLevelData level = null;
	public HostileWaveParameters.Direction direction = HostileWaveParameters.Direction.Right;
	public HostileWaveParameters.AttackDistance attack_distance = HostileWaveParameters.AttackDistance.None;
	public int appearing_delay = 0;
	public int health_points = 1; // if 0 hostile dies
	public int objective_balance = 100;  // ATM only aggressivity is implemented, TODO thief objective??
	public int max_health_points = 1;
	public int original_health_points = 1;
	public int score_points = 0;
	public int nb_move_frames;
	public int total_nb_hostiles_of_wave;
	public boolean steal_on_the_way = false;
	public JumpHeight jump_height = JumpHeight.Normal;
	public JumpWidth jump_width = JumpWidth.Normal;
	public JumpThreshold jump_threshold = JumpThreshold.Normal;
	public boolean avoid_shoot = false;
	public boolean created_at_level_start = false;
	
	public Speed move_speed;
	public Speed shoot_speed;
	public Speed shoot_frequency;
	
	public int index = 0;
	public HostileWeaponSet weapon_set = null;
	public HostileTrajectory trajectory = null;
	
	public int get_shoot_speed_value() { return HostileWaveParameters.SHOOT_SPEED_TABLE[shoot_speed.ordinal()]; }
	public int get_shoot_period_value() { return HostileWaveParameters.PERIOD_TABLE[shoot_frequency.ordinal()]; }
	public int get_ground_move_speed_value() { return HostileWaveParameters.GROUND_SPEED_TABLE[move_speed.ordinal()]; }
	public int get_flying_speed_value() { return HostileWaveParameters.FLYING_SPEED_TABLE[move_speed.ordinal()]; }
	
	public HostileWaveParameters.FireType fire_type;
	public Hero hero;
	
	public Rectangle view_bounds;
	public SfxSet sfx_set;
	
	public GfxObject object_to_drop = null;
	public GfxObject object_held = null;
	public HostileWaveParameters hostile_to_drop = null;
	public MonsterLayer hostile_set;
	
	public ControlObject location;
	
	private HostileWaveParameters wave_parameters;

	public void compute_object_to_drop()
	{
		wave_parameters.nb_enemies--;
		if (wave_parameters.nb_enemies==0)
		{
		ObjectToDrop o = wave_parameters.object_to_drop;
		if (o != null)
		{
			if (o.instance_name != null)
			{
				// existing object
			
				this.object_to_drop = this.level.get_bonus(o.instance_name);
			}
			else if (o.class_name != null)
			{
				// non existing object, no instance name required (like diamond, stamina)
			
				GfxFrameSet frame_set = this.level.get_level_palette().lookup_frame_set(o.class_name);

				String name = location.get_name()+"_"+index;
				
				this.object_to_drop = new GfxObject(location.get_x(),location.get_y(),1,
						this.level.get_grid().get_tile_height()/2,name,frame_set);

			}
			
			if (o.hostile_name != null)
			{
				ControlObject co = this.level.get_control_object(o.hostile_name);
				if (co != null)
				{
					this.hostile_to_drop = level.get_hostile_params().get(co);
				}
			}
		}
		}
		

	}
	public HostileParameters(HostileWaveParameters hwp, 
			SectoredLevelData level, MonsterLayer ml, Hero hero, SfxSet sfx_set,
			HostileWeaponSet hws, int index, int difficulty_level)
	{
		this.hostile_set = ml;
		this.wave_parameters = hwp;
		
		this.total_nb_hostiles_of_wave = hwp.nb_enemies;
		
		this.class_name = hwp.class_name;
		
		//wave_params = hwp;
		this.level = level;
		this.location = hwp.location;
		this.frame_set_name = hwp.frame_set_name;
		
		this.sfx_set = sfx_set;
		this.view_bounds = level.get_view_bounds();
		
		this.weapon_set = hws;
		this.objective_balance = hwp.objective_balance;
		this.steal_on_the_way = hwp.steal_on_the_way;
		this.jump_height = hwp.jump_height;
		this.jump_width = hwp.jump_width;
		this.jump_threshold = hwp.jump_threshold;
		this.avoid_shoot = hwp.avoid_shoot;
		
		this.direction = hwp.direction;
		this.attack_distance = hwp.attack_distance;
		
		this.appearing_delay = total_nb_hostiles_of_wave == 1 ? hwp.appearing_delay : 
				(hwp.appearing_delay * index);
		
		// convert from tenths of seconds to milliseconds
		
		this.appearing_delay *= 100;
		
		// this is rather empiric, and wrong not perfect, but
		// I found it very hard to figure out how the damage/health/scoring
		// system really works in higher levels.
		//
		// Example: on level 4 world 1, it takes 14 fireballs (power 5) to
		// kill the firing robot, which yields 4100 points (roughly 40 life points)
		// (hence the 7/4 proportion in level 4)
		//
		// If hero jumps into it, it only takes 6 life points, which is
		// (maybe) 40/damage_divisor(=6)
		//
		// harder in higher levels: first level uses nominal health points
		// 2nd level uses 5/4 * more
		// 3rd level uses 6/4 * more
		// 4th level uses 7/4 * more
		// 1sr level (2nd round) uses 5/4 * more
		// and so on... (TBC)
		//
		// maybe the algorithm is simpler but I don't have it...
		//
		//
		// after seeing the real values with Kroah level editor
		//
		// player walks into monster: hit by monster current hp / 4
		
		
		this.health_points = hwp.health_points;
		this.max_health_points = this.health_points;
		this.original_health_points = hwp.health_points;
		
		/*if (hwp.damage_points>0)
		{
			// change for special cases like snakes
			this.damage_points = hwp.damage_points;
		}
		else
		{
			this.damage_points = hwp.health_points;
		}*/
		GfxFrameSet gfs = this.level.get_level_palette().lookup_frame_set(frame_set_name);
				
		// default value, can be changed by specific hostile
		
		this.nb_move_frames = gfs.get_nb_frames();
		this.fire_type = hwp.fire_type;
		this.move_speed = hwp.move_speed;
		this.shoot_speed = hwp.shoot_speed;
		this.shoot_frequency = hwp.shoot_frequency;
	
		this.hero = hero;
		
		this.trajectory = hwp.get_trajectory();
		
		this.created_at_level_start = hwp.instant_creation;
		
		this.index = index;
			
	}
}
