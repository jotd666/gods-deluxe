package gods.sys;

import java.io.*;

import micromod.MicromodPlayer;

public class MusicModule
{
	MicromodPlayer m_player = null;
	
	public void load(String path, boolean loop) throws IOException
	{
		  stop();
		  m_player = new MicromodPlayer(new File(path).toURI().toURL());
		  m_player.setloop(loop);
		  new Thread(m_player).start();		
	}
	
	public boolean is_playing()
	{
		return m_player != null;
	}
	
	public void stop()
	{
	    if (m_player != null)
	    {
	      m_player.stop();
	      m_player = null;
	    }

	}
	 
}
