package ca.uhn.sail.proxy.model.entity;

public abstract class BasePersMethodStats {

	public abstract BasePersMethodStatsPk getPk();

	public abstract void mergeUnsynchronizedEvents(BasePersMethodStats theNext);

	
}
