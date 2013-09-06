package net.svcret.admin.shared.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.XmlConstants;

@XmlType(namespace=XmlConstants.DTO_NAMESPACE, name="ServiceList")
@XmlAccessorType(XmlAccessType.FIELD)
public class GServiceList extends BaseDtoList<GService> {

	private static final long serialVersionUID = 1L;

	public GServiceList() {
		setComparator(new BaseGDashboardObjectComparator());
	}

	public GService getServiceById(String theServiceId) {
		for (GService next : this) {
			if (next.getId().equals(theServiceId)) {
				return next;
			}
		}
		return null;
	}

	public GService getServiceByPid(long theServicePid) {
		for (GService next : this) {
			if (next.getPid() == theServicePid) {
				return next;
			}
		}
		return null;
	}

	public BaseGServiceVersion getFirstServiceVersion() {
		if (size()>0) {
			return get(0).getVersionList().getFirstServiceVersion();
		}
		return null;
	}

}
