package bedgraphTools;

import inputOutput.CalenderStuff;
import inputOutput.TextFileAccess;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import mpicbg.imglib.algorithm.gauss.DownSample;
import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.interpolation.Interpolator;
import mpicbg.imglib.interpolation.linear.LinearInterpolatorFactory;
import mpicbg.imglib.outofbounds.OutOfBoundsStrategyValueFactory;
import mpicbg.imglib.type.numeric.real.FloatType;

public class PileupWindowFloat 
{

	public String inputFormat, outputFormat; 
	public int bins;
	
	public HashMap<String, HashMap<Integer, Float>> pileupHash; 
	public ArrayList<float[]> windowList;
	int winSizeMax, winSizeMin; 
	
	public PileupWindowFloat( final String inputFormat, final String outputFormat, final String processing, final int bins, final String inputPileup, final String inputBED )
	{
		if( processing.equals("genome") )
		{
			fillPileupHash( inputPileup, inputFormat ); 
			createWindowList( inputBED ); 		
		}
		else 
		{
			PrintWriter logOut = TextFileAccess.openFileWrite( "pileupWindowLOG_" + CalenderStuff.now( CalenderStuff.DATE_Time ) + ".log");
			
			logOut.println( "Pileup file : " + inputPileup + " of format " + inputFormat);
			logOut.println( "Window BEDFile : " + inputBED + "\n");
			
			
			HashMap<Integer, Float> chrHash = new HashMap<Integer, Float>(); 
			this.windowList = new ArrayList<float[]>(); 
			for( String chr : getChromosomesInBED( inputBED ) )
			{	
				
				logOut.print( "Chromosome " + chr + " - Creating chromosome Hash from pileupFile\t" );
				logOut.flush();
				
				chrHash = fillChromosomePileupHash( inputPileup, inputFormat, chr );
				
				logOut.print( "Done. " + chrHash.values().size() + " entries." + "\n" ); 
				logOut.print( "Chromosome " + chr + " - Creating value windows from Window BEDFile\t" );
				logOut.flush(); 
				
				addChromosomeWindowsToList( inputBED, chrHash, chr ); 
				
				chrHash = null; 
				System.gc(); 
				
				logOut.print( "Done. " + this.windowList.size() + " processed BED entries (Sum over all Chr)" + "\n" );
				logOut.flush(); 
				
			}
			logOut.close(); 
		}
		if( bins == -1 )
		{
			printWindowGrouping( outputFormat ); 
		}
		else 
		{
			printScaledWindows( bins, outputFormat ); 
			//printWindowGrouping( outputFormat ); 
		}
		
		System.exit( 0 );
	}
	
	public void addChromosomeWindowsToList( final String inputBED, final HashMap<Integer, Float> chrHash, final String chr )
	{
		ArrayList<float[]> windowList = new ArrayList<float[]>(); 
		
		this.winSizeMax = - Integer.MAX_VALUE; 
		this.winSizeMin = Integer.MAX_VALUE; 
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
					windowList.add( getWindowEntry( entries, chrHash ) ); 
			}
		}
		catch (Exception e) 
		{
			System.err.println( "Unable to extract values for windows specified in input.BED." );
			System.exit( 1 ); 
		} 
		
		this.windowList.addAll( windowList ); 
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
	
	public void printScaledWindows( final int bins, final String outputFormat )
	{
		
		ArrayList<float[]> scaledWindowList = new ArrayList<float[]>(); 
		
		for( float[] originalWindow : windowList )
		{
			float[] scaledWindow = new float[ bins ]; 
			if( originalWindow.length == bins )
			{
				for( int i = 0; i < bins; i++ )
					scaledWindow[ i ] = (float) originalWindow[ i ]; 
			}
			else if( originalWindow.length > bins * 2 )
			{
				scaledWindow = scaleDown( originalWindow, bins );
			}
			else 
			{
				scaledWindow = scaleUp( originalWindow, bins ); 
			}
			scaledWindowList.add( scaledWindow ); 
		}
		
		final float[] groupedWindow = new float[ bins ]; 
		for( float[] window : scaledWindowList )
		{
			for( int i = 0; i < window.length; i++ )
				groupedWindow[ i ] += window[ i ]; 
		}
		
		final int nrOfWindows = windowList.size(); 
		for( int i = 0; i < groupedWindow.length; i++ )
		{
			if( outputFormat.equals( "avg" ) )
				System.out.println( i + "\t" + groupedWindow[ i ] / nrOfWindows ); 
			else
				System.out.println( i + "\t" + groupedWindow[ i ] ); 
		}
	}
	
	public float[] scaleDown( final float[] originalWindow, final int bins )
	{
		Image<FloatType> image = arrayToImage( originalWindow ); 
		
		
		final int[] size = new int[] { (int) bins };
		DownSample<FloatType> ds = new DownSample<FloatType>( image, size, 0.5f, 0.5f );
		ds.process();
		Image<FloatType> downsampledImage = ds.getResult();
		
		return imageToArray( downsampledImage ); 
	}
	
	public float[] scaleUp( final float[] originalWindow, final int bins )
	{
		final ImageFactory<FloatType> factory = new ImageFactory<FloatType>( new FloatType(), new ArrayContainerFactory() );
		final Image<FloatType> scaledTrace = factory.createImage( new int[] { bins } );
		
		final Image<FloatType> originalImage = arrayToImage( originalWindow ); 
		final double scalingFactor = (double) originalImage.getDimension( 0 ) / (double) scaledTrace.getDimension( 0 );
		Cursor<FloatType> scaledTraceCursor = scaledTrace.createCursor(); 
		
		Interpolator<FloatType> interpolator = originalImage.createInterpolator(new LinearInterpolatorFactory<FloatType>( new OutOfBoundsStrategyValueFactory<FloatType>() ) );
		
		int  cursorPos = -1;
		double originalPos = -1; 
		final float[] temp = new float[ 1 ]; 
		while( scaledTraceCursor.hasNext() )
		{
			scaledTraceCursor.fwd(); 
			cursorPos++; 
			
			originalPos = cursorPos * scalingFactor; 
			temp[ 0 ] = (float) originalPos;
			
			interpolator.setPosition( temp );
			scaledTraceCursor.getType().set( interpolator.getType().get() ); 
			
			//System.out.println( "Cursor Pos: " + cursorPos + " Scaled Cursor Pos: " + originalPos + " Original Length " + originalTrace.getDimension( 0 ) + " Scaled Length: " + scaledTrace.getDimension( 0 ) ); 
			
		}
		
		
		return imageToArray( scaledTrace ); 
	}
	
	public float[] imageToArray( final Image<FloatType> image )
	{
		float[] array = new float[ image.getDimension( 0 ) ]; 
		Cursor<FloatType> cursor = image.createCursor();
		
		int x = -1; 
		while( cursor.hasNext() )
		{
			cursor.fwd();
			x++; 
			array[ x ] = cursor.getType().get(); 
		}	
		return array; 
	}
	 
	
	public Image<FloatType> arrayToImage( final float[] array )
	{
		//Convert int[] into Image<FloatType> 
		final int[] size = new int[] { (array.length) }; 
		ImageFactory<FloatType> factory = new ImageFactory<FloatType>( new FloatType(), new ArrayContainerFactory() ); 
		Image<FloatType> image = factory.createImage( size ); 
		Cursor<FloatType> cursor = image.createCursor(); 
		
		int arrayPos = -1; 
		while( cursor.hasNext() )
		{
			cursor.fwd(); 
			arrayPos++; 
			
			cursor.getType().set( (float) array[ arrayPos ] );  
		}
		
		return image; 
	}
	
	public void printWindowGrouping( final String outputFormat ) 
	{
		final double[] groupedWindow = new double[ this.winSizeMax ];
		
		for( float[] window : windowList )
		{
			for( int i = 0; i < window.length; i++ )
				groupedWindow[ i ] += window[ i ]; 
		}
		
		final int nrOfWindows = windowList.size(); 
		for( int i = 0; i < groupedWindow.length; i++ )
		{
			if( outputFormat.equals( "avg" ) )
				System.out.println( i + "\t" + groupedWindow[ i ] / nrOfWindows ); 
			else
				System.out.println( i + "\t" + groupedWindow[ i ] ); 
		}
	}
	
	public void createWindowList( final String inputBED )
	{
		ArrayList<float[]> windowList = new ArrayList<float[]>(); 
		
		
		this.winSizeMax = - Integer.MAX_VALUE; 
		this.winSizeMin = Integer.MAX_VALUE; 
		BufferedReader in = TextFileAccess.openFileRead( inputBED ); 
		try
		{
			String line; 
			String[] entries;
			while( in.ready() )
			{
				line = in.readLine(); 
				entries = line.split("\t"); 
				windowList.add( getWindowEntry( entries, pileupHash.get( entries[ 0 ] ) ) ); 
			}
		}
		catch (Exception e) 
		{
			System.err.println( "Unable to extract values for windows specified in input.BED." );
			System.exit( 1 ); 
		} 
		 
		this.windowList = windowList; 
	}
	
	public float[] getWindowEntry( final String[] entries, final HashMap<Integer, Float> chrHash )
	{
		final int start = Integer.parseInt( entries[ 1 ] ); 
		final int end = Integer.parseInt( entries[ 2 ] );
		
		final int winSize = end - start;
		final float[] window = new float[ winSize ]; 
		
		if( winSizeMax < winSize )
			winSizeMax = winSize; 
		if( winSizeMin > winSize )
			winSizeMin = winSize; 
		
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
		
		return window; 
	}
	
	public void fillPileupHash( final String inputPileup, final String inputFormat )
	{
		HashMap<String, HashMap<Integer, Float>> pileupHash = new HashMap<String, HashMap<Integer,Float>>(); 
		
		
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
				
				//System.out.println( line + "\t" + entries[ yValuePos ] ); 
				
				if( !pileupHash.containsKey( entries[ 0 ] ) )
				{
					pileupHash.put( entries[ 0 ], new HashMap<Integer, Float>() ); 
				}
				//System.out.println( entries[ 0 ] + " " + entries[ 1 ] + " " + entries[ 3 ] ); 
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
		if( args.length < 5 || args.length > 6 )
		{
			final String info = "\n#######################\n" +
					"java -jar pileupWindow -input=[bedgraph,pileup,coverage] -output=[avg,sum] -processing=[genome,perChromosome] -bins=x input.bedgraph/pileup input.BED\n" +
					"#######################\n" + 
					"Returns a file of the average( -output=avg ) or sum( -output=sum ) value for each position relative to the start of each input.BED entry.\n" +
					"Specify if your input histogram is of bedgraph ( -input=bedgraph ), pileup ( -input=pileup ) or coverage ( -input=genomeCoverage , generated by bedtools) format \n" +
					"Specify if the entire genome should be processed at once ( -processing=genome ) or if it will process chromosomes sequentially ( -processing=perChromosome). This just influences the perfomance NOT the result. For large genomes the perChromosome option is recommended \n" +
					"Add number of bins ( -bins=x ) if the input.BED windows are of unequal size. The output will be scaled from start to end for each window.\n" +
					"If not specified the values will be calculated with windows aligned at starts specified in the input.BED file\n" +
					"#######################\n\n" +
					"This script is identical to pileupWindow.jar, but excepts float values in the yValue position. It requires more memory to run.\n\n" +
					"\n"; 
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
			
			final String output = args[ 1 ].substring( args[ 1 ].indexOf("=") + 1);  
			if( !output.equals("avg") && !output.equals("sum"))
			{
				System.err.println( "Please specify an output format: Averaged value (avg) or Summed value (sum)" ); 
				System.exit( 1 ); 
			}
			
			final String processing = args[ 2 ].substring( args[ 2 ].indexOf("=") + 1 ); 
			if( !processing.equals("genome") && !processing.equals("perChromosome") )
			{
				System.err.println( "Please specify a processing method: Entire genome (genome) or per Chromosome (perChromosome)" ); 
				System.exit( 1 ); 
			}
			
			int bins = -1; 
			if( args.length == 6 )
			{
				try 
				{ 
					bins = Integer.parseInt( args[ 3 ].substring( args[ 3 ].indexOf("=") + 1).toLowerCase() ); 
				}
				catch (Exception e) 
				{
					System.err.println( "Please provide the number of bins as an Integer value." );
					System.exit( 1 ); 
				}	
			}
			
			new PileupWindowFloat( input, output, processing, bins, args[ args.length - 2 ], args[ args.length - 1] ); 
		}
	}

}
