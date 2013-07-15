package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "PX_MONITOR_RULE")
@NamedQueries(value = { @NamedQuery(name = Queries.MONITORRULE_FINDALL, query = Queries.MONITORRULE_FINDALL_Q) })
public class PersMonitorRule extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "myRule")
	private Collection<PersMonitorAppliesTo> myAppliesTo;

	@Column(name = "FIRE_FOR_LATENCY_MILLIS", nullable = true)
	private Integer myFireForBackingServiceLatencyIsAboveMillis;

	@Column(name = "FIRE_FOR_LATENCY_SUS_MINS", nullable = true)
	private Integer myFireForBackingServiceLatencySustainTimeMins;

	@Column(name = "FIRE_FOR_ALL_URLS")
	private boolean myFireIfAllBackingUrlsAreUnavailable;

	@Column(name = "FIRE_FOR_SINGLE_URL")
	private boolean myFireIfSingleBackingUrlIsUnavailable;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "myRule")
	private Collection<PersMonitorRuleNotifyContact> myNotifyContact;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Column(name = "RULE_ACTIVE", nullable=false)
	private boolean myRuleActive;

	@Column(name = "RULE_NAME", length = 200, nullable=true)
	private String myRuleName;

	public Collection<PersMonitorAppliesTo> getAppliesTo() {
		if (myAppliesTo == null) {
			myAppliesTo = new ArrayList<PersMonitorAppliesTo>();
		}
		return Collections.unmodifiableCollection(myAppliesTo);
	}

	/**
	 * @return the fireForBackingServiceLatencyIsAboveMillis
	 */
	public Integer getFireForBackingServiceLatencyIsAboveMillis() {
		return myFireForBackingServiceLatencyIsAboveMillis;
	}

	/**
	 * @return the fireForBackingServiceLatencySustainTimeMins
	 */
	public Integer getFireForBackingServiceLatencySustainTimeMins() {
		return myFireForBackingServiceLatencySustainTimeMins;
	}

	public Collection<PersMonitorRuleNotifyContact> getNotifyContact() {
		if (myNotifyContact == null) {
			myNotifyContact = new ArrayList<PersMonitorRuleNotifyContact>();
		}
		return myNotifyContact;
	}

	@Override
	public Long getPid() {
		return myPid;
	}

	public String getRuleName() {
		return myRuleName;
	}

	public boolean isFireIfAllBackingUrlsAreUnavailable() {
		return myFireIfAllBackingUrlsAreUnavailable;
	}

	public boolean isFireIfSingleBackingUrlIsUnavailable() {
		return myFireIfSingleBackingUrlIsUnavailable;
	}

	public boolean isRuleActive() {
		return myRuleActive;
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

	/**
	 * @param theFireForBackingServiceLatencyIsAboveMillis
	 *            the fireForBackingServiceLatencyIsAboveMillis to set
	 */
	public void setFireForBackingServiceLatencyIsAboveMillis(Integer theFireForBackingServiceLatencyIsAboveMillis) {
		if (theFireForBackingServiceLatencyIsAboveMillis != null) {
			if (theFireForBackingServiceLatencyIsAboveMillis < 1) {
				throw new IllegalArgumentException("Invalid value: " + theFireForBackingServiceLatencyIsAboveMillis);
			}
		}
		myFireForBackingServiceLatencyIsAboveMillis = theFireForBackingServiceLatencyIsAboveMillis;
	}

	/**
	 * @param theFireForBackingServiceLatencySustainTimeMins
	 *            the fireForBackingServiceLatencySustainTimeMins to set
	 */
	public void setFireForBackingServiceLatencySustainTimeMins(Integer theFireForBackingServiceLatencySustainTimeMins) {
		myFireForBackingServiceLatencySustainTimeMins = theFireForBackingServiceLatencySustainTimeMins;
	}

	public void setFireIfAllBackingUrlsAreUnavailable(boolean theFireIfAllBackingUrlsAreUnavailable) {
		myFireIfAllBackingUrlsAreUnavailable = theFireIfAllBackingUrlsAreUnavailable;
	}

	public void setFireIfSingleBackingUrlIsUnavailable(boolean theFireIfSingleBackingUrlIsUnavailable) {
		myFireIfSingleBackingUrlIsUnavailable = theFireIfSingleBackingUrlIsUnavailable;
	}

	public void setRuleActive(boolean theRuleActive) {
		myRuleActive = theRuleActive;
	}

	public void setRuleName(String theRuleName) {
		myRuleName = theRuleName;
	}

	public Set<BasePersServiceVersion> toAppliesToServiceVersions() {
		HashSet<BasePersServiceVersion> retVal = new HashSet<BasePersServiceVersion>();

		for (PersMonitorAppliesTo next : getAppliesTo()) {
			retVal.addAll(next.getItem().getAllServiceVersions());
		}

		return retVal;
	}

}
