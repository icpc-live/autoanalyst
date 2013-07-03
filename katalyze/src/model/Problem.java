package model;

public class Problem {
	final String id;
	final String abbreviation;
	final String name;
	
	public Problem(int id, String name) {
		this.id = String.format("%d", id);
		this.abbreviation = ""+Character.toChars(64+id)[0];
		this.name = name.trim();
	}
	
	@Override
	public String toString() {
		return String.format("#p%s", abbreviation);
	}

	public String getId() {
		return id;
	}
	
	public String getLetter() {
		return abbreviation;
	}

	public String getName() {
		String prefix = abbreviation + " - ";
		if (name.startsWith(prefix)) {
			return name;
		} else {
			return prefix + name;
		}
	}

}
