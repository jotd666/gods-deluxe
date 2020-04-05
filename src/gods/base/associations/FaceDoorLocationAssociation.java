package gods.base.associations;

import gods.base.AssociationProperty;
import gods.base.ControlObject;
import gods.base.LevelData;
import gods.sys.ParameterParser;

public class FaceDoorLocationAssociation extends ObjectAssociation {

	@Override
	public int get_property_index() 
	{
		return 2;
	}
	@Override
	public String describe()
	{
		String face_door_name = get_face_door().get_name();
		String location_name = get_location().get_name();
		String rval = "if hero enters in \""+face_door_name+"\" then ";
		
		if (face_door_name.equals(location_name))
		{
			// loop: special case
			rval +="end the level";
		}
		else
		{
			rval += "teleport to \""+location_name+"\"";

			if ((get_location_property() != null) && (!get_location_property().get_name().equals("")))
			{
				rval += " printing \""+get_location_property().get_name()+"\"";
			}
		}
		return rval;
	}
	public FaceDoorLocationAssociation()
	{
		super(Type.Face_Door_Location,3);
	}

	@Override
	public void parse(ParameterParser fr, LevelData ld) throws java.io.IOException
	{
		set_object(0,ld.get_control_object(fr.readString("object")));
		set_object(1,ld.get_control_object(fr.readString("object")));
		String location_desc = fr.readString("object",true);
		if (!location_desc.equals(ParameterParser.UNDEFINED_STRING))
		{
			set_object(2,new AssociationProperty(location_desc));
		}
	}
	
	public ControlObject get_face_door()
	{
		return (ControlObject)get_object(0);
	}
	
	public AssociationProperty get_location_property()
	{
		return (AssociationProperty)get_object(get_property_index());
	}
	public ControlObject get_location()
	{
		return (ControlObject)get_object(1);
	}

}
