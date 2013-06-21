package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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

	@Column(name = "RULE_ACTIVE")
	private boolean myRuleActive;

	@Column(name = "RULE_NAME", length = 200)
	private String myRuleName;

	public Collection<PersMonitorAppliesTo> getAppliesTo() {
		if (myAppliesTo == null) {
			myAppliesTo = new ArrayList<PersMonitorAppliesTo>();
		}
		return myAppliesTo;
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

}
