package gods.base;

import java.util.HashMap;
import java.util.Map;

import gods.sys.Localizer;
import gods.sys.ParameterParser;

public class GameOptions
{
	public static final int NO_TEST = 0; 
	public static final int SHOP_TEST = 1;
	
	public final static String[] CONTROL_METHOD = { "control method keyboard" };

	
	public enum GfxType { REWORKED, ORIGINAL, SCALE2X }
	public enum HeroType { BLUE, GREEN }
	
	public enum SoundType { FULL_SOUND ,MUSIC_ONLY, SOUND_EFFECTS, SILENCE}
	public enum StartDifficultyLevel { NORMAL, HARD, VERY_HARD};
	public enum TileSetType { JOTD, YASIR }
	
	public enum JumpType { UP, SHIFT }
	
	public String[] LANGUAGES;
	
	private boolean cheat_mode = false;	
	public boolean with_intro = true;
	public boolean with_level_cache = true;
	public boolean unlock_levels = false;
	public boolean direct_game = false;		
	public boolean full_screen = false;
	
	public int test_mode = NO_TEST;
	private SoundType audio_mode = SoundType.FULL_SOUND;
	private GfxType gfx_type = GfxType.REWORKED;
	private HeroType hero_type = HeroType.BLUE;
	private TileSetType tileset_type = TileSetType.JOTD;
	private JumpType control_type = JumpType.UP;
	private int current_language_index = 0;
	private StartDifficultyLevel start_difficulty_level = StartDifficultyLevel.NORMAL;
	
	private boolean player_boost = false;
	
	private static final GameOptions m_instance = new GameOptions();

	private LevelSetContainer m_level_set_container;
	
	public JumpType get_control_type()
	{
		return control_type;
	}
	public TileSetType get_tileset_type()
	{
		return tileset_type;
	}

	public class MaximumLevel
	{
		int value;
		MaximumLevel(int max_level)
		{
			value = max_level;
		}
	}
	
	private HashMap<String,MaximumLevel> m_unlocked_levels = new HashMap<String,MaximumLevel>();
	
	
	public static final GameOptions instance()
	{
		return m_instance;
	}

	public String get_font_gfx_flavor()
	{
		return (gfx_type == GfxType.ORIGINAL) ? "original" : "reworked";
	}
	
	private int get_max_level_current()
	{
		return get_max_level(get_current_level_set_name());
	}
	private int get_max_level(String level_set)
	{
		MaximumLevel ml = m_unlocked_levels.get(level_set);
		
		int rval = ml == null ? 1 : ml.value;
		
		if (unlock_levels)
		{
			rval = m_level_set_container.get_current_level_set().get_nb_levels();
		}
		
		return rval;
	}
	
	public String get_current_level_set_name()
	{
		return m_level_set_container.get_current_level_set().get_name();
	}
	public boolean get_cheat_mode()
	{
		return cheat_mode;
	}
	
	public boolean get_music_state()
	{
		return (audio_mode == SoundType.FULL_SOUND) || (audio_mode == SoundType.MUSIC_ONLY);
	}
	
	public boolean get_sfx_state()
	{
		return (audio_mode == SoundType.FULL_SOUND) || (audio_mode == SoundType.SOUND_EFFECTS);
	}
	
	
	protected GameOptions()	{}
	
	public void prev_language()
	{
		 if (current_language_index == 0)
		 {
			 current_language_index = LANGUAGES.length;
		 }
		 current_language_index--;
		 
		 set_language(current_language_index);
	}
	public void next_language()
	{
		 if (++current_language_index == LANGUAGES.length)
		 {
			 current_language_index = 0;
		 }
		 set_language(current_language_index);
		 
	}
	public int get_language()
	{
		return current_language_index;
	}
	
	public void set_language(int language_index)
	{
		// o tilde and u tilde are replaced by unicode c
		final int [] LETTERS_TO_REPLACE = 
		{256+'Q',256+'q',
				'à','á','é','è','í','ó','ö','ú','Õ','Á','É','Ö','Ó','Ü','Í'};
		final int [] LETTERS_REPLACED = 
		{'o','u',
				'a','a','e','e','i','o','o','u','O','A','E','O','O','U','I'};
		
		current_language_index = language_index;
		Localizer.set_replacement_letters(LETTERS_TO_REPLACE,LETTERS_REPLACED);
		
		Localizer.set_language(LANGUAGES[language_index]);	
	}
	
	public void save()
	{
		try
		{
			ParameterParser fr = ParameterParser.create(get_settings_file_path());

			fr.startBlockWrite("GODS_LEVELS");
			
			int nbl = m_unlocked_levels.size();
			
			fr.write("nb_level_sets", nbl);
			
			for (Map.Entry<String,MaximumLevel> ml : m_unlocked_levels.entrySet())
			{
				fr.write("level_set", ml.getKey());
				fr.write("value", ml.getValue().value);
			}
			
			fr.endBlockWrite();			
			
			fr.startBlockWrite("GODS_SETTINGS");
						
			fr.write("language",current_language_index);
			
			fr.write("audio", audio_mode.toString());
			
			fr.write("gfx", gfx_type.toString());
			
			fr.write("difficulty_level",start_difficulty_level.toString());
			
			fr.write("level_set_index", m_level_set_container.get_current_level_set_index());
			
			fr.write("level_index", m_level_set_container.get_current_level_set().get_start_level());
			
			fr.write("hero_type", hero_type.toString());
			fr.write("tileset_type", tileset_type.toString());
			fr.write("control_type", control_type.toString());
			fr.endBlockWrite();
			

			fr.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}		
	}

	public void load_settings()
	{
		int level_set_index = 1;  // if no settings: classic
		int level_index = 1;
		
		try
		{
			LANGUAGES = Localizer.get_available_languages();

			ParameterParser fr = ParameterParser.open(get_settings_file_path());
			
			fr.startBlockVerify("GODS_LEVELS");
			
			int nbl = fr.readInteger("nb_level_sets");

			for (int i = 0; i < nbl; i++)
			{
				String level_set = fr.readString("level_set");
				int value =	fr.readInteger("value");
				
				m_unlocked_levels.put(level_set, new MaximumLevel(value));
			}
			
			m_unlocked_levels.put("test", new MaximumLevel(20));
			
			fr.endBlockVerify();

			fr.startBlockVerify("GODS_SETTINGS");
			
			//update_cheat_mode();
	
			set_language(fr.readInteger("language"));

			audio_mode = SoundType.valueOf(fr.readString("audio"));
			
			gfx_type = GfxType.valueOf(fr.readString("gfx"));
			
			start_difficulty_level = StartDifficultyLevel.valueOf(fr.readString("difficulty_level"));

			level_set_index = fr.readInteger("level_set_index");
			
			level_index = fr.readInteger("level_index");

			hero_type = HeroType.valueOf(fr.readString("hero_type"));
			tileset_type = TileSetType.valueOf(fr.readString("tileset_type"));
			control_type = JumpType.valueOf(fr.readString("control_type"));
			
			fr.endBlockVerify();
			
			fr.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		m_level_set_container = new LevelSetContainer(level_set_index);
		m_level_set_container.get_current_level_set().set_start_level(level_index);
	}

	public void prev_gfx_type()
	{
		int current = gfx_type.ordinal();
		
		if (current == 0)
		{
			current = GfxType.values().length;
		}
		
		current--;
		gfx_type = GfxType.values()[current];
	}
	public void next_gfx_type()
	{
		int current = gfx_type.ordinal();
		current++;
		if (current == GfxType.values().length)
		{
			current = 0;
		}
		
		gfx_type = GfxType.values()[current];
	}
	public GfxType get_gfx_type() {
		return gfx_type;
	}

	public void set_gfx_type(GfxType gfx_type) {
		this.gfx_type = gfx_type;
	}
	public void next_tileset_type()
	{
		int current = tileset_type.ordinal();
		current++;
		if (current == TileSetType.values().length)
		{
			current = 0;
		}
		
		tileset_type = TileSetType.values()[current];
	}
	public void prev_tileset_type()
	{
		int current = tileset_type.ordinal();
		
		if (current == 0)
		{
			current = TileSetType.values().length;
		}
		
		current--;
		tileset_type = TileSetType.values()[current];
			
	}

	public void next_control_type()
	{
		int current = control_type.ordinal();
		current++;
		if (current == JumpType.values().length)
		{
			current = 0;
		}
		
		control_type = JumpType.values()[current];
	}
	public void prev_control_type()
	{
		int current = control_type.ordinal();
		
		if (current == 0)
		{
			current = JumpType.values().length;
		}
		
		current--;
		control_type = JumpType.values()[current];
			
	}
	public void prev_hero_type()
	{
		int current = hero_type.ordinal();
		
		if (current == 0)
		{
			current = HeroType.values().length;
		}
		
		current--;
		hero_type = HeroType.values()[current];
	}
	public void next_hero_type()
	{
		int current = hero_type.ordinal();
		current++;
		if (current == HeroType.values().length)
		{
			current = 0;
		}
		
		hero_type = HeroType.values()[current];
	}

	public HeroType get_hero_type() {
		return hero_type;
	}

	public void set_hero_type(HeroType hero_type) {
		this.hero_type = hero_type;
	}
	public StartDifficultyLevel get_start_difficulty_level()
	{
		return start_difficulty_level;
	}
	public void next_difficulty_level()
	{
		int current = start_difficulty_level.ordinal();
		current++;
		if (current == StartDifficultyLevel.values().length)
		{
			current = 0;
		}
		start_difficulty_level = StartDifficultyLevel.values()[current];
	}
	
	public void prev_difficulty_level()
	{
		int current = start_difficulty_level.ordinal();
		
		if (current == 0)
		{
			current = StartDifficultyLevel.values().length;
		}
		
		current--;
		start_difficulty_level = StartDifficultyLevel.values()[current];
	}	
	public SoundType get_audio_mode()
	{
		return audio_mode;
	}

	public void next_audio_mode()
	{
		int current = audio_mode.ordinal();
		current++;
		if (current == SoundType.values().length)
		{
			current = 0;
		}
		
		audio_mode = SoundType.values()[current];
	}
	public void prev_audio_mode()
	{
		int current = audio_mode.ordinal();
		
		if (current == 0)
		{
			current = SoundType.values().length;
		}
		
		current--;
		audio_mode = SoundType.values()[current];
	}
	
	public boolean set_audio_mode(SoundType audio_mode)
	{
		boolean rval = (this.audio_mode != audio_mode);
		
		this.audio_mode = audio_mode;
		
		return rval;
	}




    public boolean get_player_boost()
    {
    	//update_cheat_mode();
    	
    	return cheat_mode && player_boost;
    }
    
    public void set_player_boost(boolean b)
    {
    	player_boost = b;
    }

    public boolean unlock_next_level_in_current_level_set()
    {
    	LevelSet level_set = get_current_level_set();
    	
    	boolean rval = level_set.next_level();

    	MaximumLevel ml = m_unlocked_levels.get(level_set.get_name());

    	int max_level = level_set.get_level_index();

    	if (ml == null)
    	{
    		m_unlocked_levels.put(level_set.get_name(),new MaximumLevel(max_level));
    	}
    	else
    	{
    		if (ml.value < max_level)
    		{		
    			ml.value = max_level;
    		}

    	}   
    	
    	// we don't want to lose this precious piece of information
    	
    	save();
    	
    	return rval;
    }
    public void next_level_in_current_level_set()
    {
    	get_current_level_set().next_start_level(get_max_level_current());
    }
    public void prev_level_in_current_level_set()
    {
    	get_current_level_set().prev_start_level(get_max_level_current());
    }
    public LevelSet get_current_level_set()
    {
    	return m_level_set_container.get_current_level_set();
    }
    public void previous_level_set()
    {
    	m_level_set_container.previous_level_set();
    }
    public void next_level_set()
    {
      	m_level_set_container.next_level_set();
    }
   
    public LevelSetContainer get_level_set_container()
    {
    	return m_level_set_container;
    }

	private String get_settings_file_path()
	{
		return DirectoryBase.get_data_path() + "gods_settings";
	}
}
