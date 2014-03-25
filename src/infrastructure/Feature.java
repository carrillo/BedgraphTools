package infrastructure;

public class Feature implements Comparable< Feature >
{
	final String chr;
	String fpkm = ""; 
	final long start, end; 
	final String name;
	final boolean strand;
	
	public Feature ( final String chr, final long start, final long end, final String name, final boolean strand )
	{
		this.chr = chr;
		this.start = start;
		this.end = end;
		this.name = name;
		this.strand = strand;
	}
	
	public void setFPKM( String fpkm ) { this.fpkm = fpkm; }
	
	public final String toString()
	{
		return chr + "\t" + start + "\t" + end + "\t" + name + "\t" + strand; 
	}

	@Override
	public int compareTo( final Feature f )
	{
		return (int)( this.start - f.start );
	}
	
}
