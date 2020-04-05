package gods.editor;

import java.awt.*;
import java.util.*;
import java.awt.event.*;

import javax.swing.filechooser.FileFilter;
import javax.swing.*;

import gods.base.*;
import gods.sys.ParameterParser;

import java.io.*;


/**
 * <p>
 * Titre :
 * </p>
 * <p>
 * Description :
 * </p>
 * <p>
 * Copyright : Copyright (c) 2005
 * </p>
 * <p>
 * Société :
 * </p>
 * 
 * @author non attribuable
 * @version 1.0
 */

public abstract class GodsEditor extends JFrame implements Observer
{
	static final long serialVersionUID = 3453;
	
	enum MenuActionType { NO_ACTION, NEW_PROJECT, SAVE_PROJECT ,LOAD_PROJECT, ABOUT, QUIT }

	
	private JMenuBar m_menu_bar = new JMenuBar();
	private JToolBar m_tool_bar = new JToolBar();
	private JLabel m_status_bar = new JLabel();

	private JMenu jMenuFile = new JMenu();
	private JMenu jMenuFileRecent = new JMenu();

	private JMenuItem jMenuFileExit = new JMenuItem();
	private JMenuItem jMenuFileNew = new JMenuItem();
	private JMenuItem jMenuFileSave = new JMenuItem();
	private JMenuItem jMenuFileLoad = new JMenuItem();

	private JMenuItem jMenuHelpAbout = new JMenuItem();

	private JMenu jMenuHelp = new JMenu();

	private Vector<String> m_recent_files = new Vector<String>();
	
	private static final int MAX_RECENT_FILES = 6;

	protected EditableData m_data;
	
	private ScrolledCanvas<? extends JPanel> m_scrolled_window;

	private String m_recent_prefix;
	private String m_title;
	
	
	public GodsEditor(String title, String recent_prefix)
	{
		m_recent_prefix = recent_prefix;
		m_title = title;
		
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
	}

	/*
	public GodsEditor(String tiles_file, String title,String recent_prefix)
	{
		this(title,recent_prefix);
		load_project(DirectoryBase.get_tiles_path() + tiles_file);
	}
*/
	
	 protected void init(EditableData data, ScrolledCanvas<? extends JPanel> sw)
	  {
			m_data = data;
			m_scrolled_window = sw;
		 
		 try {
			 init_widgets();

			 add_main_menu(jMenuHelp);

		 } catch (Exception e) {
			 e.printStackTrace();
		 }
			
	    boolean packframe = false;
	
	    if (packframe)
	    {
	      pack();
	    }
	    else
	    {
	      validate();
	    }

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
	    
	    setTitle(m_title);
	    
	    setVisible(true);
	  }
	private void set_status(String status)
	{
		m_status_bar.setText(status);
	}

	static protected ImageIcon load_icon(String name)
	{
		String image_path = DirectoryBase.get_root() + File.separator + "icons"
				+ File.separator + name;
		return new ImageIcon(image_path);
	}

	private JButton insert_toolbar_button(String file, String tooltip, ActionListener a)
	{
		JButton button = new JButton();
		button.setIcon(load_icon(file));
		button.setToolTipText(tooltip);
		button.addActionListener(a);
		m_tool_bar.add(button);
		return button;
	}


	private void load_recent_files()
	{
		String recent_file = DirectoryBase.get_user_path() + m_recent_prefix+".recent";
	
		try
		{
			ParameterParser p = ParameterParser.open(recent_file);
			p.startBlockVerify(m_recent_prefix+"_RECENT_FILES");
			
			int nb_recent = p.readInteger("nb_recent");
			
			for (int i = 0; i < nb_recent; i++)
			{
				add_to_recent_files(p.readString("file"));
			}
			
			p.endBlockVerify();
		}
		catch(IOException e)
		{
			
		}
	}
	private void save_recent_files()
	{
		String recent_file = DirectoryBase.get_user_path() + m_recent_prefix+".recent";
	
		try
		{
			ParameterParser p = ParameterParser.create(recent_file);
			p.startBlockWrite(m_recent_prefix+"_RECENT_FILES");
			
			p.write("nb_recent",m_recent_files.size());
			
			for (String s : m_recent_files)
			{
				p.write("file",s);
			}
			
			p.endBlockWrite();
			p.close();
		}
		catch(IOException e)
		{
			
		}
	}
	

	protected void add_main_menu(JMenu j)
	{
		m_menu_bar.add(j);
	}
	protected void init_widgets() throws Exception
	{
		JPanel contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BorderLayout());

		ToolbarActionAdapter new_project_action_adapter = new ToolbarActionAdapter(MenuActionType.NEW_PROJECT);
		ToolbarActionAdapter load_project_action_adapter = new ToolbarActionAdapter(MenuActionType.LOAD_PROJECT);
		ToolbarActionAdapter save_project_action_adapter = new ToolbarActionAdapter(MenuActionType.SAVE_PROJECT);
		ToolbarActionAdapter about_action_adapter = new ToolbarActionAdapter(MenuActionType.ABOUT);

		insert_toolbar_button("new.png", "New",new_project_action_adapter);
		insert_toolbar_button("open.png", "Open",load_project_action_adapter);
		insert_toolbar_button("save.png", "Save",save_project_action_adapter);


		jMenuFileSave.addActionListener(save_project_action_adapter);
		jMenuFileSave.setText("Save");

		jMenuFileLoad.addActionListener(load_project_action_adapter);
		jMenuFileLoad.setText("Load");
		
		jMenuFileExit.addActionListener(new ToolbarActionAdapter(MenuActionType.QUIT));
		jMenuFileExit.setText("Quit");
				
		jMenuHelpAbout.setText("About");
		jMenuHelpAbout.addActionListener(about_action_adapter);
		jMenuHelp.setText("Help");
		
		contentPane.add(m_tool_bar, BorderLayout.NORTH);
		contentPane.add(m_status_bar, BorderLayout.SOUTH);
		contentPane.add(m_scrolled_window);
		this.setSize(new Dimension(700, 550));
		contentPane.setSize(getSize());

		jMenuFile.setText("File");
		jMenuFileExit.setText("Quit");
		
		
		jMenuFileNew.setText("New");
		jMenuFileNew.addActionListener(new_project_action_adapter);
		
		jMenuFile.add(jMenuFileNew);
		jMenuFile.addSeparator();		
		
		jMenuFileRecent.setText("Recent");
		
		// load recent
		
		load_recent_files();
		
		update_recent_menu_item();
		
		// File menu

		jMenuFile.add(jMenuFileNew);
		jMenuFile.add(jMenuFileLoad);
		jMenuFile.add(jMenuFileSave);
		jMenuFile.addSeparator();
		jMenuFile.add(jMenuFileRecent);
		jMenuFile.addSeparator();
		jMenuFile.add(jMenuFileExit);
		
		add_main_menu(jMenuFile);		
		
			this.setJMenuBar(m_menu_bar);
		jMenuHelp.add(jMenuHelpAbout);

	}

	
	private void quit()
	{
		int option = 0;
		if (m_data.is_modified()) {
			option = JOptionPane.showConfirmDialog(this,
					"Project modified, really quit?", "Confirm",
					JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE);
		}
		if (option == 0) 
		{
			save_recent_files();
			System.exit(0);
		}
	}

	
	public void about()
	{		
		AboutBox dlg = new AboutBox(this,"2.2",m_title,"JOTD","2007-2011");
		Dimension dlgSize = dlg.getPreferredSize();
		Dimension frmSize = getSize();
		Point loc = getLocation();
		dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
				(frmSize.height - dlgSize.height) / 2 + loc.y);
		dlg.setModal(true);
		dlg.pack();
		dlg.setVisible(true);
	}

	
	protected void processWindowEvent(WindowEvent e)
	{
		if (e.getID() == WindowEvent.WINDOW_CLOSING) 
		{
			quit();
		} else 
		{
			super.processWindowEvent(e);
		}
	}

	protected abstract void new_project();
	protected abstract FileFilter get_project_file_filter();
	
	protected void load_image()
	{
		try {
			m_scrolled_window.update();

			m_scrolled_window.setSize(getSize());
			
			set_status("image for \"" + m_data.get_project_file()
					+ "\" loaded");
		} catch (IOException ex) {
			set_status("image not loaded: " + ex.toString());
		}
	}

	protected abstract String get_default_path();
	
	private void save_project()
	{
		String project_file = m_data.get_project_file();

		if (project_file == null) 
		{
			JFileChooser fc = new JFileChooser(get_default_path());
			fc.showSaveDialog(this);
			File f = fc.getSelectedFile();
			if (f != null)
			{
				project_file = f.getAbsolutePath();
			}
		}
		if (project_file != null) 
		{
			try {
				m_data.set_project_file(project_file);
				if (m_data.save())
				{
					add_to_recent_files(project_file);
					set_status("Project \"" + project_file + "\" has been saved");
				}
				else
				{
					JOptionPane.showMessageDialog(this, "Save error",
							"Exception while saving",JOptionPane.ERROR_MESSAGE);
				}
			} 
			catch (java.io.IOException e) 
			{
				set_status(e.toString());
			}
		}
	}
	private void debug(String m)
	{
		
		System.out.println(this.getClass().getName()+": "+m);
		
	}
	
	protected void load_project(String project_name)
	{
		boolean parsing_errors = false;
	
		try 
		{
			// load the vector data

			m_data.load(project_name,EditableData.GfxMode.EDITOR);

		} catch (Exception e) 
		{
			debug("parsing exception: "+e.getMessage());
			parsing_errors = true;
			e.printStackTrace();
		}
		try 
		{
			// load the image
			m_scrolled_window.update();
			repaint();
			
			String err = parsing_errors ? "with parsing errors" :
				"without errors";
			set_status("Project \"" + project_name + "\" has been loaded "+err);
			
			add_to_recent_files(project_name);
		}
		catch (Exception e) 
		{
			set_status(e.toString());			
		}
	}
	private void add_to_recent_files(String s)
	{
		// avoid absolute names
		s = s.replace(DirectoryBase.get_levels_path(), "");
		s = s.replace(DirectoryBase.LEVEL_EXTENSION, "");
		s = s.replace(DirectoryBase.GFX_OBJECT_SET_EXTENSION, "");
		
		int found = -1;
		for (int i = 0; i < m_recent_files.size() && found == -1; i++)
		{
			if (m_recent_files.elementAt(i).equals(s))
			{
				found = i;
			}
		}
		
		if (found == -1)
		{
			if (m_recent_files.size() < MAX_RECENT_FILES)
			{
				m_recent_files.add(s);
			}
			else
			{
				// scroll down the other files
				for (int i = 1; i < MAX_RECENT_FILES; i++)
				{
					m_recent_files.set(MAX_RECENT_FILES-i,
							m_recent_files.elementAt(MAX_RECENT_FILES-i-1));
				}
				m_recent_files.set(0,s);
			}
		}
		else
		{
			if (found != 0)
			{
				// swap in history
				
				String swp = m_recent_files.elementAt(0);
				m_recent_files.set(0,m_recent_files.elementAt(found));
				m_recent_files.set(found,swp);
			}
		}
		
		update_recent_menu_item();
		
		
			
	}
	
	private void update_recent_menu_item()
	{
		jMenuFileRecent.removeAll();
		
		for (String s : m_recent_files)
		{
			JMenuItem item = new JMenuItem(s);
			item.addActionListener(new RecentActionListener(s));

			jMenuFileRecent.add(item);
		}
	}
	

	private void load_project()
	{
		JFileChooser fc = new JFileChooser(get_default_path());
		
		fc.setFileFilter(get_project_file_filter());
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) 
		{			
			load_project_with_modify_check(fc.getSelectedFile().getAbsolutePath());
			
		}
	}
	
	private void load_project_with_modify_check(String f)
	{
		int option = 0;
		
		if (m_data.is_modified())
		{
			option = JOptionPane.showConfirmDialog(this,
					"Current project has not been saved. Load project anyway?", "Confirm",
					JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE);
		}
		if (option == 0)
		{
			load_project(f);
		}
	}
	

	private void toolbar_action(MenuActionType action)
	{
		switch(action)
		{
		case NEW_PROJECT:
			new_project();
			break;
		case SAVE_PROJECT:
			save_project();
			break;
		case LOAD_PROJECT:
			load_project();
			break;
		case QUIT:
			quit();
		case ABOUT:
			about();
			default:
				break;
		}
	}
	
	
	private class RecentActionListener implements ActionListener
	{
		String project_file;
	
		public RecentActionListener(String s) 
		{
			project_file = s;
		
		}
		
		public void actionPerformed(ActionEvent evt) 
		{
			load_project_with_modify_check(project_file);
		}
	}
	

	class ToolbarActionAdapter implements
			java.awt.event.ActionListener
	{
		private GodsEditor.MenuActionType action;
		
		ToolbarActionAdapter(MenuActionType action)
		{
			this.action = action;
		}

		public void actionPerformed(ActionEvent e)
		{
			toolbar_action(action);
		}
	}
	

}





