package gods.base;

import java.io.File;

public class LevelSet 
{
	
	/*public String get_level_path()
	{
		return DirectoryBase.get_levels_path() + name + File.separator + "level_" + 
		(counter+1) + DirectoryBase.LEVEL_EXTENSION;
	}*/

	private int start_level = 1; // start level
	private int level_index = 1; // current played level
	private int nb_levels = 0; // total number of levels in the level set
	private int index; // identify the level set in the array
	private String name;
	
	public static final LevelSet create(String name, int index) 
	{
		LevelSet rval = null;
		
		// compute number of levels
		
		File f = new File(DirectoryBase.get_levels_path() + name);
		if (f.isDirectory())
		{
			rval = new LevelSet();
			rval.name = name;
			rval.index = index;

			String [] files = f.list();
			rval.nb_levels = 0;
			for (String s : files)
			{
				if (s.endsWith(DirectoryBase.LEVEL_EXTENSION))
				{
					rval.nb_levels++;
				}
			}
		}
		
		return rval;
	}
	
	public String get_name()
	{
		return name;
	}
	
	public int get_index()
	{
		return index;
	}
	
	public int get_nb_levels()
	{
		return nb_levels;
	}
	
	public int get_level_index()
	{
		return level_index;
	}
	
	public void init()
	{
		level_index = start_level;
	}
	
	public boolean next_level()
	{
		boolean game_completed = (level_index == nb_levels);
		
		if (game_completed)
		{
			// loopback
			level_index = 0;
		}
		
		level_index++;
		
		return game_completed;

	}
	public void next_start_level(int max_level)
	{
		if (start_level == Math.min(nb_levels,max_level))
		{
			start_level = 0;
		}
		
		start_level++;

		init();
	}
	public void prev_start_level(int max_level)
	{
		start_level--;
		
		if (start_level == 0)
		{
			start_level = Math.min(nb_levels,max_level);
		}

		init();
	}

	public int get_start_level() 
	{
		return start_level;
	}

	public void set_start_level(int start_level) 
	{
		level_index = start_level;
		this.start_level = start_level;
	}
}
