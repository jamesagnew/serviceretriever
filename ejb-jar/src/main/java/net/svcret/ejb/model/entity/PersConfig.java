package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang3.time.DateUtils;

import com.google.common.annotations.VisibleForTesting;

@Entity
@Table(name = "PX_CONFIG")
public class PersConfig {

	private static final int DEF_STATS_COL_10MIN = 2;
	private static final int DEF_STATS_COL_DAYS = 90;
	private static final int DEF_STATS_COL_HOUR = 48;
	public static final long DEFAULT_ID = 1L;

	@Column(name = "STATS_COL_DAY", nullable = false)
	private int myCollapseStatsToDaysAfterNumDays;

	@Column(name = "STATS_COL_HOUR", nullable = false)
	private int myCollapseStatsToHoursAfterNumHours;

	@Column(name = "STATS_COL_10MIN", nullable = false)
	private int myCollapseStatsToTenMinutesAfterNumHours;

	@Transient
	private transient long myNowForUnitTests;

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	@Id
	@Column(name = "PID")
	private long myPid = DEFAULT_ID;
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "myConfig", fetch = FetchType.EAGER, orphanRemoval=true)
	private Collection<PersConfigProxyUrlBase> myProxyUrlBases;

	public void addProxyUrlBase(PersConfigProxyUrlBase theBase) {
		theBase.setConfig(this);
		getProxyUrlBases();
		myProxyUrlBases.add(theBase);
	}

	/**
	 * @return the collapseStatsToDaysAfterNumDays
	 */
	public int getCollapseStatsToDaysAfterNumDays() {
		if (myCollapseStatsToDaysAfterNumDays <= 0) {
			return DEF_STATS_COL_DAYS;
		}
		return myCollapseStatsToDaysAfterNumDays;
	}

	public Date getCollapseStatsToDaysCutoff() {
		return new Date(getNow() - (getCollapseStatsToDaysAfterNumDays() * DateUtils.MILLIS_PER_DAY));
	}

	/**
	 * @return the collapseStatsToHoursAfterNumHours
	 */
	public int getCollapseStatsToHoursAfterNumHours() {
		if (myCollapseStatsToHoursAfterNumHours <= 0) {
			return DEF_STATS_COL_HOUR;
		}
		return myCollapseStatsToHoursAfterNumHours;
	}

	public Date getCollapseStatsToHoursCutoff() {
		return new Date(getNow() - (getCollapseStatsToHoursAfterNumHours() * DateUtils.MILLIS_PER_HOUR));
	}

	/**
	 * @return the collapseStatsToTenMinutesAfterNumHours
	 */
	public int getCollapseStatsToTenMinutesAfterNumHours() {
		if (myCollapseStatsToTenMinutesAfterNumHours <= 0) {
			return DEF_STATS_COL_10MIN;
		}
		return myCollapseStatsToTenMinutesAfterNumHours;
	}

	public Date getCollapseStatsToTenMinutesCutoff() {
		return new Date(getNow() - (getCollapseStatsToTenMinutesAfterNumHours() * DateUtils.MILLIS_PER_HOUR));
	}

	private long getNow() {
		if (myNowForUnitTests > 0) {
			return myNowForUnitTests;
		}
		return System.currentTimeMillis();
	}

	/**
	 * @return the proxyUrlBases
	 */
	public Collection<PersConfigProxyUrlBase> getProxyUrlBases() {
		if (myProxyUrlBases == null) {
			myProxyUrlBases = new ArrayList<PersConfigProxyUrlBase>();
		}
		return Collections.unmodifiableCollection(myProxyUrlBases);
	}

	public void merge(PersConfig theFromUi) {
		myCollapseStatsToDaysAfterNumDays = theFromUi.getCollapseStatsToDaysAfterNumDays();
		myCollapseStatsToHoursAfterNumHours = theFromUi.getCollapseStatsToDaysAfterNumDays();
		myCollapseStatsToTenMinutesAfterNumHours = theFromUi.getCollapseStatsToTenMinutesAfterNumHours();
		
		// Merge base URLs
		getProxyUrlBases();
		myProxyUrlBases.clear();
		myProxyUrlBases.addAll(theFromUi.getProxyUrlBases());
		
	}

	/**
	 * @param theCollapseStatsToDaysAfterNumDays the collapseStatsToDaysAfterNumDays to set
	 */
	public void setCollapseStatsToDaysAfterNumDays(int theCollapseStatsToDaysAfterNumDays) {
		myCollapseStatsToDaysAfterNumDays = theCollapseStatsToDaysAfterNumDays;
	}
	
	/**
	 * @param theCollapseStatsToHoursAfterNumHours the collapseStatsToHoursAfterNumHours to set
	 */
	public void setCollapseStatsToHoursAfterNumHours(int theCollapseStatsToHoursAfterNumHours) {
		myCollapseStatsToHoursAfterNumHours = theCollapseStatsToHoursAfterNumHours;
	}

	/**
	 * @param theCollapseStatsToTenMinutesAfterNumHours the collapseStatsToTenMinutesAfterNumHours to set
	 */
	public void setCollapseStatsToTenMinutesAfterNumHours(int theCollapseStatsToTenMinutesAfterNumHours) {
		myCollapseStatsToTenMinutesAfterNumHours = theCollapseStatsToTenMinutesAfterNumHours;
	}

	public void setDefaults() {
		addProxyUrlBase(new PersConfigProxyUrlBase("http://localhost:8080/service"));
		myCollapseStatsToTenMinutesAfterNumHours = DEF_STATS_COL_10MIN;
		myCollapseStatsToHoursAfterNumHours = DEF_STATS_COL_HOUR;
		myCollapseStatsToDaysAfterNumDays = DEF_STATS_COL_DAYS;
	}

	@VisibleForTesting
	public void setNow(long theNow) {
		myNowForUnitTests = theNow;
	}
}
