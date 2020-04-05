package gods.sys;

public class AngleUtils
{
	private static final int NB_VALUES = 360;
	private static final int HALF_RANGE = NB_VALUES / 2;
	private static final double [] COS = build_cosine_table();
	private static final double [] SIN = build_sine_table();
	
	private static final double to_radians_shift(int i)
	{
		return (i - HALF_RANGE) * Math.PI / 180;
	}
	private static double [] build_cosine_table()
	{
		double [] rval = new double[NB_VALUES];
		
		for (int i = 0; i < NB_VALUES; i++)
		{
			rval[i] = Math.cos(to_radians_shift(i));
		}
		
		return rval;
	}

	private static double [] build_sine_table()
	{
		double [] rval = new double[NB_VALUES];
		
		for (int i = 0; i < NB_VALUES; i++)
		{
			rval[i] = Math.sin(to_radians_shift(i));
		}
		
		return rval;
	}
	
	private static int reframe_angle(int v)
	{
		int rval = (v + HALF_RANGE) % NB_VALUES;
		if (rval < 0)
		{
			rval += NB_VALUES;
		}
		return rval;
	}
	public static final double cosd(int v)
	{
		return COS[reframe_angle(v)];
	}
	
	public static final double sind(int v)
	{
		return SIN[reframe_angle(v)];
	}
	
	public static final int atan2d(double dy, double dx)
	{
		return (int)Math.round(Math.atan2(dy,dx) * 180 / Math.PI);
	}
	
	public static final double angle_difference(double a1, double a2)
	{
		  return normalize_m180_180(a1 - a2);
	}
	
	public static final double average_angle(double a1, double a2)
	{
		double angle = (a1 + a2) / 2;
		return normalize_m180_180(angle);
	}
	
	public static final double oppose(double a)
	{
		return normalize_m180_180(a+180);
	}
	public static final double normalize_m180_180(double a)
	{
		double angle = a;
		
		  while (angle > 180.0) {
			  angle -= 360.0;
		  }
		  while (angle < -180.0) 
		  {
			  angle += 360.0;
		  }
		  
		  return angle;
	}
	
	public static final double normalize_0_360(double a)
	{
		double angle = a;
		
		  while (angle < 0) 
		  {
			  angle += 360.0;
		  }
		  while (angle > 360) 
		  {
			  angle -= 360.0;
		  }
		  
		  return angle;
	}
}
