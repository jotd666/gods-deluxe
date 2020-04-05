package gods.base;

import gods.sys.ParameterParser;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;


public class ControlObject extends GfxObject implements EditorRenderable, Cloneable
{
	@Override
	public ControlObject clone() throws CloneNotSupportedException 
	{
		ControlObject rval = (ControlObject) super.clone();
		
		rval.m_name = new String(m_name);
		return rval;
	}

		public static boolean can_be_unnamed(Type t)
		{
			return 		t == ControlObject.Type.Foreground_Block || 
			t == ControlObject.Type.Background_Block || t == Type.Reset_Clock_Trigger
			|| t == Type.Restart;
		}
	private Type m_type = Type.Misc;
	private Rectangle m_work_rectangle = new Rectangle();
	
	public static final int OUTLINE_SIZE = 3;
	
	public enum Type { Restart, Teleport_Destination, Bonus, Enemy_Trigger, Enemy, Face_Door, 
		Side_Door, Trap, Message_Trigger, Reset_Clock_Trigger,
		Moving_Block_Start, Moving_Block_End, Foreground_Block, Background_Block, 
		Misc, World_End_Trigger }
	
	public static final Type [] DOORS = {Type.Side_Door, Type.Face_Door, Type.Trap};
	public static final Type [] TELEPORT_DESTS_AND_FACE_DOORS = {Type.Restart, Type.Face_Door, Type.Teleport_Destination };
	public static final Type [] TRIGGERS = {Type.Enemy_Trigger, Type.Bonus, Type.Message_Trigger, Type.World_End_Trigger, Type.Reset_Clock_Trigger};
	public static final Type [] RESTART = {Type.Restart};
	public static final Type [] TELEPORT_DESTS = {Type.Teleport_Destination };
	
	public void editor_render(Graphics g) 
	{
		switch (m_type)
		{
		case Reset_Clock_Trigger:
			g.setColor(Color.PINK);
			break;
		case Message_Trigger:
			g.setColor(Color.LIGHT_GRAY);
			break;
		case Bonus:
			g.setColor(Color.YELLOW);
			break;
		case Enemy_Trigger:
			g.setColor(Color.RED);
			break;
		case Foreground_Block:
			g.setColor(Color.CYAN);
			break;
		case Face_Door:
		case Side_Door:
			g.setColor(Color.WHITE);
			break;
		case Restart:
			g.setColor(Color.GREEN);
			break;
		case Enemy:
			g.setColor(Color.MAGENTA);
			break;
		default:
			g.setColor(Color.BLUE);
		break;
		}
		get_bounds(m_work_rectangle);
		Rectangle r = m_work_rectangle;
		g.drawRect(r.x, r.y, r.width, r.height);
		g.drawRect(r.x+OUTLINE_SIZE, r.y+OUTLINE_SIZE, 
				r.width-2*OUTLINE_SIZE, r.height-2*OUTLINE_SIZE);
		
	}

	public void set_dimension(int w, int h)
	{
		set_width(w);
		set_height(h);
	}
	
	public boolean set_width(int w)
	{
		int old_w = m_width;
		if (w > 0)
		{
			m_width = (w / m_x_resolution) * m_x_resolution;
			if (m_width == 0)
			{
				m_width = m_x_resolution;
			}
		}
		
		return (old_w != m_width);
	}
		
	
	public boolean set_height(int h)
	{
		int old_h = m_height;
		if (h > 0)
		{
			m_height = (h / m_y_resolution) * m_y_resolution;
			if (m_height == 0)
			{
				m_height = m_y_resolution;
			}
		}
		
		return (old_h != m_height);
	}
	
	public void set_type(Type t)
	{
		m_type = t;
	}
	public void set_type(int type_index)
	{
		m_type = Type.values()[type_index];
	}
	public Type get_type()
	{
		return m_type;
	}
	
	public ControlObject(ControlObject copy)
	{
		this(copy.get_x(),copy.get_y(), copy.m_width, copy.m_y_resolution, copy.get_name(), copy.m_type);
		m_height = copy.m_height;
	}
	
	public ControlObject(int x, int y, int x_resolution, int y_resolution, String name, Type tt) 
	{
		super(x, y, 2, y_resolution, name, (GfxFrameSet)null);
		
		// we'll rely on the visible attribute to enable triggers
		
		set_visible(true);
		
		m_width = x_resolution;
		m_type = tt;
		
		if (m_height == 0)
		{
			m_height = 32;
		}


	}
	
	@Override
	public void update(long elapsed_time, LevelData levelData, Rectangle animation_bounds)
	{
		// leave this method as is, to disable GfxObject behaviour (gravity, etc...)
	}
	public String get_class_name()
	{
		return "control object";
	}
	
	public String get_block_name() 
	{
		return "CONTROL_OBJECT";
	}

	public void serialize(ParameterParser fw) throws java.io.IOException
	{
		fw.startBlockWrite(get_block_name());
		
		
		fw.write("name", m_name.equals("") ? UNNAMED_PREFIX : m_name);
		
		fw.write("x", get_x());
		fw.write("y", get_y());				
		fw.write("width",m_width);
		fw.write("height",m_height);
		
		fw.write("type",m_type.toString());
		
		fw.write("activated",m_visible);
		
		fw.endBlockWrite();
	}
	
	public String toString()
	{
		return m_name + " (" + m_type.toString() + ")";
	}
	
	public ControlObject(ParameterParser fr, int x_resolution, int y_resolution, int scale_factor) throws java.io.IOException
	{
		super(2,y_resolution);

		
		fr.startBlockVerify(get_block_name());
		
		m_name = fr.readString("name");
		if (m_name.equals("_unnamed_"))
		{
			m_name = "";
		}
		
		int x = fr.readInteger("x");
		int y = fr.readInteger("y");		
		int w = fr.readInteger("width");
		int h = fr.readInteger("height");
		
		m_type = Type.valueOf(fr.readString("type"));		
		
		m_visible = fr.readBoolean("activated");
		
		
			
			x *= scale_factor;
			y *= scale_factor;
			w *= scale_factor;
			h *= scale_factor;
		
		
		set_coordinates(x, y);
		set_dimension(w, h);
		
		m_appearing_x = x;
		m_appearing_y = y;
		
		fr.endBlockVerify();
	}
}
