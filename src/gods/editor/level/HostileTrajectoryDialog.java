package gods.editor.level;

import gods.base.*;

import javax.swing.JPanel;
import java.awt.*;
import gods.editor.DataModifier;
import gods.sys.AngleUtils;

import javax.swing.JDialog;
import javax.swing.JList;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import java.util.*;
import java.awt.Rectangle;


public class HostileTrajectoryDialog extends JDialog implements
		DataModifier<HostileTrajectory> 
{
	private HostileTrajectory m_data = null;
	private LevelData m_level_data;
	
	private static final ControlObject.Type [] CANDIDATES = { ControlObject.Type.Enemy_Trigger,
		ControlObject.Type.Misc };
	
	public void data_to_gui() 
	{
		Collection<ControlObject> col = m_level_data.get_control_layer().get_items(CANDIDATES);
		
		for (ControlObject co : col)
		{
			getJLocationComboBox().addItem(co);
		}
		
		getJAngleTextField().setText(""+m_data.get_initial_angle());
		getJVerticalOscillationTextField().setText(""+m_data.get_vertical_oscillation_speed());
		
		update_location_combo();
		
		update_list();
		
	}

	public HostileTrajectory get_data() {
		return m_data;
	}

	public void gui_to_data() 
	{
		m_data.set_initial_angle(Integer.parseInt(getJAngleTextField().getText()));
		m_data.set_vertical_oscillation_speed(Integer.parseInt(getJVerticalOscillationTextField().getText()));
	}

	public void on_close() 
	{
		try
		{
			gui_to_data();
			dispose();
		}
		catch (NumberFormatException e)
		{
			
		}
	}

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JList jSegmentList = null;

	private JButton jButton = null;

	private JButton jAddSegmentButton = null;

	private JComboBox jLocationComboBox = null;

	private JComboBox jTypeComboBox = null;

	private JTextField jDurationTextField = null;

	private JLabel jLabel = null;
	private JTextField jCurrentSegmentTextField = null;
	private JLabel jLabel1 = null;
	private JButton jReplaceSegmentButton = null;
	private JButton jDeleteButton = null;
	private JButton jUpButton = null;
	private JButton jDownButton = null;
	private JLabel jLabel2 = null;
	private Canvas jDirectionPanel = null;
	private JTextField jAngleTextField = null;
	private JLabel jLabel3 = null;
	private JTextField jVerticalOscillationTextField = null;

	public HostileTrajectoryDialog(Frame owner, HostileTrajectory data, LevelData ld, String hostile_name)
	{
		super(owner);
		m_data = data;
		m_level_data = ld;
		initialize();
		setTitle("Edit trajectory for "+hostile_name);
		data_to_gui();
	}
	/**
	 * @param owner
	 */
	public HostileTrajectoryDialog(Frame owner) {
		super(owner);
		initialize();
	}

	private void load_trajectory_segment(TrajectorySegment ts)
	{
		if (ts != null)
		{
			getJTypeComboBox().setSelectedItem(ts.type);
			update_location_combo();
			getJLocationComboBox().setSelectedItem(ts.location);
			getJDurationTextField().setText(""+ts.duration);		
			getJCurrentSegmentTextField().setText(ts.toString());
		}
		else
		{
			getJTypeComboBox().setSelectedItem(null);
			update_location_combo();
			getJLocationComboBox().setSelectedItem(null);
			getJDurationTextField().setText("0");		
			getJCurrentSegmentTextField().setText("");
		}
		
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(540, 344);
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel3 = new JLabel();
			jLabel3.setBounds(new Rectangle(210, 240, 64, 21));
			jLabel3.setToolTipText("vertical oscillation speed. 0 for no oscillation");
			jLabel3.setText("oscillation");
			jLabel2 = new JLabel();
			jLabel2.setBounds(new Rectangle(211, 206, 66, 24));
			jLabel2.setText("initial angle");
			jLabel1 = new JLabel();
			jLabel1.setBounds(new Rectangle(231, 11, 51, 30));
			jLabel1.setText("current");
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(343, 141, 82, 30));
			jLabel.setText("duration");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getJSegmentList(), null);
			make_scrollable(jSegmentList);
			jContentPane.add(getJButton(), null);
			jContentPane.add(getJAddSegmentButton(), null);
			jContentPane.add(getJLocationComboBox(), null);
			jContentPane.add(getJTypeComboBox(), null);
			jContentPane.add(getJDurationTextField(), null);
			jContentPane.add(jLabel, null);
			jContentPane.add(getJCurrentSegmentTextField(), null);
			jContentPane.add(jLabel1, null);
			jContentPane.add(getJReplaceSegmentButton(), null);
			jContentPane.add(getJDeleteButton(), null);
			jContentPane.add(getJUpButton(), null);
			jContentPane.add(getJDownButton(), null);
			jContentPane.add(jLabel2, null);
			jContentPane.add(getJDirectionCanvas(), null);
			jContentPane.add(getJAngleTextField(), null);
			jContentPane.add(jLabel3, null);
			jContentPane.add(getJVerticalOscillationTextField(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jSegmentList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getJSegmentList() {
		if (jSegmentList == null) {
			jSegmentList = new JList();
			jSegmentList.setBounds(new Rectangle(12, 12, 181, 255));
			
			MouseListener mouseListener = new MouseAdapter() {
			     public void mouseClicked(MouseEvent e) 
			     {
			    	 //int index = jSegmentList.locationToIndex(e.getPoint());
			    	 switch (e.getButton())
			    	 {
			    	 case MouseEvent.BUTTON1:
			    	 {
			    		 // load into right widgets
			    		 TrajectorySegment s = get_selected_segment();
			    		 if (s != null)
			    		 {
			    			 load_trajectory_segment(s);
			    		 }
			    	 }
			    	 break;	
			    	 }
			     }
			};
			jSegmentList.addMouseListener(mouseListener);

		}		
		return jSegmentList;
	}
	private void make_scrollable(JList l)
	{
		JScrollPane scrollPane = new JScrollPane(l);
		scrollPane.setBounds(l.getBounds());
		jContentPane.add(scrollPane,null);
	}
	private void add_segment()
	{
		TrajectorySegment ts = new TrajectorySegment();
		set_segment(ts);
		m_data.add(ts);
		update_list();
		load_trajectory_segment(ts);
		
	}
	
	private void set_segment(TrajectorySegment ts)
	{
		ControlObject co = null;
		HostileTrajectory.Type t = (HostileTrajectory.Type)getJTypeComboBox().getSelectedItem();
		
		if (TrajectorySegment.to_location(t))
		{
			co = (ControlObject)getJLocationComboBox().getSelectedItem();
		}
		
		int duration = -1;
		
		try {
			duration = Integer.parseInt(getJDurationTextField().getText());
		} catch (NumberFormatException e) {
		}
		
		if (duration >= 0)
		{
			ts.duration = duration;
			ts.location = co;
			ts.type = t;
			load_trajectory_segment(ts);
			
		}
		
	}

	private void update_list()
	{
		Object [] items = new Object[m_data.items().size()];
		int i = 0;
		for (Object o : m_data.items())
		{
			items[i++] = o;
		}
		getJSegmentList().setListData(items);
	}
	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setBounds(new Rectangle(193, 282, 122, 27));
			jButton.setText("OK");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					on_close();
				}
			});
		}
		return jButton;
	}
	private void update_location_combo()
	{
		HostileTrajectory.Type t = (HostileTrajectory.Type)getJTypeComboBox().getSelectedItem();
		getJLocationComboBox().setEnabled(TrajectorySegment.to_location(t));
	}
	

	/**
	 * This method initializes jAddSegmentButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJAddSegmentButton() {
		if (jAddSegmentButton == null) {
			jAddSegmentButton = new JButton();
			jAddSegmentButton.setBounds(new Rectangle(210, 95, 74, 20));
			jAddSegmentButton.setText("Add <<");
			jAddSegmentButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) 
				{
					add_segment();
				}
			});
		}
		return jAddSegmentButton;
	}

	/**
	 * This method initializes jLocationComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJLocationComboBox() {
		if (jLocationComboBox == null) {
			jLocationComboBox = new JComboBox();
			jLocationComboBox.setBounds(new Rectangle(306, 100, 217, 24));
			
					
		}
		return jLocationComboBox;
	}

	/**
	 * This method initializes jTypeComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJTypeComboBox() {
		if (jTypeComboBox == null) {
			jTypeComboBox = new JComboBox();
			jTypeComboBox.setBounds(new Rectangle(305, 64, 216, 26));
			jTypeComboBox.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) 
				{
					update_location_combo();
				}
			});
			
			for (HostileTrajectory.Type t : HostileTrajectory.Type.values())
			{
				jTypeComboBox.addItem(t);
			}
		}
		return jTypeComboBox;
	}

	/**
	 * This method initializes jDurationTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJDurationTextField() {
		if (jDurationTextField == null) {
			jDurationTextField = new JTextField();
			jDurationTextField.setBounds(new Rectangle(445, 142, 79, 28));
			jDurationTextField.setText("0");
		}
		return jDurationTextField;
	}

	/**
	 * This method initializes jCurrentSegmentTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJCurrentSegmentTextField() {
		if (jCurrentSegmentTextField == null) {
			jCurrentSegmentTextField = new JTextField();
			jCurrentSegmentTextField.setBounds(new Rectangle(289, 13, 230, 26));
			jCurrentSegmentTextField.setEditable(false);
		}
		return jCurrentSegmentTextField;
	}

	/**
	 * This method initializes jReplaceSegmentButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJReplaceSegmentButton() {
		if (jReplaceSegmentButton == null) {
			jReplaceSegmentButton = new JButton();
			jReplaceSegmentButton.setBounds(new Rectangle(209, 68, 74, 21));
			jReplaceSegmentButton.setText("Set <<");
			jReplaceSegmentButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					TrajectorySegment ht = get_selected_segment();
					
					if (ht != null)
					{
						set_segment(ht);
						update_list();
					}
				}
			});
		}
		return jReplaceSegmentButton;
	}

	/**
	 * This method initializes jDeleteButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJDeleteButton() {
		if (jDeleteButton == null) {
			jDeleteButton = new JButton();
			jDeleteButton.setBounds(new Rectangle(210, 122, 72, 20));
			jDeleteButton.setText("<< Del");
			jDeleteButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) 
				{
					TrajectorySegment ts = get_selected_segment();
					if (ts != null)
					{
						m_data.remove(ts);
						update_list();
					}
				}
			});
		}
		return jDeleteButton;
	}

	private TrajectorySegment get_selected_segment()
	{
		return (TrajectorySegment)getJSegmentList().getSelectedValue();
	}
	
	private void move_selected_item(boolean down)
	{
		TrajectorySegment ts =	get_selected_segment();
		
		if (ts != null)
		{
			m_data.move(ts,down);
		
			update_list();
		}
		
	}
	/**
	 * This method initializes jUpButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJUpButton() {
		if (jUpButton == null) {
			jUpButton = new JButton();
			jUpButton.setBounds(new Rectangle(210, 152, 72, 20));
			jUpButton.setText("<< Up");
			jUpButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					move_selected_item(false);
					}
			});
		}
		return jUpButton;
	}

	/**
	 * This method initializes jDownButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJDownButton() {
		if (jDownButton == null) {
			jDownButton = new JButton();
			jDownButton.setBounds(new Rectangle(210, 179, 90, 20));
			jDownButton.setText("<< Down");
			jDownButton.addActionListener(new java.awt.event.ActionListener() 
			{
				public void actionPerformed(java.awt.event.ActionEvent e) {
					move_selected_item(true);
				}
			});
		}
		return jDownButton;
	}

	private class DirectionCanvas extends Canvas
	{
		private static final long serialVersionUID = 1L;
		public void paint(Graphics g)
		{
			try {
				int f = Integer.parseInt(getJAngleTextField().getText());
				int radius = 6;
				int px = getWidth() / 2;
				int py = getHeight() / 2;
				
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(0,0,getWidth(),getHeight());
				
				g.setColor(Color.BLUE);
				
				g.fillArc(px  - radius, py - radius, radius * 2, radius * 2, 0, 360);
				
				int dx = (int)(px + getWidth() * AngleUtils.cosd(f));
				int dy = (int)(py + getWidth() * AngleUtils.sind(f));
				
				g.drawLine(px,py,dx,dy);
				
			} catch (NumberFormatException e) 
			{
				
			}
			
		}
	}
	
	/**
	 * This method initializes jDirectionPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private Canvas getJDirectionCanvas() {
		if (jDirectionPanel == null) {
			jDirectionPanel = new DirectionCanvas();
			jDirectionPanel.setBounds(new Rectangle(345, 183, 149, 121));
		}
		return jDirectionPanel;
	}

	/**
	 * This method initializes jAngleTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJAngleTextField() {
		if (jAngleTextField == null) {
			jAngleTextField = new JTextField();
			jAngleTextField.setBounds(new Rectangle(286, 205, 55, 28));
			jAngleTextField.setText("0");
			jAngleTextField.addCaretListener(new javax.swing.event.CaretListener() {
				public void caretUpdate(javax.swing.event.CaretEvent e) 
				{
					getJDirectionCanvas().repaint();
				}
			});
		}
		return jAngleTextField;
	}

	/**
	 * This method initializes jVerticalOscillationTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJVerticalOscillationTextField() {
		if (jVerticalOscillationTextField == null) {
			jVerticalOscillationTextField = new JTextField();
			jVerticalOscillationTextField.setBounds(new Rectangle(285, 241, 55, 20));
			jVerticalOscillationTextField.setText("0");
		}
		return jVerticalOscillationTextField;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
