package gods.base.associations;

import gods.base.*;
import gods.sys.*;

public class TriggerItemDisplayAssociation extends ObjectAssociation 
{
	public TriggerItemDisplayAssociation()
	{
		super(Type.Trigger_Item_Display,3);
	}
	@Override
	public String describe()
	{
		return "if hero walks into \""+get_trigger().get_name()+"\" and owns \""+get_must_own().get_name()+"\" then steal object "+
		"and display \""+get_item_to_display().get_name()+"\"";
	}
	@Override
	public void parse(ParameterParser fr, LevelData ld) throws java.io.IOException
	{
		set_object(0,ld.get_control_object(fr.readString("object")));
		for (int i = 1; i < m_associated_objects.size(); i++)
		{
			set_object(i,ld.get_bonus(fr.readString("object")));
		}
	}
	
	public ControlObject get_trigger()
	{
		return (ControlObject)get_object(0);
	}

	public GfxObject get_must_own()
	{
		return (GfxObject)get_object(1);
	}
	public GfxObject get_item_to_display()
	{
		return (GfxObject)get_object(2);
	}

	

}
