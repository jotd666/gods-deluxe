package gods.editor.palette;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import gods.base.DirectoryBase;
import gods.base.GfxPalette;
import gods.editor.GfxObjectSetFileFilter;
import gods.editor.GodsEditor;
import gods.editor.ImageFileFilter;
import gods.editor.ScrolledCanvas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Observable;
import javax.swing.filechooser.FileFilter;

public class GodsPaletteEditor extends GodsEditor 
{
	private static final long serialVersionUID = 1L;

	private GfxPalette palette =  new GfxPalette();
	private EditGfxPalette palette_editor;
	
	private JMenu jMenuView = new JMenu("View");
	private JCheckBoxMenuItem jMenuViewDefined = new JCheckBoxMenuItem("Defined tiles");
	
	public GodsPaletteEditor() 
	{
		super("Gods Palette editor", "GFXOBJECT");
		
		palette_editor = new EditGfxPalette(this,palette);
		
		ScrolledCanvas<? extends JPanel> sw = new PaletteCanvas(palette_editor);
		
		init(palette,sw);
	}
	protected String get_default_path()
	{
		return DirectoryBase.get_tiles_path();
	}
	

	protected void init_widgets() throws Exception
	{
		super.init_widgets();
		
		jMenuViewDefined.setSelected(true);
		
		jMenuViewDefined.addActionListener(new ViewActionListener(ViewActionType.TOGGLE_DEFINED));
		jMenuView.add(jMenuViewDefined);
		
		add_main_menu(jMenuView);
		
	}
	
	protected FileFilter get_project_file_filter()
	{
		return new GfxObjectSetFileFilter();
	}
	
	protected void new_project()
	{
		JFileChooser fc = new JFileChooser(DirectoryBase.get_tiles_path() + "1x");
		ImageFileFilter ff = new ImageFileFilter();
		fc.setFileFilter(ff);
		
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) 
		{		
			int option = 0;
			String image_file = fc.getSelectedFile().getAbsolutePath();
			
			if (m_data.is_modified())
			{
				option = JOptionPane.showConfirmDialog(this,
						"Current project has not been saved. New project anyway?", "Confirm",
						JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE);
			}
			if (option == 0)
			{
				   int idx = image_file.lastIndexOf('.');
				    String project_file = image_file.substring(0, idx) +
				        DirectoryBase.GFX_OBJECT_SET_EXTENSION;
				    
				    if (new File(project_file).exists())
					{
						option = JOptionPane.showConfirmDialog(this,
								"Project file already exists, create new one?", "Confirm",
								JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE);
					}
			}
			if (option == 0)
			{	
				m_data.new_project(image_file);
				load_image();
			}
		}

	}

	public static void main(String[] args) {
		  try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		new GodsPaletteEditor();
		
	}	
	
	private enum ViewActionType { TOGGLE_DEFINED }
	
	private class ViewActionListener implements ActionListener
	{

		ViewActionType m_vat;

		public ViewActionListener(ViewActionType vat) 
		{
			m_vat = vat;
		}

		public void actionPerformed(ActionEvent evt) 
		{
			switch (m_vat)
			{
			case TOGGLE_DEFINED:			
				palette_editor.toggle_view_defined();
				break;

			}
		}

	}

	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}
}
