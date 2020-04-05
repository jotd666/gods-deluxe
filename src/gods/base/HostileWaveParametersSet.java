package gods.base;

import gods.sys.ParameterParser;

import java.io.IOException;
import java.util.*;

public class HostileWaveParametersSet implements Describable {

	private HashMap<ControlObject,HostileWaveParameters> m_items = new HashMap<ControlObject,HostileWaveParameters>();

	public String get_block_name() 
	{
		return "HOSTILE_WAVE_PARAMETER_SET";
	}

	public void put(ControlObject co, HostileWaveParameters hwp)
	{
		m_items.put(co,hwp);
	}
	
	public HostileWaveParameters get(ControlObject co)
	{
		return m_items.get(co);
	}
	
	public void remove(ControlObject co)
	{
		m_items.remove(co);
	}
	
	public Collection<HostileWaveParameters> items()
	{
		return m_items.values();
	}
	
	/*public Collection<HostileWaveParameters> get_initial_hostiles()
	{
		Collection<HostileWaveParameters> rval = new LinkedList<HostileWaveParameters>();
		
		for (HostileWaveParameters hwp : m_items.values())
		{
			if (hwp.instant_creation)
			{
				rval.add(hwp);
			}
		}
		
		return rval;
	}*/
	
	public void serialize(ParameterParser fw) throws IOException 
	{
		fw.startBlockWrite(get_block_name());
		
		fw.write("nb_items", m_items.size());
		
		for (HostileWaveParameters hwp : m_items.values())
		{
			hwp.serialize(fw);
		}
		
		fw.endBlockWrite();

	}
	public HostileWaveParametersSet()
	{
		
	}
	private void debug(String m)
	{
		if (DebugOptions.debug)
		{
			System.out.println(this.getClass().getName()+": "+m);
		}
	}
	
	public HostileWaveParametersSet(ParameterParser fr, LevelData ld) throws IOException 
	{
		fr.startBlockVerify(get_block_name());
	
		int nb_items = fr.readInteger("nb_items");
		
		for (int i = 0; i < nb_items; i++)
		{
			debug("loading wave parameter #"+(i+1));
			
			HostileWaveParameters hwp = new HostileWaveParameters(fr,ld);
			if (hwp.location != null)
			{
				ControlObject co = hwp.location;
				if (co != null)
				{
					m_items.put(co,hwp);
				}
			}
			
			debug("wave parameter #"+(i+1)+" loaded OK");
		}
		
		fr.endBlockVerify();
	}
}
