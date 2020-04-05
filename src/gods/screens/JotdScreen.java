package gods.screens;

import gods.game.GameState;

public class JotdScreen extends WhiteFontScreen {

	public JotdScreen()
	{
		super("JOTD",4000);	
	}
	
	protected void p_init()
	{
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			
		}
		m_game.load_music("gods.mod");
	}
	 protected GameState get_next_default_screen()
	 {
		 return new PresentsScreen();
	 }
	 
}
