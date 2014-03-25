package toolbox;


import infrastructure.BEDgraphEntry;
import inputOutput.TextFileAccess;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Iterator;

public class DecompressBedgraph 
{ 	
	protected static float FILL_EMPTY_POS_VALUE = 0f; 
	
	/**
	 * Construct a DecompressBedgraph class. Define the bedgraph file. 
	 * @param bedgraph
	 */
	public DecompressBedgraph( final String bedgraph )
	{   
		decompressBedgraph( bedgraph );
	}
	
	public void decompressBedgraph( final String bedgraph )
	{ 
	
		//Open file, define starting parameters for line and lastEntry
		BufferedReader in = TextFileAccess.openFileRead( bedgraph ); 
		String line = "Empty first line";
		BEDgraphEntry lastEntry = new BEDgraphEntry( "empty", 0, 1, 0 );
		
		try
		{ 
			while( in.ready() )
			{ 
				line = in.readLine();
				BEDgraphEntry entry = new BEDgraphEntry( line ); 
				//Decompress the current entry. 
				lastEntry = processEntry( lastEntry, entry ); 
			} 
		}
		catch (Exception e) 
		{
			System.err.println( "Cannot process the bedgraph entry: ." + line );
			System.exit( 1 ); 
		} 
	}
	
	/**
	 * Decompress the current entry. The distance between the current and last entry will be filled
	 * with the value stored in FILL_EMPTY_POS_VALUE.  
	 * @param lastEntry
	 * @param line
	 * @return
	 */
	public BEDgraphEntry processEntry( BEDgraphEntry lastEntry, final BEDgraphEntry entry )
	{
		//Check if both entries are on the same chromosome, but are separated by 'empty' positions. 
		if( lastEntry.getChrom().equals( entry.getChrom() ) ) 
			fillSpaceBetweenNeighbours( lastEntry, entry ); 
		
		//Decompress the last entry (i.e. undo run length encoding). 
		lastEntry = decompress( lastEntry, entry ); 
		
		return lastEntry; 
	}
	
	/**
	 * Fill space between two neighbors with FILL_EMPTY_POS_VALUE
	 * @param lastEntry
	 * @param entry
	 */
	public void fillSpaceBetweenNeighbours( final BEDgraphEntry lastEntry, final BEDgraphEntry entry )
	{ 
		//Check if space between two neighbors. If so, fill with FILL_EMPTY_POS_VALUE
		if( lastEntry.getChromEnd() != entry.getChromStart() )
		{
			int newStart = lastEntry.getChromEnd(); 
			while( newStart != entry.getChromStart() )
			{
				System.out.println( new BEDgraphEntry( entry.getChrom(), newStart, ( newStart + 1 ), FILL_EMPTY_POS_VALUE ) ); 
				newStart++; 
			}
		}
	}
	
	/**
	 * Undo run length encoding. I.e. represent each bedgraph entry as a list of bedgraph entries for each position.    
	 * @param lastEntry
	 * @param entry
	 * @return
	 */
	public BEDgraphEntry decompress( BEDgraphEntry lastEntry, final BEDgraphEntry entry )
	{
		if( entry.getPositionSpanning() == 1 )
		{
			System.out.println( entry ); 
			lastEntry = entry; 
		}
		else
		{
			ArrayList<BEDgraphEntry> expandedEntries = entry.expandPositions();
			Iterator<BEDgraphEntry> i = expandedEntries.iterator();  
			int index = -1; 
			while( i.hasNext() )
			{
				index++; 
				System.out.println( i.next() );
			}
			lastEntry = expandedEntries.get( index );
		}
		
		return lastEntry; 
	}
	
	/*
	public void printOutOfBoundaryValues( final ArrayList<BEDgraphEntry> values )
	{
		for( BEDgraphEntry e : values )
		{
			//e.setScore( OUT_OF_BOUNDARY_VALUE ); 
			System.out.println( e ); 
		}
	}
	
	public ArrayList<BEDgraphEntry> processEndOfChromosome( final ArrayList<BEDgraphEntry> input )
	{
		
		ArrayList<BEDgraphEntry> newChr = new ArrayList<BEDgraphEntry>(); 
		newChr.add( input.get( 1 ) ); 
		
		ArrayList<BEDgraphEntry> oldChr = new ArrayList<BEDgraphEntry>();
		oldChr.add( input.get( 0 ) ); 
		printOutOfBoundaryValues( oldChr ); 
		
		return newChr; 
	}
	public void processEndOfFile( final ArrayList<BEDgraphEntry> input )
	{
		ArrayList<BEDgraphEntry> oldChr = new ArrayList<BEDgraphEntry>();
		oldChr.add( input.get( 1 ) ); 
		printOutOfBoundaryValues( oldChr ); 
	}
	*/
	
	/**
	 * Main method used for running this script from shell
	 * @param args
	 */
	public static void main(String[] args) 
	{
		if( args.length != 1 )
		{
			final String info = "\n#######################\n" + 
					"java -jar decompressBedgraph bedgraphFile\n" +
					"#######################\n" + 
					"This tool decompresses bedgraphs.\n" +
					"Expands bedgraph entries spanning more than one positions and fills up empty positions\n" +
					"Outputs the decompressed bedgraph to STDOUT.\n" +
					"\n\n"; 
			System.err.println( info ); 
		}
		else 
		{	
					
			new DecompressBedgraph( args[ 0 ] );
			
		}		
	}

}
