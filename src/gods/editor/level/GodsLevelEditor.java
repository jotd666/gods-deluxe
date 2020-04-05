package gods.editor.level;


import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import gods.base.*;
import gods.editor.GodsEditor;
import gods.editor.ImageFileFilter;
import gods.editor.LevelFileFilter;
import gods.editor.ScrolledCanvas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;

public class GodsLevelEditor extends GodsEditor {

	private static final long serialVersionUID = 1L;
	private LevelData m_level_data =  new LevelData();
	private EditLevel m_level_editor;
	
	
	private JMenu jMenuLevel = new JMenu("Level");

	private JMenuItem jMenuLevelProperties = new JMenuItem("Properties");
	private JMenuItem jMenuLevelImport = new JMenuItem("Import");
	private JMenuItem jMenuLevelExport = new JMenuItem("Export");
	private JMenuItem jMenuLevelResize = new JMenuItem("Resize");
	private JCheckBoxMenuItem jMenuLevelTileOverwrite = new JCheckBoxMenuItem("Tile overwrite mode");
	private JMenuItem jMenuLevelAssociations = new JMenuItem("Associations");
	private JMenu jMenuView = new JMenu("View");
	private JMenu jMenuViewObjects = new JMenu("Objects");
	private JMenu jMenuViewControlObjects = new JMenu("Control Objects");
	
	public GodsLevelEditor() 
	{
		super("Gods Level editor", "LEVEL");
		
		m_level_editor = new EditLevel(this,m_level_data);
		
		ScrolledCanvas<? extends JPanel> sw = new LevelCanvas(m_level_editor);
		
		
		init(m_level_data,sw);
		
		
		// call this magic crap so the keys work (why doesn't it work in the class constructor???)
		m_level_editor.setFocusable(true);
		m_level_editor.requestFocusInWindow();
		
		
	}
	protected String get_default_path()
	{
		return DirectoryBase.get_levels_path();
	}

	@Override
	protected void load_project(String f)
	{
		super.load_project(f);
		// reset toggle menu states
		jMenuLevelTileOverwrite.setSelected(false);
		
		/*TileVincinity tv = new TileVincinity();
		tv.compute(m_level_data.get_grid());
		tv.debug();*/
	}
	protected void new_project()
	{
		LevelCreationDialog lid = new LevelCreationDialog(this,m_level_editor,m_level_data,true);
		lid.setModal(true);
		lid.setLocationRelativeTo(this);
		lid.setVisible(true);
		
	}
	private class EditAssocActionListener implements ActionListener
	{
		
		public void actionPerformed(ActionEvent arg0) 
		{
			if (m_level_data != null)
			{
				AssociationDialog ad = new AssociationDialog(GodsLevelEditor.this,m_level_data);
				// register association dialog for later use (selection within the dialog)
				m_level_editor.set_association_dialog(ad);
				ad.setLocationRelativeTo(GodsLevelEditor.this);
				ad.setVisible(true);
			}
		}

		public EditAssocActionListener()
		{
		}
	}
	public enum Mode { Creation, Edition, Import, Resize, Export }

	private class TileOverwriteModeActionListener implements ActionListener
	{
		TileOverwriteModeActionListener()
		{
			
		}
		public void actionPerformed(ActionEvent arg0) 
		{
			m_level_data.set_tile_overwrite_mode(jMenuLevelTileOverwrite.isSelected());
		}
	}
	private class CreateLevelActionListener implements ActionListener
	{
		private Mode m_mode;
		
		public CreateLevelActionListener(Mode mode)
		{
			m_mode = mode;
		}
		
		public void actionPerformed(ActionEvent arg0) 
		{
			GodsEditor parent = GodsLevelEditor.this;
			
			JDialog d = null;
			
			switch (m_mode)
			{
			case Resize:			
				d = new LevelResizeDialog(parent, m_level_editor, m_level_data);
				break;
			case Export:
				JFileChooser fc = new JFileChooser(get_default_path());
				
				fc.setFileFilter(new ImageFileFilter());

				if (fc.showOpenDialog(GodsLevelEditor.this) == JFileChooser.APPROVE_OPTION) 
				{			
					String s = fc.getSelectedFile().getAbsolutePath();
					
					m_data.export(s);
				}
				break;
			case Import:
				d = new LevelImportDialog(parent,m_level_editor,m_level_data);
				break;
			case Creation:			
				d = new LevelCreationDialog(parent,m_level_editor,m_level_data,true);
				break;
			case Edition:
				d = new LevelCreationDialog(parent,m_level_editor,m_level_data,false);
				break;
			}
			if (d != null)
			{
				d.setModal(true);
				d.setLocationRelativeTo(parent);
				d.setVisible(true);
			}
		}
		
	}
	
	protected FileFilter get_project_file_filter()
	{
		return new LevelFileFilter();
	}
		
	private class ViewActionListener implements ActionListener
	{
		private int m_action_type = 0;
		private ControlObject.Type m_frame_type = null;
		
		private JCheckBoxMenuItem m_source;
		
		ViewActionListener(JCheckBoxMenuItem src,int at)
		{
			m_action_type = at;
			m_source = src;
		}
		ViewActionListener(JCheckBoxMenuItem src,ControlObject.Type t)
		{
			m_frame_type = t;
			m_source = src;
		}
		public void actionPerformed(ActionEvent arg0) 
		{
			boolean checked = m_source.isSelected();

			if (m_action_type != 0)
			{

				if (!checked)
				{
					m_level_data.bitclr_object_view_filter(m_action_type);
				}
				else
				{
					m_level_data.bitset_object_view_filter(m_action_type);
				}
			}
			else
			{
				if (checked)
				{
					m_level_data.add_control_object_filter(m_frame_type);

				}
				else
				{
					m_level_data.remove_control_object_filter(m_frame_type);
				}
			}
			m_level_editor.update_map(false,false);
			m_level_editor.repaint();
		}
		
	}
	private JCheckBoxMenuItem create_view_menu_item(ControlObject.Type t)
	{
		JCheckBoxMenuItem rval = new JCheckBoxMenuItem(t.toString(),true);
		rval.addActionListener(new ViewActionListener(rval,t));
		jMenuViewControlObjects.add(rval);
		return rval;
	}

	
	private JCheckBoxMenuItem create_view_menu_item(String label, int action_type)
	{
		JCheckBoxMenuItem rval = new JCheckBoxMenuItem(label,true);
		rval.addActionListener(new ViewActionListener(rval,action_type));
		jMenuViewObjects.add(rval);
		return rval;
	}
	
	protected void init_widgets() throws Exception
	{
		super.init_widgets();
		
		
		jMenuLevelProperties.addActionListener(new CreateLevelActionListener(Mode.Edition));
		jMenuLevel.add(jMenuLevelProperties);
		jMenuLevelImport.addActionListener(new CreateLevelActionListener(Mode.Import));
		jMenuLevel.add(jMenuLevelImport);
		jMenuLevelExport.addActionListener(new CreateLevelActionListener(Mode.Export));
		jMenuLevel.add(jMenuLevelExport);
		jMenuLevelAssociations.addActionListener(new EditAssocActionListener());
		jMenuLevel.add(jMenuLevelAssociations);
		
		jMenuLevelResize.addActionListener(new CreateLevelActionListener(Mode.Resize));

		jMenuLevel.add(jMenuLevelResize);
		jMenuLevel.add(jMenuLevelTileOverwrite);
		jMenuLevelTileOverwrite.addActionListener(new TileOverwriteModeActionListener());
		
		jMenuView.add(jMenuViewObjects);
		jMenuView.add(jMenuViewControlObjects);
		
		create_view_menu_item("Visible objects",LevelData.SHOW_VISIBLE_OBJECTS);
		create_view_menu_item("Hidden objects",LevelData.SHOW_HIDDEN_OBJECTS);
		
		for (ControlObject.Type t : ControlObject.Type.values())
		{
			create_view_menu_item(t);
		}
		
		add_main_menu(jMenuLevel);
		add_main_menu(jMenuView);
		
	}
	
	public static void main(String[] args) {
		  try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		new GodsLevelEditor();
		
	}
	public void update(Observable arg0, Object arg1) 
	{
		
		
	}	
}
