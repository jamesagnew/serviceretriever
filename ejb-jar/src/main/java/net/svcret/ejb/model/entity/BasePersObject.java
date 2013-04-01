package net.svcret.ejb.model.entity;

import java.io.Serializable;

import javax.persistence.Transient;

import com.google.common.base.Objects;

public abstract class BasePersObject implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Transient
	private boolean myNewlyCreated;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		if (theObj == null) {
			return false;
		}
		
		if (getClass().equals(theObj.getClass()) == false) {
			return false;
		}

		BasePersObject obj = (BasePersObject) theObj;
		
		if (getPid() == null && obj.getPid() == null) {
			return this == theObj;
		}
		
		return Objects.equal(getPid(), obj.getPid());
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
