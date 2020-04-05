package gods.base;

import java.io.File;

/**
 * <p>Titre : </p>
 * <p>Description : </p>
 * <p>Copyright : Copyright (c) 2005</p>
 * <p>Société : </p>
 * @author non attribuable
 * @version 1.0
 */

public class DirectoryBase 
{
  public static final String ROOT_DIR_VARIABLE = "ROOT_DIR";
  private static String m_root = null;
  private static final String m_user = ""+System.getProperty("user.dir");
  public static final String GFX_OBJECT_SET_EXTENSION = ".gos";
  public static final String LEVEL_EXTENSION = ".glv";
  
  static public String get_user_path()
  {
	  return m_user + File.separator;
  }
  
  static public String get_level_class_name()
  {
	  return "gods.game.levels";
  }
  static public String get_level_class_path()
  {
	  return get_root() + "bin" + File.separator + "gods" + File.separator + "game" + 
	  File.separator + "levels";
  }
  
  static public String get_images_path()
  {
    return get_root() + "images" + File.separator;
  }
  static private String get_music_path()
  {
    return get_root() + "music" + File.separator;
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
    return get_root() + "sound" + File.separator;
  }
  
  static public String get_tiles_path()
  {
    return get_root() + "tiles" + File.separator;
  }
  static public String get_snapshot_path()
  {
    return get_root() + "snapshots" + File.separator;
  }
  
   static public void env_check() throws Exception
   {
       if (get_root() == null)
	   {
	       throw new Exception(ROOT_DIR_VARIABLE+" has not been set");
	   }
       File r = new File(get_root());
       if (!r.exists())
       {
	       throw new Exception(ROOT_DIR_VARIABLE+" directory "+get_root()+" does not exist, should equal ${project_loc}" );
    	   
       }
       
   }
   
  
  static public String get_root()
  {
	  if (m_root == null)
	  {
		  m_root = System.getProperty(ROOT_DIR_VARIABLE) + File.separator;
	  }
    return m_root;
  }

  
  static public String get_font_path()
  {
	  return get_root() + "fonts" + File.separator;
  }
  
  static public String get_levels_path()
  {
	  return get_root() + "levels" + File.separator;
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
