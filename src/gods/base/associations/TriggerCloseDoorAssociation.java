package gods.base.associations;

import gods.base.*;
import gods.sys.*;

public class TriggerCloseDoorAssociation extends ObjectAssociation 
{
	public TriggerCloseDoorAssociation()
	{
		super(Type.Trigger_Close_Door,2);
	}
	@Override
	public String describe()
	{
		return "if hero walks into \""+get_trigger().get_name()+"\" then close \""+get_door_to_close().get_name()+"\"";
	}
	@Override
	public void parse(ParameterParser fr, LevelData ld) throws java.io.IOException
	{
		set_object(0,ld.get_control_object(fr.readString("object")));
		for (int i = 1; i < m_associated_objects.size(); i++)
		{
			set_object(i,ld.get_control_object(fr.readString("object")));
		}
	}
	
	public ControlObject get_trigger()
	{
		return (ControlObject)get_object(0);
	}

	public GfxObject get_door_to_close()
	{
		return (GfxObject)get_object(1);
	}

	

}
