package net.svcret.admin.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class BaseGList<T extends BaseGObject<T>> implements Iterable<T>, Serializable {

	private static final long serialVersionUID = 1L;

	private List<T> myList;

	public void clear() {
		myList.clear();
	}

	public BaseGList() {
		myList = new ArrayList<T>();
	}

	public void add(T theObject) {
		myList.add(theObject);
	}

	public void remove(T theObject) {
		myList.remove(theObject);
	}
	
	public void addAll(Collection<T> theList) {
		myList.addAll(theList);
	}

	public void mergeResults(BaseGList<T> theResult) {
		for (int i = 0; i < theResult.size(); i++) {
			T nextSrc = theResult.get(i);

			if (size() <= i) {
				myList.add(nextSrc);
			} else {
				T nextDest = get(i);
				if (nextDest.getPid() == nextSrc.getPid()) {
					nextDest.merge(nextSrc);
				} else {
					myList.add(i, nextSrc);
				}
			}
		}
		while (myList.size() > theResult.size()) {
			myList.remove(myList.size() - 1);
		}
	}

	public T get(int theIndex) {
		return myList.get(theIndex);
	}

	public int size() {
		return myList.size();
	}

	public Iterator<T> iterator() {
		return myList.iterator();
	}


	// public AbstractDataProvider<T> asDataProvider() {
	// return myDataProvider;
	// }
	//
	// public class MyDataProvider extends ListDataProvider<T> {
	//
	//
	// }

}
