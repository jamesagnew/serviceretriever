package net.svcret.ejb.model.entity;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BasePersInvocationMethodStats<P extends BasePersInvocationMethodStatsPk<P, O>, O extends BasePersInvocationMethodStats<P, O>> extends BasePersInvocationStats<P, O> {

	private static final long serialVersionUID = 1L;

}
