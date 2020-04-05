package gods.base;


public abstract class NamedLocatable extends Locatable implements Nameable<Object>
{
	public NamedLocatable()
	{
		super();
	}
	public NamedLocatable(int x, int y, int width, int height)
	{
		super(0,0,width,height);
	}
}
