package net.svcret.ejb.model.entity;

import javax.persistence.Transient;

public abstract class BasePersMethodStats {

	public abstract BasePersMethodStatsPk getPk();

	public abstract void mergeUnsynchronizedEvents(BasePersMethodStats theNext);

	@Transient
	private transient boolean myNewlyCreated;

	/**
	 * @return the newlyCreated
	 */
	public boolean isNewlyCreated() {
		return myNewlyCreated;
	}

	/**
	 * @param theNewlyCreated the newlyCreated to set
	 */
	public void setNewlyCreated(boolean theNewlyCreated) {
		myNewlyCreated = theNewlyCreated;
	}
	
}
