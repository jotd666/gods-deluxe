package gods.game;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import gods.base.*;
import gods.base.layer.SectorSet;
import gods.game.GodsLevel.ButtonActivationState;
import gods.game.GodsLevel.LeverActivationState;
import gods.game.characters.hostiles.SpikeUpDownServer;
import gods.game.items.*;
import gods.sys.WavLoop;

public class SectoredLevelData extends LevelData 
{

	public void load(String project_file, WavLoop moving_block_sound, Rectangle view_bounds) throws IOException 
	{
	
		GameOptions.GfxType gfx_type = GameOptions.instance().get_gfx_type();
		
		EditableData.GfxMode gfx_mode = EditableData.GfxMode.REWORKED_GAME;
		switch (gfx_type)
		{
		case ORIGINAL:
			gfx_mode = EditableData.GfxMode.ORIGINAL_GAME;
			break;
		case SCALE2X:
			gfx_mode = EditableData.GfxMode.ORIGINAL_GAME_SCALE_2X;
			break;
		case REWORKED:
			break;
		}
		super.load(project_file, gfx_mode);
		
		m_view_bounds = view_bounds;
		
		create_sectors();
	
		create_platforms();

		// add the sound to the platform
		
		for (MovingBlock p : m_moving_blocks_values)
		{
			p.set_moving_sound(moving_block_sound);
		}
		
	}

	public SpikeUpDownServer get_spike_server()
	{
		return m_spike_server;
	}
	@Override
	public void add_object(GfxObject go) 
	{
		// insert it in the correct sector set
		
		if (go.get_source_set().is_bonus())
		{
			bonus_sector_set.add(go);
		}
		else
		{
			items_sector_set.add(go);
		}
		
		super.add_object(go);
	}
	public GfxObject get_visible_bonus_by_type(String name)
	{
		GfxObject rval = null;
		Collection<GfxObject> goc = get_object_layer().get_items();
		
		for (GfxObject go : goc)
		{
			// match if source set name OK, visible and in screen
			
			if (go.is_visible() && go.get_source_set().get_name().equals(name))
			{
				if (m_view_bounds.contains(go.get_x_center(),go.get_y_center()))
				{
					rval = go;
					break;
				}
			}
		}
		return rval;
	}

	@Override
	public boolean remove_object(GfxObject go)
	{
		boolean rval = super.remove_object(go);
		// try to remove from both sector sets
		items_sector_set.remove(go);
		bonus_sector_set.remove(go);
		return rval;
	}

	private int m_nb_sectors = 0;

	public SectorSet<GfxObject> bonus_sector_set;
	public SectorSet<GfxObject> items_sector_set;
	
	// package-visible variables
	SectorSet<GfxObject> activable_sector_set;
	SectorSet<ControlObject> restart_sector_set;
	SectorSet<ControlObject> trigger_sector_set;
	SectorSet<ControlObject> face_door_sector_set;
	HashMap<String,Door> doors_hash_set;
	
	HashMap<GfxObject, LeverActivationState> lever_states = new HashMap<GfxObject, LeverActivationState>();
	HashMap<GfxObject, ButtonState> button_states = new HashMap<GfxObject, ButtonState>();

	public class ButtonState
	{
		ButtonActivationState press_state = ButtonActivationState.NOT_PRESSED;
		boolean activated = false;
	}

	public void notify_x_move_object(GfxObject o, int old_x)
	{
	    bonus_sector_set.move(o, old_x);
	}
	
	private void make_trigger_list()
	{
		trigger_sector_set = new SectorSet<ControlObject>(m_nb_sectors,get_width());

		trigger_sector_set.add(get_control_layer().get_items(ControlObject.TRIGGERS));
		
		restart_sector_set = new SectorSet<ControlObject>(m_nb_sectors,get_width());

		restart_sector_set.add(get_control_layer().get_items(ControlObject.RESTART));
	}
	private void make_face_door_list()
	{
		face_door_sector_set = new SectorSet<ControlObject>(m_nb_sectors,get_width());

		ControlObject.Type [] fd = { ControlObject.Type.Face_Door };
		
		face_door_sector_set.add(get_control_layer().get_items(fd));
	}
	
	private void make_bonus_list()
	{
		bonus_sector_set = new SectorSet<GfxObject>(m_nb_sectors,get_width());

		bonus_sector_set.add(get_object_layer().get_items(GfxFrameSet.BONUSES));
	}
	
	
	private void make_items_list()
	{
		items_sector_set = new SectorSet<GfxObject>(m_nb_sectors,get_width());

		items_sector_set.add(get_object_layer().get_items(GfxFrameSet.MISC_PICKABLE_ITEMS));
	}
	
	private void make_door_list()
	{
		doors_hash_set = new HashMap<String,Door>();
		
		Collection<ControlObject> coc = get_control_layer().get_items(ControlObject.DOORS);
		
		for (ControlObject co : coc)
		{
			switch (co.get_type())
			{
			case Face_Door:
				doors_hash_set.put(co.get_name(),new FaceDoor(co,get_grid()));
				break;
			case Side_Door:
				doors_hash_set.put(co.get_name(),new SideDoor(co,get_grid()));
				break;
			case Trap:
				doors_hash_set.put(co.get_name(),new TrapDoor(co,get_grid()));
				break;
				default:
					break;
			}
		}
	}
	
	private void make_lever_list()
	{
		activable_sector_set = new SectorSet<GfxObject>(m_nb_sectors,get_width());
		
		Collection<GfxObject> activable_list = new LinkedList<GfxObject>();
		
		get_object_layer().get_items(GfxFrameSet.Type.activable, activable_list);
		
		activable_sector_set.add(activable_list);
		
		for (GfxObject go : activable_list)
		{
			if (go.get_class_name().equals("lever"))
			{
				lever_states.put(go, LeverActivationState.INITIAL_DEACTIVATED);
			}
			else
			{
				// secret button
				
				button_states.put(go, new ButtonState());
			}
		}
	}



	private void duplicate_world_end_triggers()
	{
		LinkedList<ControlObject> restarts = new LinkedList<ControlObject>();
		Collection<ControlObject> clitems = get_control_layer().get_items();
		
		// duplicate world end triggers into restart triggers
		// because we have to disable world end triggers when met (to avoid
		// the world end state forever)
		
		for (ControlObject co : clitems)
		{
			if (co.get_type() == ControlObject.Type.World_End_Trigger)
			{
				ControlObject r = new ControlObject(co);
				r.set_type(ControlObject.Type.Restart);
				restarts.add(r);
			}
		}
		
		// add restarts to control layer
		
		clitems.addAll(restarts);
	}
	
	private void create_sectors()
	{
		// number of horizontal sectors is designed for lookup speed
		// we don't want to scan all the level for an item
		// it used to be 40 but on a small level it can cause problems (test levels)
		// since closest() algorithm only looks in the current + closest sectors (+1,-1)
		// so we create as much sectors as there are screens, times 2
		
		m_nb_sectors = (get_width() / get_screen_width()) * 2;
		if (m_nb_sectors == 0)
		{
			m_nb_sectors = 1;
		}
		
		duplicate_world_end_triggers();
		make_lever_list();		
		make_bonus_list();
		make_items_list();
		make_door_list();
		make_trigger_list();
		make_face_door_list();
	}
	

}
