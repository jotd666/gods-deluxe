package gods.base.layer;

import java.util.*;

import gods.base.*;

public class Sector<T extends NamedLocatable> 
{
	private HashMap<String, T> m_items = new HashMap<String, T>();
	
	public class Distance
	{
		public T item = null;
		public int square_distance = Integer.MAX_VALUE;
		public Sector<T> source = null;
	}
	
	public Sector()
	{
		
	}

	
	public Distance closest(Locatable other, Locatable avoid)
	{
		Distance rval = new Distance();
				
		for (T t : m_items.values())
		{
			if (t != avoid && t.is_pickable())
			{
				int dist = other.square_distance_to(t);

				if (dist < rval.square_distance)
				{
					rval.item = t;
					rval.square_distance = dist;
					rval.source = this;
				}
			}
		}
		
		return rval;
	}
	
	public Collection<T> items()
	{
		return m_items.values();
	}
	
	public void add(T o)
	{
		m_items.put(o.get_name(), o);
	}
	
	public boolean remove(T o)
	{
		return m_items.remove(o.get_name()) != null;
	}
	
	public T lookup(String name)
	{
		return m_items.get(name);
	}
}
