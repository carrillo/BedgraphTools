package toolbox;

import inputOutput.TextFileAccess;
import java.io.BufferedReader;

public class GetIntrons 
{
	public int rangeUp, rangeDo; 
	
	/**
	 * @param rangeUp Integer defining number of nucleotide the intron will be extended upstream
	 * @param rangeDo Integer defining number of nucleotide the intron will be extended downstream
	 * @param bedFile Path to BED file to be processed.
	 */
	public GetIntrons( final int rangeUp, final int rangeDo, final String bedFile )
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
				// Read line by line, split into fields, store as String[] and pass to intron extraction method.
				line = in.readLine(); 
				entries = line.split("\t");  
				printIntrons( entries );  					
			}
		}
		catch (Exception e) 
		{
			System.err.println( "Unable to extract intron from BED file. " + e );
			System.err.println( "Current line: " + line );
			System.exit( 1 ); 
		}
		
		System.exit( 0 ); 
	}
	
	/**
	 * This method extracts all introns from a given BED entry and prints a BED entry for each intron to STDOUT 
	 * @param entries String[] holding the fields of a single BED entry (row).
	 */
	public void printIntrons( final String[] entries )
	{ 
		// Check if transcript is intron containing (Exon >= 2). 
		final int exonCount = Integer.parseInt( entries[ 9 ] ); 
		if( exonCount > 1 )
		{
			//Extract String[] holding start values (blockStart) and length (blockSize) of all exons.
			final String[] blockStart = entries[ 11 ].split(",");
			final String[] blockSize = entries[ 10 ].split(",");
			
			int start, end;
			int startIntron = 0;
			int endIntron = 0; 
			
			//Assign absolute value for gene start. Exon starts in blockStart are relative to this value.
			final int geneStart = Integer.parseInt( entries[ 1 ] );  
			
			//Loop through all introns.
			for( int i = 0; i < exonCount - 1 ; i++ )
			{
				//Calculate absolute boundary positions of exons (start and end are not consiering strandness). 
				start = geneStart + Integer.parseInt( blockStart[ i ] ) + Integer.parseInt( blockSize[ i ] ); 
				end = geneStart + Integer.parseInt( blockStart[ i + 1 ] ); 
				
				//Take strandness (+ or - Strand) into account. Assign Absolute values to startIntron and endIntron
				if( entries[ 5 ].equals("+") )
				{
					startIntron = (start - rangeUp); 
					endIntron = (end + rangeDo); 
				}
				else if( entries[ 5 ].equals("-") )
				{
					startIntron = ( start - rangeDo );
					endIntron = ( end + rangeUp );
				}
				
				//Print new BED entry for each intron. Each Intron ID is derived from the transcript ID followed by "_Intron" the intron count, followed by "_Up" the extension upstream, followed by "_Do" the extension Downstream.
				String bedEntry = entries[ 0 ] + "\t" + startIntron + "\t" + endIntron + "\t" + entries[ 3 ] + "_Intron" + ( i + 1) + "_Up" + rangeUp + "_Do" + rangeDo + "\t";
				bedEntry += entries[ 4 ] + "\t" + entries[ 5 ] + "\t" + start + "\t" + end + "\t" + entries[ 8 ] + "\t" + 1 + "\t" + (endIntron - startIntron ) + "\t" + 0; 
				System.out.println( bedEntry ); 			
			}			
		}
	}
	
	public static void main(String[] args) 
	{
		if( args.length != 3 )
		{
			final String info = "\n#######################\n" + 
					"java -jar getIntrons -rangeUpstream=[Integer] -rangeDownstream=[Integer] pathToInputBEDFile\n" +
					"#######################\n" + 
					"Extracts all introns from the input BED file (pathToInputBEDFile).\n" +
					"Returns a 6 field BED file for each intron extended by a fixed number of nucleotides upstream (rangeUpstream) and downstream (rangeDownstream).\n" +
					"If you want to retrieve the intron without up- or downstream region, specify 0 for both values. \n" +
					"The result is printed to STDOUT. Store to file by redirecting (java -jar getIntrons -rangeUpstream=[Integer] -rangeDownstream=[Integer] pathToInputBEDFile > outputFile.BED).\n\n"; 
			System.err.println( info ); 
		}
		else 
		{	
			try 
			{ 
				// Read upstream and downstream extension from argument array.
				final int rangeUpstream = Integer.parseInt( args[ 0 ].substring( args[ 0 ].indexOf("=") + 1).toLowerCase() ); 
				final int rangeDownstream = Integer.parseInt( args[ 1 ].substring( args[ 1 ].indexOf("=") + 1).toLowerCase() ); 
				
				new GetIntrons( rangeUpstream, rangeDownstream, args[ 2 ] ); 
			}
			catch (Exception e) 
			{
				// Arguments for upstream and downstream extension are either absent or no integer.
				System.err.println( "Please provide the range as an Integer." );
				System.exit( 1 ); 
			}
			
		}

	}

}
