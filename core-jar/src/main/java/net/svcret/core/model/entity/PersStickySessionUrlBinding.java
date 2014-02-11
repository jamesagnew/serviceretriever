package net.svcret.core.model.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

import net.svcret.admin.shared.model.DtoStickySessionUrlBinding;
import net.svcret.core.model.entity.soap.PersServiceVersionSoap11;

import org.apache.commons.lang3.builder.ToStringBuilder;

//@formatter:off
@Entity
@Table(name = "PX_STICKY_SESSION_URL")
@NamedQueries(value= {
		@NamedQuery(name=Queries.SSURL_FINDALL, query=Queries.SSURL_FINDALL_Q)
})
//@org.hibernate.annotations.Table(appliesTo="PX_STICKY_SESSION_URL", indexes= {
//		@Index(name="IDX_SSU_TIMESTAMP", columnNames= {"LAST_ACCESS"})
//})
//@formatter:on
public class PersStickySessionUrlBinding implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "BINDING_CREATED")
	@Temporal(TemporalType.TIMESTAMP)
	private volatile Date myCreated;

	@Column(name = "LAST_ACCESS",nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private volatile Date myLastAccessed;

	@Transient
	private transient boolean myNewlyCreated;

	@Version()
	@Column(name = "OPTLOCK")
	private long myOptLock;

	@EmbeddedId
	private PersStickySessionUrlBindingPk myPk;

	@Column(name = "REQ_IP", length=BasePersSavedTransactionRecentMessage.MAX_REQ_IP_LEN)
	private volatile String myRequestingIp;

	@ManyToOne()
	@JoinColumn(name = "URL_PID", referencedColumnName = "PID", nullable = false)
	private volatile PersServiceVersionUrl myUrl;

	public PersStickySessionUrlBinding() {
	}

	public PersStickySessionUrlBinding(PersStickySessionUrlBindingPk theBindingPk, PersServiceVersionUrl theUrl) {
		myPk = theBindingPk;
		myUrl = theUrl;
		myLastAccessed = new Date();
	}

	public PersStickySessionUrlBinding(String theSessionId, PersServiceVersionSoap11 theSvcVer, PersServiceVersionUrl theUrl) {
		this(new PersStickySessionUrlBindingPk(theSessionId, theSvcVer), theUrl);
	}

	public Date getCreated() {
		return myCreated;
	}

	public Date getLastAccessed() {
		return myLastAccessed;
	}

	public PersStickySessionUrlBindingPk getPk() {
		return myPk;
	}

	public String getRequestingIp() {
		return myRequestingIp;
	}

	public PersServiceVersionUrl getUrl() {
		return myUrl;
	}

	public boolean isNewlyCreated() {
		return myNewlyCreated;
	}

	public void setCreated(Date theCreated) {
		myCreated = theCreated;
	}

	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.append("svcVer", myPk.getServiceVersion().getPid());
		b.append("sessionId",myPk.getSessionId());
		b.append("url", getUrl().getPid());
		return b.build();
	}
	
	public void setLastAccessed(Date theLastAccessed) {
		myLastAccessed = theLastAccessed;
	}

	public void setNewlyCreated(boolean theNewlyCreated) {
		myNewlyCreated = theNewlyCreated;
	}

	public void setPk(PersStickySessionUrlBindingPk thePk) {
		myPk = thePk;
	}

	public void setRequestingIp(String theRequestingIp) {
		myRequestingIp = theRequestingIp;
	}

	public void setUrl(PersServiceVersionUrl theUrl) {
		myUrl = theUrl;
	}

	public DtoStickySessionUrlBinding toDao() {
		DtoStickySessionUrlBinding retVal = new DtoStickySessionUrlBinding();
		retVal.setSessionId(getPk().getSessionId());
		retVal.setServiceVersionPid(getPk().getServiceVersion().getPid());
		retVal.setUrlPid(getUrl().getPid());
		retVal.setUrlId(getUrl().getUrlId());
		retVal.setUrlHref(getUrl().getUrl());
		retVal.setRequestingIp(getRequestingIp());
		retVal.setCreated(getCreated());
		retVal.setLastAccessed(getLastAccessed());
		return retVal;
	}

}
