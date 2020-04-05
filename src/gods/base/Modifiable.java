package gods.base;

public interface Modifiable extends Describable {
	boolean is_modified();
	void set_modified(boolean m);
}
