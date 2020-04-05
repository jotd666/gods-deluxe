package gods.editor;

import java.io.File;
import javax.swing.filechooser.*;
import gods.base.DirectoryBase;

/**
 * <p>Titre : Asm Processor</p>
 * <p>Description : </p>
 * <p>Copyright : Copyright (c) 2003</p>
 * <p>Société : Crack Inc.</p>
 * @author JOTD
 * @version 1.0
 */

public class LevelFileFilter extends FileFilter {
  public LevelFileFilter() {
  }

  public boolean accept(File f)
  {
    String s = f.toString().toLowerCase();

    return (f.isDirectory() || (s.endsWith(DirectoryBase.LEVEL_EXTENSION)));
  }

  public String getDescription()
  {
    return "Level files (*"+DirectoryBase.LEVEL_EXTENSION+")";
  }
}
