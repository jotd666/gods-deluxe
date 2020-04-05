package gods.screens;

import gods.game.GameState;

public class PresentsScreen extends WhiteFontScreen {

	public PresentsScreen()
	{
		super("PRESENTS",4000);	
	}
	
	
	 protected GameState get_next_default_screen()
	 {
		 return new ABitmapBrosScreen();
	 }
	 
}
