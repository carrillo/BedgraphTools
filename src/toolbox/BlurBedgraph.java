package toolbox;



import infrastructure.BEDgraphEntry;
import infrastructure.GaussianKernel;
import inputOutput.TextFileAccess;

import java.io.BufferedReader;
import java.util.ArrayList;

public class BlurBedgraph 
{ 
	protected double[] gaussianKernel;
	protected static float OUT_OF_BOUNDARY_VALUE = 0 ; 
	
	/**
	 * Construct a BlurBedgraph class. Define the gaussian sigma and the bedgraph-file to blur. 
	 * @param sigma
	 * @param bedgraph
	 */
	public BlurBedgraph( final float sigma, final String bedgraph )
	{  
		setGaussianKernel( sigma ); 
		readBedgraph( bedgraph );
		System.exit( 0 ); 
	}
	
	/**
	 * Parse the input bedgraph file. Blur on the fly using a gaussian kernel with defined sigma. 
	 * @param bedgraph
	 */
	public void readBedgraph( final String bedgraph )
	{ 
		
		BufferedReader in = TextFileAccess.openFileRead( bedgraph ); 
		String line = "Empty first line"; 
		String chrId = "";
		
		//Initiate the ArrayList holding the values to convolve.  
		ArrayList<BEDgraphEntry> valueArray = new ArrayList<BEDgraphEntry>(); 
		 
		try
		{ 
			while( in.ready() )
			{ 
				line = in.readLine();  
				
				//Add current BEDgraph entry to the valueArray 
				valueArray = addAndShift( valueArray, line ); 
				
				//Check if the current entry is representing a new chromosome (new chrId). 
				if( !valueArray.get( valueArray.size() - 1 ).getChrom().equals( chrId ) )
				{
					//Check if the current value is the first entry. 
					if( !chrId.equals("") )	
					{ 
						//Process the last entries.
						valueArray = processEndOfChromosome( valueArray ); 
					}
					
					//Assign current entry's chromosome id to chrId
					chrId = valueArray.get( valueArray.size() -1 ).getChrom(); 
				}
				
				//Check if the valueArray is smaller than the gaussian kernel
				if( valueArray.size() < getGaussianKernel().length )
				{
					//If of exact the right size, print the out-of-boundary value. 
					if( valueArray.size() == ( getGaussianKernel().length - 1 )/2  )
					{
						printOutOfBoundaryValues( valueArray ); 
					}
				}
				
				//If everything is 'normal', then blur. 
				else 
				{
					System.out.println( getBlurredBEDgraphEntry( valueArray ) ); 				
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
	 * Get the convolved center position of the value array. 
	 * @param valueArray
	 * @return
	 */
	public BEDgraphEntry getBlurredBEDgraphEntry( final ArrayList<BEDgraphEntry> valueArray )
	{
		//Create a new object to not change the input values
		BEDgraphEntry output = new BEDgraphEntry( valueArray.get( getArrayMiddleIndex( valueArray) ).toString() ); 
		
		float blurredValue = 0.0f; 
		for( int i = 0; i < getGaussianKernel().length; i++ )
		{
			blurredValue += getGaussianKernel()[ i ] * valueArray.get( i ).getScore();  
		}
		
		output.setScore( blurredValue ); 
		
		return output; 
	}
	
	public int getArrayMiddleIndex( final ArrayList<BEDgraphEntry> input )
	{
		return ( ( input.size() - 1 )/ 2 ); 
	}
	
	/**
	 * Adds a value to the input arraylist. 
	 * If the input arrayList has the same number of entries as the gaussian kernel, then the oldest entry is removed.  
	 * @param input
	 * @param line
	 * @return
	 */
	public ArrayList<BEDgraphEntry> addAndShift( ArrayList<BEDgraphEntry> input, final String line )
	{
		
		input.add( new BEDgraphEntry( line ) ); 
		
		if( input.size() > getGaussianKernel().length )
		{
			input.remove( 0 ); 			
		}
		
		return input; 
	}
	
	/**
	 * Print the defined OUT_OF_BOUNDARY_VALUE as score for all BEDgraphEntries in the input list. 
	 * @param values
	 */
	public void printOutOfBoundaryValues( final ArrayList<BEDgraphEntry> values )
	{
		for( BEDgraphEntry e : values )
		{
			e.setScore( OUT_OF_BOUNDARY_VALUE ); 
			System.out.println( e ); 
		}
	}
	
	/**
	 * Process the last lines representing one chromosome. Return the list for the next chromosome. 
	 * @param input
	 * @return
	 */
	public ArrayList<BEDgraphEntry> processEndOfChromosome( final ArrayList<BEDgraphEntry> input )
	{
		
		ArrayList<BEDgraphEntry> newChr = new ArrayList<BEDgraphEntry>(); 
		newChr.add( input.get( input.size() - 1 ) ); 
		
		ArrayList<BEDgraphEntry> oldChr = new ArrayList<BEDgraphEntry>();
		for( int i = input.size() - ( input.size() + 1 )/2 ; i < ( input.size() - 1); i++ )
		{
			oldChr.add( input.get( i ) ); 
		}
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
		for( int i = ( input.size() - ( ( input.size() + 1 )/2 - 1) ); i < ( input.size() ); i++ )
		{
			oldChr.add( input.get( i ) ); 
		}
		printOutOfBoundaryValues( oldChr ); 
		 
	}
	
	//Getter and setter
	public void setGaussianKernel( final float sigma ) { this.gaussianKernel = GaussianKernel.createGaussianKernel1DDouble( sigma, true ); }
	public double[] getGaussianKernel() { return this.gaussianKernel; } 
	
	
	/**
	 * Main method used for running this script from shell
	 * @param args
	 */
	public static void main(String[] args) 
	{
		if( args.length != 2 )
		{
			final String info = "\n#######################\n" + 
					"java -jar blurBedgraph -sigma=[float] bedgraphFile\n" +
					"#######################\n" + 
					"Gaussian Blurs the bedgraph with a gaussian Kernel sized by the given sigma.\n" +
					"Specify the sigma (-sigma=[float]).\n" +
					"Outputs the blurred bedgraph to STDOUT.\n" +
					"Important: Decompress your bedgraph prior use!\n" +
					"\n\n"; 
			System.err.println( info ); 
		}
		else 
		{	
			final String s = args[ 0 ].substring( args[ 0 ].indexOf("=") + 1);
			float sigma = -1; 
			try 
			{ 
				sigma = Float.parseFloat( s ); 
			}
			catch (Exception e) 
			{
				System.out.println( "Please provide a float value to size the kernel" ); 
				System.exit( 1 );
			}
			
			new BlurBedgraph( sigma, args[ 1 ] );
			
		}		
	}

}
