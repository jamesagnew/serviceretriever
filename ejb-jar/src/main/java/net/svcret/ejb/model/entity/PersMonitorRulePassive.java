package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import net.svcret.admin.shared.enm.MonitorRuleTypeEnum;

@Entity
@DiscriminatorValue("PASSIVE")
@NamedQueries(value = { @NamedQuery(name = Queries.MONITORRULE_FINDALL, query = Queries.MONITORRULE_FINDALL_Q) })
public class PersMonitorRulePassive extends BasePersMonitorRule {

	private static final long serialVersionUID = 1L;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "myRule")
	private Collection<PersMonitorAppliesTo> myAppliesTo;

	@Column(name = "FIRE_FOR_LATENCY_MILLIS", nullable = true)
	private Integer myPassiveFireForBackingServiceLatencyIsAboveMillis;

	@Column(name = "FIRE_FOR_LATENCY_SUS_MINS", nullable = true)
	private Integer myPassiveFireForBackingServiceLatencySustainTimeMins;

	@Column(name = "FIRE_FOR_ALL_URLS", nullable = true)
	private boolean myPassiveFireIfAllBackingUrlsAreUnavailable;

	@Column(name = "FIRE_FOR_SINGLE_URL", nullable = true)
	private boolean myPassiveFireIfSingleBackingUrlIsUnavailable;

	public Collection<PersMonitorAppliesTo> getAppliesTo() {
		if (myAppliesTo == null) {
			myAppliesTo = new ArrayList<PersMonitorAppliesTo>();
		}
		return (myAppliesTo);
	}

	public void setAppliesToItems(BasePersServiceCatalogItem... theItems) {
		setAppliesToItems(Arrays.asList(theItems));
	}

	public void setAppliesToItems(Collection<BasePersServiceCatalogItem> theItems) {
		getAppliesTo();

		ArrayList<BasePersServiceCatalogItem> toAdd = new ArrayList<BasePersServiceCatalogItem>(theItems);
		ArrayList<PersMonitorAppliesTo> toRemove = new ArrayList<PersMonitorAppliesTo>();

		for (PersMonitorAppliesTo nextExisting : myAppliesTo) {
			if (toAdd.contains(nextExisting.getItem())) {
				toAdd.remove(nextExisting.getItem());
			}
			if (!theItems.contains(nextExisting.getItem())) {
				toRemove.add(nextExisting);
			}
		}

		for (BasePersServiceCatalogItem next : toAdd) {
			myAppliesTo.add(new PersMonitorAppliesTo(this, next));
		}

		for (PersMonitorAppliesTo next : toRemove) {
			myAppliesTo.remove(next);
		}

	}

	
	public Set<BasePersServiceVersion> toAppliesToServiceVersions() {
		HashSet<BasePersServiceVersion> retVal = new HashSet<BasePersServiceVersion>();

		for (PersMonitorAppliesTo next : getAppliesTo()) {
			retVal.addAll(next.getItem().getAllServiceVersions());
		}

		return retVal;
	}

	private PersMonitorAppliesTo getAppliesTo(BasePersServiceCatalogItem theItem) {
		for (PersMonitorAppliesTo next : getAppliesTo()) {
			if (next.getItem().equals(theItem)) {
				return next;
			}
		}
		return null;
	}

	/**
	 * @return the fireForBackingServiceLatencyIsAboveMillis
	 */
	public Integer getPassiveFireForBackingServiceLatencyIsAboveMillis() {
		return myPassiveFireForBackingServiceLatencyIsAboveMillis;
	}

	/**
	 * @return the fireForBackingServiceLatencySustainTimeMins
	 */
	public Integer getPassiveFireForBackingServiceLatencySustainTimeMins() {
		return myPassiveFireForBackingServiceLatencySustainTimeMins;
	}

	public boolean isPassiveFireIfAllBackingUrlsAreUnavailable() {
		return myPassiveFireIfAllBackingUrlsAreUnavailable;
	}

	public boolean isPassiveFireIfSingleBackingUrlIsUnavailable() {
		return myPassiveFireIfSingleBackingUrlIsUnavailable;
	}

	public void merge(PersMonitorRulePassive theRule) {
		super.merge(theRule);
		
		setPassiveFireForBackingServiceLatencyIsAboveMillis(theRule.getPassiveFireForBackingServiceLatencyIsAboveMillis());
		setPassiveFireForBackingServiceLatencySustainTimeMins(theRule.getPassiveFireForBackingServiceLatencySustainTimeMins());

		setPassiveFireIfAllBackingUrlsAreUnavailable(theRule.isPassiveFireIfAllBackingUrlsAreUnavailable());
		setPassiveFireIfSingleBackingUrlIsUnavailable(theRule.isPassiveFireIfSingleBackingUrlIsUnavailable());

		for (PersMonitorAppliesTo nextApplies : new ArrayList<PersMonitorAppliesTo>(theRule.getAppliesTo())) {
			PersMonitorAppliesTo existing = getAppliesTo(nextApplies.getItem());
			if (existing == null) {
				getAppliesTo().add(new PersMonitorAppliesTo(this, nextApplies.getItem()));
			}
		}
		for (Iterator<PersMonitorAppliesTo> iter = getAppliesTo().iterator(); iter.hasNext();) {
			PersMonitorAppliesTo nextApplies = iter.next();
			PersMonitorAppliesTo wanted = theRule.getAppliesTo(nextApplies.getItem());
			if (wanted == null) {
				iter.remove();
			}
		}

	}

	/**
	 * @param thePassiveFireForBackingServiceLatencyIsAboveMillis
	 *            the fireForBackingServiceLatencyIsAboveMillis to set
	 */
	public void setPassiveFireForBackingServiceLatencyIsAboveMillis(Integer thePassiveFireForBackingServiceLatencyIsAboveMillis) {
		if (thePassiveFireForBackingServiceLatencyIsAboveMillis != null) {
			if (thePassiveFireForBackingServiceLatencyIsAboveMillis < 1) {
				throw new IllegalArgumentException("Invalid value: " + thePassiveFireForBackingServiceLatencyIsAboveMillis);
			}
		}
		myPassiveFireForBackingServiceLatencyIsAboveMillis = thePassiveFireForBackingServiceLatencyIsAboveMillis;
	}

	/**
	 * @param thePassiveFireForBackingServiceLatencySustainTimeMins
	 *            the fireForBackingServiceLatencySustainTimeMins to set
	 */
	public void setPassiveFireForBackingServiceLatencySustainTimeMins(Integer thePassiveFireForBackingServiceLatencySustainTimeMins) {
		myPassiveFireForBackingServiceLatencySustainTimeMins = thePassiveFireForBackingServiceLatencySustainTimeMins;
	}

	public void setPassiveFireIfAllBackingUrlsAreUnavailable(boolean thePassiveFireIfAllBackingUrlsAreUnavailable) {
		myPassiveFireIfAllBackingUrlsAreUnavailable = thePassiveFireIfAllBackingUrlsAreUnavailable;
	}

	public void setPassiveFireIfSingleBackingUrlIsUnavailable(boolean thePassiveFireIfSingleBackingUrlIsUnavailable) {
		myPassiveFireIfSingleBackingUrlIsUnavailable = thePassiveFireIfSingleBackingUrlIsUnavailable;
	}

	@Override
	public MonitorRuleTypeEnum getRuleType() {
		return MonitorRuleTypeEnum.PASSIVE;
	}


}
