package gods.screens;
import java.awt.*;

import gods.game.*;
import gods.base.GameOptions;
import gods.base.GfxPalette;
import gods.base.GameOptions.GfxType;
import gods.sys.*;

public class GodsOptionsMenuScreen2 extends GameState 
{
	private int m_cursor_pos = 0;
	private final static int Y_STEP = 66;	
	private final static int Y_START = 32+Y_STEP;
	private static final int ARROW_OFFSET = 60;

	private String [] MENU_OPTIONS = new String[5];
	private StatusBar m_status_bar;
	private int m_nb_active_options;
	
	private GameOptions m_options = GameOptions.instance();
	
	private static final int OPT_GFXTYPE = 0;
	private static final int OPT_CONTROLS = 2;
	private static final int OPT_LANGUAGE = 1;
	private static final int OPT_TILESET = 3;
	private static final int OPT_HEROTYPE = 4;

	
	private String [] m_languages;
	
	public GodsOptionsMenuScreen2()
	{
		set_fadeinout_time(1000,1000,0);

		m_languages = Localizer.get_available_languages();

		
		
	}
	 private void update_language_option()
	 {
		 String l = m_languages[m_options.get_language()];
		 
		 MENU_OPTIONS[OPT_LANGUAGE] = Localizer.value("LANGUAGE",true)+": "+
		 Localizer.value(l,true).toUpperCase();

		 update_controls_option();
		 update_tileset_option();
		 update_hero_option();
		 update_gfx_option();
	 }
	 
	private void update_controls_option()
	{
		 MENU_OPTIONS[OPT_CONTROLS] = Localizer.value("JUMP",true)+": "+
		 Localizer.value(m_options.get_control_type().toString().replace('_', ' '),true);		
	}
	private void update_tileset_option()
	{
		 MENU_OPTIONS[OPT_TILESET] = Localizer.value("TILESET",true)+": "+
		 Localizer.value(m_options.get_tileset_type().toString().replace('_', ' '),true);		
	}

	private void update_hero_option()
	{
		 MENU_OPTIONS[OPT_HEROTYPE] = Localizer.value("HERO",true)+": "+
		 Localizer.value(m_options.get_hero_type().toString().replace('_', ' '),true);		
	}
	 
	 private void update_gfx_option()
	 {
		 MENU_OPTIONS[OPT_GFXTYPE] = Localizer.value("GFX",true)+": "+
		 Localizer.value(m_options.get_gfx_type().toString().replace('_', ' '),true);
		 		 
		 if (m_options.get_gfx_type() == GfxType.REWORKED)
		 {
			 m_nb_active_options = MENU_OPTIONS.length;
		 }
		 else
		 {
			 m_nb_active_options = OPT_TILESET;
		 }
		
		 // reload fonts as original fonts are supported now
		 
		 init_fonts();
		 
		 m_status_bar.init_font();
		 
		 
	 }
	 
	 protected GameState get_next_default_screen()
	 {
		 return new GodsMainMenuScreen();
	 }
	 

	 protected void p_update()
	 {
		 if (is_a_button_pressed())
		 {
			 fadeout();
		 }
		 
		 m_controls.read();
		 
		 if (m_controls.key_down)
		 {
			 
			 m_cursor_pos++;
			 if (m_cursor_pos == m_nb_active_options)
			 {
				 m_cursor_pos = 0;
			 }
			 
		 }
	
	 
		 
		 if (m_controls.key_up)
		 {

			 if (m_cursor_pos == 0)
			 {
				 m_cursor_pos = m_nb_active_options;
			 }
			 m_cursor_pos--;
		 }
		
		 
		 if (is_fadeout_done())
		 {
			 GameOptions.instance().save();
			 
			 set_next(get_next_default_screen());
		 }
		 
		 //LevelSet ls = m_options.get_current_level_set();
		 
		 switch (m_cursor_pos)
		 {
		 case OPT_GFXTYPE:			 
			 if (m_controls.key_left)
			 {
				 GfxPalette.clear_cache();
				 m_options.prev_gfx_type();
				 update_gfx_option();
			 }
			 else if (m_controls.key_right)
			 {
				 GfxPalette.clear_cache();
				 m_options.next_gfx_type();
				 update_gfx_option();
			 }
			 break;
		 case OPT_CONTROLS:			 
			 if (m_controls.key_left)
			 {				 
				 m_options.prev_control_type();
				 update_controls_option();
			 }
			 else if (m_controls.key_right)
			 {				 
				 m_options.next_control_type();
				 update_controls_option();
			 }
			 break;
		 case OPT_TILESET:			 
			 if (m_controls.key_left)
			 {				 
				 GfxPalette.clear_cache();
				 m_options.prev_tileset_type();
				 update_tileset_option();
			 }
			 else if (m_controls.key_right)
			 {				 
				 GfxPalette.clear_cache();
				 m_options.next_tileset_type();
				 update_tileset_option();
			 }
			 break;
		 case OPT_HEROTYPE:			 
			 if (m_controls.key_left)
			 {				 
				 GfxPalette.clear_cache();
				 m_options.prev_hero_type();
				 update_hero_option();
			 }
			 else if (m_controls.key_right)
			 {				 
				 GfxPalette.clear_cache();
				 m_options.next_hero_type();
				 update_hero_option();
			 }
			 break;
			 
		 case OPT_LANGUAGE:
			 if (m_controls.key_left)
			 {
				m_options.prev_language();
			 }
			 else if (m_controls.key_right)
			 {
				 m_options.next_language();
			 }
			 update_language_option();
			 break;
		 }
		 
		 m_status_bar.update(get_elapsed_time());
	 }
	
	 
	 
	 protected void p_init()
	 {
		 m_status_bar = StatusBar.instance(getWidth(),getHeight());
		 
		 update_language_option();		 

	 }
	 
	 protected void p_render(Graphics2D g)
	 {
		 int h = getHeight();
		 int y_limit = h-m_status_bar.get_height()-4;
		 
		 g.drawImage(m_helmet, 0, 0, getWidth(), y_limit, 
				 0,0, getWidth(), y_limit,null);
		 
		 g.setColor(Color.BLACK);
		 g.fillRect(0, y_limit, getWidth(), h);
		 
		 for (int i = 0; i < m_nb_active_options; i++)
		 {
			 GOLDEN_BIG_FONT.write_line(g,Localizer.value(MENU_OPTIONS[i]),getWidth()/2,
					 Y_START + Y_STEP * i,-2,true,true);
		 }
		 int ypos = Y_START + Y_STEP * m_cursor_pos;
		 
		 GOLDEN_BIG_FONT.write_line(g,")",ARROW_OFFSET,ypos,0,true,true);
		 GOLDEN_BIG_FONT.write_line(g,"(",getWidth()-ARROW_OFFSET,ypos,0,true,true);
		 
		 m_status_bar.render(g);
	 }
}
