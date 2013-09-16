package net.svcret.admin.shared.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.enm.ThrottlePeriodEnum;
import net.svcret.admin.shared.util.XmlConstants;

@XmlType(namespace = XmlConstants.DTO_NAMESPACE, name = "MonitorRuleActiveCheck")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoMonitorRuleActiveCheck extends BaseGObject {

	private static final long serialVersionUID = 1L;

	@XmlElement(name = "config_CheckFrequencyNumber")
	private int myCheckFrequencyNum;
	@XmlElement(name = "config_CheckFrequencyUnit")
	private ThrottlePeriodEnum myCheckFrequencyUnit;
	@XmlElement(name = "config_ExpectLatencyUnderMillis")
	private Long myExpectLatencyUnderMillis;
	@XmlElement(name = "config_ExpectResponseContainsText")
	private String myExpectResponseContainsText;
	@XmlElement(name = "config_ExpectResponseType")
	private ResponseTypeEnum myExpectResponseType;
	@XmlElement(name = "runtime_MessageDescription")
	private String myMessageDescription;
	@XmlElement(name = "config_MessagePid")
	private long myMessagePid;
	@XmlElement(name = "runtime_RecentOutcomesForUrl")
	private List<DtoMonitorRuleActiveCheckOutcomeList> myRecentOutcomesForUrl;
	@XmlElement(name = "runtime_ServiceVersionPid")
	private long myServiceVersionPid;

	public int getCheckFrequencyNum() {
		return myCheckFrequencyNum;
	}

	public ThrottlePeriodEnum getCheckFrequencyUnit() {
		return myCheckFrequencyUnit;
	}

	public Long getExpectLatencyUnderMillis() {
		return myExpectLatencyUnderMillis;
	}

	public String getExpectResponseContainsText() {
		return myExpectResponseContainsText;
	}

	public ResponseTypeEnum getExpectResponseType() {
		return myExpectResponseType;
	}

	public String getMessageDescription() {
		return myMessageDescription;
	}

	public long getMessagePid() {
		return myMessagePid;
	}

	public List<DtoMonitorRuleActiveCheckOutcomeList> getRecentOutcomesForUrl() {
		if (myRecentOutcomesForUrl == null) {
			myRecentOutcomesForUrl = new ArrayList<DtoMonitorRuleActiveCheckOutcomeList>();
		}
		return myRecentOutcomesForUrl;
	}

	public long getServiceVersionPid() {
		return myServiceVersionPid;
	}

	public void setCheckFrequencyNum(int theCheckFrequencyNum) {
		myCheckFrequencyNum = theCheckFrequencyNum;
	}

	public void setCheckFrequencyUnit(ThrottlePeriodEnum theCheckFrequencyUnit) {
		myCheckFrequencyUnit = theCheckFrequencyUnit;
	}

	public void setExpectLatencyUnderMillis(Long theExpectLatencyUnderMillis) {
		myExpectLatencyUnderMillis = theExpectLatencyUnderMillis;
	}

	public void setExpectResponseContainsText(String theExpectResponseContainsText) {
		myExpectResponseContainsText = theExpectResponseContainsText;
	}

	public void setExpectResponseType(ResponseTypeEnum theExpectResponseType) {
		myExpectResponseType = theExpectResponseType;
	}

	public void setMessageDescription(String theDescription) {
		myMessageDescription = theDescription;
	}

	public void setMessagePid(long theMessagePid) {
		myMessagePid = theMessagePid;
	}

	public void setServiceVersionPid(long theServiceVersionPid) {
		myServiceVersionPid = theServiceVersionPid;
	}

}
