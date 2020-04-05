package gods.game;


public class ParabolicMobileBehaviour
{
	protected int m_start_x, m_start_y;
	protected double m_index, m_index_variation;
	protected int m_x_amplitude;
	private int m_y_amplitude;
	private int m_sq_length;
	protected boolean m_active;
	protected int m_direction;
	private int m_index_offset;
	private boolean m_zero_dx;
	
	public int get_x(double previous_x)
	{
		return (int)Math.round(previous_x + m_index_variation * m_direction);

	}
	public int get_x()
	{
		// return a variating x even if zero_dx (if vertical parabol has been ordered
		// that's because there's a wall in front
		
		return m_start_x + ((int)m_index - m_index_offset) * m_direction;
	}
	public int get_y()
	{
		int rval = m_start_y;
		
		if (m_sq_length != 0)
		{
			int sqx = (((int)m_index - m_index_offset) - m_x_amplitude / 2);
			rval -= (4 * m_y_amplitude * (m_sq_length/ 4 - sqx * sqx)) / m_sq_length;
		}
		return rval;
	}
	
	public void init(int x, int y, int direction, int plength, int height, int min_width, int index_offset) 
	{
		int length = plength;
		
		m_zero_dx = length == 0;
		
		if (m_zero_dx)
		{
			// arbitrary value for length because
			// with the current interface it is not possible
			// to set dx to zero: we fake a non-zero x for y-only parabols
			
			length = min_width;
		}
		
		m_x_amplitude = length;
		m_y_amplitude = height;
		m_sq_length = length * length;	

		m_start_y = y;
		m_start_x = x;
		m_index = index_offset;
		m_index_offset = index_offset;
		m_direction = direction;
		
		m_active = true;
	

	}

	public void update(double index_variation) 
	{
		m_index_variation = index_variation;
		m_index += index_variation;

		if (m_index >= m_x_amplitude)
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
