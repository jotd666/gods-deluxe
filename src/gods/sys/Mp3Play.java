package gods.sys;

import java.io.Closeable;
import java.io.FileInputStream;

import javazoom.jl.player.Player;

public class Mp3Play
{
	static class Mp3Player implements Closeable, Runnable {
		
		private final String fileName;
		private Player player;
		private boolean playing = true;
		private boolean repeatable = true;
		
		public Mp3Player(String mp3_name, boolean mp3_repeat) {
			fileName = mp3_name;
			repeatable = mp3_repeat;
		}

		@Override
		public void run() {
			if (!playing) {
				return;
			}
			System.out.println("Start playing " + fileName);
			try (FileInputStream fileStream = new FileInputStream(fileName)) {
				player = new Player(fileStream);
				player.play();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				playing = false;
			}
		}

		@Override
		public void close() {
			playing = false;
			if (player != null) {
				System.out.println("Stop playing " + fileName);
				player.close();
			}
		}
		
		boolean isPlaying() {
			return playing;
		}
		
		boolean isRepeatable() {
			return repeatable;
		}
	}
	
	private static Mp3Player m_player;
	private static String m_mp3_name;
	
	public static synchronized boolean is_playing()
	{
		return m_player != null && m_player.isPlaying();
	}

	public static synchronized void play(String mp3_name, boolean repeat)
	{
		stop();
		m_mp3_name = mp3_name;
		m_player = new Mp3Player(mp3_name, repeat);
		SoundService.execute(m_player);	
	}

	public synchronized static void play(String mp3_name)
	{
		play(mp3_name, true);
	}

	public synchronized static void replay()
	{
		if (m_player != null && m_player.isRepeatable())
		{
			System.out.println("Replay " + m_mp3_name);
			play(m_mp3_name, true);
		}
	}
	
	public synchronized static void stop()
	{
		if (m_player != null)
		{
			m_player.close();
			m_player = null;
		}
	}
}