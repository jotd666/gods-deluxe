package gods.editor;


import javax.swing.JPanel;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JComboBox;
import java.awt.Dimension;
import javax.swing.JLabel;

public class ComboSelectorDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JButton jButton = null;

	private JComboBox jComboBox = null;

	private JLabel jLabel = null;

	private Object m_selected = null;
	
	public static Object show(Frame owner, java.util.Collection<?> values, String message, String title)
	{
		ComboSelectorDialog cs = new ComboSelectorDialog(owner,values,message,title);
		cs.setModal(true);
		cs.setVisible(true);
		cs.setLocationRelativeTo(owner);
		if ((cs.m_selected != null) && (cs.m_selected.toString().equals("<null>")))
		{
			cs.m_selected = null;
		}
		
		return cs.m_selected;
	}
	/**
	 * @param owner
	 */
	private ComboSelectorDialog(Frame owner, java.util.Collection<?> values, String message, String title) {
		super(owner);
		initialize();
		
	    //Centrer la fenêtre
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    Dimension frameSize = getSize();
	    if (frameSize.height > screenSize.height) {
	      frameSize.height = screenSize.height;
	    }
	    if (frameSize.width > screenSize.width) {
	      frameSize.width = screenSize.width;
	    }
	    setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
	    
		jLabel.setText(message);
		setTitle(title);
		for (Object o : values)
		{
			getJComboBox().addItem(o);
		}
		getJComboBox().addItem("<null>");
	}
	
	/**
	 * @param owner
	 */
	public ComboSelectorDialog(Frame owner) {
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 89);
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel = new JLabel();
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJButton(), BorderLayout.SOUTH);
			jContentPane.add(getJComboBox(), BorderLayout.CENTER);
			jContentPane.add(jLabel, BorderLayout.NORTH);
		}
		return jContentPane;
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
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
						m_selected = getJComboBox().getSelectedItem();
						dispose();
					}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJComboBox() {
		if (jComboBox == null) {
			jComboBox = new JComboBox();
		}
		return jComboBox;
	}

}  //  @jve:decl-index=0:visual-constraint="67,46"
