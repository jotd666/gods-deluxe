package gods.base;


public class BackAndForthPlatform extends MovingBlock 
{


	@Override
	public void update(long elapsed_time) 
	{
		boolean was_moving = m_moving;
		
		super.update(elapsed_time);
		if (was_moving != m_moving)
		{
			reverse();
			m_moving = false; // to trigger the sound
			move();
		}
	}

}
