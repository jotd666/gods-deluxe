package gods.base;

public interface Describable
{
	void serialize(gods.sys.ParameterParser fw) throws java.io.IOException;
	String get_block_name();
}
