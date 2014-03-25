package toolbox;

import inputOutput.TextFileAccess;

import java.io.BufferedReader;
import java.util.ArrayList;

import array.tools.StringArrayTools;

public class FilterExonsOnPosition {

	public int exonPos; 
	
	/**
	 * @param exonPos Integer defining the exon position relative to start (positive) or end (negative)
	 * @param inputBED Path to exon BED file to be processed.
	 */
	public FilterExonsOnPosition( final int exonPos, final String inputBED )
	{
		this.exonPos = exonPos;
		
		// Read input BED file
		readInput( inputBED );
		
		//Exit system with return code 0.
		System.exit( 0 ); 
	}
	
	/**
	 * Method to parse BED file and pass BED entries to filter method
	 * @param inputBED Path to BED file to be processed.
	 */
	public void readInput( final String inputBED )
	{
		// Open input Stream.
		BufferedReader in = TextFileAccess.openFileRead( inputBED );
		// Initiate first line as "No line parsed" to be 'printable' if exception is thrown.
		String line = "No line parsed";
		try
		{
			String[] entries; 
			
			// Initiate previous and current ID to check if exons stem from the same transcript. 
			String previousId = "";
			String currentId = ""; 
			// Initiate ArrayList holding all exons of one transcript. 
			ArrayList<String[]> exons = new ArrayList<String[]>();
			
			while( in.ready() )
			{
				// Read by line and split at tabs
				line = in.readLine(); 
				entries = line.split("\t"); 
				
				// Assign transcript portion of the exonID to 'currentId' 
				currentId = entries[ 3 ].substring(0, entries[ 3].indexOf("Exon") -1 ); 
				
				// Add exons to array if from the same transcript. If not filter the old ArrayList and initiate a new one. 
				if( currentId.equals( previousId ) )
				{
					exons.add( entries ); 
				}
				else
				{
					filter( exons );  						
					exons = new ArrayList<String[]>(); 
					exons.add( entries ); 
				}
				
				previousId = currentId; 
			}
		}
		catch (Exception e) 
		{
			System.err.println( "Unable to extract exon. " + e );
			System.err.println( "Current line: " + line );
			System.exit( 1 ); 
		}
	}
	
	/**
	 * This method filters an ArrayList of exons on position. 
	 * @param exons ArrayList<String[]> holding a set of BED entries split by tab. 
	 */
	public void filter( ArrayList<String[]> exons )
	{	
		// Check if the desired relative position of the exon is available in the transcript. 
		if( exons.size() >= Math.sqrt( Math.pow(exonPos, 2) ) )
		{
			// Take care of strandness.
			final boolean plus = exons.get( 0 )[ 5 ].equals("+"); 
			if( plus )
			{
				// Take care of exon position relative to gene start (>0) or gene end (<0) , print each exon as BED entry to STDOUT 
				if( exonPos >  0 )
				{
					String[] exon = exons.get( exonPos - 1 ); 
					if( !exon[ 3 ].contains("Exon1_") )
						System.out.println( StringArrayTools.arrayToString( exon ) ); 
						
					System.out.println( StringArrayTools.arrayToString( exons.get( exonPos - 1) ) );
				}
				else 
				{
					System.out.println ( StringArrayTools.arrayToString( exons.get( exons.size() + exonPos ) ) ); 
				}
			}
			else
			{	
				if( exonPos > 0 )
				{
					System.out.println( StringArrayTools.arrayToString( exons.get( exons.size() - exonPos ) ) ); 
				}
				else
				{
					System.out.println( StringArrayTools.arrayToString( exons.get(  - ( exonPos + 1 ) ) ) );
				}
				
			}
		}
	}
	
	
	public static void main(String[] args) 
	{
		if( args.length != 2 )
		{
			final String info = "\n#######################\n" +
					"java -jar filterExonsOnPosition -exonPos=[x] pathToExonBEDFile\n" +
					"#######################\n" + 
					"Filters a BED file produced by the 'getExons' script to retrieve all exons at user defined transcript positions.\n" +
					"Please define the exon position relative to the start (positive integer) or end (negative integer)\n" +
					"For example -exonPos=2 returns the second exon of the transcript. -exonPos=-3 returns the third last exon.\n" +
					"If no exon matches the criterium for the given transcript the transcript will be skipped.\n" +
					"The result is printed to STDOUT. Store to file by redirecting (java -jar filterExonsOnPosition -exonPos=[x] pathToInputBEDFile > outputFile.BED)\n\n";
			System.err.println( info ); 
		}
		else 
		{
			int exonPos = 0; 
			try
			{
				// Read exon position from argument array.
				exonPos = Integer.parseInt( args[ 0 ].substring( args[ 0 ].indexOf("=") + 1 ) );
			}
			catch (Exception e) 
			{
				// Argument for exon position is no integer.
				System.err.println( "Please specify the desired exon position as an integer. Positive from the start, negative from the end. No zero allowed." );
				System.exit( 1 ); 
			}
			
			// Check if exon position value is not zero
			if( exonPos == 0 )
			{
				System.err.println( "Please specify the desired exon position as an integer. Positive from the start, negative from the end. No zero allowed." );
				System.exit( 1 );
			}
			else 
			{
				new FilterExonsOnPosition( exonPos, args[ 1 ] ); 				
			}
		}

	}

}
