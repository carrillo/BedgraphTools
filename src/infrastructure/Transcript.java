package infrastructure;

import java.util.ArrayList;
import java.util.Collections;

public class Transcript implements Comparable< Transcript >
{
	final public ArrayList< Feature > features;
	final String id;
	boolean isSorted = false;
	
	public Transcript( final String id )
	{
		this.id = id;
		this.features = new ArrayList<Feature>();
	}
	
	public void addFeature( final Feature feature )
	{
		features.add( feature );
		this.isSorted = false;
	}
	
	public void sortFeatures()
	{
		Collections.sort( features );
		this.isSorted = true;
	}

	/**
	 * Assemble the output BED-formatted
	 * 
	 * @return - the {@link String} containing all information
	 */
	public String assembleBEDString( final boolean fpkm )
	{
		/*
		---- BED ----
		
		The first three required BED fields are:
		
		chrom - The name of the chromosome (e.g. chr3, chrY, chr2_random) or scaffold (e.g. scaffold10671).
		chromStart - The starting position of the feature in the chromosome or scaffold. The first base in a chromosome is numbered 0.
		chromEnd - The ending position of the feature in the chromosome or scaffold. The chromEnd base is not included in the display of the feature. For example, the first 100 bases of a chromosome are defined as chromStart=0, chromEnd=100, and span the bases numbered 0-99.
		The 9 additional optional BED fields are:
		
		name - Defines the name of the BED line. This label is displayed to the left of the BED line in the Genome Browser window when the track is open to full display mode or directly to the left of the item in pack mode.
		score - A score between 0 and 1000. If the track line useScore attribute is set to 1 for this annotation data set, the score value will determine the level of gray in which this feature is displayed (higher numbers = darker gray). This table shows the Genome Browser's translation of BED score values into shades of gray:
		strand - Defines the strand - either '+' or '-'.
		thickStart - The starting position at which the feature is drawn thickly (for example, the start codon in gene displays).
		thickEnd - The ending position at which the feature is drawn thickly (for example, the stop codon in gene displays).
		itemRgb - An RGB value of the form R,G,B (e.g. 255,0,0). If the track line itemRgb attribute is set to "On", this RBG value will determine the display color of the data contained in this BED line. NOTE: It is recommended that a simple color scheme (eight colors or less) be used with this attribute to avoid overwhelming the color resources of the Genome Browser and your Internet browser.
		blockCount - The number of blocks (exons) in the BED line.
		blockSizes - A comma-separated list of the block sizes. The number of items in this list should correspond to blockCount.
		blockStarts - A comma-separated list of block starts. All of the blockStart positions should be calculated relative to chromStart. The number of items in this list should correspond to blockCount.
		*/

		if ( !isSorted )
			sortFeatures();
		
		// the first feature is required several time
		final Feature f1 = features.get( 0 );
		
		//
		// Build the string for a transcript
		//
		
		// the chr is the same for all features
		final StringBuilder b = new StringBuilder( f1.chr );
		b.append( '\t' );
		
		// the chromosome start position is the start of the first feature
		b.append( f1.start );
		b.append( '\t' );
		
		// the chromosome end position is the end of the last feature
		b.append( features.get( features.size() - 1 ).end );
		b.append( '\t' );
		
		// the name is the id of the transcript
		b.append( id );
		
		if ( fpkm )
		{
			b.append( ';' );
			b.append( f1.fpkm );
		}
		
		b.append( '\t' );
		
		// score
		b.append( '0' );
		b.append( '\t' );
				
		// the strand is the same for all features
		if ( f1.strand )
			b.append( '+' );
		else
			b.append( '-' );
		b.append( '\t' );
		
		// thickStart is the same as start
		b.append( f1.start );
		b.append( '\t' );

		// thickEnd is the same as end
		b.append( features.get( features.size() - 1 ).end );
		b.append( '\t' );
		
		// itemRGB
		b.append( '0' );
		b.append( '\t' );
		
		// blockCount
		b.append( features.size() );
		b.append( '\t' );

		// blockSizes
		for ( final Feature f : features )
		{
			b.append( f.end - f.start + 1 );
			b.append( ',' );
		}
		b.append( '\t' );

		// blockStarts
		for ( final Feature f : features )
		{
			b.append( f.start - f1.start );
			b.append( ',' );
		}
		
		return b.toString();
	}
	
	public String toString()
	{
		return id + " " + features.size() + " features.";
	}

	@Override
	public int compareTo( final Transcript o ) 
	{
		if ( !isSorted )
			sortFeatures();
		
		if ( !o.isSorted )
			o.sortFeatures();
		
		final Feature f1 = features.get( 0 );
		final Feature f2 = o.features.get( 0 );
		final int cmp = f1.chr.compareTo( f2.chr );
		
		if ( cmp == 0 )
			return (int)( f1.start - f2.start );
		else
			return cmp;
	}
}
