package net.svcret.admin.shared.model;

import java.util.Date;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.enm.ThrottlePeriodEnum;

public class DtoMonitorRuleActiveCheck extends BaseGObject<DtoMonitorRuleActiveCheck> {

	private static final long serialVersionUID = 1L;

	private int myCheckFrequencyNum;
	private ThrottlePeriodEnum myCheckFrequencyUnit;
	private Long myExpectLatencyUnderMillis;
	private String myExpectResponseContainsText;
	private ResponseTypeEnum myExpectResponseType;
	private Date myLastTransactionDate;
	private Boolean myLastTransactionOutcome;
	private String myMessageDescription;
	private long myMessagePid;

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

	public String getMessageDescription() {
		return myMessageDescription;
	}

	public long getMessagePid() {
		return myMessagePid;
	}

	public long getServiceVersionPid() {
		return myServiceVersionPid;
	}

	@Override
	public void merge(DtoMonitorRuleActiveCheck theObject) {
		// nothing
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
