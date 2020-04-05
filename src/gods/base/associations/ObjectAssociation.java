package gods.base.associations;

import gods.base.LevelData;
import gods.base.Nameable;
import gods.sys.ParameterParser;

import java.io.IOException;
import java.util.Vector;

@SuppressWarnings("unchecked")
public abstract class ObjectAssociation implements Nameable
{	
	protected Vector<Nameable<?> > m_associated_objects;
	
	public enum Type { Teleport_Location, Lever_Door_Key, Lever_Platform_Key, Lever_Platform, Lever_Door, 
		Face_Door_Location, Trigger_Monster, Lever_Monster_Kill,
		Moving_Block_Tile, Lever_Item_Display, Trigger_Close_Door, 
		Trigger_Item_Display, Trigger_Item_Display_2, Trigger_Item_Display_Special }

	protected Type m_type;
	
	public abstract void parse(ParameterParser fr, LevelData ld) throws java.io.IOException;
	
	public abstract String describe();
	
	public boolean contains(Nameable<?> o)
	{
		return m_associated_objects.contains(o);
	}
	
	public int get_property_index()
	{
		return -1;
	}
	
	public Type get_type()
	{
		return m_type;
	}
	
	
	public int compareTo(Object arg0) 
	{
		return toString().compareTo(arg0.toString());
	}


	public String get_name()
	{
		String rval = null;
		Nameable<?> go = m_associated_objects.elementAt(0);
		if (go != null)
		{
			rval = go.get_name();
		}
		
		return rval;
	}
	public boolean is_named()
	{
		return get_name() != null;
	}
	
	public void set_name(String n) {
		
	}
	public String toString()
	{
		return get_name() + " (" + m_type.toString() +")";
	}
	
	public Nameable<?> get_object(int i)
	{
		return i < m_associated_objects.size() ? m_associated_objects.elementAt(i) : null;
	}

	public int get_nb_objects()
	{
		return m_associated_objects.size();
	}
	public void set_objects(Nameable<?> go, Object [] l)
	{
		m_associated_objects.clear();
		
		m_associated_objects.add(go);
		
		
		for (Object o : l)
		{
			m_associated_objects.add((Nameable<?>)o);
		}
	}
	
	public void set_object(int i, Nameable<?> o)
	{
		// add null items until index is reached
		while (i >= m_associated_objects.size())
		{
			m_associated_objects.add(null);
		}
		m_associated_objects.set(i,o);
		
		/*if (o != null)
		System.out.println("set object "+get_name()+" "+i+" "+o.get_name());
		else
			System.out.println("set object "+get_name()+i+" NULL");*/
		
		//mon amour
	}
	
	public static ObjectAssociation build_association(Type t) throws Exception
	{
		String class_name = "gods.base.associations."+t.toString().replace("_","") + "Association";
		
		return (ObjectAssociation)Class.forName(class_name).newInstance();
	
	}
	public static ObjectAssociation create(ParameterParser fr, LevelData ld) throws IOException
	{
		ObjectAssociation rval = null;
		
		fr.startBlockVerify(ObjectAssociation.get_block_name());
		
		String ts = fr.readString("type");
		Type t = ObjectAssociation.Type.valueOf(ts);
		
		int nb_objects = fr.readInteger("nb_objects");
		
		try 
		{
			rval = build_association(t);
			
			while (rval.m_associated_objects.size() < nb_objects)
			{
				rval.m_associated_objects.add(null);
			}
			
			rval.parse(fr, ld);
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		fr.endBlockVerify();
		
		// cancel the object if there was a problem
		
		if (rval.get_object(0) == null)
		{
			rval = null;
		}
		return rval;
	}	
	
	protected ObjectAssociation(Type type)
	{
		this(type,1);
	}
	
	protected ObjectAssociation(Type type, int nb_objects)
	{
		m_type = type;
		m_associated_objects = new Vector<Nameable<?>>();
		
		for (int i = 0; i < nb_objects; i++)
		{
			m_associated_objects.add(null);
		}
	}
	
	
	public static String get_block_name()
	{
		return "OBJECT_ASSOCIATION";
	}
	
	public boolean is_multi()
	{
		return false;
	}
	
	public void serialize(ParameterParser fw) throws IOException 
	{
		fw.startBlockWrite(get_block_name());
		
		fw.write("type", m_type.toString());
				
		fw.write("nb_objects", m_associated_objects.size());
		
		for (Nameable<?> go : m_associated_objects)
		{
			if ((go != null) && (go.get_name().length() > 0))
			{
				fw.write("object",go.get_name());
			}
			else
			{
				fw.write("object",ParameterParser.UNDEFINED_STRING);
			}
		}
		
		fw.endBlockWrite();
		
	}
	protected String make_list(String conjuction, int index, int len)	
	{
		String rval = "";
		
		for (int i = index; i < len+index; i++)
		{
			if (i != index)
			{
				rval += " "+conjuction+" ";				
			}
			if (get_object(i) != null)
			{
			rval += "\""+get_object(i).get_name()+"\"";
			}
		}
		return rval;
	}
	
}
