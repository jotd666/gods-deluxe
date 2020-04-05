package gods.editor.level;

import javax.swing.JPanel;
import java.awt.Frame;
import java.awt.BorderLayout;
import gods.editor.DataModifier;

import javax.swing.JDialog;

public class ShopTokensDialog extends JDialog implements DataModifier<Object> {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	public void data_to_gui() {
		// TODO Auto-generated method stub

	}

	public Object get_data() {
		// TODO Auto-generated method stub
		return null;
	}

	public void gui_to_data() throws Exception {
		// TODO Auto-generated method stub

	}

	public void on_close() {
		// TODO Auto-generated method stub

	}

	/**
	 * @param owner
	 */
	public ShopTokensDialog(Frame owner) {
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 200);
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
		}
		return jContentPane;
	}

}
