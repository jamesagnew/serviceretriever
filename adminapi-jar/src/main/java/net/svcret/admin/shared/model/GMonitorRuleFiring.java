package net.svcret.admin.shared.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GMonitorRuleFiring extends BaseDtoObject {

	private static final long serialVersionUID = 1L;

	private Date myEndDate;
	private List<GMonitorRuleFiringProblem> myProblems;
	private long myRulePid;
	private Date myStartDate;

	public Date getEndDate() {
		return myEndDate;
	}

	public List<GMonitorRuleFiringProblem> getProblems() {
		if (myProblems == null) {
			myProblems = new ArrayList<GMonitorRuleFiringProblem>();
		}
		return myProblems;
	}

	public long getRulePid() {
		return myRulePid;
	}

	public Date getStartDate() {
		return myStartDate;
	}

	@Override
	public void merge(BaseDtoObject theObject) {
		throw new UnsupportedOperationException();
	}

	public void setEndDate(Date theEndDate) {
		myEndDate = theEndDate;
	}

	public void setRulePid(long theRulePid) {
		myRulePid = theRulePid;
	}

	public void setStartDate(Date theStartDate) {
		myStartDate = theStartDate;
	}

}
