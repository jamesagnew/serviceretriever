package net.svcret.admin.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public class BaseDtoList<T extends BaseGObject> implements Iterable<T>, Serializable {

	private static final long serialVersionUID = 1L;

	@XmlTransient
	private Comparator<? super T> myComparator;

	@XmlTransient
	private Date myLastMerged;

	@XmlElement(name = "Element")
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