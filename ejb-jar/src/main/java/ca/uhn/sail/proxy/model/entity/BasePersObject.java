package ca.uhn.sail.proxy.model.entity;

import com.google.common.base.Objects;

public abstract class BasePersObject {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		return getClass().equals(theObj.getClass()) && Objects.equal(getPid(), ((BasePersObject) theObj).getPid());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(getPid());
	}

	public abstract Long getPid();
	
}
