package toolbox;

import inputOutput.TextFileAccess;
import java.io.BufferedReader;

import array.tools.*;

public class FilterBEDEntryOnLength {

	public int minLength, maxLength; 
	
	/**
	 * Main class 
	 * @param minLength Defines minimal length of BED entry. 
	 * @param maxLength Defines maximal length of BED entry. 
	 * @param inputBED Path to BED file to be processed. 
	 */
	public FilterBEDEntryOnLength( final int minLength, final int maxLength, final String inputBED )
	{
		// Set instance variables
		this.minLength = minLength;
		this.maxLength = maxLength; 
		
		// Read input BED file
		readInput( inputBED );
		
		//Exit system with return code 0. 
		System.exit( 0 ); 
	}
	
	/**
	 * Method to parse BED file
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
			while( in.ready() )
			{
				// Read line by line, split into fields, store as String[] and pass to filter method. 
				line = in.readLine(); 
				entries = line.split("\t");  
				filter( entries );  
			}
		}
		catch (Exception e) 
		{
			System.err.println( "Unable to filter BED entries by length.." + e );
			System.err.println( "Current line: " + line ); 
			System.exit( 1 ); 
		}
	}
	
	/**
	 * Method to filter BED entry (row) on length. 
	 * @param entries String[] holding the fields of a single BED entry (row). 
	 */
	public void filter( String[] entries )
	{	
		// Calculate length of current entry  
		final int length = Integer.parseInt( entries[ 2 ] ) - Integer.parseInt( entries[ 1 ] ); 
		
		// Print BED entry to STDOUT if matching the length criteria
		if( length >= minLength && length <= maxLength )
			System.out.println( StringArrayTools.arrayToString( entries ) );
	}

	
	public static void main(String[] args) 
	{
		if( args.length != 3 )
		{
			final String info = "\n#######################\n" +
					"java -jar filterBEDEntryOnLength.jar -minLength=[x] -maxLength=[x] pathToInputBEDFile\n" +
					"#######################\n" + 
					"Filters a BED file (6 or 12-field) on the length of each entry. \n" +
					"Entries bigger or equal the mininum (-minLength=[x]) and maximum length (-maxLength=[x]) are returned to stdout\n" +
					"Store to file by redirecting stdout to file (java -jar filterBEDEntryOnLength.jar -minLength=[x] -maxLength=[x] pathToInputBEDFile > outputFile.BED)\n\n"; 
			System.err.println( info ); 
		}
		else 
		{
			int minLength = 0;
			int maxLength = 0;
			try
			{
				// Read minimal and maximal length from argument array. 
				minLength = Integer.parseInt( args[ 0 ].substring( args[ 0 ].indexOf("=") + 1 ) );
				maxLength = Integer.parseInt( args[ 1 ].substring( args[ 1 ].indexOf("=") + 1 ) );
			}
			catch (Exception e) 
			{
				// Arguments for minimal or maximal length are either absent or no integer. 
				System.err.println( "Please specify the desired splice site position as an integer. Positive from the start, negative from the end." );
				System.exit( 1 ); 
			}
			
			new FilterBEDEntryOnLength( minLength, maxLength, args[ 2 ] ); 
		}

	}

}
