package gods.base.associations;

import gods.base.ControlObject;
import gods.base.GfxObject;
import gods.base.LevelData;
import gods.sys.ParameterParser;

public class TeleportLocationAssociation extends ObjectAssociation {

	public TeleportLocationAssociation()
	{
		super(Type.Teleport_Location,2);
		
		
	}
	@Override
	public String describe()
	{
		return "if hero takes teleport gem \""+get_teleport().get_name()+
		"\" then teleport to \""+get_location().get_name()+"\"";
	}
	
	@Override
	public void parse(ParameterParser fr, LevelData ld) throws java.io.IOException
	{
		set_object(0,ld.get_bonus(fr.readString("object")));
		set_object(1,ld.get_control_object(fr.readString("object")));
	}

	public GfxObject get_teleport()
	{
		return (GfxObject)get_object(0);		
	}
	
	public ControlObject get_location()
	{
		return (ControlObject)get_object(1);
	}

}
