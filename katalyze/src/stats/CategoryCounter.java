package stats;

public interface CategoryCounter<T> {
	
	boolean process(T item);
	int getCount();

}
