package gods.game;


public class HeroJumpBehaviour
{
	static private int [] TABLE;
	final static int JUMP_WIDTH = 170;
	final static int JUMP_HEIGHT = 64;
	private static final double JUMP_RESOLUTION = 1000/168.0; 

	static
	{
		int margin = 10;
		TABLE = new int[JUMP_WIDTH+margin];

		for (int i = 0; i < JUMP_WIDTH;i++)
		{
			int nt = (i-JUMP_WIDTH/2);
			TABLE[i] = (4*JUMP_HEIGHT*nt*nt)/(JUMP_WIDTH*JUMP_WIDTH)-JUMP_HEIGHT;
		}
		for(int i=0;i<margin;i++)
		{
		TABLE[JUMP_WIDTH+i]=TABLE[JUMP_WIDTH-1];
		}
	}
	protected int m_start_x, m_start_y;
	protected double m_index;
	protected boolean m_active;
	protected int m_direction;
	protected int m_elapsed_time;
	private boolean m_giant_jump;
	
	public int get_x(double previous_x)
	{
		return (int)Math.round(m_start_x + m_index * m_direction);

	}
	public int get_x()
	{
		// return a variating x even if zero_dx (if vertical parabol has been ordered
		// that's because there's a wall in front
		
		return m_start_x + ((int)m_index) * m_direction;
	}
	public int get_y()
	{
		int delta = TABLE[(int)m_index];
		if (m_giant_jump)
		{
			delta *= 2;
		}
		return m_start_y+delta;
	}
	
	public void init(int x, int y, int direction,boolean giant_jump) 
	{
		
			m_elapsed_time = 0;
		m_start_y = y;
		m_start_x = x;
		m_index = 0;
		m_direction = direction;
		m_giant_jump = giant_jump;
		m_active = true;
		

	}

	public void update_jump(long elapsed_time) 
	{
		m_elapsed_time += elapsed_time;
		m_index = m_elapsed_time/JUMP_RESOLUTION ;
		
		if (m_index >= JUMP_WIDTH)
		{
			m_active = false;
		}
	}
	
	public boolean is_active()
	{
		return m_active;
	}
	public void set_active(boolean active)
	{
		m_active = active;
	}

}
