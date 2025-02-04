package gods.game;

public class Version
{
	public static String current() {
		String version = Version.class.getPackage().getImplementationVersion();
        return version == null ? "<unpacked>" : version;
	}
}
