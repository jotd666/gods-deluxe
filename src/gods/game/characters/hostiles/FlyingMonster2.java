package gods.game.characters.hostiles;

import gods.game.characters.FlyingMonster;
import gods.game.characters.HostileParameters;

public class FlyingMonster2 extends FlyingMonster {

	public void init(HostileParameters p)
	{
		super.init(p);
		m_params.nb_move_frames--;
	}
}
