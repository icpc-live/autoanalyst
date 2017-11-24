package stats;

public abstract class Filter<T> {
	final String categoryName;
	
	public Filter(String name) {
		this.categoryName = name;
	}
	public abstract boolean contains(T item);
	
	public String name() {
		return this.categoryName;
	}
	
}
