package net.svcret.admin.shared.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.BaseGDashboardObjectComparator;
import net.svcret.admin.shared.util.XmlConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class GServiceList extends BaseDtoList<GService> {

	private static final long serialVersionUID = 1L;

	public GServiceList() {
		setComparator(new BaseGDashboardObjectComparator());
	}


	@XmlElement(name="Service")
	@Override
	public List<GService> getListForJaxb() {
		return super.getListForJaxb();
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

	public BaseDtoServiceVersion getFirstServiceVersion() {
		if (size()>0) {
			return get(0).getVersionList().getFirstServiceVersion();
		}
		return null;
	}

}
