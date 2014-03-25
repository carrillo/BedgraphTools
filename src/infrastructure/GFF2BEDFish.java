package infrastructure;

import inputOutput.TextFileAccess;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class GFF2BEDFish 
{
	public static String defaultSortId = "gene_id";
	
	public static void main( String[] args ) throws IOException
	{
		//args = new String[]{ "--fpkm", "transcripts.gtf" };
		
		final BufferedReader in;
		boolean fpkm = false;
		
		if ( args.length == 0 )
		{
			// Initiate and read input stream
			InputStreamReader inp = new InputStreamReader( System.in );
			in = new BufferedReader( inp );  
		}
		else
		{
			for ( int i = 0; i < args.length - 1; ++i )
				if ( args[ i ].toLowerCase().equals( "--fpkm" ) )
					fpkm = true;
			
			System.err.println( "Trying to extract FPKM values." );
			
			final File f = new File( args[ args.length - 1 ] );
			
			if ( !f.exists() )
			{
				System.out.println( "File '" + f.getAbsolutePath() + "' does not exist." );
				System.exit( 0 );
			}
			in = TextFileAccess.openFileRead( f.getAbsolutePath() );
		}
		
		System.err.println( "WARNING: Only using 'exon' features." );
		
		writeBED( readGFF( in, defaultSortId, fpkm ), fpkm );
	}
	
	
	/**
	 * Write the contents of the list BED-formatted into stout.
	 * 
	 * @param list - the {@link List} with {@link Transcript}s
	 */
	public static void writeBED( final List< Transcript > list, final boolean fpkm )
	{
		PrintWriter out = TextFileAccess.openFileWrite( "zebrafish.bed" ); 
		for ( final Transcript transcript : list )
			out.println( transcript.assembleBEDString( fpkm ) );
		
		out.close(); 
	}
	
	/**
	 * Read GTF data from a stream into a sorted list (sorted by chromosome & chromosome position, ordered by transcript_id by default)
	 * 
	 * @param in - the {@link BufferedReader}
	 * @param sortId - the id the {@link Feature}s will be sorted inside a transcript, by default this should be "transcript_id"
	 * @return - a sorted {@link ArrayList} containing all {@link Transcript}s containing all {@link Feature}s
	 * @throws IOException - if stream is corrupt
	 */
	public static ArrayList< Transcript > readGFF( final BufferedReader in, final String sortId, final boolean fpkm ) throws IOException
	{		
		/*
		---- GTF ----
		
		chrI	Cufflinks	exon	10413	10585	.	+	.	gene_id "XLOC_000001"; transcript_id "TCONS_00000002"; exon_number "1"; gene_name "Y74C9A.2.3"; oId "Y74C9A.2.3"; nearest_ref "Y74C9A.2.3"; class_code "="; tss_id "TSS1"; p_id "P1";
		chrI	Cufflinks	exon	11618	11689	.	+	.	gene_id "XLOC_000001"; transcript_id "TCONS_00000002"; exon_number "2"; gene_name "Y74C9A.2.3"; oId "Y74C9A.2.3"; nearest_ref "Y74C9A.2.3"; class_code "="; tss_id "TSS1"; p_id "P1";
		chrI	Cufflinks	exon	14951	15160	.	+	.	gene_id "XLOC_000001"; transcript_id "TCONS_00000002"; exon_number "3"; gene_name "Y74C9A.2.3"; oId "Y74C9A.2.3"; nearest_ref "Y74C9A.2.3"; class_code "="; tss_id "TSS1"; p_id "P1";
		chrI	Cufflinks	exon	16473	16842	.	+	.	gene_id "XLOC_000001"; transcript_id "TCONS_00000002"; exon_number "4"; gene_name "Y74C9A.2.3"; oId "Y74C9A.2.3"; nearest_ref "Y74C9A.2.3"; class_code "="; tss_id "TSS1"; p_id "P1";
		chrI	Cufflinks	exon	10413	10585	.	+	.	gene_id "XLOC_000001"; transcript_id "TCONS_00000001"; exon_number "1"; gene_name "Y74C9A.2.2"; oId "CUFF.5.2"; nearest_ref "Y74C9A.2.2"; class_code "j"; tss_id "TSS1";
		chrI	Cufflinks	exon	11618	11689	.	+	.	gene_id "XLOC_000001"; transcript_id "TCONS_00000001"; exon_number "2"; gene_name "Y74C9A.2.2"; oId "CUFF.5.2"; nearest_ref "Y74C9A.2.2"; class_code "j"; tss_id "TSS1";
		chrI	Cufflinks	exon	14951	15160	.	+	.	gene_id "XLOC_000001"; transcript_id "TCONS_00000001"; exon_number "3"; gene_name "Y74C9A.2.2"; oId "CUFF.5.2"; nearest_ref "Y74C9A.2.2"; class_code "j"; tss_id "TSS1";
		chrI	Cufflinks	exon	16473	16585	.	+	.	gene_id "XLOC_000001"; transcript_id "TCONS_00000001"; exon_number "4"; gene_name "Y74C9A.2.2"; oId "CUFF.5.2"; nearest_ref "Y74C9A.2.2"; class_code "j"; tss_id "TSS1";
		chrI	Cufflinks	exon	16702	16842	.	+	.	gene_id "XLOC_000001"; transcript_id "TCONS_00000001"; exon_number "5"; gene_name "Y74C9A.2.2"; oId "CUFF.5.2"; nearest_ref "Y74C9A.2.2"; class_code "j"; tss_id "TSS1";
		
		seqname - The name of the sequence. Must be a chromosome or scaffold.
		source - The program that generated this feature.
		feature - The name of this type of feature. Some examples of standard feature types are "CDS", "start_codon", "stop_codon", and "exon".
		start - The starting position of the feature in the sequence. The first base is numbered 1.
		end - The ending position of the feature (inclusive).
		score - A score between 0 and 1000. If the track line useScore attribute is set to 1 for this annotation data set, the score value will determine the level of gray in which this feature is displayed (higher numbers = darker gray). If there is no score value, enter ".".
		strand - Valid entries include '+', '-', or '.' (for don't know/don't care).
		frame - If the feature is a coding exon, frame should be a number between 0-2 that represents the reading frame of the first base. If the feature is not a coding exon, the value should be '.'.
		group - All lines with the same group are linked together into a single item.		
		*/

		final HashMap< String, Transcript > transcripts = new HashMap<String, Transcript>();
		final HashSet< String > ignoredFeatures = new HashSet< String >();
		
		while ( in.ready() )
		{
			
			final String[] line = in.readLine().split( "\t" );
			 
			if( !line[ 0 ].substring(0,1).equals("#") )
			{
				final String seqname = line[ 0 ];
				//final String source = line[ 1 ];
				final String featureName = line[ 2 ];
				
				if ( !featureName.toLowerCase().trim().equals( "exon") )
				{
					if ( !ignoredFeatures.contains( featureName ) )
						ignoredFeatures.add( featureName );
					
					continue;
				}
				
				final long start = Long.parseLong( line[ 3 ] );
				final long end = Long.parseLong( line[ 4 ] );
				
				//final String score = line[ 5 ];
				final boolean strand;
				
				if ( line[ 6 ].equals( "+") )
					strand = true;
				else
					strand = false;
				
				//final String frame = line[ 7 ];
				final String group = line[ 8 ];
				
				// find the transcript 
				final String idenfier = extractId( group, sortId ); 
				Transcript transcript = transcripts.get( idenfier );
				
				// build a feature
				final Feature feature = new Feature( seqname, start, end, idenfier, strand );
				
				if ( fpkm )
					feature.setFPKM( extractId( group, "FPKM" ) );
				
				if ( transcript == null )
				{
					// add new one
					transcript = new Transcript( idenfier );
					transcript.addFeature( feature );
					transcripts.put( idenfier, transcript );
				}
				else
				{
					// add new feature to the transcript
					transcript.addFeature( feature );
				}
				
				/*
			if ( transcripts.size() == 6 )
			{
				final ArrayList< Transcript > list = sort( transcripts );
				for ( final Transcript tr : list )
				{
					System.out.println( tr );
					for ( final Feature f : tr.features )
						System.out.println( f );						
				}
				
				System.exit( 0 );
			}
				 */
				
			}
		}
		
		if ( ignoredFeatures.size() > 0 )
		{
			System.err.println( "ignored the following features:" );
			
			for ( final String feature : ignoredFeatures )
				System.err.print( "'" + feature + "' " );
			
			System.err.println();
		}
		
		return sort( transcripts );
	}
	
	/**
	 * Copies all entries of the {@link HashMap} into an {@link ArrayList} and sorts it
	 * 
	 * @param transcripts
	 * @return
	 */
	public static ArrayList< Transcript > sort( final HashMap< String, Transcript > transcripts )
	{
		final ArrayList< Transcript > list = new ArrayList<Transcript>();
		
		for ( final Transcript tr : transcripts.values() )
		{
			tr.sortFeatures();
			list.add( tr );
		}
		
		Collections.sort( list );
		
		return list;
	}
	
	/**
	 * Extracts the value for a certain id in the GTF group string (e.g. the value for "transcript_id")
	 * 
	 * @param group - the String containing all values
	 * @param id - the id to look for
	 * @return - the value inside the " "
	 */
	public static String extractId( final String group, final String id )
	{
		// the index in the string of the feature we are looking for
		int i = group.indexOf( id );
		
		if ( i < 0 )
			return "";
		
		// add the length of the string
		i += id.length();
		
		
		return group.substring( i + 1 ).split(";")[ 0 ];
	}
}
