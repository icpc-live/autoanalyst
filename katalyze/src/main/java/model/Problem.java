package model;

public class Problem {
	final String id;
	final String abbreviation;
	final String name;
	
	public Problem(String id, String name, String label) {
		this.id = id;
		if (label == null || label.isEmpty()) {
			String fakeLabel;
			try {
				int problemNumber = Integer.parseInt(id);
				fakeLabel = "" + Character.toChars(64 + problemNumber)[0];
			}
			catch (NumberFormatException e) {
				fakeLabel = id;
			}
			abbreviation = fakeLabel;
		} else {
			this.abbreviation = label;
		}
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
