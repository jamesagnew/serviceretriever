package net.svcret.ejb.model.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import net.svcret.admin.shared.model.DtoStickySessionUrlBinding;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;

@Entity
@Table(name = "PX_STICKY_SESSION_URL")
public class PersStickySessionUrlBinding implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "BINDING_CREATED")
	@Temporal(TemporalType.TIMESTAMP)
	private volatile Date myCreated;

	@Column(name = "LAST_ACCESS")
	@Temporal(TemporalType.TIMESTAMP)
	private volatile Date myLastAccessed;

	private boolean myNewlyCreated;

	@Version()
	@Column(name = "OPTLOCK")
	private long myOptLock;

	@EmbeddedId
	private PersStickySessionUrlBindingPk myPk;

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

	public PersServiceVersionUrl getUrl() {
		return myUrl;
	}

	public boolean isNewlyCreated() {
		return myNewlyCreated;
	}

	public void setCreated(Date theCreated) {
		myCreated = theCreated;
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

	public void setUrl(PersServiceVersionUrl theUrl) {
		myUrl = theUrl;
	}

	public DtoStickySessionUrlBinding toDao() {
		DtoStickySessionUrlBinding retVal = new DtoStickySessionUrlBinding();
		retVal.setSessionId(getPk().getSessionId());
		retVal.setServiceVersionPid(getPk().getServiceVersion().getPid());
		retVal.setUrlPid(getUrl().getPid());
		return retVal;
	}

}
