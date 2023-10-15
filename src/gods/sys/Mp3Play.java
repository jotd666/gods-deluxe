package gods.sys;

import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class Mp3Play implements Runnable
{
	private static final Mp3Play m_instance = new Mp3Play();
	
	private String m_mp3_name;
	private Player m_player;
	private Thread m_play_thread;
	private boolean m_repeat;
	
	public static boolean is_playing()
	{
		return m_instance.is_music_playing();
	}

	public static void play(String mp3_name, boolean repeat)
	{
		stop();
		m_instance.m_mp3_name = mp3_name;
		m_instance.play_music(repeat);
	}

	public static void play(String mp3_name)
	{
		play(mp3_name,true);
	}

	public static void replay()
	{
		if (m_instance.m_mp3_name != null && m_instance.m_repeat)
		{
			stop();
			m_instance.play_music(true);
		}
	}
	
	public static void stop()
	{
		m_instance.stop_music();
	}
	
	private void play_music(boolean repeat)
	{
		m_repeat = repeat;
		m_play_thread = new Thread(this);
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
			System.out.println("Stop playing " + m_mp3_name);
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
			System.out.println("Start playing " + m_mp3_name);
			m_player = new Player(bufferedStream);
			m_player.play();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}