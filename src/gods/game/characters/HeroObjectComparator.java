package gods.game.characters;

import gods.base.*;

import java.util.*;

public class HeroObjectComparator implements Comparator<GfxObject> 
{
	private Hero m_hero;
	
	public HeroObjectComparator(Hero h)
	{
	m_hero = h;	
	}
	public int compare(GfxObject arg0, GfxObject arg1) 
	{
		return m_hero.square_distance_to(arg0) - m_hero.square_distance_to(arg1);
	}



}
