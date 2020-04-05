package gods.base;

import java.util.Observable;

/**
 * <p>Titre : </p>
 * <p>Description : </p>
 * <p>Copyright : Copyright (c) 2005</p>
 * <p>Société : </p>
 * @author non attribuable
 * @version 1.0
 */

public class Selectable extends Observable {
  private boolean m_selected = false;

 public boolean is_selected()
  {
    return m_selected;
  }

  public void toggle_selection()
  {
    m_selected = !m_selected;
  }
  public void select()
    {
      m_selected = true;
    }

    public void unselect()
    {
      m_selected = false;
    }

    public void set_changed()
    {
      setChanged();
    }
}