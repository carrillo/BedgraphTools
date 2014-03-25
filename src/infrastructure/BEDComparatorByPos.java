package infrastructure;

import java.util.Comparator;

/**
 * This comparator compares BED entries by chromosomal position. 
 * @author carrillo
 *
 */
public class BEDComparatorByPos implements Comparator<String[]> 
{

	public int compare(String[] arg0, String[] arg1) 
	{
		int chrComp;
		try 
		{
			final int chrId0 = Integer.parseInt( arg0[ 0 ].substring( 3 ) );
			final int chrId1 = Integer.parseInt( arg1[ 0 ].substring( 3 ) );
			
			if( chrId0 > chrId1 )
				chrComp = 1; 
			else if( chrId0 < chrId1 )
				chrComp = -1;
			else
				chrComp = 0; 
		}
		catch (Exception e) 
		{
			chrComp = arg0[ 0 ].compareTo( arg1[ 0 ] ); 
		}
		if( chrComp != 0 )
			return chrComp; 
		else 
		{
			final int start0 = Integer.parseInt( arg0[ 1 ] );
			final int start1 = Integer.parseInt( arg1[ 1 ] );
			if( start0 > start1 )
				return 1; 
			else if( start0 < start1 )
				return -1; 
			else 
				return 0; 
		}
	}

}
