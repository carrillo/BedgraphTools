package infrastructure;

import inputOutput.TextFileAccess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

public class PairwiseShuffle 
{
	public static void shuffle( final List<?> list1, final List<?> list2 ) 
	{
		Random rnd = new Random();
		 
        final int size = list1.size();
        Object arr1[] = list1.toArray();
        Object arr2[] = list2.toArray();
  
        // Shuffle array
        for (int i=size; i>1; i--)
        {
        	final int r = rnd.nextInt(i);
        	swap( arr1, i-1, r );
        	swap( arr2, i-1, r );
        }
  
        // Dump array back into list
        ListIterator it1 = list1.listIterator();
        ListIterator it2 = list2.listIterator();
        
		for (int i=0; i<arr1.length; i++) 
		{
			it1.next();
			it2.next();
			it1.set( arr1[i] );
			it2.set( arr2[i] );
		}
	}
	
	private static void swap( final Object[] arr, final int i, final int j ) 
	{
		Object tmp = arr[i];
		arr[i] = arr[j];
		arr[j] = tmp;
	}
	
	public static ArrayList< String > intoObjects( final String fileName, final int numLines ) throws IOException
	{
		final BufferedReader in = TextFileAccess.openFileRead( fileName );
		final ArrayList< String > list = new ArrayList<String>();
		
		while ( true )
		{
			String s = "";
			
			for ( int i = 0; i < numLines; ++i )
			{
				if ( in.ready() )
					s += in.readLine() + "\n";
				else
					return list;
			}
			list.add( s );
		}
	}
	
	public static void write( final ArrayList< String > list, final String fileName )
	{
		final PrintWriter out = TextFileAccess.openFileWrite( fileName );
		
		for ( final String s : list )
			out.print( s );
		
		out.close();
	}
	
	public static void main( String[] args ) throws IOException
	{
		final int numLines = Integer.parseInt( args[ 0 ] );
		
		final String fileName1 = args[ 1 ];
		final ArrayList< String > list1 = intoObjects( fileName1, numLines );
		
		if ( args.length == 2 )
		{
			Collections.shuffle( list1 );
			write( list1, fileName1 + ".shuffle" );
		}
		else
		{
			final String fileName2 = args[ 2 ];
			final ArrayList< String > list2 = intoObjects( fileName2, numLines );
			
			shuffle( list1, list2 );
			
			write( list1, fileName1 + ".shuffle" );
			write( list2, fileName2 + ".shuffle" );
		}
	}
}
