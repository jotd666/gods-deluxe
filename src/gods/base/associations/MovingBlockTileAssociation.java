package gods.base.associations;

import gods.base.*;
import gods.sys.ParameterParser;

public class MovingBlockTileAssociation extends ObjectAssociation {


	public MovingBlockTileAssociation()
	{
		super(Type.Moving_Block_Tile,3);
	}
	@Override
	public String describe()
	{
		return "displays moving block \""+get_moving_block().get_name()+"\" with \""+get_tile().get_source_set().get_name()+
		"\" tile(s), move time is "+get_move_time()+" mills" + (is_back_and_forth() ? ", back and forth" : "");
	}
	
	@Override
	public void parse(ParameterParser fr, LevelData ld) throws java.io.IOException
	{
		set_object(0,ld.get_control_object(fr.readString("object")));
		set_object(1,ld.get_level_palette().lookup_frame_set(fr.readString("object")));
		set_object(2,new AssociationProperty(fr.readString("object")));
	}
	
	public ControlObject get_moving_block()
	{
		return (ControlObject)get_object(0);
	}
	
	@Override
	public int get_property_index()
	{
		return 2;
	}
	
	public GfxFrame get_tile()
	{
		return ((GfxFrameSet)get_object(1)).get_first_frame();
	}
	
	public AssociationProperty get_move_time_property()
	{
		return (AssociationProperty)get_object(2);
	}
	
	public int get_move_time()
	{
		String s = get_move_time_property().get_name();
		return Math.abs(Integer.parseInt(s));
	}
	
	public boolean is_back_and_forth()
	{
		String s = get_move_time_property().get_name();
		return Integer.parseInt(s) < 0;
	}
	

}
