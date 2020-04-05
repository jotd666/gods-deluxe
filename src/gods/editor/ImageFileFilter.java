package gods.editor;

import java.io.File;
import javax.swing.filechooser.*;



public class ImageFileFilter extends FileFilter 
{
  public ImageFileFilter() {
  }

  public boolean accept(File f)
  {
    String s = f.toString().toLowerCase();

    return (f.isDirectory() || (s.endsWith(".png")));
  }

  public String getDescription()
  {
    return "Image files (*.png)";
  }
}
