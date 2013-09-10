package net.svcret.ejb.model.entity;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import net.svcret.ejb.util.UrlUtils;
import net.svcret.ejb.util.Validate;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Objects;

@Table(name = "PX_SVC_VER_URL", uniqueConstraints = { @UniqueConstraint(name = "PX_URL_CONS_URL", columnNames = { "SVC_VERSION_PID", "URL" }), // -
		@UniqueConstraint(name = "PX_URL_CONS_URLID", columnNames = { "SVC_VERSION_PID", "URL_ID" }), // -
		// @UniqueConstraint(name = "PX_URL_CONS_ORDER", columnNames = {
		// "SVC_VERSION_PID", "URL_ORDER" }), // -
})
@Entity
public class PersServiceVersionUrl extends BasePersObject implements Comparable<PersServiceVersionUrl> {

	public static final int MAX_URL_LENGTH = 300;
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(PersServiceVersionUrl.class);
	private static final long serialVersionUID = 1L;

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	@Column(name = "URL_ORDER", nullable = false)
	private int myOrder;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@ManyToOne()
	@JoinColumn(name = "SVC_VERSION_PID", referencedColumnName = "PID", nullable = false)
	private BasePersServiceVersion myServiceVersion;

	@OneToOne(cascade = {}, fetch = FetchType.LAZY, mappedBy = "myUrl", orphanRemoval = true)
	private PersServiceVersionUrlStatus myStatus;

	@OneToMany(fetch = FetchType.LAZY, cascade = {}, orphanRemoval = true, mappedBy = "myImplementationUrl")
	private List<PersMonitorRuleActiveCheckOutcome> myMonitorRuleActiveCheckOutcomes;

	@Column(name = "URL", length = MAX_URL_LENGTH, nullable = false)
	private String myUrl;

	@Column(name = "URL_ID", length = 100, nullable = false)
	private String myUrlId;

	@OneToMany(cascade = {}, mappedBy = "myImplementationUrl", orphanRemoval = true)
	private Collection<PersMonitorRuleActiveCheckOutcome> myMonitorActiveCheckOutcomes;

	@OneToMany(cascade = {}, mappedBy = "myImplementationUrl", orphanRemoval = true)
	private Collection<PersServiceVersionRecentMessage> myServiceVersionRecentMessages;

	@OneToMany(cascade = {}, mappedBy = "myImplementationUrl", orphanRemoval = true)
	private Collection<PersUserRecentMessage> myUserRecentMessages;

	@Transient
	private transient boolean myUrlIsLocal;

	// @Column(name="ENV_ID", length=20)
	// @Enumerated(EnumType.STRING)
	// private PersEnvironment

	@Transient
	private transient boolean myUrlIsValid;

	public PersServiceVersionUrl() {
		super();
	}

	public PersServiceVersionUrl(long thePid, String theUrl) {
		myPid = thePid;
		myUrl = theUrl;
	}

	public PersServiceVersionUrl(Long thePid, String theUrlId, String theUrl) {
		myPid = thePid;
		myUrlId=theUrlId;
		myUrl = theUrl;
	}

	@Override
	public int compareTo(PersServiceVersionUrl theUrl) {
		return myUrlId.compareTo(theUrl.getUrlId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		return theObj instanceof PersServiceVersionUrl && Objects.equal(myPid, ((PersServiceVersionUrl) theObj).myPid);
	}

	/**
	 * @return the versionNum
	 */
	public int getOptLock() {
		return myOptLock;
	}

	/**
	 * @return the order
	 */
	public int getOrder() {
		return myOrder;
	}

	/**
	 * @return the id
	 */
	public Long getPid() {
		return myPid;
	}

	/**
	 * @return the service
	 */
	public BasePersServiceVersion getServiceVersion() {
		return myServiceVersion;
	}

	/**
	 * @return the stats
	 */
	public PersServiceVersionUrlStatus getStatus() {
		return myStatus;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return StringUtils.defaultString(myUrl);
	}

	/**
	 * @return the urlId
	 */
	public String getUrlId() {
		return myUrlId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(myPid);
	}

	@PostLoad
	public void initUrlInternal() {
		try {
			URL url = UrlUtils.toUrl(getUrl());
			InetAddress addr = UrlUtils.toAddress(url);
			myUrlIsValid = true;
			myUrlIsLocal = UrlUtils.isLocal(addr);
		} catch (MalformedURLException e) {
			ourLog.warn("Failed to parse URL[{}]: {}", getUrlId(), getUrl());
			myUrlIsLocal = false;
			myUrlIsValid = false;
		} catch (UnknownHostException e) {
			ourLog.warn("URL[{}] refers to an unknown host: {}", getUrlId(), getUrl());
			myUrlIsLocal = false;
			myUrlIsValid = false;
		}

	}

	public boolean isLocal() {
		return myUrlIsLocal;
	}

	public boolean isValid() {
		return myUrlIsValid;
	}

	public void loadAllAssociations() {
		// nothing
	}

	/**
	 * @param theOptLock
	 *            the versionNum to set
	 */
	public void setOptLock(int theOptLock) {
		myOptLock = theOptLock;
	}

	/**
	 * @param theOrder
	 *            the order to set
	 */
	public void setOrder(int theOrder) {
		myOrder = theOrder;
	}

	/**
	 * @param thePid
	 *            the id to set
	 */
	public void setPid(Long thePid) {
		myPid = thePid;
	}

	/**
	 * @param theServiceVersion
	 *            the serviceVersion to set
	 */
	public void setServiceVersion(BasePersServiceVersion theServiceVersion) {
		Validate.notNull(theServiceVersion, "ServiceVersion");

		if (myServiceVersion != null && !myServiceVersion.equals(theServiceVersion)) {
			throw new IllegalStateException("Can't reassign URL to another version");
		}
		// else if (myServiceVersion == null ||
		// !myServiceVersion.getUrls().contains(this)) {
		// theServiceVersion.addUrl(this);
		// }

		myServiceVersion = theServiceVersion;
	}

	/**
	 * @param theStats
	 *            the stats to set
	 */
	public void setStatus(PersServiceVersionUrlStatus theStats) {
		if (myStatus == theStats) {
			return;
		}
		myStatus = theStats;
		theStats.setUrl(this);
	}

	/**
	 * @param theUrl
	 *            the url to set
	 */
	public void setUrl(String theUrl) {
		Validate.notNull(theUrl, "Url");
		myUrl = theUrl;
		initUrlInternal();
	}

	/**
	 * @param theUrlId
	 *            the urlId to set
	 */
	public void setUrlId(String theUrlId) {
		myUrlId = theUrlId;
	}

	public void merge(PersServiceVersionUrl theObj) {
		setUrl(theObj.getUrl());
		setUrlId(theObj.getUrlId());
	}

}
