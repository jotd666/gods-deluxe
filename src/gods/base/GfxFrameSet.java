package gods.base;

import java.io.IOException;
import java.util.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import gods.sys.*;

public class GfxFrameSet implements EditorRenderable, Nameable<GfxFrameSet>, Modifiable
{
	public class Properties implements Describable, Cloneable
	{
		public String get_block_name() 
		{
			return "PROPERTIES";
		}
		public void serialize(ParameterParser fw) throws IOException 
		{
			fw.startBlockWrite(get_block_name());
			
			fw.write("value", value);
			fw.write("alias",alias.equals("") ? ParameterParser.UNDEFINED_STRING : alias);
			fw.write("description", description.equals("") ? ParameterParser.UNDEFINED_STRING : description);
			fw.write("animation_type", animation_type.toString());
			fw.write("swap_left_right", swap_left_right);
			fw.endBlockWrite();
			
		}
		public Properties()
		{}
		
		public Properties(ParameterParser fr) throws IOException 
		{
			fr.startBlockVerify(get_block_name());
			value = fr.readInteger("value");
			alias = fr.readString("alias",true);
			description = fr.readString("description",true);
			animation_type = AnimatedFrames.Type.valueOf(fr.readString("animation_type"));
			swap_left_right = fr.readBoolean("swap_left_right");
			fr.endBlockVerify();
			
			game_params();
		}
		
		public Properties clone() throws CloneNotSupportedException
		{
			return (Properties)super.clone();
		}
		
		public String toString()
		{
			return status_bar_value;
		}
		
		private void game_params()
		{
			String al = (alias.equals(ParameterParser.UNDEFINED_STRING) ? get_name().replace('_', ' ') : alias);
			al = Localizer.value(al,true);
			String d = (description.equals(ParameterParser.UNDEFINED_STRING) ? "" : " # " + Localizer.value(description,true));
			
			String v = "";
			
			if (value > 0)
			{
				v += value;

				while (v.length() < 5)
				{
					v = "0" + v;
				}
			
				status_bar_value = "# " + al + d + " # "+Localizer.value("value",true)+" " + v + " #";
			}
			else
			{
				status_bar_value = "# " + al + d + " #";
			}
		}
		
		private String status_bar_value;
		
		public int get_points()
		{
			return value/8;
		}
		public int value = 0;
		public String alias = ParameterParser.UNDEFINED_STRING;
		public String description = ParameterParser.UNDEFINED_STRING;
		public boolean first_appearance = true;
		public AnimatedFrames.Type animation_type = AnimatedFrames.Type.CUSTOM;
		public boolean swap_left_right = false;
	}
	
	int distance_to_zero()
	{
		int rval = 0;
		
		if (!m_frames.isEmpty())
		{
			GfxFrame g = get_first_frame();

			rval =  (g.get_src_x()*g.get_src_x())/64 + (g.get_src_y()*g.get_src_y())*100000;
		}
		
		return rval;
	}
	public int compareTo(GfxFrameSet o)
	{
		return get_name().compareTo(o.get_name());
	}

	private Vector<GfxFrame> m_frames = new Vector<GfxFrame>();
	private String m_name = "";
	private Type m_type = Type.other;
	private boolean m_modified = false;
	private Properties m_properties;
	
	public Properties get_properties()
	{
		return m_properties;
	}
	
	public enum Type { other, still_picture, background_tile, foreground_tile, 
		enemy, hero, weapon, stamina, bonus, pickable, chest, money, teleport_gem, 
		key, ladder, open_face_door, activable, background_item,
		breakable_block }
	

	public static final Type [] TILES = { Type.background_tile, Type.foreground_tile, Type.ladder, 
		Type.background_item, Type.breakable_block };
	public static final Type [] TOKENS = { Type.stamina, Type.chest, Type.money, Type.key, Type.activable, Type.bonus, Type.pickable,Type.teleport_gem, Type.background_item };
	public static final Type [] BONUSES = {Type.stamina, Type.money, Type.bonus ,Type.teleport_gem};
	public static final Type [] MISC_PICKABLE_ITEMS = { Type.chest, Type.key, Type.pickable };
	public static final Type [] ALL_ITEMS = { Type.stamina, Type.chest, Type.money, Type.key, Type.bonus, Type.pickable,Type.teleport_gem };

	public boolean is_bonus()
	{
		boolean rval = false;

		switch (m_type)
		{
		case bonus:
		case money:
		case stamina:
		case teleport_gem:
			rval = true;
			break;
		}

		return rval;
	}
	public void append(GfxFrameSet source)
	{
		m_frames.addAll(source.get_frames());
		m_modified = true;
	}
	
	public boolean is_modified()
	{
		return m_modified;
	}
	
	public void set_modified(boolean m)
	{
		m_modified = m;
	}
	
	public GfxFrameSet()
	{
		m_properties = new Properties();
	}
	
	public GfxFrame get_first_frame()
	{
		return get_frame(1);
	}
	public GfxFrame get_frame(int counter)
	{
		return m_frames.elementAt(counter - 1);
	}
	
	
	public int get_width()
	{
		return toImage().getWidth();
	}
	public int get_height()
	{
		return toImage().getHeight();
	}
	
	public BufferedImage toImage()
	{
		return m_frames.isEmpty() ? null : m_frames.elementAt(0).toImage();
		
	}

	public GfxFrameSet(GfxFrameSet symmetric, GfxFrame.SymmetryType st)
	{
		m_name = symmetric.get_name();
		switch (st)
		{
		case mirror_left:
			m_name = symmetric.get_name().replace("right", "left");
		case no_op_clone:
		case no_op_same_ref:
			m_type = symmetric.get_type();
			
			// symmetrize frames
			
			for (GfxFrame fr : symmetric.get_frames())
			{
				GfxFrame gf = new GfxFrame(fr,st);
				gf.set_source_set(this);
				m_frames.add(gf);
			}
		break;
			
		}
		
		try {
			m_properties = symmetric.m_properties.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}
	
	public GfxFrameSet(BufferedImage source_2x,GfxFrameSet frame_set_1x)
	{
		m_name = frame_set_1x.get_name();
		m_type = frame_set_1x.get_type();
		try {
			m_properties = frame_set_1x.m_properties.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		for (GfxFrame fr : frame_set_1x.get_frames())
		{
			add(new GfxFrame(source_2x,fr));
		}
	}
	
	public GfxFrameSet(String name, Type type)
	{
		m_name = name;
		m_type = type;
		m_properties = new Properties();
	}
	
	public String toString()
	{
		return m_name+" ("+m_type+")";
	}
	public void set_name(String name)
	{
		if (!m_name.equals(name))
		{
			set_modified(true);
		}
		
		m_name = name;
	}
	
	public void set_type(Type i)
	{
		if (i != m_type)
		{
			set_modified(true);
		}
		
		m_type = i;
	}
	
	public Type get_type()
	{
		return m_type;
	}
	
	public GfxFrameSet(BufferedImage source,ParameterParser fr,boolean scale2x, boolean rework_mosaics) throws java.io.IOException
	{
		fr.startBlockVerify(get_block_name());
		
		m_name = fr.readString("name");
		
		String ts = fr.readString("type");
		
		for (Type t : Type.values())
		{
			if (t.name().equals(ts))
			{
				m_type = t;
				break;
			}
		}
		
		int nb_frames = fr.readInteger("nb_frames");
		
		for (int i = 0; i < nb_frames; i++)
		{
			GfxFrame gf = new GfxFrame(this,source,fr,i+1,scale2x,rework_mosaics);
			add(gf);
		}
		
		m_properties = new Properties(fr);
		fr.endBlockVerify();
		
	}
	
	public int get_nb_frames()
	{
		return m_frames.size();		
	}
	
	public Vector<GfxFrame> get_frames()
	{
		return m_frames;
	}
	
	public String get_block_name()
	{
		return "FRAME_SET";
	}
	public void serialize(ParameterParser fw) throws java.io.IOException
	{
		fw.startBlockWrite(get_block_name());

		fw.write("name", m_name);
		
		fw.write("type", m_type.toString());
		
		fw.write("nb_frames",m_frames.size());
		
		for (GfxFrame gf : m_frames)
		{
			gf.serialize(fw);
		}
		
		m_properties.serialize(fw);
		
		fw.endBlockWrite();
		
		set_modified(false);
	}
	
	public String get_name()
	{
		return m_name;
	}
	public boolean is_named()
	{
		return !m_name.equals("");
	}
	
	public void remove(GfxFrame gf)
	{		
		if (gf != null)
		{
			if (m_frames.remove(gf))
			{
				set_modified(true);
				int i = 1;
				for (GfxFrame g : m_frames)
				{
					g.set_counter(i++);
				}
			}
		}
	}
	public void add(GfxFrame gf)
	{
		gf.set_counter(m_frames.size()+1);
		if (m_frames.add(gf))
		{
			set_modified(true);
		}
	}
	

	public void editor_render(Graphics g)
	{
		Color c = Color.BLUE;

		switch (m_type)
		{
		case background_tile:
			c = Color.RED;
		case foreground_tile:
			c = Color.GREEN;
			break;
		case enemy:
			c = Color.ORANGE;
			break;
		case hero:
			c = Color.CYAN;
			break;
		case still_picture:
			c = Color.YELLOW;
			break;
		case weapon:
			c = Color.DARK_GRAY;
			break;
		}
		
		g.setColor(c);
		for (GfxFrame gf : m_frames)
		{
			gf.editor_render(g);
		}
	}
	
	// x & y can be inaccurate
	
	public GfxFrame lookup_frame(int x, int y)
	{
		GfxFrame rval = null;
		
		for (GfxFrame gf : m_frames)
		{
			if (gf.get_bounds().contains(x,y))
			{
				rval = gf;
				break;
			}
		}
		
		return rval;
	}
}
