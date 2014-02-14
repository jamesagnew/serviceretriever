package net.svcret.admin.shared.model;

import java.util.ArrayList;
import java.util.List;

public class DtoNodeStatusAndStatisticsList {

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

}
