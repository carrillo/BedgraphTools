package toolbox;


import infrastructure.BEDgraphEntry;
import inputOutput.TextFileAccess;

import java.io.BufferedReader;
import java.util.ArrayList;

public class CompressBedgraph 
{ 
	/**
	 * Construct a CompressBedgraph class. Define the bedgraph file. 
	 * @param bedgraph
	 */
	public CompressBedgraph( final String bedgraph )
	{ 
		compress( bedgraph );
		System.exit( 0 ); 
	}
	
	public void compress( final String bedgraph )
	{
		//Open file, define starting parameters for line and chrId
		BufferedReader in = TextFileAccess.openFileRead( bedgraph ); 
		String line = "Empty first line";
		String chrId = "";
		
		//Initiate list holding the current bedgraph entries. 
		ArrayList<BEDgraphEntry> valueArray = new ArrayList<BEDgraphEntry>(); 
		try
		{ 
			while( in.ready() )
			{ 
				line = in.readLine();
				
				//Add the current bedgraph entry to the array. 
				valueArray = addAndShift( valueArray, line ); 
				 
				//Check if the current entry's chromosome matches the last entry's one.  
				if( !valueArray.get( valueArray.size() - 1 ).getChrom().equals( chrId ) )
				{
					// Check the current entry represents the first line. 
					if( !chrId.equals("") )	
					{ 
						valueArray = processEndOfChromosome( valueArray ); 
					}
					
					//Assign current entry's chromosome id to chrId
					chrId = valueArray.get( valueArray.size() -1 ).getChrom(); 
				}
				
				if( valueArray.size() == 1 )
				{
						//printOutOfBoundaryValues( valueArray ); 
				}
				
				//If everything is 'normal' compress value array. 
				else 
				{
					 	valueArray = compressValueArray( valueArray ); 			
				}
				
			}
			//Process last lines
			processEndOfFile( valueArray ); 
		}
		catch (Exception e) 
		{
			System.err.println( "Cannot process the bedgraph entry: ." + line );
			System.exit( 1 ); 
		} 
	}
	
	/**
	 * Compress the value array using run length encoding (i.e. represent two neighboring positions with the same score as one longer entry)
	 * @param input
	 * @return
	 */
	public ArrayList<BEDgraphEntry> compressValueArray( final ArrayList<BEDgraphEntry> input )
	{
		//Check if entries are neighbors
		if( input.get( 0 ).getChromEnd() == input.get( 1 ).getChromStart() )
		{
			//Check if neighbors have the same score
			if( input.get( 0 ).getScore() == input.get( 1 ).getScore() )
			{
				input.get( 1 ).setChromStart( input.get( 0 ).getChromStart() ); 				
			}
			else
			{
				System.out.println( input.get( 0 ) );
			}
			
		}
		else
		{
			System.out.println( input.get( 0 ) ); 
		}
		return input; 
	}
	
	/**
	 * Adds a value to the input arraylist. Keep the number of entries == 2, by removing the oldest entry.   
	 * @param input
	 * @param line
	 * @return
	 */
	public ArrayList<BEDgraphEntry> addAndShift( ArrayList<BEDgraphEntry> input, final String line )
	{
		BEDgraphEntry entry = new BEDgraphEntry( line );  
		
		input.add( entry ); 
		
		if( input.size() > 2 )
		{
			input.remove( 0 ); 			
		}
		
		return input; 
	}
	
	/**
	 * Process the last lines representing one chromosome. Return the list for the next chromosome. 
	 * @param input
	 * @return
	 */
	public ArrayList<BEDgraphEntry> processEndOfChromosome( final ArrayList<BEDgraphEntry> input )
	{
		
		ArrayList<BEDgraphEntry> newChr = new ArrayList<BEDgraphEntry>(); 
		newChr.add( input.get( 1 ) ); 
		
		ArrayList<BEDgraphEntry> oldChr = new ArrayList<BEDgraphEntry>();
		oldChr.add( input.get( 0 ) ); 
		printOutOfBoundaryValues( oldChr ); 
		
		return newChr; 
	}
	
	/**
	 * Process the last lines of the bedgraph file. 
	 * @param input
	 * @return
	 */
	public void processEndOfFile( final ArrayList<BEDgraphEntry> input )
	{
		ArrayList<BEDgraphEntry> oldChr = new ArrayList<BEDgraphEntry>();
		oldChr.add( input.get( 1 ) ); 
		printOutOfBoundaryValues( oldChr ); 
		 
	}
	
	/**
	 * Print the defined OUT_OF_BOUNDARY_VALUE as score for all BEDgraphEntries in the input list. 
	 * @param values
	 */
	public void printOutOfBoundaryValues( final ArrayList<BEDgraphEntry> values )
	{
		for( BEDgraphEntry e : values )
		{
			//e.setScore( OUT_OF_BOUNDARY_VALUE ); 
			System.out.println( e ); 
		}
	}
	
	/**
	 * Main method used for running this script from shell
	 * @param args
	 */
	public static void main(String[] args) 
	{
		if( args.length != 1 )
		{
			final String info = "\n#######################\n" + 
					"java -jar compressBedgraph bedgraphFile\n" +
					"#######################\n" + 
					"Compresses the bedgraph using run length encoding.\n" +
					"\n\n"; 
			System.err.println( info ); 
		}
		else 
		{	
			new CompressBedgraph( args[ 0 ] );
		}		
	}

}
