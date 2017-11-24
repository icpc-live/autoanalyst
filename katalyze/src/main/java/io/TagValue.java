package io;

public class TagValue implements Token {
	final String value;
	
	public TagValue(String value) {
		assert value != null;
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
	
	@Override
	public String toString() {
		return "\""+value+"\"";
	}
	
	@Override
	public boolean equals(Object other) {
		return (other instanceof TagValue) && ((TagValue) other).value.equals(value);
	}
	
	@Override
	public int hashCode() {
		return value.hashCode();
	}
	
	public boolean isWhiteSpace() {
		return value.trim().length() == 0;
	}
	

}
