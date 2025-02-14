package gods.sys;

import java.util.Date;

public class WavSample extends WavSound
{	
	public WavSample(String file_prefix)
	{
		super(file_prefix);
	}
	
	public void play_offset(int index)
	{	
		set_sample_position(index);
	}
	
	public static void main(String[] args) throws Exception
	{
		int avpro = Runtime.getRuntime().availableProcessors();
		System.out.println("Available Processors : " + avpro);
		System.out.println(new Date());
		
		WavLoop sound = new WavLoop("spike");
		//WavSound sound = new WavSample("starburst");
		sound.play();
		
		for (int i = 0; i < 100; i++) {
			sound.pause();
			Thread.sleep(100);
			if (i % 33 == 0) {
				sound.play();
			}
			Thread.sleep(100);
		}

		//Thread.sleep(3000);
		sound.stop();
		System.out.println(new Date());
		
		SoundService.shutdown();
		System.out.println(new Date());
	}		
}
