package net.svcret.ejb.model.entity;

import java.io.Serializable;

import javax.persistence.Transient;

public abstract class BasePersStats<P extends BasePersStatsPk<?,?>, O extends BasePersStats<?,?>> implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract P getPk();

	public abstract void mergeUnsynchronizedEvents(O theNext);

	public abstract <T> T accept(IStatsVisitor<T> theVisitor);

	
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
	
	public interface IStatsVisitor<T>
	{

		T visit(PersNodeStats theStats, PersNodeStatsPk thePk);

		T visit(PersStaticResourceStats theStats, PersStaticResourceStatsPk thePk);

		T visit(PersInvocationMethodSvcverStats theStats, PersInvocationMethodSvcverStatsPk thePk);

		T visit(PersInvocationUrlStats theStats, PersInvocationUrlStatsPk thePk);

		T visit(PersInvocationMethodUserStats theStats, PersInvocationMethodUserStatsPk thePk);
		
	}
	
}
