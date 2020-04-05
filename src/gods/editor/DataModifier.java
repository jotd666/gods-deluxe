package gods.editor;

public interface DataModifier<T>
{
	void gui_to_data() throws Exception;
	void data_to_gui();
	void on_close();
	T get_data();
}
