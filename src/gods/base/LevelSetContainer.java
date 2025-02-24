package gods.base;

import java.io.File;
import java.util.Arrays;
import java.util.Vector;

public class LevelSetContainer 
{
	private Vector<LevelSet> m_levels = new Vector<LevelSet>();
	private int m_current_level_set_index;
	
	public int size()
	{
		return m_levels.size();
	}
	
	public LevelSet get_item(int i)
	{
		LevelSet rval = m_levels.elementAt(0);
		
		if (i < m_levels.size())
		{
			rval = m_levels.elementAt(i);
		}
		
		return rval;
	}
	
	public void next_level_set()
	{
		m_current_level_set_index++;
		if (m_current_level_set_index == m_levels.size())
		{
			m_current_level_set_index = 0;
		}
		
		get_current_level_set().init();
	}
	public void previous_level_set()
	{
		if (m_current_level_set_index == 0)
		{
			m_current_level_set_index = m_levels.size();
		}
		m_current_level_set_index--;

		get_current_level_set().init();
	}

	public LevelSet get_current_level_set()
	{
		return m_levels.elementAt(m_current_level_set_index);
	}
	
	public int get_current_level_set_index()
	{
		return m_current_level_set_index;
	}
	public void set_current_level_set_index(int idx)
	{
		m_current_level_set_index = idx;
	}
	
	public LevelSetContainer(int current_level_set_index)
	{
		File dir = new File(DirectoryBase.get_levels_path());
		String[] level_list = dir.list();
		Arrays.sort(level_list);
		
		int i = 0;
		
		for (String l : level_list)
		{	
			LevelSet ls = LevelSet.create(l,i);
			if (ls != null)
			{
				m_levels.add(ls); 
			}
			
			i++;
		}
		
		m_current_level_set_index = current_level_set_index;
	}
}
