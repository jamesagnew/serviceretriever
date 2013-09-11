package net.svcret.ejb.model.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class PersStickySessionUrlBindingPk implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne()
	@JoinColumn(name = "SVC_VER_PID", referencedColumnName = "PID", nullable = false)
	private BasePersServiceVersion myServiceVersion;
	
	@Column(name="SESSION_ID", length=1000)
	private String mySessionId;

	public PersStickySessionUrlBindingPk() {
	}

	public PersStickySessionUrlBindingPk(String theSessionId, BasePersServiceVersion theServiceVersion) {
		super();
		mySessionId = theSessionId;
		myServiceVersion = theServiceVersion;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PersStickySessionUrlBindingPk other = (PersStickySessionUrlBindingPk) obj;
		if (myServiceVersion == null) {
			if (other.myServiceVersion != null)
				return false;
		} else if (!myServiceVersion.equals(other.myServiceVersion))
			return false;
		if (mySessionId == null) {
			if (other.mySessionId != null)
				return false;
		} else if (!mySessionId.equals(other.mySessionId))
			return false;
		return true;
	}

	public BasePersServiceVersion getServiceVersion() {
		return myServiceVersion;
	}

	public String getSessionId() {
		return mySessionId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((myServiceVersion == null) ? 0 : myServiceVersion.hashCode());
		result = prime * result + ((mySessionId == null) ? 0 : mySessionId.hashCode());
		return result;
	}

	public void setServiceVersion(BasePersServiceVersion theServiceVersion) {
		myServiceVersion = theServiceVersion;
	}

	public void setSessionId(String theSessionId) {
		mySessionId = theSessionId;
	}

}
