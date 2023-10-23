package gods.editor.level;

import gods.base.*;
import gods.editor.*;

import javax.swing.filechooser.*;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import java.awt.Frame;
import javax.swing.JDialog;
import java.awt.Dimension;
import javax.swing.JTextField;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JLabel;

public class LevelImportDialog extends JDialog implements DataModifier<LevelData>
{
	private LevelData m_data;
	private int m_tolerance = 0;
	private boolean m_ignore_edge = false;
	private static String m_previous_image_file = "";
	
	public void data_to_gui() 
	{
		get_level_tile_file_tf().setText(m_data.get_level_tile());
		get_common_tile_file_tf().setText(m_data.get_common_tile());
		get_project_name_tf().setText(m_data.get_project_file());
		get_image_file_tf().setText(m_previous_image_file);

	}

	public LevelData get_data() 
	{
		return m_data;
	}

	public void gui_to_data() 
	{		
		
		try 
		{
			m_previous_image_file = get_image_file_tf().getText();
			
			if (!getJKeepLevelDataCheckBox().isSelected())
			{
				m_data.init(get_project_name_tf().getText(),
						get_common_tile_file_tf().getText(),
						get_level_tile_file_tf().getText(),EditableData.GfxMode.EDITOR);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	public void on_close() 
	{
		boolean notify = (!get_level_tile_file_tf().getText().equals("")) &&
		(!get_common_tile_file_tf().getText().equals("")) && 
		(!get_project_name_tf().getText().equals(""));
		
		if (notify)
		{
			
				try
				{
					m_tolerance = Integer.parseInt(getJRgbToleranceTextField().getText());
					m_ignore_edge = getJIgnoreBadEdgeCheckBox().isSelected();
					notify = (!get_image_file_tf().equals(""));
				}
				catch (NumberFormatException e)
				{
					notify = false;
				}
			
		}
		if (notify)
		{			
			gui_to_data();
			dispose();
			m_observable.signal();
		}
		
	}

	private class EditObs extends Observable
	{
		void signal()
		{
			setChanged();
			notifyObservers(LevelImportDialog.this);
		}
	}
	
	private EditObs m_observable = new EditObs();  //  @jve:decl-index=0:
	
	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;  //  @jve:decl-index=0:visual-constraint="10,10"

	private JTextField m_image_file_tf = null;

	private ImageIcon m_file_chooser_icon = null;
	
	private JButton m_select_image_button = null;

	private JTextField m_tile_file_tf = null;

	private JButton m_select_tiles_button = null;

	private JButton m_validate_button = null;

	private JTextField m_project_name_tf = null;

	private JLabel jLabel = null;
	
	private JLabel jLabel12 = null;

	private JLabel jLabel1 = null;

	private JButton m_select_main_tiles_button = null;

	private JTextField m_main_tile_file_tf = null;

	private JLabel jLabel2 = null;

	private JLabel jLabel3 = null;

	private JTextField jRgbToleranceTextField = null;

	private JCheckBox jKeepLevelDataCheckBox = null;
	private JCheckBox jIgnoreBadEdgeCheckBox = null;
	
	public LevelImportDialog()
	{
		this(null,null,null);
	}
	
	/**
	 * @param owner
	 */
	
	
	public LevelImportDialog(Frame owner, Observer obs, LevelData levelData) 
	{
		super(owner);
		m_data = levelData;
		m_observable.addObserver(obs);
		m_file_chooser_icon = load_icon("open.png");
		initialize();
		data_to_gui();
	}
	
	private ImageIcon load_icon(String name)
	{
		String image_path = DirectoryBase.get_icons_path() + name;
		return new ImageIcon(image_path);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() 
	{
		this.setSize(448, 335);
	
		this.setTitle("Import level from image");
		
		
		this.setContentPane(getJContentPane());
	}
	/**
	 * This method initializes jAddLeftCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJKeepLevelDataCheckBox() {
		if (jKeepLevelDataCheckBox == null) {
			jKeepLevelDataCheckBox = new JCheckBox();
			jKeepLevelDataCheckBox.setBounds(new Rectangle(180, 225, 130, 26));
			jKeepLevelDataCheckBox.setText("Keep level data");
		}
		return jKeepLevelDataCheckBox;
	}
	private JCheckBox getJIgnoreBadEdgeCheckBox() {
		if (jIgnoreBadEdgeCheckBox == null) {
			jIgnoreBadEdgeCheckBox = new JCheckBox();
			jIgnoreBadEdgeCheckBox.setBounds(new Rectangle(320, 225, 130, 26));
			jIgnoreBadEdgeCheckBox.setText("Ignore bad edge");
		}
		return jIgnoreBadEdgeCheckBox;
	}
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
		
			jLabel3 = new JLabel();
			jLabel3.setBounds(new Rectangle(28, 227, 99, 23));
			jLabel3.setText("rgb tolerance");
			jLabel2 = new JLabel();
			jLabel2.setBounds(new Rectangle(19, 178, 55, 16));
			jLabel2.setText("main tiles");
			jLabel1 = new JLabel();
			jLabel1.setBounds(new Rectangle(18, 134, 63, 23));
			jLabel1.setText("level tiles");
			jLabel12 = new JLabel();
			jLabel12.setBounds(new Rectangle(18, 84, 60, 26));
			jLabel12.setText("image");
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(19, 26, 96, 34));
			jLabel.setText("level name");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.setSize(new Dimension(460, 200));
			
			jContentPane.add(get_image_file_tf(), null);
			jContentPane.add(get_select_image_button(), null);
			jContentPane.add(jLabel12, null);
			jContentPane.add(getJRgbToleranceTextField(), null);

			jContentPane.add(get_level_tile_file_tf(), null);
			jContentPane.add(get_select_tiles_button(), null);
			jContentPane.add(get_import_button(), null);
			jContentPane.add(get_project_name_tf(), null);
			jContentPane.add(jLabel, null);
			jContentPane.add(jLabel1, null);
			jContentPane.add(get_select_main_tiles_button(), null);
			jContentPane.add(get_common_tile_file_tf(), null);
			jContentPane.add(jLabel2, null);
			jContentPane.add(jLabel3, null);
			jContentPane.add(getJKeepLevelDataCheckBox(), null);
			jContentPane.add(getJIgnoreBadEdgeCheckBox(),null);
		}
		return jContentPane;
	}

	private String choose_file(String path,FileFilter ff)
	{
		JFileChooser fc = new JFileChooser(path);
		String rval = null;
		if (ff != null)
		{
			fc.setFileFilter(ff);
		}
		
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) 
		{		
			rval = fc.getSelectedFile().getAbsolutePath();
		}
		return rval;
	}

	/**
	 * This method initializes m_image_file_tf	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField get_image_file_tf() {
		if (m_image_file_tf == null) {
			m_image_file_tf = new JTextField();
			m_image_file_tf.setBounds(new Rectangle(139, 86, 281, 26));
			
		}
		return m_image_file_tf;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton get_select_image_button() {
		if (m_select_image_button == null) {
			m_select_image_button = new JButton();
			m_select_image_button.setBounds(new Rectangle(85, 83, 40,24));
			m_select_image_button.setIcon(m_file_chooser_icon);			
			m_select_image_button.addActionListener(new java.awt.event.ActionListener() {   
				public void actionPerformed(java.awt.event.ActionEvent e) 
				{    
					String image = choose_file(null, null);
					if (image != null)
					{
						m_image_file_tf.setText(image);
					}
				}
			
			});
		}
		return m_select_image_button;
	}

	/**
	 * This method initializes m_tile_file_tf	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField get_level_tile_file_tf() {
		if (m_tile_file_tf == null) {
			m_tile_file_tf = new JTextField();
			m_tile_file_tf.setBounds(new Rectangle(141, 135, 278, 26));
			
		}
		return m_tile_file_tf;
	}

	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton get_select_tiles_button() {
		if (m_select_tiles_button == null) {
			m_select_tiles_button = new JButton();
			m_select_tiles_button.setBounds(new Rectangle(86, 133, 40, 24));
			m_select_tiles_button.setIcon(m_file_chooser_icon);
			m_select_tiles_button.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) 
				{
					String tiles = choose_file(DirectoryBase.get_tiles_path(), new GfxObjectSetFileFilter());
					if (tiles != null)
					{
						get_level_tile_file_tf().setText(tiles);
					}
				}
			});
		}
		return m_select_tiles_button;
	}


	public String get_image_file()
	{
		return get_image_file_tf().getText();
	}
	
	
	
	/**
	 * This method initializes jButton2	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton get_import_button() {
		if (m_validate_button == null) {
			m_validate_button = new JButton();
			m_validate_button.setBounds(new Rectangle(158, 275, 113, 25));
			m_validate_button.setText("Import");
			m_validate_button.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) 
				{
					on_close();
				}
			});
		}
		return m_validate_button;
	}

	/**
	 * This method initializes m_project_name	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField get_project_name_tf() {
		if (m_project_name_tf == null) {
			m_project_name_tf = new JTextField();
			m_project_name_tf.setBounds(new Rectangle(127, 29, 291, 28));
		}
		return m_project_name_tf;
	}

	/**
	 * This method initializes m_select_main_tiles_button	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton get_select_main_tiles_button() {
		if (m_select_main_tiles_button == null) {
			m_select_main_tiles_button = new JButton();
			m_select_main_tiles_button.setBounds(new Rectangle(85, 178, 40, 24));
			m_select_main_tiles_button.setIcon(m_file_chooser_icon);
			m_select_main_tiles_button
			.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String tiles = choose_file(DirectoryBase.get_tiles_path(), new GfxObjectSetFileFilter());
					if (tiles != null)
					{
						get_common_tile_file_tf().setText(tiles);
					}
				}
			});
		}
		return m_select_main_tiles_button;
	}

	/**
	 * This method initializes m_main_tile_file_tf	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField get_common_tile_file_tf() 
	{
		if (m_main_tile_file_tf == null) {
			m_main_tile_file_tf = new JTextField();
			m_main_tile_file_tf.setBounds(new Rectangle(142, 179, 276, 26));
			

		}
		return m_main_tile_file_tf;
	}

	/**
	 * This method initializes jRgbToleranceTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJRgbToleranceTextField() {
		if (jRgbToleranceTextField == null) {
			jRgbToleranceTextField = new JTextField();
			jRgbToleranceTextField.setBounds(new Rectangle(120, 225, 54, 26));
			jRgbToleranceTextField.setText(""+20);
		}
		return jRgbToleranceTextField;
	}

	public boolean is_ignore_edge()
	{
		return m_ignore_edge;
	}
	public int get_tolerance() 
	{
		return m_tolerance;
	}


} 
