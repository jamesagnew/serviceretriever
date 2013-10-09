package net.svcret.admin.shared.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import net.svcret.admin.shared.util.BaseGDashboardObjectComparator;

@XmlAccessorType(XmlAccessType.NONE)
public class GServiceVersionList extends BaseDtoList<BaseDtoServiceVersion> {

	private static final long serialVersionUID = 1L;

	public GServiceVersionList() {
		setComparator(new BaseGDashboardObjectComparator());
	}

	@XmlElement(name="Version")
	@Override
	public List<BaseDtoServiceVersion> getListForJaxb() {
		return super.getListForJaxb();
	}

	public BaseDtoServiceVersion getVersionByPid(long thePid) {
		for (BaseDtoServiceVersion next : this) {
			if  (next.getPid() == thePid) {
				return next;
			}
		}
		return null;
	}

	public BaseDtoServiceVersion getFirstServiceVersion() {
		if (size() >0) {
			return get(0);
		}
		return null;
	}

	
	
}
