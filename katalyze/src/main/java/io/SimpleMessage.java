package io;

import java.util.HashMap;
import java.util.Map;

public class SimpleMessage {
	String name;
	Map<String,String> values = new HashMap<String, String>();
	
	public SimpleMessage(String name) { 
		this.name = name;
	}
	
	public void put(String attribute, String value) {
		values.put(attribute, value);
	}
	
	public String get(String attribute) {
		return values.get(attribute);
	}
	
	private static String dropWhiteSpace(String source) {
		return (source == null) ? null : source.replaceAll("\\s", "");
	}
	
	public int getInt(String attribute) {
		return Integer.parseInt(dropWhiteSpace(get(attribute)));
	}
	
	public boolean getBool(String attribute) {
		return "True".equals(dropWhiteSpace(get(attribute)));
	}
	
	public double tryGetDouble(String attribute, double defaultValue) {
		String value = get(attribute);
		if (value != null) {
			return Double.parseDouble(dropWhiteSpace(value));
		}
		return defaultValue;
	}
	
	public double getDouble(String attribute) {
		return Double.parseDouble(dropWhiteSpace(get(attribute)));
	}
	
	public int size() {
		return values.size();
	}
	
	public String getName() {
		return name;
	}
	
	public Map<String, String> getValues() {
		return values;
	}
	
	@Override
	public String toString() {
		return String.format("%s, %d values", name, values.size());
	}

}
