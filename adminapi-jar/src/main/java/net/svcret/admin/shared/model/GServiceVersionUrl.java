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

	private String myUrl;
	private Date myStatsLastFailure;
	private String myStatsLastFailureMessage;
	private Date myStatsLastFault;
	private String myStatsLastFaultMessage;
	private Date myStatsLastSuccess;
	private String myStatsLastSuccessMessage;

	public GServiceVersionUrl() {
		// nothing
	}

	public GServiceVersionUrl(String theId, String theUrl) {
		setId(theId);
		myUrl = theUrl;
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
		
		GServiceVersionUrl obj = (GServiceVersionUrl)theObject;

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

	public Date getStatsLastFailure() {
		return myStatsLastFailure;
	}

	public void setStatsLastFailure(Date theStatsLastFailure) {
		myStatsLastFailure = theStatsLastFailure;
	}

	public String getStatsLastFailureMessage() {
		return myStatsLastFailureMessage;
	}

	public void setStatsLastFailureMessage(String theStatsLastFailureMessage) {
		myStatsLastFailureMessage = theStatsLastFailureMessage;
	}

	public Date getStatsLastFault() {
		return myStatsLastFault;
	}

	public void setStatsLastFault(Date theStatsLastFault) {
		myStatsLastFault = theStatsLastFault;
	}

	public String getStatsLastFaultMessage() {
		return myStatsLastFaultMessage;
	}

	public void setStatsLastFaultMessage(String theStatsLastFaultMessage) {
		myStatsLastFaultMessage = theStatsLastFaultMessage;
	}

	public Date getStatsLastSuccess() {
		return myStatsLastSuccess;
	}

	public void setStatsLastSuccess(Date theStatsLastSuccess) {
		myStatsLastSuccess = theStatsLastSuccess;
	}

	public String getStatsLastSuccessMessage() {
		return myStatsLastSuccessMessage;
	}

	public void setStatsLastSuccessMessage(String theStatsLastSuccessMessage) {
		myStatsLastSuccessMessage = theStatsLastSuccessMessage;
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

	
	/**
	 * @param theUrl
	 *            the url to set
	 */
	public void setUrl(String theUrl) {
		myUrl = theUrl;
	}

}
