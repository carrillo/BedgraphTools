package toolbox;

import inputOutput.TextFileAccess;

import java.io.BufferedReader;

public class GetTSSorPolyASite {

	public String feature; 
	public int rangeUp, rangeDo; 
	
	public GetTSSorPolyASite( final String feature, final int rangeUp, final int rangeDo, final String bedFile )
	{
		this.feature = feature; 
		this.rangeUp = rangeUp; 
		this.rangeDo = rangeDo; 
		
		readInput( bedFile ); 
	}
	
	public void readInput( final String bedFile )
	{
		BufferedReader in = TextFileAccess.openFileRead( bedFile ); 
		try
		{
			String line; 
			String[] entries; 
			while( in.ready() )
			{
				line = in.readLine(); 
				entries = line.split("\t"); 
				
				//System.out.println( line ); 
				printFeature( entries );  					
			
			}
		}
		catch (Exception e) 
		{
			System.err.println( "Unable to extract exon-intron boundary from specified file." );
			System.exit( 1 ); 
		}
		
		System.exit( 0 ); 
	}
	
	public void printFeature( final String[] entries )
	{
		int featureStart = -1; 
		int featureEnd = -1;
		int feature =-1 ;
		
		if( entries[ 5 ].equals( "+" ) )
		{
			final int TSS = Integer.parseInt( entries[ 1 ] ); 
			final int polyA = Integer.parseInt( entries[ 2 ] ); 
			 
			if( this.feature.equals("TSS") )
				feature = TSS;  
			else
				feature = polyA; 

			featureStart = (feature - rangeUp); 
			featureEnd = (feature + rangeDo) + 1; 
		}
		else if( entries[ 5 ].equals( "-" ) )
		{
			final int TSS = Integer.parseInt( entries[ 2 ] ) - 1; 
			final int polyA = Integer.parseInt( entries[ 1 ] ) - 1;
			
			if( this.feature.equals("TSS") )
				feature = TSS;  
			else
				feature = polyA;
			
			featureStart = ( feature - rangeDo );
			featureEnd = ( feature + rangeUp ) + 1; 
		}
		else
		{
			System.err.println( "Unknown strand identifier: " + entries[ 5 ] ); 
			System.exit( 1 ); 
		}
		
		String bedEntry = entries[ 0 ] + "\t" + featureStart + "\t" + featureEnd + "\t" + entries[ 3 ] + this.feature + "_Up" + rangeUp + "_Do" + rangeDo + "\t";
		bedEntry += entries[ 4 ] + "\t" + entries[ 5 ];
		if( entries.length > 6 )
			bedEntry += "\t" + feature + "\t" + (feature + 1) + "\t" + entries[ 8 ] + "\t" + 1 + "\t" + (featureEnd - featureStart ) + "\t" + 0; 
		System.out.println( bedEntry );
	}
	
	
	public static void main(String[] args) 
	{
		if( args.length != 4 )
		{
			final String info = "\n#######################\n" +
					"java -jar getTSSorPolyA -feature=[TSS,PolyA] -rangeUpstream=[Integer] -rangeDownstream=[Integer] input.BED\n" +
					"#######################\n" + 
					"Returns a BED file containing the specified feature (TSS or PolyA)\n" +
					"Extended by the number nucleotides upstream of the feature (rangeUpstream) and downstream of the feature (rangeDownstream)\n" + 
					"Where the feature treats the BEDstart/BEDend as TSS or PolyA depending on the strand of the BED entry"; 
			System.err.println( info ); 
		}
		else 
		{
			final String feature = args[ 0 ].substring( args[0].indexOf("=") + 1); 
			//System.out.println( args[ 0 ] + " " + spliceSite ); 
			if( !feature.equals("TSS") && !feature.equals("PolyA"))
			{
				System.err.println( "Please specify a feature: TSS or PolyA" ); 
				System.exit( 1 ); 
			}
			
			try 
			{ 
				final int rangeUpstream = Integer.parseInt( args[ 1 ].substring( args[ 1 ].indexOf("=") + 1).toLowerCase() ); 
				//System.out.println( rangeUpstream ); 
				
				final int rangeDownstream = Integer.parseInt( args[ 2 ].substring( args[ 2 ].indexOf("=") + 1).toLowerCase() ); 
				//System.out.println( rangeDownstream );
				
				new GetTSSorPolyASite( feature, rangeUpstream, rangeDownstream, args[ 3 ] ); 
			}
			catch (Exception e) 
			{
				System.err.println( "Please provide the range as an Integer." );
				System.exit( 1 ); 
			}
			
		}
	}

}
