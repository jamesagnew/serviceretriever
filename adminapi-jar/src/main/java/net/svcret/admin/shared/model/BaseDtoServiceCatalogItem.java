package net.svcret.admin.shared.model;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseDtoServiceCatalogItem extends BaseGDashboardObject implements IProvidesUrlCount {

	private static final long serialVersionUID = 1L;

	private Set<Long> myFailingApplicableRulePids;
	private Set<String> myInheritedObscureRequestElementsInLogCache;
	private Set<String> myInheritedObscureResponseElementsInLogCache;
	private Set<Long> myMonitorRulePids;
	private Set<String> myObscureRequestElementsInLogCache;
	private Set<String> myObscureResponseElementsInLogCache;
	private ServerSecuredEnum myServerSecured;
	private int myUrlsActive;
	private int myUrlsDown;
	private int myUrlsUnknown;

	public Set<Long> getFailingApplicableRulePids() {
		if (myFailingApplicableRulePids == null) {
			myFailingApplicableRulePids = new HashSet<Long>();
		}
		return myFailingApplicableRulePids;
	}

	public Set<String> getInheritedObscureRequestElementsInLogCache() {
		return myInheritedObscureRequestElementsInLogCache;
	}

	public Set<String> getInheritedObscureResponseElementsInLogCache() {
		return myInheritedObscureResponseElementsInLogCache;
	}

	public Set<Long> getMonitorRulePids() {
		if (myMonitorRulePids == null) {
			myMonitorRulePids = new HashSet<Long>();
		}
		return myMonitorRulePids;
	}

	public Set<String> getObscureRequestElementsInLogCache() {
		return myObscureRequestElementsInLogCache;
	}

	public Set<String> getObscureResponseElementsInLogCache() {
		return myObscureResponseElementsInLogCache;
	}

	/**
	 * @return the serverSecured
	 */
	public ServerSecuredEnum getServerSecured() {
		return myServerSecured;
	}

	/**
	 * @return the urlsActive
	 */
	public int getUrlsActive() {
		return myUrlsActive;
	}

	/**
	 * @return the urlsDown
	 */
	public int getUrlsDown() {
		return myUrlsDown;
	}

	/**
	 * @return the urlsUnknown
	 */
	public int getUrlsUnknown() {
		return myUrlsUnknown;
	}

	@Override
	public void merge(BaseGObject theObject) {
		super.merge(theObject);

		BaseDtoServiceCatalogItem obj = (BaseDtoServiceCatalogItem) theObject;
		myServerSecured = obj.getServerSecured();

		getMonitorRulePids().clear();
		getMonitorRulePids().addAll(obj.getMonitorRulePids());

		setInheritedObscureRequestElementsInLogCache(obj.getInheritedObscureRequestElementsInLogCache());
		setInheritedObscureResponseElementsInLogCache(obj.getInheritedObscureResponseElementsInLogCache());
		setObscureRequestElementsInLogCache(obj.getObscureRequestElementsInLogCache());
		setObscureResponseElementsInLogCache(obj.getObscureResponseElementsInLogCache());
		
		if (obj.isStatsInitialized()) {
			getFailingApplicableRulePids().clear();
			getFailingApplicableRulePids().addAll(obj.getFailingApplicableRulePids());
			setUrlsActive(obj.getUrlsActive());
			setUrlsDown(obj.getUrlsDown());
			setUrlsUnknown(obj.getUrlsUnknown());
		}
	}

	public void setInheritedObscureRequestElementsInLogCache(Set<String> theInheritedObscureRequestElementsInLogCache) {
		myInheritedObscureRequestElementsInLogCache = theInheritedObscureRequestElementsInLogCache;
	}

	public void setInheritedObscureResponseElementsInLogCache(Set<String> theInheritedObscureResponseElementsInLogCache) {
		myInheritedObscureResponseElementsInLogCache = theInheritedObscureResponseElementsInLogCache;
	}

	public void setObscureRequestElementsInLogCache(Set<String> theObscureRequestElementsInLogCache) {
		myObscureRequestElementsInLogCache = theObscureRequestElementsInLogCache;
	}

	public void setObscureResponseElementsInLogCache(Set<String> theObscureResponseElementsInLogCache) {
		myObscureResponseElementsInLogCache = theObscureResponseElementsInLogCache;
	}

	/**
	 * @param theServerSecured
	 *            the serverSecured to set
	 */
	public void setServerSecured(ServerSecuredEnum theServerSecured) {
		myServerSecured = theServerSecured;
	}

	/**
	 * @param theUrlsActive
	 *            the urlsActive to set
	 */
	public void setUrlsActive(int theUrlsActive) {
		myUrlsActive = theUrlsActive;
	}

	/**
	 * @param theUrlsDown
	 *            the urlsDown to set
	 */
	public void setUrlsDown(int theUrlsDown) {
		myUrlsDown = theUrlsDown;
	}

	/**
	 * @param theUrlsUnknown
	 *            the urlsUnknown to set
	 */
	public void setUrlsUnknown(int theUrlsUnknown) {
		myUrlsUnknown = theUrlsUnknown;
	}

}
