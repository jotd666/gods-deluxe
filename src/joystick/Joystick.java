package joystick;

/******************************************************************
*
*	Copyright (C) Satoshi Konno 1999
*
*	File : Joystick.java
*
******************************************************************/

public class Joystick {

	public static final int BUTTON1 = 0x0001;
	public static final int BUTTON2 = 0x0002;
	public static final int BUTTON3 = 0x0004;
	public static final int BUTTON4 = 0x0008;
	
	private static boolean dll_available = false;
	private int joyID = 0;
	
	static
	{
		try
		{
			System.loadLibrary("joystick");
			dll_available = true;
			
		}
		catch (UnsatisfiedLinkError e)
		{
			System.err.println("ctor: joystick.dll not found");
		}
	}
	private Joystick(int id) 
	{
		joyID = id;
	}

	public native int getNumDevs();
	public native float getXPos(int id);
	public native float getYPos(int id);
	public native float getZPos(int id);
	public native int getButtons(int id);

	public static final Joystick create(int id)
	{
		Joystick rval = null;
		
		if (dll_available)
		{
			rval = new Joystick(id);

			try
			{
				rval.getXPos();
			}
			catch (UnsatisfiedLinkError e)
			{
				System.err.println("create: joystick.dll not found");
				rval = null;
			}
		}
		return rval;
	}
	public float getXPos() {
		return getXPos(joyID);
	}
	
	public float getYPos() {
		return getYPos(joyID);
	}
	
	public float getZPos() {
		return getZPos(joyID);
	}
	
	public int getButtons() 
	{
		return getButtons(joyID);
	}
	
	public String toString() {
		return "Joystick";
	}
}
