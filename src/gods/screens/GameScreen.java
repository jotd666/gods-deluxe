package gods.screens;

import gods.game.GameState;

public class GameScreen extends WhiteFontScreen {

	public GameScreen()
	{
		super("INTRO_GAME",4000);
	}
	
	 protected GameState get_next_default_screen()
	 {
		 return new GodsTitleScreen();
	 }
	 
}
