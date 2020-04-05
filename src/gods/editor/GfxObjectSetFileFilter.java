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

public class GfxObjectSetFileFilter extends FileFilter {
  public GfxObjectSetFileFilter() {
  }

  public boolean accept(File f)
  {
    String s = f.toString().toLowerCase();

    return (f.isDirectory() || (s.endsWith(DirectoryBase.GFX_OBJECT_SET_EXTENSION)));
  }

  public String getDescription()
  {
    return "Graphic Object Set files (*"+DirectoryBase.GFX_OBJECT_SET_EXTENSION+")";
  }
}
