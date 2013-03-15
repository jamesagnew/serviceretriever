package ca.uhn.sail.proxy.admin.shared.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class BaseGList<T extends BaseGObject<T>> extends BaseGListenable<T> implements Iterable<T> {

	private static final long serialVersionUID = 1L;

	private List<T> myList;

	// private final MyDataProvider myDataProvider;

	public BaseGList() {
		myList = new ArrayList<T>();

		// myDataProvider = new MyDataProvider();
		//
		// // Dataprovider gives a nice wrapped list that detects underlying
		// changes
		// myDataProvider.setList(myList);
		// myList = myDataProvider.getList();
	}

	public void add(T theObject) {
		myList.add(theObject);
		fireChanged();
	}

	public void addAll(Collection<T> theList) {
		myList.addAll(theList);
	}

	public void mergeResults(BaseGList<T> theResult) {
		for (int i = 0; i < theResult.size(); i++) {
			T nextSrc = theResult.get(i);

			if (size() <= i) {
				nextSrc.initChildList();
				myList.add(nextSrc);
				nextSrc.markInitialized();
			} else {
				T nextDest = get(i);
				if (nextDest.getPid() == nextSrc.getPid()) {
					nextDest.merge(nextSrc);
				} else {
					nextSrc.initChildList();
					myList.add(i, nextSrc);
				}
			}
		}
		while (myList.size() > theResult.size()) {
			myList.remove(myList.size() - 1);
		}
		markInitialized();
		fireChanged();
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
