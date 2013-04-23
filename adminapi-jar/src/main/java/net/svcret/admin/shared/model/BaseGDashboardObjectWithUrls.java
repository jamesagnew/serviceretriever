package net.svcret.admin.shared.model;

public abstract class BaseGDashboardObjectWithUrls<T> extends BaseGDashboardObject<T>  implements IProvidesUrlCount{

	private static final long serialVersionUID = 1L;
	
	private int myUrlsActive;
	private int myUrlsDown;
	private int myUrlsUnknown;
	private ServerSecuredEnum myServerSecured;
	
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

	/**
	 * @param theServerSecured
	 *            the serverSecured to set
	 */
	public void setServerSecured(ServerSecuredEnum theServerSecured) {
		myServerSecured = theServerSecured;
	}

	@Override
	public void merge(BaseGDashboardObject<T> theObject) {
		super.merge((BaseGDashboardObject<T>)theObject);

		BaseGDashboardObjectWithUrls<T> obj = (BaseGDashboardObjectWithUrls<T>)theObject;
		myServerSecured = obj.getServerSecured();

		if (theObject.isStatsInitialized()) {
			setUrlsActive(obj.getUrlsActive());
			setUrlsDown(obj.getUrlsDown());
			setUrlsUnknown(obj.getUrlsUnknown());
		}
	}

	/**
	 * @param theUrlsActive the urlsActive to set
	 */
	public void setUrlsActive(int theUrlsActive) {
		myUrlsActive = theUrlsActive;
	}

	/**
	 * @param theUrlsDown the urlsDown to set
	 */
	public void setUrlsDown(int theUrlsDown) {
		myUrlsDown = theUrlsDown;
	}

	/**
	 * @param theUrlsUnknown the urlsUnknown to set
	 */
	public void setUrlsUnknown(int theUrlsUnknown) {
		myUrlsUnknown = theUrlsUnknown;
	}

}
