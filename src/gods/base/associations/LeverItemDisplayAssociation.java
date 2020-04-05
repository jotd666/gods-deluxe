package gods.base.associations;

import gods.base.GfxObject;
import gods.base.LevelData;
import gods.sys.ParameterParser;


public class LeverItemDisplayAssociation extends ObjectAssociation {

	public LeverItemDisplayAssociation()
	{
		super(Type.Lever_Item_Display,2);
	}
	@Override
	public String describe()
	{
		return "if hero pulls lever \""+get_object(0).get_name()+"\" then display \""+get_object(1).get_name()+"\"";
	}
	@Override
	public void parse(ParameterParser fr, LevelData ld) throws java.io.IOException
	{
		for (int i = 0; i < m_associated_objects.size(); i++)
		{
			set_object(i,ld.get_bonus(fr.readString("object")));
		}
	}
	
	public GfxObject get_lever()
	{
		return (GfxObject)get_object(0);
	}

	public GfxObject get_item_to_display()
	{
		return (GfxObject)get_object(1);
	}
}
