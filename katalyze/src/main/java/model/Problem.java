package model;

public class Problem implements ApiEntity {
	final String id;
	final String label;
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
			this.label = fakeLabel;
		} else {
			this.label = label;
		}
		this.name = name.trim();
		this.color = color;
	}
	
	@Override
	public String toString() {
		return String.format("#p%s", label);
	}

	public String getId() {
		return id;
	}
	
	public String getLabel() {
		return label;
	}

	public String getColor() {
		return color;
	}

	public String getName() { return name; }

	public String getNameAndLabel() {
		String prefix = label + " - ";
		if (name.startsWith(prefix)) {
			return name;
		} else {
			return prefix + name;
		}
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Problem)) {
			return false;
		}
		Problem other = (Problem) o;
		return id.equals(other.id);
	}

	public String stringForCommentary() {
		// This will at some point change into "{problems:<problem ID>}".
		return getNameAndLabel();
	}

}
