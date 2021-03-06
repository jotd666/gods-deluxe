package gods.base.associations;

import gods.base.ControlObject;
import gods.base.GfxObject;
import gods.base.LevelData;
import gods.sys.ParameterParser;

public class LeverPlatformKeyAssociation extends ObjectAssociation {

	public LeverPlatformKeyAssociation()
	{
		super(Type.Lever_Platform_Key,3);
	}
	@Override
	public String describe()
	{
		return "if hero pulls \""+get_lever().get_name()+"\" lever and owns \""+get_key().get_name()+"\" then steal the key and move \""+get_platform().get_name()+"\"";		
		
	}
	@Override
	public void parse(ParameterParser fr, LevelData ld) throws java.io.IOException
	{
		set_object(0,ld.get_bonus(fr.readString("object")));
		set_object(1,ld.get_control_object(fr.readString("object")));
		set_object(2,ld.get_bonus(fr.readString("object")));
	}

	public GfxObject get_lever()
	{
		return (GfxObject)get_object(0);
	}
	public ControlObject get_platform()
	{
		return (ControlObject)get_object(1);
	}
	public GfxObject get_key()
	{
		return (GfxObject)get_object(2);
	}
	
}
