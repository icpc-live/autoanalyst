package model;

public class Problem implements ApiEntity {
	final String id;
	final String abbreviation;
	final String name;
	final String color;
	
	public Problem(String id, String name, String label, String color) {
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
		this.color = color;
	}
	
	@Override
	public String toString() {
		return String.format("#p%s", abbreviation);
	}

	public String getId() {
		return id;
	}
	
	public String getLabel() {
		return abbreviation;
	}

	public String getColor() {
		return color;
	}

	public String getName() { return name; }

	public String getNameAndLabel() {
		String prefix = abbreviation + " - ";
		if (name.startsWith(prefix)) {
			return name;
		} else {
			return prefix + name;
		}
	}

}
