package net.svcret.core.model.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ForeignKey;

@Embeddable
public class PersPropertyCapturePk implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "PROP_NAME", length = 100)
	private String myPropertyName;

	@JoinColumn(name = "SVCVER_PID")
	@ManyToOne(optional = false, cascade = {})
	@ForeignKey(name = "FK_PERS_PROPCAP_SVCVERPID")
	private BasePersServiceVersion myServiceVersion;

	public PersPropertyCapturePk() {
	}

	public PersPropertyCapturePk(BasePersServiceVersion theSvcVer, String thePropertyName) {
		myServiceVersion = theSvcVer;
		myPropertyName = thePropertyName;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PersPropertyCapturePk)) {
			return false;
		}
		PersPropertyCapturePk other = (PersPropertyCapturePk) obj;
		if (myPropertyName == null) {
			if (other.myPropertyName != null) {
				return false;
			}
		} else if (!myPropertyName.equals(other.myPropertyName)) {
			return false;
		}
		if (myServiceVersion == null) {
			if (other.myServiceVersion != null) {
				return false;
			}
		} else if (!myServiceVersion.equals(other.myServiceVersion)) {
			return false;
		}
		return true;
	}

	public String getPropertyName() {
		return myPropertyName;
	}

	public BasePersServiceVersion getServiceVersion() {
		return myServiceVersion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((myPropertyName == null) ? 0 : myPropertyName.hashCode());
		result = prime * result + ((myServiceVersion == null) ? 0 : myServiceVersion.hashCode());
		return result;
	}

	public void setPropertyName(String thePropertyName) {
		myPropertyName = thePropertyName;
	}

	public void setServiceVersion(BasePersServiceVersion theServiceVersion) {
		myServiceVersion = theServiceVersion;
	}

	@Override
	public String toString() {
		return "PropertyCapture[msg=" + myPropertyName + ", svcVer=" + myServiceVersion.getVersionId() + "]";
	}

}
