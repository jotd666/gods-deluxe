package gods.editor.level;


import gods.base.*;
import gods.base.associations.*;
import gods.editor.*;
import gods.sys.ImageLoadSave;
import gods.sys.MiscUtils;

import java.awt.image.BufferedImage;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.*;
import java.io.File;

import java.util.*;
import javax.swing.*;

public class EditLevel extends JPanel  implements MouseListener, KeyListener, MouseMotionListener, Observer
{
	
	private AssociationDialog m_association_dialog = null;
	
	public void set_association_dialog(AssociationDialog ad)
	{
		m_association_dialog = ad;
	}
	
	@Override
	public void keyPressed(KeyEvent arg0) 
	{
		int x = m_mouse_x;
		int y = m_mouse_y;
		
		switch (arg0.getKeyCode())
		{
		case KeyEvent.VK_DELETE:		
		{
			ControlObject trig = m_data.get_control_object(x, y);
			GfxObject gfo = m_data.get_bonus(x, y);
			PopupMenuEditActionType at = PopupMenuEditActionType.Delete_Tile;
			
			if (gfo != null)
			{
				at = PopupMenuEditActionType.Delete_Object;
				
				//m_data.remove_object(gfo)
			}
			else if (trig != null)
			{
				at = PopupMenuEditActionType.Delete_Control_Object;
				
				//m_data.set_control_object(x, y, name, tt)
			}
			else
			{
				//m_data.set_tile(x, y, null);
				//update_map(true,false);
				//repaint();
			}
			new PopupMenuEditActionAdapter(0,0,at).action_performed_at(x,y);
			break;
		}
		case KeyEvent.VK_E:
		{
			new PopupMenuEditActionAdapter(0,0,PopupMenuEditActionType.Edit_Enemy_Properties).action_performed_at(x,y);
			break;
		}
		default:
			break;
		}
		
	}
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void keyTyped(KeyEvent arg0) 
	{
		
	}

	static final long serialVersionUID = 1;
	
	private Rectangle m_work_rectangle = new Rectangle();
	
	/*private class Message extends Observable
	{
		public Message(Observer obs)
		{
			addObserver(obs);
		}
		
		@Override
		public void setChanged() 
		{			
			super.setChanged();
			notifyObservers();
		}
	
	}*/
	
	private HostileWaveParameters m_copied_hw_params = null;
	//private Message m_observable;
	
	private String get_default_new_instance_name(String name)
	{
		int len = name.length();
		int split = 0;
		String rval = name;

		if (!name.equals(""))
		{
			rval += "_1";

			if (len > 0)
			{
				for (int i = len-1; i > 0; i--)
				{
					char c = name.charAt(i);
					if (!Character.isDigit(c))
					{
						split = i;
						break;
					}
				}

				if ((split != 0) && (split < len-1))
				{
					String prefix = name.substring(0,split+1);
					int suffix = Integer.parseInt(name.substring(split+1));

					do
					{
						suffix++;
						rval = prefix + suffix;
					}
					while (m_data.get_control_object(rval) != null ||  m_data.get_bonus(rval) != null);
				}
			}
		}
		return rval;
	}
	public void mouseDragged(MouseEvent arg0) 
	{
		int x = arg0.getX();
		int y = arg0.getY();
		
		if (m_current_object != null)
		{
			boolean move_it = (m_current_object_horiz_side == -1) && (m_current_object_vert_side == -1);
			
			if (!move_it)
			{
				// cast to control object
				
				boolean modified = false;
				
				ControlObject co = (ControlObject)m_current_object;
								
				int new_height;
				int old_y, new_y;
				int old_x, new_x;
				
				switch (m_current_object_horiz_side)
				{
				case 0:
					old_y = co.get_y();
					if (co.set_y(y))
					{
						modified = true;
					}
					new_y = co.get_y();
					
					new_height = co.get_height() - new_y + old_y;
					
					if (co.set_height(new_height))
					{
						modified = true;
					}

					break;
				case 1:
					new_height = y - co.get_y();
					
					if (co.set_height(new_height))
					{
						modified = true;
					}
					break;
				}
				switch (m_current_object_vert_side)
				{
				case 0:
					old_x = co.get_x();
					
					if (co.set_x(x))
					{
						modified = true;
					}
					new_x = co.get_x();
									
					if (co.set_width(co.get_width() - new_x + old_x))
					{
						modified = true;
					}
					
					break;
				case 1:
					if (co.set_width(x - co.get_x()));
					{
						modified = true;
					}
					break;
				}
				if (modified)
				{
					update_map(true,false);			
					repaint();
				}
			}
			else
			{
				if (m_current_object.set_coordinates(x,y))
				{
					update_map(true,false);			
					repaint();
				}
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) 
	{	
		m_mouse_x = arg0.getPoint().x;
		m_mouse_y = arg0.getPoint().y;
	}

	private static int NB_RECENT_FRAMES = 5;
	
	private int m_mouse_x, m_mouse_y;
	
	private Frame m_parent;
	private LevelData m_data;
	private BufferedImage m_image = null;
	
	private GfxObject m_current_object = null;
	private int m_current_object_horiz_side = -1;
	private int m_current_object_vert_side = -1;
	private HostileWaveParameters m_last_edited_hostile_wave_params = null;
	
	@SuppressWarnings("rawtypes")
	private class RecentAndCopy<T extends Nameable, U>
	{
		public Vector<T> recent = new Vector<T>();
		public U copied = null;
		

		public void add_to_recent_frames(T frame_set)
		{
			boolean already_there = false;
			
			for (T gfs : recent)
			{
				if (gfs.get_name().equals(frame_set.get_name()))
				{
					already_there = true;
					break;
				}
			}

			if (!already_there)
			{
				int sz = recent.size();
				if (sz < NB_RECENT_FRAMES)
				{
					recent.add(frame_set);
				}
				else
				{
					for (int i = 0; i < sz-1; i++)
					{
						recent.setElementAt(recent.elementAt(i+1),i);
					}

					recent.setElementAt(frame_set, sz-1);
				}
			}
		}
	}
	
	private RecentAndCopy<GfxFrameSet, GfxFrame> m_tile = new RecentAndCopy<GfxFrameSet, GfxFrame>();
	private RecentAndCopy<GfxFrameSet, GfxObject> m_object = new RecentAndCopy<GfxFrameSet, GfxObject>();
	
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	private JMenuItem create_popup_menu_item(JPopupMenu popup, int x, int y, PopupMenuEditActionType at)
	{
		return create_popup_menu_item(popup,at.name(),new PopupMenuEditActionAdapter(x,y,at));						
	}
	
	private JMenuItem create_popup_menu_item(JPopupMenu popup,String name,ActionListener aa)
	{
		JMenuItem menu_item = new JMenuItem(name.replace('_', ' '));
		menu_item.addActionListener(aa);
		popup.add(menu_item);
		return menu_item;
	}
	
	
	private JMenu create_popup_sub_menu(JMenu popup, String name)
	{
		JMenu rval = new JMenu(name.replace('_', ' '));
		
		popup.add(rval);
		
		return rval;
	}
	private JMenu create_popup_sub_menu(JPopupMenu popup, String name)
	{
		JMenu rval = new JMenu(name.replace('_', ' '));
		
		popup.add(rval);
		
		return rval;
	}
	
	private JMenuItem create_menu_item(GfxFrameSet i)
	{
		JMenuItem mi = null;
		if (i.toImage() == null)
		{
			mi = new JMenuItem(i.get_name());
		}
		else
		{
			ImageIcon ii = new ImageIcon(i.toImage(),i.get_name());
			mi = new JMenuItem(i.get_name(),ii);
		}
		
		return mi;
	}


	private JMenu create_add_tile_sub_menu(JPopupMenu popup, int x, int y)
	{
		return create_add_item_sub_menu(popup, "Create Tile", x, y, GfxFrameSet.TILES, 
				new PopupMenuEditActionAdapter(x,y,PopupMenuEditActionType.New_Tile));
	}
	
	private JMenu create_add_bonus_sub_menu(JPopupMenu popup, int x, int y)
	{
		return create_add_item_sub_menu(popup, "Create Token", x, y, GfxFrameSet.TOKENS, 
				new PopupMenuEditActionAdapter(x,y,PopupMenuEditActionType.New_Object));
	}

	
	
	private boolean check_duplicate(GfxObject go, String name)
	{
		boolean duplicate_name = false;

		if (!name.equals(""))
		{
			String object_type = null;
			
			if (go instanceof ControlObject)
			{
				duplicate_name = (m_data.get_control_object(name) != null);
				object_type = "Control object";
			}
			else
			{
				duplicate_name = (m_data.get_bonus(name) != null);
				object_type = "Object";
			}	


			if (duplicate_name)
			{
				error_message("Duplicate instance error", object_type+" "+name+" already exists");
			}	
		}

		return duplicate_name;
	}
	public JMenu create_add_item_sub_menu(JPopupMenu popup, String title,
			int x, int y, GfxFrameSet.Type [] types,PopupMenuEditActionAdapter action_listener_model)
	{		
		JMenu m = create_popup_sub_menu(popup,title);

		for (GfxFrameSet.Type t : types)
		{
			Collection<GfxFrameSet> gfsl = m_data.get_level_palette().lookup_by_type(t);

			if (!gfsl.isEmpty())
			{
				JMenu mt = create_popup_sub_menu(m,t.name());

				int split_size = 40;

				JMenuItem mi = null;

				if (gfsl.size() > split_size)
				{
					// split menu
					int counter = 0;

					JMenu mts = null;

					for (GfxFrameSet i : gfsl)
					{
						if (counter % split_size == 0)
						{
							mts = create_popup_sub_menu(mt,""+counter/split_size);
						}

						counter++;

						mi = create_menu_item(i);
						ActionListener action_listener = action_listener_model.clone_with_user_data(i,null);
						mi.addActionListener(action_listener);
						mts.add(mi);
					}
				}
				else
				{
					for (GfxFrameSet i : gfsl)
					{
						mi = create_menu_item(i);

						ActionListener action_listener = action_listener_model.clone_with_user_data(i,null);
						mi.addActionListener(action_listener);
						mt.add(mi);
					}
				}
			}
		}
		return m;
	}
	
	
	private void error_message(String title, String message)
	{
	MiscUtils.show_error_dialog(m_parent, message, title);
	}

	
	
	public enum PopupMenuEditActionType { New_Tile, New_Tile_Set, Delete_Tile, Copy_Tile, 
		New_Object,Delete_Object, Edit_Object_Properties, 
		Edit_Enemy_Properties, 
		Copy_Enemy_Properties, 
		Paste_Enemy_Properties,
		Paste_Enemy,
		Edit_Enemy_Trajectory,
		Copy_Object,  New_Control_Object, 
		Edit_Control_Object, Delete_Control_Object, 
		Copy_Control_Object, Paste_Object, Derive_Associated_Trigger }
	
	
	private class PopupMenuEditActionAdapter implements ActionListener
	{
		private PopupMenuEditActionType m_action_type;
		private int m_x,m_y;
		private Object m_user_data_1 = null;
		
		public void set_user_data_1(Object ud1)
		{
			m_user_data_1 = ud1;
		}
		
		public PopupMenuEditActionAdapter clone_with_user_data(Object user_data_1, Object user_data_2)
		{
			PopupMenuEditActionAdapter rval = new PopupMenuEditActionAdapter(m_x,m_y,m_action_type);
			rval.m_user_data_1 = user_data_1;
			//rval.m_user_data_2 = user_data_2;
			
			return rval;
		}

		public PopupMenuEditActionAdapter(GfxFrameSet gfs, int x, int y,PopupMenuEditActionType at)
		{			
			this(x,y,at);
			m_user_data_1 = gfs;
		}
		public PopupMenuEditActionAdapter(int x, int y,PopupMenuEditActionType at)
		{			
			m_x = x;
			m_y = y;
			m_action_type = at;
		}
		
		private void edit_enemy(ControlObject co)
		{
			HostileWaveParametersSet hwps = m_data.get_hostile_params();
			HostileWaveParameters hwp = hwps.get(co);
			
			if (hwp == null)
			{
				if (m_last_edited_hostile_wave_params != null)
				{
					try {
						hwp = m_last_edited_hostile_wave_params.clone();
					} catch (CloneNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				if (hwp == null)
				{
					hwp = new HostileWaveParameters(co,m_data);
				}
				else
				{
					hwp.location = co;
				}
				
				hwps.put(co,hwp);
				m_data.set_modified(true);
			}
			else
			{
				m_last_edited_hostile_wave_params = null;
			}
			
			HostileWaveDialog hwd = new HostileWaveDialog(m_parent,hwp,EditLevel.this);
			
			if (m_last_edited_hostile_wave_params != null)
			{
				hwd.setTitle(hwd.getTitle()+" (defaulted from "+m_last_edited_hostile_wave_params.location+")");
			}
				
			hwd.setLocationRelativeTo(m_parent);
			
			m_last_edited_hostile_wave_params = hwd.get_data();
			
			
			hwd.setVisible(true);
		}
		private LinkedList<ControlObject> get_enemy_list()
		{
			LinkedList<ControlObject> col = new LinkedList<ControlObject>();
			ControlObject co = m_data.get_control_object(m_x,m_y);
			if (co!=null)
			{
			switch (co.get_type())
			{
			case Enemy:
			{
				col.add(co);
			}
			break;
			case Enemy_Trigger:
				TriggerMonsterAssociation ta = (TriggerMonsterAssociation)m_data.get_association_set().get(co.get_name());

				for (int i = 0; i < ta.get_nb_monsters(); i++)
				{
					col.add(ta.get_monster(i));
				}
				break;
			default:
				break;
			}
			}
			
			return col;
		}
		
		private void set_tile_set(GfxFrameSet gfs)
		{
			for (int i = 0; i < gfs.get_nb_frames(); i++)
			{
				m_data.set_tile(m_x + i * m_data.get_grid().get_tile_width(), 
						m_y, gfs.get_frame(i+1));
			}
		}
		public void action_performed_at(int x, int y)
		{
			m_x = x;
			m_y = y;
			actionPerformed(null);
		}
		
		public void actionPerformed(ActionEvent e_unused) 
		{
			String name;
			GfxObject go;
			GfxFrameSet gfs;
			ControlObject co;
			
			switch (m_action_type)
			{
			case Delete_Tile:
				m_data.set_tile(m_x, m_y, null);
				update_map(true,false);
				repaint();
				break;
			case Copy_Tile:
				m_tile.copied = m_data.get_tile(m_x, m_y);
				break;
			case Delete_Object:
				go = m_data.get_bonus(m_x, m_y);
				
				boolean confirm = true;
				
				if (m_data.get_association_set().contains(go))
				{
					confirm = MiscUtils.show_yes_no_dialog(m_parent,
							"Object "+go.get_name()+" belongs to at least an association.\n"+
							"Deleting it will delete the association. Confirm?","Delete object");

					if (confirm)
					{
						m_data.get_association_set().remove_associations_containing(go);
					}
				}
				if (confirm)
				{
					m_data.set_bonus(m_x,m_y,"",null);
					update_map(true,false);
					repaint();
				}
				break;
			case Delete_Control_Object:
				co = m_data.get_control_object(m_x,m_y);
				
				// check if the control object is within an association
				
				confirm = true;
				
				if (m_data.get_association_set().contains(co))
				{
					confirm = MiscUtils.show_yes_no_dialog(m_parent,
							"Control object "+co.get_name()+" belongs to at least an association.\n"+
							"Deleting it will delete the association. Confirm?","Delete object");
					
					if (confirm)
					{
						m_data.get_association_set().remove_associations_containing(co);
					}
				}
				if (confirm)
				{
					if (co.get_type() == ControlObject.Type.Enemy)
					{
						// delete enemy properties to avoid leftovers
						m_data.get_hostile_params().remove(co);
					}
					m_data.set_control_object(m_x,m_y,null,null);
					update_map(true,false);
					repaint();
				}
				break;
			case Derive_Associated_Trigger:
				co = m_data.get_control_object(m_x,m_y);
				name = co.get_name();
				
				if (co.get_type() == ControlObject.Type.Enemy)
				{
					int liu = name.lastIndexOf('_');
					
					String new_name = null;
					if (liu != -1)
					{
						new_name = name.substring(0,liu) + 't' + name.substring(liu,name.length());
					}
					else
					{
						new_name = name + "_trigger";
					}
					
					int idx = 1;
					
					String new_name_to_try = "" + new_name;
					
					while (true)
					{
						if (m_data.get_control_object(new_name_to_try) == null) break;
						
						new_name_to_try = new_name + "_" + idx;
						
						idx++;
					}
					
					// bugfix: no longer remove closest control object
					
					ControlObject trigger = m_data.add_control_object(m_x + m_data.get_grid().get_tile_width(),
							m_y + m_data.get_grid().get_tile_height(), new_name_to_try, ControlObject.Type.Enemy_Trigger);
					
					TriggerMonsterAssociation tma = new TriggerMonsterAssociation();
					tma.set_object(0, trigger);
					tma.set_object(1, co);
					m_data.add_association(tma);
					update_map(true,false);
					repaint();
				}
				break;
			case Edit_Enemy_Trajectory:
			{
				Collection<ControlObject> col = get_enemy_list();
				for (ControlObject c : col)
				{
					HostileWaveParameters hwp = m_data.get_hostile_params().get(c);
					
					if (hwp != null)
					{
						HostileTrajectory ht = hwp.get_trajectory();
						HostileTrajectoryDialog htd = new HostileTrajectoryDialog(m_parent,ht,m_data,c.get_name());

						htd.setLocationRelativeTo(m_parent);
						htd.setVisible(true);
					}
					else
					{
						error_message("Trajectory error","Hostile parameters not set yet");						
					}
				}
				
				break;
			}
			case Paste_Enemy:
			{	
				
				name = JOptionPane.showInputDialog(m_parent, "Instance name for hostile",
						get_default_new_instance_name(m_copied_hw_params.location.get_name()));
				
				if (name != null)
				{	
					if (name.equals(""))
					{
						error_message("Instance naming error","Hostile object should have a name");											
					}
					else
					{
						if (m_data.get_control_object(name) == null)
						{	
							// first, create control object
							co = m_data.set_control_object(m_x, m_y, name, ControlObject.Type.Enemy);

							// copy dimensions of the original
							
							ControlObject comodel =m_copied_hw_params.location;
							
							co.set_width(comodel.get_width());
							co.set_height(comodel.get_height());
							
							// then, set new hostile params in the list
							
							m_copied_hw_params.location = co;
							
							m_data.get_hostile_params().put(co,m_copied_hw_params);
							
							repaint();
						}
						else
						{
							error_message("Duplicate instance error","Control object "+name+" already exists");											
						}
					}
				}
				
				
			}
			break;
			case Paste_Enemy_Properties:
			{
				HostileWaveParametersSet hwps = m_data.get_hostile_params();
				
				LinkedList<ControlObject> col = get_enemy_list();
				if (col.size() == 1)
				{
					co = col.getFirst();
				
					m_copied_hw_params.location = co;
					
					hwps.put(co,m_copied_hw_params);
				}
				
				m_copied_hw_params = null;

				break;
			}
			case Copy_Enemy_Properties:
			{
				LinkedList<ControlObject> col = get_enemy_list();
				HostileWaveParametersSet hwps = m_data.get_hostile_params();
				HostileWaveParameters hw = null;
				
				if (col.size() == 1)
				{
					co = col.getFirst();
				
					hw = hwps.get(co);
				}
				
				try 
				{
					m_copied_hw_params = null;
					if (hw != null)
					{
						m_copied_hw_params = hw.clone();	
					}
				} catch (CloneNotSupportedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
				
		
				break;
			case Edit_Enemy_Properties:
			{
				Collection<ControlObject> col = get_enemy_list();
				for (ControlObject c : col)
				{
					edit_enemy(c);
				}
			
			}
				break;
			case Edit_Object_Properties:
				go = m_data.get_bonus(m_x,m_y);
				
				GfxObjectDialog god = new GfxObjectDialog(m_parent,go,m_data.get_object_layer(),EditLevel.this);
				god.setLocationRelativeTo(m_parent);
				god.setVisible(true);
				
				break;
			/*case Edit_Object_Name:
				go = m_data.get_bonus(m_x, m_y);
				name = JOptionPane.showInputDialog(m_parent, 
						"New instance name for "+go.toString(),go.get_name());
				
				if ((name != null) && (!name.equals(go.get_name())))
				{
					if (!check_duplicate(go, name))
					{				
						go.set_name(name);
						m_data.set_modified(true);						
					}
				}
				break;*/
			case Copy_Object:
				m_object.copied = m_data.get_bonus(m_x, m_y);
				break;
			case New_Tile:
				gfs = (GfxFrameSet)m_user_data_1;
				
				set_tile_set(gfs);
				
				m_tile.add_to_recent_frames(gfs);
				update_map(true,false);
				repaint();
				
				break;
			case New_Tile_Set:
				gfs = (GfxFrameSet)m_user_data_1;
				
				GfxFrame gf = gfs.get_first_frame();

				int xf, yf = m_y;
				int nb_rows = MiscUtils.ask_integer_value(m_parent,"# of rows",1);
				int nb_cols = MiscUtils.ask_integer_value(m_parent,"# of cols",1);
				
				if ((nb_rows > 0) && (nb_rows > 0))
				{
					for (int i = 0; i < nb_rows; i++)
					{
						xf = m_x;
						for (int j = 0; j < nb_cols; j++)
						{
							m_data.set_tile(xf, yf, gf);

							xf += gf.get_width();
						}

						yf += gf.get_height();
					}

					m_tile.add_to_recent_frames(gfs);
					update_map(true,false);
					repaint();
				}
				break;
			case New_Object:
				gfs = (GfxFrameSet)m_user_data_1;
				
				if (gfs.get_type() == GfxFrameSet.Type.background_item)
				{
					name = "";
				}
				else
				{
					name = JOptionPane.showInputDialog(m_parent, "Instance name for "+gfs.toString());
				}
				if (name != null)
				{					
					if (name.equals("") || (m_data.get_bonus(name) == null))
					{						
						go = m_data.set_bonus(m_x,m_y,name,gfs);
						/*if (gfs.get_type() != GfxFrameSet.Type.background_item)
						{
							choice = JOptionPane.showConfirmDialog(m_parent, "Should the object be shown ?", 
									"Properties for "+name,JOptionPane.YES_NO_OPTION);

							go.set_visible(choice == JOptionPane.YES_OPTION);
						}*/

						m_object.add_to_recent_frames(gfs);
						update_map(true,false);
						repaint();
					}
					else
					{
						error_message("Duplicate instance error", "Object "+name+" already exists");
					}
				}
				break;
			case Paste_Object:
				go = m_object.copied;
				
				if (go.get_name().equals(""))
				{
					name = "";
				}
				else
				{
					name = JOptionPane.showInputDialog(m_parent, "Instance name for copy of "+go.toString(),
							get_default_new_instance_name(go.get_name()));
				}
				if (name != null)
				{		
					
					if (!check_duplicate(go,name))
					{
						if (go instanceof ControlObject) 
						{
							if (!name.equals("") || 
									ControlObject.can_be_unnamed(((ControlObject) go).get_type()))
							{
								co = (ControlObject) go;
								ControlObject nco = m_data.set_control_object(m_x, m_y, name, co.get_type());
								
								nco.set_visible(go.is_visible());
								
								nco.set_dimension(co.get_width(), co.get_height());
							}
							else
							{
								error_message("Instance naming error","Control object should have a name");											
							}
						}
						else
						{
							GfxObject ngo = m_data.set_bonus(m_x, m_y, name, go.get_source_set());
							ngo.set_visible(go.is_visible());
						}
						update_map(true,false);
						repaint();
					}
				}
				break;
			case Copy_Control_Object:
				m_object.copied = m_data.get_control_object(m_x,m_y);
				break;
			case Edit_Control_Object:
				co = m_data.get_control_object(m_x,m_y);
				
				ControlObjectDialog cod = new ControlObjectDialog(m_parent, co, m_data.get_control_layer(), EditLevel.this);
				cod.setLocationRelativeTo(m_parent);
				cod.setVisible(true);
				break;
			case New_Control_Object:
				ControlObject.Type tt = (ControlObject.Type)m_user_data_1;
				
				name = JOptionPane.showInputDialog(m_parent, "Instance name for "+tt.toString());
				if (name != null)
				{	
					if (name.equals("") && !ControlObject.can_be_unnamed(tt))
					{
						error_message("Instance naming error","Control object should have a name");											
					}
					else
					{
						if (m_data.get_control_object(name) == null)
						{
							LinkedList<ControlObject> lco = new LinkedList<ControlObject>();
							
							m_data.get_control_layer().get_items(tt, lco);
							
							// create the control object
							co = m_data.set_control_object(m_x, m_y, name, tt);
											
							// set preferred size if possible, from previously created objects of
							// the same type (ok, if sizes are different it will not work, but
							// statistically it will)
							
							if (!lco.isEmpty())
							{
								ControlObject model = lco.getFirst();
								co.set_width(model.get_width());
								co.set_height(model.get_height());
							}

							repaint();
						}
						else
						{
							error_message("Duplicate instance error","Control object "+name+" already exists");											
						}
					}
				}
				break;
			}
			
		}
	}
	
	private void handle_button_1_click(int x, int y)
	{
		m_current_object_horiz_side = -1;
		m_current_object_vert_side = -1;
		
		m_current_object = m_data.get_control_object(x, y);
		
		if (m_current_object == null)
		{
			m_current_object = m_data.get_bonus(x, y);
		}
		else
		{
			m_current_object.get_bounds(m_work_rectangle);	
			Rectangle r = m_work_rectangle;
			if (r.contains(x, y))
			{
				Rectangle r2 = (Rectangle)r.clone();
				
				r2.height = ControlObject.OUTLINE_SIZE;
				
				if (r2.contains(x,y))
				{
					// top
					m_current_object_horiz_side = 0;
				}
				else
				{
					r2.y += r.height - r2.height;
					if (r2.contains(x,y))
					{
						m_current_object_horiz_side = 1;
					}
				}
				
				r2.width = ControlObject.OUTLINE_SIZE;
				r2.height = r.height;
				r2.y = r.y;

				if (r2.contains(x,y))
				{
					m_current_object_vert_side = 0;
				}
				else
				{
					r2.x += r.width - r2.width;
					if (r2.contains(x,y))
					{
						m_current_object_vert_side = 1;
					}

				}
									
			}					
		}
	}
	
	// main mouse event
	
	@Override
	public void mousePressed(MouseEvent e) 
	{
		if (m_image != null)
		{
			int x = e.getX();
			int y = e.getY();

			switch (e.getButton()) 
			{
			case MouseEvent.BUTTON1:
				
				// compute which object is selected
				
				if (e.getClickCount()==1)
				{
					handle_button_1_click(x, y);
				}
				else
				{
					ControlObject co = m_data.get_control_object(x, y);
					GfxObject gfo = m_data.get_bonus(x, y);

					//m_current_object
					if (co != null)
					{
						if (co.get_type()==ControlObject.Type.Enemy)
						{
							PopupMenuEditActionAdapter edit_enemy_properties = new PopupMenuEditActionAdapter(x,y,PopupMenuEditActionType.Edit_Enemy_Properties);
							edit_enemy_properties.actionPerformed(null);
						}
						else
						{
							PopupMenuEditActionAdapter edit_control_object = new PopupMenuEditActionAdapter(x,y,PopupMenuEditActionType.Edit_Control_Object);
							edit_control_object.actionPerformed(null);
						}
					}
					else if (gfo != null)
					{
						PopupMenuEditActionAdapter edit_control_object = new PopupMenuEditActionAdapter(x,y,PopupMenuEditActionType.Edit_Object_Properties);
						edit_control_object.actionPerformed(null);
					}
				}
				break;
			case MouseEvent.BUTTON3: 
			{		
				ControlObject co = m_data.get_control_object(x, y);
				GfxObject gfo = m_data.get_bonus(x, y);
				
				if (m_association_dialog != null)
				{
					m_association_dialog.select_current_object(co);
					m_association_dialog.select_current_object(gfo);					
				}
				GfxFrame gf = null;
				
				if ((gfo == null) && (co == null))
				{
					gf = m_data.get_tile(x, y);
				}
				
				JPopupMenu popup = new JPopupMenu();

				if ((gf != null) || m_data.is_tile_overwrite_mode())
				{
					// display name and 2 sets of coordinates: xc, yc is the click coord,
					// xtg and ytg are the tile-rounded coords
					
					String label_start = gf == null ? "<no tile>" : gf.toString();
					
					popup.add(new JLabel(label_start+" (xc="+x+",yc="+y+") "+
							"(xtg="+m_data.get_grid().get_rounded_x(x,false)+
							",ytg="+m_data.get_grid().get_rounded_y(y,false)+")"));
					
					popup.add(new JSeparator(JSeparator.HORIZONTAL));
					if (gf != null)
					{
						create_popup_menu_item(popup,x,y,PopupMenuEditActionType.Copy_Tile);
						create_popup_menu_item(popup,x,y,PopupMenuEditActionType.Delete_Tile);
					}
					
					JMenu min = new JMenu("Create Controls");
										
					PopupMenuEditActionAdapter action_listener_model = new PopupMenuEditActionAdapter(x,y,PopupMenuEditActionType.New_Control_Object);
					for (ControlObject.Type tt : ControlObject.Type.values())
					{
						JMenuItem mi = new JMenuItem(tt.toString());
						
						ActionListener action_listener = action_listener_model.clone_with_user_data(tt,null);
						mi.addActionListener(action_listener);
						min.add(mi);
					}
					
						
					popup.add(min);
					
					if (m_object.copied != null)
					{
						BufferedImage bi = m_object.copied.toImage();
						JMenuItem mi = new JMenuItem("Paste " + m_object.copied.toString());
						
						if (bi != null)
						{
							ImageIcon ii = new ImageIcon(bi,"paste");
							mi = new JMenuItem("Paste " + m_object.copied.toString(),ii);
						}
						
						mi.addActionListener(new PopupMenuEditActionAdapter(m_object.copied.get_source_set(),
								x,y,PopupMenuEditActionType.Paste_Object));						
						popup.add(mi);
					}
					
					if (m_copied_hw_params != null)
					{
						JMenuItem mi = new JMenuItem("Paste " + m_copied_hw_params.location+ " hostile");
						mi.addActionListener(new PopupMenuEditActionAdapter(x,y,PopupMenuEditActionType.Paste_Enemy));						
						popup.add(mi);						
					}
					create_add_bonus_sub_menu(popup, x, y);
				
					if (!m_object.recent.isEmpty())
					{
						JMenu recent = create_popup_sub_menu(popup,"Recently added objects");
						for (GfxFrameSet gfs : m_object.recent)
						{
							JMenuItem mi = create_menu_item(gfs);
							mi.addActionListener(new PopupMenuEditActionAdapter(gfs,x,y,PopupMenuEditActionType.New_Object));

							recent.add(mi);					
						}
					}			
					
				}
				else if (co != null)
				{
					popup.add(new JLabel(co.toString()+" ("+x+","+y+")"));				
					popup.add(new JSeparator(JSeparator.HORIZONTAL));
					create_popup_menu_item(popup,x,y,PopupMenuEditActionType.Edit_Control_Object);
					create_popup_menu_item(popup,x,y,PopupMenuEditActionType.Copy_Control_Object);											
					create_popup_menu_item(popup,x,y,PopupMenuEditActionType.Delete_Control_Object);
					
					boolean create_edit_enemy = (co.get_type() == ControlObject.Type.Enemy);
					
					if (!create_edit_enemy && (co.get_type() == ControlObject.Type.Enemy_Trigger))
					{
						TriggerMonsterAssociation oa = (TriggerMonsterAssociation)m_data.get_association_set().get(co.get_name());
						create_edit_enemy = (oa != null);

						
					}
					
					if (create_edit_enemy)
					{
						popup.add(new JSeparator(JSeparator.HORIZONTAL));
						create_popup_menu_item(popup,x,y,PopupMenuEditActionType.Edit_Enemy_Properties);						
						create_popup_menu_item(popup,x,y,PopupMenuEditActionType.Copy_Enemy_Properties);
						if (m_copied_hw_params != null)
						{
							create_popup_menu_item(popup,x,y,PopupMenuEditActionType.Paste_Enemy_Properties);							
						}
						
						create_popup_menu_item(popup, x,y,PopupMenuEditActionType.Edit_Enemy_Trajectory);
						create_popup_menu_item(popup, x,y,PopupMenuEditActionType.Derive_Associated_Trigger);
					}
					
				}
				else if (gfo != null)
				{
					popup.add(new JLabel(gfo.toString()));				
					popup.add(new JSeparator(JSeparator.HORIZONTAL));
					create_popup_menu_item(popup,x,y,PopupMenuEditActionType.Edit_Object_Properties);
					create_popup_menu_item(popup,x,y,PopupMenuEditActionType.Copy_Object);
					create_popup_menu_item(popup,x,y,PopupMenuEditActionType.Delete_Object);						
				}
				else
				{
					popup.add(new JLabel("New"));				
					popup.add(new JSeparator(JSeparator.HORIZONTAL));
				}
				
				if (m_data.is_tile_overwrite_mode() || gf == null)
				{
					create_add_tile_sub_menu(popup, x, y);
					
					if (m_tile.copied != null)
					{
						ImageIcon ii = new ImageIcon(m_tile.copied.toImage(),"paste");
						
						JMenuItem mi = new JMenuItem("Paste " + m_tile.copied.toString(),ii);
						
						mi.addActionListener(new PopupMenuEditActionAdapter(m_tile.copied.get_source_set(),
								x,y,PopupMenuEditActionType.New_Tile));						
						popup.add(mi);

						mi = new JMenuItem("Paste set of " + m_tile.copied.toString(),ii);
						
						mi.addActionListener(new PopupMenuEditActionAdapter(m_tile.copied.get_source_set(),
								x,y,PopupMenuEditActionType.New_Tile_Set));						
						popup.add(mi);						
					}
					
					if (!m_tile.recent.isEmpty())
					{
						JMenu recent = create_popup_sub_menu(popup,"Recently added tiles");
						for (GfxFrameSet gfs : m_tile.recent)
						{
							JMenuItem mi = create_menu_item(gfs);
							mi.addActionListener(new PopupMenuEditActionAdapter(gfs,x,y,PopupMenuEditActionType.New_Tile));						
							recent.add(mi);					
						}
					}
				}

									
			
				popup.show(this, x,y);
			}
			break;
			}
		}
	}

	public void mouseReleased(MouseEvent arg0) 
	{
		m_current_object = null;
		
	}
	public void paintComponent(Graphics graphics)
	{
		if (m_image != null) 
		{
			graphics.drawImage(m_image, 0, 0, null);
		}
		
		m_data.editor_render(graphics);
	}

	public void update(Observable arg0, Object arg1) 
	{
		if ((arg1 instanceof ControlObjectDialog) || 
				(arg1 instanceof HostileWaveDialog))
		{
			repaint();
			m_data.set_modified(true);
		}
		else if (arg1 instanceof GfxObjectDialog)
		{
			update_map(true,false);			
			repaint();
		}
		else if (arg1 instanceof LevelResizeDialog) 
		{
			update_map(true,true);
			repaint();
		}
		else if (arg1 instanceof LevelCreationDialog) 
		{
			update_map(true,true);
			repaint();				
		}
		else if (arg1 instanceof LevelImportDialog)
		{
			LevelImportDialog lid = (LevelImportDialog) arg1;

			String image_file = lid.get_image_file();			

			// import

			BufferedImage leftovers = m_data.import_from_image(image_file,lid.get_tolerance(),lid.is_ignore_edge());

			m_data.set_modified(true);

			if (leftovers != null)
			{
				String leftover_file = DirectoryBase.get_snapshot_path()+"leftovers_"+m_data.get_project_file()+".png";

				File f = new File(leftover_file).getParentFile();
				boolean ok = true;
				
				if (!f.isDirectory())
				{
					ok = f.mkdir();					
				}
				if (ok)
				{
					ImageLoadSave.save_png(leftovers, leftover_file);

					JOptionPane.showMessageDialog(m_parent, "Leftover tiles saved in "+leftover_file);
				}
				else
				{
					error_message("I/O error", "Failed to write "+leftover_file);
				}
			}

			// refresh image

			update_map(true,true);

			repaint();

		}
		
	}
	public void update_map(boolean is_modified, boolean invalidate)
	{
		if (invalidate)
		{
			m_image = null;
		}
		m_image = m_data.render_all_layers(m_image,null);
	
		if (is_modified)
		{
			m_data.set_modified(true);
		}
		
		if (m_image != null)
		{
			setPreferredSize(new Dimension(m_image.getWidth(), m_image.getHeight()));

			revalidate();
		}
	}
	
	public EditLevel(GodsEditor parent,LevelData levelData)
	{
		addMouseListener(this);
		addKeyListener(this);
		addMouseMotionListener(this);
		

		m_data = levelData;
		m_parent = parent;
		//m_observable = new Message(parent);
	}
}
