package net.svcret.core.model.entity;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import net.svcret.admin.shared.util.Validate;

@Entity
@Table(name = "PX_LIB_MSG_APPLIESTO")
public class PersLibraryMessageAppliesTo implements Serializable {

	private static final long serialVersionUID = 1L;

	@EmbeddedId()
	private PersLibraryMessageAppliesToPk myPk;

	public PersLibraryMessageAppliesTo() {
		// nothing
	}

	public PersLibraryMessageAppliesTo(PersLibraryMessage theMessage, BasePersServiceVersion theSvcVer) {
		Validate.notNull(theMessage);
		Validate.notNull(theSvcVer);

		myPk = new PersLibraryMessageAppliesToPk();
		myPk.setMessage(theMessage);
		myPk.setServiceVersion(theSvcVer);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PersLibraryMessageAppliesTo)) {
			return false;
		}
		PersLibraryMessageAppliesTo other = (PersLibraryMessageAppliesTo) obj;
		if (myPk == null) {
			if (other.myPk != null) {
				return false;
			}
		} else if (!myPk.equals(other.myPk)) {
			return false;
		}
		return true;
	}

	public PersLibraryMessageAppliesToPk getPk() {
		return myPk;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((myPk == null) ? 0 : myPk.hashCode());
		return result;
	}

	public void setPk(PersLibraryMessageAppliesToPk thePk) {
		myPk = thePk;
	}

}
