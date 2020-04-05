package gods.game.copperbar;

import gods.game.CopperBar;

	public class Classic2 extends CopperBar {
	
		@Override
		public int[] create(int step) {
	
			int [] rgb_array = new int[64+64+32];
			int counter = 0;
			
			for (int i = 0; i < 64; i+=step)
			{
				for (int j=0;j<step;j++)
				{
				rgb_array[counter++] = (i*238)/64 * (1<<8);
				}
			}
			for (int i = 0; i < 64; i+=step)
			{
				for (int j=0;j<step;j++)
				{
				rgb_array[counter++] = (i*238)/64 * (1<<16) + 238 * (1<<8);
				}
			}
			for (int i = 0; i < 32; i+=step)
			{
				for (int j=0;j<step;j++)
				{
				rgb_array[counter++] = 238 * (1<<16) + 238 * (1<<8) + (i*170)/32;
				}
			}
			
			return rgb_array;
	
		}
	
	}
