package gods.sys;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;

public abstract class GameEngine 
{
	private static final int CPUS = Runtime.getRuntime().availableProcessors();
	private static final int WAIT_MILLIS = 10;
	private static final int MAX_ELAPSED = WAIT_MILLIS * 16;
	private static final int MAX_FPS = 50 + (CPUS * 2);
	private static final int MIN_FPS = MAX_FPS / 2;
	
	private int m_max_fps = MAX_FPS;
	private JFrame m_window;
	private GameView m_canvas;
	private Rectangle m_useful_bounds;
	private Graphics2D m_graphics;
	private BufferedImage m_frame;
	private boolean m_cursor_state = true;
	private UserInputListener m_key_listener = null;
	private int m_exit_key;
	private int m_old_key_code = 0;
	private double m_scale_factor = 0.0;
	private boolean m_antialiasing = false;
	private boolean m_window_resize = false;
	
	public abstract void initResources();

	public abstract void render(Graphics2D arg0);

	public abstract void update(long arg0);
	
	
	public void hide_cursor()
	{
		set_cursor_state(false);
	}
	public void show_cursor()
	{
		set_cursor_state(true);
	}
	
	public void finish()
	{
		System.exit(0);
	}
	
	
	public boolean is_key_pressed(int key_code)
	{
		boolean rval = (m_old_key_code != key_code);
		boolean key_match = m_key_listener.key_code == key_code;
		
		if (rval)
		{
			rval = key_match;
			if (rval)
			{
				m_old_key_code = key_code;
			}			
		}
		else
		{
			if (!key_match)
			{
				m_old_key_code = 0;
			}
		}
		return rval;
	}
	
	public boolean is_key_down(int key_code)
	{		
		return m_key_listener.key_table[key_code & 0xff];
	}
	
	private void set_cursor_state(boolean flag)
	{
		if(m_cursor_state != flag)
		{
			m_cursor_state = flag;
			if(!m_cursor_state)
			{				
				BufferedImage bufferedimage = new BufferedImage(1,1, BufferedImage.TYPE_INT_ARGB);
				m_window.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(bufferedimage, 
						new Point(0, 0), "empty"));
			}
			else
			{
				m_window.setCursor(Cursor.getDefaultCursor());
			}
		}
	}
	public int get_width()
	{
		return m_useful_bounds.width;
	}
	
	public int get_height()
	{
		return m_useful_bounds.height;
	}
	public Rectangle get_display_bounds()
	{
		return m_useful_bounds;
	}
	public Window get_window()
	{
		return m_window;
	}
	
	/**
	 * Setup the screen
	 */
	public void setup(Rectangle useful_bounds, String title, int exit_key, Color background, boolean double_display, boolean antialiasing)
	{
		m_exit_key = exit_key;
		m_antialiasing = antialiasing;
		m_useful_bounds = new Rectangle(useful_bounds.width, useful_bounds.height);
		Dimension initSize = new Dimension(m_useful_bounds.width,m_useful_bounds.height);
		
		m_window = new JFrame(title);
		m_window.addWindowListener(new WindowCloseListener());
		m_window.setBackground(background);
		m_window.setIgnoreRepaint(true);
		m_window.setVisible(false);
		
		// Create canvas for rendering
		m_key_listener = new UserInputListener();
		m_canvas = new GameView();
		m_canvas.setBackground(background);
		m_canvas.setIgnoreRepaint(true);
		m_canvas.addKeyListener(m_key_listener);
		m_canvas.setAlignmentX(Component.CENTER_ALIGNMENT);
		m_canvas.setAlignmentY(Component.CENTER_ALIGNMENT);
		m_canvas.resize(double_display ? 2.0 : 1.0);
		
        // Center the content inside the frame
 		Container rootPane = m_window.getRootPane();
 		rootPane.setMinimumSize(initSize);
 		rootPane.setBackground(background);
 		rootPane.setIgnoreRepaint(true);
 		rootPane.setLayout(new BoxLayout(rootPane, BoxLayout.Y_AXIS));
 		rootPane.add(m_canvas);
 		rootPane.add(Box.createVerticalGlue());
        
 		// Bigger than necessary, will be resized
		m_window.pack();
		// Show game window in the middle of screen
		m_window.setLocationRelativeTo(null);
		m_window.setVisible(true);

		// Create double buffered frame for rendering
		m_frame = m_window.getGraphicsConfiguration()
				.createCompatibleImage(m_useful_bounds.width,m_useful_bounds.height,BufferedImage.TYPE_INT_RGB);
		m_graphics = m_frame.createGraphics();
		
		m_window.addComponentListener(new GameWindowListener());
		m_canvas.requestFocus();
		
		System.out.println("Game size: " + m_canvas.getWidth() + "x" + m_canvas.getHeight() + " - Scaling factor: " + m_scale_factor 
				+ " - CPUs: " + CPUS + " - Max FPS: " + m_max_fps);
	}

    public void start()
    {
    	long millis_interval = 1000 / m_max_fps;
    	
    	long clock1;
    	long clock2;
    	long elapsed_time = millis_interval;
    	long accumulated = 0;
    	
    	// init the resources
    	
    	initResources();
    	
    	// main loop
    	
   		clock1 = System.currentTimeMillis();
   		
  
   		while(true)
    	{
   			accumulated += millis_interval;
   			
   			if (is_key_down(m_exit_key))
   			{
   				finish();
   			}
   			
    		update(elapsed_time);
    		
    		if (accumulated >= 0)
    		{	
    			// reset all alpha channel & clip properties before calling
    			// custom render
    			m_graphics.setComposite(AlphaComposite.SrcOver);
    			m_graphics.setClip(m_useful_bounds);
   			
    			// call custom render method
     			render(m_graphics);
     			
     			// Do not refresh during resize
     			if (m_window_resize) {
     				m_window.revalidate();
    				m_window.pack();
    				m_window.repaint();
    				m_window_resize = false;
    			} else {
    				m_canvas.renderFrame(m_frame);
    			}
    		}

    		clock2 = System.currentTimeMillis();
    		
    		elapsed_time = clock2 - clock1;
    		
     		accumulated -= elapsed_time;
    		
     		if (accumulated > WAIT_MILLIS) {
    			try {
    				Thread.sleep(WAIT_MILLIS);
				} catch (InterruptedException e) {
					System.out.println(e);
				}
    		}
     		
     		long millis = System.currentTimeMillis();
     		
     		accumulated -= millis - clock2;
       		
     		clock2 = millis;
       		
       		elapsed_time = clock2 - clock1;
       		
       		clock1 = clock2;
       		
       		// safety to avoid too much delay between 2 updates
       		
       		if (elapsed_time > MAX_ELAPSED)
       		{
       			elapsed_time = MAX_ELAPSED;
       			accumulated = 0;
       		}

    	}
    }
    
    class GameWindowListener extends ComponentAdapter {

    	@Override
    	public void componentResized(ComponentEvent evt) {
    		Dimension winSize = evt.getComponent().getSize();
    		Insets winInsets = m_window.getInsets();
    		Dimension availableSize = new Dimension(winSize.width - winInsets.left - winInsets.right,
    				winSize.height - winInsets.top - winInsets.bottom);
    		double scaleWidth = (double)availableSize.width / m_useful_bounds.width;
    		double scaleHeight = (double)availableSize.height / m_useful_bounds.height;
    		m_canvas.resize(Math.min(scaleWidth, scaleHeight));
    	}
    }
    
    /**
     * Manage game view rendering and resizing.
     */
    class GameView extends JComponent {
    	
    	private static final long serialVersionUID = 1L;
    	
    	private Dimension viewSize;
    	private Rectangle viewBounds;
    	private AffineTransformOp scaleTransform;
		
    	@Override
    	public void paint(Graphics g) {
    		renderFrame((Graphics2D)g, m_frame);
    	}
    	
    	@Override
    	public boolean isOptimizedDrawingEnabled() {
    		return true;
    	}
    	
    	void renderFrame(BufferedImage frame) {
			 renderFrame((Graphics2D)getGraphics(), frame);
		}
		
		private void renderFrame(Graphics2D graphics, BufferedImage frame) {
			if (scaleTransform != null) {
				// Scale and draw the buffer
				if (m_antialiasing) {
					graphics.drawImage(frame, scaleTransform, 0, 0);
				}
				else {
					graphics.drawImage(frame, scaleTransform.getTransform(), null);
				}
			}
			else {
				// Draw the buffer on the screen
				graphics.drawImage(frame, 0, 0, null);
 			}
		}
		
		boolean resize(double actualScale) {
			if (actualScale == m_scale_factor) {
				return false;
			}
			int actualWidth = m_useful_bounds.width;
			int actualHeight = m_useful_bounds.height;			
			if (actualScale > 1.0) {
				actualWidth = (int)(m_useful_bounds.width * actualScale);
				actualHeight = (int)(m_useful_bounds.height * actualScale);
				scaleTransform = new AffineTransformOp(AffineTransform.getScaleInstance(actualScale, actualScale),
						AffineTransformOp.TYPE_BILINEAR);
				m_max_fps = Math.max(MIN_FPS, MAX_FPS - (int)((CPUS/2) * actualScale));
			} else {
				// Minimum size allowed
				actualScale = 1.0;
				scaleTransform = null;
				m_max_fps = MAX_FPS;
				Insets winInsets = m_window.getInsets();
	    		m_window.setMinimumSize(new Dimension(actualWidth + winInsets.left + winInsets.right,
	    				actualHeight + winInsets.top + winInsets.bottom));
			}
			
			m_scale_factor = actualScale;
			setPreferredSize(new Dimension(actualWidth, actualHeight));
			revalidate();
//			System.out.println("Game size: " + getWidth() + "x" + getHeight() +
//					" - Scaling factor: " + m_scale_factor + " - Max FPS: " + m_max_fps);
			return true;
		}
		
		void zoom(double delta) {
			double actualScale = m_scale_factor + delta;
			
			// Adjust window size if needed
			m_window_resize = resize(actualScale < 1.0 ? 1.0 : actualScale);
	    }
		
		/**
	     * Manages zoom key combinations:
	     * 
	     * 		[Shift] [+] 	zoom in
	     * 		[Shift] [-] 	zoom out
	     * 		[Shift] [=] 	reset zoom (same as [Shift] [0])
	     */
		@Override
		protected void processKeyEvent(KeyEvent evt) {
			if (!(evt.getID() == KeyEvent.KEY_RELEASED) || !evt.isShiftDown() || evt.isConsumed()
					|| m_window_resize || m_window.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
				super.processKeyEvent(evt);
				return;
			}
			double delta = 0.0;
			if (evt.getKeyCode() == KeyEvent.VK_PLUS || evt.getKeyCode() == KeyEvent.VK_ADD) {
				// Zoom in
				delta = 0.25;
			} else if (evt.getKeyCode() == KeyEvent.VK_MINUS || evt.getKeyCode() == KeyEvent.VK_SUBTRACT) {
				// Zoom out
				delta = -0.25;
			} else if (evt.getKeyCode() == KeyEvent.VK_EQUALS || evt.getKeyCode() == KeyEvent.VK_0) {
				// Restore default size
				delta = 1.0 - m_scale_factor;
			}
			m_canvas.zoom(delta);
			super.processKeyEvent(evt);
		}
		
		@Override
		public void setPreferredSize(Dimension size) {
			viewSize = size;
			viewBounds = new Rectangle(size.width, size.height);
		}
		
		@Override
		public Dimension getSize() {
			return viewSize;
		}
		
		@Override
		public Dimension getMaximumSize() {
			return viewSize;
		}
		
		@Override
		public Dimension getMinimumSize() {
			return viewSize;
		}
		
		@Override
		public Dimension getPreferredSize() {
			return viewSize;
		}
		
		@Override
		public Rectangle getBounds() {
			return viewBounds;
		}
		
		@Override
		public int getHeight() {
			return viewSize.height;
		}
		
		@Override
		public int getWidth() {
			return viewSize.width;
		}
    }
}
