package net.svcret.admin.shared.model;

import java.util.HashSet;
import java.util.Set;

public class GMonitorRule extends BaseGObject<GMonitorRule> {

	private static final long serialVersionUID = 1L;

	private Integer myFireForBackingServiceLatencyIsAboveMillis;
	private Integer myFireForBackingServiceLatencySustainTimeMins;
	private boolean myFireIfAllBackingUrlsAreUnavailable;
	private boolean myFireIfSingleBackingUrlIsUnavailable;
	private String myName;

	private Set<String> myNotifyEmailContacts;

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

	public String getName() {
		return myName;
	}

	/**
	 * @return the notifyEmailContacts
	 */
	public Set<String> getNotifyEmailContacts() {
		if (myNotifyEmailContacts == null) {
			myNotifyEmailContacts = new HashSet<String>();
		}
		return myNotifyEmailContacts;
	}

	/**
	 * @return the fireIfAllBackingUrlsAreUnavailable
	 */
	public boolean isFireIfAllBackingUrlsAreUnavailable() {
		return myFireIfAllBackingUrlsAreUnavailable;
	}

	/**
	 * @return the fireIfSingleBackingUrlIsUnavailable
	 */
	public boolean isFireIfSingleBackingUrlIsUnavailable() {
		return myFireIfSingleBackingUrlIsUnavailable;
	}

	@Override
	public void merge(GMonitorRule theObject) {

	}

	/**
	 * @param theFireForBackingServiceLatencyIsAboveMillis
	 *            the fireForBackingServiceLatencyIsAboveMillis to set
	 */
	public void setFireForBackingServiceLatencyIsAboveMillis(Integer theFireForBackingServiceLatencyIsAboveMillis) {
		myFireForBackingServiceLatencyIsAboveMillis = theFireForBackingServiceLatencyIsAboveMillis;
	}

	/**
	 * @param theFireForBackingServiceLatencySustainTimeMins
	 *            the fireForBackingServiceLatencySustainTimeMins to set
	 */
	public void setFireForBackingServiceLatencySustainTimeMins(Integer theFireForBackingServiceLatencySustainTimeMins) {
		myFireForBackingServiceLatencySustainTimeMins = theFireForBackingServiceLatencySustainTimeMins;
	}

	/**
	 * @param theFireIfAllBackingUrlsAreUnavailable
	 *            the fireIfAllBackingUrlsAreUnavailable to set
	 */
	public void setFireIfAllBackingUrlsAreUnavailable(boolean theFireIfAllBackingUrlsAreUnavailable) {
		myFireIfAllBackingUrlsAreUnavailable = theFireIfAllBackingUrlsAreUnavailable;
	}

	/**
	 * @param theFireIfSingleBackingUrlIsUnavailable
	 *            the fireIfSingleBackingUrlIsUnavailable to set
	 */
	public void setFireIfSingleBackingUrlIsUnavailable(boolean theFireIfSingleBackingUrlIsUnavailable) {
		myFireIfSingleBackingUrlIsUnavailable = theFireIfSingleBackingUrlIsUnavailable;
	}

	/**
	 * @param theName the name to set
	 */
	public void setName(String theName) {
		myName = theName;
	}

}
