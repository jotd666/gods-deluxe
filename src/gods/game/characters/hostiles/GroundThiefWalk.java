package gods.game.characters.hostiles;

import gods.game.characters.GroundThief;

public class GroundThiefWalk extends GroundThief 
{
	// offsets taken from level 2 thief
	
	private static final int [] STOLEN_OBJECT_XY_OFFSET_ARRAY = 
	{ 8, 0, 
     14, 0,
     24, 0,
     34, -1,
     40, -2, 
     36, -3,
     24, -2, 
     14, -1,
     8,  2 };
	
	public GroundThiefWalk() 
	{
		super(true,STOLEN_OBJECT_XY_OFFSET_ARRAY);
	}

}
