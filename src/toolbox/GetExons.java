package toolbox;

import inputOutput.TextFileAccess;
import java.io.BufferedReader;


public class GetExons 
{
	public int rangeUp, rangeDo; 
	
	/**
	 * @param rangeUp Integer defining number of nucleotide the exon will be extended upstream
	 * @param rangeDo Integer defining number of nucleotide the exon will be extended downstream
	 * @param bedFile Path to BED file to be processed.
	 */
	public GetExons( final int rangeUp, final int rangeDo, final String bedFile )
	{ 
		this.rangeUp = rangeUp; 
		this.rangeDo = rangeDo; 
		
		// Read input BED file
		readInput( bedFile ); 
		
		//Exit system with return code 0. 
		System.exit( 0 ); 
	}
	
	/**
	 * Method to parse BED file and pass BED entries to filter method
	 * @param bedFile Path to BED file to be processed.
	 */
	public void readInput( final String bedFile )
	{
		// Open input Stream. 
		BufferedReader in = TextFileAccess.openFileRead( bedFile );
		// Initiate first line as "No line parsed" to be 'printable' if exception is thrown.
		String line = "No line parsed";
		try
		{
			String[] entries; 
			while( in.ready() )
			{ 
				// Read line by line, split into fields, store as String[] and pass to exon extraction method.
				line = in.readLine(); 
				entries = line.split("\t");  
				printExons( entries );  					
			}
		}
		catch (Exception e) 
		{
			System.err.println( "Unable to extract exon from BED file. " + e );
			System.err.println( "Current line: " + line );
			System.exit( 1 ); 
		}
		
		System.exit( 0 ); 
	}
	
	/**
	 * This method extracts all exons from a given BED entry and prints a BED entry for each exon to STDOUT 
	 * @param entries String[] holding the fields of a single BED entry (row).
	 */
	public void printExons( final String[] entries )
	{ 
		//Extract String[] holding start values (blockStart) and length (blockSize) of all exons.  
		final String[] blockStart = entries[ 11 ].split(",");
		final String[] blockSize = entries[ 10 ].split(",");
		
		int start, end;
		int startExon = 0;
		int endExon = 0; 
		
		//Assign absolute value for gene start. Exon starts in blockStart are relative to this value. 
		final int geneStart = Integer.parseInt( entries[ 1 ] );  
		
		//Loop through all exons (Exon count is contained in BED field 10). 
		for( int i = 0; i < Integer.parseInt( entries[ 9 ] ); i++ )
		{
			//Assign absolute start and end values for each exon. This value doesn't consider strandness
			start = geneStart + Integer.parseInt( blockStart[ i ] ); 
			end = start + Integer.parseInt( blockSize[ i ] ); 
			
			//Take strandness (+ or - Strand) into account. Assign Absolute values to startExon and endExon 
			if( entries[ 5 ].equals("+") )
			{
				startExon = (start - rangeUp); 
				endExon = (end + rangeDo) + 1; 
			}
			else if( entries[ 5 ].equals("-") )
			{
				startExon = ( start - rangeDo );
				endExon = ( end + rangeUp ) + 1;
			}
			
			//Print new BED entry for each exon. Each Exon ID is derived from the transcript ID followed by "_Exon" the exon count, followed by "_Up" the extension upstream, followed by "_Do" the extension Downstream. 
			String bedEntry = entries[ 0 ] + "\t" + startExon + "\t" + endExon + "\t" + entries[ 3 ] + "_Exon" + ( i + 1) + "_Up" + rangeUp + "_Do" + rangeDo + "\t";
			bedEntry += entries[ 4 ] + "\t" + entries[ 5 ] + "\t" + start + "\t" + end + "\t" + entries[ 8 ] + "\t" + 1 + "\t" + (endExon - startExon ) + "\t" + 0; 
			System.out.println( bedEntry ); 			
		}
	}
	
	
	public static void main(String[] args) 
	{
		if( args.length != 3 )
		{
			final String info = "\n#######################\n" + 
					"java -jar getExons -rangeUpstream=[Integer] -rangeDownstream=[Integer] pathToInputBEDFile\n" +
					"#######################\n" + 
					"Extracts all exons from the input BED file (pathToInputBEDFile).\n" +
					"Returns a 6 field BED file for each exon extended by a fixed number of nucleotides upstream (rangeUpstream) and downstream (rangeDownstream).\n" +
					"If you want to retrieve the exon without up- or downstream region, specify 0 for both values. \n" +
					"The result is printed to STDOUT. Store to file by redirecting (java -jar getExons -rangeUpstream=[Integer] -rangeDownstream=[Integer] pathToInputBEDFile > outputFile.BED).\n\n";
			System.err.println( info ); 
		}
		else 
		{	
			int rangeUpstream = 0;
			int rangeDownstream = 0; 
			try 
			{ 
				// Read upstream and downstream extension from argument array.
				rangeUpstream = Integer.parseInt( args[ 0 ].substring( args[ 0 ].indexOf("=") + 1).toLowerCase() );  
				rangeDownstream = Integer.parseInt( args[ 1 ].substring( args[ 1 ].indexOf("=") + 1).toLowerCase() ); 
			}
			catch (Exception e) 
			{
				// Arguments for upstream and downstream extension are either absent or no integer.
				System.err.println( "Please provide upstream and downstream range as an Integer." );
				System.exit( 1 ); 
			}
			
			new GetExons( rangeUpstream, rangeDownstream, args[ 2 ] ); 
		}

	}

}
