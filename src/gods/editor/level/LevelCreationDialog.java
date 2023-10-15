package gods.editor.level;

import gods.base.*;
import gods.sys.*;
import gods.editor.*;

import javax.swing.filechooser.*;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import java.awt.Frame;
import javax.swing.JDialog;
import java.awt.Dimension;
import javax.swing.JTextField;
import java.awt.Rectangle;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JComboBox;

public class LevelCreationDialog extends JDialog implements DataModifier<LevelData>
{
	private LevelData m_data;
	
	public void data_to_gui() 
	{
		String project_file = m_data.get_project_file();
		
		if (project_file != null)
		{
			File level_name = new File(project_file);

			getJLevelSetComboBox().setSelectedItem(level_name.getParentFile().getName());
			getJLevelIndexTextField().setText(level_name.getName());
		}
		else
		{
			getJLevelSetComboBox().setSelectedIndex(0);
		}
		get_level_tile_file_tf().setText(m_data.get_level_tile());
		get_common_tile_file_tf().setText(m_data.get_common_tile());
		
		//getJDamageDivisorTextField().setText(m_data.get_damage_divisor()+"");
		getJCopperBarComboBox().setSelectedItem(m_data.get_copperbar_class());
		getJLevelClassComboBox().setSelectedItem(m_data.get_level_class());
		getJMusicComboBox().setSelectedItem(m_data.get_level_music());
		getJBossMusicComboBox().setSelectedItem(m_data.get_boss_music());

	}

	public LevelData get_data() 
	{
		return m_data;
	}

	public void gui_to_data() throws Exception
	{		
		if (m_new_level)
		{
			String level_name = (String)getJLevelSetComboBox().getSelectedItem() + 
			File.separator + 
			getJLevelIndexTextField().getText();
			
			m_data.init(level_name,
					get_common_tile_file_tf().getText(),
					get_level_tile_file_tf().getText(),EditableData.GfxMode.EDITOR);

		}
		
		
		//m_data.set_damage_divisor(Integer.parseInt(getJDamageDivisorTextField().getText()));
		m_data.set_copperbar_class((String)getJCopperBarComboBox().getSelectedItem());
		m_data.set_level_class((String)getJLevelClassComboBox().getSelectedItem());
		m_data.set_level_music((String)getJMusicComboBox().getSelectedItem());
		m_data.set_boss_music((String)getJBossMusicComboBox().getSelectedItem());
	}

	public void on_close() 
	{
		boolean notify = (!get_level_tile_file_tf().getText().equals("")) &&
		(!get_common_tile_file_tf().getText().equals(""));
		//&& 
		//(!get_project_name_tf().getText().equals(""));
		
		if (notify)
		{			
			try
			{
				gui_to_data();
				dispose();
				m_observable.signal();
			}
			catch (Exception e)
			{
				MiscUtils.show_error_dialog(this, "Data input error", e.getMessage());
				e.printStackTrace();
			}
		}
		
	}

	private class EditObs extends Observable
	{
		void signal()
		{
			setChanged();
			notifyObservers(LevelCreationDialog.this);
		}
	}
	
	private EditObs m_observable = new EditObs();  //  @jve:decl-index=0:
	
	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;  //  @jve:decl-index=0:visual-constraint="10,10"

	private ImageIcon m_file_chooser_icon = null;
	
	private JTextField m_tile_file_tf = null;

	private JButton m_select_tiles_button = null;

	private JButton m_validate_button = null;

	//private JTextField m_project_name_tf = null;

	private JLabel jLabel = null;
	

	private JLabel jLabel1 = null;

	private JButton m_select_main_tiles_button = null;

	private JTextField m_main_tile_file_tf = null;

	private JLabel jLabel2 = null;

	private JLabel jLabel4 = null;
	private JTextField jDamageDivisorTextField = null;
	private boolean m_new_level;

	private JComboBox jCopperBarComboBox = null;

	private JLabel jLabel3 = null;

	private JLabel jLabel5 = null;

	private JComboBox jLevelClassComboBox = null;

	private JTextField jLevelIndexTextField = null;

	private JLabel jLabel6 = null;

	private JComboBox jLevelSetComboBox = null;

	private JLabel jLabel7 = null;

	private JTextField jShieldLevelTextField = null;

	private JComboBox jMusicComboBox = null;
	private JComboBox jBossMusicComboBox = null;

	private JLabel jLabel51 = null;
	private JLabel jLabel52 = null;
	
	public LevelCreationDialog()
	{
		this(null,null,null,true);
	}
		
	public LevelCreationDialog(Frame owner, Observer obs, LevelData levelData,boolean new_level) 
	{
		super(owner);
		m_data = levelData;
		m_observable.addObserver(obs);
		m_file_chooser_icon = load_icon("open.png");
		m_new_level = new_level;
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
		this.setSize(448, 460);
		if (m_new_level)
		{
			this.setTitle("Create new level");
		}
		else
		{
		
			this.setTitle("Edit level "+m_data.get_project_file());
		}
		
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel51 = new JLabel();
			jLabel51.setBounds(new Rectangle(12, 285, 73, 16));
			jLabel51.setText("music file");
			jLabel52 = new JLabel();
			jLabel52.setBounds(new Rectangle(12, 315, 73, 16));
			jLabel52.setText("boss music file");
			jLabel7 = new JLabel();
			jLabel7.setBounds(new Rectangle(171, 154, 79, 25));
			jLabel7.setText("shield level");
			jLabel6 = new JLabel();
			jLabel6.setBounds(new Rectangle(265, 28, 74, 33));
			jLabel6.setText("level index");
			jLabel5 = new JLabel();
			jLabel5.setBounds(new Rectangle(13, 233, 85, 25));
			jLabel5.setText("level class");
			jLabel3 = new JLabel();
			jLabel3.setBounds(new Rectangle(15, 192, 76, 22));
			jLabel3.setText("copperbar");
			jLabel4 = new JLabel();
			jLabel4.setBounds(new Rectangle(14, 154, 91, 26));
			jLabel4.setText("damage divisor");
			jLabel2 = new JLabel();
			jLabel2.setBounds(new Rectangle(11, 115, 55, 16));
			jLabel2.setText("other tiles");
			jLabel1 = new JLabel();
			jLabel1.setBounds(new Rectangle(9, 71, 63, 23));
			jLabel1.setText("level tiles");
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(11, 25, 72, 34));
			jLabel.setText("level set");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.setSize(new Dimension(460, 200));
			

			jContentPane.add(get_level_tile_file_tf(), null);
			jContentPane.add(get_common_tile_file_tf(), null);
			
			if (m_new_level)
			{
				jContentPane.add(get_select_tiles_button(), null);
				jContentPane.add(get_select_main_tiles_button(), null);
			}
			else
			{
				get_common_tile_file_tf().setEditable(false);
				get_level_tile_file_tf().setEditable(false);
				getJLevelSetComboBox().setEnabled(false);
				getJLevelIndexTextField().setEditable(false);
			}
			jContentPane.add(get_validate_button(), null);

			jContentPane.add(jLabel, null);
			jContentPane.add(jLabel1, null);
			jContentPane.add(jLabel2, null);
			jContentPane.add(jLabel4, null);
			jContentPane.add(getJDamageDivisorTextField(), null);
			jContentPane.add(getJCopperBarComboBox(), null);
			jContentPane.add(jLabel3, null);
			jContentPane.add(jLabel5, null);
			jContentPane.add(getJLevelClassComboBox(), null);
			jContentPane.add(getJLevelIndexTextField(), null);
			jContentPane.add(jLabel6, null);
			jContentPane.add(getJLevelSetComboBox(), null);
			jContentPane.add(jLabel7, null);
			jContentPane.add(getJShieldLevelTextField(), null);
			jContentPane.add(getJMusicComboBox(), null);
			jContentPane.add(getJBossMusicComboBox(), null);
			jContentPane.add(jLabel51, null);
			jContentPane.add(jLabel52, null);
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
	 * This method initializes m_tile_file_tf	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField get_level_tile_file_tf() {
		if (m_tile_file_tf == null) {
			m_tile_file_tf = new JTextField();
			m_tile_file_tf.setBounds(new Rectangle(132, 72, 278, 26));
			
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
			m_select_tiles_button.setBounds(new Rectangle(81, 74, 40, 24));
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

	
	/**
	 * This method initializes jButton2	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton get_validate_button() {
		if (m_validate_button == null) {
			m_validate_button = new JButton();
			m_validate_button.setBounds(new Rectangle(151, 370, 113, 25));
			m_validate_button.setText(m_new_level ? "Create" : "Commit");
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
	 * This method initializes m_select_main_tiles_button	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton get_select_main_tiles_button() {
		if (m_select_main_tiles_button == null) {
			m_select_main_tiles_button = new JButton();
			m_select_main_tiles_button.setBounds(new Rectangle(82, 112, 40, 24));
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
			m_main_tile_file_tf.setBounds(new Rectangle(134, 111, 276, 26));
			

		}
		return m_main_tile_file_tf;
	}


	/**
	 * This method initializes jDamageDivisorTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJDamageDivisorTextField() {
		if (jDamageDivisorTextField == null) {
			jDamageDivisorTextField = new JTextField();
			jDamageDivisorTextField.setBounds(new Rectangle(113, 153, 46, 26));
			jDamageDivisorTextField.setText("1");
		}
		return jDamageDivisorTextField;
	}

	private void load_combo_with_files(JComboBox cb, String subdir_name, boolean recurse)
	{
		// load it with classes
		
		String mp3_dir = DirectoryBase.get_mp3_path();
		
		File [] classes = new File(mp3_dir + subdir_name).listFiles();
		
		for (File c : classes)
		{
			
			String s = subdir_name + File.separator + c.getName();
			
			if ((recurse) && (c.isDirectory()))
			{
				load_combo_with_files(cb,s,true);
			}
			else
			{
				if (s.endsWith(".mp3"))
				{
					s = s.replace("."+File.separator,"");
										
					cb.addItem(s);
				}
			}
		}
	}
	private void load_combo_with_classes(JComboBox cb, String package_name, boolean recurse)
	{
		// load it with classes
		
		String bin_dir = DirectoryBase.get_root()+"bin";
		String class_dir = bin_dir+File.separator+package_name.replace('.', File.separatorChar);
		
		File [] classes = new File(class_dir).listFiles();
		
		for (File c : classes)
		{
			String s = package_name+"."+c.getName();
			
			if ((recurse) && (c.isDirectory()))
			{
				load_combo_with_classes(cb,s,true);
			}
			else
			{
				if (!s.contains("$"))
				{
					int dotindex = s.lastIndexOf('.');
					cb.addItem(s.substring(0,dotindex));
				}
			}
		}
	}
	/**
	 * This method initializes jCopperBarComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJCopperBarComboBox() {
		if (jCopperBarComboBox == null) {
			jCopperBarComboBox = new JComboBox();
			jCopperBarComboBox.setBounds(new Rectangle(109, 190, 260, 26));
			
			load_combo_with_classes(jCopperBarComboBox,"gods.game.copperbar",false);
			
		}
		return jCopperBarComboBox;
	}

	/**
	 * This method initializes jLevelClassComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJLevelClassComboBox() {
		if (jLevelClassComboBox == null) {
			jLevelClassComboBox = new JComboBox();
			jLevelClassComboBox.setBounds(new Rectangle(111, 236, 261, 25));

			load_combo_with_classes(jLevelClassComboBox,"gods.game.levels",true);
		}
		

		return jLevelClassComboBox;
	}

	/**
	 * This method initializes jLevelIndexTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJLevelIndexTextField() {
		if (jLevelIndexTextField == null) {
			jLevelIndexTextField = new JTextField();
			jLevelIndexTextField.setBounds(new Rectangle(348, 31, 68, 31));
		}
		return jLevelIndexTextField;
	}

	/**
	 * This method initializes jLevelSetComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJLevelSetComboBox() {
		if (jLevelSetComboBox == null) {
			jLevelSetComboBox = new JComboBox();
			jLevelSetComboBox.setBounds(new Rectangle(91, 30, 163, 31));
			
			File level_dir = new File(DirectoryBase.get_levels_path());
			String [] dirs = level_dir.list();
			for (String s : dirs)
			{
				jLevelSetComboBox.addItem(s);
			}
		}
		return jLevelSetComboBox;
	}

	/**
	 * This method initializes jShieldLevelTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJShieldLevelTextField() {
		if (jShieldLevelTextField == null) {
			jShieldLevelTextField = new JTextField();
			jShieldLevelTextField.setBounds(new Rectangle(265, 153, 29, 27));
			jShieldLevelTextField.setText("0");
		}
		return jShieldLevelTextField;
	}

	/**
	 * This method initializes jMusicComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJMusicComboBox() 
	{
		if (jMusicComboBox == null) 
		{
			jMusicComboBox = new JComboBox();
			jMusicComboBox.setBounds(new Rectangle(110, 279, 263, 25));
		
			jMusicComboBox.addItem(ParameterParser.UNDEFINED_STRING);
			
			load_combo_with_files(jMusicComboBox,".",true);
		}
		return jMusicComboBox;
	}
	private JComboBox getJBossMusicComboBox() 
	{
		if (jBossMusicComboBox == null) 
		{
			jBossMusicComboBox = new JComboBox();
			jBossMusicComboBox.setBounds(new Rectangle(110, 310, 263, 25));
		
			jBossMusicComboBox.addItem(ParameterParser.UNDEFINED_STRING);
			
			load_combo_with_files(jBossMusicComboBox,".",true);
		}
		return jBossMusicComboBox;
	}

}  //  @jve:decl-index=0:visual-constraint="23,10"
