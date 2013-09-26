package net.svcret.admin.shared.model;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class BaseDtoServiceCatalogItem extends BaseDtoDashboardObject implements IProvidesUrlCount {

	private static final long serialVersionUID = 1L;

	@XmlElement(name = "config_DisplayInPublicRegistry")
	private Boolean myDisplayInPublicRegistry;

	@XmlElementWrapper(name = "runtime_FailingApplicableRulePids")
	@XmlElement(name = "Element")
	private Set<Long> myFailingApplicableRulePids;

	@XmlElementWrapper(name = "ro_InheritedObscureRequestElementsInLogCache")
	@XmlElement(name = "Element")
	private Set<String> myInheritedObscureRequestElementsInLogCache;

	@XmlElementWrapper(name = "ro_InheritedObscureResponseElementsInLogCache")
	@XmlElement(name = "Element")
	private Set<String> myInheritedObscureResponseElementsInLogCache;

	@XmlElementWrapper(name = "ro_MonitorRulePids")
	@XmlElement(name = "Element")
	private Set<Long> myMonitorRulePids;

	@XmlElementWrapper(name = "config_ObscureRequestElementsInLogCache")
	@XmlElement(name = "Element")
	private Set<String> myObscureRequestElementsInLogCache;

	@XmlElementWrapper(name = "config_ObscureResponseElementsInLogCache")
	@XmlElement(name = "Element")
	private Set<String> myObscureResponseElementsInLogCache;

	@XmlElement(name = "ro_ServerSecured")
	private ServerSecuredEnum myServerSecured;

	@XmlElement(name = "runtime_UrlsActive")
	private Integer myUrlsActive;

	@XmlElement(name = "runtime_UrlsDown")
	private Integer myUrlsDown;

	@XmlElement(name = "runtime_UrlsUnknown")
	private Integer myUrlsUnknown;

	public Boolean getDisplayInPublicRegistry() {
		return myDisplayInPublicRegistry;
	}

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
	public Integer getUrlsActive() {
		return myUrlsActive;
	}

	/**
	 * @return the urlsActive
	 */
	public int getUrlsActiveAsIntWithDefault() {
		return myUrlsActive != null ? myUrlsActive : 0;
	}

	/**
	 * @return the urlsDown
	 */
	public Integer getUrlsDown() {
		return myUrlsDown;
	}

	/**
	 * @return the urlsDown
	 */
	public int getUrlsDownAsIntWithDefault() {
		return myUrlsDown != null ? myUrlsDown : 0;
	}

	/**
	 * @return the urlsUnknown
	 */
	public Integer getUrlsUnknown() {
		return myUrlsUnknown;
	}

	/**
	 * @return the urlsUnknown
	 */
	public int getUrlsUnknownAsIntWithDefault() {
		return myUrlsUnknown != null ? myUrlsUnknown : 0;
	}

	@Override
	public void merge(BaseDtoObject theObject) {
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

	public void setDisplayInPublicRegistry(Boolean theDisplayInPublicRegistry) {
		myDisplayInPublicRegistry = theDisplayInPublicRegistry;
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
	public void setUrlsActive(Integer theUrlsActive) {
		myUrlsActive = theUrlsActive;
	}

	/**
	 * @param theUrlsDown
	 *            the urlsDown to set
	 */
	public void setUrlsDown(Integer theUrlsDown) {
		myUrlsDown = theUrlsDown;
	}

	/**
	 * @param theUrlsUnknown
	 *            the urlsUnknown to set
	 */
	public void setUrlsUnknown(Integer theUrlsUnknown) {
		myUrlsUnknown = theUrlsUnknown;
	}

}
