package legacyfeed;

public class EndOfStream implements Token {
	
	final public static EndOfStream instance = new EndOfStream();
	
	private EndOfStream() {	}
	
	@Override
	public String toString() {
		return "<end>";
	}
	
	@Override
	public boolean equals(Object other) {
		return (other instanceof EndOfStream);
	}
	
	@Override
	public int hashCode() {
		return 0;
	}		
}
