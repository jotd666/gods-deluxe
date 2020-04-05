package gods.base.associations;

import gods.base.*;
import gods.sys.*;

public class TriggerMonsterAssociation extends ObjectAssociation 
{
	public TriggerMonsterAssociation()
	{
		super(Type.Trigger_Monster);
	}
	
	@Override
	public void parse(ParameterParser fr, LevelData ld) throws java.io.IOException
	{
		for (int i = 0; i < m_associated_objects.size(); i++)
		{
			set_object(i,ld.get_control_object(fr.readString("object")));
		}
	}
	@Override
	public String describe()
	{
		int nb_monsters = get_nb_monsters();
		
		String hostiles = make_list("and",1,nb_monsters);
		if (nb_monsters > 1)
		{
			hostiles = "s "+hostiles;
		}
		else
		{
			hostiles = " "+hostiles;
		}
		
		return "if hero walks into \""+get_trigger().get_name()+"\" then spawn hostile"+hostiles;

	}
	public ControlObject get_trigger()
	{
		return (ControlObject)get_object(0);
	}
	public int get_nb_monsters()
	{
		return m_associated_objects.size()-1;
	}
	public ControlObject get_monster(int i)
	{
		return (ControlObject)get_object(i+1);
	}

	@Override
	public boolean is_multi() 
	{
		return true;
	}
	

}
