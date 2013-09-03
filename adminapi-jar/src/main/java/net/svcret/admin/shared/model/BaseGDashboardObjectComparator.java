package net.svcret.admin.shared.model;

import java.io.Serializable;
import java.util.Comparator;

import net.svcret.admin.shared.util.StringUtil;

public class BaseGDashboardObjectComparator implements Comparator<BaseGDashboardObject>, Serializable {

	private static final long serialVersionUID = 7039040153939517911L;

	@Override
	public int compare(BaseGDashboardObject theO1, BaseGDashboardObject theO2) {
		return StringUtil.compare(theO1.getName(), theO2.getName());
	}

}
