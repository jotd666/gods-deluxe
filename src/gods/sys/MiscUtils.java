package gods.sys;

import java.awt.Component;

import javax.swing.JOptionPane;

public class MiscUtils {
	private static final String ZEROS = "0000000000";
	
	public static final String ask_string_value(Component parent, String question, String default_value)
	{
		return JOptionPane.showInputDialog(parent,question,default_value);
	}
	
	public static final boolean show_yes_no_dialog(Component parent, String title, String message)
	{
		int choice = JOptionPane.showConfirmDialog(parent, title ,message, JOptionPane.YES_NO_OPTION);

		return (choice == JOptionPane.YES_OPTION);
	}
	public static final void show_error_dialog(Component parent, String title, String message)
	{
		JOptionPane.showMessageDialog(parent, title, message, JOptionPane.ERROR_MESSAGE);
	}
	// @return MIN_VALUE when illegal
	
	public static final int ask_integer_value(Component parent, String question, int default_value)
	{
		int rval = Integer.MIN_VALUE;
		String r = JOptionPane.showInputDialog(parent,question,""+default_value);
		
		if (r != null)
		{
			try
			{
				rval = Integer.parseInt(r);
			}
			catch (NumberFormatException e)
			{

			}
		}
		
		return rval;
	}
	public static final int get_gray_average_value(int rgb)
	{		
		int red = ((rgb & 0xFF0000) >> 16);
		int green = ((rgb & 0xFF00) >> 8);
		int blue = ((rgb & 0xFF));
		
		int average = (red + green + blue) / 3;
		
		return average;
	}
	public static final String zero_pad(int value, int pad)
	{
		int count = 0;
		int value_copy = value;
		while (value_copy >= 10)
		{
			count++;
			value_copy /= 10;
		}
		int to_pad = pad - count;
		
		return to_pad > 1 ? (ZEROS.substring(0,to_pad-1) + value) : ("" + value);
	}
	
	public static final String capitalize(String s)
	{
		String rval = s;
		
		if (s.length() > 0)
		{
			char c = (char)(s.charAt(0) + 'A' - 'a');
			rval = c + s.substring(1);

			boolean done = false;
			while (!done)
			{
				int i = rval.indexOf('_');
				
				if (i >= 0)
				{
					char lc = s.charAt(i+1);
					char uc = (char)(lc + 'A' - 'a');
					rval = rval.replace("_"+lc, ""+uc);
				}
				else
				{
					done = true;
				}
			}
		}
		
		return rval;
	}
}
