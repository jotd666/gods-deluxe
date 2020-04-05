package gods.base;

import java.util.Comparator;

public class GfxFrameSetLocationComparator implements Comparator<GfxFrameSet> 
{

	public int compare(GfxFrameSet arg0, GfxFrameSet arg1) {
		int rval = arg0.distance_to_zero() - arg1.distance_to_zero();
		if (rval == 0)
		{
			rval = arg0.compareTo(arg1);
		}
		return rval;
	}

}
