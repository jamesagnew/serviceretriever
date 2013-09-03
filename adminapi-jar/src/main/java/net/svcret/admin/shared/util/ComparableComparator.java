package net.svcret.admin.shared.util;

import java.util.Comparator;

public class ComparableComparator<T extends Comparable<T>> implements Comparator<T> {

	@Override
	public int compare(T theO1, T theO2) {
		return theO1.compareTo(theO2);
	}

}
