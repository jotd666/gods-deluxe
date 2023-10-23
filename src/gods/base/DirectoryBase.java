package gods.base;

import java.io.File;

/**
 * Manage base directories paths.
 */
public class DirectoryBase 
{
  private static final String USER_DIR = "" + System.getProperty("user.dir");
  public static final String GFX_OBJECT_SET_EXTENSION = ".gos";
  public static final String LEVEL_EXTENSION = ".glv";

  private static String m_root = USER_DIR;
  private static String assets_path = m_root + File.separator;
  private static String data_path = m_root + File.separator + "data" + File.separator;

  public static String get_icons_path()
  {
    return get_assets_path() + "icons" + File.separator;
  }
  
  static public String get_images_path()
  {
    return get_assets_path() + "images" + File.separator;
  }
  
  static private String get_music_path()
  {
    return get_assets_path() + "music" + File.separator;
  }
  
  static public String get_mp3_path()
  {
    return get_music_path() + File.separator + "mp3" + File.separator;
  }
  
  static public String get_mod_path()
  {
    return get_music_path() + File.separator + "mod" + File.separator;
  }
  
  static public String get_sound_path()
  {
    return get_assets_path() + "sound" + File.separator;
  }
  
  static public String get_tiles_path()
  {
    return get_assets_path() + "tiles" + File.separator;
  }

  static public String get_snapshot_path()
  {
    return get_data_path() + "snapshots" + File.separator;
  }
  
  static public void check_paths() throws Exception
  {
    File r = new File(get_root());
    if (!r.exists())
    {
      throw new Exception("Directory "+ get_root() +" does not exist.");
    }
    
    String envAssetsDir = System.getProperty("GODS_ASSETS_DIR");
    if (envAssetsDir != null)
    {
      File assetsDir = new File(envAssetsDir);
      assets_path = assetsDir.getAbsolutePath() + File.separator;
    }
    
    String envDataDir = System.getProperty("GODS_DATA_DIR");
    if (envDataDir != null) {
      data_path = envDataDir;
    } else {
      data_path = assets_path + File.separator + "data" + File.separator;
    }
    File dataDir = new File(data_path);
    if (!dataDir.exists())
    {
      dataDir.mkdirs();
    }
    if (!dataDir.canWrite())
    {
      System.out.println("Cannot write data to " + data_path);
      System.exit(1);
    }
    data_path = dataDir.getAbsolutePath() + File.separator;
    System.out.println("Using assets path: " + assets_path);
    System.out.println("Using data path: " + data_path);
  }

  public static String get_root()
  {
    return m_root;
  }

  public static String get_assets_path()
  {
	  return assets_path;
  }

  public static String get_data_path()
  {
	  return data_path;
  }
  
  static public String get_font_path()
  {
	  return get_assets_path() + "fonts" + File.separator;
  }
  
  static public String get_levels_path()
  {
	  return get_assets_path() + "levels" + File.separator;
  }
  
  public static String cut_extension(String s)
  {
	  int idx = s.lastIndexOf(".");
	  return idx == -1 ? s : s.substring(0,idx);
  }

  static public String rework_path(String path,String leading_to_remove,boolean remove_root,boolean remove_extension)
  {
	  String rval = path;
	  if (remove_root)
	  {
		  rval = path.replace(leading_to_remove.subSequence(0, leading_to_remove.length()),"");
	  }
	  if (remove_extension)
	  {
		  File f = new File(rval);
		  String dn = f.getParent();
		  String fn = f.getName();
		  
		  fn = cut_extension(fn);
		  if (dn != null)
		  {
			  rval = dn + File.separator + fn;
		  }
		  else
			 
		  {
			  rval = fn;
		  }
	  }
 
	  
	  return rval;
  }

}
