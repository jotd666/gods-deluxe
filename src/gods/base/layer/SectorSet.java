package gods.base.layer;

import gods.base.*;

import java.util.*;

public class SectorSet<T extends NamedLocatable>
{

	
	private Vector< Sector<T> > m_sector_list;
	private int m_width;
	
	private Sector<T> sector(int i)
	{
		return m_sector_list.elementAt(Math.max(i,0) / m_width);
	}
	
	public SectorSet(int nb_sectors,int width)
	{
		if (nb_sectors == 0)
		{
			throw new RuntimeException("nb_sectors == 0");
		}
		m_sector_list = new Vector<Sector <T> >();
		
		for (int i = 0; i < nb_sectors; i++)
		{
			m_sector_list.add(new Sector<T>());
		}
		
		m_width = width / nb_sectors;
	}
//	 we have to declare when an object has moved x-wise

	public boolean move(T o, int old_x)
	{
		boolean rval = false;

		Sector<T> s_old = sector(old_x);
		Sector<T> s_new = sector(o.get_x());

		if (s_old != s_new)
		{
			// object is now misplaced: update

			s_old.remove(o);
			s_new.add(o);
		}
		return rval;
	}
	public boolean remove(T item)
	{
		boolean rval = false;
		if (item != null)
		{
			Sector<T> s = sector(item.get_x());
			if (s != null)
			{
				rval = s.remove(item);
			}
		}
		
		return rval;
	}
	
	public void add(Collection<T> list)
	{
		for (T t : list)
		{
			add(t);
		}
	}
	
	public void add(T item)
	{
		sector(item.get_x()).add(item);
	}
	
	public Sector<T>.Distance closest(Locatable other)
	{
		return closest(other,null);
	}
	
	public Sector<T>.Distance closest(Locatable other, Locatable avoid)
	{
		int sector_index = other.get_x() / m_width;
		
		Sector<T>.Distance t1 = m_sector_list.elementAt(sector_index).closest(other,avoid);
		
		if (sector_index > 0)
		{
			Sector<T>.Distance t2 = m_sector_list.elementAt(sector_index-1).closest(other,avoid);
			
			if (t1.square_distance > t2.square_distance)
			{
				t1 = t2;
			}
		}
		if (sector_index < m_sector_list.size() - 1)
		{
			Sector<T>.Distance t2 = m_sector_list.elementAt(sector_index+1).closest(other,avoid);
			
			if (t1.square_distance > t2.square_distance)
			{
				t1 = t2;
			}
		}
		
		return t1;
	}
	
}
