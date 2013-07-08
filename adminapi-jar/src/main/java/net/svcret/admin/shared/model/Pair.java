package net.svcret.admin.shared.model;

import java.io.Serializable;

public class Pair<T extends Serializable> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private T myFirst;
	private T mySecond;
	public Pair() {
	}
	public Pair(T theFirst, T theSecond) {
		super();
		myFirst = theFirst;
		mySecond = theSecond;
	}
	/**
	 * @return the first
	 */
	public T getFirst() {
		return myFirst;
	}
	/**
	 * @return the second
	 */
	public T getSecond() {
		return mySecond;
	}
	/**
	 * @param theFirst the first to set
	 */
	public void setFirst(T theFirst) {
		myFirst = theFirst;
	}
	/**
	 * @param theSecond the second to set
	 */
	public void setSecond(T theSecond) {
		mySecond = theSecond;
	}
	
}
