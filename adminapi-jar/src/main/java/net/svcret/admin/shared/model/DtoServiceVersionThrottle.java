package net.svcret.admin.shared.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class DtoServiceVersionThrottle extends GThrottle {

	private static final long serialVersionUID = 1L;

	@XmlElement(name = "config_ApplyPerUser")
	private boolean myApplyPerUser;
	@XmlElement(name = "config_ApplyPropertyCaptureName")
	private String myApplyPropCapName;

	public String getApplyPropCapName() {
		return myApplyPropCapName;
	}


	public boolean isApplyPerUser() {
		return myApplyPerUser;
	}

	public void setApplyPerUser(boolean theApplyPerUser) {
		myApplyPerUser = theApplyPerUser;
	}

	public void setApplyPropCapName(String theApplyPropCapName) {
		myApplyPropCapName = theApplyPropCapName;
	}


}
