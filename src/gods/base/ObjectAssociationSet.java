package gods.base;

import gods.base.associations.ObjectAssociation;
import gods.sys.ParameterParser;

import java.io.IOException;
import java.util.*;

public class ObjectAssociationSet implements Describable 
{
	private HashMap<String,ObjectAssociation> m_associations = new HashMap<String,ObjectAssociation>();
	
	public String get_block_name() {
		
		return "OBJECT_ASSOCIATION_SET";
	}

	public int remove_associations_containing(Nameable<?> o)
	{
		int nb_removed = 0;
		
		while(true)
		{
		   // delete the association(s) containing this object TODO
			ObjectAssociation oa = get_first_containing(o);
			if (oa == null) break;
			nb_removed++;
			remove(oa);
		}
		
		return nb_removed;
	}
	
	public boolean contains(Nameable<?> o)
	{
		return get_first_containing(o) != null;
	}
	public ObjectAssociation get_first_containing(Nameable<?> o)
	{
		ObjectAssociation rval = null;
		
		for (ObjectAssociation oa : m_associations.values())
		{
			if (oa.contains(o))
			{
				rval = oa;
				break;
			}
		}
		
		return rval;
	}
	
	ObjectAssociation add(ObjectAssociation o)
	{
		return m_associations.put(o.get_name(), o);
	}
	
	public void remove(ObjectAssociation o)
	{
		m_associations.remove(o.get_name());
	}
	

	public ObjectAssociationSet()
	{
		
	}
	
	public ObjectAssociationSet(ParameterParser fr, LevelData ld) throws IOException
	{
		fr.startBlockVerify(get_block_name());
		
		int nb_associations = fr.readInteger("nb_associations");
		
		for (int i = 0; i < nb_associations; i++)
		{
			ObjectAssociation oa = ObjectAssociation.create(fr, ld);
			if (oa != null)
			{
				add(oa);
			}
		}
		fr.endBlockVerify();
		
	}
	public ObjectAssociation get(String name)
	{
		return m_associations.get(name);
	}
	public Collection<ObjectAssociation> items()
	{
		return m_associations.values();
	}
	
	public void serialize(ParameterParser fw) throws IOException 
	{
		fw.startBlockWrite(get_block_name());
	
		fw.write("nb_associations", m_associations.size());
		
		for (ObjectAssociation o : items())
		{
			o.serialize(fw);
		}
		
		fw.endBlockWrite();
	}

}
