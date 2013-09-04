package net.svcret.admin.shared.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.util.ComparableComparator;
import net.svcret.admin.shared.util.InverseComparator;

public class GServiceVersionUrl extends BaseGDashboardObject {

	private static final long serialVersionUID = 1L;

	private Date myStatsLastFailure;
	private String myStatsLastFailureContentType;
	private String myStatsLastFailureMessage;
	private Integer myStatsLastFailureStatusCode;
	private Date myStatsLastFault;
	private String myStatsLastFaultContentType;
	private String myStatsLastFaultMessage;
	private Integer myStatsLastFaultStatusCode;
	private Date myStatsLastSuccess;
	private String myStatsLastSuccessContentType;
	private String myStatsLastSuccessMessage;
	private Integer myStatsLastSuccessStatusCode;
	private Date myStatsNextCircuitBreakerReset;
	private String myUrl;

	public GServiceVersionUrl() {
		// nothing
	}

	public GServiceVersionUrl(String theId, String theUrl) {
		setId(theId);
		myUrl = theUrl;
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
	public void merge(BaseGObject theObject) {
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
