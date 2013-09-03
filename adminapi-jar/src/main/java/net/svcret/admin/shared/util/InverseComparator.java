package net.svcret.admin.shared.util;

import java.util.Comparator;

public class InverseComparator<T> implements Comparator<T> {

	private Comparator<T> myCmp;

	public InverseComparator(Comparator<T> theComparator) {
		myCmp = theComparator;
	}
	
	@Override
	public int compare(T theO1, T theO2) {
		return -myCmp.compare(theO1, theO2);
	}

}
