package net.svcret.ejb.model.entity;

import javax.persistence.Transient;

import com.google.common.base.Objects;

public abstract class BasePersObject {

	@Transient
	private boolean myNewlyCreated;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		return getClass().equals(theObj.getClass()) && Objects.equal(getPid(), ((BasePersObject) theObj).getPid());
	}

	public abstract Long getPid();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(getPid());
	}

	/**
	 * @return the newlyCreated
	 */
	public boolean isNewlyCreated() {
		return myNewlyCreated;
	}

	/**
	 * @param theNewlyCreated
	 *            the newlyCreated to set
	 */
	public void setNewlyCreated(boolean theNewlyCreated) {
		myNewlyCreated = theNewlyCreated;
	}

}
