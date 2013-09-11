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

import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;

@Entity
@Table(name = "PX_STICKY_SESSION_URL")
public class PersStickySessionUrlBinding implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "LAST_ACCESS")
	@Temporal(TemporalType.TIMESTAMP)
	private Date myLastAccessed;

	@Version()
	@Column(name = "OPTLOCK")
	private long myOptLock;

	@EmbeddedId
	private PersStickySessionUrlBindingPk myPk;

	@ManyToOne()
	@JoinColumn(name = "URL_PID", referencedColumnName = "PID", nullable = false)
	private PersServiceVersionUrl myUrl;

	public PersStickySessionUrlBinding() {
	}

	public PersStickySessionUrlBinding(String theSessionId, PersServiceVersionSoap11 theSvcVer, PersServiceVersionUrl theUrl) {
		this(new PersStickySessionUrlBindingPk(theSessionId, theSvcVer),theUrl);
	}

	public PersStickySessionUrlBinding(PersStickySessionUrlBindingPk theBindingPk, PersServiceVersionUrl theUrl) {
		myPk = theBindingPk;
		myUrl = theUrl;
		myLastAccessed = new Date();
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

	public void setLastAccessed(Date theLastAccessed) {
		myLastAccessed = theLastAccessed;
	}

	public void setPk(PersStickySessionUrlBindingPk thePk) {
		myPk = thePk;
	}

	public void setUrl(PersServiceVersionUrl theUrl) {
		myUrl = theUrl;
	}

}
