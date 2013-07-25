package net.svcret.ejb.model.entity;

import java.util.Collection;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BasePersServiceCatalogItem extends BasePersKeepsRecentTransactions {

	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = {}, optional = true)
	@JoinColumn(name = "MOST_REC_MONITORRULE_FIR", nullable = true)
	private PersMonitorRuleFiring myMostRecentMonitorRuleFiring;

	public abstract Collection<? extends BasePersServiceVersion> getAllServiceVersions();

	public PersMonitorRuleFiring getMostRecentMonitorRuleFiring() {
		return myMostRecentMonitorRuleFiring;
	}

	public boolean isMostRecentMonitorRuleFiringActive() {
		return getMostRecentMonitorRuleFiring() != null && getMostRecentMonitorRuleFiring().getEndDate() == null;
	}

	/**
	 * @param theMostRecentMonitorRuleFiring
	 *            the mostRecentMonitorRuleFiring to set
	 */
	public void setMostRecentMonitorRuleFiring(PersMonitorRuleFiring theMostRecentMonitorRuleFiring) {
		myMostRecentMonitorRuleFiring = theMostRecentMonitorRuleFiring;
	}

}
