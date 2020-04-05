package gods.editor.palette;


import java.awt.Frame;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JDialog;

import java.util.*;

import gods.base.AnimatedFrames;
import gods.base.GfxFrameSet;
import gods.editor.DataModifier;
import gods.editor.ViewGfxFrames;
import gods.sys.MiscUtils;

import javax.swing.JLabel;
import java.awt.Rectangle;

import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JButton;

public class EditGfxFrameSet extends JDialog implements DataModifier<GfxFrameSet> 
{
	private static GfxFrameSet.Type m_last_combo_index = GfxFrameSet.Type.other;
	
	private class EditObs extends Observable
	{
		void signal()
		{
			setChanged();
			notifyObservers(EditGfxFrameSet.this);
		}
	}
	
	private BufferedImage m_source_2x = null;
	
	private boolean m_is_new_frame = false;
	
	private EditObs m_observable = new EditObs();  //  @jve:decl-index=0:
	
	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private GfxFrameSet m_data = null;

	private JLabel jLabel = null;

	private JTextField m_name_tf = null;

	private JComboBox m_type_cbx = null;

	private JLabel jLabel1 = null;

	private JButton m_ok_button = null;
	
	private JLabel jLabel2 = null;

	private JTextField m_nb_frames_tf = null;

	private JButton m_view_frames_button = null;

	private JTextField jDescriptionTextField = null;

	private JTextField jAliasTextField = null;

	private JLabel jLabel3 = null;

	private JLabel jLabel4 = null;

	private JLabel jLabel5 = null;

	private JTextField jValueTextField = null;

	private JLabel jLabel6 = null;

	private JTextField jPointsTextField = null;

	private JLabel jLabel7 = null;

	private JComboBox jAnimationTypeComboBox = null;

	private JLabel jLabel8 = null;

	private JTextField jDimensionTextField = null;
	private JCheckBox jSymCheckBox = null;
	
	/**
	 * This is the default constructor
	 */
	public EditGfxFrameSet() {
		this(null,(GfxFrameSet)null,null,null,true);
	}
	
	public boolean is_new()
	{
		return m_is_new_frame;
	}
	
	
	public EditGfxFrameSet(Frame parent,GfxFrameSet edited,Observer obs,BufferedImage source_2x, boolean is_new_frame) 
	{
		super(parent);
		m_observable.addObserver(obs);
		m_data = edited;
		m_source_2x = source_2x;
		m_is_new_frame = is_new_frame;
		initialize();
		if (m_data != null)
		{
			setTitle("Edit "+m_data.get_name()+" frame set");
		}
	}

	public GfxFrameSet get_data()
	{
		return m_data;
	}
	
	public void on_close()
	{
		try 
		{
			gui_to_data();

			dispose();
			m_observable.signal();
			
		} catch (Exception e1) 
		{
			MiscUtils.show_error_dialog(this, "Input error", e1.getMessage());
		}
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() 
	{
		this.setSize(525, 316);
		this.setContentPane(getJContentPane());
		getJContentPane().setSize(getSize());
		this.setTitle("JFrame");
		
		for (GfxFrameSet.Type t : GfxFrameSet.Type.values())
		{
			m_type_cbx.addItem(t);
		}
		
		for (AnimatedFrames.Type t : AnimatedFrames.Type.values())
		{
			getJAnimationTypeComboBox().addItem(t);
		}
		data_to_gui();
	}


	public void data_to_gui()
	{
		m_name_tf.setText(m_data.get_name());
		if (!m_is_new_frame)
		{
			m_type_cbx.setSelectedItem(m_data.get_type());
		}
		else
		{
			m_type_cbx.setSelectedItem(m_last_combo_index);
		}
		
		getJDimensionTextField().setText(m_data.get_width()+" x "+m_data.get_height());
		
		m_nb_frames_tf.setText(m_data.get_nb_frames()+"");
		m_view_frames_button.setEnabled(m_data.get_nb_frames() > 1);
		GfxFrameSet.Properties p = m_data.get_properties();
		
		getJAnimationTypeComboBox().setSelectedItem(p.animation_type);
		getJAliasTextField().setText(p.alias);
		getJPointsTextField().setText(p.get_points()+"");
		getJDescriptionTextField().setText(p.description);
		getJValueTextField().setText(p.value+"");
		jSymCheckBox.setSelected(p.swap_left_right);
	}
	public void gui_to_data() throws Exception
	{
		m_last_combo_index = (GfxFrameSet.Type)get_type_cbx().getSelectedItem();

		m_data.set_name(m_name_tf.getText());
		if (m_data.get_name().length() == 0)
		{
			throw new Exception("Invalid name");
		}
		m_data.set_type(m_last_combo_index);	
		
		GfxFrameSet.Properties p = m_data.get_properties();
		
		p.alias = getJAliasTextField().getText();
		p.description = getJDescriptionTextField().getText();			
		p.value = Integer.parseInt(getJValueTextField().getText());
		p.animation_type = (AnimatedFrames.Type)getJAnimationTypeComboBox().getSelectedItem();
		p.swap_left_right = jSymCheckBox.isSelected();
	}
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel8 = new JLabel();
			jLabel8.setBounds(new Rectangle(328, 60, 76, 26));
			jLabel8.setText("dimension");
			jLabel7 = new JLabel();
			jLabel7.setBounds(new Rectangle(16, 206, 113, 23));
			jLabel7.setText("animation type");
			jLabel6 = new JLabel();
			jLabel6.setBounds(new Rectangle(217, 164, 52, 26));
			jLabel6.setText("points");
			jLabel5 = new JLabel();
			jLabel5.setBounds(new Rectangle(16, 163, 48, 25));
			jLabel5.setText("value");
			jLabel4 = new JLabel();
			jLabel4.setBounds(new Rectangle(205, 115, 67, 24));
			jLabel4.setText("description");
			jLabel3 = new JLabel();
			jLabel3.setBounds(new Rectangle(13, 116, 40, 24));
			jLabel3.setText("alias");
			jLabel2 = new JLabel();
			jLabel2.setBounds(new Rectangle(327, 15, 116, 22));
			jLabel2.setText("number of frames");
			jLabel1 = new JLabel();
			jLabel1.setBounds(new Rectangle(14, 65, 56, 16));
			jLabel1.setText("type");
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(14, 15, 53, 24));
			jLabel.setText("name");
			jSymCheckBox = new JCheckBox();
			jSymCheckBox.setBounds(new Rectangle(380, 166, 116, 22));
			jSymCheckBox.setText("swap left/right");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(jLabel, null);
			jContentPane.add(get_name_tf(), null);
			jContentPane.add(get_type_cbx(), null);
			jContentPane.add(jLabel1, null);
			jContentPane.add(get_ok_button(), null);
			jContentPane.add(jLabel2, null);
			jContentPane.add(get_nb_frames_tf(), null);
			jContentPane.add(getM_view_frames_button(), null);
			jContentPane.add(getJDescriptionTextField(), null);
			jContentPane.add(getJAliasTextField(), null);
			jContentPane.add(jLabel3, null);
			jContentPane.add(jLabel4, null);
			jContentPane.add(jLabel5, null);
			jContentPane.add(getJValueTextField(), null);
			jContentPane.add(jLabel6, null);
			jContentPane.add(getJPointsTextField(), null);
			jContentPane.add(jLabel7, null);
			jContentPane.add(getJAnimationTypeComboBox(), null);
			jContentPane.add(jLabel8, null);
			jContentPane.add(getJDimensionTextField(), null);
			jContentPane.add(jSymCheckBox,null);
		}
		return jContentPane;
	}
	/**
	 * This method initializes m_name_tf	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField get_name_tf() {
		if (m_name_tf == null) {
			m_name_tf = new JTextField();
			m_name_tf.setBounds(new Rectangle(89, 16, 225, 25));
			m_name_tf.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					on_close();
				}
			});
		}
		return m_name_tf;
	}
	/**
	 * This method initializes m_type_cbx	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox get_type_cbx() {
		if (m_type_cbx == null) {
			m_type_cbx = new JComboBox();
			m_type_cbx.setBounds(new Rectangle(91, 59, 223, 30));
		}
		return m_type_cbx;
	}
	/**
	 * This method initializes m_ok_button	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton get_ok_button() {
		if (m_ok_button == null) {
			m_ok_button = new JButton();
			m_ok_button.setBounds(new Rectangle(221, 258, 79, 24));
			m_ok_button.setText("OK");
			m_ok_button.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					on_close();
						
					
				}
			});
		}
		return m_ok_button;
	}
	/**
	 * This method initializes m_nb_frames_tf	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField get_nb_frames_tf() {
		if (m_nb_frames_tf == null) {
			m_nb_frames_tf = new JTextField();
			m_nb_frames_tf.setBounds(new Rectangle(457, 18, 37, 22));
			m_nb_frames_tf.setEditable(false);
		}
		return m_nb_frames_tf;
	}
	/**
	 * This method initializes m_view_frames_button	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getM_view_frames_button() {
		if (m_view_frames_button == null) {
			m_view_frames_button = new JButton();
			m_view_frames_button.setBounds(new Rectangle(383, 210, 117, 25));
			m_view_frames_button.setText("View Frames");
			m_view_frames_button.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					ViewGfxFrames vfg = new ViewGfxFrames((Frame)getParent(),m_data,m_source_2x,500,500);
					vfg.setVisible(true);
				}
			});
		}
		return m_view_frames_button;
	}

	/**
	 * This method initializes jDescriptionTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJDescriptionTextField() {
		if (jDescriptionTextField == null) {
			jDescriptionTextField = new JTextField();
			jDescriptionTextField.setBounds(new Rectangle(282, 116, 227, 26));
		}
		return jDescriptionTextField;
	}

	/**
	 * This method initializes jAliasTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJAliasTextField() {
		if (jAliasTextField == null) {
			jAliasTextField = new JTextField();
			jAliasTextField.setBounds(new Rectangle(68, 116, 123, 25));
		}
		return jAliasTextField;
	}

	/**
	 * This method initializes jValueTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJValueTextField() {
		if (jValueTextField == null) {
			jValueTextField = new JTextField();
			jValueTextField.setBounds(new Rectangle(77, 162, 125, 28));
		}
		return jValueTextField;
	}

	/**
	 * This method initializes jPointsTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJPointsTextField() {
		if (jPointsTextField == null) {
			jPointsTextField = new JTextField();
			jPointsTextField.setBounds(new Rectangle(285, 166, 72, 28));
			jPointsTextField.setEditable(false);
		}
		return jPointsTextField;
	}

	/**
	 * This method initializes jAnimationTypeComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJAnimationTypeComboBox() {
		if (jAnimationTypeComboBox == null) {
			jAnimationTypeComboBox = new JComboBox();
			jAnimationTypeComboBox.setBounds(new Rectangle(146, 209, 181, 24));
		}
		return jAnimationTypeComboBox;
	}

	/**
	 * This method initializes jDimensionTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJDimensionTextField() {
		if (jDimensionTextField == null) {
			jDimensionTextField = new JTextField();
			jDimensionTextField.setBounds(new Rectangle(420, 62, 88, 23));
			jDimensionTextField.setEditable(false);
		}
		return jDimensionTextField;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
