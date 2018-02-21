package legacyfeed;

public class StartTag implements Token {
	final String tagName;
	
	public StartTag(String tagName) {
		assert tagName != null;
		this.tagName = tagName;
	}
	
	public String getName() {
		return tagName;
	}
	
	@Override
	public String toString() {
		return tagName;
	}

	public boolean matches(EndTag t) {
		return tagName == t.getName();
	}
	
	@Override
	public boolean equals(Object other) {
		return (other instanceof StartTag) && ((StartTag) other).tagName.equals(tagName);
	}
	
	@Override
	public int hashCode() {
		return tagName.hashCode();
	}
	

}
