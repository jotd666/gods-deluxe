package gods.editor.level;

import javax.swing.JPanel;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JLabel;
import java.awt.Rectangle;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JComboBox;

import gods.base.ControlObject;
import gods.base.layer.ControlObjectLayer;
import gods.editor.DataModifier;
import gods.editor.EvalSimpleExpresssion;

import javax.swing.JCheckBox;
import java.util.Observable;
import java.util.Observer;

public class ControlObjectDialog extends JDialog implements DataModifier<ControlObject> {

	public void data_to_gui() 
	{
		getJTypeComboBox().setSelectedIndex(m_control_object.get_type().ordinal());
		getJNameTextField().setText(m_control_object.get_name());
		getJNameTextField().selectAll();
		getJXTextField().setText(m_control_object.get_x()+"");
		getJYTextField().setText(m_control_object.get_y()+"");
		getJHeightTextField().setText(m_control_object.get_height()+"");
		getJWidthTextField().setText(m_control_object.get_width()+"");
		getJActivatedCheckBox().setSelected(m_control_object.is_visible());
	}

	public ControlObject get_data() 
	{
		return m_control_object;
	}

	public void gui_to_data() throws Exception
	{
		String new_name = getJNameTextField().getText();

		if ( !new_name.equals(m_control_object.get_name()) && 
				(m_control_object_layer.get(new_name) != null))
		{
			getJNameTextField().setText(m_control_object.get_name());
			throw new Exception("Name \""+new_name+"\" is already used");
		}
		else
		{
			if ((new_name.equals("")) &&
					!ControlObject.can_be_unnamed(m_control_object.get_type()))
			{
				getJNameTextField().setText(m_control_object.get_name());
				throw new Exception("Object of this type must have a name");
			}
			m_control_object.set_name(new_name);
		}
		
		m_control_object.set_type(getJTypeComboBox().getSelectedIndex());

		m_control_object.set_width(EvalSimpleExpresssion.evaluate(getJWidthTextField().getText()));
		m_control_object.set_height(EvalSimpleExpresssion.evaluate(getJHeightTextField().getText()));
		m_control_object.set_x(EvalSimpleExpresssion.evaluate(getJXTextField().getText()));
		m_control_object.set_y(EvalSimpleExpresssion.evaluate(getJYTextField().getText()));

		m_control_object.set_visible(getJActivatedCheckBox().isSelected());
		
	}

	private class EditObs extends Observable
	{
		void signal()
		{
			setChanged();
			notifyObservers(ControlObjectDialog.this);
		}
	}
	
	private EditObs m_observable = new EditObs();  //  @jve:decl-index=0:
	
	public void on_close() 
	{
		try
		{
			gui_to_data();

					
				m_observable.signal();
				dispose();
		
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(this, e.getMessage(), 
					"User input error", 
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JLabel jLabel = null;

	private JTextField jNameTextField = null;

	private JButton jButton = null;

	private JComboBox jTypeComboBox = null;

	private JLabel jLabel1 = null;

	private ControlObject m_control_object;  //  @jve:decl-index=0:

	private JTextField jWidthTextField = null;
	private JTextField jHeightTextField = null;
	private JTextField jXTextField = null;
	private JTextField jYTextField = null;

	private JLabel jLabel2 = null;
	private JLabel jLabel3 = null;
	private JLabel jLabel4 = null;
	private JLabel jLabel5 = null;
	
	private ControlObjectLayer m_control_object_layer;

	private JCheckBox jActivatedCheckBox = null;
	
	public ControlObjectDialog(Frame owner, ControlObject co, ControlObjectLayer col, Observer obs) 
	{
		super(owner);
		m_control_object = co;
		m_control_object_layer = col;
		m_observable.addObserver(obs);
		initialize();
	}
	/**
	 * @param owner
	 */
	public ControlObjectDialog(Frame owner) {
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(390, 300);
		this.setTitle("Edit control object");
		this.setContentPane(getJContentPane());
		
		
		if (m_control_object != null)
		{
			for (ControlObject.Type tt : ControlObject.Type.values())
			{
				getJTypeComboBox().addItem(tt.toString());
			}
			
			data_to_gui();
		}
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel3 = new JLabel();
			jLabel3.setBounds(new Rectangle(27, 120, 40, 27));
			jLabel3.setText("width");
			jLabel2 = new JLabel();
			jLabel2.setBounds(new Rectangle(221, 120, 45, 25));
			jLabel2.setText("height");
			jLabel4 = new JLabel();
			jLabel4.setBounds(new Rectangle(27, 160, 40, 27));
			jLabel4.setText("x");
			jLabel5 = new JLabel();
			jLabel5.setBounds(new Rectangle(221, 160, 45, 25));
			jLabel5.setText("y");
			jLabel1 = new JLabel();
			jLabel1.setBounds(new Rectangle(16, 73, 56, 31));
			jLabel1.setText("type");
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(14, 19, 54, 28));
			jLabel.setText("name");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(jLabel, null);
			jContentPane.add(getJNameTextField(), null);
			jContentPane.add(getJButton(), null);
			jContentPane.add(getJTypeComboBox(), null);
			jContentPane.add(jLabel1, null);
			jContentPane.add(getJWidthTextField(), null);
			jContentPane.add(getJHeightTextField(), null);
			jContentPane.add(getJXTextField(), null);
			jContentPane.add(getJYTextField(), null);
			jContentPane.add(jLabel2, null);
			jContentPane.add(jLabel3, null);
			jContentPane.add(jLabel4, null);
			jContentPane.add(jLabel5, null);
			jContentPane.add(getJActivatedCheckBox(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jNameTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJNameTextField() {
		if (jNameTextField == null) {
			jNameTextField = new JTextField();
			jNameTextField.setBounds(new Rectangle(80, 17, 270, 31));
			jNameTextField.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					on_close();
				}
			});
		}
		return jNameTextField;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setBounds(new Rectangle(157, 240, 78, 33));
			jButton.setText("OK");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) 
				{
					on_close();					
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jTypeComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJTypeComboBox() {
		if (jTypeComboBox == null) {
			jTypeComboBox = new JComboBox();
			jTypeComboBox.setBounds(new Rectangle(88, 75, 260, 30));
		}
		return jTypeComboBox;
	}

	/**
	 * This method initializes jWidthTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJWidthTextField() {
		if (jWidthTextField == null) {
			jWidthTextField = new JTextField();
			jWidthTextField.setBounds(new Rectangle(92, 120, 62, 28));
		}
		return jWidthTextField;
	}

	/**
	 * This method initializes jHeightTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJHeightTextField() {
		if (jHeightTextField == null) {
			jHeightTextField = new JTextField();
			jHeightTextField.setBounds(new Rectangle(276, 120, 62, 28));
		}
		return jHeightTextField;
	}
	/**
	 * This method initializes jWidthTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJXTextField() {
		if (jXTextField == null) {
			jXTextField = new JTextField();
			jXTextField.setBounds(new Rectangle(92, 160, 62, 28));
		}
		return jXTextField;
	}

	/**
	 * This method initializes jHeightTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJYTextField() {
		if (jYTextField == null) {
			jYTextField = new JTextField();
			jYTextField.setBounds(new Rectangle(275, 160, 62, 28));
		}
		return jYTextField;
	}
	/**
	 * This method initializes jActivatedCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJActivatedCheckBox() {
		if (jActivatedCheckBox == null) {
			jActivatedCheckBox = new JCheckBox();
			jActivatedCheckBox.setBounds(new Rectangle(20, 200, 209, 21));
			jActivatedCheckBox.setText("activated (AKA visible)");
		}
		return jActivatedCheckBox;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
