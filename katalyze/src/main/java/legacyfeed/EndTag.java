package legacyfeed;

public class EndTag implements Token {
	String tagName;
	
	public EndTag(String tagName) {
		this.tagName = tagName;
	}
	
	@Override
	public String toString() {
		return "/"+tagName;
	}
	
	@Override
	public boolean equals(Object other) {
		return (other instanceof EndTag) && ((EndTag) other).tagName.equals(tagName);
	}
	
	@Override
	public int hashCode() {
		return tagName.hashCode();
	}	

	public String getName() {
		return tagName;
	}

}
