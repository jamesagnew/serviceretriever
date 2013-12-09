package net.svcret.admin.shared.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class BaseDtoServerSecurity extends BaseDtoObject {

	private static final long serialVersionUID = 1L;

	@XmlElement(name="config_AuthHostId")
	private String myAuthHostId;

	@XmlElement(name="config_AuthHostPid")
	private long myAuthHostPid;

	@XmlTransient
	private transient boolean myEditMode;

	public String getAuthHostId() {
		return myAuthHostId;
	}

	/**
	 * @return the authHostPid
	 */
	public long getAuthHostPid() {
		return myAuthHostPid;
	}
	
	public abstract ServerSecurityEnum getType();

	/**
	 * @return the editMode
	 */
	public boolean isEditMode() {
		return myEditMode;
	}

	public void setAuthHostId(String theAuthHostId) {
		myAuthHostId = theAuthHostId;
	}

	/**
	 * @param theAuthHostPid the authHostPid to set
	 */
	public void setAuthHostPid(long theAuthHostPid) {
		myAuthHostPid = theAuthHostPid;
	}
	
	/**
	 * @param theEditMode the editMode to set
	 */
	public void setEditMode(boolean theEditMode) {
		myEditMode = theEditMode;
	}

}
