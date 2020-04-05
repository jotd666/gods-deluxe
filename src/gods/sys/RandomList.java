package gods.sys;

import java.util.*;

public class RandomList<T>
{
	private Vector<T> m_contents;
	
	public Vector<T> get_contents()
	{
		return m_contents;
	}
	
	public RandomList(Collection<T> input)
	{
		this(input,input.size());
	}
	
	// TODO optimize randomlist
	
	public RandomList(Collection<T> input, int nb_items_to_draw)
	{
		m_contents = new Vector<T>();
		
		// properly clone input list

		LinkedList<T> copy = new LinkedList<T>();
		
		Iterator<T> it = input.iterator();

		while (it.hasNext())
		{
			copy.add(it.next());
		}

		while(copy.size() > 0)
		{
			int pos = (int)(Math.random() * copy.size());

			it = copy.iterator();
			T s = it.next();
			for (int i = 0; i < pos; i++)
			{
				s = it.next();
			}
			copy.remove(s);

			m_contents.add(s);
		}		

	}
}
