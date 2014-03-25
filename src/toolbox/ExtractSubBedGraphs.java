package toolbox;

import inputOutput.CalenderStuff;
import inputOutput.TextFileAccess;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ExtractSubBedGraphs {

	public String inputFormat, outputPath; 
	public int bins;
	
	public HashMap<String, HashMap<Integer, Float>> pileupHash; 
	
	public ExtractSubBedGraphs( final String inputFormat, final String outputPath, final String processing, final String inputPileup, final String inputBED )
	{
		this.inputFormat = inputFormat; 
		this.outputPath = outputPath;
		
		//Check if all chromosomes should be processed simultaneously 
		if( processing.equals("genome") ) 	 
		{
			fillPileupHash( inputPileup, inputFormat ); 
			createWindowList( inputBED ); 		
		}
		//Or if processing should take place on a chromosome by chromosome basis 
		else 
		{
			//Initiate a log file writer
			PrintWriter logOut = TextFileAccess.openFileWrite( "pileuUpWindowLOG_" + CalenderStuff.now( CalenderStuff.DATE_Time ) + ".log");
			logOut.println( "Pileup file : " + inputPileup + " of format " + inputFormat);
			logOut.println( "Window BEDFile : " + inputBED + "\n");
			
			//Initiate a hashmap for each chromosome 
			HashMap<Integer, Float> chrHash = new HashMap<Integer, Float>(); 
			
			//Go chromosome by chromosome
			for( String chr : getChromosomesInBED( inputBED ) )
			{	
				logOut.print( "Chromosome " + chr + " - Creating chromosome Hash from pileupFile\t" );
				logOut.flush();
				
				//Add values to chromosome hash
				chrHash = fillChromosomePileupHash( inputPileup, inputFormat, chr );
				
				logOut.print( "Done. " + chrHash.values().size() + " entries." + "\n" ); 
				logOut.print( "Chromosome " + chr + " - Creating value windows from Window BEDFile\t" );
				logOut.flush(); 
				
				addChromosomeWindowsToList( inputBED, chrHash, chr ); 
				
				chrHash = null; 
				System.gc();  
				
			}
			logOut.close(); 
		}
		
		System.exit( 0 );
	}
	
	public void addChromosomeWindowsToList( final String inputBED, final HashMap<Integer, Float> chrHash, final String chr )
	{
		BufferedReader in = TextFileAccess.openFileRead( inputBED ); 
		try
		{
			String line; 
			String[] entries;
			while( in.ready() )
			{
				line = in.readLine(); 
				entries = line.split("\t"); 
				
				if( entries[ 0 ].equals( chr ) )
					writeWindowEntry( entries, chrHash ); 
			}
		}
		catch (Exception e) 
		{
			System.err.println( "Unable to extract values for windows specified in input.BED." );
			System.exit( 1 ); 
		} 
	}
	
	public HashMap<Integer, Float> fillChromosomePileupHash( final String inputPileup, final String inputFormat, final String chrId )
	{
		HashMap<Integer, Float> chrHash = new HashMap<Integer, Float>(); 
		
		int yValuePos = -1; 
		if( inputFormat.equals("bedgraph") || inputFormat.equals("pileup") )
			yValuePos = 3;
		else if( inputFormat.equals("coverage") )
			yValuePos = 2; 
		
		BufferedReader in = TextFileAccess.openFileRead( inputPileup ); 
		try
		{
			String line; 
			String[] entries; 
			int start; 
			int end; 
			float value;
			while( in.ready() )
			{
				line = in.readLine(); 
				entries = line.split("\t");   
				
				if( entries[ 0 ].equals( chrId ) )
				{
					start = Integer.parseInt( entries[ 1 ]); 
					end = Integer.parseInt( entries[ 2 ] ); 
					value = Float.parseFloat( entries[ yValuePos ] );
					
					if( value != 0f )
					{ 
						while( start < end )
						{
							chrHash.put( start, value ); 
							start++; 
						}
					}
					
					//chrHash.put( Integer.parseInt( entries[ 1 ]), Float.parseFloat( entries[ yValuePos ] ) ); 
				}   
			}
		}
		catch (Exception e) 
		{
			System.err.println( "Unable to hash values specified in input.bedgraph/pileup." );
			System.exit( 1 ); 
		} 
		
		return chrHash; 
	}
	
	public ArrayList<String> getChromosomesInBED( final String inputBED )
	{
		ArrayList<String> chrList = new ArrayList<String>(); 
		HashSet<String> chrHash = new HashSet<String>();
		chrHash.add(""); 
		
		BufferedReader in = TextFileAccess.openFileRead( inputBED ); 
		try
		{
			String line; 
			String[] entries;
			while( in.ready() )
			{
				line = in.readLine(); 
				 
				entries = line.split("\t"); 
				if( !chrHash.contains( entries[ 0 ] ) )
				{
					chrList.add( entries[ 0 ] ); 
					chrHash.add( entries[ 0 ] ); 
				} 
			}
		}
		catch (Exception e) 
		{
			System.err.println( "Unable to extract chromosomes specified in input.BED." );
			System.exit( 1 ); 
		} 
		
		return chrList; 
	}
	
	
	
	
	
	public void createWindowList( final String inputBED )
	{
		 
		
		
		BufferedReader in = TextFileAccess.openFileRead( inputBED ); 
		try
		{
			String line; 
			String[] entries;
			while( in.ready() )
			{
				line = in.readLine(); 
				entries = line.split("\t"); 
				writeWindowEntry( entries, pileupHash.get( entries[ 0 ] ) ); 
			}
		}
		catch (Exception e) 
		{
			System.err.println( "Unable to extract values for windows specified in input.BED." );
			System.exit( 1 ); 
		} 
		 
	}
	
	public void writeWindowEntry( final String[] entries, final HashMap<Integer, Float> chrHash )
	{
		PrintWriter out = TextFileAccess.openFileWrite( outputPath + "/" + entries[ 3 ] + ".bedgraph"); 
		
		final String chrId = entries[ 0 ];
		final int start = Integer.parseInt( entries[ 1 ] ); 
		final int end = Integer.parseInt( entries[ 2 ] );
		
		final String header = "track name=" + chrId + "," + start + "," + end;
		out.println( header ); 
		
		final int winSize = end - start;
		final float[] window = new float[ winSize ];  
		
		if( entries[ 5 ].equals("+") )
		{
			for( int i = start; i < end; i++)
			{
				if( chrHash.containsKey( i ) )
				{
					window[ i - start ] = chrHash.get( i ); 
				}
				else 
					window[ i - start ] = 0; 
			}
		}
		else
		{
			 for( int i = end - 1; i > start - 1; i-- )
			 { 
				 if( chrHash.containsKey( i ) )
				 {
					 //System.out.print( window.length + ":" + ( end - i - 1 ) + ":" + i + ", " ); 
					 window[ ( end - i - 1 ) ] = chrHash.get( i ); 
				 }
				 else 
				 {
					 
					 window[ ( end - i - 1 ) ] = 0; 
				 }
			 } 
		} 
		
		for( int i = 0; i < window.length; i++ )
		{
			out.println( chrId + "\t" + i + "\t" + ( i + 1 ) + "\t" + window[ i ] ); 
		}
		 
		out.close(); 
	}
	
	/**
	 * Fill HashMap with coverage values sorted by chromosome ids. 
	 * @param inputPileup
	 * @param inputFormat
	 */
	public void fillPileupHash( final String inputPileup, final String inputFormat )
	{
		//Generate chromosome hashmap
		HashMap<String, HashMap<Integer, Float>> pileupHash = new HashMap<String, HashMap<Integer,Float>>(); 
		
		//Define which position of the entry string holds the coverage value
		int yValuePos = -1; 
		if( inputFormat.equals("bedgraph") || inputFormat.equals("pileup") )
			yValuePos = 3;
		else if( inputFormat.equals("coverage") )
			yValuePos = 2; 
		
		//Read input bedgraph/pileup
		BufferedReader in = TextFileAccess.openFileRead( inputPileup ); 
		try
		{
			String line; 
			String[] entries; 
			int start; 
			int end;
			float value; 
			
			while( in.ready() )
			{
				line = in.readLine(); 
				entries = line.split("\t"); 
				
				
				
				if( !pileupHash.containsKey( entries[ 0 ] ) )
				{
					pileupHash.put( entries[ 0 ], new HashMap<Integer, Float>() ); 
				}
				
				
				start = Integer.parseInt( entries[ 1 ] );
				end = Integer.parseInt( entries[ 2 ] ); 
				value = Float.parseFloat( entries[ yValuePos ] ); 
				
				if( value != 0f )
				{
					//System.out.println("Value unequal zero " + start + ":" + end ); 
					while( start < end )
					{ 
						pileupHash.get( entries[ 0 ] ).put( start, value ); 
						start++; 
					}					
				} 
			}
		}
		catch (Exception e) 
		{
			System.err.println( "Unable to hash values specified in input.bedgraph/pileup." );
			System.exit( 1 ); 
		} 
		
		this.pileupHash = pileupHash; 
	}
	
	/**
	 * Main method used for running this script from shell
	 * @param args
	 */
	public static void main(String[] args) 
	{
		if( args.length != 5 )
		{
			final String info = "\n#######################\n" +
					"java -jar extractSubBedGraphs -input=[bedgraph,pileup,coverage] -outputPath=[x] -processing=[genome,perChromosome] input.bedgraph/pileup input.BED\n" +
					"#######################\n" + 
					"Intersects the input bedgraph/pileup (input.bedgraph/pileup) file with a BED file (input.BED) and returns each subregion as a single file to the output directory (-outputPath).\n" +
					"Strandness is taken into account. Thus features on the Minus-Strand are reversed.\n" +
					"Specify if your input histogram is of bedgraph ( -input=bedgraph ), pileup ( -input=pileup ) or coverage ( -input=genomeCoverage , generated by bedtools) format \n" +
					"Specify if the entire genome should be processed at once ( -processing=genome ) or if it will process chromosomes sequentially ( -processing=perChromosome). This just influences the perfomance NOT the result. For large genomes the perChromosome option is recommended \n" +
					"#######################\n\n";  
			System.err.println( info ); 
		}
		else 
		{
			final String input = args[ 0 ].substring( args[ 0 ].indexOf("=") + 1);  
			if( !input.equals("bedgraph") && !input.equals("pileup") && !input.equals("coverage") )
			{
				System.err.println( "Please specify an output format: Averaged value (avg) or Summed value (sum)" ); 
				System.exit( 1 ); 
			}
			
			final String outputPath = args[ 1 ].substring( args[ 1 ].indexOf("=") + 1);  
			
			final String processing = args[ 2 ].substring( args[ 2 ].indexOf("=") + 1 ); 
			if( !processing.equals("genome") && !processing.equals("perChromosome") )
			{
				System.err.println( "Please specify a processing method: Entire genome (genome) or per Chromosome (perChromosome)" ); 
				System.exit( 1 ); 
			}
			
			new ExtractSubBedGraphs( input, outputPath, processing, args[ args.length - 2 ], args[ args.length - 1] ); 
		}
	}

}
