package gods.editor.level;

import java.awt.Frame;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import java.util.*;
import gods.base.*;
import gods.base.associations.*;
import gods.editor.DataModifier;
import gods.sys.MiscUtils;

public class AssociationDialog extends JDialog implements DataModifier<ObjectAssociationSet> {

		public void select_current_object(GfxObject go)
		{
			if (go != null && go.is_named())
			{
				// try to select the object in list items if loaded in it
				getJListObject1().setSelectedValue(go, true);
				getJListObject2().setSelectedValue(go, true);
				getJListObject3().setSelectedValue(go, true);

			}
		}
		
	public void data_to_gui() 
	{
		update_association_list();
		
	}

	public ObjectAssociationSet get_data() {
	
		return m_level_data.get_association_set();
	}

	public void gui_to_data() {
		// nothing done
		
	}

	public void on_close() {
		dispose();
		
	}

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JButton jButton = null;
	private JTextField jAssocDescription = null;
	
	private JList jListAssoc = null;

	private JList jListObject2 = null;

	private JList jListObject3 = null;

	private JComboBox jTypeComboBox = null;

	private JList jListObject1 = null;

	private LevelData m_level_data = null;

	private JButton jButtonDelete = null;
	private JButton jButtonNew = null;
	private JButton jButtonUpdate = null;
	
	public AssociationDialog(Frame owner, LevelData data) {
		super(owner);
		m_level_data = data;
		initialize();
		update_association_list();
	}

	/**
	 * @param owner
	 */
	public AssociationDialog(Frame owner) {
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(745, 400);
		this.setTitle("Edit Associations");
		this.setContentPane(getJContentPane());
	}

	private void make_scrollable(JList l)
	{
	JScrollPane scrollPane = new JScrollPane(l);
	scrollPane.setBounds(l.getBounds());
	jContentPane.add(scrollPane,null);
	}
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getJCloseButton(), null);
			jContentPane.add(getJAssociationDescription(),null);
			make_scrollable(getJListAssoc());
			make_scrollable(getJListObject1());
			make_scrollable(getJListObject2());
			make_scrollable(getJListObject3());
			
			jContentPane.add(getJTypeComboBox(), null);
			jContentPane.add(getJButtonDelete(), null);
			jContentPane.add(getJButtonNew(),null);
			jContentPane.add(getJButtonUpdate(),null);
		
		}
		return jContentPane;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJCloseButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setBounds(new Rectangle(303, 340, 82, 25));
			jButton.setText("Close");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) 
				{
					on_close();
				}
			});
		}
		return jButton;
	}
	private JTextField getJAssociationDescription() {
		if (jAssocDescription == null) {
			jAssocDescription = new JTextField();
			jAssocDescription.setBounds(new Rectangle(10, 290, 700, 30));
			jAssocDescription.setEditable(false);
		}
		return jAssocDescription;
	}

	/**
	 * This method initializes jListAssoc	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getJListAssoc() {
		if (jListAssoc == null) {
			jListAssoc = new JList();
			jListAssoc.setBounds(new Rectangle(10, 45, 179, 220));
			jListAssoc.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			MouseListener mouseListener = new MouseAdapter() {
			     public void mouseClicked(MouseEvent e) {
		             int index = jListAssoc.locationToIndex(e.getPoint());
			         /*if (e.getClickCount() == 2) {
			             System.out.println("Double clicked on Item " + index);
			          }*/
		             // selects the associated objects in the other lists
		             set_association_context(index);
			     }
			 };
			 jListAssoc.addMouseListener(mouseListener);
		}
		return jListAssoc;
	}

	/**
	 * This method initializes jListObject2	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getJListObject2() {
		if (jListObject2 == null) {
			jListObject2 = new JList();
			jListObject2.setBounds(new Rectangle(375, 43, 179, 226));
			}
		return jListObject2;
	}

	/**
	 * This method initializes jListObject3	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getJListObject3() {
		if (jListObject3 == null) {
			jListObject3 = new JList();
			jListObject3.setBounds(new Rectangle(566, 46, 158, 224));
			jListObject3.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return jListObject3;
	}

	/**
	 * This method initializes jTypeComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJTypeComboBox() {
		if (jTypeComboBox == null) {
			jTypeComboBox = new JComboBox();
			jTypeComboBox.setBounds(new Rectangle(143, 11, 307, 24));
			jTypeComboBox.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					combo_box_value_change();
				}
			});
			/*
			jTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
				}
			});*/
			
			for (ObjectAssociation.Type t : ObjectAssociation.Type.values())
			{
				jTypeComboBox.addItem(t);
			}
		}
		return jTypeComboBox;
	}

	/**
	 * This method initializes jListObject1	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getJListObject1() 
	{
		if (jListObject1 == null) {
			jListObject1 = new JList();
			jListObject1.setBounds(new Rectangle(199, 43, 161, 221));
		}
		return jListObject1;
	}

	private void update_current_association()
	{
		int [] none = new int[0];
		
		getJListObject1().setSelectedIndices(none);
		
		create_association(false);
	}
	
	private void create_new_association()
	{
		create_association(true);
	}
	
	private void create_association(boolean must_be_new)
	{
		ObjectAssociation.Type t = (ObjectAssociation.Type)getJTypeComboBox().getSelectedItem();
		ObjectAssociation to_add = null;
		try 
		{
			Nameable<?> go1 = (Nameable<?>)getJListObject1().getSelectedValue();
			
			if (!must_be_new && (go1 == null))
			{
				JList jl = getJListAssoc();
				ObjectAssociation existing = (ObjectAssociation)jl.getSelectedValue();
				
				if (existing != null)
				{
					to_add = existing;
					go1 = to_add.get_object(0);
				}
			}
			
			if ((to_add == null)&&must_be_new)
			{
				to_add = ObjectAssociation.build_association(t);
			}
			
			if ((to_add != null) && to_add.is_multi())
			{
				if (go1 != null)
				{
					Object [] selected = getJListObject2().getSelectedValues();
					if (selected.length > 0)
					{
						to_add.set_objects(go1,selected);
					}
				}
				else
				{
					to_add = null;
				}
			}
			else
			{
				Nameable<?> go2 = (Nameable<?>)getJListObject2().getSelectedValue();
				Nameable<?> go3 = (Nameable<?>)getJListObject3().getSelectedValue();

				if (go1 != null) 
					
				{
					to_add.set_object(0, go1);


					if (go2 != null)
					{
						to_add.set_object(1, go2);
						if (go3 != null)
						{
							to_add.set_object(2, go3);
						}
						else
						{
							if (getJListObject3().isVisible())
							{
								to_add = null;
							}
						}
					}
					else
					{				
						to_add = null;
						
					}
				}
				else
				{				
					to_add = null;
					
				}
				
			}
		} catch (Exception e) 
		{

			e.printStackTrace();
		}
		if (to_add != null)
		{
			int propind = to_add.get_property_index();
			
			if (propind >= 0)
			{
				// ask for property
				Object o = to_add.get_object(propind);
				AssociationProperty ap = (AssociationProperty)o;
				String old_v = ap != null ? ap.get_name() : "";
				
				String v = JOptionPane.showInputDialog(this,"new property value",old_v);
				
				if (v != null)
				{
					if (ap == null)
					{
						ap = new AssociationProperty(v);
					}
					else
					{
						ap.set_name(v);
					}
				
					to_add.set_object(propind, ap);
				}
				
			}
			if (!must_be_new)
			{
				m_level_data.remove_association(to_add);				
			}
			if (!m_level_data.add_association(to_add))
			{
				JOptionPane.showMessageDialog(this, 
						"Association "+to_add.get_name()+" already exists", 
						"Duplicate association", JOptionPane.ERROR_MESSAGE);
			}
			else if (!must_be_new)
			{
				JOptionPane.showMessageDialog(this, 
						"Updated association "+to_add.get_name(), 
						"Existing association", JOptionPane.INFORMATION_MESSAGE);

			}
		

			update_association_list();
		}
		else
		{
			MiscUtils.show_error_dialog(this, "creation/update error", "wrong parameters");
		}
	}

	
	
	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButtonDelete() {
		if (jButtonDelete == null) {
			jButtonDelete = new JButton();
			jButtonDelete.setBounds(new Rectangle(10, 13, 124, 21));
			jButtonDelete.setText("Delete");
			jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) 
				{
					delete_association();
				}
			});
		}
		return jButtonDelete;
	}
	private JButton getJButtonNew() {
		if (jButtonNew == null) {
			jButtonNew = new JButton();
			jButtonNew.setBounds(new Rectangle(460, 13, 120, 21));
			jButtonNew.setText("Create new");
			jButtonNew.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) 
				{
					create_new_association();
				}
			});
		}
		return jButtonNew;
	}
	private JButton getJButtonUpdate() {
		if (jButtonUpdate == null) {
			jButtonUpdate = new JButton();
			jButtonUpdate.setBounds(new Rectangle(600, 13, 120, 21));
			jButtonUpdate.setText("Update");
			jButtonUpdate.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) 
				{
					update_current_association();
				}
			});
		}
		return jButtonUpdate;
	}

	private void set_association_context(int idx)
	{
		JList jl = getJListAssoc();
		ObjectAssociation oa = (ObjectAssociation)jl.getSelectedValue();
		ObjectAssociation.Type t = oa.get_type();
		getJTypeComboBox().setSelectedItem(t);  // first, set association type combo
		
		//getJListObject1().setSelectedValue(oa.get_object(0), true);
		//getJListObject1().setEnabled(false);
		
		// then, update description of the association
		getJAssociationDescription().setText(oa.describe());
		
		switch (t)
		{
		case Lever_Item_Display:
		case Lever_Monster_Kill:
		case Trigger_Monster:
			int [] l = new int[oa.get_nb_objects()-1];

			for (int i = 1; i < oa.get_nb_objects(); i++)
			{
				getJListObject2().setSelectedValue(oa.get_object(i), true);	
				l[i-1] = getJListObject2().getSelectedIndex();
			}
			
			getJListObject2().setSelectedIndices(l);
			
			break;
		default:
			getJListObject2().setSelectedValue(oa.get_object(1), true);
		getJListObject3().setSelectedValue(oa.get_object(2), true);
		break;
		}
		
	}
	@SuppressWarnings("unchecked")
	private void update_association_list()
	{
		Vector<ObjectAssociation> v = new Vector<ObjectAssociation>();
		v.addAll(m_level_data.get_association_set().items());
		Collections.sort(v);
		getJListAssoc().setListData(v);
	}

	private void load_tile_list(JList l)
	{
		Collection<GfxFrameSet> gfsl = m_level_data.get_level_palette().lookup_by_type(GfxFrameSet.Type.foreground_tile);
		Vector<GfxFrameSet> gfsv = new Vector<GfxFrameSet>();
			
		gfsv.addAll(gfsl);
		
		load_and_exclude(l,gfsv,null);
	}
	private void load_list(JList l,ControlObject.Type t)
	{
		ControlObject.Type [] tl = new ControlObject.Type[1];
		tl[0] = t;
		load_list(l,tl);
	} 
	


	private void load_list(JList l,ControlObject.Type [] tl)
	{
		Vector<ControlObject> gl = new Vector<ControlObject>();
		for (ControlObject.Type t : tl)
		{
			m_level_data.get_control_layer().get_items(t,gl);
		}
		
		Collections.sort(gl);
		
		l.setListData(gl);
		l.setVisible(true);
	}
	
	@SuppressWarnings("unchecked")
	private void load_list(JList l,GfxFrameSet.Type t, Collection<? extends Nameable> exclude)
	{
		Vector<GfxObject> gl = new Vector<GfxObject>();
		m_level_data.get_object_layer().get_items(t, gl);
		
		load_and_exclude(l,gl,exclude);

	}
	@SuppressWarnings("unchecked")
	private void load_list(JList l,GfxFrameSet.Type [] tl, Collection<? extends Nameable> exclude)
	{
		Vector<GfxObject> gl = new Vector<GfxObject>();
		for (GfxFrameSet.Type t : tl)
		{
			m_level_data.get_object_layer().get_items(t, gl);
		}
		
		load_and_exclude(l,gl,exclude);

	}
	@SuppressWarnings("unchecked")
	private void load_list(JList l,ControlObject.Type t, Collection<? extends Nameable> exclude)
	{
		Vector<ControlObject> gl = new Vector<ControlObject>();
		m_level_data.get_control_layer().get_items(t, gl);
		
		load_and_exclude(l,gl,exclude);
	}
	
	@SuppressWarnings("unchecked")
	private void load_and_exclude(JList l,Vector<? extends Nameable> gl,Collection<? extends Nameable> exclude)
	{
		// remove all items from the list if contained in exclude list
		// also remove items without name
		
		
		Vector<Integer> unnamed = new Vector<Integer>();
		
		for (int j = gl.size()-1; j >= 0; --j)
		{
			if (!gl.elementAt(j).is_named())
			{
				unnamed.add(j);
			}
		}
		for (Integer i : unnamed)
		{
			gl.removeElementAt(i);
		}
		
		if (exclude != null)
		{
			for (Nameable n : exclude)
			{
				int i = -1;
				
				for (int j = 0; j < gl.size(); j++)
				{
					if (gl.elementAt(j).get_name().equals(n.get_name()))
					{
						i = j;
						break;
					}
				}
				if (i != -1)
				{
					gl.removeElementAt(i);
				}
			}
		}
		
		Collections.sort(gl);

		l.setListData(gl);
		l.setVisible(true);
	}
	
	private void delete_association()
	{
		ObjectAssociation oa = (ObjectAssociation)getJListAssoc().getSelectedValue();
		if (oa != null)
		{
			m_level_data.remove_association(oa);
			update_association_list();
			combo_box_value_change(); // re-insert value in list 1
		}
	}
	
	private void combo_box_value_change()
	{
		ObjectAssociation.Type t = (ObjectAssociation.Type)getJTypeComboBox().getSelectedItem();
		getJListObject3().setVisible(false);
		getJListObject2().setVisible(true);
		
		Collection<ObjectAssociation> assoc = m_level_data.get_association_set().items();
	
		getJListObject1().setEnabled(true);
		
		jListObject2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		switch (t)
		{
		case Trigger_Item_Display:
			load_list(getJListObject1(),ControlObject.Type.Bonus,assoc);
			load_list(getJListObject2(),GfxFrameSet.Type.pickable,null);
			load_list(getJListObject3(),GfxFrameSet.ALL_ITEMS,null);
			getJListObject3().setVisible(true);
			break;
		case Trigger_Item_Display_2:
		case Trigger_Item_Display_Special:
			load_list(getJListObject1(),ControlObject.Type.Bonus,assoc);
			load_list(getJListObject2(),GfxFrameSet.ALL_ITEMS,null);
			break;
		case Trigger_Close_Door:
			load_list(getJListObject1(),ControlObject.Type.Bonus,assoc);
			load_list(getJListObject2(),ControlObject.DOORS);
			break;
		case Trigger_Monster:
			load_list(getJListObject1(),ControlObject.Type.Enemy_Trigger,assoc);
			load_list(getJListObject2(),ControlObject.Type.Enemy);
			jListObject2.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			break;
		case Lever_Monster_Kill:
			load_list(getJListObject1(),GfxFrameSet.Type.activable,assoc);
			load_list(getJListObject2(),ControlObject.Type.Enemy);
			jListObject2.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			break;
		case Teleport_Location:
			load_list(getJListObject1(),GfxFrameSet.Type.teleport_gem,assoc);
			load_list(getJListObject2(),ControlObject.TELEPORT_DESTS);
			break;
		case Lever_Door:
			load_list(getJListObject1(),GfxFrameSet.Type.activable,assoc);
			load_list(getJListObject2(),ControlObject.DOORS);
			break;
		case Lever_Door_Key:
			load_list(getJListObject1(),GfxFrameSet.Type.activable,assoc);
			load_list(getJListObject2(),ControlObject.DOORS);
			load_list(getJListObject3(),GfxFrameSet.Type.key,null);
			getJListObject3().setVisible(true);
			break;
		case Lever_Platform:
			load_list(getJListObject1(),GfxFrameSet.Type.activable,assoc);
			load_list(getJListObject2(),ControlObject.Type.Moving_Block_Start);
			break;
		case Lever_Platform_Key:
			load_list(getJListObject1(),GfxFrameSet.Type.activable,assoc);
			load_list(getJListObject2(),ControlObject.Type.Moving_Block_Start);
			load_list(getJListObject3(),GfxFrameSet.Type.key,null);
			getJListObject3().setVisible(true);
			break;
		case Face_Door_Location:
			load_list(getJListObject1(),ControlObject.Type.Face_Door);
			load_list(getJListObject2(),ControlObject.TELEPORT_DESTS_AND_FACE_DOORS);
			break;
		case Moving_Block_Tile:
			load_list(getJListObject1(),ControlObject.Type.Moving_Block_Start);
			load_tile_list(getJListObject2());
			break;
		case Lever_Item_Display:
			load_list(getJListObject1(),GfxFrameSet.Type.activable,assoc);
			load_list(getJListObject2(),GfxFrameSet.ALL_ITEMS,null);
			break;
			
		}
	}
}  //  @jve:decl-index=0:visual-constraint="52,-55"
