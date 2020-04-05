package gods.sys;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class UserInputListener implements KeyListener 
{
	public boolean [] key_table = new boolean[0xff];
	public int key_code = KeyEvent.VK_UNDEFINED;
	
	
	public void keyPressed(KeyEvent arg0) 
	{
		key_code = arg0.getKeyCode();
		key_table[key_code & 0xff] = true;
	}

	public void keyReleased(KeyEvent arg0) 
	{
		key_table[arg0.getKeyCode() & 0xff] = false;
		key_code = KeyEvent.VK_UNDEFINED;
	}

	public void keyTyped(KeyEvent arg0) 
	{
		// JFF: not sure it works
		//key_code = arg0.getKeyChar();
	}

}
