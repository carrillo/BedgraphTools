package toolbox;

import infrastructure.BEDComparatorByPos;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import array.tools.StringArrayTools;
/**
 * 
 * @author carrillo
 *
 */
public class GetBidirectionalTranscriptPairs 
{
	public int maxDistance; 
	public ArrayList<String[]> entries; 
	
	/**
	 * @param maxDistance Integer defining the maximal distance between two adjacent, bidirectional TSSs
	 * @param in BufferReader reading the input stream. 
	 */
	public GetBidirectionalTranscriptPairs( final int maxDistance, BufferedReader in )
	{
		// Set instance variables, set maximal distance, read input BED file
		this.maxDistance = maxDistance; 
		this.entries = readInput( in );
		
		// Sort BED entries by ID
		Collections.shuffle( this.entries ); 
		Collections.sort(this.entries, new BEDComparatorByPos() );
		
		// Find and print pairs
		printPairs( this.entries ); 
	}
	
	/**
	 * This method reads the input BED file and stores its entries in an ArrayList of String[]. 
	 * @param in BufferReader reading STDIN
	 * @return ArrayList<String[]> holding all BED entries. Field values are stored as String[]. 
	 */
	public ArrayList<String[]> readInput( BufferedReader in )
	{
		// Initiate ArrayList holding each BED entry as a String[] of its field values
		ArrayList<String[]> entries = new ArrayList<String[]>(); 
		
		// Initiate first line as "No line parsed" to be 'printable' if exception is thrown. 
		String line = "No line parsed";  
		try 
		{
			while( in.ready() )
			{
				// Read BED file line by line, split at tab character and add to ArrayList
				line = in.readLine(); 
				entries.add( line.split("\t") ); 
			}
		}
		catch (Exception e) 
		{
			System.err.println( "Unable to read BED entries. " + e );
			System.err.println( "Current line: " + line ); 
			System.exit( 1 ); 
		}
		
		// Return ArrayList filled with BED entries. 
		return entries; 
	}
	
	/**
	 * 
	 * @param entries
	 */
	public void printPairs( ArrayList<String[]> entries )
	{
		for( int i = 0; i < entries.size() - 1; ++i )
		{ 
			//Check current and next (downstream gene) 
			String[] current = entries.get( i ); 
			String[] next = entries.get( i + 1 );  
			
			//Check if i) on same chr, ii) on opposite strands
			if( current[ 0 ].equals( next[ 0 ] ) && !current[ 5 ].equals( next[ 5 ] ) )
			{
				// Assign TSS of first gene
				int currentStart = -1;  
				if( current[ 5 ].equals("+"))
				{
					currentStart = Integer.parseInt( current[ 1 ] ); 					
				}
				else
				{
					currentStart = Integer.parseInt( current[ 2 ] );					
				}
				
				// Assign TSS of second gene
				int nextStart = -1;
				if( next[ 5 ].equals("+"))
				{
					nextStart = Integer.parseInt( next[ 1 ] ); 					
				}
				else 
				{
					nextStart = Integer.parseInt( next[ 2 ] );					
				}
				
				// Calculate distance between both TSSs 
				int distance = nextStart - currentStart; 
				
				// Print both BED files separated by ';' if their TSS are closer or equally spaced as given in the max distance variable.
				if( distance <= this.maxDistance && distance >= 0 )
				{
					System.out.println( StringArrayTools.arrayToString( current ) + ";" + StringArrayTools.arrayToString( next ) ); 
				}
			}
			
		}
	}
	
	public static void main(String[] args) 
	{
		if( args.length != 1 )
		{
			final String info = "\n#######################\n" +
					"java -jar getBidirectionalTranscriptPairs.jar -maxDistance=[int]\n" +
					"#######################\n" +
					"This script returns transcripts which are pointing away from each other (bidirectional) and which transcriptional start sites have a user defined (-maxDistance=[int]) maximal distance to each other.\n" +
					"Bidirectional transcript pairs are printed to STDOUT.\n" + 
					"Please provide an input BED file via STDIN\n" + 
					"Store to file by redirecting stdout to file (cat input.BED | java -jar getBidirectionalTranscriptPairs.jar -maxDistance=[int] > outputFile.BED)\n" +
					"#######################\n\n";  
			System.err.println( info ); 
		}
		else 
		{
			// Read maximal distance of TSSs from argument array.
			final int maxDistance = Integer.parseInt( args[ 0 ].substring( args[ 0 ].indexOf("=") + 1) );
			
			// Initiate and read input stream
			InputStreamReader inp = new InputStreamReader( System.in );
			BufferedReader in = new BufferedReader( inp );  
			
			// Start main 
			new GetBidirectionalTranscriptPairs( maxDistance, in ); 
		}

	}

}
