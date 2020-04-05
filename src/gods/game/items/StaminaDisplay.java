package gods.game.items;

import java.awt.Graphics2D;

import gods.base.*;
import gods.game.characters.LivingCharacter;

public class StaminaDisplay extends NamedLocatable implements Renderable 
{
	public static final int MAX_HEALTH = 24;
	private static final int STAMINA_OFFSET = 12;
	private LivingCharacter m_character;
	private GfxFrame m_empty_stamina;
	private GfxFrame m_current_stamina;
	private GfxFrameSet m_stamina;
	private int m_stamina_timer = 0;
	private int m_stamina_index = 0;
	private int m_scale = 0;
	
	public String get_name() {
	
		return "stamina";
	}
	public boolean is_named()
	{
		return true;
	}
	public void set_name(String n) 
	{
		
		
	}
	
	public StaminaDisplay(GfxPalette palette, LivingCharacter character)
	{
		m_character = character;
		m_stamina = palette.lookup_frame_set(character.get_name()+"_stamina");

		m_empty_stamina = palette.lookup_frame_set("empty_stamina").get_first_frame();
		m_width = m_empty_stamina.toImage().getWidth();
		m_height = m_empty_stamina.toImage().getHeight();
		
		m_current_stamina = m_stamina.get_first_frame();
	}
	
	public void render(Graphics2D g) 
	{
		if (m_scale == 0)
		{
			m_scale = (m_character.get_max_health() / MAX_HEALTH);
		}
		
		int value = m_character.get_health(true) / m_scale;
		
		int h = STAMINA_OFFSET + (MAX_HEALTH - value);

		int x = (int)m_x;
		int y = (int)m_y;
		g.drawImage(m_empty_stamina.toImage(), x, y, 
				x + m_width, y + h, 
				0, 0, m_width, h, null);

		g.drawImage(m_current_stamina.toImage(), x, y + h, 
				x + m_width, y + m_height, 
				0, h, m_width, m_height, null);
		
	}

	public void update(long elapsed_time) 
	{
		m_character.handle_health(elapsed_time);
		
		m_stamina_timer += elapsed_time;
		if (m_stamina_timer > 200)
		{
			m_stamina_timer -= 200;
			m_stamina_index++;
			if (m_stamina_index == m_stamina.get_nb_frames())
			{
				m_stamina_index = 0;
			}
		}
		
		m_current_stamina = m_stamina.get_frame(m_stamina_index + 1);
	}

}
