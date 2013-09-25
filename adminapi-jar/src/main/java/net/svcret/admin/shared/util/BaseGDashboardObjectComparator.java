package net.svcret.admin.shared.util;

import java.io.Serializable;
import java.util.Comparator;

import net.svcret.admin.shared.model.BaseDtoDashboardObject;

public class BaseGDashboardObjectComparator implements Comparator<BaseDtoDashboardObject>, Serializable {

	private static final long serialVersionUID = 7039040153939517911L;

	@Override
	public int compare(BaseDtoDashboardObject theO1, BaseDtoDashboardObject theO2) {
		return StringUtil.compare(theO1.getName(), theO2.getName());
	}

}
