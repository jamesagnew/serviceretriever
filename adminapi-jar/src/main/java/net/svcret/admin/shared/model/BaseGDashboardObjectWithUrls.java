package net.svcret.admin.shared.model;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseGDashboardObjectWithUrls<T> extends BaseGDashboardObject<T> implements IProvidesUrlCount {

	private static final long serialVersionUID = 1L;

	private int myFailingRuleCount;
	private List<Long> myMonitorRulePids;
	private ServerSecuredEnum myServerSecured;
	private int myUrlsActive;
	private int myUrlsDown;
	private int myUrlsUnknown;

	public int getFailingRuleCount() {
		return myFailingRuleCount;
	}

	public List<Long> getMonitorRulePids() {
		if (myMonitorRulePids == null) {
			myMonitorRulePids = new ArrayList<Long>();
		}
		return myMonitorRulePids;
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
	public void merge(BaseGDashboardObject<T> theObject) {
		super.merge((BaseGDashboardObject<T>) theObject);

		BaseGDashboardObjectWithUrls<T> obj = (BaseGDashboardObjectWithUrls<T>) theObject;
		myServerSecured = obj.getServerSecured();

		if (theObject.isStatsInitialized()) {
			setUrlsActive(obj.getUrlsActive());
			setUrlsDown(obj.getUrlsDown());
			setUrlsUnknown(obj.getUrlsUnknown());
		}
	}

	public void setFailingRuleCount(int theFailingRuleCount) {
		myFailingRuleCount = theFailingRuleCount;
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
