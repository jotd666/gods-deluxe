package gods.game;

import java.io.IOException;
import java.util.Arrays;

import gods.base.DirectoryBase;
import gods.base.GameOptions;
import gods.sys.ParameterParser;


public class RecordScore 
{
	public final static int NB_SCORES = 10;

	private ScoreEntry [] m_score_entry = new ScoreEntry[NB_SCORES];
	
	public RecordScore(ParameterParser fr) throws IOException
	{
		parse(fr);
	}
	public ScoreEntry [] get_entries()
	{
		return m_score_entry;
	}
	
	public RecordScore()
	{
		for (int i = 0; i < m_score_entry.length; i++)
		{
			m_score_entry[i] = new ScoreEntry();
		}
	}
	
	public boolean is_high_score(int score)
	{
		return score > m_score_entry[NB_SCORES-1].score;
	}
	
	public boolean insert(String name, int score)
	{
		boolean rval = false;
		
		for (int i = 0; i < m_score_entry.length; i++)
		{
			ScoreEntry se = m_score_entry[i];
			
			if (se.score < score)
			{
				se.score = score;
				se.player = name;
				rval = true;
				break;
			}
		}
		
		return rval;
	}
	public String get_score_file_name()
	{
		return DirectoryBase.get_data_path()+"hiscores_"+GameOptions.instance().get_current_level_set_name();
	}
	public void save()
	{
		try 
		{
			ParameterParser fw = ParameterParser.create(get_score_file_name());

			serialize(fw);

			fw.close();
		} 
		catch (IOException e)
		{
			
		}
	}
	
	public void load()
	{
		try {
			ParameterParser fr = ParameterParser.open(get_score_file_name());

			parse(fr);

			fr.close();
		} 
		catch (IOException e) 
		{

		}
	}
	
	private void parse(ParameterParser fr) throws IOException
	{
		fr.startBlockVerify("HISCORES");
		for (int i = 0; i < m_score_entry.length; i++)
		{
			m_score_entry[i] = new ScoreEntry();
		}
		for (ScoreEntry se : m_score_entry)
		{
			se.parse(fr);
		}
		fr.endBlockVerify();
		
		sort();
	}
	private void serialize(ParameterParser fw) throws IOException
	{
		fw.startBlockWrite("HISCORES");
		for (ScoreEntry se : m_score_entry)
		{
			se.serialize(fw);
		}
		fw.endBlockWrite();	

	}

	private void sort()
	{
		Arrays.sort(m_score_entry);
	}
	
	public boolean set(String player,int challenging_score)
	{
		sort();

		ScoreEntry se = m_score_entry[NB_SCORES-1];
				
		boolean rval = (se.score < challenging_score);
		
		if (rval)
		{
			se.score = challenging_score;
			se.player = player;

			// sort required
			
			sort();
		}
		return rval;
	}

	
}


