package gods.sys;

import java.awt.image.BufferedImage;
import java.awt.*;

public class AnimatedImage
{
	private BufferedImage [] frames;
	private int x, y;
	private int refresh_rate;
	private int time_counter = 0;
	private int frame_counter = 0;
	private boolean back_and_forth;
	private int increment = 1;
	
	public AnimatedImage(int nb_frames, int refresh_rate, boolean back_and_forth, Rectangle bounds)
	{
		frames = new BufferedImage[nb_frames];
		x = bounds.x;
		y = bounds.y;
		this.back_and_forth = back_and_forth;
		
		this.refresh_rate = refresh_rate;
		
		for (int i = 0; i < frames.length; i++)
		{
			frames[i] = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
		}
	}
	
	public BufferedImage [] get_frames()
	{
		return frames;
	}
	
	public void update(long elapsed_time)
	{
		time_counter += elapsed_time;
		if (time_counter > refresh_rate)
		{
			time_counter -= refresh_rate;
			
			frame_counter+=increment;
			
			if (frame_counter == frames.length)
			{
				if (back_and_forth)
				{
					frame_counter--;
					increment = -1;
				}
				else
				{
					frame_counter = 0;
				}
			}
			else if ((back_and_forth) && (frame_counter < 0))
			{
				frame_counter = 0;
				increment = 1;
			}
		}
	}
	
	public void render(Graphics g)
	{		
		g.drawImage(frames[frame_counter],x,y,null);
		
	}
}
