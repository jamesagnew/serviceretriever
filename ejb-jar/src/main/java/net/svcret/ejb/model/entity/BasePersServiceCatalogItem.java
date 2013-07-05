package net.svcret.ejb.model.entity;

import java.util.Collection;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BasePersServiceCatalogItem extends BasePersKeepsRecentTransactions {

	private static final long serialVersionUID = 1L;

	public abstract Collection<? extends BasePersServiceVersion> getAllServiceVersions();


}
