package net.svcret.admin.shared.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.XmlConstants;

@XmlType(namespace = XmlConstants.DTO_NAMESPACE, name = "MonitorRuleActiveCheckOutcome")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoMonitorRuleActiveCheckOutcome extends BaseDtoSavedTransaction {

	private static final long serialVersionUID = 1L;

	@XmlElement(name = "Failed")
	private boolean myFailed;


	public boolean isFailed() {
		return myFailed;
	}


	public void setFailed(boolean theFailed) {
		myFailed = theFailed;
	}

}
