package net.svcret.ejb.model.entity;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

@Table(name = "PX_SVC_VER_URL", uniqueConstraints = { 
		@UniqueConstraint(name="PX_URL_CONS_URL", columnNames = { "SVC_VERSION_PID", "URL" }), //-
		@UniqueConstraint(name="PX_URL_CONS_URLID", columnNames = { "SVC_VERSION_PID", "URL_ID" }) //-
})
@Entity
public class PersServiceVersionUrl extends BasePersObject implements Comparable<PersServiceVersionUrl> {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(PersServiceVersionUrl.class);

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
	@JoinColumn(name = "SVC_VERSION_PID", referencedColumnName = "PID", nullable = false)
	private BasePersServiceVersion myServiceVersion;

	@OneToOne(cascade = {}, fetch = FetchType.LAZY, mappedBy = "myUrl", orphanRemoval = true)
	private PersServiceVersionUrlStatus myStatus;

	@Column(name = "URL", length = 200, nullable = false)
	private String myUrl;

//	@Column(name="ENV_ID", length=20)
//	@Enumerated(EnumType.STRING)
//	private PersEnvironment
	
	@Column(name = "URL_ID", length = 100, nullable = false)
	private String myUrlId;

	@Transient
	private transient boolean myUrlIsLocal;

	@Transient
	private transient boolean myUrlIsValid;

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
		Validate.throwIllegalArgumentExceptionIfNull("ServiceVersion", theServiceVersion);
		
		if (myServiceVersion != null && !myServiceVersion.equals(theServiceVersion)) {
			throw new IllegalStateException("Can't reassign URL to another version");
		} else if (myServiceVersion == null || !myServiceVersion.getUrls().contains(this)) {
			theServiceVersion.addUrl(this);
		}
		
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
		Validate.throwIllegalArgumentExceptionIfNull("Url", theUrl);
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

}
