package gods.base;


import gods.base.ControlObject.Type;
import gods.base.associations.MovingBlockTileAssociation;
import gods.base.associations.ObjectAssociation;
import gods.base.layer.*;
import gods.game.characters.hostiles.SpikeUpDownServer;
import gods.sys.*;

import java.io.IOException;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
	
import java.util.*;
import java.io.*;


public class LevelData implements EditableData, EditorRenderable, Renderable

{
	private String m_copperbar_class;
	private String m_level_class;
	private String m_level_music;
	private String m_boss_music;
	
	private ObjectAssociationSet m_association_set;
	private HashMap<String,MovingBlock> m_moving_blocks = new HashMap<String,MovingBlock>();
	private LinkedList<AnimatedFrames> m_breaking_blocks = new  LinkedList<AnimatedFrames>();
	//private LinkedList<ControlObject> m_added_blocks = new LinkedList<ControlObject>();
	private Renderable m_moving_blocks_renderable = new MovingBlocks();
	private Renderable m_breaking_blocks_renderable = new BreakingBlocks();
	private HostileWaveParametersSet m_hostile_params = new HostileWaveParametersSet();
	protected Rectangle m_view_bounds;
	protected Collection<MovingBlock> m_moving_blocks_values;
	protected SpikeUpDownServer m_spike_server = new SpikeUpDownServer();

	private String m_project_file;
	
	private TileGrid m_tile_grid;
	private GfxObjectLayer m_object_layer;
	private ControlObjectLayer m_control_layer;
	
	private boolean m_modified;

	private GfxFrameSet m_breaking_block_frames;
	
	private int m_hostile_damage_divisor = 1;
	
	private int m_shield_level = 0;
	
	private int m_translated_y = 0;
	private BufferedImage m_copper_image = null;
	private Line2D.Double m_work_line = new Line2D.Double();
	private int m_bonus_index = 1;

	public GfxObject create_bonus(int x, int y, String class_name)
	{
		String name = "_bonus_"+m_bonus_index;
		m_bonus_index++;

		GfxFrameSet frame_set = get_level_palette().lookup_frame_set(class_name);

		GfxObject rval = new GfxObject(x,y,1,get_grid().get_tile_height(),name,frame_set);
		
		add_object(rval);
		
		return rval;
	}

	private void debug(String m)
	{
		if (DebugOptions.debug)
		{
			System.out.println(this.getClass().getName()+": "+m);
		}
	}
	
	private class BreakingBlocks implements Renderable
	{
		public void render(Graphics2D g) 
		{
			for (AnimatedFrames af : m_breaking_blocks)
			{
				af.render(g);
			}
		}
		
		public void update(long elapsed_time) 
		{
			ListIterator<AnimatedFrames> it = m_breaking_blocks.listIterator();
			
			while (it.hasNext())
			{
				AnimatedFrames af = it.next();
				
				af.update(elapsed_time);
				if (af.is_done())
				{
					it.remove();
				}
			}
		}
		
	}
	
	private class MovingBlocks implements Renderable
	{

		public void render(Graphics2D g) 
		{
			for (MovingBlock b : m_moving_blocks_values)
			{
				b.render(g);
			}
			
		}

		public void update(long elapsed_time) 
		{
			for (MovingBlock b : m_moving_blocks_values)
			{
				b.update(elapsed_time);
			}	
		}
		
	}
	
	public void hit_block(int x, int y, int power)
	{
		if (m_tile_grid.hit_block(x, y, power))
		{
			AnimatedFrames af = new AnimatedFrames();
			af.init(m_breaking_block_frames, 100, AnimatedFrames.Type.ONCE);
			af.set_coordinates(m_tile_grid.get_rounded_x(x,false),
					m_tile_grid.get_rounded_y(y,false));
			
			// block has just been destroyed
			m_breaking_blocks.add(af);
		}
	}
	
	public void export(String s)
	{
		BufferedImage bi = m_tile_grid.render(null, null);
		ImageLoadSave.save_png(bi, s);
	}
	
	/*public int get_damage_divisor()
	{
		return 1; // TODO: next series of level on completion
		//return m_hostile_damage_divisor;
	}
	
	public void set_damage_divisor(int d)
	{
		m_hostile_damage_divisor = d;
	}*/
	
	
	public void set_copper_data(int y, BufferedImage copper_image)
	{
		m_translated_y = y;
		m_copper_image = copper_image;
	}
	
	public void editor_render(Graphics g) 
	{
		if (m_control_layer != null)
		{
			m_control_layer.set_hostile_parameters(get_hostile_params(),get_association_set(),get_level_palette());
			
			m_control_layer.editor_render(g);
			
		}
	}

	public static final int SHOW_VISIBLE_OBJECTS = 1;
	public static final int SHOW_HIDDEN_OBJECTS = 1<<1;
	
	private int m_object_view_filter = 0xff;
	private boolean m_tile_overwrite_mode = false;
	
	
	private Rectangle m_work_rectangle = new Rectangle();
	 	
	// prefix = null: consider only unnamed objects
	// prefix != null: consider named objects starting with prefix
	
	public void link_objects_around_boss(GfxObject centre, int distance)
	{
		int square_distance = distance * distance;
		
		for (GfxObject go : m_object_layer.get_items())
		{
			if (!go.is_visible() && (centre != go) && (go.square_distance_to(centre) < square_distance))
			{
				if (go.get_name().startsWith("boss_"))
				{
					centre.link_object(go);
				}
			}
		}
	}
	
	public MovingBlock get_moving_block(String block_start_name)
	{
		return m_moving_blocks.get(block_start_name);
	}
	
	public void add_moving_block(MovingBlock l)
	{
		m_moving_blocks.put(l.get_name(),l);
		
		m_moving_blocks_values = m_moving_blocks.values();
	}

	public void remove_moving_block(MovingBlock l)
	{
		m_moving_blocks.remove(l.get_name());
	}
	
	
	public int get_width()
	{
		return m_tile_grid.get_tile_width() * m_tile_grid.get_nb_cols();
	}
	
	protected int get_screen_width()
	{
		return m_tile_grid.get_tile_width() * 10;
	}

	
	public int get_height()
	{
		return m_tile_grid.get_tile_height() * m_tile_grid.get_nb_rows();
	}
	

	public boolean is_lateral_way_blocked(double px, double py, int h)
	{
		int x = (int)px;
		int y = (int)py;

		boolean rval = m_tile_grid.is_lateral_way_blocked(x,y,h);
				
		return rval || line_test(x, y, h, true, m_moving_blocks_values);

	}
	
	public boolean is_vertical_way_blocked(double px, double py, int w)
	{
		int x = (int)px;
		int y = (int)py;
		
		boolean rval = m_tile_grid.is_vertical_way_blocked(x,y);
		
		return rval || line_test(x, y, w, false, m_moving_blocks_values);
	}

	private boolean line_test(int x, int y, int len, boolean vertical, Collection<? extends NamedLocatable> c)
	{
		boolean rval = false;
		
		if (vertical)
		{
			m_work_line.y1 = y;
			m_work_line.y2 = m_tile_grid.get_rounded_y(y+len, false);
			m_work_line.x1 = x;
			m_work_line.x2 = x;
			
			for (NamedLocatable l : c)
			{
				l.get_bounds(m_work_rectangle,0,-1);

				if (m_work_line.intersects(m_work_rectangle))
				{
					rval = true;
					break;
				}
			}
		}
		else
		{
			m_work_line.x1 = x;
			m_work_line.x2 = m_tile_grid.get_rounded_x(x+len, false);
			m_work_line.y1 = y;
			m_work_line.y2 = y;

			for (NamedLocatable l : c)
			{

				l.get_bounds(m_work_rectangle,-1,0);

				//if (m_work_rectangle.contains(x,y) || m_work_rectangle.contains(x2,y))
				
				if (m_work_line.intersects(m_work_rectangle))
				{
					rval = true;
					break;
				}
			}			
		}
		
		return rval;
	}

	public TileGrid get_grid()
	{
		return m_tile_grid;
	}
	
	public void add_control_object_filter(ControlObject.Type t)
	{
		m_control_layer.add_filter(t);
	}
	
	public void remove_control_object_filter(ControlObject.Type t)
	{
		m_control_layer.remove_filter(t);
	}
	
	public void set_object_view_filter(int vf)
	{
		m_object_view_filter = vf;
	}
	
	public void bitset_object_view_filter(int m)
	{
		m_object_view_filter |= m;
	}
	public void bitclr_object_view_filter(int m)
	{
		m_object_view_filter &= ~m;
	}
	
	public int get_object_view_filter()
	{
		return m_object_view_filter;
	}
	
	
	public ControlObjectLayer get_control_layer()
	{
		return m_control_layer;
	}
	
	public GfxObjectLayer get_object_layer()
	{
		return m_object_layer;
	}

	public void add_object(GfxObject go)
	{

		get_object_layer().get_items().add(go);

	}
	public boolean remove_object(GfxObject go)
	{
		go.set_visible(false);
		return get_object_layer().get_items().remove(go);
	}
	
	public void drop_object(GfxObject go)
	{
		go.set_visible(true);
		add_object(go);
	}
	
	public void init(String name, String common_tile_file, String level_tile_file, EditableData.GfxMode gfx_mode) throws IOException
	{
		new_project(name);	
		debug("loading common tiles");
		m_common.load(common_tile_file,gfx_mode);
		debug("loading level tiles");
		m_level.load(level_tile_file,gfx_mode);
		compute_tile_size();
		/*debug("loading hero tiles");
		m_hero.load("hero",gfx_mode);*/
	}
	
	
	private boolean compute_tile_size()
	{
		Dimension d = m_level.get_tile_dimension();
		
		if (d != null)
		{
			int w = (int)d.width;
			int h = (int)d.height;
			
			m_tile_grid = new TileGrid(w,h,get_level_palette());		
			m_object_layer = new GfxObjectLayer(h);
			m_control_layer = new ControlObjectLayer(w,h/2);
			
		}
		return (d != null);
	}
	
	public LevelData()
	{
		reset();
	}
	
	public GfxPalette get_level_palette()
	{
		return m_level;
	}
	public GfxPalette get_common_palette()
	{
		return m_common;
	}
	public GfxPalette get_hero_palette()
	{
		return m_hero;
	}
	

	
	public void update(long elapsed_time)
	{
		// object layer is updated independently
		//m_object_layer.update(elapsed_time);
		
		m_moving_blocks_renderable.update(elapsed_time);
		
		m_breaking_blocks_renderable.update(elapsed_time);
		
		m_spike_server.update(elapsed_time);
	}
	
	public BufferedImage render_tiles(BufferedImage img, Rectangle bounds)
	{
		return m_tile_grid.render(img, bounds);
	}
	
	public BufferedImage render_all_layers(BufferedImage img, Rectangle bounds)
	{
		BufferedImage bi = render_tiles(img, bounds);

		boolean show_visible = ((m_object_view_filter & SHOW_VISIBLE_OBJECTS) == SHOW_VISIBLE_OBJECTS);
		boolean show_hidden = ((m_object_view_filter & SHOW_HIDDEN_OBJECTS) == SHOW_HIDDEN_OBJECTS);

		m_object_layer.set_view_filter(show_visible,show_hidden);
	
		if (bi != null)
		{
			m_object_layer.render((Graphics2D)bi.getGraphics());
		}		
		
		return bi;
	}
	
	public GfxFrame get_tile(int x, int y)
	{
		return m_tile_grid.get(x, y);
	}
	
	public void set_tile(int x, int y, GfxFrame gf)
	{
		m_tile_grid.set(x, y, gf);
		set_modified(true);
	}
	
	public GfxObject get_bonus(int x, int y)
	{
		return m_object_layer.get(x, y);
	}
	
	public GfxObject get_bonus(String name)
	{
		return m_object_layer.get(name);
	}
	

	public GfxObject set_bonus(int x, int y, String name, GfxFrameSet bonus)
	{
		set_modified(true);
		return m_object_layer.set(x, y, name, bonus);
	}
	
	public ControlObject get_control_object(int x, int y)
	{
		return m_control_layer.get(x,y);
	}
	public ControlObject get_control_object(String name)
	{
		return m_control_layer.get(name);
	}
	
	public ControlObject set_control_object(int x, int y, String name, ControlObject.Type tt)
	{
		set_modified(true);
		
		ControlObject co = m_control_layer.set(x, y, name, tt);
		
		//m_hostile_params.remove(co);
		
		return co;
	}
	public ControlObject add_control_object(int x, int y,
			String name, Type tt) 
	{
		set_modified(true);
		
		return m_control_layer.add(x, y, name, tt);
	}
	
	public String get_level_tile()
	{
		return m_level.get_project_file();
	}
	
	public String get_common_tile()
	{
		return m_common.get_project_file();
	}
	
	private void reset()
	{
		m_level = new GfxPalette();
		m_common = new GfxPalette();
		m_hero = new GfxPalette();
		m_association_set = new ObjectAssociationSet();
		m_modified = false;
		m_tile_overwrite_mode = false;
	}
	
	public String get_project_file() 
	{
		return m_project_file;
	}
	
	public static String get_level_class(String project_file) throws IOException
	{
		LevelData l = new LevelData();
		l.load_start(project_file).close();
		
		return l.get_level_class();		
	}
	// "light" open of the file only to get class name
	
	private ParameterParser load_start(String project_file) throws IOException
	{
		set_project_file(project_file);
		String project_file_path = DirectoryBase.get_levels_path() + m_project_file + DirectoryBase.LEVEL_EXTENSION;
		ParameterParser fr = ParameterParser.open(project_file_path);

		fr.startBlockVerify(get_block_name());

		m_level_class = fr.readString("level_class",true);
		
		m_level_music = fr.readString("level_music",true);

		m_boss_music = fr.readString("boss_music",true);

		return fr;
	}
	
	public void load(String project_file,EditableData.GfxMode gfx_mode) throws IOException 
	{		
		m_tile_overwrite_mode = false;
		
		ParameterParser fr = load_start(project_file);

		debug("loading common tiles");
		
		m_common.load(fr.readString("common_tiles"),gfx_mode);
		
		debug("loading level tiles");
		
		m_level.load(fr.readString("level_tiles"),gfx_mode);
		
		debug("loading hero tiles");
		
		String hero_suffix = GameOptions.instance().get_hero_type().toString().toLowerCase();
		
		m_hero.load("hero_"+hero_suffix,gfx_mode);
		
		m_copperbar_class = fr.readString("copperbar_class",true);
		
		m_hostile_damage_divisor = fr.readInteger("hostile_damage_divisor");

		m_shield_level = fr.readInteger("shield_level");
		
		debug("loading tile grid");
		
		m_tile_grid = new TileGrid(fr,m_level);
		
		
		debug("loading objects");
		
		int game_scale = gfx_mode.equals(EditableData.GfxMode.EDITOR) ? 1 : 2;
		
		m_object_layer = new GfxObjectLayer(fr,m_level,1,this,game_scale);
		
		debug("loading control objects");
		
		m_control_layer = new ControlObjectLayer(fr,m_tile_grid.get_tile_width(),m_tile_grid.get_tile_height()/4,game_scale);
		
		debug("loading associations");
		
		m_association_set = new ObjectAssociationSet(fr,this);
		
		debug("loading hostile parameters");
		m_hostile_params = new HostileWaveParametersSet(fr,this);
		
		debug("all done OK");
		
		fr.endBlockVerify();
		fr.close();
		
		set_modified(false);
		
		if (game_scale > 1)
		{
			// game context
			
			for (ControlObject co : m_control_layer.get_items())
			{
				// convert all tiles under foreground blocks to foreground tiles
				if (co.get_type() == ControlObject.Type.Foreground_Block)
				{
					convert_block_type(co,GfxFrameSet.Type.foreground_tile);
				}
				else if (co.get_type() == ControlObject.Type.Background_Block)
				{
					convert_block_type(co,GfxFrameSet.Type.background_tile);
				}
			}
			
			m_breaking_block_frames = m_level.lookup_frame_set("breakable_block");

		}
		
	}
	
	protected void create_platforms()
	{
		
		ObjectAssociationSet oas = get_association_set();
		
		m_moving_blocks_values = m_moving_blocks.values();

		for (ObjectAssociation oa : oas.items())
		{
			if (oa.get_type() == ObjectAssociation.Type.Moving_Block_Tile)
			{
				MovingBlockTileAssociation mbta = (MovingBlockTileAssociation)oa;
				
				try
				{
					int move_duration = mbta.get_move_time();

					MovingBlock p = null;
					
					if (mbta.is_back_and_forth())
					{
						p = new BackAndForthPlatform();
					}
					else
					{
						p = new MovingBlock();
					}
					p.init(mbta.get_tile(),mbta.get_moving_block().get_name(),
							this,get_view_bounds(),move_duration);
					
					add_moving_block(p);
				}
				catch (Exception e)
				{
					
				}
			}
		}
				
	}
	private void convert_block_type(ControlObject co, GfxFrameSet.Type t)
	{
		int tile_width = m_tile_grid.get_tile_width();
		int tile_height = m_tile_grid.get_tile_height();
		
		for (int i = 0; i < co.get_width(); i += tile_width)
		{
			for (int j = 0; j < co.get_height(); j += tile_height)
			{
				int x = co.get_x() + i;
				int y = co.get_y() + j;
				
				GfxFrame gf = m_tile_grid.get(x, y);
				
				if (gf != null)
				{
					GfxFrameSet gfs_foreground = new GfxFrameSet(gf.get_source_set(),GfxFrame.SymmetryType.no_op_same_ref);

					gfs_foreground.set_type(t);

					m_tile_grid.set(x, y, gfs_foreground.get_first_frame());
				}
			}
		}
	}
	public void new_project(String file) 
	{
		reset();
		set_project_file(file);
		
	}
	
	public Rectangle get_view_bounds()
	{
		return m_view_bounds;
	}
	
	public boolean save() throws IOException 
	{
		boolean rval = false;
		
		String project_file_path = DirectoryBase.get_levels_path() + get_project_file() + DirectoryBase.LEVEL_EXTENSION;
		String backup_file_path = project_file_path + "~";

		// create backup file just in case...
		
		File fb = new File(backup_file_path);
		fb.delete();
		File f = new File(project_file_path);
		f.renameTo(fb);

		ParameterParser fw = ParameterParser.create(project_file_path);
		
		try
		{
			serialize(fw);
			fw.close();
			fw = null;
			
			rval = true;
		}
		catch (RuntimeException r)
		{
			// print the exception
			r.printStackTrace();
			if (fw != null)
			{
				fw.close();
			}
		}
		
		
		set_modified(false);
		
		return rval;
	}
	
	public void set_project_file(String file) 
	{		
		m_project_file = DirectoryBase.rework_path(file,DirectoryBase.get_levels_path(),true,true);
		
	}
	
	public BufferedImage import_from_image(String image_file, int rgb_tolerance,boolean with_margin)
	{
		BufferedImage bi = ImageLoadSave.load(image_file);
		BufferedImage rval = null;
		
		if (bi != null)
		{			
			debug("importing tiles from "+image_file);
			rval = m_tile_grid.import_from_image(bi,m_level,rgb_tolerance,with_margin);			
		}
		return rval;
	}
	
	public boolean is_modified() {
		return m_modified;
	}
	public void set_modified(boolean m) {
		m_modified = m;
	}
	public String get_block_name() {		
		return "GODS_LEVEL";
	}
	public void serialize(ParameterParser fw) throws IOException 
	{
		fw.startBlockWrite(get_block_name());
		fw.write("level_class",m_level_class);
		fw.write("level_music", m_level_music);
		fw.write("boss_music", m_boss_music);
		fw.write("common_tiles",m_common.get_project_file());
		fw.write("level_tiles",m_level.get_project_file());
		fw.write("copperbar_class",m_copperbar_class);
		
		fw.write("hostile_damage_divisor", m_hostile_damage_divisor);
		
		fw.write("shield_level", m_shield_level);
		
		m_tile_grid.serialize(fw);		
		m_object_layer.serialize(fw);
		m_control_layer.serialize(fw);
		m_association_set.serialize(fw);
		m_hostile_params.serialize(fw);
		
		fw.endBlockWrite();
		
		fw.endBlockWrite();		
	}
	
	private GfxPalette m_level = null,m_common = null,m_hero = null;

	
	public void render(Graphics2D g) 
	{
		// draw background
		
		m_tile_grid.render(g, m_view_bounds, m_translated_y, m_copper_image);
		
		// draw moving blocks
		
		m_moving_blocks_renderable.render(g);
		
		// draw breaking blocks
		
		m_breaking_blocks_renderable.render(g);
		
		// draw items
		
		m_object_layer.set_view_filter(true,false);

		//m_object_layer.render(g); done in GodsLevel (priority problems)
	}


	public ObjectAssociationSet get_association_set() 
	{
		return m_association_set;
	}

	public HostileWaveParametersSet get_hostile_params()
	{
		return m_hostile_params;
	}
	
	public boolean add_association(ObjectAssociation o)
	{
		boolean rval = false;

		if (o != null)
		{
			// put ours (add returns null: it worked)
			rval = m_association_set.add(o) == null;
			if (rval)
			{
				set_modified(true);
			}
		}
		
		return rval;
	}
	public void remove_association(ObjectAssociation o)
	{
		m_association_set.remove(o);
		
		set_modified(true);
		
	}

	public String get_copperbar_class() {
		return m_copperbar_class;
	}

	public void set_copperbar_class(String copperbar_class) {
		this.m_copperbar_class = copperbar_class;
	}

	public String get_level_class() 
	{
		return m_level_class;
	}

	public String get_level_music()
	{
		return m_level_music;
	}
	public String get_boss_music()
	{
		return m_boss_music;
	}
	public void set_level_class(String level_class) {
		this.m_level_class = level_class;
	}

	public void set_level_music(String level_music)
	{
		this.m_level_music = level_music;
	}
	public void set_boss_music(String boss_music)
	{
		this.m_boss_music = boss_music;
	}
	// resize
	
	public void set_dimension(int nb_rows, int nb_cols, boolean on_left, boolean on_top)
	{
		int old_nb_rows = m_tile_grid.get_nb_rows();
		int old_nb_cols = m_tile_grid.get_nb_cols();
		
		int left_offset = on_left ? nb_cols - old_nb_cols : 0;
		int top_offset = on_top ? nb_rows - old_nb_rows : 0;
		
		left_offset *= m_tile_grid.get_tile_width();
		top_offset *= m_tile_grid.get_tile_height();
		
		m_object_layer.move_objects(left_offset,top_offset);
		m_control_layer.move_objects(left_offset,top_offset);
				
		m_tile_grid.set_dimension(nb_rows,nb_cols,on_left,on_top,m_tile_grid.get_palette());

		m_object_layer.remove_invisible_objects(get_width(),get_height());
		m_control_layer.remove_invisible_objects(get_width(),get_height());
	}


	public boolean is_tile_overwrite_mode() {
		return m_tile_overwrite_mode;
	}

	public void set_tile_overwrite_mode(boolean tile_overwrite_mode) {
		this.m_tile_overwrite_mode = tile_overwrite_mode;
	}

	
	
	public void notify_x_move_object(GfxObject o, int old_x)
	{
	    // do nothing
	}

	
}
