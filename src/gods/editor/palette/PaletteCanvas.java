package gods.editor.palette;

import gods.editor.ScrolledCanvas;

import java.io.IOException;


public class PaletteCanvas extends ScrolledCanvas<EditGfxPalette> {

	private static final long serialVersionUID = 1L;
	
	public PaletteCanvas(EditGfxPalette contents) 
	{
		super(contents);	
	}

	@Override
	public void update() throws IOException 
	{
		contents.load_image();

	}

}
