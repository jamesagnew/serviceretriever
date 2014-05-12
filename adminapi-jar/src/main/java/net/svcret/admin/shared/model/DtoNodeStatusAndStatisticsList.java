package net.svcret.admin.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DtoNodeStatusAndStatisticsList implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<DtoNodeStatistics> myNodeStatistics;
	private List<DtoNodeStatus> myNodeStatuses;

	public List<DtoNodeStatistics> getNodeStatistics() {
		if (myNodeStatistics == null) {
			myNodeStatistics = new ArrayList<>();
		}
		return myNodeStatistics;
	}

	public List<DtoNodeStatus> getNodeStatuses() {
		if (myNodeStatuses == null) {
			myNodeStatuses = new ArrayList<>();
		}
		return myNodeStatuses;
	}

	public DtoNodeStatistics getNodeStatisticsForNodeId(String theNodeId) {
		for (int i =0; i < myNodeStatuses.size();i++) {
			if (myNodeStatuses.get(i).getNodeId().equals(theNodeId)) {
				return myNodeStatistics.get(i);
			}
		}
		return null;
	}

}
