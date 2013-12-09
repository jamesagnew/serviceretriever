package net.svcret.admin.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class BaseDtoList<T extends BaseDtoObject> implements Iterable<T>, Serializable {

	private static final long serialVersionUID = 1L;

	private Comparator<? super T> myComparator;
	private Date myLastMerged;
	private List<T> myList;

	public BaseDtoList() {
		myList = new ArrayList<T>();
	}

	public void add(T theObject) {
		myList.add(theObject);
		sort();
	}

	public void addAll(Collection<T> theList) {
		myList.addAll(theList);
		sort();
	}

	public int indexOf(T theObj) {
		return myList.indexOf(theObj);
	}

	public void clear() {
		myList.clear();
	}

	public T get(int theIndex) {
		return myList.get(theIndex);
	}

	/**
	 * @return the lastMerged
	 */
	public Date getLastMerged() {
		return myLastMerged;
	}

	public Iterator<T> iterator() {
		return myList.iterator();
	}

	public void mergeResults(BaseDtoList<T> theResult) {
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
		sort();
		myLastMerged = new Date();
	}

	public T remove(int theIndex) {
		return myList.remove(theIndex);
	}

	public void remove(T theObject) {
		myList.remove(theObject);
	}

	public int size() {
		return myList.size();
	}

	public Collection<T> toCollection() {
		return Collections.unmodifiableCollection(myList);
	}

	public List<T> toList() {
		return Collections.unmodifiableList(myList);
	}

	private void sort() {
		if (myComparator != null) {
			Collections.sort(myList, myComparator);
		}
	}

	protected List<T> getListForJaxb() {
		if (myList == null) {
			myList = new ArrayList<T>();
		}
		return myList;
	}

	protected void setComparator(Comparator<? super T> theComparator) {
		myComparator = theComparator;
		sort();
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
