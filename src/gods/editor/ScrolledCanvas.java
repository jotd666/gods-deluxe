package gods.editor;

import java.awt.*;
import javax.swing.*;

/**
 * <p>Titre : </p>
 * <p>Description : Scrolled manager for circuit display in editor</p>
 * <p>Copyright : Copyright (c) 2005</p>
 * <p>Société : </p>
 * @author non attribuable
 * @version 1.0
 */

public abstract class ScrolledCanvas<T extends JPanel> extends JPanel
{
	private static final long serialVersionUID = 1L;
    
	protected T contents;
    
  public ScrolledCanvas(T contents)
  {
	  super(new BorderLayout());
	  this.contents = contents;
	  add(new JScrollPane(contents),BorderLayout.CENTER);  
    }

  public abstract void update() throws java.io.IOException;

 

}