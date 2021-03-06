package util;

public class Range {
	public static final Range ZERO = new Range(0, 0);
	
	private final int from, to;
	
	public Range(int from, int to) {
		if (from > to) throw new IndexOutOfBoundsException(from+"->"+to); 
		this.from = from;
		this.to = to;
	}
	
	public int from() {return from;}
	public int to() {return to;}
	public int interval() {return to - from;}
	
	@Override
	public String toString() {
		return from + "->" + to;
	}
}
