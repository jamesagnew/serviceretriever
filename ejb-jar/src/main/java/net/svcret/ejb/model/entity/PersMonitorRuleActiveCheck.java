package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.enm.ThrottlePeriodEnum;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheck;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheckOutcome;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheckOutcomeList;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.ex.ProcessingException;

//@formatter:off
@Entity
@Table(name = "PX_MONITOR_RULE_ACTIVECHK")
@NamedQueries(value = { @NamedQuery(name = Queries.PERSACTIVECHECK_FINDALL, query = Queries.PERSACTIVECHECK_FINDALL_Q) })
// @formatter:on
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

	@ManyToOne(cascade = {}, optional = false)
	@JoinColumn(name = "MSG_PID", nullable = false)
	private PersLibraryMessage myMessage;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	@Access(AccessType.FIELD)
	private Long myPid;

	@OneToMany(cascade = CascadeType.REMOVE, mappedBy = "myCheck", orphanRemoval = true)
	@org.hibernate.annotations.OrderBy(clause="XACT_TIME ASC")
	private List<PersMonitorRuleActiveCheckOutcome> myRecentOutcomes;

	@ManyToOne(cascade = {}, optional = false)
	@JoinColumn(name = "RULE_PID", nullable = false)
	@ForeignKey(name="FK_PMAC_RULE")
	private PersMonitorRuleActive myRule;

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

	public PersLibraryMessage getMessage() {
		return myMessage;
	}

	@Override
	public Long getPid() {
		return myPid;
	}

	public List<PersMonitorRuleActiveCheckOutcome> getRecentOutcomes() {
		if (myRecentOutcomes==null) {
			myRecentOutcomes = new ArrayList<PersMonitorRuleActiveCheckOutcome>();
		}
		return myRecentOutcomes;
	}

	public BasePersMonitorRule getRule() {
		return myRule;
	}

	public BasePersServiceVersion getServiceVersion() {
		return myServiceVersion;
	}

	public void loadAllAssociations() {
		// nothing yet

	}

	public void loadMessageAndRule() {
		myMessage.getMessageBody().toString();
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

	public void setMessage(PersLibraryMessage theMessage) {
		myMessage = theMessage;
	}

	public void setPid(Long thePid) {
		myPid = thePid;
	}

	public void setRule(PersMonitorRuleActive theRule) {
		myRule = theRule;
	}

	public void setServiceVersion(BasePersServiceVersion theSvcVersion) {
		myServiceVersion = theSvcVersion;
	}

	public DtoMonitorRuleActiveCheck toDto(boolean theLoadDetailedStatistics) {
		DtoMonitorRuleActiveCheck retVal = new DtoMonitorRuleActiveCheck();
		retVal.setCheckFrequencyNum(getCheckFrequencyNum());
		retVal.setCheckFrequencyUnit(getCheckFrequencyUnit());
		retVal.setExpectLatencyUnderMillis(getExpectLatencyUnderMillis());
		retVal.setExpectResponseContainsText(getExpectResponseContainsText());
		retVal.setExpectResponseType(getExpectResponseType());
		retVal.setMessagePid(getMessage().getPid());
		retVal.setMessageDescription(getMessage().getDescription());
		if (getPid()!=null) {
			retVal.setPid(getPid());
		}
		retVal.setServiceVersionPid(getServiceVersion().getPid());
		
		if (theLoadDetailedStatistics) {

			// Sort recent outcomes by URL
			TreeMap<PersServiceVersionUrl, List<PersMonitorRuleActiveCheckOutcome>> outcomes = new TreeMap<PersServiceVersionUrl, List<PersMonitorRuleActiveCheckOutcome>>(new Comparator<PersServiceVersionUrl>() {
				@Override
				public int compare(PersServiceVersionUrl theO1, PersServiceVersionUrl theO2) {
					return theO1.getUrlId().compareTo(theO2.getUrlId());
				}
			});
			for (PersMonitorRuleActiveCheckOutcome next : getRecentOutcomes()) {
				if (!outcomes.containsKey(next.getImplementationUrl())){
					outcomes.put(next.getImplementationUrl(), new ArrayList<PersMonitorRuleActiveCheckOutcome>());
				}
				outcomes.get(next.getImplementationUrl()).add(next);
			}
			
			// .. and add them to the DTO
			for (Entry<PersServiceVersionUrl, List<PersMonitorRuleActiveCheckOutcome>> nextEntry : outcomes.entrySet()) {
				DtoMonitorRuleActiveCheckOutcomeList outcomeList = new DtoMonitorRuleActiveCheckOutcomeList();
				outcomeList.setUrl(nextEntry.getKey().getUrl());
				outcomeList.setUrlId(nextEntry.getKey().getUrlId());
				List<DtoMonitorRuleActiveCheckOutcome> outcomesList = new ArrayList<DtoMonitorRuleActiveCheckOutcome>();
				for (PersMonitorRuleActiveCheckOutcome next : nextEntry.getValue()) {
					outcomesList.add(next.toDto());
				}
				outcomeList.setOutcomes(outcomesList);
				retVal.getRecentOutcomesForUrl().add(outcomeList);
			}
			
		}
		
		return retVal;
	}

	public static PersMonitorRuleActiveCheck fromDto(DtoMonitorRuleActiveCheck theCheck, PersMonitorRuleActive theRule, IDao theDao) throws ProcessingException {
		PersMonitorRuleActiveCheck retVal = new PersMonitorRuleActiveCheck();
		
		retVal.setRule(theRule);
		retVal.setCheckFrequencyNum(theCheck.getCheckFrequencyNum());
		retVal.setCheckFrequencyUnit(theCheck.getCheckFrequencyUnit());
		retVal.setExpectLatencyUnderMillis(theCheck.getExpectLatencyUnderMillis());
		retVal.setExpectResponseContainsText(theCheck.getExpectResponseContainsText());
		retVal.setExpectResponseType(theCheck.getExpectResponseType());
		retVal.setMessage(theDao.getLibraryMessageByPid(theCheck.getMessagePid()));
		
		if (retVal.getMessage() == null) {
			throw new ProcessingException("Unknown message PID: " + theCheck.getMessagePid());
		}
		
		retVal.setPid(theCheck.getPidOrNull());
		retVal.setServiceVersion(theDao.getServiceVersionByPid(theCheck.getServiceVersionPid()));
		if (retVal.getServiceVersion() == null) {
			throw new ProcessingException("Unknown service version PID: " + theCheck.getServiceVersionPid());
		}
		return retVal;
	}
	
}
