package net.svcret.admin.shared.model;

import net.svcret.admin.shared.util.BaseGDashboardObjectComparator;


public class GServiceMethodList extends BaseDtoList<DtoMethod>{

	private static final long serialVersionUID = 1L;

	public GServiceMethodList() {
		setComparator(new BaseGDashboardObjectComparator());
	}




}
