package gods.editor.level;

import gods.editor.ScrolledCanvas;

import java.io.IOException;


public class LevelCanvas extends ScrolledCanvas<EditLevel> {

	static final long serialVersionUID = 1;
	
	public LevelCanvas(EditLevel contents) 
	{
		super(contents);	
	}

	@Override
	public void update() throws IOException 
	{
		contents.update_map(false,true);
	}

}
