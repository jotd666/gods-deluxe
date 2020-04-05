package gods.base;

public interface EditableData extends Modifiable 
{
	public enum GfxMode { EDITOR, REWORKED_GAME, ORIGINAL_GAME, ORIGINAL_GAME_SCALE_2X }

	public void load(String project_file, GfxMode gfx_mode) throws java.io.IOException;
	public boolean save() throws java.io.IOException;
	public void new_project(String file);
	public String get_project_file();
	public void set_project_file(String file);
	public void export(String file);
	
}
