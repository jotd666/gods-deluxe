package gods.base;

public class AssociationProperty implements Nameable<Object> {

	private String m_name;
	
	public AssociationProperty(String n)
	{
		m_name = n;
	}
	
	public String get_name() 
	{
		return m_name;
	}

	public boolean is_named()
	{
		return !m_name.equals("");
	}
	public String toString()
	{
		return m_name+" (property)";
	}
	
	public void set_name(String n) 
	{
		m_name = n;

	}

	public int compareTo(Object arg0) 
	{
		return m_name.compareTo(arg0.toString());
	}

}
