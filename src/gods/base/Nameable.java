package gods.base;

/**
 * <p>Titre : </p>
 * <p>Description : </p>
 * <p>Copyright : Copyright (c) 2005</p>
 * <p>Société : </p>
 * @author non attribuable
 * @version 1.0
 */

public interface Nameable<T> extends Comparable<T>
{
  public String get_name();
  public void set_name(String n);
  public boolean is_named();
}