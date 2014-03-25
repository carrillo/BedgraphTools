package toolbox;

import inputOutput.TextFileAccess;
import java.io.BufferedReader;
import java.util.HashSet;

import array.tools.StringArrayTools;

public class FilterBEDOnIDs {

	public HashSet<String> idHash; 
	
	public FilterBEDOnIDs( final String inputBED, final String idFile )
	{
		//Initiate HashSet with IDs in idFile
		this.idHash = readIdFile( idFile ); 
		
		//Filter BED file for IDs present in the HashSet
		filterInput( inputBED ); 
		
		//Exit with return code 0 
		System.exit( 0 ); 
	}
	
	/*
	 * This method generates a HashSet containing all the ID values contained in the IDFile. If the IDFile is a multicolumn
	 * tab-separated file, only the value in the first field is considered. 
	 * @param idFile Path to the ID File. 
	 * @return
	 */
	public HashSet<String> readIdFile( final String idFile )
	{
		HashSet<String> idHash =  new HashSet<String>(); 
		
		BufferedReader in = TextFileAccess.openFileRead( idFile );
		try
		{ 
			String[] entries; 
			while( in.ready() )
			{ 
				//Read IDFile line by line, split tab-separated entries and add the first entry to the HashSet. 
				entries = in.readLine().split("\t"); 
				idHash.add( entries[ 0 ] );   
			}
		}
		catch (Exception e) 
		{
			//Print error and exit with code 1. 
			System.err.println( "Unable to hash ID. " + e  );
			System.exit( 1 ); 
		}
		
		return idHash; 
	}
	
	/*
	 * This method filters the input BED file for IDs present in the HashSet.  
	 * @param inputBED Path to the BED file to be filtered. 
	 */
	public void filterInput( final String inputBED )
	{
		BufferedReader in = TextFileAccess.openFileRead( inputBED ); 
		try
		{ 
			String[] entries; 
		
			while( in.ready() )
			{
				//Read BED entries line by line, split tab separated fields and pass to filter method. 
				entries = in.readLine().split("\t");  
				filter( entries );  
			}
		}
		catch (Exception e) 
		{
			//Print error and exit with return code 1. 
			System.err.println( "Unable to filter BED file. " + e );
			System.exit( 1 ); 
		}
	}
	
	/**
	 * This method takes a BED entry and prints it to STDOUT only if it ID is contained in the ID HashSet.
	 * @param entries String[] of the fields of one BED file. 
	 */
	public void filter( String[] entries )
	{	  
		if( this.idHash.contains( entries[ 3 ] ) )
			System.out.println( StringArrayTools.arrayToString( entries ) ); 
	}
	
	public static void main(String[] args) 
	{
		if( args.length != 2 )
		{
			final String info = "\n#######################\n" +
					"java -jar filterBEDOnIDs.jar pathToInputBEDFile pathToIdFile\n" +
					"#######################\n" + 
					"Filters a BED file (pathToInputBEDFile) by testing if the BED entry id (column 4) is present in the ID file (pathToIdFile).\n" +
					"If BED entry ID is contained in the ID File, the BED entry is printed to STDOUT.\n" +
					"Store to file by redirecting stdout to file (java -jar filterBEDOnIDs.jar pathToInputBEDFile pathToIdFile > outputFile.BED)\n" + 
					"Please provide each id on a separate row in the ID file. If the ID file is a multicolumn tab-separated file, only the value in the first field is considered.\n\n";
			System.err.println( info ); 
		}
		else 
		{	
			new FilterBEDOnIDs( args[ 0 ], args[ 1 ] ); 
		}

	}

}
