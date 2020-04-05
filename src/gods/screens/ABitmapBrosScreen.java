package gods.screens;

import gods.game.GameState;

public class ABitmapBrosScreen extends WhiteFontScreen {

	public ABitmapBrosScreen()
	{
		super("A BITMAP BROTHERS",4000);
	}
	
	 protected GameState get_next_default_screen()
	 {
		 return new GameScreen();
	 }
	 
}
