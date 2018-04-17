package stats;

import java.util.ArrayList;
import java.util.HashMap;

import model.Judgement;


public class CategoryStats<T> implements CategoryCounter<T>{
	
	final Filter<T> other = new Filter<T>("other") {
		public boolean contains(T item) {
			return true;
		}
	};
	
	class CategoryCount {
		Filter<T> category;
		int n;
		
		public CategoryCount(Filter<T> category) {
			this.category = category;
		}
		
		public boolean process(T s) {
			if (category.contains(s)) {
				n++;
				return true;
			} else {
				return false;
			}
		}
				
		public int getCount() {
			return n;
		}
	}
	
	
	ArrayList<CategoryCount> categories = new ArrayList<CategoryCount>();
	HashMap<Filter<T>, CategoryCount> categoryMap = new HashMap<Filter<T>, CategoryCount>();
	CategoryCount otherCount = new CategoryCount(other);
	int n = 0;
	
	public void addCategory(Filter<T> category) {
		this.categories.add(new CategoryCount(category));
	}
	
	
	public boolean process(T submission) {
		n++;
		for(CategoryCount category : categories) {
			if (category.process(submission)) {
				return true;
			}
		}
		otherCount.process(submission);
		return true;
	}
	
	public int getCount() {
		return n;
	}
	
	public int get(Filter<Judgement> category) {
		CategoryCount counter = categoryMap.get(category);
		if (counter == null) {
			return 0;
		} else {
			return counter.getCount();
		}
	}

}
