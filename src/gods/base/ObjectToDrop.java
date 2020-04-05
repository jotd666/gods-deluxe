package gods.base;

import gods.sys.ParameterParser;

import java.io.IOException;

public class ObjectToDrop implements Describable, Cloneable
{
	public String instance_name = null;
	public String class_name = null;
	public String hostile_name = null;
	
	public String get_block_name() 
	{
		return "OBJECT_DROPPED";
	}
	public ObjectToDrop()
	{}
	
	public ObjectToDrop clone() throws CloneNotSupportedException
	{
		ObjectToDrop rval = (ObjectToDrop)super.clone();
		
		if (instance_name != null) rval.instance_name = new String(instance_name);
		if (class_name != null) rval.class_name = new String(class_name);
		if (hostile_name != null) rval.hostile_name = new String(hostile_name);
		
		return rval;
	}
	public ObjectToDrop(ParameterParser fr) throws IOException 
	{
		String bn = fr.readBlockName();
		
		if (bn.equals("OBJECT_DROPPED"))
		{
			instance_name = fr.readString("instance_name",true);
			if (instance_name.equals(ParameterParser.UNDEFINED_STRING))
			{
				instance_name = null;
			}
			
			class_name = fr.readString("class_name",true);
			if (class_name.equals(ParameterParser.UNDEFINED_STRING))
			{
				class_name = null;
			}
			hostile_name = fr.readString("hostile_name",true);
			
			if (hostile_name.equals(ParameterParser.UNDEFINED_STRING))
			{
				hostile_name = null;
			}
		}
		
		fr.endBlockVerify();
	}
	
	public void serialize(ParameterParser fw) throws IOException 
	{
		fw.startBlockWrite(get_block_name());
		fw.write("instance_name",instance_name); // may be null
		fw.write("class_name",class_name);
		fw.write("hostile_name",hostile_name); // may be null
		fw.endBlockWrite();
	}		
}
