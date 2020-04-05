package gods.editor.level;


import gods.base.LevelData;
import gods.base.layer.TileGrid;
import gods.editor.DataModifier;
import gods.editor.EvalSimpleExpresssion;

import javax.swing.JPanel;
import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JButton;
import java.awt.Rectangle;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;

public class LevelResizeDialog extends JDialog implements DataModifier<TileGrid>
{
	private class EditObs extends Observable
	{
		void signal()
		{
			setChanged();
			notifyObservers(LevelResizeDialog.this);
		}
	}
	public void data_to_gui() 
	{
		if (m_data.get_grid() != null)
		{
			getJColumnsTextField().setText(m_data.get_grid().get_nb_cols()+"");
			getJRowsTextField().setText(m_data.get_grid().get_nb_rows()+"");
		}
		set_current_dimensions();
	}

	public TileGrid get_data() 
	{
		
		return m_data.get_grid();
	}

	public void gui_to_data() 
	{
		
	}

	public void on_close() 
	{
		try {
			
			int	nb_rows = EvalSimpleExpresssion.evaluate(getJRowsTextField().getText());
			int	nb_cols = EvalSimpleExpresssion.evaluate(getJColumnsTextField().getText());
			
			boolean on_left = getJAddLeftCheckBox().isSelected();
			boolean on_top = getJAddTopCheckBox().isSelected();
			
			m_data.set_dimension(nb_rows, nb_cols, on_left,on_top);			
			
			
			dispose();
			m_observable.signal();
		
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
	
	private EditObs m_observable = new EditObs();  //  @jve:decl-index=0:

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JButton jOkButton = null;

	private JLabel jLabel = null;

	private JLabel jLabel1 = null;

	private JTextField jRowsTextField = null;

	private JTextField jColumnsTextField = null;

	private JTextField jCurrentDimensionTextField = null;

	private LevelData m_data;

	private JCheckBox jAddLeftCheckBox = null;

	private JCheckBox jAddRowsCheckBox = null;
	
	/**
	 * @param owner
	 */
	public LevelResizeDialog(Frame owner) 
	{
		this(owner,null,null);
	}
	
	public LevelResizeDialog(Frame owner, Observer obs, LevelData data) 
	{
		super(owner);
		m_data = data;
		m_observable.addObserver(obs);
		initialize();
		data_to_gui();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() 
	{
		this.setSize(266, 283);
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel1 = new JLabel();
			jLabel1.setBounds(new Rectangle(14, 15, 99, 27));
			jLabel1.setText("# of rows");
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(15, 59, 97, 25));
			jLabel.setText("# of columns");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getJOkButton(), null);
			jContentPane.add(jLabel, null);
			jContentPane.add(jLabel1, null);
			jContentPane.add(getJRowsTextField(), null);
			jContentPane.add(getJColumnsTextField(), null);
			jContentPane.add(getJCurrentDimensionTextField(), null);
			jContentPane.add(getJAddLeftCheckBox(), null);
			jContentPane.add(getJAddTopCheckBox(), null);
		}
		return jContentPane;
	}

	private void set_current_dimensions()
	{
		if (m_data.get_grid() != null)
		{
			getJCurrentDimensionTextField().setText("Current dimension "+
					m_data.get_grid().get_nb_rows()+" rows, "+m_data.get_grid().get_nb_cols()+" cols.");
		}
	}
	/**
	 * This method initializes jOkButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJOkButton() {
		if (jOkButton == null) {
			jOkButton = new JButton();
			jOkButton.setBounds(new Rectangle(86, 224, 79, 27));
			jOkButton.setText("OK");
			jOkButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
				    on_close();
				}
			});
		}
		return jOkButton;
	}

	/**
	 * This method initializes jRowsTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJRowsTextField() {
		if (jRowsTextField == null) {
			jRowsTextField = new JTextField();
			jRowsTextField.setBounds(new Rectangle(137, 16, 90, 30));
		}
		return jRowsTextField;
	}

	/**
	 * This method initializes jColumnsTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJColumnsTextField() {
		if (jColumnsTextField == null) {
			jColumnsTextField = new JTextField();
			jColumnsTextField.setBounds(new Rectangle(137, 60, 90, 30));
		}
		return jColumnsTextField;
	}

	/**
	 * This method initializes jCurrentDimensionTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJCurrentDimensionTextField() {
		if (jCurrentDimensionTextField == null) {
			jCurrentDimensionTextField = new JTextField();
			jCurrentDimensionTextField.setBounds(new Rectangle(17, 183, 215, 29));
		}
		return jCurrentDimensionTextField;
	}

	/**
	 * This method initializes jAddLeftCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJAddLeftCheckBox() {
		if (jAddLeftCheckBox == null) {
			jAddLeftCheckBox = new JCheckBox();
			jAddLeftCheckBox.setBounds(new Rectangle(27, 106, 197, 33));
			jAddLeftCheckBox.setText("Add/del columns by the left");
		}
		return jAddLeftCheckBox;
	}

	/**
	 * This method initializes jAddRowsCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJAddTopCheckBox() {
		if (jAddRowsCheckBox == null) {
			jAddRowsCheckBox = new JCheckBox();
			jAddRowsCheckBox.setBounds(new Rectangle(29, 139, 172, 31));
			jAddRowsCheckBox.setText("Add/del rows by the top");
		}
		return jAddRowsCheckBox;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
