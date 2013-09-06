package net.svcret.ejb.ejb.log;

import java.util.LinkedList;

import net.svcret.ejb.model.entity.BasePersSavedTransaction;

public abstract class BaseUnflushed<T extends BasePersSavedTransaction> {
	private LinkedList<T> myFail;
	private LinkedList<T> myFault;
	private LinkedList<T> mySecurityFail;
	private LinkedList<T> mySuccess;

	public synchronized int getCount() {
		return getFail().size() + getSecurityFail().size() + getSuccess().size() + getFault().size();
	}

	/**
	 * CALL FROM SYNCHRONIZED CONTEXT
	 */
	protected LinkedList<T> getFail() {
		return myFail;
	}

	/**
	 * CALL FROM SYNCHRONIZED CONTEXT
	 */
	protected LinkedList<T> getFault() {
		return myFault;
	}

	/**
	 * CALL FROM SYNCHRONIZED CONTEXT
	 */
	protected LinkedList<T> getSecurityFail() {
		return mySecurityFail;
	}

	/**
	 * CALL FROM SYNCHRONIZED CONTEXT
	 */
	protected LinkedList<T> getSuccess() {
		return mySuccess;
	}

	/**
	 * CALL FROM SYNCHRONIZED CONTEXT
	 */
	protected void initIfNeeded() {
		if (mySuccess == null) {
			mySuccess = new LinkedList<T>();
			myFault = new LinkedList<T>();
			myFail = new LinkedList<T>();
			mySecurityFail = new LinkedList<T>();
		}
	}

	public synchronized LinkedList<T> getSuccessAndRemove() {
		return getAndRemove(mySuccess);
	}

	public synchronized LinkedList<T> getSecurityFailAndRemove() {
		return getAndRemove(mySecurityFail);
	}

	public synchronized LinkedList<T> getFaultAndRemove() {
		return getAndRemove(myFault);
	}

	public synchronized LinkedList<T> getFailAndRemove() {
		return getAndRemove(myFail);
	}

	private LinkedList<T> getAndRemove(LinkedList<T> list) {
		initIfNeeded();
		
		LinkedList<T> retVal=new LinkedList<T>(list);
		list.clear();
		return retVal;
	}
}