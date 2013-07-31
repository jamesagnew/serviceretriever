package net.svcret.ejb.model.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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

	public abstract Collection<PersMonitorRuleFiring> getActiveRuleFiringsWhichMightApply();
	
	public PersMonitorRuleFiring getMostRecentMonitorRuleFiring() {
		return myMostRecentMonitorRuleFiring;
	}

	public Set<Long> getActiveMonitorRuleFiringPidsWhichApply() {
		HashSet<Long> retVal = new HashSet<Long>();
		
		for (PersMonitorRuleFiring next : getActiveRuleFiringsWhichMightApply()) {
			Set<Long> applicableSvcVerPids = next.getAppliesToServiceVersionPids();
			for (BasePersServiceVersion nextSvcVer : getAllServiceVersions()) {
				if (applicableSvcVerPids.contains(nextSvcVer.getPid())) {
					retVal.add(next.getPid());
				}
			}
			
		}
		
		return retVal;
	}

	/**
	 * @param theMostRecentMonitorRuleFiring
	 *            the mostRecentMonitorRuleFiring to set
	 */
	public void setMostRecentMonitorRuleFiring(PersMonitorRuleFiring theMostRecentMonitorRuleFiring) {
		myMostRecentMonitorRuleFiring = theMostRecentMonitorRuleFiring;
	}

}
