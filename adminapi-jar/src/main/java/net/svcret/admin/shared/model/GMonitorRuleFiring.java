package net.svcret.admin.shared.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GMonitorRuleFiring extends BaseGObject<GMonitorRuleFiring> {

	private static final long serialVersionUID = 1L;
	
	private Date myEndDate;
	private List<GMonitorRuleFiringProblem> myProblems;
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

	public Date getStartDate() {
		return myStartDate;
	}

	@Override
	public void merge(GMonitorRuleFiring theObject) {
		throw new UnsupportedOperationException();
	}

	public void setEndDate(Date theEndDate) {
		myEndDate = theEndDate;
	}

	public void setStartDate(Date theStartDate) {
		myStartDate = theStartDate;
	}

}
