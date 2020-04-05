package gods.editor.palette;
import javax.swing.*;


import java.awt.*;

import gods.base.*;

import java.awt.image.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;

public class EditGfxPalette extends JPanel  implements MouseListener, Observer
{
	public static final long serialVersionUID = 1;
	
	private Frame m_parent;
	private boolean m_image_loaded;
	private GfxFrameSet m_frame_set_to_move = null;
	
	public void toggle_view_defined()
	{
		m_data.toggle_view_defined();
		repaint();
	}
	
	
	public void update(Observable arg0, Object arg1) 
	{
		if (arg1 instanceof EditGfxFrameSet) 
		{
			EditGfxFrameSet egf = (EditGfxFrameSet) arg1;
			if (egf.is_new())
			{
				String name = egf.get_data().get_name();
				if (name != null)
				{
					if (m_data.lookup_frame_set(name) != null)
					{
						JOptionPane.showMessageDialog(this, "Object \""+name+"\" already exists",
								"Duplicate name error", JOptionPane.ERROR_MESSAGE);
					}
					else
					{
						/*Rectangle r = egf.get_bounding_box();
						if (r != null)
						GfxFrameSet gfs = new GfxFrameSet(name,.get_type());		
						GfxFrame gf = new GfxFrame(m_data.get_1x_image(),r);

						gfs.add(gf);*/

						m_data.add(egf.get_data());
					}
				}		
			}
			repaint();

		}
		
	}
	public void mouseClicked(MouseEvent arg0) 
	{

			
		
	}

	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	public enum PopupMenuNewActionType { New_Frame, New_Frame_Set, Append_Frame_to_Frame_Set }

	private class PopupMenuNewActionAdapter implements ActionListener
	{
		private PopupMenuNewActionType m_action_type;
		private Rectangle m_bounds;
		public PopupMenuNewActionAdapter(Rectangle bounds,PopupMenuNewActionType at)
		{
			m_bounds = bounds;
			m_action_type = at;
		}
		public void actionPerformed(ActionEvent e) 
		{
			switch (m_action_type)
			{
			case New_Frame:
				create_gfx_frames(m_bounds,true);
				break;
			case New_Frame_Set:
				create_gfx_frames(m_bounds,false);
				break;
			}
		}
	}
	
	public enum PopupMenuEditActionType { Edit, Move_To, Delete_All, Delete_Frame }

	private class PopupMenuEditActionAdapter implements ActionListener
	{
		private PopupMenuEditActionType m_action_type;
		private GfxFrameSet m_frame_set;
		private GfxFrame m_frame;
		
		public PopupMenuEditActionAdapter(GfxFrameSet gfs,GfxFrame gf,PopupMenuEditActionType at)
		{
			m_action_type = at;
			m_frame_set = gfs;
			m_frame = gf;
		}
		public void actionPerformed(ActionEvent e) 
		{
			switch (m_action_type)
			{
			case Move_To:
				m_frame_set_to_move = m_frame_set;
				break;
			case Delete_All:
				m_data.remove(m_frame_set);
				break;
			case Delete_Frame:
				m_frame_set.remove(m_frame);
				if (m_frame_set.get_nb_frames() == 0)
				{
					m_data.remove(m_frame_set);
				}
				break;
			case Edit:
				EditGfxFrameSet egf = new EditGfxFrameSet(m_parent, m_frame_set, EditGfxPalette.this, m_data.get_2x_image(),false);
				
				egf.setLocationRelativeTo(EditGfxPalette.this);
				egf.setVisible(true);

				break;
			}
			repaint();
		}

	}
	
	private class PopupMenuAppendActionAdapter implements ActionListener
	{
		private Rectangle m_source_bounds;
		private GfxFrameSet m_destination;
		
		public PopupMenuAppendActionAdapter(Rectangle source_bounds, GfxFrameSet destination)
		{
			m_source_bounds = source_bounds;
			m_destination = destination;
		}
		public void actionPerformed(ActionEvent e) 
		{
			append_gfx_frames(m_source_bounds,m_destination);
			repaint();
		}
	}
	
	private JMenu create_popup_sub_menu(JPopupMenu popup, String name)
	{
		JMenu rval = new JMenu(name.replace('_', ' '));
		
		popup.add(rval);
		
		return rval;
	}
	private JMenu create_popup_sub_menu(JMenu popup, String name)
	{
		JMenu rval = new JMenu(name.replace('_', ' '));
		
		popup.add(rval);
		
		return rval;
	}
	
	private JMenuItem create_popup_menu_item(JPopupMenu popup,String name,ActionListener aa)
	{
		JMenuItem menu_item = new JMenuItem(name.replace('_', ' '));
		menu_item.addActionListener(aa);
		popup.add(menu_item);
		return menu_item;
	}
	
	public void mousePressed(MouseEvent e) 
	{
		if (m_image_loaded)
		{
			int x = e.getX();
			int y = e.getY();

			switch (e.getButton()) 
			{
			case MouseEvent.BUTTON1: 
			{
				// contextual menu
				Rectangle r = m_data.compute_gfx_frame(x,y);
				
				if ((r != null) && (m_frame_set_to_move != null))
				{
					GfxFrameSet gfs = m_data.lookup_frame_set(r.x, r.y);
					
					if (gfs == null)
					{
						int x0 = r.x;
						int y0 = r.y;
						
						for (GfxFrame gf : m_frame_set_to_move.get_frames())
						{
							gf.set_coordinates(x0, y0);
						}
						m_frame_set_to_move = null;
						
						repaint();
					}
				}
				break;
			}
			case MouseEvent.BUTTON3: 
			{
				// contextual menu
				Rectangle r = m_data.compute_gfx_frame(x,y);

				if (r != null)
				{
					GfxFrameSet gfs = m_data.lookup_frame_set(r.x, r.y);

					JPopupMenu popup = new JPopupMenu();

					if (gfs != null)
					{
						popup.add(new JLabel(gfs.toString()));
						popup.add(new JSeparator(JSeparator.HORIZONTAL));
						// create popup menu according to type

						GfxFrame gf = gfs.lookup_frame(x, y);

						for (PopupMenuEditActionType at : PopupMenuEditActionType.values())
						{
							create_popup_menu_item(popup,at.name(),new PopupMenuEditActionAdapter(gfs,gf,at));						
						}
					}
					else
					{
						popup.add(new JLabel("Frame set"));
						popup.add(new JSeparator(JSeparator.HORIZONTAL));
						// create popup menu according to type

						for (PopupMenuNewActionType at : PopupMenuNewActionType.values())
						{
							switch (at)
							{
							case Append_Frame_to_Frame_Set:
								JMenu m = create_popup_sub_menu(popup,at.name());
								
								for (GfxFrameSet.Type t : GfxFrameSet.Type.values())
								{

									Collection<GfxFrameSet> gfsl = m_data.lookup_by_type(t);
									
									if (!gfsl.isEmpty())
									{
										JMenu mt = create_popup_sub_menu(m,t.name());
										int nb_items_per_menu = 30;
										
										int counter = 0;
										JMenu mt_sub = null;
										
										for (GfxFrameSet i : gfsl)
										{
											if (counter % nb_items_per_menu == 0)
											{
												mt_sub = create_popup_sub_menu(mt,""+counter/nb_items_per_menu);
											}
											
											JMenuItem mi = new JMenuItem(i.get_name());								
											mi.addActionListener(new PopupMenuAppendActionAdapter(r,i));
											mt_sub.add(mi);
											
											counter++;
										}
									}
								}
								break;
							default:
								create_popup_menu_item(popup,at.name(),new PopupMenuNewActionAdapter(r,at));						
							break;
							}
						}

					}
					popup.show(this, x,y);
				}
			}
			break;

			}
		}
	}


	
	private GfxFrameSet create_unnamed_gfx_frames(Rectangle r, int nb_frames)
	{
		Rectangle r2 = (Rectangle)r.clone();
		
		GfxFrameSet gfs = new GfxFrameSet();
		
		for (int i = 0; i < nb_frames; i++)
		{
			gfs.add(new GfxFrame(gfs,m_data.get_1x_image(),r2));
			
			if (i < nb_frames - 1)
			{
				r2 = m_data.compute_gfx_frame(r2.x + r2.width+1, r2.y+1);
			}
		}
		
		return gfs;
	}
	
	private void create_gfx_frames(Rectangle r, boolean one_frame)
	{
		int nb_frames = 0;
		
		if (one_frame)
		{
			nb_frames = 1;
		}
		else
		{
			String s = JOptionPane.showInputDialog("Number of consecutive horizontal frames", ""+1);
			if (s != null)
			{
				try 
				{
					nb_frames = Integer.parseInt(s);
				}
				catch (NumberFormatException e) 
				{

				}
			}
		}
		
		if (nb_frames > 0)
		{
			GfxFrameSet gfs = create_unnamed_gfx_frames(r,nb_frames);

			EditGfxFrameSet egf = new EditGfxFrameSet(m_parent, gfs, this, m_data.get_2x_image(), true);

			egf.setLocationRelativeTo(this);
			egf.setVisible(true);
		} 
	}

	private void append_gfx_frames(Rectangle r,GfxFrameSet destination)
	{
		String s = JOptionPane.showInputDialog("Number of consecutive horizontal frames to append", ""+1);
		if (s != null)
		{
			try 
			{
				int nb_frames = Integer.parseInt(s);

				GfxFrameSet gfs = create_unnamed_gfx_frames(r,nb_frames);
				
				destination.append(gfs);
			} 
			catch (NumberFormatException e) 
			{
				
			}
			
		}
	}
	

	public void mouseReleased(MouseEvent arg0) 
	{
		// TODO Auto-generated method stub
		
	}

	private GfxPalette m_data;
	
	public EditGfxPalette(Frame parent,GfxPalette object_set)
	{
		addMouseListener(this);
		m_data = object_set;
		m_parent = parent;
	}
	
	public void load_image() throws IOException
	{
		BufferedImage img = m_data.get_1x_image();
		 m_image_loaded = (img != null);
		 
		 if (m_image_loaded)
		{
		    setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));

		    revalidate();
		}
	}

	public void paintComponent(Graphics graphics)
	{
		//     super.paintComponent(graphics);

		if (m_data.get_1x_image() != null) 
		{
			graphics.drawImage(m_data.get_1x_image(), 0, 0, null);
		}
		
		m_data.editor_render(graphics);
	}

	//MenuActionType
}
