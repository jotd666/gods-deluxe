package gods.editor.level;


import java.awt.Frame;
import gods.editor.DataModifier;

import javax.swing.JDialog;

import gods.base.*;
import gods.base.HostileWaveParameters.AppearingDelay;
import gods.editor.ComboSelectorDialog;
import gods.sys.JMultiLineToolTip;

import java.util.*;
import java.io.*;

import javax.swing.*;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JList;


public class HostileWaveDialog extends JDialog implements DataModifier<HostileWaveParameters> {

	private HostileWaveParameters m_data;
	
	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JLabel jLabel = null;

	private JComboBox jFrameSetComboBox = null;

	private JLabel jLabel2 = null;

	private JButton jButton = null;

	private JLabel jLabel3 = null;


	private JLabel jLabel4 = null;

	private JComboBox jClassComboBox = null;

	private JTextField jMaxFramesTextField = null;

	private JTextField jAppearingDelayTextField = null;

	private JLabel jLabel7 = null;
	private JLabel jLabel71 = null;

	private JTextField jNbEnemiesTextField = null;
	private JTextField jObjectiveBalanceTextField = null;


	private JLabel jLabel8 = null;

	private JLabel jLabel6 = null;

	private JList jClassNameList = null;

	private JList jInstanceNameList = null;

	private JLabel jLabel9 = null;

	private JLabel jLabel10 = null;

	private JCheckBox jInstantCreationCheckBox = null;

	private JLabel jLabel11 = null;

	private JTextField jHealthPointsTextField = null;

	private JLabel jLabel1 = null;

	private JComboBox jFireTypeComboBox = null;

	private JList jHostileNameList = null;

	private JLabel jLabel5 = null;

	private JComboBox jShootSpeedComboBox = null;

	private JLabel jLabel12 = null;

	private JLabel jLabel13 = null;

	private JComboBox jShootFrequencyComboBox = null;

	private JLabel jLabel14 = null;

	private JComboBox jSpeedComboBox = null;
	private JComboBox jDirectionComboBox = null;
	private JComboBox jAttackDistanceComboBox = null;
	private JComboBox jJumpHeightComboBox = null;
	private JComboBox jJumpThresholdComboBox = null;
	private JComboBox jJumpWidthComboBox = null;

	private JLabel jLabel15 = null;

	private JLabel jLabel16 = null;

	private JCheckBox jStealOnTheWayCheckBox = null;

	private JCheckBox jAvoidShotsCheckBox = null;


	private JLabel jLabel161 = null;


	private JLabel jLabel31 = null;

	private JComboBox jAppearingDelayComboBox = null;

	private JLabel jLabel1611 = null;


	private class EditObs extends Observable
	{
		void signal()
		{
			setChanged();
			notifyObservers(HostileWaveDialog.this);
		}
	}
	
	private EditObs m_observable = new EditObs();  //  @jve:decl-index=0:

	public void data_to_gui() 
	{
		if (m_data != null)
		{
			setTitle("Edit params for hostile "+m_data.location);
			
			Collection<GfxFrameSet> egfs = m_data.level.get_level_palette().lookup_by_type(GfxFrameSet.Type.enemy);
			
			for (GfxFrameSet gfs : egfs)
			{
				getJFrameSetComboBox().addItem(gfs.get_name());
			}
			
			frame_set_changed();
			
			for (HostileWaveParameters.Speed s : HostileWaveParameters.Speed.values())
			{
				getJMoveSpeedComboBox().addItem(s);
				getJShootSpeedComboBox().addItem(s);
				getJShootFrequencyComboBox().addItem(s);
			}
			getJMoveSpeedComboBox().setSelectedItem(m_data.move_speed);
			getJShootSpeedComboBox().setSelectedItem(m_data.shoot_speed);
			getJShootFrequencyComboBox().setSelectedItem(m_data.shoot_frequency);
			
			for (HostileWaveParameters.JumpHeight j : HostileWaveParameters.JumpHeight.values())
			{
				getJJumpHeightComboBox().addItem(j);
			}
			getJJumpHeightComboBox().setSelectedItem(m_data.jump_height);
			
			for (HostileWaveParameters.JumpThreshold j : HostileWaveParameters.JumpThreshold.values())
			{
				getJJumpThresholdComboBox().addItem(j);
			}
			getJJumpThresholdComboBox().setSelectedItem(m_data.jump_threshold);
			
			for (HostileWaveParameters.JumpWidth j : HostileWaveParameters.JumpWidth.values())
			{
				getJJumpWidthComboBox().addItem(j);
			}
			getJJumpWidthComboBox().setSelectedItem(m_data.jump_width);
			
			for (HostileWaveParameters.FireType s : HostileWaveParameters.FireType.values())
			{
				getJFireTypeComboBox().addItem(s);
			}
			getJFireTypeComboBox().setSelectedItem(m_data.fire_type);
			
			for (HostileWaveParameters.AttackDistance s : HostileWaveParameters.AttackDistance.values())
			{
				getJAttackDistanceComboBox().addItem(s);
			}
			getJAttackDistanceComboBox().setSelectedItem(m_data.attack_distance);
			
			for (HostileWaveParameters.Direction s : HostileWaveParameters.Direction.values())
			{
				getJDirectionComboBox().addItem(s);
			}
			getJDirectionComboBox().setSelectedItem(m_data.direction);
			
			//getJShootShieldCheckBox().setSelected(m_data.shoot_shield);
			
			getJAvoidShotsCheckBox().setSelected(m_data.avoid_shoot);
			
			getJStealOnTheWayCheckBox().setSelected(m_data.steal_on_the_way);

			String package_name = "gods.game.characters.hostiles";
			String bin_dir = DirectoryBase.get_root()+"bin";
			String class_dir = bin_dir+File.separator+package_name.replace('.', File.separatorChar);
			
			File [] classes = new File(class_dir).listFiles();
			
			for (File c : classes)
			{
				String s = package_name+"."+c.getName();
				getJClassComboBox().addItem(s.replace(".class", ""));
			}
			
			getJFrameSetComboBox().setSelectedItem(m_data.frame_set_name);
			
			getJInstantCreationCheckBox().setSelected(m_data.instant_creation);
			
			getJClassComboBox().setSelectedItem(m_data.class_name);
			
			for (AppearingDelay ad : AppearingDelay.values())
			{
				getJAppearingDelayComboBox().addItem(ad);
			}
			
			getJAppearingDelayComboBox().setSelectedItem(m_data.appearing_delay_type);
			
			update_appearing_delay();
			
			getJNbEnemiesTextField().setText(""+m_data.nb_enemies);
			getJObjectiveBalanceTextField().setText(""+m_data.objective_balance);
			
			getJHealthPointsTextField().setText(""+m_data.health_points);
			update_dropped_object_list();
		}
	}

	private void update_appearing_delay()
	{
		Object si = getJAppearingDelayComboBox().getSelectedItem();
		
		boolean custom = si == AppearingDelay.Custom;
		
		getJAppearingDelayTextField().setEnabled(custom);
		
		if (!custom)
		{
			getJAppearingDelayTextField().setText(""+m_data.appearing_delay);
		}
	}
	
	public HostileWaveParameters get_data() {
		
		return m_data;
	}

	public void gui_to_data() 
	{
		m_data.frame_set_name = ((String)getJFrameSetComboBox().getSelectedItem());
		if (m_data.frame_set_name == null) throw new NullPointerException();
		
		//m_data.to_left = getJToLeftCheckBox().isSelected();
		
		m_data.appearing_delay_type = (AppearingDelay)getJAppearingDelayComboBox().getSelectedItem();
		if (m_data.appearing_delay_type == AppearingDelay.Custom)
		{
			m_data.appearing_delay = Integer.parseInt(getJAppearingDelayTextField().getText());
		}
		m_data.class_name = (String)getJClassComboBox().getSelectedItem();
		m_data.instant_creation = getJInstantCreationCheckBox().isSelected();
		m_data.health_points = Integer.parseInt(getJHealthPointsTextField().getText());
		
		m_data.move_speed = (HostileWaveParameters.Speed)getJMoveSpeedComboBox().getSelectedItem();
		m_data.shoot_speed = (HostileWaveParameters.Speed)getJShootSpeedComboBox().getSelectedItem();
		m_data.shoot_frequency = (HostileWaveParameters.Speed)getJShootFrequencyComboBox().getSelectedItem();
		m_data.fire_type = (HostileWaveParameters.FireType)getJFireTypeComboBox().getSelectedItem();
		m_data.direction = (HostileWaveParameters.Direction)getJDirectionComboBox().getSelectedItem();
		m_data.attack_distance = (HostileWaveParameters.AttackDistance)getJAttackDistanceComboBox().getSelectedItem();
		//m_data.shoot_shield = getJShootShieldCheckBox().isSelected();
		m_data.avoid_shoot = getJAvoidShotsCheckBox().isSelected();
		m_data.steal_on_the_way = getJStealOnTheWayCheckBox().isSelected();
		m_data.jump_height = (HostileWaveParameters.JumpHeight)getJJumpHeightComboBox().getSelectedItem();
		m_data.jump_threshold = (HostileWaveParameters.JumpThreshold)getJJumpThresholdComboBox().getSelectedItem();
		m_data.jump_width = (HostileWaveParameters.JumpWidth)getJJumpWidthComboBox().getSelectedItem();
		
		m_data.nb_enemies = Integer.parseInt(getJNbEnemiesTextField().getText());
		m_data.objective_balance = Integer.parseInt(getJObjectiveBalanceTextField().getText());
		
		frame_set_changed();
	}

	public void on_close() 
	{
		try
		{
			gui_to_data();
			dispose();
			m_observable.signal();
		}
		catch (Exception e)
		{
			
		}
	}

	/**
	 * @param owner
	 */
	public HostileWaveDialog(Frame owner) {
		super(owner);
		initialize();
	}
	/**
	 * @param owner
	 */
	public HostileWaveDialog(Frame owner,HostileWaveParameters data,Observer obs) 
	{
		super(owner);
		m_data = data;
		initialize();
		data_to_gui();
		m_observable.addObserver(obs);
		
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(574, 613);
		this.setContentPane(getJContentPane());
	}

	private void update_dropped_object_list()
	{
		int nb_items = 1;

		String [] instance_names = new String[nb_items];
		String [] class_names = new String[nb_items];
		String [] hostile_names = new String[nb_items];
		
		int i = 0;
		
		ObjectToDrop o = m_data.object_to_drop;
		
			instance_names[i] = ((o == null) || (o.instance_name == null)) ? "<null>" : o.instance_name;
			class_names[i] = ((o == null) || (o.class_name == null)) ? "<null>" : o.class_name;
			hostile_names[i] = ((o == null) || (o.hostile_name == null)) ? "<null>" : o.hostile_name;
			i++;
		
		getJClassNameList().setListData(class_names);
		getJInstanceNameList().setListData(instance_names);
		getJHostileNameList().setListData(hostile_names);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			int offset=330;
			int width=90;
			jLabel1611 = new JLabel();
			jLabel1611.setBounds(new Rectangle(offset, 325, width, 16));
			jLabel1611.setText("Jump width");
			jLabel1611.setToolTipText("Jumps when more or less close to the wall");
			jLabel31 = new JLabel();
			jLabel31.setBounds(new Rectangle(15, 173, 98, 25));
			jLabel31.setText("appearing delay");
			jLabel161 = new JLabel();
			jLabel161.setBounds(new Rectangle(offset, 285, width, 26));
			jLabel161.setToolTipText("Jumps when more or less close to the wall");
			jLabel161.setText("Jump threshold");
			jLabel16 = new JLabel();
			jLabel16.setBounds(new Rectangle(offset, 248, width, 27));
			jLabel16.setText("Jump height");
			jLabel15 = new JLabel();
			jLabel15.setBounds(new Rectangle(offset, 211, width, 22));
			jLabel15.setText("attack distance");
			jLabel14 = new JLabel();
			jLabel14.setBounds(new Rectangle(270, 172, 121, 28));
			jLabel14.setText("appearing direction");
			jLabel13 = new JLabel();
			jLabel13.setBounds(new Rectangle(281, 135, 91, 26));
			jLabel13.setText("shoot freq.");
			jLabel12 = new JLabel();
			jLabel12.setBounds(new Rectangle(282, 90, 83, 29));
			jLabel12.setText("shoot speed");
			jLabel5 = new JLabel();
			jLabel5.setBounds(new Rectangle(451, 391, 57, 21));
			jLabel5.setText("hostiles");
			jLabel1 = new JLabel();
			jLabel1.setBounds(new Rectangle(12, 133, 60, 26));
			jLabel1.setText("fire type");
			jLabel11 = new JLabel();
			jLabel11.setBounds(new Rectangle(12, 301, 84, 27));
			jLabel11.setText("Health points");
			jLabel10 = new JLabel();
			jLabel10.setBounds(new Rectangle(300, 391, 116, 21));
			jLabel10.setText("instance (keys ...)");
			jLabel9 = new JLabel();
			jLabel9.setBounds(new Rectangle(167, 393, 96, 19));
			jLabel9.setText("class (bonuses)");
			jLabel6 = new JLabel();
			jLabel6.setBounds(new Rectangle(212, 363, 233, 24));
			jLabel6.setText("Objects/hostiles dropped by hostiles:");
			jLabel8 = new JLabel();
			jLabel8.setBounds(new Rectangle(11, 91, 73, 26));
			jLabel8.setText("move speed");
			jLabel7 = new JLabel();
			jLabel7.setBounds(new Rectangle(13, 262, 82, 23));
			jLabel7.setText("# of enemies");
			jLabel71 = new JLabel();
			jLabel71.setBounds(new Rectangle(180, 250, 82, 23));
			jLabel71.setText("aggressivity");
			jLabel4 = new JLabel();
			jLabel4.setBounds(new Rectangle(12, 51, 106, 27));
			jLabel4.setText("Class to create");
			jLabel3 = new JLabel();
			jLabel3.setBounds(new Rectangle(13, 214, 131, 33));
			jLabel3.setText("custom delay (10th/s)");
			jLabel2 = new JLabel();
			jLabel2.setBounds(new Rectangle(469, 12, 70, 31));
			jLabel2.setText("# of frames");
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(13, 9, 62, 27));
			jLabel.setText("frame set");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.setToolTipText("Not really a thief, but picks objects when encounters them");
			jContentPane.add(jLabel, null);
			jContentPane.add(getJFrameSetComboBox(), null);
			jContentPane.add(jLabel2, null);
			jContentPane.add(jLabel3, null);
			jContentPane.add(jLabel4, null);
			jContentPane.add(getJClassComboBox(), null);
			jContentPane.add(getJMaxFramesTextField(), null);
			jContentPane.add(getJAppearingDelayTextField(), null);
			jContentPane.add(jLabel7, null);
			jContentPane.add(jLabel71, null);
			jContentPane.add(getJNbEnemiesTextField(), null);
			jContentPane.add(getJObjectiveBalanceTextField(),null);
			jContentPane.add(getJMoveSpeedComboBox(), null);
			jContentPane.add(jLabel8, null);
			jContentPane.add(jLabel6, null);
			jContentPane.add(getJClassNameList(), null);
			jContentPane.add(getJInstanceNameList(), null);
			jContentPane.add(jLabel9, null);
			jContentPane.add(jLabel10, null);
			jContentPane.add(getJInstantCreationCheckBox(), null);
			jContentPane.add(jLabel11, null);
			jContentPane.add(getJHealthPointsTextField(), null);
			jContentPane.add(jLabel1, null);
			jContentPane.add(getJFireTypeComboBox(), null);
			jContentPane.add(getJHostileNameList(), null);
			jContentPane.add(jLabel5, null);
			jContentPane.add(getJShootSpeedComboBox(), null);
			jContentPane.add(jLabel12, null);
			jContentPane.add(jLabel13, null);
			jContentPane.add(getJShootFrequencyComboBox(), null);
			jContentPane.add(jLabel14, null);
			jContentPane.add(getJDirectionComboBox(), null);
			jContentPane.add(jLabel15, null);
			jContentPane.add(getJAttackDistanceComboBox(), null);
			jContentPane.add(jLabel16, null);
			jContentPane.add(getJStealOnTheWayCheckBox(), null);
			jContentPane.add(getJAvoidShotsCheckBox(), null);
			jContentPane.add(getJJumpHeightComboBox(), null);
			jContentPane.add(jLabel161, null);
			jContentPane.add(getJJumpThresholdComboBox(), null);
			jContentPane.add(getJButton(), null);
			jContentPane.add(jLabel31, null);
			jContentPane.add(getJAppearingDelayComboBox(), null);
			jContentPane.add(jLabel1611, null);
			jContentPane.add(getJJumpWidthComboBox(), null);
		}
		return jContentPane;
	}

	private void frame_set_changed()
	{
		String s = (String)getJFrameSetComboBox().getSelectedItem();
		GfxFrameSet gfs = m_data.level.get_level_palette().lookup_frame_set(s);
		if (gfs != null)
		{
			getJMaxFramesTextField().setText(""+gfs.get_nb_frames());
		}
	}
	/**
	 * This method initializes jFrameSetComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJFrameSetComboBox() 
	{
		if (jFrameSetComboBox == null) {
			jFrameSetComboBox = new JComboBox();
			jFrameSetComboBox.setBounds(new Rectangle(140, 13, 305, 26));
			jFrameSetComboBox.setToolTipText("The name of the frame set defined in the palette editor");
			jFrameSetComboBox.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
						frame_set_changed();
					}
				}
			);
		}
		return jFrameSetComboBox;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("OK");
			jButton.setBounds(new Rectangle(231, 548, 92, 28));
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					on_close();
				}
			});
		}
		return jButton;
	}



	/**
	 * This method initializes jClassComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJClassComboBox() {
		if (jClassComboBox == null) {
			jClassComboBox = new JComboBox();
			jClassComboBox.setBounds(new Rectangle(141, 51, 304, 26));
			jClassComboBox.setToolTipText("The class that drives the hostile");
		}
		return jClassComboBox;
	}

	/**
	 * This method initializes jMaxFramesTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJMaxFramesTextField() {
		if (jMaxFramesTextField == null) {
			jMaxFramesTextField = new JTextField();
			jMaxFramesTextField.setBounds(new Rectangle(474, 52, 59, 31));
			jMaxFramesTextField.setEditable(false);
		}
		return jMaxFramesTextField;
	}

	/**
	 * This method initializes jAppearingDelayTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJAppearingDelayTextField() {
		if (jAppearingDelayTextField == null) {
			jAppearingDelayTextField = new JTextField()
			{
				public static final long serialVersionUID = 1L;
				
				public JToolTip createToolTip()
				{
					return new JMultiLineToolTip();
				}
			};
			jAppearingDelayTextField.setBounds(new Rectangle(155, 210, 60, 31));
			jAppearingDelayTextField.setToolTipText("Custom appearing delay value in tenths of seconds");
		}
		return jAppearingDelayTextField;
	}

	/**
	 * This method initializes jAppearingDelayComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJAppearingDelayComboBox() {
		if (jAppearingDelayComboBox == null) {
			jAppearingDelayComboBox = new JComboBox()
			{
				public static final long serialVersionUID = 1L;
				
				public JToolTip createToolTip()
				{
					return new JMultiLineToolTip();
				}
			};
			
			jAppearingDelayComboBox.setToolTipText("Appearing delay between each hostile of the same wave.\n"+
					"If only one hostile in the wave, delays the hostile.\n"+
					"Set to Custom, a custom value can be set");
			jAppearingDelayComboBox.setBounds(new Rectangle(121, 173, 122, 25));
			jAppearingDelayComboBox.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) 
				{
					update_appearing_delay();
				}
			});
		}
		return jAppearingDelayComboBox;
	}
	
	private JTextField getJObjectiveBalanceTextField() {
		if (jObjectiveBalanceTextField == null) {
			jObjectiveBalanceTextField = new JTextField()
			{
				public static final long serialVersionUID = 1L;
				
				public JToolTip createToolTip()
				{
					return new JMultiLineToolTip();
				}
			};
			jObjectiveBalanceTextField.setBounds(new Rectangle(250, 254, 40, 24));
			jObjectiveBalanceTextField.setToolTipText("100: maximum aggressivity");

			
			
		}
		return jObjectiveBalanceTextField;
	}
	/**
	 * This method initializes jNbEnemiesTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJNbEnemiesTextField() {
		if (jNbEnemiesTextField == null) {
			jNbEnemiesTextField = new JTextField()
			{
				public static final long serialVersionUID = 1L;
				
				public JToolTip createToolTip()
				{
					return new JMultiLineToolTip();
				}
			};
			jNbEnemiesTextField.setBounds(new Rectangle(107, 254, 40, 24));
			jNbEnemiesTextField.setToolTipText("Number of hostiles in the wave");

			
			
		}
		return jNbEnemiesTextField;
	}

	/**
	 * This method initializes jSpeedComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJMoveSpeedComboBox() {
		if (jSpeedComboBox == null) {
			jSpeedComboBox = new JComboBox()
			{
				public static final long serialVersionUID = 1L;
				
				public JToolTip createToolTip()
				{
					return new JMultiLineToolTip();
				}
			};
			jSpeedComboBox.setBounds(new Rectangle(91, 90, 179, 27));
			jSpeedComboBox.setToolTipText("hostile move speed.\nMost of hostiles move as \"normal\". Useless for non moving hostiles");
		}
		return jSpeedComboBox;
	}


	/**
	 * This method initializes jClassNameList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getJClassNameList() {
		if (jClassNameList == null) 
		{
			jClassNameList = new JList();
			jClassNameList.setBounds(new Rectangle(149, 413, 140, 114));
			MouseListener mouseListener = new MouseAdapter() {
			     public void mouseClicked(MouseEvent e) {
		             int index = jClassNameList.locationToIndex(e.getPoint());
			         if (e.getClickCount() == 2) 
			         {			        	 
			        	 GfxFrameSet go = (GfxFrameSet)ComboSelectorDialog.show((JFrame)(HostileWaveDialog.this.getParent()), 
			        			 m_data.level.get_level_palette().lookup_by_type(GfxFrameSet.BONUSES),
			        			 "Choose an existing class", "Class selection");
			     		        
			        		 ObjectToDrop o = m_data.object_to_drop;
			        		 if (o == null)
			        		 {
			        			 o = new ObjectToDrop();
			        			
			        		 }
			        		 o.class_name = go != null ? go.get_name() : null;
			        		 if (o.class_name == null)
			        		 {
			        			 o.instance_name = null;
			        		 }
			        		 
			        		 m_data.object_to_drop = o;
			        		 update_dropped_object_list();
			        	 
			          }
		           
			     }
			 };
			 jClassNameList.addMouseListener(mouseListener);
		}
		return jClassNameList;
	}

	/**
	 * This method initializes jInstanceNameList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getJInstanceNameList() {
		if (jInstanceNameList == null) {
			jInstanceNameList = new JList();
			jInstanceNameList.setBounds(new Rectangle(296, 414, 125, 114));
			MouseListener mouseListener = new MouseAdapter() {
			     public void mouseClicked(MouseEvent e) {
		             int index = jInstanceNameList.locationToIndex(e.getPoint());
			         if (e.getClickCount() == 2) 
			         {			        	 
			        	 // make list of hidden pickable
			        	 
			        	 Collection<GfxObject> cog = m_data.level.get_object_layer().get_items(GfxFrameSet.MISC_PICKABLE_ITEMS,false);
			        	 // add the teleport gem if hidden
			        	 m_data.level.get_object_layer().get_items(GfxFrameSet.Type.teleport_gem,cog, false);
			        	 
			        	 GfxObject go = (GfxObject)ComboSelectorDialog.show((JFrame)(HostileWaveDialog.this.getParent()),
			        			cog , "choose an instance", "Object drop instance selection");
			        	
			        		 ObjectToDrop o = m_data.object_to_drop;
			        		 if (o == null)
			        		 {
			        			 o = new ObjectToDrop();
			        		 }
		        			 o.instance_name = go != null ? go.get_name() : null;
		        			 if (o.instance_name != null)
		        			 {
		        				o.class_name = go.get_class_name(); 
		        			 }
			        		 m_data.object_to_drop = o;
			        		 update_dropped_object_list();
			        	 

			          }
			     }
			 };
			 jInstanceNameList.addMouseListener(mouseListener);		}
		return jInstanceNameList;
	}

	/**
	 * This method initializes jInstantCreationCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJInstantCreationCheckBox() {
		if (jInstantCreationCheckBox == null) {
			jInstantCreationCheckBox = new JCheckBox();
			jInstantCreationCheckBox.setBounds(new Rectangle(7, 339, 158, 28));
			jInstantCreationCheckBox.setToolTipText("If set, hostile appears at level creation. Useful for spikes, hives & snake pots");
			jInstantCreationCheckBox.setText("Create at level start");
		}
		return jInstantCreationCheckBox;
	}

	/**
	 * This method initializes jHealthPointsTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJHealthPointsTextField() {
		if (jHealthPointsTextField == null) {
			jHealthPointsTextField = new JTextField()
			{
				public static final long serialVersionUID = 1L;
				
				public JToolTip createToolTip()
				{
					return new JMultiLineToolTip();
				}
			};
			jHealthPointsTextField.setBounds(new Rectangle(106, 294, 54, 26));
			jHealthPointsTextField.setToolTipText("Number of basic shots required to kill the hostile.\n"+
					"The hero has 24 health points. Knife counts for 1, throwing star for 2, fireball for 5 ...");
		}
		return jHealthPointsTextField;
	}

	/**
	 * This method initializes jFireTypeComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJFireTypeComboBox() {
		if (jFireTypeComboBox == null) {
			jFireTypeComboBox = new JComboBox()
			{
				public static final long serialVersionUID = 1L;
				
				public JToolTip createToolTip()
				{
					return new JMultiLineToolTip();
				}
			};
			
			jFireTypeComboBox.setBounds(new Rectangle(92, 133, 175, 29));
			jFireTypeComboBox.setToolTipText("directional fire is mostly used by flying hostiles\nand some aggressive ground hostiles");
		}
		return jFireTypeComboBox;
	}

	/**
	 * This method initializes jHostileList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getJHostileNameList() {
		if (jHostileNameList == null) {
			jHostileNameList = new JList();
			jHostileNameList.setBounds(new Rectangle(427, 415, 114, 114));
			MouseListener mouseListener = new MouseAdapter() {
			     public void mouseClicked(MouseEvent e) {
		             int index = jHostileNameList.locationToIndex(e.getPoint());
			         if (e.getClickCount() == 2) 
			         {			        	 
			        	 ControlObject.Type [] hostile_list = { ControlObject.Type.Enemy };
			        	 
			        	ControlObject co = (ControlObject)ComboSelectorDialog.show((JFrame)(HostileWaveDialog.this.getParent()),
			        			 m_data.level.get_control_layer().get_items(hostile_list), "choose an hostile to spawn", "Hostile selection");
			        	
			        		 ObjectToDrop o = m_data.object_to_drop;
			        		 if (o == null)
			        		 {
			        			 o = new ObjectToDrop();
			        		 }
		        			 o.hostile_name = co != null ? co.get_name() : null;

			        		 m_data.object_to_drop = o;
			        		 update_dropped_object_list();

			          }
		 
			     }
			 };		
			 
			 jHostileNameList.addMouseListener(mouseListener);
		}
		return jHostileNameList;
	}

	/**
	 * This method initializes jShootSpeedComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJShootSpeedComboBox() {
		if (jShootSpeedComboBox == null) {
			jShootSpeedComboBox = new JComboBox()
			{
				public static final long serialVersionUID = 1L;
				
				public JToolTip createToolTip()
				{
					return new JMultiLineToolTip();
				}
			};
			jShootSpeedComboBox.setBounds(new Rectangle(378, 89, 157, 30));
			jShootSpeedComboBox.setToolTipText("speed of projectiles");
		}
		return jShootSpeedComboBox;
	}

	/**
	 * This method initializes jShootFrequencyComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJShootFrequencyComboBox() {
		if (jShootFrequencyComboBox == null) {
			jShootFrequencyComboBox = new JComboBox()
			{
				public static final long serialVersionUID = 1L;
				
				public JToolTip createToolTip()
				{
					return new JMultiLineToolTip();
				}
			};
			jShootFrequencyComboBox.setBounds(new Rectangle(382, 136, 163, 26));
			jShootFrequencyComboBox.setToolTipText("shoot period for hostiles that shoot. Useless for others");
		}
		return jShootFrequencyComboBox;
	}

	/**
	 * This method initializes jDirectionComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJDirectionComboBox() {
		if (jDirectionComboBox == null) {
			jDirectionComboBox = new JComboBox()
			{
				public static final long serialVersionUID = 1L;
				
				public JToolTip createToolTip()
				{
					return new JMultiLineToolTip();
				}
			};
			jDirectionComboBox.setBounds(new Rectangle(386, 173, 162, 27));
			jDirectionComboBox.setToolTipText("initial direction when appearing.\n"+
					"Random is normally for internal use (small snakes)");
		}
		return jDirectionComboBox;
	}

	/**
	 * This method initializes jAttackDistanceComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJAttackDistanceComboBox() {
		if (jAttackDistanceComboBox == null) {
			jAttackDistanceComboBox = new JComboBox()
			{
				public static final long serialVersionUID = 1L;
				
				public JToolTip createToolTip()
				{
					return new JMultiLineToolTip();
				}
			};
			jAttackDistanceComboBox.setBounds(new Rectangle(420, 211, 120, 23));
			jAttackDistanceComboBox.setToolTipText("affects hostile A.I.\n"+
					"None means basic behaviour unless a trajectory is defined\n"+
					"Close: means if the hero moves close to the hostilehe heads towards him\n"+
					"Closer: same thing but the hero must be very close\n"+
					"Always is a direct attack no matter the distance\n\n"+
					"Useful for ground monsters only");
		}
		return jAttackDistanceComboBox;
	}


	/**
	 * This method initializes jStealOnTheWayCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJStealOnTheWayCheckBox() {
		if (jStealOnTheWayCheckBox == null) {
			jStealOnTheWayCheckBox = new JCheckBox() {
					public static final long serialVersionUID = 1L;
					
					public JToolTip createToolTip()
					{
						return new JMultiLineToolTip();
					}
				};
			jStealOnTheWayCheckBox.setBounds(new Rectangle(10, 430, 143, 26));
			jStealOnTheWayCheckBox.setToolTipText("Not really a thief but picks up anything\nthat falls its way");
			jStealOnTheWayCheckBox.setText("Steal on the way");
		}
		return jStealOnTheWayCheckBox;
	}

	/**
	 * This method initializes jAvoidShotsCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJAvoidShotsCheckBox() {
		if (jAvoidShotsCheckBox == null) {
			jAvoidShotsCheckBox = new JCheckBox()
			{
				public static final long serialVersionUID = 1L;
				
				public JToolTip createToolTip()
				{
					return new JMultiLineToolTip();
				}
			};
			jAvoidShotsCheckBox.setBounds(new Rectangle(10, 372, 126, 30));
			jAvoidShotsCheckBox.setToolTipText("smart trajectory (flying monsters only) to avoid player shots");
			jAvoidShotsCheckBox.setText("Avoid shots");
		}
		return jAvoidShotsCheckBox;
	}

	/**
	 * This method initializes jJumpHeightComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJJumpHeightComboBox() {
		if (jJumpHeightComboBox == null) {
			jJumpHeightComboBox = new JComboBox();
			jJumpHeightComboBox.setBounds(new Rectangle(420, 247, 130, 27));
		}
		return jJumpHeightComboBox;
	}

	/**
	 * This method initializes jJumpWidthComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJJumpThresholdComboBox() {
		if (jJumpThresholdComboBox == null) {
			jJumpThresholdComboBox = new JComboBox();
			jJumpThresholdComboBox.setBounds(new Rectangle(420, 285, 130, 25));
		}
		return jJumpThresholdComboBox;
	}

	/**
	 * This method initializes jJumpWidthComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJJumpWidthComboBox() {
		if (jJumpWidthComboBox == null) {
			jJumpWidthComboBox = new JComboBox();
			jJumpWidthComboBox.setBounds(new Rectangle(420, 324, 130, 25));
		}
		return jJumpWidthComboBox;
	}




}  //  @jve:decl-index=0:visual-constraint="10,10"
