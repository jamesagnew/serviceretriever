package net.svcret.admin.shared.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.XmlConstants;

@XmlType(namespace = XmlConstants.DTO_NAMESPACE, name = "MonitorRuleActiveCheckOutcome")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoMonitorRuleActiveCheckOutcome extends BaseDtoObject {

	private static final long serialVersionUID = 1L;

	@XmlElement(name = "FailureMessage", nillable = true)
	private String myFailureMessage;

	private long myLatency;

	@XmlElement(name = "Success")
	private boolean mySuccess;

	@XmlElement(name = "Timestamp")
	private Date myTimestamp;

	public String getFailureMessage() {
		return myFailureMessage;
	}

	public long getLatency() {
		return myLatency;
	}

	public Date getTimestamp() {
		return myTimestamp;
	}

	public boolean isSuccess() {
		return mySuccess;
	}

	public void setFailureMessage(String theFailureMessage) {
		myFailureMessage = theFailureMessage;
	}

	public void setLatency(long theLatency) {
		myLatency = theLatency;
	}

	public void setSuccess(boolean theSuccess) {
		mySuccess = theSuccess;
	}

	public void setTimestamp(Date theTimestamp) {
		myTimestamp = theTimestamp;
	}

}
