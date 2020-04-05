package gods.game;

import java.io.IOException;

import gods.sys.ParameterParser;

public class ScoreEntry implements Comparable<ScoreEntry>
{
	public int compareTo(ScoreEntry other)
	{
		return other.score - score;
	}
	
	public String player;
	public int score;
	
	public ScoreEntry()
	{
		this("BITMAP BROS RULEZ",0);
	}
	public ScoreEntry(String driver, int score)
	{
		this.player = driver;
		this.score = score;
	}
	public void parse(ParameterParser fr) throws IOException
	{
		fr.startBlockVerify("entry");
		player = fr.readString("player");
		score = fr.readInteger("score");
		fr.endBlockVerify();
	}
	public void serialize(ParameterParser fw) throws IOException
	{
		fw.startBlockWrite("entry");
		fw.write("player",player.equals("") ? "WHO" : player);
		fw.write("score",score);
		fw.endBlockWrite();
	}
}
