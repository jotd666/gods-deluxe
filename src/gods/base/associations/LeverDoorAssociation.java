package gods.base.associations;

import gods.base.ControlObject;
import gods.base.GfxObject;
import gods.base.LevelData;
import gods.sys.ParameterParser;

public class LeverDoorAssociation extends ObjectAssociation {

	public LeverDoorAssociation()
	{
		super(Type.Lever_Door,2);
	}
	@Override
	public String describe()
	{
		// default: print "open", else print "close" (naming heuristic)
		
		String action = get_lever().get_name().contains("close") ? "close" : "open";
		return "if hero pulls \""+get_lever().get_name()+"\" lever then "+action+" \""+get_door().get_name()+"\"";		
	}
	@Override
	public void parse(ParameterParser fr, LevelData ld) throws java.io.IOException
	{
		set_object(0,ld.get_bonus(fr.readString("object")));
		set_object(1,ld.get_control_object(fr.readString("object")));
	}

	public GfxObject get_lever()
	{
		return (GfxObject)get_object(0);
	}
	public ControlObject get_door()
	{
		return (ControlObject)get_object(1);
	}
	
}
