package gods.sys;

import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class Mp3Play implements Runnable
{
	private static Mp3Play instance()
	{
		if (m_instance == null)
		{
			m_instance = new Mp3Play();
		}
		
		return m_instance;
	}
	
	private static Mp3Play m_instance = null;
	
	 private String m_mp3_name;
	 private Player m_player;
	 private Thread m_play_thread;
	 private boolean m_repeat;
	 
	 public static boolean is_playing()
	 {
		 return instance().is_music_playing();
	 }

	 public static void play(String mp3_name, boolean repeat)
	 {
		 stop();
		 instance().m_mp3_name = mp3_name;
		 instance().play_music(repeat);
	 }
	 public static void play(String mp3_name)
	 {
		 play(mp3_name,true);
	 }
	 public static void replay()
	 {
		 if ((instance().m_mp3_name != null) && (instance().m_repeat))
		 {
			 stop();
			 instance().play_music(true);
		 }
	 }
	 
	 public static void stop()
	 {
		 instance().stop_music();
	 }
	 
	 private void play_music(boolean repeat)
	 {
		 m_play_thread = new Thread(this);
		 m_repeat = repeat;
		 m_play_thread.start();
	 }

	 private boolean is_music_playing()
	 {
		  return m_play_thread != null && m_play_thread.isAlive();
	 }
	 private synchronized void stop_music()
	 {
		 if (m_player != null)
		 {
		 m_player.close();
		 m_player = null;
		 m_play_thread = null;
		 }
	 }

	public void run()
	{
		try (FileInputStream fileStream = new FileInputStream(m_mp3_name);
			 BufferedInputStream bufferedStream = new BufferedInputStream(fileStream))
		{
			m_player = new Player(bufferedStream);
			m_player.play();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}