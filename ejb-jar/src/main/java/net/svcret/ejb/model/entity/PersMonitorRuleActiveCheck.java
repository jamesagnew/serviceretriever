package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.enm.ThrottlePeriodEnum;

//@formatter:off
@Entity
@Table(name = "PX_MONITOR_RULE_ACTIVECHK")
@NamedQueries(value= {
		@NamedQuery(name=Queries.PERSACTIVECHECK_FINDALL, query=Queries.PERSACTIVECHECK_FINDALL_Q)
	})
//@formatter:on
public class PersMonitorRuleActiveCheck extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@Column(name = "CHECK_FREQ_NUM", nullable = false)
	private int myCheckFrequencyNum;

	@Column(name = "CHECK_FREQ_UNIT", nullable = false, length = EntityConstants.MAXLEN_THROTTLE_PERIOD_ENUM)
	@Enumerated(EnumType.STRING)
	private ThrottlePeriodEnum myCheckFrequencyUnit;

	@Column(name = "EXPECT_LATENCY_MILLIS", nullable = true)
	private Long myExpectLatencyUnderMillis;

	@Column(name = "EXPECT_RESP_TEXT", length = 200, nullable = true)
	private String myExpectResponseContainsText;

	@Column(name = "EXPECT_RESP_TYPE", length = EntityConstants.MAXLEN_RESPONSE_TYPE_ENUM, nullable = false)
	private ResponseTypeEnum myExpectResponseType;

	@Column(name = "LASTXACT_DATE", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date myLastTransactionDate;

	@Column(name = "LASTXACT_OUTCOME", nullable = true)
	private Boolean myLastTransactionOutcome;

	@ManyToOne(cascade = {}, optional = false)
	@JoinColumn(name = "MSG_PID", nullable = false)
	private PersLibraryMessage myMessage;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@ManyToOne(cascade = {}, optional = false)
	@JoinColumn(name = "RULE_PID", nullable = false)
	private BasePersMonitorRule myRule;

	@ManyToOne(cascade = {}, optional = false)
	@JoinColumn(name = "SVCVER_PID", nullable = false)
	private BasePersServiceVersion myServiceVersion;

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

	public PersLibraryMessage getMessage() {
		return myMessage;
	}

	@Override
	public Long getPid() {
		return myPid;
	}

	public BasePersMonitorRule getRule() {
		return myRule;
	}

	public BasePersServiceVersion getServiceVersion() {
		return myServiceVersion;
	}

	public void loadMessageAndRule() {
		myMessage.getMessage().toString();
		myRule.toString();
	}

	public void merge(PersMonitorRuleActiveCheck theCheck) {
		// Don't merge stats
		setCheckFrequencyNum(theCheck.getCheckFrequencyNum());
		setCheckFrequencyUnit(theCheck.getCheckFrequencyUnit());
		setExpectLatencyUnderMillis(theCheck.getExpectLatencyUnderMillis());
		setExpectResponseContainsText(theCheck.getExpectResponseContainsText());
		setExpectResponseType(theCheck.getExpectResponseType());
		setMessage(theCheck.getMessage());
		setServiceVersion(theCheck.getServiceVersion());
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

	public void setMessage(PersLibraryMessage theMessage) {
		myMessage = theMessage;
	}

	public void setPid(Long thePid) {
		myPid = thePid;
	}

	public void setRule(BasePersMonitorRule theRule) {
		myRule = theRule;
	}

	public void setServiceVersion(BasePersServiceVersion theSvcVersion) {
		myServiceVersion = theSvcVersion;
	}

}
