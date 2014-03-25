package toolbox;

import inputOutput.TextFileAccess;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class DeriveBedgraph
{
	
	public DeriveBedgraph( final BufferedReader in )
	{
		String[] line = null;
		String chrId = "";
		ArrayList<double[]> values = new ArrayList<double[]>(); 
		try
		{
			while( in.ready() )
			{
				line = in.readLine().split("\t"); 
				if( !line[ 0 ].substring(0,3).equals( "tra" ) )
				{
					
					if( !chrId.equals( line[ 0 ]) )
					{ 
						 
						//Calculate derivative of the values of the current chromosome
						if( values.size() != 0 )
							printDerivative( chrId, values );  
						
						chrId = line[ 0 ];
						values = new ArrayList<double[]>();  
					}
					
					addLine( line, values );					
				}
			}
		}
		catch (IOException e) 
		{
			System.out.println( e ); 
		}
		
		printDerivative(chrId,  values); 
	}
	
	/**
	 * Calculate the first derivative of the lines hold in the ArrayList.
	 * @param chrId Current Chromosome
	 * @param values All bedgraph values 
	 */
	public void printDerivative( final String chrId, final ArrayList<double[]> values )
	{
		//Boundary condition: derivative 0 for those which cannot be calculated
		System.out.println( chrId + "\t" + (int) values.get( 0 )[ 0 ]+ "\t" + (int) values.get( 0 )[ 1 ] + "\t" + 0.0 );
		final double[] emptyArray1 = new double[ 3 ];
		final double[] emptyArray2 = new double[ 3 ];
		for( int i = 1; i < values.size() - 1; i++ )
		{
			//If leading and tailing Value are flanking the center value with distance 1, calculate center value
			if( values.get( i + 1 )[ 0 ] - values.get( i - 1 )[ 0 ] == 2 )
			{
				derive( chrId, values.get( i - 1 ), values.get( i + 1 ) ); 				
			}
			
			//Go through cases in which the position values of the bedgraph file are not continous. 
			else
			{
				if( values.get( i )[ 0 ] - values.get( i - 1 )[ 0 ] == 1 )
				{ 
					emptyArray1[ 0 ] = values.get( i )[ 0 ] + 1; 
					emptyArray1[ 1 ] = values.get( i )[ 1 ] + 1;
					derive( chrId, values.get( i -1 ), emptyArray1 ); 
				}
				else if( values.get( i + 1 )[ 0 ] - values.get( i )[ 0 ] == 1 )
				{
					emptyArray1[ 0 ] = values.get( i )[ 0 ] - 1; 
					emptyArray1[ 1 ] = values.get( i )[ 1 ] - 1;
					derive( chrId, emptyArray1, values.get( i + 1 ) );
				}
				else
				{
					emptyArray1[ 0 ] = values.get( i )[ 0 ] - 1; 
					emptyArray1[ 1 ] = values.get( i )[ 1 ] - 1;
					emptyArray2[ 0 ] = values.get( i )[ 0 ] + 1; 
					emptyArray2[ 1 ] = values.get( i )[ 1 ] + 1;
					derive( chrId, emptyArray1, emptyArray2 ); 
				}
			}
		}
		
		//Boundary condition: derivative 0 for those which cannot be calculated
		System.out.println( chrId + "\t" + (int) values.get( values.size() - 1 )[ 0 ]+ "\t" + (int) values.get( values.size() - 1 )[ 1 ] + "\t" + 0.0 );
	}
	
	public void derive( final String chrId, final double[] leadingValues, final double[] tailingValues )
	{
		final double derivative = ( tailingValues[ 2 ] - leadingValues[ 2 ] )/2.0; 
		System.out.println( chrId + "\t" + (int) (leadingValues[ 0 ] + 1) + "\t" + (int) (leadingValues[ 1 ] + 1) + "\t" + derivative ); 			
	}
	
	/**
	 * Add current line to ArrayList holding all values for the current chromosome
	 * @param line
	 * @param values
	 */
	public void addLine( final String[] line, final ArrayList<double[]> values )
	{
		final int start = (int) Double.parseDouble( line[ 1 ] );
		final int end = (int) Double.parseDouble( line[ 2 ] );
		
		if( end - start == 1 )
		{
			final double[] v = new double[ 3 ];
			v[ 0 ] = start; 
			v[ 1 ] = end;  
			v[ 2 ] = Double.parseDouble( line[ 3 ] );
			values.add( v ); 
		}
		else 
		{
			System.err.println( "This bedgraph seems to be compressed. Please use only uncompressed bedgraphs."); 			
		}
	}
	
	/**
	 * Main method used for running this script from shell
	 * @param args
	 */
	public static void main(String[] args) 
	{
		if( args.length != 1 )
		{
			final String info = "\n#######################\n" +
					"java -jar deriveBedgraph.jar input.bedgraph\n" +
					"#######################\n" +
					"This script generates the derivative of the input bedgraph\n" +
					"Derivative is printed to STDOUT as a bedgraph.\n" + 
					"Please provide an input bedgraph\n" + 
					"Store to file by redirecting stdout to file (cat input.bedgraph | java -jar deriveBedgraph.jar > outputFile.bedgraph)\n" +
					"#######################\n\n";  
			System.err.println( info ); 
		}
		else 
		{
			final String file = args[ 0 ]; 
			// Initiate and read input stream
			//InputStreamReader inp = new InputStreamReader( System.in );
			//BufferedReader in = new BufferedReader( inp );
			BufferedReader in = TextFileAccess.openFileRead( file ); 
			
			// Start main 
			new DeriveBedgraph( in ); 
		}

	}

}
