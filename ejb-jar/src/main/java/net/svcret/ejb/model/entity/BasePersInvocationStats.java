package net.svcret.ejb.model.entity;

import java.io.Serializable;

import javax.persistence.Transient;

public abstract class BasePersInvocationStats implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract BasePersInvocationStatsPk getPk();

	public abstract void mergeUnsynchronizedEvents(BasePersInvocationStats theNext);

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
