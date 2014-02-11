package net.svcret.core.model.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ForeignKey;

@Embeddable
public class PersLibraryMessageAppliesToPk implements Serializable {

	private static final long serialVersionUID = 1L;

	@JoinColumn(name="MESSAGE_PID")
	@ManyToOne(optional=false, cascade= {CascadeType.PERSIST})
	@ForeignKey(name="FK_PERS_LIBMSG_MSGPID")
	private PersLibraryMessage myMessage;

	@JoinColumn(name="SVCVER_PID")
	@ManyToOne(optional=false, cascade= {})
	@ForeignKey(name="FK_PERS_LIBMSG_SVCVERPID")
	private BasePersServiceVersion myServiceVersion;

	@Override
	public String toString() {
		return "LibraryMessage[msg=" + myMessage.getPid() + ", svcVer=" + myServiceVersion.getVersionId()+"]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PersLibraryMessageAppliesToPk)) {
			return false;
		}
		PersLibraryMessageAppliesToPk other = (PersLibraryMessageAppliesToPk) obj;
		if (myMessage == null) {
			if (other.myMessage != null) {
				return false;
			}
		} else if (!myMessage.equals(other.myMessage)) {
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

	public PersLibraryMessage getMessage() {
		return myMessage;
	}

	public BasePersServiceVersion getServiceVersion() {
		return myServiceVersion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((myMessage == null) ? 0 : myMessage.hashCode());
		result = prime * result + ((myServiceVersion == null) ? 0 : myServiceVersion.hashCode());
		return result;
	}

	public void setMessage(PersLibraryMessage theMessage) {
		myMessage = theMessage;
	}

	public void setServiceVersion(BasePersServiceVersion theServiceVersion) {
		myServiceVersion = theServiceVersion;
	}

}
