package gods.game.characters.hostiles;

import gods.game.characters.GroundThief;
import gods.game.characters.HostileParameters;

public class GroundThiefCraneClassicLevel4 extends GroundThief 
{
	// offsets taken from level 2 thief
	
	@Override
	public void init(HostileParameters p) {
		
		super.init(p);
		
		m_object_to_avoid = "w3_trap_door_key";
	}

	private static final int X_OFFSET = 8;
	private static final int Y_OFFSET = -20;
	
	private static final int [] STOLEN_OBJECT_XY_OFFSET_ARRAY = 
	{ X_OFFSET, 8 + Y_OFFSET, 
		X_OFFSET, 10 + Y_OFFSET,
		X_OFFSET, 12 + Y_OFFSET,
		X_OFFSET, 10 + Y_OFFSET,
		X_OFFSET, 8 + Y_OFFSET, 
		X_OFFSET, 10 + Y_OFFSET,
		X_OFFSET, 12 + Y_OFFSET, 
		X_OFFSET, 10 + Y_OFFSET,
		X_OFFSET,  8  + Y_OFFSET};
	
	public GroundThiefCraneClassicLevel4() 
	{
		super(true,STOLEN_OBJECT_XY_OFFSET_ARRAY);
		
	}

}
