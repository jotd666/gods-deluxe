package gods.editor;

import javax.swing.JPanel;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Graphics;

import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JSlider;
import java.awt.Canvas;

import gods.base.GfxFrame;
import gods.base.GfxFrameSet;

public class ViewGfxFrames extends JDialog {



	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JButton jButton = null;

	private JSlider m_slider = null;

	private Canvas m_canvas = null;

	private GfxFrameSet m_orig_frame_set = null;
	private GfxFrameSet m_symm_frame_set = null;
	
	private int m_width = 400;
	private int m_height = 400;
	
	public ViewGfxFrames()
	{
		this(null,null,null,400,400);
	}
	/**
	 * @param owner
	 */
	public ViewGfxFrames(Frame owner, GfxFrameSet gfs, 
			java.awt.image.BufferedImage source_2x, int width, int height) 
	{
		super(owner);
		m_width = width;
		m_height = height;
		m_orig_frame_set = new GfxFrameSet(source_2x,gfs);		
		m_symm_frame_set = new GfxFrameSet(m_orig_frame_set,GfxFrame.SymmetryType.mirror_left);		
		initialize();
		m_slider.setMinimum(0);
		m_slider.setMaximum(m_orig_frame_set.get_nb_frames()-1);

	}


	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() 
	{
		this.setSize(m_width, m_height);
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) 
		{
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJButton(), BorderLayout.SOUTH);
			jContentPane.add(get_slider(), BorderLayout.NORTH);
			jContentPane.add(get_canvas(), BorderLayout.CENTER);
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
			jButton.setText("Close");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) 
				{
					dispose();
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes m_slider	
	 * 	
	 * @return javax.swing.JSlider	
	 */
	private JSlider get_slider() {
		if (m_slider == null) 
		{
			m_slider = new JSlider();
			
			m_slider.addChangeListener(new javax.swing.event.ChangeListener()
			{
				public void stateChanged(javax.swing.event.ChangeEvent e)
				{
					m_canvas.repaint();
				}
			});
		}
		return m_slider;
	}

	private class AnimationCanvas extends Canvas
	{
		private static final long serialVersionUID = 1L;
		public void paint(Graphics g)
		{
			int f = m_slider.getValue();
			java.util.Vector<GfxFrame> frames = m_orig_frame_set.get_frames();
			java.awt.image.BufferedImage image = frames.elementAt(f).toImage();
						
			g.drawImage(image,0,0,null);

			frames = m_symm_frame_set.get_frames();
			image = frames.elementAt(f).toImage();

			g.drawImage(image,image.getWidth()+1,0,null);
		}
	}
	/**
	 * This method initializes button	
	 * 	
	 * @return java.awt.Button	
	 */
	private Canvas get_canvas() {
		if (m_canvas == null) {
			m_canvas = new AnimationCanvas();
			m_canvas.setSize(m_orig_frame_set.get_width() * 2 + 1, m_orig_frame_set.get_height());			
		}
		return m_canvas;
	}

}  //  @jve:decl-index=0:visual-constraint="175,17"
