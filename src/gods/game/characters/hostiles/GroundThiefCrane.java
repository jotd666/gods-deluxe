package gods.game.characters.hostiles;

import gods.game.characters.GroundThief;

public class GroundThiefCrane extends GroundThief 
{
	// offsets taken from level 2 thief
	
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
	
	public GroundThiefCrane() 
	{
		super(true,STOLEN_OBJECT_XY_OFFSET_ARRAY);
	}

}
