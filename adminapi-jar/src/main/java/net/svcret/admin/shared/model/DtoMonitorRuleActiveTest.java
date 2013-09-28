package net.svcret.admin.shared.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.enm.ThrottlePeriodEnum;

@XmlAccessorType(XmlAccessType.FIELD)
public class DtoMonitorRuleActiveTest extends BaseDtoObject {

	private static final long serialVersionUID = 1L;

	@XmlElement(name = "CheckFrequencyNumber")
	private int myCheckFrequencyNum;
	@XmlElement(name = "CheckFrequencyUnit")
	private ThrottlePeriodEnum myCheckFrequencyUnit;
	@XmlElement(name = "ExpectLatencyUnderMillis")
	private Long myExpectLatencyUnderMillis;
	@XmlElement(name = "ExpectResponseContainsText")
	private String myExpectResponseContainsText;
	@XmlElement(name = "ExpectResponseType")
	private ResponseTypeEnum myExpectResponseType;
	@XmlElement(name = "LastTransactionDate")
	private Date myLastTransactionDate;
	@XmlElement(name = "LastTransactionOutcome")
	private Boolean myLastTransactionOutcome;
	@XmlElement(name = "MessagePid")
	private long myMessagePid;
	@XmlElement(name = "Pid")
	private long myPid;
	@XmlElement(name = "ServiceVersionPid")
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

	public Date getLastTransactionDate() {
		return myLastTransactionDate;
	}

	public Boolean getLastTransactionOutcome() {
		return myLastTransactionOutcome;
	}

	public long getMessagePid() {
		return myMessagePid;
	}

	public long getPid() {
		return myPid;
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

	public void setLastTransactionDate(Date theLastTransactionDate) {
		myLastTransactionDate = theLastTransactionDate;
	}

	public void setLastTransactionOutcome(Boolean theLastTransactionOutcome) {
		myLastTransactionOutcome = theLastTransactionOutcome;
	}

	public void setMessagePid(long theMessagePid) {
		myMessagePid = theMessagePid;
	}

	public void setPid(long thePid) {
		myPid = thePid;
	}

	public void setServiceVersionPid(long theServiceVersionPid) {
		myServiceVersionPid = theServiceVersionPid;
	}

}
