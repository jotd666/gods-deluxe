package gods.sys;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import java.awt.Color;

public class ImageLoadSave 
{
	public static BufferedImage load(String image, Color mask_color)
	{
		BufferedImage img = load(image);
		BufferedImage img2 = new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_INT_ARGB);
		
		img2.getGraphics().drawImage(img,0,0,null);
		
		apply_mask(img2,mask_color);
		
		return img2;
	}
	

	public static void replace_color(BufferedImage image, Color original_color, Color replacement_color)
	{
		int mask_rgb = original_color.getRGB();

		for (int x = 0; x < image.getWidth(); x++)
		{
			for (int y = 0; y < image.getHeight(); y++)
			{
				int rgb = image.getRGB(x,y);
				if (rgb == mask_rgb)
				{
					image.setRGB(x, y, replacement_color.getRGB());
				}
			}
		}
	}
	public static void apply_mask(BufferedImage image, Color mask_color)
	{
		replace_color(image, mask_color, new Color(0,0,0,0));
	}
	
	public static BufferedImage load(String image)
	{
		  File f = new File(image);
		  BufferedImage img = null;
		  try
		  {
			  img = ImageIO.read(f);
		  }
		  catch (java.io.IOException e)
		  {
			  System.out.println("Error: "+f+": "+e.getMessage());
		  }
		  return img;
	}
	 public static BufferedImage load_png(String png_image)
	  {
		 return load(png_image+".png");
	  }
	 public static BufferedImage load_png(String png_image, Color mask_color)
	  {
		 return load(png_image+".png",mask_color);
	  }
	 
	 public static void save_png(BufferedImage img,String image_name)
	 {
		  File f = new File(image_name);
		  
		  try
		  {
		 		 ImageIO.write(img,"png",f);
		  }
		  catch (java.io.IOException e)
		  {
			  System.out.println("Error: "+f+": "+e.getMessage());
		  }
		 
	 }
}
