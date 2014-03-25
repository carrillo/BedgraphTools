package toolbox;

import inputOutput.TextFileAccess;

import java.io.BufferedReader;
import java.util.ArrayList;

import array.tools.StringArrayTools;

public class FilterIntronsOnPosition {

	public int intronPos; 
	
	/**
	 * @param intronPos Integer defining the intron position relative to start (positive) or end (negative)
	 * @param inputBED Path to exon BED file to be processed.
	 */
	public FilterIntronsOnPosition( final int intronPos, final String inputBED )
	{
		this.intronPos = intronPos; 
		
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
			
			// Initiate previous and current ID to check if introns stem from the same transcript.
			String previousId = "";
			String currentId = ""; 
			// Initiate ArrayList holding all introns of one transcript.
			ArrayList<String[]> introns = new ArrayList<String[]>();
			
			while( in.ready() )
			{
				// Read by line and split at tabs
				line = in.readLine();  
				entries = line.split("\t"); 
				
				// Assign transcript portion of the intronID to 'currentId' 
				currentId = entries[ 3 ].substring(0, entries[ 3].indexOf("Intron") -1 );  
				
				// Add introns to array if from the same transcript. If not filter the old ArrayList and initiate a new one.
				if( currentId.equals( previousId ) )
				{
					introns.add( entries ); 
				}
				else
				{ 
					filter( introns );  						
					introns = new ArrayList<String[]>(); 
					introns.add( entries ); 
				}
				
				previousId = currentId; 
			}
		}
		catch (Exception e) 
		{
			System.err.println( "Unable to extract intron. " + e );
			System.err.println( "Current line: " + line );
			System.exit( 1 );
		}
	}
	
	/**
	 * This method filters an ArrayList of introns on position. 
	 * @param introns ArrayList<String[]> holding a set of BED entries split by tab. 
	 */
	public void filter( ArrayList<String[]> introns )
	{	
		// Check if the desired relative position of the intron is available in the transcript.
		if( introns.size() >= Math.sqrt( Math.pow(intronPos, 2) ) )
		{
			// Take care of strandness.
			final boolean plus = introns.get( 0 )[ 5 ].equals("+");
			if( plus )
			{
				// Take care of intron position relative to gene start (>0) or gene end (<0) , print each intron as BED entry to STDOUT
				if( intronPos >  0 )
				{
					String[] exon = introns.get( intronPos - 1 ); 
					if( !exon[ 3 ].contains("Exon1_") )
						System.out.println( StringArrayTools.arrayToString( exon ) ); 
						
					System.out.println( StringArrayTools.arrayToString( introns.get( intronPos - 1) ) );
				}
				else 
				{
					System.out.println ( StringArrayTools.arrayToString( introns.get( introns.size() + intronPos ) ) ); 
				}
			}
			else
			{	
				if( intronPos > 0 )
				{ 
					System.out.println( StringArrayTools.arrayToString( introns.get( introns.size() - intronPos ) ) ); 
				}
				else
				{
					System.out.println( StringArrayTools.arrayToString( introns.get(  - ( intronPos + 1 ) ) ) );
				}
				
			}
		}
	}
	
	
	public static void main(String[] args) 
	{
		if( args.length != 2 )
		{
			final String info = "\n#######################\n" +
					"java -jar filterIntronsOnPosition -intronPos=[x] input.BED\n" +
					"#######################\n" + 
					"Filters a BED file produced by the 'getIntrons' script to retrieve all introns at user defined transcript positions.\n" +
					"Please define the intron position relative to the start (positive integer) or end (negative integer)\n" +
					"For example -intronPos=2 returns the second intron of the transcript. -intronPos=-3 returns the third last intron.\n" +
					"If no intron matches the criterium for the given transcript the transcript will be skipped.\n" +
					"The result is printed to STDOUT. Store to file by redirecting (java -jar filterIntronOnPosition -intronPos=[x] pathToInputBEDFile > outputFile.BED)\n\n";
			System.err.println( info ); 
		}
		else 
		{
			int intronPos = 0; 
			try
			{
				// Read intron position from argument array.
				intronPos = Integer.parseInt( args[ 0 ].substring( args[ 0 ].indexOf("=") + 1 ) );
			}
			catch (Exception e) 
			{
				// // Argument for exon position is no integer.
				System.err.println( "Please specify the desired intron position as an integer. Positive from the start, negative from the end. No zero allowed." );
				System.exit( 1 ); 
			}
			
			// Check if intron position value is not zero
			if( intronPos == 0 )
			{
				System.err.println( "Please specify the desired intron position as an integer. Positive from the start, negative from the end. No zero allowed." );
				System.exit( 1 ); 
			}
			else 
			{
				new FilterIntronsOnPosition( intronPos, args[ 1 ] ); 				
			}
						
		}

	}

}
