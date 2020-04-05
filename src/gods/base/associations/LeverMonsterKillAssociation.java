package gods.base.associations;

import gods.base.*;
import gods.sys.*;

public class LeverMonsterKillAssociation extends ObjectAssociation 
{
	public LeverMonsterKillAssociation()
	{
		super(Type.Lever_Monster_Kill);
	}
	

	@Override
	public String describe()
	{
		String hostiles = make_list("and",1,get_nb_monsters());
		
		return "if hero pulls \""+get_lever().get_name()+"\" lever then kill hostiles "+hostiles;	
		
	}
	
	@Override
	public void parse(ParameterParser fr, LevelData ld) throws java.io.IOException
	{
		set_object(0,ld.get_bonus(fr.readString("object")));
		for (int i = 1; i < m_associated_objects.size(); i++)
		{
			set_object(i,ld.get_control_object(fr.readString("object")));
		}
	}
	
	public GfxObject get_lever()
	{
		return (GfxObject)get_object(0);
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
