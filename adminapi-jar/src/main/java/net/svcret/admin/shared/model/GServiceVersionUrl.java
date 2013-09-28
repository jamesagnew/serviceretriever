package net.svcret.admin.shared.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.util.ComparableComparator;
import net.svcret.admin.shared.util.InverseComparator;

@XmlAccessorType(XmlAccessType.FIELD)
public class GServiceVersionUrl extends BaseDtoDashboardObject {

	private static final long serialVersionUID = 1L;

	@XmlElement(name = "ServiceVersionPid")
	private long myServiceVersionPid;
	@XmlElement(name = "runtime_StatsLastFailure")
	private Date myStatsLastFailure;
	@XmlElement(name = "runtime_StatsLastFailureContentType")
	private String myStatsLastFailureContentType;
	@XmlElement(name = "runtime_StatsLastFailureMessage")
	private String myStatsLastFailureMessage;
	@XmlElement(name = "runtime_StatsLastFailureStatusCode")
	private Integer myStatsLastFailureStatusCode;
	@XmlElement(name = "runtime_StatsLastFault")
	private Date myStatsLastFault;
	@XmlElement(name = "runtime_StatsLastFaultContentType")
	private String myStatsLastFaultContentType;
	@XmlElement(name = "runtime_StatsLastFaultMessage")
	private String myStatsLastFaultMessage;
	@XmlElement(name = "runtime_StatsLastFaultStatusCode")
	private Integer myStatsLastFaultStatusCode;
	@XmlElement(name = "runtime_StatsLastSuccess")
	private Date myStatsLastSuccess;
	@XmlElement(name = "runtime_StatsLastSuccessContentType")
	private String myStatsLastSuccessContentType;
	@XmlElement(name = "runtime_StatsLastSuccessMessage")
	private String myStatsLastSuccessMessage;
	@XmlElement(name = "runtime_StatsLastSuccessStatusCode")
	private Integer myStatsLastSuccessStatusCode;
	@XmlElement(name = "runtime_StatsNextCircuitBreakerReset")
	private Date myStatsNextCircuitBreakerReset;
	@XmlElement(name = "config_Url")
	private String myUrl;

	public GServiceVersionUrl() {
		// nothing
	}

	public GServiceVersionUrl(String theId, String theUrl) {
		setId(theId);
		myUrl = theUrl;
	}

	public long getServiceVersionPid() {
		return myServiceVersionPid;
	}

	public Date getStatsLastFailure() {
		return myStatsLastFailure;
	}

	public String getStatsLastFailureContentType() {
		return myStatsLastFailureContentType;
	}

	public String getStatsLastFailureMessage() {
		return myStatsLastFailureMessage;
	}

	public Integer getStatsLastFailureStatusCode() {
		return myStatsLastFailureStatusCode;
	}

	public Date getStatsLastFault() {
		return myStatsLastFault;
	}

	public String getStatsLastFaultContentType() {
		return myStatsLastFaultContentType;
	}

	public String getStatsLastFaultMessage() {
		return myStatsLastFaultMessage;
	}

	public Integer getStatsLastFaultStatusCode() {
		return myStatsLastFaultStatusCode;
	}

	public ResponseTypeEnum getStatsLastResponseType() {
		TreeMap<Date, ResponseTypeEnum> responses = new TreeMap<Date, ResponseTypeEnum>(new InverseComparator<Date>(new ComparableComparator<Date>()));
		if (myStatsLastFailure != null) {
			responses.put(myStatsLastFailure, ResponseTypeEnum.FAIL);
		}
		if (myStatsLastSuccess != null) {
			responses.put(myStatsLastSuccess, ResponseTypeEnum.SUCCESS);
		}
		if (myStatsLastFault != null) {
			responses.put(myStatsLastFault, ResponseTypeEnum.FAULT);
		}
		if (responses.size() == 0) {
			return null;
		}
		return responses.get(responses.keySet().iterator().next());
	}

	public List<ResponseTypeEnum> getStatsLastResponseTypesFromMostRecentToLeast() {
		TreeMap<Date, ResponseTypeEnum> responses = new TreeMap<Date, ResponseTypeEnum>(new InverseComparator<Date>(new ComparableComparator<Date>()));
		if (myStatsLastFailure != null) {
			responses.put(myStatsLastFailure, ResponseTypeEnum.FAIL);
		}
		if (myStatsLastSuccess != null) {
			responses.put(myStatsLastSuccess, ResponseTypeEnum.SUCCESS);
		}
		if (myStatsLastFault != null) {
			responses.put(myStatsLastFault, ResponseTypeEnum.FAULT);
		}
		return new ArrayList<ResponseTypeEnum>(responses.values());
	}

	public Date getStatsLastSuccess() {
		return myStatsLastSuccess;
	}

	public String getStatsLastSuccessContentType() {
		return myStatsLastSuccessContentType;
	}

	public String getStatsLastSuccessMessage() {
		return myStatsLastSuccessMessage;
	}

	public Integer getStatsLastSuccessStatusCode() {
		return myStatsLastSuccessStatusCode;
	}

	public Date getStatsNextCircuitBreakerReset() {
		return myStatsNextCircuitBreakerReset;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return myUrl;
	}

	@Override
	public void merge(BaseDtoObject theObject) {
		super.merge(theObject);

		setPid(theObject.getPid());

		GServiceVersionUrl obj = (GServiceVersionUrl) theObject;

		setUrl(obj.getUrl());

		if (obj.isStatsInitialized()) {
			setStatsLastSuccess(obj.getStatsLastSuccess());
			setStatsLastSuccessMessage(obj.getStatsLastSuccessMessage());
			setStatsLastFailure(obj.getStatsLastFailure());
			setStatsLastFailureMessage(obj.getStatsLastFailureMessage());
			setStatsLastFault(obj.getStatsLastFault());
			setStatsLastFaultMessage(obj.getStatsLastFaultMessage());
		}
	}

	public void setServiceVersionPid(long theServiceVersionPid) {
		myServiceVersionPid = theServiceVersionPid;
	}

	public void setStatsLastFailure(Date theStatsLastFailure) {
		myStatsLastFailure = theStatsLastFailure;
	}

	public void setStatsLastFailureContentType(String theStatsLastFailureContentType) {
		myStatsLastFailureContentType = theStatsLastFailureContentType;
	}

	public void setStatsLastFailureMessage(String theStatsLastFailureMessage) {
		myStatsLastFailureMessage = theStatsLastFailureMessage;
	}

	public void setStatsLastFailureStatusCode(Integer theStatsLastFailureStatusCode) {
		myStatsLastFailureStatusCode = theStatsLastFailureStatusCode;
	}

	public void setStatsLastFault(Date theStatsLastFault) {
		myStatsLastFault = theStatsLastFault;
	}

	public void setStatsLastFaultContentType(String theStatsLastFaultContentType) {
		myStatsLastFaultContentType = theStatsLastFaultContentType;
	}

	public void setStatsLastFaultMessage(String theStatsLastFaultMessage) {
		myStatsLastFaultMessage = theStatsLastFaultMessage;
	}

	public void setStatsLastFaultStatusCode(Integer theStatsLastFaultStatusCode) {
		myStatsLastFaultStatusCode = theStatsLastFaultStatusCode;
	}

	public void setStatsLastSuccess(Date theStatsLastSuccess) {
		myStatsLastSuccess = theStatsLastSuccess;
	}

	public void setStatsLastSuccessContentType(String theStatsLastSuccessContentType) {
		myStatsLastSuccessContentType = theStatsLastSuccessContentType;
	}

	public void setStatsLastSuccessMessage(String theStatsLastSuccessMessage) {
		myStatsLastSuccessMessage = theStatsLastSuccessMessage;
	}

	public void setStatsLastSuccessStatusCode(Integer theStatsLastSuccessStatusCode) {
		myStatsLastSuccessStatusCode = theStatsLastSuccessStatusCode;
	}

	public void setStatsNextCircuitBreakerReset(Date theNextCircuitBreakerReset) {
		myStatsNextCircuitBreakerReset = theNextCircuitBreakerReset;
	}

	/**
	 * @param theUrl
	 *            the url to set
	 */
	public void setUrl(String theUrl) {
		myUrl = theUrl;
	}

}
