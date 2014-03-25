package infrastructure;

import java.util.ArrayList;

public class BEDgraphEntry 
{
	protected String chrom; 
	protected int chromStart, chromEnd; 
	protected float score; 
	
	/**
	 * Generate BEDgraphEntry from BED graph line 
	 * @param entryLine
	 */
	public BEDgraphEntry( final String entryLine )
	{
		parseEntryLine( entryLine );
	}
	
	/**
	 * Generate BEDgraphEntry from individual position information. 
	 * @param entryLine
	 */
	public BEDgraphEntry( final String chrom, final int chromStart, final int chromEnd, final float score )
	{
		this.chrom = chrom.trim(); 
		this.chromStart = chromStart; 
		this.chromEnd = chromEnd; 
		this.score = score; 
	}
	
	/**
	 * Extract position info from BED graph entry. 
	 * @param entryLine
	 */
	public void parseEntryLine( final String entryLine )
	{
		final String[] entries = entryLine.split("\t"); 
		this.chrom = entries[ 0 ].trim(); 
		this.chromStart = Integer.parseInt( entries[ 1 ].trim() ); 
		this.chromEnd = Integer.parseInt( entries[ 2 ].trim() );
		this.score = Float.parseFloat( entries[ 3 ].trim() ); 
	}
	
	/**
	 * Extract all positions contained in this BEDgraphentry as a List of BEDgraphentries. 
	 * @return
	 */
	public ArrayList<BEDgraphEntry> expandPositions()
	{
		ArrayList<BEDgraphEntry> entryList = new ArrayList<BEDgraphEntry>(); 
		
		for( int i = getChromStart(); i < getChromEnd(); i++ )
		{
			entryList.add( new BEDgraphEntry( getChrom(), i, (i+1), getScore() ) ); 
		}
		
		return entryList; 
	}
	
	public String toString() { return getChrom() + "\t" + getChromStart() + "\t" + getChromEnd() + "\t" + getScore(); }
	
	// Getter and setter
	public String getChrom() { return chrom; }
	public void setChromStart( final int chromStart ) { this.chromStart = chromStart; }  
	public int getChromStart() { return chromStart; } 
	public int getChromEnd() { return chromEnd; }
	public void setChromEnd( final int chromEnd ) { this.chromEnd = chromEnd; } 
	public int getPositionSpanning() { return getChromEnd() - getChromStart(); } 
	
	
	
	public float getScore() { return score; }
	public void setScore( final float score ) { this.score = score; }
	
}
